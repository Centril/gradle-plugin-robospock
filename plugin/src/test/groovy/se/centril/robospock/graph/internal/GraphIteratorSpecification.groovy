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

package se.centril.robospock.graph.internal

import spock.lang.Specification

/**
 * Tests {@link GraphIterator}.
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-12-27
 */
class GraphIteratorSpecification extends GraphSpecification {
	GraphIterator<Integer> i

	def graph() {
		/* This is how the graph looks like:
		 *
		 *          1
		 *          |---
		 *         /|\  \
		 *        / | \  \
		 *       2  3  4  5
		 *      /| / \ |\
		 *     / |/   \| \
		 *    6  7     8  9
		 *   /|  \     |\
		 *  / |   \    | \
		 * 10 11  |   12 13
		 *  \ |   |    | /
		 *   \|   \    |/
		 *    \   |    /
		 *     \  \   /
		 *      --14--
		 */
		g.clear()
		g.add( 1, 2..5 )
		g.add( 2, 6..7 )
		g.add( 3, 7..8 )
		g.add( 4, 8..9 )
		g.add( 6, 10..11 )
		g.add( 7, 14 )
		g.add( 8, 12..13 )
		g.add( 10..13, 14 )
	}
}
