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

import org.gradle.api.GradleException
import org.gradle.api.Project

import java.util.regex.Pattern

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin

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
	 * (Required) The android {@link Project} under test.
	 * One of {@link #tester} or {@link #android} must be set.
	 */
	Project android

	/**
	 * (Required) The tester {@link Project} that the configuration is being applied on.
	 * One of {@link #tester} or {@link #android} must be set.
	 */
	Project tester

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

	/**
	 * Constructs the configuration.
	 *
	 * @param proj the project to begin configuration with.
	 * @param inverse if true, proj honors {@link #isAndroid(Project)},
	 *  otherwise it's assumed to be the tester.
	 */
	public RoboSpockConfiguration( Project proj, boolean inverse = false ) {
		if ( inverse ) {
			setAndroid( proj )
		} else {
			setTester( proj )
		}
	}

	/**
	 * Sets the path of the tester {@link Project} testing the android project.
	 *
	 * @param t the path of the tester {@link Project}.
	 */
	public void setTester( String t ) {
		this.setTester( android.project( t ) )
	}

	/**
	 * Sets the tester {@link Project}.
	 *
	 * @param t the tester {@link Project} to set.
	 * @throws GradleException if it's an android project.
	 */
	public void setTester( Project t ) {
		if ( isAndroid( t ) ) {
			throw new GradleException( "${t} must not be an android project." )
		}
		this.tester = t
	}

	/**
	 * Sets the path of the android {@link Project} being tested.
	 *
	 * @param t the path of the android {@link Project}.
	 */
	public void setAndroid( String t ) {
		this.setAndroid( tester.project( t ) )
	}

	/**
	 * Sets the android {@link Project}.
	 *
	 * @param a the android {@link Project} to set.
	 * @throws GradleException if not an android project.
	 */
	public void setAndroid( Project a ) {
		if ( !isAndroid( a ) ) {
			throw new GradleException( "${a} is not an android project" )
		}
		this.android = a
	}

	/**
	 * Returns the android {@link Project} to test.
	 *
	 * @return the android {@link Project}.
	 * @throws GradleException if an android project could not be resolved.
	 */
	public Project getAndroid() {
		// Make an attempt to guess the android project.
		if ( !this.android && !(this.android = findAndroidProject()) ) {
			throw new GradleException( "RoboSpock: could not guess project and no project found in robospock.android, please set it!" )
		}

		return this.android
	}

	/**
	 * Returns the tester {@link Project} to test.
	 *
	 * @return the tester {@link Project}.
	 * @throws GradleException if a tester project could not be resolved.
	 */
	public Project getTester() {
		// Make an attempt to guess the tester project.
		if ( !this.tester && !(this.tester = findTesterProject()) ) {
			throw new GradleException( "RoboSpock: could not guess project and no project found in robospock.tester, please set it!" )
		}

		return this.tester
	}

	/**
	 * Verifies that the configuration is sound.
	 *
	 * @return the android {@link Project}.
	 */
	public void verify() {
		this.getAndroid()
		this.verifyBuildType()

		this.getTester()
	}

	/**
	 * Returns the android-sdk directory as a {@link java.io.File}.
	 *
	 * @return android-sdk directory.
	 */
	public File sdkDir() {
		getAndroid().android.sdkDirectory
	}

	//================================================================================
	// Internal logic, setters, etc.
	//================================================================================

	/**
	 * The suffix to remove from {@link #tester}.path
	 */
	private static final Pattern PROJECT_SUFFIX_REMOVE = ~/[^a-zA-Z0-9]?test$/

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
	 * Finds an android project that is either the parent of {@link #tester}
	 * or has a similar path/name as {@link #tester}
	 *
	 * @return an android project, or null if none found.
	 */
	private Project findTesterProject() {
		def notAndroid = { !RoboSpockConfiguration.isAndroid( it ) }

		// First look in children.
		Project aspirant = this.android.childProjects.values().find {
			def tryPath = RoboSpockConfiguration.tryPath( it.name ) - this.android.name
			tryPath.isEmpty() && notAndroid( it )
		}

		// Second, look in siblings.
		def parent = this.android.parent
		if ( !aspirant && parent ) {
			aspirant = parent.childProjects.values().find {
				this.android.path == RoboSpockConfiguration.tryPath( it.path ) && notAndroid( it )
			}
		}

		return aspirant
	}

	/**
	 * Finds an android project that is either the parent of {@link #tester}
	 * or has a similar path/name as {@link #tester}
	 *
	 * @return an android project, or null if none found.
	 */
	private Project findAndroidProject() {
		// Parent == android? Found it!
		Project aspirant = this.tester.parent
		if ( aspirant != null && !isAndroid( aspirant ) ) {
			// Look in siblings.
			def tryPath = RoboSpockConfiguration.tryPath( this.tester.path )
			if ( tryPath.length() < this.tester.path.length() ) {
				aspirant = aspirant.childProjects.values().find {
					it.path == tryPath && RoboSpockConfiguration.isAndroid( it )
				}
			}
		}

		return aspirant
	}

	/**
	 * Returns a path that assumes that the PROJECT_SUFFIX_REMOVE removed is an android {@link #tester}.
	 *
	 * @param path the path to remove suffix from.
	 * @return the path to try for an android {@link #tester}.
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
		return project.plugins.hasPlugin( AppPlugin ) || project.plugins.hasPlugin( LibraryPlugin )
	}
}
