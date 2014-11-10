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
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

/**
 * Tests {@link RoboSpockPlugin}
 *
 * @author Centril <twingoow @ gmail.com> / Mazdak Farrokhzad.
 * @version 1.0
 * @since Oct, 02, 2014
 */
class RoboSpockActionSpecification extends Specification {
	def "getSubprojects"() {
		given:
			def libA = androidLibraryProject( 'libraryA' )
			def libB = androidLibraryProject( 'libraryB' )
			libB.dependencies {
				compile libA
			}
			setupAndroid().dependencies {
				compile libB
			}
		expect:
			action.getSubprojects( android ).containsAll( [libB, libA] )
	}

	def "addDependencies"() {
		when:
			config.spockVersion     = '0.7-groovy-2.0'
			config.groovyVersion    = '2.3.6'
			config.robospockVersion = '0.5.+'
			config.cglibVersion     = '3.1'
			config.objenesisVersion = '2.1'
			action.addDependencies( config )
		then:
			test.configurations
				.find { it.name == 'testCompile' }.allDependencies
				.collect { "${it.group}:${it.name}:${it.version}" }
				.containsAll( [
					"org.codehaus.groovy:groovy-all:${config.groovyVersion}",
					"org.spockframework:spock-core:${config.spockVersion}",
					"org.robospock:robospock:${config.robospockVersion}",
					"cglib:cglib-nodep:${config.cglibVersion}",
					"org.objenesis:objenesis:${config.objenesisVersion}",
				] )
		when:
			config.spockVersion = '0.7-groovy-2.0'
			config.groovyVersion = '2.3.6'
			config.robospockVersion = '0.5.+'
			config.cglibVersion = ''
			config.objenesisVersion = ''
			action.addDependencies( config )
		then:
			test.configurations
					.find { it.name == 'testCompile' }.allDependencies
					.collect { "${it.group}:${it.name}:${it.version}" }
					.containsAll( [
					"org.codehaus.groovy:groovy-all:${config.groovyVersion}",
					"org.spockframework:spock-core:${config.spockVersion}",
					"org.robospock:robospock:${config.robospockVersion}"
			] )
	}

	def "addAndroidRepositories"() {
		given:
			def sdk = setupAndroid().android.sdkDirectory.toURI().toString()
			action.addAndroidRepositories( config )
		expect:
			test.repositories
				.findAll { it instanceof MavenArtifactRepository }
				.collect { ((MavenArtifactRepository) it).url.toString() }
				.containsAll( [
					test.file( "${sdk}/extras/android/m2repository" ).toURL().toString(),
					test.file( "${sdk}/extras/google/m2repository" ).toURL().toString()
				] )
	}

	static final ANDROID_PLUGIN_PATH = 'com.android.tools.build:gradle:+'

	Project root
	Project test
	Project android
	RoboSpockConfiguration config
	RoboSpockAction action

	def setup() {
		root = ProjectBuilder.builder().build()
		test = testProject()
		test.apply plugin: 'groovy'
		config = new RoboSpockConfiguration( test )
		action = new RoboSpockAction()
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
