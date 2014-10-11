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
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Tests {@link RoboSpockConfiguration}
 *
 * @author Centril < twingoow @ gmail.com >  / Mazdak Farrokhzad.
 * @version 1.0
 * @since Oct , 02, 2014
 */
class RoboSpockConfigurationSpecification extends Specification {
	def "isAndroid"() {
		given:
			def t = testProject()
			def a = androidProject()
			def l = androidLibraryProject()
		expect:
			RoboSpockConfiguration.isAndroid( a )
			RoboSpockConfiguration.isAndroid( l )
			!RoboSpockConfiguration.isAndroid( t )
	}

	def "tryPath"() {
		expect:
			RoboSpockConfiguration.tryPath( path ) == result
		where:
			result      | path
			'app'       | 'app-test'
			'app'       | 'apptest'
			'app'       | 'app_test'
			'app'       | 'app test'
			'app-spock' | 'app-spock'
	}

	def "findAndroidProject"() {
		given:
			def a1 = androidProject()
			def t1 = testProject( 'app-test', a1 )
			def c1 = new RoboSpockConfiguration( t1 )
			def a2 = androidProject()
			def t2 = testProject( 'app-test' )
			def c2 = new RoboSpockConfiguration( t2 )
			def t3 = testProject( '', null )
			def c3 = new RoboSpockConfiguration( t3 )
		expect:
			c1.findAndroidProject() == a1
			c2.findAndroidProject() == a2
			c3.findAndroidProject() == null
	}

	def "verifyBuildType"() {
		given:
			def t = testProject()
			def c1 = new RoboSpockConfiguration( t )
			def c2 = new RoboSpockConfiguration( t )
			c2.buildType = 'lolcats'
			c1.android = androidProject()
			c2.android = c1.android
		when:
			c1.verifyBuildType()
		then:
			notThrown( GradleException )
		when:
			c2.verifyBuildType()
		then:
			thrown( GradleException )
	}

	def "setAndroid"() {
		given:
			def t = testProject()
			def c = new RoboSpockConfiguration( t )
			def a = androidProject()
		when:
			c.setAndroid( t )
		then:
			thrown( GradleException )
		when:
			c.setAndroid( a )
		then:
			notThrown( GradleException )
			c.getAndroid() == a
	}

	def "setTesting"() {
		given:
			def t = testProject()
			def c = new RoboSpockConfiguration( t )
			def a = androidProject()
		when:
			c.setTesting( t.path )
		then:
			thrown( GradleException )
		when:
			c.setTesting( a.path )
		then:
			notThrown( GradleException )
			c.getAndroid() == a
	}

	static final ANDROID_PLUGIN_PATH = 'com.android.tools.build:gradle:+'

	Project root
	Project test
	Project android
	RoboSpockConfiguration config

	def setup() {
		root = ProjectBuilder.builder().build()
	}

	def setupAndroid() {
		android = androidProject()
		config.android = android
		return android
	}

	Project testProject( String name = 'app-test', Project parent = root ) {
		Project proj = ProjectBuilder.builder().withParent( parent ).withName( name ).build()
		return proj
	}

	Project androidProject( String name = 'app', Project parent = root ) {
		return androidProjectCommon( parent, name, 'android' )
	}

	Project androidLibraryProject( String name = 'app-lib', Project parent = root ) {
		return androidProjectCommon( parent, name, 'android-library' )
	}

	Project androidProjectCommon( Project parent, String name, String plugin ) {
		Project proj = ProjectBuilder.builder().withParent( parent ).withName( name ).build()
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
}
