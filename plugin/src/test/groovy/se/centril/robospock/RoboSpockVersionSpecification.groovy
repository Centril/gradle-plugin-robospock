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

import static se.centril.robospock.internal.RoboSpockConstants.*

import spock.lang.Specification

/**
 * Tests {@link RoboSpockVersion}
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-12-29
 */
class RoboSpockVersionSpecification extends RoboSpockSpecification {
	RoboSpockVersion v

	def setup() {
		v = new RoboSpockVersion()
	}

	def "trim"() {
		when:
			v.setRobospock( 'robospock ' )
		then:
			v.robospock == 'robospock'
		when:
			v.setSpock( "spock\t" )
		then:
			v.spock == 'spock'
		when:
			v.setGroovy( "groovy\n" )
		then:
			v.groovy == 'groovy'
		when:
			v.setCglib( "\tcglib\n" )
		then:
			v.cglib == 'cglib'
		when:
			v.setObjenesis( " objenesis" )
		then:
			v.objenesis == 'objenesis'
	}

	def "minimum"() {
		when:
			v.minimum()
		then:
			v.robospock
			v.spock
			v.groovy
			!v.cglib
			!v.objenesis
			v.dependencies().size() == 3
	}

	def "dependencies"() {
		when:
			v.robospock	= '1'
			v.spock		= '2'
			v.groovy	= '3'
			v.cglib		= '4'
			v.objenesis	= '5'
			def deps = [
				MAVEN_GROOVY	+ v.groovy,
				MAVEN_SPOCK		+ v.spock,
				MAVEN_ROBOSPOCK	+ v.robospock
			]
			def c = MAVEN_CGLIB	+ v.cglib
			def o = MAVEN_OBJNESIS + v.objenesis
		then:
			v.dependencies() == deps + [c, o]
		when:
			v.objenesis = ''
			v.cglib = '4'
		then:
			v.dependencies() == deps + [c]
		when:
			v.objenesis = '5'
			v.cglib = ''
		then:
			v.dependencies() == deps + [o]
		when:
			v.objenesis = ''
			v.cglib = ''
		then:
			v.dependencies() == deps
	}
}
