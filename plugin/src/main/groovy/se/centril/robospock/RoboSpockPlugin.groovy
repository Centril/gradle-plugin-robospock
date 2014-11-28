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
 * 		android = ':app'
 * }
 * }</pre></p>
 *
 * <p>However, if there is an android project in the path<br/>
 * that has the same name as the test project but with<br/>
 * ~/[^a-zA-Z0-9]?test$/ removed then the requirement doesn't<br/>
 * apply as the project is found automatically.<br/>
 * An example: test project is named: app-test,<br/>
 * while android app is named app</p>
 *
 * It is also possible to apply the plugin on an android project
 * and instead specify the tester project like so:
 *
 * <pre>{@code
 * robospock {
 * 		tester = ':app-test'
 * }
 * }</pre></p>
 *
 * If you have a project named test as a child or **`app-test`**
 * as a child or a sibling of the android project, it will be automatically
 * found and used. This can rid you of the need for a `build.gradle`
 * file for the tester project altogether.
 *
 * <p>A more extensive configuration:
 *
 * <pre>{@code
 * robospock {
 *		android          = project( ':app' )<br/>
 *		buildType        << 'release'
 *		spockVersion     = '0.7-groovy-2.0'
 *	    groovyVersion    = '2.3.6'
 *		cglibVersion     = '3.1'
 *		objenesisVersion = '2.1'
 * }
 * }</pre></p>
 *
 * @see RoboSpockConfiguration#android
 * @see RoboSpockConfiguration#tester
 * @see RoboSpockConfiguration#buildType
 * @see RoboSpockConfiguration#spockVersion
 * @see RoboSpockConfiguration#groovyVersion
 * @see RoboSpockConfiguration#cglibVersion
 * @see RoboSpockConfiguration#objenesisVersion
 * @see RoboSpockConfiguration#perspective
 * @see RoboSpockConfiguration#afterConfigured
 * @see RoboSpockConfiguration#robospockTask
 * @since 2014-10-01
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class RoboSpockPlugin implements Plugin<Project> {
	void apply( Project project ) {
		// Create & execute.
		RoboSpockAction.perform( project.extensions.create( "robospock", RoboSpockConfiguration, project ) )
	}
}
