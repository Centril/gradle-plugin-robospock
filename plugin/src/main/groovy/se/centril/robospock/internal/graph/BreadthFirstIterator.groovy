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

package se.centril.robospock.internal.graph

/**
 * {@link BreadthFirstIterator} iterates the graph,
 * first going to adjacent out-going vertrices
 * and then the depth. Using the BFS algorithm.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-26
 */
class BreadthFirstIterator<V> extends GraphIterator<V> {
	public BreadthFirstIterator( DirectedAcyclicGraph<V> g, V v ) {
		super( g, v )
	}

	protected V advance() {
		// Loop queue of iterators until we found an unvisited node.
		Iterator<V> i
		while ( (i = qos.peek()) != null ) {
			while ( i.hasNext() ) {
				V curr = i.next()
				// Found a node == bail, add iterator for children.
				if ( !(curr in visited) ) {
					pollWhenLast( i )
					qos << outs( curr )
					visited << curr
					return curr
				}
			}
			pollWhenLast( i )
		}
		// If we reach this, queue has been exhausted & no unvisited nodes found.
		return null
	}

	private void pollWhenLast( Iterator<V> i ) {
		if ( !i.hasNext() ) {
			qos.pop()
		}
	}
}