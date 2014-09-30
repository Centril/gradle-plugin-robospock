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
package se.toxbee.lgio

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * lgio is a small IO plugin for gradle.
 * if the console is present, it gets input from console,
 * otherwise it uses swing.
 *
 * It can be used like so:
 *
 * apply plugin: 'lgio'
 * def val1 = lgio.readLine( 'my message' )
 * def pwd1 = lgio.readPassword( 'my message' )
 * ...
 *
 * lgio and io can be used interchangably, they are aliases.
 * To stop lgio from io as an alias, set:
 * project.ext.lgioAliasDisable = true
 *
 * @version 0.1
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class LGIOPlugin implements Plugin<Project> {
	void apply( Project project ) {
		// Config:
		def extKey = 'lgio'
		def aliasKey = 'io'
		def disableAliasKey = 'lgioAliasDisable'

		// Create the extension object.
		LGIOPluginExtension plugin = project.extensions.create( extKey, LGIOPluginExtension )

		// Provide alias if allowed.
		boolean useAlias = !(project.hasProperty( disableAliasKey ) && project.property( disableAliasKey ))
		if ( useAlias ) {
			project.extensions.add( aliasKey, plugin );
		}
	}
}