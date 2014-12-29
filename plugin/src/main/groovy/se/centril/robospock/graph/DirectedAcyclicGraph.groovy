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

package se.centril.robospock.graph

/**
 * {@link DirectedAcyclicGraph}: is a Directed Acyclic Graph.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-27
 */
interface DirectedAcyclicGraph<V> extends Collection<V> {
	//================================================================================
	// Adding operations.
	//================================================================================

	/**
	 * Adds vertex to graph if not already present.
	 *
	 * @param  vertex the vertex to add to graph.
	 * @return        true if vertex is added.
	 */
	boolean add( V vertex )

	/**
	 * Adds vertrices to graph if not already present.
	 *
	 * @param  c the vertrices to add to graph.
	 * @return   true if any of the vertrices are added.
	 */
	boolean addAll( Collection<? extends V> c )

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
	boolean add( V from, V to )

	/**
	 * Adds a directional edge
	 * each e in to: (from, e).
	 *
	 * @param  from the from part of the edge.
	 * @param  to   the collection from which to
	 *              take the to part of the edges.
	 * @return      true if any of to or from are added.
	 */
	boolean add( V from, Collection<V> to )

	/**
	 * Adds a directional edge
	 * each e in from: (e, to).
	 *
	 * @param  from the collection from which to
	 *              take the from part of the edges.
	 * @param  to   the from part of the edge.
	 * @return      true if any of to or from are added.
	 */
	boolean add( Collection<V> from, to )

	//================================================================================
	// Removing operations.
	//================================================================================

	/**
	 * Removes the given object from the graph.
	 *
	 * @param  o the object.
	 * @return   true if the object was removed.
	 */
	boolean remove( Object o )

	/**
	 * Removes the all of the given objects from the graph.
	 *
	 * @param  c the collection.
	 * @return   true if any of the vertrices in the graph was removed.
	 */
	boolean removeAll( Collection<?> c )

	/**
	 * Removes the all of the objects not in given collection from the graph.
	 *
	 * @param  c the collection.
	 * @return   true if any of the vertrices in the graph was removed.
	 */
	boolean retainAll( Collection<?> c )

	/**
	 * Clears the graph removing all vertrices and edges.
	 */
	void clear()

	/**
	 * Removes edge (from, to).
	 *
	 * @param  from the from part of edge.
	 * @param  to   the to part of edge.
	 * @return      true if the edge was removed.
	 */
	boolean remove( V from, V to )

	/**
	 * Removes edge (from, e)
	 * each e in from: (e, to).
	 *
	 * @param  from the from part of the edge.
	 * @param  to   the collection from which to
	 *              take the to part of the edges.
	 * @return      true if any edge was removed.
	 */
	boolean remove( V from, Collection<V> to )

	/**
	 * Removes edge (from, e)
	 * each e in from: (e, to).
	 *
	 * @param  from the collection from which to
	 *              take the from part of the edges.
	 * @param  to   the from part of the edge.
	 * @return      true if any edge was removed.
	 */
	boolean remove( Collection<V> from, V to )

	/**
	 * Clears the graph of all edges.
	 */
	void clearEdges()

	//================================================================================
	// Contains & edging operations.
	//================================================================================

	/**
	 * Returns the number of vertrices in the graph.
	 *
	 * @return the number of vertrices in the graph.
	 */
	int size()

	/**
	 * Returns the number of edges in the graph.
	 *
	 * @return the number of edges in the graph.
	 */
	int edges()

	/**
	 * Returns true if the graph contains no elements.
	 *
	 * @return Returns true if the graph contains no elements.
	 */
	boolean isEmpty()

	/**
	 * Returns true if the graph contains vertex.
	 *
	 * @param  vertex the vertex to check for.
	 * @return        true if it is contained in the graph.
	 */
	boolean contains( Object vertex )

	/**
	 * Returns true if the graph contains all vertrices.
	 *
	 * @param  c the vertrices to check for.
	 * @return   true if all vertrices are contained in the graph.
	 */
	boolean containsAll( Collection<?> c )

	/**
	 * Returns true if the graph contains any of the vertrices.
	 *
	 * @param  c the vertrices to check for.
	 * @return   true if any of the vertrices are contained in the graph.
	 */
	boolean containsAny( Collection<?> c )

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
	boolean edged( V from, V to )

	/**
	 * Returns true if there's an edge to all of to's.
	 * An empty to's always results in true.
	 *
	 * @param  from the from vertex/node.
	 * @param  to   the collection of vertex/node.
	 * @return      if it was edged to all of the to's.
	 */
	boolean edgedAll( V from, Collection<V> to )

	/**
	 * Returns true if there's an edge to any of to's.
	 *
	 * @param  from the from vertex/node.
	 * @param  to   the collection of vertex/node.
	 * @return      if it was edged to any of the to's.
	 */
	boolean edgedAny( V from, Collection<V> to )

	//================================================================================
	// Search and iteration operations.
	//================================================================================

	/**
	 * Returns an array containing all of the vertrices.
	 *
	 * @return an array containing all of the vertrices.
	 */
	V[] toArray()

	/**
	 * Returns an array containing all of the vertrices as T[].
	 *
	 * @param  a an array of type T[].
	 * @return   an array containing all of the vertrices as T[].
	 */
	public <T> T[] toArray( T[] a )

	/**
	 * Returns an iterator over the vertrices in the collection.
	 *
	 * @return Returns the iterator.
	 */
	Iterator<V> iterator()

	/**
	 * Returns an iterable starting from first node
	 * using the DFS algorithm.
	 *
	 * @return the iterable.
	 */
	Iterable<V> depthFirst()

	/**
	 * Returns an iterable starting from first node
	 * using the BFS algorithm.
	 *
	 * @return the iterable.
	 */
	Iterable<V> breadthFirst()

	/**
	 * Returns an iterable starting from vertex/node
	 * using the DFS algorithm.
	 *
	 * @param  vertex the starting vertex/node.
	 * @return        the iterable.
	 */
	Iterable<V> depthFirst( V vertex )

	/**
	 * Returns an iterable starting from vertex/node
	 * using the BFS algorithm.
	 *
	 * @param  vertex the starting vertex/node.
	 * @return        the iterable.
	 */
	Iterable<V> breadthFirst( V vertex )

	/**
	 * Returns all the vertrices that
	 * the vertex/node links to.
	 *
	 * @param  vertex the vertex/node.
	 * @return        the outgoing vertrices.
	 */
	Set<V> outs( V vertex )

	/**
	 * Returns all the vertrices that
	 * links to the the vertex/node.
	 *
	 * @param  vertex the vertex/node.
	 * @return        the ingoing vertrices.
	 */
	Set<V> ins( V vertex )

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
	int outDegree( V vertex )

	/**
	 * Returns the out-degree for all vertrices in the graph.
	 *
	 * @return the map of vertex -> out-degree.
	 */
	Map<V, Integer> outDegree()

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
	int inDegree( V vertex )

	/**
	 * Returns the in-degree for all vertrices in the graph.
	 *
	 * @return the map of vertex -> in-degree.
	 */
	Map<V, Integer> inDegree()
}