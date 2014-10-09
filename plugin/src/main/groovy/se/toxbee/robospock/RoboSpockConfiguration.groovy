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

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.regex.Pattern

/**
 * {@link RoboSpockConfiguration} determines how
 * the {@link RoboSpockPlugin} should be used.
 *
 * @version 0.1
 * @since 2014-10-01
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class RoboSpockConfiguration {
	//================================================================================
	// Gradle DSL
	//================================================================================

	/**
	 * (Required) The path of the android {@link Project} under test.
	 * Either this has to be set, or {@link #android}
	 */
	String testing

	/**
	 * (Required) The the android {@link Project} under test.
	 * Either this has to be set, or {@link #testing}
	 */
	Project android

	/**
	 * (Optional) The buildType being tested.
	 * Default = 'debug'
	 * @
	 */
	String buildType        = 'debug'

	/**
	 * (Optional) The robospock version to use.
	 * Default: Latest known version (0.5.+)
	 */
	String robospockVersion = '0.5.+'

	/**
	 * (Optional) The spock-framework version to use.
	 * Default: Latest known version (0.7-groovy-2.0)
	 */
	String spockVersion     = '0.7-groovy-2.0'

	/**
	 * (Optional) The groovy version to use.
	 * Default: Latest known version (2.3.6).
	 */
	String groovyVersion    = '2.3.6'

	/**
	 * (Optional) The clib version to use as dependency.
	 * Default: Latest known version (3.1).
	 * If the dependency is unwanted, set the string to empty.
	 */
	String cglibVersion = '3.1'

	/**
	 * (Optional) The objenesis version to use as dependency.
	 * Default: Latest known version (2.1).
	 * If the dependency is unwanted, set the string to empty.
	 */
	String objenesisVersion = '2.1'

	//================================================================================
	// Public non-DSL API:
	//================================================================================

	public RoboSpockConfiguration( Project proj ) {
		project = proj
	}

	/**
	 * Sets the path of the android {@link Project} being tested.
	 *
	 * @param t the path of the android {@link Project}.
	 */
	public void setTesting( String t ) {
		this.setAndroid( project.project( t ) )
	}

	/**
	 * Sets the android {@link Project}.
	 *
	 * @param a the android {@link Project} to set.
	 * @throws GradleException if not an android project.
	 */
	public void setAndroid( Project a ) {
		if ( !isAndroid( a ) ) {
			throw new GradleException( a.toString() + " is not an android project" )
		}
		android( a )
	}

	/**
	 * Returns the android {@link Project} to test.
	 *
	 * @return the android {@link Project}.
	 * @throws GradleException if an android project could not be resolved.
	 */
	public Project getAndroid() {
		if ( !this.android ) {
			// Make an attempt to guess the android project.
			if ( !android( findAndroidProject() ) ) {
				throw new GradleException( "RoboSpock: could not guess project and no project found in robospock.testing, please set it!" )
			}

		}

		return this.android
	}

	/**
	 * Verifies that the configuration is sound.
	 *
	 * @return the android {@link Project}.
	 */
	public Project verify() {
		def a = this.getAndroid()
		this.verifyBuildType()
		return a
	}

	//================================================================================
	// Internal logic, setters, etc.
	//================================================================================

	/**
	 * The suffix to remove from {@link #project}.path
	 */
	private static final Pattern PROJECT_SUFFIX_REMOVE = ~/[^a-zA-Z0-9]?test$/

	/**
	 * The {@link Project} that the configuration is being applied on.
	 */
	def Project project

	/**
	 * Verify that the buildType exists.
	 */
	private void verifyBuildType() {
		// Verify that the buildType specified exists.
		if ( !this.android.android.buildTypes.find { it.name == buildType } ) {
			throw new GradleException( "Specified buildType: " + buildType + ' does not exist.' )
		}
	}

	/**
	 * Internal: sets the android DSL property without checks.
	 *
	 * @param a the android {@link Project}
	 * @return the android {@link Project}
	 */
	private Project android( Project a ) {
		this.android = a
		this.testing = a.path
		return a
	}

	/**
	 * Finds an android project that is either the parent of {@link #project}
	 * or has a similar path/name as {@link #project}
	 *
	 * @return an android project, or null if none found.
	 */
	private Project findAndroidProject() {
		def aspirant = project.getParent()
		if ( aspirant != null ) {
			// Parent == android? Found it!
			if ( !isAndroid( aspirant ) ) {
				// Look in subprojects of parents.
				def tryPath = tryPath( this.project.path )
				if ( tryPath.length() < project.path.length() ) {
					aspirant = aspirant.subprojects.find { it.path == tryPath && RoboSpockConfiguration.isAndroid( it ) }
				}
			}
		}

		return aspirant
	}

	/**
	 * Returns a path that assumes that the PROJECT_SUFFIX_REMOVE removed is an android {@link #project}.
	 *
	 * @param path the path to remove suffix from.
	 * @return the path to try for an android {@link #project}.
	 */
	private static String tryPath( String path ) {
		path - PROJECT_SUFFIX_REMOVE
	}

	/**
	 * Checks if the provided project is an android or android-library project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	private static boolean isAndroid( Project project ) {
		return ['android', 'android-library', 'com.android.application', 'com.android.library']
			.find { project.plugins.hasPlugin( it ) }
	}
}
