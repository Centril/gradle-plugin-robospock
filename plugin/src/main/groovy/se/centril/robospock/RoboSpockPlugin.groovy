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

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * {@link RoboSpockPlugin}:
 *
 * <p>Simplifies the usage of RoboSpock using build.</p>
 *
 * <p>Requires that the project property "robospock"
 * be declared like so:
 *
 * <pre>{@code
 * robospock {
 * 		android = ':myApp'
 * }
 * }</pre></p>
 *
 * <p>However, if there is an android project in the path<br/>
 * that has the same name as the test project but with<br/>
 * ~/[^a-zA-Z0-9]?test$/ removed then the requirement doesn't<br/>
 * apply as the project is found automatically.<br/>
 * An example: test project is named: myapp-test,<br/>
 * while android app is named myapp</p>
 *
 * <p>A more extensive configuration:
 *
 * <pre>{@code
 * robospock {
 *		android          = ':myApp'<br/>
 *		buildType        = 'debug'
 *		spockVersion     = '0.7-groovy-2.0'
 *	    groovyVersion    = '2.3.6'
 *		cglibVersion      = '3.1'
 *		objenesisVersion = '2.1'
 * }
 * }</pre></p>
 *
 * @see RoboSpockConfiguration#testing
 * @see RoboSpockConfiguration#buildType
 * @see RoboSpockConfiguration#spockVersion
 * @see RoboSpockConfiguration#groovyVersion
 * @see RoboSpockConfiguration#cglibVersion
 * @see RoboSpockConfiguration#objenesisVersion
 * @version 0.1
 * @since 2014-10-01
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class RoboSpockPlugin implements Plugin<Project> {
	void apply( Project project ) {
		boolean inverse = RoboSpockConfiguration.isAndroid( project )

		// Add jcenter to buildscript repo.
		project.buildscript {
			repositories {
				jcenter()
			}
		}

		// Create the extension.
		def robospock = project.extensions.create( "robospock", RoboSpockConfiguration, project, inverse )

		// Temporarily fix the issue with not being able to set the tester in inverse mode.
		if ( project.hasProperty( 'robospockTester' ) ) {
			robospock.tester = project.getProperty( 'robospockTester' )
		}

		// Apply the groovy plugin.
		(inverse ? robospock.tester : project).apply plugin: 'groovy'

		// Configure robospock.
		project.afterEvaluate {
			new RoboSpockAction().execute( robospock )
		}
	}
}
