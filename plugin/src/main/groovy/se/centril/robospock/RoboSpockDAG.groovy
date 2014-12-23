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

/**
 * {@link RoboSpockDAG}: is a simple implementation
 * of a DAG, a Directed Acyclic Graph.
 *
 * The DAG does not check for cycles, so the user
 * must take this into consideration.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-22
 */
class RoboSpockDAG<V> {
	Map<V, Set<V>> edges = [:]

	/**
	 * View for the DAG.
	 * Curries all relevant methods of DAG with from.
	 */
	class View {
		V from

		private View( V vertex ) {
			from = vertex
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
		def boolean edged( V to ) {
			dag.edged( from, to )
		}

		/**
		 * Returns the out-degree for vertex.
		 * The out-degree is the amount of vertrices
		 * that vertex links to.
		 *
		 * This assumes that from /= to, no
		 * check is done done for this however.
		 *
		 * @param  vertex the vertex to return out-degree for.
		 * @return        out-degree for vertex.
		 */
		public int outDegree() {
			dag.outDegree( from )
		}

		/**
		 * Returns the in-degree for vertex.
		 * The in-degree is the amount of vertrices
		 * that links to this vertex, in other words.
		 *
		 * This assumes that from /= to, no
		 * check is done done for this however.
		 *
		 * @param  vertex the vertex to return in-degree for.
		 * @return        in-degree for vertex.
		 */
		public int inDegree() {
			dag.inDegree( from )
		}
	}

	/**
	 * Returns a view of the graph from the
	 * perspective of the given vertex.
	 *
	 * @param  vertex the perspective.
	 * @return        the view.
	 */
	public View view( V vertex ) {
		this.new View( vertex )
	}

	/**
	 * Returns true if the DAG contains vertex.
	 *
	 * @param  vertex the vertex to check for.
	 * @return        true if it is contained in the DAG.
	 */
	public boolean contains( V vertex ) {
		edges.containsKey( vertex )
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
		def e = edges[from]
		return e != null && e.contains( to )
	}

	/**
	 * Adds vertex to DAG if not already present.
	 *
	 * @param  vertex the vertex to add to graph.
	 * @return        the DAG itself.
	 */
	public RoboSpockDAG<V> add( V vertex ) {
		addImpl( vertex )
		return this
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
	 * @return      the DAG itself.
	 */
	public RoboSpockDAG<V> add( V from, V to ) {
		addImpl( to )
		addImpl( from ).add( to )
	}

	/**
	 * Adds a directional edge
	 * each e in to: (from, e).
	 *
	 * @param  from the from part of the edge.
	 * @param  to   the collection from which to
	 *              take the to part of the edges.
	 * @return      the DAG itself.
	 */
	public RoboSpockDAG<V> add( V from, Collection<V> to ) {
		addImpl( to )
		def ft = addImpl( from )
		ft += to
	}

	private RoboSpockDAG<V> addImpl( V vertex ) {
		def e = edges[vertex]
		if ( e == null ) {
			e = edges[vertex] = []
		}
		return e
	}

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
		def e = edges[vertex]
		return e == null ? -1 : e.size()
	}

	/**
	 * Returns the out-degree for all vertrices in the graph.
	 */
	public Map<V, Integer> outDegree () {
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
		edges.containsKey( vertex ) ? -1 : edges.inject( 0 ) { k, s ->
			k != vertex && vertex in s ? d++ : d
		}
	}

	/**
	 * Returns the in-degree for all vertrices in the graph.
	 */
	public Map<V, Integer> inDegree() {
		def k = edges.keySet(),
			r = k.collectEntries { [(it): 0] }
		k.each { f -> edges[f].each { t -> r[t]++ } }
		return r
	}
}