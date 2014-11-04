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

import com.android.build.gradle.api.AndroidSourceSet
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.testfixtures.ProjectBuilder

import java.util.regex.Pattern

import static se.centril.robospock.RoboSpockConfiguration.isAndroid
import static se.centril.robospock.RoboSpockConfiguration.tryPath

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

	/**
	 * You can provide closures that will be run after
	 * {@link RoboSpockAction} has finished its work.
	 */
	List<Closure> afterConfigured = []

	/**
	 * Read Only robospock testing task, can only be read from
	 * in {@link #afterConfigured(groovy.lang.Closure)}.
	 */
	Task robospockTask

	/**
	 * The perspective (of a project) from which things are applied.
	 */
	Project perspective

	/**
	 * Constructs the configuration.
	 *
	 * @param proj the project to begin configuration with.
	 * @param inverse if true, proj honors {@link #isAndroid(Project)},
	 *  otherwise it's assumed to be the tester.
	 */
	public RoboSpockConfiguration( Project proj ) {
		this.perspective = proj

		if ( isAndroid( proj ) ) {
			setAndroid( proj )
		} else {
			setTester( proj )
		}
	}

	/**
	 * Adds a closure to be executed once the configuration of everything is done.
	 * The only and first argument to the closure is the RoboSpockConfiguration.
	 *
	 * @param c the closure to execute.
	 * @return this RoboSpockConfiguration.
	 */
	public RoboSpockConfiguration afterConfigured( Closure c ) {
		this.afterConfigured << c
		return this
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

	//================================================================================
	// Public non-DSL API:
	//================================================================================

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

	/**
	 * Returns the main source dir for android project.
	 *
	 * @return the source dir as a {@link java.io.File}
	 */
	public File mainSourceDir() {
		return sourceDir( this.android.android.sourceSets.main )
	}

	//================================================================================
	// Internal logic, setters, etc.
	//================================================================================

	protected void setAfterConfigured( List<Closure> l ) {
		this.roboospockTask = t
	}

	protected void setRoboospockTask( Task t ) {
		this.roboospockTask = t
	}

	protected void setPerspective( Project p ) {
		this.perspective = p
	}

	/**
	 * Executes all afterConfigured closures.
	 */
	protected void executeAfterConfigured() {
		this.afterConfigured.each {
			it( this )
		}
	}

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
		def notAndroid = { !isAndroid( it ) }

		// First look in children.
		Project aspirant = this.android.childProjects.values().find {
			def tryPath = tryPath( it.name ) - this.android.name
			tryPath.isEmpty() && notAndroid( it )
		}

		def parent = this.android.parent
		if ( !aspirant ) {
			// Second, look in siblings.
			if ( parent ) {
				aspirant = parent.childProjects.values().find {
					this.android.path == tryPath( it.path ) && notAndroid( it )
				}
			}

			// Third, pray that the user has {android}/src/test/* directory.
			if ( !aspirant ) {
				aspirant = createTesterProject()
			}
		}

		return aspirant
	}

	/**
	 * Creates a new tester project dynamically
	 * and configures to behave correctly.
	 *
	 * The directory used for source files is mostly:
	 *  {android}/src/test/ or {android}/src/unit-test
	 * if the first one was occupied by androidTest.
	 *
	 * @return the created tester project.
	 */
	private Project createTesterProject() {
		File srcDir = mainSourceDir().parentFile
		File testDir = new File( srcDir, 'test' )
		File androidTestDir = sourceDir( this.android.android.sourceSets.androidTest )

		if ( testDir == androidTestDir ) {
			// Houston, we have a problem! androidTest uses 'test' dir.
			// We'll be nice and try to use directory 'unit-test' instead.
			testDir = new File( srcDir, 'unit-test' )
		}

		if ( !testDir.exists() ) {
			return null
		}

		// Create tester project.
		Project aspirant = new ProjectBuilder()
				.withName( this.android.name + '-test' )
				.withParent( this.android )
				.withProjectDir( this.android.projectDir )
				.build();

		// Move buildDir, ensure no conflict.
		aspirant.buildDir = new File( aspirant.buildDir, 'robospock' )

		// Pre apply groovy, clear main SourceSet, correct test SourceSet.
		// Kind of ugly hack to use internal Gradle API, but source is not exposed :(
		aspirant.apply plugin: 'groovy'
		aspirant.sourceSets.main.allSource.@source.each {
			it.srcDirs = []
		}
		aspirant.sourceSets.test.allSource.@source.each {
			it.srcDirs = it.srcDirs.collect {
				new File( testDir, it.name )
			}
		}

		return aspirant
	}

	/**
	 * Finds the first source dir for a sourceSet
	 *
	 * @param ass Android Source Set.
	 * @return the directory as a {@link java.io.File}.
	 */
	private File sourceDir( AndroidSourceSet ass ) {
		ass.java.srcDirs.find().parentFile
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
			def tryPath = tryPath( this.tester.path )
			if ( tryPath.length() < this.tester.path.length() ) {
				aspirant = aspirant.childProjects.values().find {
					it.path == tryPath && isAndroid( it )
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
		return isApplication( project ) || isLibrary( project )
	}

	/**
	 * Checks if the provided project is an android application project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	private static boolean isApplication( Project project ) {
		return project.plugins.hasPlugin('com.android.application') || project.plugins.hasPlugin('android')
	}

	/**
	 * Checks if the provided project is an android-library project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	private static boolean isLibrary( Project project ) {
		return project.plugins.hasPlugin('com.android.library') || project.plugins.hasPlugin('android-library')
	}
}
