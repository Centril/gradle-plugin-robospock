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
import static se.centril.robospock.RoboSpockConfigurator.addJCenterBuildScript

/**
 * {@link RoboSpockAction}: Is a standalone action that executes a {@link RoboSpockConfigurator}.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @version 1.0
 * @since Oct, 02, 2014
 */
class RoboSpockAction implements Action<RoboSpockConfiguration> {
	static void perform( RoboSpockConfiguration cfg ) {
		// Add jcenter to buildscript repo.
		addJCenterBuildScript( cfg )

		// Configure robospock.
		cfg.perspective.afterEvaluate {
			new RoboSpockAction().execute( cfg )
		}
	}

	@Override
	void execute( RoboSpockConfiguration cfg ) {
		new RoboSpockConfigurator( cfg ).configure()
	}
}
