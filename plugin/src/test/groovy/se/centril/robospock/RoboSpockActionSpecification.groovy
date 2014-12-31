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

import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests {@link RoboSpockAction}
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-12-31
 */
class RoboSpockActionSpecification extends RoboSpockSpecification {
	@Shared def dver = new RoboSpockVersion()
	def i = 0

	def setup() {
		test = testProject()
		config = new RoboSpockConfiguration( test, dver )
	}

	def fixConfigure() {
		RoboSpockConfigurator.metaClass.configure = { i = 1337 }
	}

	def "perform"() {
		given:
			fixConfigure()
		when:
			RoboSpockAction.perform( config )
			config.perspective.evaluate()
		then:
			i == 1337
			notThrown( GradleException )
	}

	def "execute"() {
		given:
			def cfgr = Mock(RoboSpockConfigurator)
			def action = new RoboSpockAction()
			fixConfigure()
		when:
			action.execute( config )
		then:
			i == 1337
	}

	def "checkGradleVersion"() {
		when:
			RoboSpockAction.checkGradleVersion( config )
		then:
			notThrown( GradleException )
		when:
			test.gradle.metaClass.getGradleVersion = { '2.3' }
			RoboSpockAction.checkGradleVersion( config )
		then:
			notThrown( GradleException )
		when:
			test.gradle.metaClass.getGradleVersion = { '2.1' }
			RoboSpockAction.checkGradleVersion( config )
		then:
			thrown( GradleException )
	}
}
