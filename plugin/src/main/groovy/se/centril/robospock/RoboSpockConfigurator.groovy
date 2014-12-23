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

import static se.centril.robospock.RoboSpockUtils.collectWhileNested
import static se.centril.robospock.RoboSpockUtils.isLibrary

import se.centril.robospock.internal.TestVariantImpl

/**
 * {@link RoboSpockConfigurator}: Is the heart of the plugin,
 * this is where all the action happens.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since Nov, 19, 2014
 */
class RoboSpockConfigurator {
	private static final String ROBOSPOCK_TASK_NAME_BASE = 'robospock'
	private static final String JAR_TASK_BASE = 'robospockZip2Jar'
	private static final String JAR_TASK_DESCRIPTION = "Zips for Robospock."

	private static final String AAR_PATH = 'intermediates/exploded-aar/'
	private static final String CLASSES_PATH = 'intermediates/classes/'
	private static final String LIBS_PATH = 'libs'
	private static final String JAR_EXT = 'jar'

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
		 //this.&setupTestTasks,
		 this.&copyAndroidDependencies, this.&setupTestTasks, this.&fixRobolectricBugs,
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
		cfg.tester.dependencies {
			testCompile 'com.android.support:support-v4:19.0.1'
		}
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
				"org.codehaus.groovy:groovy-all:${cfg.groovyVersion}",
				"org.spockframework:spock-core:${cfg.spockVersion}",
				"org.robospock:robospock:${cfg.robospockVersion}"
		]

		cfg.cglibVersion = cfg.cglibVersion.trim()
		if ( cfg.cglibVersion ) {
			deps << "cglib:cglib-nodep:${cfg.cglibVersion}"
		}

		cfg.objenesisVersion = cfg.objenesisVersion.trim()
		if ( cfg.objenesisVersion ) {
			deps << "org.objenesis:objenesis:${cfg.objenesisVersion}"
		}

		deps.each { dep ->
			cfg.tester.dependencies {
				testCompile dep
			}
		}
	}

	/**
	 * Adds the android SDK dir repositories to {@link RoboSpockConfiguration#android}.
	 */
	def addAndroidRepositories() {
		def sdkDir = cfg.sdkDir()
		cfg.tester.repositories {
			maven { url new File( sdkDir, "extras/android/m2repository" ).toURI().toString() }
			maven { url new File( sdkDir, "extras/google/m2repository" ).toURI().toString() }
		}
	}

	/**
	 * Sets up the test tasks.
	 */
	def setupTestTasks() {
		this.setupGraph()

		def tasks = cfg.tester.tasks
		/*

		// Create the actual test tasks per variant.
		cfg.variants.each { variant ->
			cfg.robospockTask = tasks.create( name: ROBOSPOCK_TASK_NAME_BASE, type: RoboSpockTest ) {
				config = cfg
				variant = v
				sourceSet = ss
				configure()
			}
		}

		// Create the grouped robospock task.
		cfg.robospockTask = tasks.create(
			name: ROBOSPOCK_TASK_NAME_BASE,
			description: 'Runs all the unit tests using RoboSpock, for all variants.',
			group: JavaBasePlugin.VERIFICATION_GROUP )



		// Make check depend on the grouped task.
		tasks.getByName( JavaBasePlugin.CHECK_TASK_NAME ).dependsOn( cfg.robospockTask )

		// Remove all actions on test & make it basically do the grouped robospock task.
		cfg.tester.test {
			deleteAllActions()
			dependsOn cfg.robospockTask
		}
		*/
	}

	def setupGraph() {
		def p = cfg.tester,
			vars = cfg.variants,
			// The graph:
			dag = new RoboSpockDAG<RoboSpockTestVariant>(),
			// Maps from name -> TV.
			mapping = [:],
			extend = { List<Object> bases, Object vName, RoboSpockTestVariant vertex ->
				mapping[vName] = vertex
				dag.add( vertex )
				bases.each { b ->
					def base = b instanceof TestVariantImpl ? b : mapping[b]
					dag.add( base, vertex )
					vertex.extendFrom( p, base )
				}
				return vertex
			}

		// Root:
		def root = extend( [], 'root', new TestVariantImpl( p, p.sourceSets.test ) )

		// Variant that is a build type:
		// Android plugin creates variants with same name as build types, process this later.
		boolean notBtVariant = true
		for ( def v : vars ) {
			if ( v.name in cfg.buildTypes ) {
				notBtVariant = false
				break
			}
		}

		// For each build type:
		if ( notBtVariant ) {
			cfg.buildTypes.each { bt ->
				extend( [root], bt, new TestVariantImpl( p, bt, 'build type' + bt ) )
			}
		}

		// Add for each PF = product flavor:
		// Library projects don't have PFs.
		if ( !isLibrary( cfg.android ) ) {
			cfg.variants.collectMany { it.productFlavors }.unique().each { pf ->
				def vPf = extend( [root], pf, new TestVariantImpl( p, pf, 'product flavor ' + pf.name ) )
				// Add for each T = (PF, BT):
				cfg.buildTypes.each { bt ->
					def name = pf.name + bt.capitalize()
					extend( [bt, vPf], name, new TestVariantImpl( p, name, '(product flavor, build type) ' + name ) )
				}
			}
		}

		// Add for each variant.
		vars.each { var ->
			def bt = var.buildType.name,
				v = new TestVariantImpl( p, cfg, var )

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
	}

	/**
	 * Fixes/addresses various bugs in robolectric.
	 */
	def fixRobolectricBugs() {
		def manifest = 'AndroidManifest.xml'
		def correctManifestPath = 'intermediates/manifests/full'

		cfg.variants.each { variant ->
			def taskName = "robospockFixRobolectricBugs${variant.name.capitalize()}"
			def copier = cfg.android.tasks.create( name: taskName )
			variant.getOutputs()[0].processResources.finalizedBy copier

			copier << {
				// Library: Copy manifest, intermediates/manifests/full/ -> intermediates/bundles/
				cfg.android.copy {
					from( "${cfg.android.buildDir}/intermediates/bundles/${variant.dirName}/" ) {
						include manifest
					}
					into( "${cfg.android.buildDir}/${correctManifestPath}/${variant.dirName}/" )
				}

				// Manifest: Clamp any SDK VERSION in the eyes of roboelectric to API level 18.
				cfg.android.copy {
					from( "${cfg.android.buildDir}" ) {
						include "${correctManifestPath}/${variant.dirName}/${manifest}"
					}
					into( "${cfg.tester.buildDir}" )
					filter {
						it.replaceFirst( ~/android\:targetSdkVersion\=\"(\d{1,2})\"/, {
							def ver = Integer.parseInt( it[1] ) > 18
							return 'android:targetSdkVersion="' + 18 + '"'
						} )
					}
				}

				// Library: Copy intermediates/bundles/{variant.dirName}/res/ -> intermediates/res/{variant.dirName}/
				cfg.android.copy {
					from( "${cfg.android.buildDir}/intermediates/bundles/${variant.dirName}/res/" )
					into( "${cfg.android.buildDir}/intermediates/res/${variant.dirName}/" )
				}
			}
		}
	}

	/**
	 * Copies the android project dependencies to this project.
	 */
	def copyAndroidDependencies() {
		def tester = cfg.tester
		def android = cfg.android

		def aarPath = new File( android.buildDir, AAR_PATH )

		// Doing this for every variant!
		cfg.variants.each { variant ->
			// First zipify the android project iself:
			// Create zip2jar task if not present & make compileJava depend on it.
			def libsPath = new File( android.buildDir, LIBS_PATH )
			Task jarTask = android.tasks.create( name: jarTaskName( variant ), type: Zip ) {
				dependsOn variant.javaCompile
				description JAR_TASK_DESCRIPTION
				from new File( android.buildDir, CLASSES_PATH + variant.dirName )
				destinationDir = libsPath
				extension = JAR_EXT
			}
			tester.tasks.compileTestJava.dependsOn( jarTask )

			// Add dependencies.
			tester.dependencies {
				testCompile tester.fileTree( dir: libsPath, include: '*.' + JAR_EXT )
				// This handles android libraries via exploded-aars:
				testCompile tester.fileTree( dir: aarPath, include: ['*/*/*/*.' + JAR_EXT] )
				testCompile tester.fileTree( dir: aarPath, include: ['*/*/*/*/*.' + JAR_EXT] )
			}

			// Add all project dependencies as dependencies for tester.
			getSubprojects( android ).findAll { !isLibrary( it ) }.each { proj ->
				tester.dependencies {
					testCompile proj
				}
			}
		}
	}

	private String jarTaskName( variant ) {
		return JAR_TASK_BASE + '_' + variant.name
	}

	/**
	 * Returns a set of all the decendant projects to the android project.
	 * They are decendants in the sense that they are project dependencies.
	 *
	 * @param project the {@link org.gradle.api.Project}.
	 * @param configuration the configuration to look in for sub projects, default = compile.
	 * @return the sub{@link org.gradle.api.Project}s.
	 */
	def Set<Project> getSubprojects( Project project, configuration = 'compile' ) {
		collectWhileNested( project ) { p ->
			p.configurations.all.find { c -> c.name == configuration }
			 .allDependencies
			 .findResults {
				it instanceof ProjectDependency ? it.dependencyProject : null
			}
		}
	}
}
