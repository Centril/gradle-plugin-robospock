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

import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
/**
 * {@link RoboSpockTest} is the test task.
 *
 * @author Centril < twingoow @ gmail.com >  / Mazdak Farrokhzad.
 * @version 1.0
 * @since Oct , 02, 2014
 */
class RoboSpockTest extends Test {
	/*
	 * Paths, Task names, etc.:
	 */

	public static String robospockTaskName = 'robospock'
	public static final String ROBOSPOCK_TASK_DESCRIPTION = 'Runs the unit tests using RoboSpock.'

	public static String manifestPath = 'src/main/AndroidManifest.xml'
	public static String mainPath = 'src/main'

	/*
	 * Properties:
	 */

	RoboSpockConfiguration config

	/*
	 * Actions:
	 */

	@TaskAction
	public void executeTests() {
		setName         robospockTaskName
		setDescription  ROBOSPOCK_TASK_DESCRIPTION
		setGroup        JavaBasePlugin.VERIFICATION_GROUP

		// set a system property for the test JVM(s)
		systemProperty 'ro.build.date.utc', '1'
		systemProperty 'ro.kernel.qemu', '0'

		def android = config.android

		systemProperty 'android.manifest', android.android.file( manifestPath )
		systemProperty 'android.resources', android.file( "build/intermediates/res/${config.buildType}" )
		systemProperty 'android.assets', android.file( "build/intermediates/res/${config.buildType}/raw" )

		// set heap size for the test JVM(s)
		minHeapSize = "128m"
		maxHeapSize = "1024m"

		// set JVM arguments for the test JVM(s)
		jvmArgs '-XX:MaxPermSize=512m'

		// listen to events in the test execution lifecycle
		beforeTest { descriptor ->
			logger.lifecycle( "Running test: " + descriptor.toString() )
		}

		testLogging {
			lifecycle {
				exceptionFormat "full"
			}
		}

		// Set working directory.
		workingDir = "${android.projectDir}/${mainPath}"

		// Make check depend on this task.
		getProject().getTasks().getByName( JavaBasePlugin.CHECK_TASK_NAME ).dependsOn( this )
	}
}
