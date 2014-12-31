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

import se.centril.robospock.RoboSpockVersion
import se.centril.robospock.graph.DirectedAcyclicGraph
import se.centril.robospock.graph.internal.DirectedAcyclicGraphImpl

import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests {@link RoboSpockConfiguration}
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-10-02
 */
class RoboSpockConfigurationSpecification extends RoboSpockSpecification {
	@Shared def ver = new RoboSpockVersion()

	//================================================================================
	// Gradle DSL:
	//================================================================================

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

	def "setTester(Project)"() {
		given:
			def a = androidProject()
			def t = testProject( 'x' )
			def c = new RoboSpockConfiguration( a, ver )
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
			def c = new RoboSpockConfiguration( t, ver )
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

	//================================================================================
	// Public non-DSL API: - These parts are internal and are subject to change.
	//================================================================================

	def "verify"() {
		when:
			setupDefault()
			config.setTester( testProject() )
			config.verify()
		then:
			notThrown( GradleException )
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

	def "getVariants"() {
		given: "app, lib and respective configs"
			setupDefault()
			def lib = androidLibraryProject()
			def config2 = new RoboSpockConfiguration( lib, ver )
			config2.buildTypes << 'release'

		and: "the variants: debug, release, staging"
			def variants = ['debug', 'release', 'staging'].collectEntries {
				[(it): variant( it, it )]
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

	//================================================================================
	// Deny public access:
	//================================================================================

	def "setAfterConfigured"() {
		given:
			def closures = [{ 1 }]
			setupDefault()
			config.setAfterConfigured( closures )
		expect:
			config.afterConfigured.equals( closures )
	}

	def "setGraph"() {
		given:
			setupDefault()
			DirectedAcyclicGraph<RoboSpockVariant> g = new DirectedAcyclicGraphImpl<RoboSpockVariant>()
			config.setGraph( g )
		expect:
			config.graph == g
	}

	def "setPerspective"() {
		when:
			def t = testProject( 'x' )
			def c1 = new RoboSpockConfiguration()
			c1.setPerspective( t )
		then:
			c1.@android == null
			c1.@tester == t
			c1.@perspective == t
		when:
			def a = androidProject()
			def c2 = new RoboSpockConfiguration()
			c2.setPerspective( a )
		then:
			c2.@android == a
			c2.@tester == null
			c2.@perspective == a
	}

	def "setVersion( RoboSpockVersion v )"() {
		given:
			def c = new RoboSpockConfiguration()
		when:
			c.setVersion( ver )
		then:
			c.version == ver
	}

	//================================================================================
	// Internal logic, setters, etc:
	//================================================================================

	def "verifyBuildTypes"() {
		given:
			def t = testProject()
			def c1 = new RoboSpockConfiguration( t, ver )
			def c2 = new RoboSpockConfiguration( t, ver )
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
				assert new RoboSpockConfiguration( it[0], ver ).findAndroidProject() == it[1]
			}
	}

	def "findTesterProject"() {
		given:
			def a1 = androidProject( false, 'app1' )
			def a2 = androidProject( false, 'app2' )
			def a5 = androidProject( false, 'app5' )
			def a5test = androidProject( false, 'app5-test', a5 )
			def a1test = testProject( 'app1-test', a1 )
			def cases = [
				[a1, a1test],
				[a1, a1test],
				[a2, testProject( 'test', a2 )],
				[androidProject( false, 'app3' ), testProject( 'app3-test' )],
				[androidProject( false, 'app4' ), null],
				[a5, null]
			]
		expect:
			cases.each {
				assert new RoboSpockConfiguration( it[0], ver ).findTesterProject() == it[1]
			}
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
}
