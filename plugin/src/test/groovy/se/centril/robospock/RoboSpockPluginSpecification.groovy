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
 * Tests {@link RoboSpockPlugin}
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-12-31
 */
class RoboSpockPluginSpecification extends RoboSpockSpecification {
	def "apply"() {
		given:
			int i = 0
			test = testProject()
			def p = new RoboSpockPlugin()
		when:
			p.apply( test )
		then:
			test.robospock instanceof RoboSpockConfiguration
			test.robospock.version instanceof RoboSpockVersion
	}
}
