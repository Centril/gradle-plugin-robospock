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

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.bundling.Zip

/**
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @version 1.0
 * @since Oct , 02, 2014
 */
class RoboSpockAction implements Action<RoboSpockConfiguration> {
	public static String robospockTaskName = 'robospock'
	public static final String ZIP_2_JAR_TASK = 'robospock_zip2jar'
	public static final String ZIP_2_JAR_DESCRIPTION = "Zips for Robospock."

	@Override
	void execute( RoboSpockConfiguration config ) {
		config.verify()
		addMavenCentral config
		addAndroidRepositories config
		applyGroovy config
		addDependencies config

		copyAndroidDependencies config
		setupTestTask config
	}

	/**
	 * Adds the mavenCentral() to repositories (& buildscript).
	 *
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def addMavenCentral( RoboSpockConfiguration cfg ) {
		cfg.project.buildscript {
			repositories {
				mavenCentral()
			}
		}

		cfg.project.repositories {
			mavenCentral()
		}
	}

	/**
	 * Sets up the test task.
	 *
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def setupTestTask( RoboSpockConfiguration cfg ) {
		def task = cfg.project.tasks.create( name: robospockTaskName, type: RoboSpockTest ) {
			config = cfg
		}

		// Remove all actions on test & make it basically do robospock task.
		cfg.project.test {
			deleteAllActions()
			dependsOn task
		}
	}

	/**
	 * Adds all the dependencies of this configuration to {@link Project}.
	 *
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def addDependencies( RoboSpockConfiguration cfg ) {
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
			cfg.project.dependencies {
				testCompile dep
			}
		}
	}

	/**
	 * Applies the groovy plugin to project.
	 *
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def applyGroovy( RoboSpockConfiguration cfg ) {
		def p = cfg.project
		if ( !p.plugins.hasPlugin( 'groovy' ) ) {
			p.apply plugin: 'groovy'
		}
	}

	/**
	 * Adds the android SDK dir repositories to {@link #project}.
	 *
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def addAndroidRepositories( RoboSpockConfiguration cfg ) {
		def sdkDir = cfg.android.android.sdkDirectory

		cfg.project.repositories {
			maven { url "${sdkDir}/extras/android/m2repository" }
			maven { url "${sdkDir}/extras/google/m2repository" }
		}
	}

	/**
	 * Copies the android project dependencies to this project.
	 *
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def copyAndroidDependencies( RoboSpockConfiguration cfg ) {
		def android = cfg.android
		def projDep = getSubprojects( android ) + android

		def zip2jarDependsTask = "compile${cfg.buildType.capitalize()}Java"

		def p = cfg.project

		projDep.each { proj ->
			def libsPath = 'build/libs'
			def aarPath = 'build/intermediates/exploded-aar/'

			// Create zip2jar task & make compileJava depend on it.
			Task zip2jar = proj.tasks.create( name: ZIP_2_JAR_TASK, type: Zip ) {
				dependsOn zip2jarDependsTask
				description ZIP_2_JAR_DESCRIPTION
				from "build/intermediates/classes/${cfg.buildType}"
				destinationDir = proj.file( libsPath )
				extension = "jar"
			}
			p.tasks.compileJava.dependsOn( zip2jar )

			// Add all jars frm zip2jar + exploded-aar:s to dependencies.
			p.dependencies {
				compile p.fileTree( dir: proj.file( libsPath ), include: "*.jar" )
				compile p.fileTree( dir: proj.file( aarPath ), include: ['*/*/*/*.jar'] )
				compile p.fileTree( dir: proj.file( aarPath ), include: ['*/*/*/*/*.jar'] )
			}
		}
	}

	/**
	 * Returns all sub projects to the android project.
	 *
	 * @param project the {@link org.gradle.api.Project}.
	 * @return the sub{@link org.gradle.api.Project}s.
	 */
	def List<Project> getSubprojects( Project project ) {
		def projects = []
		extractSubprojects( project, projects )
		return projects
	}

	/**
	 * Recursively extracts all dependency-subprojects from project.
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
