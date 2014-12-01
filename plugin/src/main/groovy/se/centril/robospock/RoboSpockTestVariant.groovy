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
import org.gradle.api.tasks.SourceSet

/**
 * {@link RoboSpockTestVariant} holds the variant
 * and the corresponding source set hierarchy
 * and the task hierarchy.
 *
 * The hierarchies are inverted and start with
 * the deepest to and bubbles up to the top level.
 *
 * @since 2014-12-01
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
public interface RoboSpockTestVariant {
	/**
	 * Returns the variant.
	 *
	 * @return the variant.
	 */
	public def getVariant()

	/**
	 * Returns the hierarchy of the source sets involved with the deepest first.
	 *
	 * @return the source set hiearchy.
	 */
	public Iterable<SourceSet> getSourceSets()

	/**
	 * Returns the hierarchy of the tasks involved with the deepest first.
	 * Only the deepest does any actual work, the others just depend in a hiearchy.
	 *
	 * @return the source set hiearchy.
	 */
	public Iterable<Task> getTasks()
}