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

import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet

/**
 * {@link RoboSpockTestVariant} holds the variant,
 * the source set, and the task.
 *
 * @since 2014-12-01
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
public interface RoboSpockVariant {
	/**
	 * Returns the variant.
	 *
	 * @return the variant.
	 */
	public def getVariant()

	/**
	 * Returns the "testCompile" configuration for the variant.
	 *
	 * @return the "testCompile" configuration.
	 */
	public Configuration getTestCompile()

	/**
	 * Returns the "testRuntime" configuration for the variant.
	 *
	 * @return the "testRuntime" configuration.
	 */
	public Configuration getTestRuntime()

	/**
	 * Returns the source set.
	 *
	 * @return the source set.
	 */
	public SourceSet getSourceSet()

	/**
	 * Returns the task.
	 *
	 * @return the task.
	 */
	public Task getTask()
}