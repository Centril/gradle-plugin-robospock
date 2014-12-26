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
 * View for a DAG from the perspective of a vertex = "from".
 * The DAG's relevant methods are curried with "from".
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-22
 */
class GraphView<V> implements Iterable<V> {
	private DirectedAcyclicGraph<V> graph
	private V from

	/**
	 * Constructs the view.
	 *
	 * @param  g      the graph.
	 * @param  vertex the perspective.
	 * @return        the view.
	 */
	public GraphView( DirectedAcyclicGraph<V> g, V vertex ) {
		graph = g
		from = vertex
	}

	//================================================================================
	// Adding operations.
	//================================================================================

	/**
	 * Unsupported.
	 */
	public boolean add( V vertex ) {
		unsupported()
	}

	/**
	 * Unsupported.
	 */
	public boolean addAll( Collection<? extends V> c ) {
		unsupported()
	}

	/**
	 * Unsupported.
	 */
	public boolean add( V from, V to ) {
		unsupported()
	}

	/**
	 * Unsupported.
	 */
	public boolean add( V from, Collection<V> to ) {
		unsupported()
	}

	//================================================================================
	// Removing operations.
	//================================================================================

	/**
	 * Unsupported.
	 */
	public boolean remove( Object o ) {
		unsupported()
	}

	/**
	 * Unsupported.
	 */
	public boolean removeAll( Collection<?> c ) {
		unsupported()
	}

	/**
	 * Unsupported.
	 */
	public boolean retainAll( Collection<?> c ) {
		unsupported()
	}

	/**
	 * Unsupported.
	 */
	public void clear() {
		unsupported()
	}

	private boolean unsupported() {
		throw new UnsupportedOperationException()
		return false
	}

	//================================================================================
	// Contains & edging operations.
	//================================================================================

	/**
	 * Returns the number of out-going edges.
	 *
	 * @return the number of out-going edges.
	 */
	public int size() {
		edges()
	}

	/**
	 * Returns the number of out-going edges.
	 *
	 * @return the number of out-going edges.
	 */
	public int edges() {
		outs().size()
	}

	/**
	 * Returns true if there are no out-going edges.
	 *
	 * @return Returns true if there are no out-going edges.
	 */
	public boolean isEmpty() {
		outs().isEmpty()
	}

	/**
	 * Returns true if there's an edge (from, to),
	 * in other words: from -> to.
	 *
	 * The order matters, this is not the same as
	 * (to, from), in other words: to -> from.
	 * This is the case since the graph is directional.
	 *
	 * @param  to   the to part of the edge.
	 * @return      true if there's an edge (from, to).
	 */
	public boolean edged( V to ) {
		dag.edged( from, to )
	}

	/**
	 * Returns true if the graph contains vertex.
	 *
	 * @param  vertex the vertex to check for.
	 * @return        true if it is contained in the graph.
	 */
	public boolean contains( Object vertex ) {
		edges( vertex )
	}

	/**
	 * Returns true if there's an edge to all of to's.
	 *
	 * @param  to   the collection of vertex/node.
	 * @return      if it was edged to all of the to's.
	 */
	public boolean containsAll( Collection<?> to ) {
		dag.edgedAll( from, to )
	}

	/**
	 * Returns true if there's an edge to any of to's.
	 *
	 * @param  to   the collection of vertex/node.
	 * @return      if it was edged to any of the to's.
	 */
	public boolean containsAny( Collection<?> to ) {
		dag.edgedAny( from, to )
	}

	//================================================================================
	// Search and iteration operations.
	//================================================================================

	/**
	 * Returns an array containing all of the outgoing edges.
	 *
	 * @return an array containing all of the outgoing edges.
	 */
	public V[] toArray() {
		outs().toArray() as V[]
	}

	/**
	 * Returns an array containing all of the outgoing edges as T[].
	 *
	 * @param  a an array of type T[].
	 * @return   an array containing all of the outgoing edges as T[].
	 */
	public <T> T[] toArray( T[] a ) {
		outs().toArray( a )
	}

	/**
	 * Returns an iterable starting from vertex/node
	 * using the DFS algorithm.
	 *
	 * @return the iterable.
	 */
	public Iterator<V> iterator() {
		dag.breadthFirst( from )
	}

	/**
	 * Returns an iterable starting from vertex/node
	 * using the DFS algorithm.
	 *
	 * @return the iterable.
	 */
	public Iterable<V> depthFirst( V vertex ) {
		dag.depthFirst( from )
	}

	/**
	 * Returns an iterable starting from vertex/node
	 * using the BFS algorithm.
	 *
	 * @return the iterable.
	 */
	public Iterable<V> breadthFirst() {
		dag.breadthFirst( from )
	}

	/**
	 * Returns all the vertrices that
	 * the vertex/node links to.
	 *
	 * @return the outgoing vertrices.
	 */
	public Set<V> outs() {
		dag.outs( from )
	}

	/**
	 * Returns all the vertrices that
	 * links to the the vertex/node.
	 *
	 * @return the ingoing vertrices.
	 */
	public Set<V> ins() {
		dag.ins( from )
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
	 * @return  out-degree for vertex.
	 */
	public int outDegree() {
		dag.outDegree( from )
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
	 * @return in-degree for vertex.
	 */
	public int inDegree() {
		dag.inDegree( from )
	}
}