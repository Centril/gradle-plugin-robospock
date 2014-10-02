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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.bundling.Zip
/**
 * {@link RoboSpockPlugin}:
 *
 * <p>Simplifies the usage of RoboSpock using gradle.</p>
 *
 * <p>Requires that the project property "robospock"
 * be declared like so:
 *
 * <pre>{@code
 * robospock {
 * 		testing = ':myApp'
 * }
 * }</pre></p>
 *
 * <p>However, if there is an android project in the path<br/>
 * that has the same name as the test project but with<br/>
 * /[^(?!_)\w]?test$/ removed then the requirement doesn't<br/>
 * apply as the project is found automatically.<br/>
 * An example: test project is named: myapp-test,<br/>
 * while android app is named myapp</p>
 *
 * <p>A more extensive configuration:
 *
 * <pre>{@code
 * robospock {
 *		testing          = ':myApp'<br/>
 *		buildType        = 'debug'
 *		spockVersion     = '0.7-groovy-2.0'
 *	    groovyVersion    = '2.3.3'
 *		clibVersion      = '3.1'
 *		objenesisVersion = '2.1'
 * }
 * }</pre></p>
 *
 * @see RoboSpockConfiguration#testing
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
	public static final String ZIP_2_JAR_TASK = 'robospock_zip2jar'
	public static final String ZIP_2_JAR_DESCRIPTION = "Zips for Robospock."

	void apply( Project project ) {
		project.extensions.create( "robospock", RoboSpockConfiguration, project )

		project.afterEvaluate {
			RoboSpockConfiguration cfg = project.robospock

			cfg.verify()
			cfg.addAndroidRepositories()
			cfg.applyGroovy()
			cfg.addDependencies()

			copyAndroidDependencies cfg
			setupTestTask cfg
		}
	}

	/**
	 * Sets up the test task.
	 *
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def setupTestTask( RoboSpockConfiguration cfg ) {
		task robospock( type: RoboSpockTest ) {
			config  = cfg
		}
	}

	/**
	 * Copies the android project dependencies to this project.
	 *
	 * @param cfg the {@link RoboSpockConfiguration} object.
	 */
	def copyAndroidDependencies( RoboSpockConfiguration cfg ) {
		def android = cfg.android
		def projDep = android + getSubprojects( android )

		def zip2jarDependsTask = "compile${cfg.buildType.capitalize()}Java"

		projDep.each { proj ->
			def libsPath = 'build/libs'
			def aarPath = 'build/intermediates/exploded-aar/'

			// Create zip2jar task & make compileJava depend on it.
			def zip2jar = proj.tasks.create( name: ZIP_2_JAR_TASK, type: Zip ) {
				dependsOn zip2jarDependsTask
				description ZIP_2_JAR_DESCRIPTION
				from "build/intermediates/classes/${cfg.buildType}"
				destinationDir = file( libsPath )
				extension = "jar"
			}
			tasks.compileJava.dependsOn( zip2jar )

			// Add all jars frm zip2jar + exploded-aar:s to dependencies.
			project.dependencies {
				compile fileTree( dir: proj.file( libsPath ), include: "*.jar" )
				compile fileTree( dir: proj.file( aarPath ), include: ['*/*/*/*.jar'] )
				compile fileTree( dir: proj.file( aarPath ), include: ['*/*/*/*/*.jar'] )
			}
		}
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
