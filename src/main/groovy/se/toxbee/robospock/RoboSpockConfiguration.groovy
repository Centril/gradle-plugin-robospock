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

/**
 * {@link RoboSpockConfiguration} determines how
 * the {@link RoboSpockPlugin} should be used.
 *
 * @version 0.1
 * @since 2014-10-01
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class RoboSpockConfiguration {
	/**
	 * (Required) The application under test.
	 */
	def String project

	/**
	 * (Optional) The buildType being tested.
	 * Default = 'debug'
	 * @
	 */
	def String buildType        = 'debug'

	/**
	 * (Optional) The robospock version to use.
	 * Default: Latest known version (0.5.+)
	 */
	def String robospockVersion = '0.5.+'

	/**
	 * (Optional) The spock-framework version to use.
	 * Default: Latest known version (0.7-groovy-2.0)
	 */
	def String spockVersion     = '0.7-groovy-2.0'

	/**
	 * (Optional) The groovy version to use.
	 * Default: Latest known version (2.3.+).
	 */
	def String groovyVersion    = '2.3.+'

	/**
	 * (Optional) The clib version to use as dependency.
	 * Default: Latest known version (3.1).
	 * If the dependency is unwanted, set the string to empty.
	 */
	def String clibVersion      = '3.1'

	/**
	 * (Optional) The objenesis version to use as dependency.
	 * Default: Latest known version (2.1).
	 * If the dependency is unwanted, set the string to empty.
	 */
	def String objenesisVersion = '2.1'
}
