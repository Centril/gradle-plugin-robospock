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

import com.android.SdkConstants

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder

import se.centril.robospock.fixtures.BuildType
import se.centril.robospock.fixtures.Variant

import spock.lang.Specification

/**
 * {@link RoboSpockSpecification} provides utilities for testing the plugin.
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-11-29
 */
abstract class RoboSpockSpecification extends Specification {
	static final ANDROID_PLUGIN_PATH = 'com.android.tools.build:gradle:+'

	protected Project root
	protected Project test
	protected Project android
	protected RoboSpockConfiguration config

	public def setup() {
		root = ProjectBuilder.builder().withName( 'root' ).build()
	}

	protected Variant variant( String vName, String btName ) {
		[name: vName, buildType: [name: btName] as BuildType] as Variant
	}

	protected def setupDefault( boolean library = false ) {
		android = library ? androidLibraryProject() : androidProject()
		config = new RoboSpockConfiguration( android, new RoboSpockVersion() )
		return android
	}

	protected Project testProject( String name = 'app-test', Project parent = root ) {
		Project test = builder( parent ).withName( name ).build()
		test.apply plugin: 'groovy'
		return test
	}

	protected Project androidProject( boolean legacy = false, String name = 'app', Project parent = root ) {
		return androidProjectCommon( parent, name, legacy ? 'android' : 'com.android.application' )
	}

	protected Project androidLibraryProject( boolean legacy = false, String name = 'app-lib', Project parent = root ) {
		return androidProjectCommon( parent, name, legacy ? 'android-library' : 'com.android.library' )
	}

	protected Project androidProjectCommon( Project parent, String name, String plugin ) {
		Project proj = builder( parent ).withName( name ).build()
		def file = new File( proj.rootDir, SdkConstants.FN_LOCAL_PROPERTIES );
		file.write( "sdk.dir=/home" )

		proj.buildscript {
			repositories {
				mavenCentral()
			}
			dependencies {
				classpath ANDROID_PLUGIN_PATH
			}
		}

		proj.repositories {
			mavenCentral()
		}

		proj.apply plugin: plugin

		return proj
	}

	protected ProjectBuilder builder( Project parent ) {
		return ProjectBuilder.builder().withParent( parent )
	}
}
