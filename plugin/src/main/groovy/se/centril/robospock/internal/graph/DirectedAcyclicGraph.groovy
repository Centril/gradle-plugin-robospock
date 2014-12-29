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
 * {@link DirectedAcyclicGraph}: is a simple
 * implementation of a DAG, a Directed Acyclic Graph.
 *
 * The implementation does not check for cycles,
 * so the user must take this into consideration.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-22
 */
class DirectedAcyclicGraph<V> implements Collection<V> {
	Map<V, Set<V>> edges = [:]

	/**
	 * Returns a view of the graph from the
	 * perspective of the given vertex.
	 *
	 * @param  vertex the perspective.
	 * @return        the view.
	 */
	public GraphView<V> view( V vertex ) {
		new GraphView<V>( vertex )
	}

	//================================================================================
	// Adding operations.
	//================================================================================

	/**
	 * Adds vertex to graph if not already present.
	 *
	 * @param  vertex the vertex to add to graph.
	 * @return        true if vertex is added.
	 */
	public boolean add( V vertex ) {
		Set<V> e = edges[vertex]
		boolean willAdd = e == null

		if ( willAdd ) {
			e = []
			edges[vertex] = e
		}

		return willAdd
	}

	/**
	 * Adds vertrices to graph if not already present.
	 *
	 * @param  c the vertrices to add to graph.
	 * @return   true if any of the vertrices are added.
	 */
	public boolean addAll( Collection<? extends V> c ) {
		boolean added = false
		for ( V v : c ) {
			if ( add( v ) ) {
				added = true
			}
		}
		return added
	}

	/**
	 * Adds a directional edge (from, to)
	 * which is not the same as (to, from).
	 *
	 * This assumes that from /= to, no
	 * check is done done for this however.
	 *
	 * @param  from the from part of the edge.
	 * @param  to   the to part of the edge.
	 * @return      true if any of to or from are
	 *              added or if the edge is added.
	 */
	public boolean add( V from, V to ) {
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

	/**
	 * Adds a directional edge
	 * each e in to: (from, e).
	 *
	 * @param  from the from part of the edge.
	 * @param  to   the collection from which to
	 *              take the to part of the edges.
	 * @return      true if any of to or from are added.
	 */
	public boolean add( V from, Collection<V> to ) {
		// Add from & to's.
		boolean addedFrom = add( from ),
				addedTo = addAll( to ),
				linkFrom = edges[from].addAll( to )

		// Is added?
		return linkFrom || addedTo || addedFrom
	}

	/**
	 * Adds a directional edge
	 * each e in from: (e, to).
	 *
	 * @param  from the collection from which to
	 *              take the from part of the edges.
	 * @param  to   the from part of the edge.
	 * @return      true if any of to or from are added.
	 */
	public boolean add( Collection<V> from, to ) {
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

	/**
	 * Removes the given object from the graph.
	 *
	 * @param  o the object.
	 * @return   true if the object was removed.
	 */
	public boolean remove( Object o ) {
		edges.remove( o ) != null
	}

	/**
	 * Removes the all of the given objects from the graph.
	 *
	 * @param  c the collection.
	 * @return   true if any of the vertrices in the graph was removed.
	 */
	public boolean removeAll( Collection<?> c ) {
		boolean retr = false
		for ( Object o : c ) {
			if ( remove( o ) ) {
				retr = true
			}
		}
		return retr
	}

	/**
	 * Removes the all of the objects not in given collection from the graph.
	 *
	 * @param  c the collection.
	 * @return   true if any of the vertrices in the graph was removed.
	 */
	public boolean retainAll( Collection<?> c ) {
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

	/**
	 * Clears the graph removing all vertrices and edges.
	 */
	public void clear() {
		edges.clear()
	}

	/**
	 * Removes edge (from, to).
	 *
	 * @param  from the from part of edge.
	 * @param  to   the to part of edge.
	 * @return      true if the edge was removed.
	 */
	public boolean remove( V from, V to ) {
		edges[from].remove( to )
	}

	/**
	 * Removes edge (from, e)
	 * each e in from: (e, to).
	 *
	 * @param  from the from part of the edge.
	 * @param  to   the collection from which to
	 *              take the to part of the edges.
	 * @return      true if any edge was removed.
	 */
	public boolean remove( V from, Collection<V> to ) {
		def fe = edges[from]
		fe != null && fe.removeAll( to )
	}

	/**
	 * Removes edge (from, e)
	 * each e in from: (e, to).
	 *
	 * @param  from the collection from which to
	 *              take the from part of the edges.
	 * @param  to   the from part of the edge.
	 * @return      true if any edge was removed.
	 */
	public boolean remove( Collection<V> from, V to ) {
		from.inject( false ) { r, f ->
			def fe = edges[f]
			(fe != null && fe.remove( to )) || r
		}
	}

	/**
	 * Clears the graph of all edges.
	 */
	public void clearEdges() {
		edges.values().each {
			it.clear()
		}
	}

	//================================================================================
	// Contains & edging operations.
	//================================================================================

	/**
	 * Returns the number of vertrices in the graph.
	 *
	 * @return the number of vertrices in the graph.
	 */
	public int size() {
		edges.size()
	}

	/**
	 * Returns the number of edges in the graph.
	 *
	 * @return the number of edges in the graph.
	 */
	public int edges() {
		edges.inject( 0 ) { acc, o ->
			acc + o.value.size()
		}
	}

	/**
	 * Returns true if the graph contains no elements.
	 *
	 * @return Returns true if the graph contains no elements.
	 */
	public boolean isEmpty() {
		edges.isEmpty()
	}

	/**
	 * Returns true if the graph contains vertex.
	 *
	 * @param  vertex the vertex to check for.
	 * @return        true if it is contained in the graph.
	 */
	public boolean contains( Object vertex ) {
		edges.containsKey( vertex )
	}

	/**
	 * Returns true if the graph contains all vertrices.
	 *
	 * @param  c the vertrices to check for.
	 * @return   true if all vertrices are contained in the graph.
	 */
	public boolean containsAll( Collection<?> c ) {
		for ( V v : c ) {
			if ( !contains( v ) ) {
				return false
			}
		}
		return true
	}

	/**
	 * Returns true if the graph contains any of the vertrices.
	 *
	 * @param  c the vertrices to check for.
	 * @return   true if any of the vertrices are contained in the graph.
	 */
	public boolean containsAny( Collection<?> c ) {
		for ( V v : c ) {
			if ( contains( v ) ) {
				return true
			}
		}
		return false
	}

	/**
	 * Returns true if there's an edge (from, to),
	 * in other words: from -> to.
	 *
	 * The order matters, this is not the same as
	 * (to, from), in other words: to -> from.
	 * This is the case since the graph is directional.
	 *
	 * @param  from the from part of the edge.
	 * @param  to   the to part of the edge.
	 * @return      true if there's an edge (from, to).
	 */
	public boolean edged( V from, V to ) {
		Set<V> e = edges[from]
		return e != null && e.contains( to )
	}

	/**
	 * Returns true if there's an edge to all of to's.
	 * An empty to's always results in true.
	 *
	 * @param  from the from vertex/node.
	 * @param  to   the collection of vertex/node.
	 * @return      if it was edged to all of the to's.
	 */
	public boolean edgedAll( V from, Collection<V> to ) {
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

	/**
	 * Returns true if there's an edge to any of to's.
	 *
	 * @param  from the from vertex/node.
	 * @param  to   the collection of vertex/node.
	 * @return      if it was edged to any of the to's.
	 */
	public boolean edgedAny( V from, Collection<V> to ) {
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

	/**
	 * Returns an array containing all of the vertrices.
	 *
	 * @return an array containing all of the vertrices.
	 */
	public V[] toArray() {
		edges.keySet().toArray() as V[]
	}

	/**
	 * Returns an array containing all of the vertrices as T[].
	 *
	 * @param  a an array of type T[].
	 * @return   an array containing all of the vertrices as T[].
	 */
	public <T> T[] toArray( T[] a ) {
		edges.keySet().toArray( a )
	}

	/**
	 * Returns an iterator over the vertrices in the collection.
	 *
	 * @return Returns the iterator.
	 */
	public Iterator<V> iterator() {
		edges.keySet().iterator()
	}

	/**
	 * Returns an iterable starting from vertex/node
	 * using the DFS algorithm.
	 *
	 * @param  vertex the starting vertex/node.
	 * @return        the iterable.
	 */
	public Iterable<V> depthFirst( V vertex ) {
		return { new DepthFirstIterator( this, vertex ) }
	}

	/**
	 * Returns an iterable starting from vertex/node
	 * using the BFS algorithm.
	 *
	 * @param  vertex the starting vertex/node.
	 * @return        the iterable.
	 */
	public Iterable<V> breadthFirst( V vertex ) {
		return { new BreadthFirstIterator( this, vertex ) }
	}

	/**
	 * Returns all the vertrices that
	 * the vertex/node links to.
	 *
	 * @param  vertex the vertex/node.
	 * @return        the outgoing vertrices.
	 */
	public Set<V> outs( V vertex ) {
		edges[vertex]//.asImmutable()
	}

	/**
	 * Returns all the vertrices that
	 * links to the the vertex/node.
	 *
	 * @param  vertex the vertex/node.
	 * @return        the ingoing vertrices.
	 */
	public Set<V> ins( V vertex ) {
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

	/**
	 * Returns the out-degree for vertex.
	 * The out-degree is the amount of vertrices
	 * that vertex links to.
	 *
	 * If the vertex is not present in the graph
	 * -1 is returned.
	 *
	 * This assumes that from /= to, no
	 * check is done done for this however.
	 *
	 * @param  vertex the vertex to return out-degree for.
	 * @return        out-degree for vertex.
	 */
	public int outDegree( V vertex ) {
		Set<V> e = edges[vertex]
		return e == null ? -1 : e.size()
	}

	/**
	 * Returns the out-degree for all vertrices in the graph.
	 *
	 * @return the map of vertex -> out-degree.
	 */
	public Map<V, Integer> outDegree() {
		edges.keySet().collectEntries {
			[(it): outDegree( it )]
		}
	}

	/**
	 * Returns the in-degree for vertex.
	 * The in-degree is the amount of vertrices
	 * that links to this vertex, in other words.
	 *
	 * If the vertex is not present in the graph
	 * -1 is returned.
	 *
	 * This assumes that from /= to, no
	 * check is done done for this however.
	 *
	 * @param  vertex the vertex to return in-degree for.
	 * @return        in-degree for vertex.
	 */
	public int inDegree( V vertex ) {
			edges.containsKey( vertex )
		?	edges.count { k, s -> k != vertex && vertex in s }
		:	-1
	}

	/**
	 * Returns the in-degree for all vertrices in the graph.
	 *
	 * @return the map of vertex -> in-degree.
	 */
	public Map<V, Integer> inDegree() {
		Set<V> k = edges.keySet()
		Map<V, Integer>	r = k.collectEntries { [(it): 0] }
		k.each { f -> edges[f].each { t -> r[t]++ } }
		return r
	}
}