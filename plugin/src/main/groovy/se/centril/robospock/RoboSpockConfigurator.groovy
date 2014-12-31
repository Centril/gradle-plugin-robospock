/*
 * Copyright 2014 Centril / Mazdak Farrokhzad <twingoow@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.centril.robospock

import com.jakewharton.sdkmanager.internal.PackageResolver

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.plugins.JavaBasePlugin

import static java.util.regex.Pattern.quote
import static java.beans.Introspector.decapitalize

import static se.centril.robospock.internal.RoboSpockUtils.*
import static se.centril.robospock.internal.RoboSpockConstants.*
import se.centril.robospock.internal.VariantImpl

import se.centril.robospock.graph.DirectedAcyclicGraph
import se.centril.robospock.graph.internal.DirectedAcyclicGraphImpl
import se.centril.robospock.graph.internal.UnmodifiableDAGImpl

/**
 * {@link RoboSpockConfigurator}: Is the heart of the plugin,
 * this is where all the action happens.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-11-19
 */
public class RoboSpockConfigurator {
	RoboSpockConfiguration cfg

	//================================================================================
	// Public API:
	//================================================================================

	/**
	 * Constructs a configurator.
	 *
	 * @param config a {@link RoboSpockConfiguration} object to configure with.
	 */
	public RoboSpockConfigurator( RoboSpockConfiguration config ) {
		this.cfg = config
	}

	/**
	 * Configures everything.
	 */
	public void configure() {
		[cfg.&verify,
		 this.&evaluationOrder, this.&applyGroovy, this.&addJCenter,
		 this.&addAndroidRepositories, this.&addDependencies, this.&fixSupportLib,
		 this.&setupGraph, this.&copyAndroidDependencies, this.&fixRobolectricBugs,
		 cfg.&executeAfterConfigured]
		 	.each { it() }
	}

	/**
	 * Adds the jcenter() to buildscript repositories.
	 *
	 * @param  cfg the configuration.
	 */
	public static void addJCenterBuildScript( RoboSpockConfiguration cfg ) {
		cfg.perspective.buildscript {
			repositories {
				jcenter()
			}
		}
	}

	//================================================================================
	// Private API:
	//================================================================================

	/**
	 * Makes the tester projects evaluation
	 * depends on the android project.
	 */
	private void evaluationOrder() {
		cfg.tester.evaluationDependsOn( cfg.android.path )
	}

	/**
	 * Applies the groovy plugin to the tester.
	 */
	private void applyGroovy() {
		cfg.tester.apply plugin: 'groovy'
	}

	/**
	 * Makes sure that android-sdk-manager pulls support libs.
	 */
	private void fixSupportLib() {
		// Roboelectric needs this, make com.jakewharton.sdkmanager download it.
		testCompile( MAVEN_ANDROID_SUPPORT )
		cfg.tester.ext.android = cfg.android.android

		/*
		 * Fix issue: https://github.com/Centril/gradle-plugin-robospock/issues/6
		 * TODO: Remove once in upstream of com.jakewharton.sdkmanager.
		 *
		 * GroovyCastException: Cannot cast object '21.1.0' with class
		 * 'com.android.sdklib.repository.FullRevision' to class
		 * 'com.android.sdklib.repository.FullRevision'
		 */
		PackageResolver.metaClass.resolveBuildTools { ->
			def buildToolsRevision = project.android.buildToolsRevision
			log.debug "Build tools version: $buildToolsRevision"

			def buildToolsRevisionDir = new File( buildToolsDir, buildToolsRevision.toString() )
			if ( folderExists( buildToolsRevisionDir ) ) {
				log.debug 'Build tools found!'
				return
			}

			log.lifecycle "Build tools $buildToolsRevision missing. Downloading..."

			def code = androidCommand.update "build-tools-$buildToolsRevision"
			if ( code != 0 ) {
				throw new StopExecutionException( "Build tools download failed with code $code." )
			}
		}

		PackageResolver.resolve cfg.tester, cfg.sdkDir()
	}

	/**
	 * Adds the jcenter() to repositories.
	 */
	private void addJCenter() {
		cfg.tester.repositories {
			jcenter()
		}
	}

	/**
	 * Adds all the dependencies of this configuration to {@link Project}.
	 */
	private void addDependencies() {
		testCompile( cfg.version.dependencies() )
	}

	/**
	 * Adds the android SDK dir repositories to {@link RoboSpockConfiguration#android}.
	 */
	private void addAndroidRepositories() {
		def sdkDir = cfg.sdkDir()
		MAVEN_ANDROID_REPOS.each { path ->
			cfg.tester.repositories {
				maven { url new File( sdkDir, path ).toURI().toString() }
			}
		}
	}

	/**
	 * Sets up the variant graph.
	 */
	private void setupGraph() {
		def p = cfg.tester,
			vars = cfg.variants,
			// The graph:
			dag = new DirectedAcyclicGraphImpl<RoboSpockVariant>(),
			// Maps from name -> TV.
			mapping = [:],
			extend = { List<Object> bases, Object vName, RoboSpockVariant vertex ->
				mapping[vName] = vertex
				dag.add( vertex )
				bases.each { b ->
					def base = b instanceof VariantImpl ? b : mapping[b]
					dag.add( base, vertex )
					vertex.extendFrom( p, base )
				}
				return vertex
			}

		/*
		 * Root:
		 * Make check depend on root.
		 * Remove all actions on test & make it basically do root.
		 */
		def root = extend( [], 'root', new VariantImpl( p, p.sourceSets.test, '', null ) )
		p.tasks.getByName( JavaBasePlugin.CHECK_TASK_NAME ).dependsOn( root.task )
		p.test {
			deleteAllActions()
			dependsOn root.task
		}

		/*
		 * Variant that is a build type:
		 * Android plugin creates variants with same
		 * name as build types, process this later.
		 */
		boolean notBtVariant = true
		for ( def v : vars ) {
			if ( v.name in cfg.buildTypes ) {
				notBtVariant = false
				break
			}
		}

		/*
		 * For each build type:
		 */
		if ( notBtVariant ) {
			cfg.buildTypes.each { bt ->
				extend( [root], bt, new VariantImpl( p, bt, TASK_DESCRIPTION_BUILD_TYPE + bt ) )
			}
		}

		/*
		 * Add for each PF = product flavor:
		 * Library projects don't have PFs.
		 */
		cfg.variants.collectMany { it.productFlavors }.unique().each { pf ->
			def vPf = extend( [root], pf, new VariantImpl( p, pf, TASK_DESCRIPTION_PRODUCT_FLAVOR + pf.name ) )
			// Add for each T = (PF, BT):
			cfg.buildTypes.each { bt ->
				def name = pf.name + bt.capitalize()
				extend( [bt, vPf], name, new VariantImpl( p, name, TASK_DESCRIPTION_PF_BT + name ) )
			}
		}

		/*
		 * Add for each variant.
		 */
		vars.each { var ->
			def bt = var.buildType.name,
				v = new VariantImpl( p, cfg, var )

			// Either extend from the BT or every T = (PF, BT).
			// In the latter case, since T extends BT, extending T => extending BT:
			if ( var.productFlavors.isEmpty() ) {
				extend( [mapping[bt] ?: root], var.name, v )
			} else {
				var.productFlavors.each { pf ->
					extend( [pf.name + bt.capitalize()], var.name, v )
				}
			}
		}

		/*
		 * Bind unmodifiable version -> configuration.
		 */
		cfg.graph = new UnmodifiableDAGImpl<RoboSpockVariant>( dag )
	}

	/**
	 * Fixes/addresses various bugs in robolectric.
	 */
	private void fixRobolectricBugs() {
		def android = cfg.android,
			aBuildDir = "${android.buildDir}",
			tBuildDir = "${cfg.tester.buildDir}"

		variants().each { vertex ->
			def v = vertex.variant,
				dir = v.dirName,
				copier = android.tasks.create(
					name: RE_FIXTASK_BASE + v.name.capitalize(),
					group: RE_FIXTASK_GROUP
				)

			v.getOutputs()[0].processResources.finalizedBy copier
			copier << {
				// Library: Copy manifest, MANIFEST_PATH -> BUNDLES_PATH
				def bundlesPath = "$aBuildDir/$BUNDLES_PATH/$dir/"
				android.copy {
					from( bundlesPath ) { include MANIFEST_FILE }
					into( "$aBuildDir/$MANIFEST_PATH/$dir/" )
				}

				// Manifest: Clamp any SDK VERSION in the eyes of
				// roboelectric to API level ANDROID_FIX_VERSION.
				android.copy {
					from( aBuildDir ) { include "$MANIFEST_PATH/$dir/$MANIFEST_FILE" }
					into( tBuildDir )
					filter { clampTargetVersion( it ) }
				}

				// Library: Copy BUNDLES_PATH -> RES_PATH
				android.copy {
					from( "$bundlesPath/$RES_DIR" )
					into( "$aBuildDir/$RES_PATH/$dir/" )
				}
			}
		}
	}

	/**
	 * Clamps the targetSdkVersion attribute in manifest.
	 *
	 * @param  content the contents of manifest as string.
	 * @return         the contents but with targetSdkVersion attribute changed.
	 */
	private String clampTargetVersion( String content ) {
		def	attr = ANDROID_TARGET_VERSION
		def	(start, end) = attr
		def	(sq, eq) = attr.collect { quote( it ) }
		def	pattern = ~/$sq(\d{1,2})$eq/
		content.replaceFirst( pattern, { r ->
			start + clampTargetVersion( Integer.parseInt( r[1] ) ) + end
		} )
	}

	/**
	 * Clamps the targetSdkVersion.
	 *
	 * @param v the targetSdkVersion.
	 * @return  the clamped targetSdkVersion.
	 */
	private int clampTargetVersion( int v ) {
		v > ANDROID_FIX_VERSION ? ANDROID_FIX_VERSION : v
	}

	/**
	 * Copies the android project dependencies to this project.
	 */
	private void copyAndroidDependencies() {
		def compileTJ = cfg.tester.tasks.compileTestJava,
			android = cfg.android,
			buildDir = android.buildDir,
			aarPath = new File( buildDir, AAR_PATH ),
			libsPath = new File( buildDir, LIBS_PATH )

		// testCompile will end up in testCompile of variants transitively.
		testCompile( librarySubprojects( android, 'compile' ) )

		// Doing this for every variant in graph!
		variants().each { var ->
			def v = var.variant

			// First zipify the android project iself:
			// Create zip2jar task if not present & make compileJava depend on it.
			def jarPath = new File( libsPath, v.dirName )
			Task jarTask = android.tasks.create(
				name: JAR_TASK_BASE + v.name.capitalize(),
				group: JAR_TASK_GROUP,
				description: JAR_TASK_DESCRIPTION,
				type: Zip ) {
				dependsOn v.javaCompile
				from new File( buildDir, CLASSES_PATH + v.dirName )
				destinationDir = jarPath
				extension = JAR_EXT
			}
			compileTJ.dependsOn( jarTask )

			// Add zipified as dependency.
			variantCompile( var, jarPath, [''] )

			// This handles android libraries via exploded-aars:
			variantCompile( var, aarPath, JAR_DIR_WILDCARD )

			// Add all project dependencies as dependencies for tester.
			def compile = decapitalize( var.testCompile.name - 'test' )
			variantCompile( var, librarySubprojects( android, compile ) )
		}
	}

	/**
	 * Returns the list of "variants" containing actual android variants.
	 *
	 * @return the list of variants.
	 */
	private Collection<RoboSpockVariant> variants() {
		cfg.graph.findAll { it.variant != null }
	}

	/**
	 * Adds to the testCompile of the variant.
	 * The added dependency is a list of filetree's in the directory = path
	 * with the include prefixed by each in includePrefixes.
	 *
	 * @param var             the "variant"
	 * @param path            the directory.
	 * @param includePrefixes the prefix of the include.
	 */
	private void variantCompile( RoboSpockVariant var, File path, List<String> includePrefixes ) {
		def t = cfg.tester
		variantCompile( var, t.fileTree(
			dir: path,
			include: includePrefixes.collect { it + JAR_WILDCARD }
		) )
	}

	/**
	 * Adds to the testCompile of the variant.
	 *
	 * @param var the "variant".
	 * @param dep the dependency or list of dependencies.
	 */
	private void variantCompile( RoboSpockVariant var, Object dep ) {
		if ( dep instanceof Collection ) {
			dep.each { variantCompile( var, it ) }
		} else {
			cfg.tester.dependencies {
				add( var.testCompile.name, dep )
			}
		}
	}

	/**
	 * Add dependency, dep, to the testCompile configuration.
	 *
	 * @param dep the dependency.
	 */
	private void testCompile( Object dep ) {
		cfg.tester.dependencies { it.testCompile dep }
	}

	/**
	 * Returns a set of all the decendant library projects to the android project.
	 * They are decendants in the sense that they are project dependencies.
	 *
	 * @param	project		the {@link org.gradle.api.Project}.
	 * @param	conf		the configuration to look in for sub projects.
	 * @return	the subprojecs.
	 */
	private Set<Project> librarySubprojects( Project project, String conf ) {
		collectWhileNested( project ) { p ->
			p.configurations[conf].allDependencies
			 .findResults { it instanceof ProjectDependency ? it.dependencyProject : null }
		}.findAll { !isLibrary( it ) }
	}
}
