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

import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test

/**
 * {@link RoboSpockTest} is the test task.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @version 1.0
 * @since Oct, 02, 2014
 */
class RoboSpockTest extends Test {
	/*
	 * Properties:
	 */
	RoboSpockConfiguration config

	def variant
	def sourceSet

	/**
	 * Configures RoboSpock test additions.
	 * Running is not optional to make the tests work.
	 *
	 * @param  cfg the configuration object to use.
	 */
	public void configure() {
		// Setup classpath.
	    testClassesDir = sourceSet.output.classesDir
	    classpath = sourceSet.runtimeClasspath

		/*
		 * Naming:
		 */

		setDescription  'Runs the unit tests using RoboSpock.'
		setGroup        JavaBasePlugin.VERIFICATION_GROUP

		/*
		 * Setup for Roboelectric:
		 */

		// set a system property for the test JVM(s)
		systemProperty 'ro.build.date.utc', '1'
		systemProperty 'ro.kernel.qemu', '0'

		// set manifest.
		systemProperty 'android.manifest', this.buildPath( config.tester, 'manifests/full', 'AndroidManifest.xml' )

		systemProperty 'android.resources', this.buildPath( config.android, 'res', '' )
		systemProperty 'android.assets', this.buildPath( config.android, 'res', 'raw' )

		// set working directory.
		def wd = config.mainSourceDir()
		if ( wd.exists() ) {
			workingDir = wd
		}

		/*
		 * Test JVM settings:
		 */

		// set heap size for the test JVM(s)
		minHeapSize = "128m"
		maxHeapSize = "1024m"

		/*
		 * Logging:
		 */

		// listen to events in the test execution lifecycle
		beforeTest { descriptor ->
			logger.lifecycle( "Running test: " + descriptor.toString() )
		}

		testLogging {
			// set options for log level LIFECYCLE
			events "failed"
			exceptionFormat "short"
			// set options for log level DEBUG
			debug {
				events "started", "skipped", "failed"
				exceptionFormat "full"
			}

			// remove standard output/error logging from --info builds
			// by assigning only 'failed' and 'skipped' events
			info.events = ["failed", "skipped"]
		}
	}

	/**
	 * Returns a {buildDir}/intermediates relative File taking buildType into account.
	 *
	 * @param proj the project of reference in relativity.
	 * @param pre the string before buildType.
	 * @param post the string after buildType.
	 * @return the {@link java.io.File}
	 */
	def File buildPath( proj, pre, post ) {
		return new File( proj.buildDir, "intermediates/${pre}/${config.buildTypes[0]}/${post}" )
	}
}
