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
import org.gradle.api.Task
import org.gradle.api.plugins.ExtensionAware

import java.util.regex.Pattern

import static se.centril.robospock.internal.RoboSpockUtils.*
import static se.centril.robospock.RoboSpockConfiguration.tryPath

import se.centril.robospock.graph.DirectedAcyclicGraph

/**
 * {@link RoboSpockConfiguration} determines how
 * the {@link RoboSpockPlugin} should be used.
 *
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
	 * (Optional) version holds the versions of dependencies.
	 */
	RoboSpockVersion version

	/**
	 * (Optional) The buildTypes being tested.
	 * Default = ['debug']
	 */
	List<String> buildTypes	= ['debug']

	/**
	 * You can provide closures that will be run after
	 * {@link RoboSpockAction} has finished its work.
	 */
	List<Closure> afterConfigured = []

	/**
	 * (Read Only) Graph of testing variant tasks, can only be
	 * read from in {@link #afterConfigured(groovy.lang.Closure)}.
	 */
	DirectedAcyclicGraph<RoboSpockVariant> graph

	/**
	 * (Read Only) The perspective (of a project)
	 * from which things are applied.
	 */
	Project perspective

	/**
	 * Constructs the configuration.
	 *
	 * @param proj	the project to begin configuration with.
	 * @param ver	the version configuration object.
	 */
	public RoboSpockConfiguration( Project proj, RoboSpockVersion ver ) {
		setPerspective( proj )
		setVersion( ver )
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
	// Public non-DSL API: - These parts are internal and are subject to change:
	//================================================================================

	/**
	 * Verifies that the configuration is sound.
	 *
	 * @return the android {@link Project}.
	 */
	public void verify() {
		this.getAndroid()
		this.verifyBuildTypes()

		this.getTester()
	}

	/**
	 * Returns the android-sdk directory as a {@link java.io.File}.
	 *
	 * @return android-sdk directory.
	 */
	public File sdkDir() {
		this.android.android.sdkDirectory
	}

	/**
	 * Returns the main source dir for android project.
	 *
	 * @return the source dir as a {@link java.io.File}
	 */
	public File mainSourceDir() {
		def ass = this.android.android.sourceSets.main
		return ass.java.srcDirs.find().parentFile
	}

	/**
	 * Returns the variants of the android project excluding those
	 * that that are not in our {@link #buildTypes}.
	 *
	 * @return the variants.
	 */
	public List<Object> getVariants() {
		def android = this.android.android
		def v = isLibrary( this.android ) ? android.libraryVariants : android.applicationVariants
		v = v.collect().findAll { it.buildType.name in this.buildTypes }
		return v
	}

	//================================================================================
	// Deny public access:
	//================================================================================

	/**
	 * Constructs the configuration, for testing purposes.
	 */
	protected RoboSpockConfiguration() {}

	/**
	 * Sets list of afterConfigured closure.
	 *
	 * @param l the list.
	 */
	protected void setAfterConfigured( List<Closure> l ) {
		this.afterConfigured = l
	}

	/**
	 * Sets the graph.
	 *
	 * @param g the graph.
	 */
	protected void setGraph(DirectedAcyclicGraph<RoboSpockVariant> g ) {
		this.graph = g
	}

	/**
	 * Sets the perspective using setAndroid
	 * or setTester depending on context.
	 *
	 * @param p the perspective.
	 */
	protected void setPerspective( Project p ) {
		this.perspective = p

		if ( isAndroid( p ) ) {
			setAndroid( p )
		} else {
			setTester( p )
		}
	}

	/**
	 * Sets the version.
	 *
	 * @param v the version.
	 */
	protected void setVersion( RoboSpockVersion v ) {
		this.version = v
	}

	//================================================================================
	// Internal logic, setters, etc.
	//================================================================================

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
	 * Verify that all the buildTypes exists.
	 */
	private void verifyBuildTypes() {
		// Verify that the buildTypes specified all exist.
		buildTypes.each { type ->
			if ( !this.android.android.buildTypes.find { it.name == type } ) {
				throw new GradleException( "Specified buildType: " + type + ' does not exist.' )
			}
		}
	}

	/**
	 * Finds a tester project that is either the child of {@link #android}
	 * or has a similar path/name as {@link #android}
	 *
	 * @return a tester project, or null if none found.
	 */
	private Project findTesterProject() {
		def notAndroid = { !isAndroid( it ) }

		// First look in children.
		Project aspirant = this.android.childProjects.values().find {
			def tryPath = tryPath( it.name ) - this.android.name
			tryPath.isEmpty() && notAndroid( it )
		}

		def parent = this.android.parent
		// Second, look in siblings.
		if ( !aspirant && parent ) {
			aspirant = parent.childProjects.values().find {
				this.android.path == tryPath( it.path ) && notAndroid( it )
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
		Project parent = this.tester.parent
		if ( parent != null && !isAndroid( parent ) ) {
			// Look in siblings.
			def tryPath = tryPath( this.tester.path )
			if ( tryPath.length() < this.tester.path.length() ) {
				return parent.childProjects.values().find {
					it.path == tryPath && isAndroid( it )
				}
			} else {
				return null
			}
		}

		return parent
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
}
