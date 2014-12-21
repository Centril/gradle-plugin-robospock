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

/**
 * Tests {@link RoboSpockUtils}
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-11-29
 */
class RoboSpockUtilsSpecification extends RoboSpockSpecification {
	def "isAndroid"() {
		expect:
			RoboSpockUtils.isAndroid( project ) == expect
		where:
			expect || project
			false  || testProject()
			true   || androidProject()
			true   || androidProject( true )
			true   || androidLibraryProject()
			true   || androidLibraryProject( true )
	}

	def "isLibrary"() {
		expect:
			RoboSpockUtils.isLibrary( project ) == expect
		where:
			expect || project
			false  || testProject()
			false  || androidProject()
			false  || androidProject( true )
			true   || androidLibraryProject()
			true   || androidLibraryProject( true )
	}

	def "isApplication"() {
		expect:
			RoboSpockUtils.isApplication( project ) == expect
		where:
			expect || project
			false  || testProject()
			true   || androidProject()
			true   || androidProject( true )
			false  || androidLibraryProject()
			false  || androidLibraryProject( true )
	}

	def "collectWhileNested"() {
		expect:
			RoboSpockUtils.collectWhileNested( i, c ) == (r as HashSet)
		where:
			i	 | r				| c
			1	 | [1]				| {[]}
			1	 | [1, 2, 3] 		| this.&listWhenOne
			1	 | [1, 2, 3, 4, 5]	| this.&listWhenTwo
			1	 | [1, 2, 3, 4]		| this.&listOverlap
	}

	def listWhenOne( it ) { it == 1 ? [2, 3] : [] }
	def listWhenTwo( it ) { it == 2 ? [4, 5] : listWhenOne( it ) }
	def listOverlap( it ) { it == 2 ? [3, 4] : listWhenOne( it ) }

	def "collectWhileNested is passed null"() {
		when:
			RoboSpockUtils.collectWhileNested( 1, null )
		then:
			thrown( NullPointerException )
		when:
			RoboSpockUtils.collectWhileNested( null, {} )
		then:
			def e1 = thrown( Exception )
		when:
			RoboSpockUtils.collectWhileNested( null, null )
		then:
			def e2 = thrown( Exception )
		when:
			def r = RoboSpockUtils.collectWhileNested( 1, {[]} )
		then:
			r == ([1] as HashSet)
			notThrown( NullPointerException )
	}
}
