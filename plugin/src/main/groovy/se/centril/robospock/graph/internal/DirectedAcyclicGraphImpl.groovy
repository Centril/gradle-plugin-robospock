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
 * {@link DirectedAcyclicGraph}: is a simple
 * implementation of a DAG, a Directed Acyclic Graph.
 *
 * The implementation does not check for cycles,
 * so the user must take this into consideration.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-22
 */
class DirectedAcyclicGraphImpl<V> implements DirectedAcyclicGraph {
	Map<V, Set<V>> edges = [:]

	//================================================================================
	// Adding operations.
	//================================================================================

	boolean add( V vertex ) {
		Set<V> e = edges[vertex]
		boolean willAdd = e == null

		if ( willAdd ) {
			e = []
			edges[vertex] = e
		}

		return willAdd
	}

	boolean addAll( Collection<? extends V> c ) {
		boolean added = false
		for ( V v : c ) {
			if ( add( v ) ) {
				added = true
			}
		}
		return added
	}

	boolean add( V from, V to ) {
		// Add to.
		boolean addedTo = add( to )

		// Add from.
		Set<V> outFrom = edges[from]
		boolean willAddFrom = outFrom == null
		if ( willAddFrom ) {
			outFrom = []
			edges[from] = outFrom
		}

		// Is added?
		return outFrom.add( to ) || addedTo || willAddFrom
	}

	boolean add( V from, Collection<V> to ) {
		// Add from & to's.
		boolean addedFrom = add( from ),
				addedTo = addAll( to ),
				linkFrom = edges[from].addAll( to )

		// Is added?
		return linkFrom || addedTo || addedFrom
	}

	boolean add( Collection<V> from, to ) {
		// Add all from's & to.
		boolean addedFrom = addAll( from ),
				addedTo = add( to ),
				linkFroms = from.inject( false ) { r, f -> edges[f].add( to ) || r }

		// Is added?
		return	linkFroms || addedTo || addedFrom
	}

	//================================================================================
	// Removing operations.
	//================================================================================

	boolean remove( Object o ) {
		edges.remove( o ) != null
	}

	boolean removeAll( Collection<?> c ) {
		boolean retr = false
		for ( Object o : c ) {
			if ( remove( o ) ) {
				retr = true
			}
		}
		return retr
	}

	boolean retainAll( Collection<?> c ) {
		boolean retr = false
		for ( Iterator<V> iter = edges.keySet().iterator(); iter.hasNext(); ) {
			V v = iter.next()
			if ( !(v in c) ) {
				iter.remove()
				retr = true
			}
		}
		return retr
	}

	void clear() {
		edges.clear()
	}

	boolean remove( V from, V to ) {
		edges[from].remove( to )
	}

	boolean remove( V from, Collection<V> to ) {
		def fe = edges[from]
		fe != null && fe.removeAll( to )
	}

	boolean remove( Collection<V> from, V to ) {
		from.inject( false ) { r, f ->
			def fe = edges[f]
			(fe != null && fe.remove( to )) || r
		}
	}

	void clearEdges() {
		edges.values().each {
			it.clear()
		}
	}

	//================================================================================
	// Contains & edging operations.
	//================================================================================

	int size() {
		edges.size()
	}

	int edges() {
		edges.inject( 0 ) { acc, o ->
			acc + o.value.size()
		}
	}

	boolean isEmpty() {
		edges.isEmpty()
	}

	boolean contains( Object vertex ) {
		edges.containsKey( vertex )
	}

	boolean containsAll( Collection<?> c ) {
		for ( V v : c ) {
			if ( !contains( v ) ) {
				return false
			}
		}
		return true
	}

	boolean containsAny( Collection<?> c ) {
		for ( V v : c ) {
			if ( contains( v ) ) {
				return true
			}
		}
		return false
	}

	boolean edged( V from, V to ) {
		Set<V> e = edges[from]
		return e != null && e.contains( to )
	}

	boolean edgedAll( V from, Collection<V> to ) {
		Set<V> e = edges[from]
		if ( e != null ) {
			for ( V v : to ) {
				if ( !(v in e) ) {
					return false
				}
			}
		}
		return true
	}

	boolean edgedAny( V from, Collection<V> to ) {
		Set<V> e = edges[from]
		if ( e == null ) {
			return false
		}
		for ( V v : to ) {
			if ( v in e ) {
				return true
			}
		}
		return false
	}

	//================================================================================
	// Search and iteration operations.
	//================================================================================

	V[] toArray() {
		edges.keySet().toArray() as V[]
	}

	public <T> T[] toArray( T[] a ) {
		edges.keySet().toArray( a )
	}

	Iterator<V> iterator() {
		edges.keySet().iterator()
	}

	Iterable<V> depthFirst() {
		depthFirst( first() )
	}

	Iterable<V> breadthFirst() {
		breadthFirst( first() )
	}

	private first() {
		Iterator<V> i = iterator()
		if ( !i.hasNext() ) {
			throw new IllegalStateException( 'Trying to fetch first added vertex but there\'s none.' )
		}

		return i.next()
	}

	Iterable<V> depthFirst( V vertex ) {
		return { new DepthFirstIterator( this, vertex ) }
	}

	Iterable<V> breadthFirst( V vertex ) {
		return { new BreadthFirstIterator( this, vertex ) }
	}

	Set<V> outs( V vertex ) {
		edges[vertex]
	}

	Set<V> ins( V vertex ) {
		Set<V> set = [] as Set<V>
		edges.each { v, o ->
			if ( v != vertex && vertex in o ) {
				set << v
			}
		}
		return set
	}

	//================================================================================
	// Degree operations.
	//================================================================================

	int outDegree( V vertex ) {
		Set<V> e = edges[vertex]
		return e == null ? -1 : e.size()
	}

	Map<V, Integer> outDegree() {
		edges.keySet().collectEntries {
			[(it): outDegree( it )]
		}
	}

	int inDegree( V vertex ) {
			edges.containsKey( vertex )
		?	edges.count { k, s -> k != vertex && vertex in s }
		:	-1
	}

	Map<V, Integer> inDegree() {
		Set<V> k = edges.keySet()
		Map<V, Integer>	r = k.collectEntries { [(it): 0] }
		k.each { f -> edges[f].each { t -> r[t]++ } }
		return r
	}
}