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

import spock.lang.Specification

/**
 * Tests {@link RoboSpockConfiguration}
 *
 * @author Centril < twingoow @ gmail.com >  / Mazdak Farrokhzad.
 * @since Oct , 02, 2014
 */
class RoboSpockConfigurationSpecification extends Specification {
	def "setAfterConfigured"() {
		given:
			def closures = [{ 1 }]
			setupDefault()
			config.setAfterConfigured( closures )
		expect:
			config.afterConfigured.equals( closures )
	}

	def "afterConfigured"() {
		given:
			def a = 1
			setupDefault()
			def r = config.afterConfigured { ++a }
			config.executeAfterConfigured()
		expect:
			a == 2
			r == config
	}

	def "setRobospockTask"() {
		given:
			setupDefault()
			Task t = root.tasks.create( "task" )
			config.setRobospockTask( t )
		expect:
			config.robospockTask == t
	}

	def "setPerspective"() {
		given:
			setupDefault()
			config.setPerspective( root )
		expect:
			config.perspective == root
	}

	class BuildType {
		String name
	}

	class Variant {
		BuildType buildType
		String name
	}

	def "getVariants"() {
		given: "app, lib and respective configs"
			setupDefault()
			def lib = androidLibraryProject()
			def config2 = new RoboSpockConfiguration( lib )
			config2.buildTypes << 'release'

		and: "the variants: debug, release, staging"
			def variants = ['debug', 'release', 'staging'].collectEntries {
				[(it): [name: it, buildType: [name: it] as BuildType] as Variant]
			}
			android.android.applicationVariants << variants.debug
			variants.each {
				lib.android.libraryVariants << it.value
			}

		and: "the variants for each returned"
			def (v1, v2) = [config, config2]*.getVariants().collect { it.name }

		expect: "the variants to be there"
			'debug' in v1
			v1.size() == 1
			'debug' in v2
			'release' in v2
			v2.size() == 2
	}

	def "sdkDir"() {
		given:
			setupDefault()
		expect:
			config.sdkDir() == android.android.sdkDirectory
	}

	def "mainSourceDir"() {
		given:
			setupDefault()
		expect:
			config.mainSourceDir() == new File( android.projectDir, 'src/main' );
	}

	def "isAndroid"() {
		expect:
			RoboSpockConfiguration.isAndroid( project ) == expect
		where:
			expect || project
			false  || testProject()
			true   || androidProject()
			true   || androidProject( true )
			true   || androidLibraryProject()
			true   || androidLibraryProject( true )
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
			def a = androidProject()
			def cases = [
				[testProject( 'app-test', a ), a],
				[testProject( 'app-test' ), a],
				[testProject( 'wrong-test' ), null],
				[testProject( 'x', null ), null],
				[testProject( 'x' ), null]
			]
		expect:
			cases.each {
				assert new RoboSpockConfiguration( it[0] ).findAndroidProject() == it[1]
			}
	}

	def "findTesterProject"() {
		given:
			def a1 = androidProject( false, 'app1' )
			def a2 = androidProject( false, 'app2' )
			def cases = [
				[a1, testProject( 'app1-test', a1 )],
				[a2, testProject( 'test', a2 )],
				[androidProject( false, 'app3' ), testProject( 'app3-test' )],
				[androidProject( false, 'app4' ), null]
			]
		expect:
			cases.each {
				assert new RoboSpockConfiguration( it[0] ).findTesterProject() == it[1]
			}
	}

	def "verifyBuildTypes"() {
		given:
			def t = testProject()
			def c1 = new RoboSpockConfiguration( t )
			def c2 = new RoboSpockConfiguration( t )
			c2.buildTypes << 'lolcats'
			c1.android = androidProject()
			c2.android = c1.android
		when:
			c1.verifyBuildTypes()
		then:
			notThrown( GradleException )
		when:
			c2.verifyBuildTypes()
		then:
			thrown( GradleException )
	}

	def "setTester(Project)"() {
		given:
			def a = androidProject()
			def t = testProject( 'x' )
			def c = new RoboSpockConfiguration( a )
		when:
			println c.getTester()
		then:
			thrown( GradleException )
		when:
			c.setTester( a )
		then:
			thrown( GradleException )
		when:
			c.setTester( t )
		then:
			notThrown( GradleException )
			c.getTester() == t
		when:
			c.setTester( t )
			c.getTester()
		then:
			notThrown( GradleException )
	}

	def "setAndroid(Project)"() {
		given:
			def t = testProject( 'x' )
			def a = androidProject()
			def c = new RoboSpockConfiguration( t )
		when:
			c.getAndroid()
		then:
			thrown( GradleException )
		when:
			c.setAndroid( t )
		then:
			thrown( GradleException )
		when:
			c.setAndroid( a )
		then:
			notThrown( GradleException )
			c.getAndroid() == a
		when:
			c.setAndroid( a )
			c.getAndroid()
		then:
			notThrown( GradleException )
	}

	static final ANDROID_PLUGIN_PATH = 'com.android.tools.build:gradle:+'

	Project root
	Project test
	Project android
	RoboSpockConfiguration config

	def setup() {
		root = ProjectBuilder.builder().withName( 'root' ).build()
	}

	def setupDefault() {
		android = androidProject()
		config = new RoboSpockConfiguration( android )
	}

	Project testProject( String name = 'app-test', Project parent = root ) {
		Project proj = ProjectBuilder.builder().withParent( parent ).withName( name ).build()
		return proj
	}

	Project androidProject( boolean legacy = false, String name = 'app', Project parent = root ) {
		return androidProjectCommon( parent, name, legacy ? 'android' : 'com.android.application' )
	}

	Project androidLibraryProject( boolean legacy = false, String name = 'app-lib', Project parent = root ) {
		return androidProjectCommon( parent, name, legacy ? 'android-library' : 'com.android.library' )
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
