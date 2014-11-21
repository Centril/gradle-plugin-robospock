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

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project

import static se.centril.robospock.RoboSpockConfigurator.addJCenterBuildScript

/**
 * {@link RoboSpockAction}: Is a standalone action that executes a {@link RoboSpockConfigurator}.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since Oct, 02, 2014
 */
class RoboSpockAction implements Action<RoboSpockConfiguration> {
	/**
	 * Performs the all the work of the plugin.
	 *
	 * @param cfg An instance of {@link RoboSpockConfiguration}.
	 */
	static void perform( RoboSpockConfiguration cfg ) {
		checkGradleVersion( cfg )

		// Add jcenter to buildscript repo.
		addJCenterBuildScript( cfg )

		// Configure robospock.
		cfg.perspective.afterEvaluate {
			new RoboSpockAction().execute( cfg )
		}
	}

	@Override
	void execute( RoboSpockConfiguration cfg ) {
		checkGradleVersion( cfg )
		new RoboSpockConfigurator( cfg ).configure()
	}

	/**
	 * Ensures that the gradle version used is >= 2.2.
	 *
	 * @param cfg An instance of {@link RoboSpockConfiguration}.
	 */
	private static void checkGradleVersion( RoboSpockConfiguration cfg ) {
		// Check gradle version, ensure >= 2.2.
		def v = cfg.perspective.gradle.gradleVersion
		if ( v.toFloat() < 2.2 ) {
			throw new GradleException( "RoboSpock requires gradle >= 2.2, but current is: $v" )
		}
	}
}
