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

import org.gradle.api.Project

/**
 * {@link RoboSpockUtils} provides static utility methods.
 *
 * @since 2014-11-29
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
class RoboSpockUtils {
	/**
	 * Checks if the provided project is an android or android-library project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	public static boolean isAndroid( Project project ) {
		return isApplication( project ) || isLibrary( project )
	}

	/**
	 * Checks if the provided project is an android application project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	public static boolean isApplication( Project project ) {
		return project.plugins.hasPlugin('com.android.application') || project.plugins.hasPlugin('android')
	}

	/**
	 * Checks if the provided project is an android-library project.
	 *
	 * @param project The {@link Project} to check.
	 * @return true if it is.
	 */
	public static boolean isLibrary( Project project ) {
		return project.plugins.hasPlugin('com.android.library') || project.plugins.hasPlugin('android-library')
	}

	/**
	 * Given:
	 * - init, an initial value of type T.
	 * - fn: T -> {x | x in T}, a function from T to a set/collection of T.
	 *
	 * Iteratively acculumulates into a set S:
	 * - S = y(fn, {}, init)
	 *
	 * Where:
	 * - y is a recursive function defined as:
	 *   y(fn, acc, e) = {e} U fn(e) U {y(fn, acc, x) | x in fn(e)}
	 *
	 * When the last element e to be processed results in |fn(e)| = 0 then the
	 * accumulation stops. Therefore it's neccessary for fn to return an empty
	 * collection at some point, otherwise: an infinite loop will occur.
	 *
	 * Examples:
	 * - assert [1, 2, 3, 4, 5] == collectWhileNested( 1 ) {
	 *     switch( it ) {
	 *   	 case 1:  return [2, 3]
	 *   	 case 2:  return [3, 4, 5]
	 *       default: return []
	 *     }
	 *   }
	 * - collectWhileNested( 1 ) { [2] } would cause an infinite loop.
	 *
	 * @param  init		the initial value to pass to fn, non-null.
	 * @param  fn		the non-null function fn: T -> {x | x in T}.
	 *             		Returned elements are required to be non-null.
	 *             		A null return value is interpreted as an empty list.
	 * @return the accumulated results as a set.
	 */
	public static <T> Set<T> collectWhileNested( T init, Closure<Collection<T>> fn ) {
		T curr
		Set<T> acc = [init] as HashSet<T>
		Queue<T> queue = acc as ArrayDeque<T>

		while ( (curr = queue.poll()) != null ) {
			def add = fn( curr )
			if ( add ) {
				acc += add
				queue += add
			}
		}

		return acc
	}
}