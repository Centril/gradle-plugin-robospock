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
 * {@link UnmodifiableDAGImpl}: is an unmodifiable view of a DAG.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-27
 */
class UnmodifiableDAGImpl<V> implements DirectedAcyclicGraph {
	DirectedAcyclicGraph<V> g

	public UnmodifiableDAGImpl( DirectedAcyclicGraph<V> dag ) {
		g = dag
	}

	private void unsupported() { throw new UnsupportedOperationException() }

	//================================================================================
	// Adding operations.
	//================================================================================

	boolean add( V vertex ) { unsupported() }
	boolean addAll( Collection<? extends V> c ) { unsupported() }
	boolean add( V from, V to ) { unsupported() }
	boolean add( V from, Collection<V> to ) { unsupported() }
	boolean add( Collection<V> from, to ) { unsupported() }

	//================================================================================
	// Removing operations.
	//================================================================================

	boolean remove( Object o ) { unsupported() }
	boolean removeAll( Collection<?> c ) { unsupported() }
	boolean retainAll( Collection<?> c ) { unsupported() }
	void clear() { unsupported() }
	boolean remove( V from, V to ) { unsupported() }
	boolean remove( V from, Collection<V> to ) { unsupported() }
	boolean remove( Collection<V> from, V to ) { unsupported() }
	void clearEdges() { unsupported() }

	//================================================================================
	// Contains & edging operations.
	//================================================================================

	int size() { g.size() }
	int edges() { g.edges() }
	boolean isEmpty() { g.isEmpty() }
	boolean contains( Object vertex ) { g.contains( vertex ) }
	boolean containsAll( Collection<?> c ) { g.containsAll( c ) }
	boolean containsAny( Collection<?> c ) { g.containsAny( c ) }
	boolean edged( V from, V to ) { g.edged( from, to ) }
	boolean edgedAll( V from, Collection<V> to ) { g.edgedAll( from, to ) }
	boolean edgedAny( V from, Collection<V> to ) { g.edgedAny( from, to ) }

	//================================================================================
	// Search and iteration operations.
	//================================================================================

	V[] toArray() { g.toArray() }
	public <T> T[] toArray( T[] a ) { g.toArray( a ) }
	Iterator<V> iterator() { g.iterator() }
	Iterable<V> depthFirst() { g.depthFirst() }
	Iterable<V> breadthFirst() { g.breadthFirst() }
	Iterable<V> depthFirst( V vertex ) { g.depthFirst( vertex ) }
	Iterable<V> breadthFirst( V vertex ) { g.breadthFirst( vertex ) }
	Set<V> outs( V vertex ) { g.outs( vertex ).asImmutable() }
	Set<V> ins( V vertex ) { g.ins( vertex ) }

	//================================================================================
	// Degree operations.
	//================================================================================

	int outDegree( V vertex ) { g.outDegree( vertex ) }
	Map<V, Integer> outDegree() { g.outDegree() }
	int inDegree( V vertex ) { g.inDegree( vertex ) }
	Map<V, Integer> inDegree() { g.inDegree() }
}