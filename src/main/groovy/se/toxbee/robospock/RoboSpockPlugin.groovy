/*
 * Copyright 2014 toxbee.se
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

package se.toxbee.robospock

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.bundling.Zip

/**
 * {@link RoboSpockPlugin}:
 *
 * Simplifies the usage of RoboSpock using gradle.
 * Requires that the project property "robospock"
 * be declared like so:
 *
 * <pre>{@code
 * robospock {
 * 		project = ':myApp'
 * }
 * }</pre>
 *
 * Or a more extensive configuration:
 *
 * <pre>{@code
 * robospock {
 *		project          = ':myApp'<br/>
 *		buildType        = 'debug'
 *		spockVersion     = '0.7-groovy-2.0'
 *	    groovyVersion    = '2.3.3'
 *		clibVersion      = '3.1'
 *		objenesisVersion = '2.1'
 * }
 * }</pre>
 *
 * @see RoboSpockConfiguration#project
 * @see RoboSpockConfiguration#buildType
 * @see RoboSpockConfiguration#spockVersion
 * @see RoboSpockConfiguration#groovyVersion
 * @see RoboSpockConfiguration#clibVersion
 * @see RoboSpockConfiguration#objenesisVersion
 * @version 0.1
 * @since 2014-10-01
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class RoboSpockPlugin implements Plugin<Project> {
	public static String zip2jarTask = 'zip2jar'
	public static final String ZIP_2_JAR_DESCRIPTION = "Zips for Robospock."

	void apply( Project project ) {
		project.extensions.create( "robospock", RoboSpockConfiguration )

		RoboSpockConfiguration cfg = project.robospock

		def androidProject = verifyConfig project, cfg

		addAndroidRepositories project, androidProject

		applyGroovy project

		setupDependencies project, cfg

		copyAndroidDependencies androidProject, cfg

		setupTestTask androidProject, cfg
	}

	/**
	 * Sets up the test task.
	 *
	 * @param androidProject the android {@link Project}.
	 * @param cfg the {@link RoboSpockConfiguration} object
	 */
	def setupTestTask( Project androidProject, RoboSpockConfiguration cfg ) {
		task robospock( type: RoboSpockTest ) {
			android = androidProject
			config  = cfg
		}
	}

	/**
	 * Adds the android SDK dir repositories.
	 *
	 * @param project the test {@link Project}.
	 * @param androidProject the android {@link Project}.
	 */
	def addAndroidRepositories( Project project, Project androidProject ) {
		def sdkDir = androidProject.android.sdkDirectory

		project.repositories {
			maven { url "${sdkDir}/extras/android/m2repository" }
			maven { url "${sdkDir}/extras/google/m2repository" }
		}
	}

	/**
	 * Applies the groovy plugin to the project.
	 *
	 * @param project the test {@link Project}.
	 */
	def applyGroovy( Project project ) {
		project.plugins.apply( 'groovy' )
	}

	/**
	 * Setup the dependencies of the test project.
	 *
	 * @param project the test {@link Project}.
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def setupDependencies( Project project, RoboSpockConfiguration cfg ) {
		def deps = [
			"org.codehaus.groovy:groovy-all:${cfg.groovyVersion}",
			"org.spockframework:spock-core:${cfg.spockVersion}",
			"org.robospock:robospock:${cfg.robospockVersion}"
		]

		cfg.clibVersion = cfg.clibVersion.trim()
		if ( cfg.clibVersion ) {
			deps << "cglib:cglib-nodep:${cfg.clibVersion}"
		}

		cfg.objenesisVersion = cfg.objenesisVersion.trim()
		if ( cfg.objenesisVersion ) {
			deps << "cglib:cglib-nodep:${cfg.objenesisVersion}"
		}

		deps.each { dep ->
			project.dependencies {
				testCompile dep
			}
		}
	}

	/**
	 * Verifies that the config is usable.
	 *
	 * @param project the test {@link Project}.
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 * @return the android {@link Project} (project under test).
	 */
	def Project verifyConfig( Project project, RoboSpockConfiguration cfg ) {
		// Verify that we have a project named.
		cfg.project = cfg.project?.trim()
		if ( cfg.project ) {
			throw new GradleException( "RoboSpock: no project to test found in project.robospock.project, please set it!" )
		}

		// Verify that it is an android project.
		Project androidProj = project.project( cfg.project )
		if ( !isAndroid( androidProj ) ) {
			throw new GradleException( androidProj.toString() + " is not an android project" )
		}

		// Verify that the buildType specified exists.
		if ( !androidProj.android.buildTypes.find { it.name == cfg.buildType } ) {
			throw new GradleException( "Specified buildType: " + cfg.buildType + ' does not exist.' )
		}

		return androidProj
	}

	/**
	 * Copies the android project dependencies to this project.
	 *
	 * @param project the android {@link Project}.
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def copyAndroidDependencies( Project androidProject, RoboSpockConfiguration cfg ) {

		def projDep = androidProject + getSubprojects( androidProject )

		def zip2jarDependsTask = "compile${cfg.buildType.capitalize()}Java"

		projDep.each { proj ->
			// Create zip2jar task & make compileJava depend on it.
			def zip2jar = proj.tasks.create( name: zip2jarTask, type: Zip ) {
				dependsOn zip2jarDependsTask
				description ZIP_2_JAR_DESCRIPTION
				from "build/intermediates/classes/${cfg.buildType}"
				destinationDir = file( 'build/libs' )
				extension = "jar"
			}
			tasks.compileJava.dependsOn( zip2jar )

			def aarPath = 'build/intermediates/exploded-aar/'

			// Add all jars frm zip2jar + exploded-aar:s to dependencies.
			project.dependencies {
				compile fileTree( dir: proj.file( libsPath ), include: "*.jar" )
				compile fileTree( dir: proj.file( aarPath ), include: ['*/*/*/*.jar'] )
				compile fileTree( dir: proj.file( aarPath ), include: ['*/*/*/*/*.jar'] )
			}
		}
	}

	/**
	 * Checks if the provided project is an android or android-library project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	def boolean isAndroid( Project project ) {
		return project.plugins.hasPlugin( "android" ) || project.plugins.hasPlugin( "android-library" )
	}

	/**
	 * Returns all sub projects to the android project.
	 *
	 * @param androidProject the android {@link Project}.
	 * @return the sub{@link Project}s.
	 */
	def List<Project> getSubprojects( Project androidProject ) {
		def projects = []
		extractSubprojects( androidProject, projects )
		return projects
	}

	/**
	 * Recursively extracts all dependency-subprojects to the android project.
	 *
	 * @param libraryProject the library {@link Project} to search in.
	 * @param projects the list of {@link Project}s to add to.
	 * @return the sub{@link Project}s.
	 */
	def extractSubprojects( Project libraryProject, List<Project> projects ) {
		Configuration compile = libraryProject.configurations.all.find { it.name == 'compile' }

		def projDeps = compile.allDependencies
				.findAll { it instanceof ProjectDependency }
				.collect { ((ProjectDependency) it).dependencyProject }

		projDeps.each { extractSubprojects( it, projects ) }
		projects.addAll( projDeps )
	}
}
