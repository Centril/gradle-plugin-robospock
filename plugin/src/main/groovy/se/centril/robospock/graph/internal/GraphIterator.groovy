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

import se.centril.robospock.graph.DirectedAcyclicGraph

/**
 * {@link GraphIterator} iterates the graph.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-26
 */
abstract class GraphIterator<V> implements Iterator<V> {
	DirectedAcyclicGraph<V> graph
	// Using iterators so that we don't process all
	// outgoing edges of a vertex when we don't need to.
	Deque<Iterator<V>> qos
	Set<V> visited
	V next

	/**
	 * Constructs the iterator given the graph, g.
	 *
	 * @param  g the graph.
	 * @param  v the vertex.
	 */
	public GraphIterator( DirectedAcyclicGraph<V> g, V v ) {
		graph = g
		qos = [outs( v )] as ArrayDeque< Iterator<V> >
		visited = [] as Set<V>
		next = v
	}

	/**
	 * Remove not supported.
	 */
	public void remove() {
		throw new UnsupportedOperationException()
	}

	public boolean hasNext() {
		next != null
	}

	public V next() {
		if ( next == null ) {
			throw new NoSuchElementException()
		}

		V now = next
		next = advance()
		return now
	}

	/**
	 * Returns an iterator for the outgoing nodes.
	 *
	 * @param  v the vertex to get outs from.
	 * @return   the iterator.
	 */
	protected Iterator<V> outs( V v ) {
		graph.outs( v ).iterator()
	}

	/**
	 * Returns the next node.
	 *
	 * @return the next node.
	 */
	protected abstract V advance()
}