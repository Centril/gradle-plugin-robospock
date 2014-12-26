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
 * {@link DepthFirstIterator iterates the graph,
 * first going for the depth, and then to adjacent
 * out-going vertrices. Using the DFS algorithm.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-26
 */
class DepthFirstIterator<V> extends GraphIterator<V> {
	public DepthFirstIterator( DirectedAcyclicGraph<V> g, V v ) {
		super( g, v )
	}

	protected V advance() {
		visited << next

		Iterator<V> i = qos.peek()
		backtrack( i )
		while ( next in visited ) {
			backtrack( i )
		}

		qos.push( outs( next ) )
		return next
	}

	private void backtrack( Iterator<V> i ) {
		while ( !i.hasNext() ) {
			// No more nodes, back out a level
			qos.pop()
			if ( qos.isEmpty() ) {
				// Done.
				next = null
				return
			}
			i = qos.peek()
		}
		next = i.next()
	}
}