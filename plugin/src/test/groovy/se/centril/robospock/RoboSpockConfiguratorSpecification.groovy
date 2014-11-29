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
 * Tests {@link RoboSpockConfigurator}
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-10-02
 */
class RoboSpockConfiguratorSpecification extends RoboSpockSpecification {
	RoboSpockConfigurator configurator

	def setup() {
		setupDefault()
		test = testProject()
		configurator = new RoboSpockConfigurator( config )
	}

	def "getSubprojects"() {
		given:
			def libA = androidLibraryProject( false, 'libraryA' )
			def libB = androidLibraryProject( false, 'libraryB' )
			libB.dependencies {
				compile libA
			}
			android.dependencies {
				compile libB
			}
		expect:
			configurator.getSubprojects( android ).containsAll( [libB, libA] )
	}

	def "addDependencies"() {
		when:
			config.spockVersion     = '0.7-groovy-2.0'
			config.groovyVersion    = '2.3.6'
			config.robospockVersion = '0.5.+'
			config.cglibVersion     = '3.1'
			config.objenesisVersion = '2.1'
			configurator.addDependencies()
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
			configurator.addDependencies()
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
			def sdk = android.android.sdkDirectory.toURI().toString()
			configurator.addAndroidRepositories()
		expect:
			test.repositories
				.findAll { it instanceof MavenArtifactRepository }
				.collect { ((MavenArtifactRepository) it).url.toString() }
				.containsAll( [
					test.file( "${sdk}/extras/android/m2repository" ).toURL().toString(),
					test.file( "${sdk}/extras/google/m2repository" ).toURL().toString()
				] )
	}
}
