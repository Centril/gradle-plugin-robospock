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

/**
 * RoboSpockPlugin:
 *
 * Simplifies the usage of RoboSpock using gradle.
 * Requires that the project property "robospock"
 * be declared like so:
 *
 * project.ext.robospock = [
 *		project: ':myApp'	// The application under test.
 *		buildType: 'debug'	// The buildType being tested.
 *		spockVersion:     '0.7-groovy-2.0', // Note1
 *		clibVersion:      '3.1', // Note2
 *		objenesisVersion: '2.1'  // Note2
 * ]
 *
 * Note1: Optional, default: latest known.
 * Note2: Optional, set to false if you don't want it, default: latest known.
 *
 * @version 0.1
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class RoboSpockPlugin implements Plugin<Project> {
	void apply( Project project ) {
		new RoboSpockAction().execute( project )
	}
}