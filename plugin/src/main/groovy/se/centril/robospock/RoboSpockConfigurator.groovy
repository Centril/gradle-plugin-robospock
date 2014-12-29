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
 * @since Nov, 19, 2014
 */
class RoboSpockConfigurator {
	RoboSpockConfiguration cfg

	//================================================================================
	// Public API:
	//================================================================================

	/**
	 * Constructs a configurator.
	 *
	 * @param config a {@link RoboSpockConfiguration} object to configure with.
	 */
	RoboSpockConfigurator( RoboSpockConfiguration config ) {
		this.cfg = config
	}

	/**
	 * Configures everything.
	 */
	public void configure() {
		[cfg.&verify, this.&applyGroovy, this.&addJCenter,
		 this.&addAndroidRepositories, this.&addDependencies, this.&fixSupportLib,
		 this.&setupGraph, this.&copyAndroidDependencies, this.&fixRobolectricBugs,
		 cfg.&executeAfterConfigured]
		 	.each { it() }
	}

	//================================================================================
	// Private API:
	//================================================================================

	/**
	 * Applies the groovy plugin to the tester.
	 */
	def applyGroovy() {
		cfg.tester.apply plugin: 'groovy'
	}

	/**
	 * Makes sure that android-sdk-manager pulls support libs.
	 */
	def fixSupportLib() {
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
	def addJCenter() {
		cfg.tester.repositories {
			jcenter()
		}
	}

	/**
	 * Adds the jcenter() to buildscript repositories.
	 */
	def static addJCenterBuildScript( RoboSpockConfiguration cfg ) {
		cfg.perspective.buildscript {
			repositories {
				jcenter()
			}
		}
	}

	/**
	 * Adds all the dependencies of this configuration to {@link Project}.
	 */
	def addDependencies() {
		def deps = [
			MAVEN_GROOVY	+ cfg.groovyVersion,
			MAVEN_SPOCK		+ cfg.spockVersion,
			MAVEN_ROBOSPOCK	+ cfg.robospockVersion
		]

		cfg.cglibVersion = cfg.cglibVersion.trim()
		if ( cfg.cglibVersion ) {
			deps << MAVEN_CGLIB	+ cfg.cglibVersion
		}

		cfg.objenesisVersion = cfg.objenesisVersion.trim()
		if ( cfg.objenesisVersion ) {
			deps << MAVEN_OBJNESIS + cfg.objenesisVersion
		}

		deps.each( this.&testCompile )
	}

	/**
	 * Adds the android SDK dir repositories to {@link RoboSpockConfiguration#android}.
	 */
	def addAndroidRepositories() {
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
	def setupGraph() {
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
		if ( !isLibrary( cfg.android ) ) {
			cfg.variants.collectMany { it.productFlavors }.unique().each { pf ->
				def vPf = extend( [root], pf, new VariantImpl( p, pf, TASK_DESCRIPTION_PRODUCT_FLAVOR + pf.name ) )
				// Add for each T = (PF, BT):
				cfg.buildTypes.each { bt ->
					def name = pf.name + bt.capitalize()
					extend( [bt, vPf], name, new VariantImpl( p, name, TASK_DESCRIPTION_PF_BT + name ) )
				}
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
			if ( isLibrary( cfg.android ) || var.productFlavors.isEmpty() ) {
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
	def fixRobolectricBugs() {
		def android = cfg.android,
			aBuildDir = "${android.buildDir}",
			tBuildDir = "${cfg.tester.buildDir}",
			attr = ANDROID_TARGET_VERSION,
			start = quote( attr[0] ),
			end = quote( attr[1] ),
			pattern = ~/$start(\d{1,2})$end}/

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
					from( bundlesPath ) {
						include MANIFEST_FILE
					}
					into( "$aBuildDir/$MANIFEST_PATH/$dir/" )
				}

				// Manifest: Clamp any SDK VERSION in the eyes of
				// roboelectric to API level ANDROID_FIX_VERSION.
				android.copy {
					from( aBuildDir ) {
						include "$MANIFEST_PATH/$dir/$MANIFEST_FILE"
					}
					into( tBuildDir )
					filter {
						it.replaceFirst( pattern, {
							return start + clampTargetVersion( Integer.parseInt( it[1] ) ) + end
						} )
					}
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
	 * Clamps the targetSdkVersion.
	 *
	 * @param v the targetSdkVersion.
	 */
	private void clampTargetVersion( int v ) {
		v > ANDROID_FIX_VERSION ? v : ANDROID_FIX_VERSION
	}

	/**
	 * Copies the android project dependencies to this project.
	 */
	def copyAndroidDependencies() {
		def tester = cfg.tester,
			android = cfg.android,
			buildDir = android.buildDir,
			aarPath = new File( buildDir, AAR_PATH ),
			libsPath = new File( buildDir, LIBS_PATH )

		// Doing this for every variant in graph!
		variants().each { vertex ->
			def v = vertex.variant

			// First zipify the android project iself:
			// Create zip2jar task if not present & make compileJava depend on it.
			Task jarTask = android.tasks.create(
				name: JAR_TASK_BASE + v.name.capitalize(),
				group: JAR_TASK_GROUP,
				description: JAR_TASK_DESCRIPTION,
				type: Zip
			) {
				dependsOn v.javaCompile
				from new File( buildDir, CLASSES_PATH + v.dirName )
				destinationDir = libsPath
				extension = JAR_EXT
			}
			tester.tasks.compileTestJava.dependsOn( jarTask )

			// Add zipified as dependency.
			variantCompile( vertex, tester.fileTree( dir: libsPath, include: JAR_WILDCARD ) )

			// This handles android libraries via exploded-aars:
			JAR_DIR_WILDCARD.each { dir ->
				variantCompile( vertex, tester.fileTree( dir: aarPath, include: dir + JAR_WILDCARD ) )
			}

			// Add all project dependencies as dependencies for tester.
			getSubprojects( android ).findAll { !isLibrary( it ) }.each { proj ->
				variantCompile( vertex, proj )
			}

			//
			//variantCompile( vertex, vertex.sourceSet.output )
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
	 *
	 * @param v   the "variant".
	 * @param dep the dependency.
	 */
	private void variantCompile( RoboSpockVariant v, Object dep ) {
		cfg.tester.dependencies {
			add( v.testCompile.name, dep )
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
	 * Returns a set of all the decendant projects to the android project.
	 * They are decendants in the sense that they are project dependencies.
	 *
	 * @param project the {@link org.gradle.api.Project}.
	 * @param configuration the configuration to look in for sub projects, default = compile.
	 * @return the sub{@link org.gradle.api.Project}s.
	 */
	private Set<Project> getSubprojects( Project project, configuration = 'compile' ) {
		collectWhileNested( project ) { p ->
			p.configurations.all.find { c -> c.name == configuration }
			 .allDependencies
			 .findResults {
				it instanceof ProjectDependency ? it.dependencyProject : null
			}
		}
	}
}
