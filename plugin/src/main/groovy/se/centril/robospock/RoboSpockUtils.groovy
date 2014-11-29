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

import org.gradle.api.Project

/**
 * {@link RoboSpockUtils} provides static utility methods.
 *
 * @since 2014-11-29
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class RoboSpockUtils {
	/**
	 * Checks if the provided project is an android or android-library project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	public static boolean isAndroid( Project project ) {
		return isApplication( project ) || isLibrary( project )
	}

	/**
	 * Checks if the provided project is an android application project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	public static boolean isApplication( Project project ) {
		return project.plugins.hasPlugin('com.android.application') || project.plugins.hasPlugin('android')
	}

	/**
	 * Checks if the provided project is an android-library project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	public static boolean isLibrary( Project project ) {
		return project.plugins.hasPlugin('com.android.library') || project.plugins.hasPlugin('android-library')
	}
}