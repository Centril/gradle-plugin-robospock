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

/**
 * {@link RoboSpockVersion} is a gradle extension
 * object holding all of the configurable versions.
 *
 * @since 2014-12-29
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class RoboSpockVersion {
	//================================================================================
	// Gradle DSL
	//================================================================================

	/**
	 * (Optional) The robospock version to use.
	 * Default: Latest known version (0.5.+)
	 */
	String robospock	=	'0.5.+'

	/**
	 * (Optional) The spock-framework version to use.
	 * Default: Latest known version (0.7-groovy-2.0)
	 */
	String spock		=	'0.7-groovy-2.0'

	/**
	 * (Optional) The groovy version to use.
	 * Default: Latest known version (2.3.6).
	 */
	String groovy   	=	'2.3.6'

	/**
	 * (Optional) The clib version to use as dependency.
	 * Default: Latest known version (3.1).
	 * If the dependency is unwanted, set the string to empty.
	 */
	String cglib		=	'3.1'

	/**
	 * (Optional) The objenesis version to use as dependency.
	 * Default: Latest known version (2.1).
	 * If the dependency is unwanted, set the string to empty.
	 */
	String objenesis	=	'2.1'

	/**
	 * Remove dependencies not needed in minimal
	 * setup: {@link #cglib}, {@link #objenesis}.
	 */
	public void minimum() {
		cglib = ''
		objenesis = ''
	}

	/**
	 * Returns a list of dependency gradle object notation.
	 */
	public List<String> dependencies() {
		def deps = [
			MAVEN_GROOVY	+ groovy,
			MAVEN_SPOCK		+ spock,
			MAVEN_ROBOSPOCK	+ robospock
		]

		if ( cglib ) {
			deps << MAVEN_CGLIB	+ cglib
		}

		if ( objenesis ) {
			deps << MAVEN_OBJNESIS + objenesis
		}

		return deps
	}

	public void setRobospock( String value ) {
		this.robospock = value.trim()
	}

	public void setSpock( String value ) {
		this.spock = value.trim()
	}

	public void setGroovy( String value ) {
		this.groovy = value.trim()
	}

	public void setCglib( String value ) {
		this.cglib = value.trim()
	}

	public void setObjenesis( String value ) {
		this.objenesis = value.trim()
	}
}
