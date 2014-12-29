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

/**
 * Tests {@link DirectedAcyclicGraph}.
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-12-26
 */
class DirectedAcyclicGraphSpecification extends GraphSpecification {
	def add( Collection<Integer> c ) {
		g.addAll( c )
	}

	def add0t9() {
		add( 0..9 )
	}

	def "add( V vertex )"() {
		when:
			def added1 = g.add( null )
		then:
			added1
			g.contains( null )
			g.size() == 1
			g.outs( null ).isEmpty()
		when:
			def added2 = g.add( 1 )
		then:
			added2
			g.contains( 1 )
			g.size() == 2
			g.outs( 1 ).isEmpty()
		when:
			def added3 = g.add( 1 )
		then:
			!added3
			g.contains( 1 )
			g.size() == 2
			g.outs( 1 ).isEmpty()
		when:
			def added4 = g.add( -1 )
		then:
			added4
			g.contains( -1 )
			g.size() == 3
			g.outs( -1 ).isEmpty()
	}

	def "addAll( Collection<? extends V> c )"() {
		given:
			def l1 = [1, 2]
			def l2 = [2, 3]
		when:
			def added1 = g.addAll( l1 )
		then:
			added1
			l1.each { assert g.contains( it ) }
			g.size() == 2
		when:
			def added2 = g.addAll( l1 )
		then:
			!added2
			l1.each { assert g.contains( it ) }
			g.size() == 2
		when:
			def added3 = g.addAll( l2 )
		then:
			added3
			l2.each { assert g.contains( it ) }
			g.size() == 3
		when:
			def added4 = g.addAll( [] )
		then:
			!added4
			g.size() == 3
	}

	def "add( V from, V to )"() {
		when: "add first time"
			def e1 = g.add( 0, 1 )
		then: "expect 0,1 added & 0->1"
			e1
			g.size() == 2
			g.edges() == 1
			g.containsAll( [0, 1] )
			g.edged( 0, 1 )
		when: "add again"
			def e2 = g.add( 0, 1 )
		then: "expect not added"
			!e2
			g.size() == 2
			g.edges() == 1
			g.containsAll( [0, 1] )
			g.edged( 0, 1 )
		when: "add 1 -> 2"
			def e3 = g.add( 1, 2 )
		then: "expect 1,2 added & 1->2"
			e3
			g.size() == 3
			g.edges() == 2
			g.containsAll( [0, 1, 2] )
			g.edged( 1, 2 )
		when: "add 3->2"
			def e4 = g.add( 3, 2 )
		then: "expect 3 added & 3->2"
			e4
			g.size() == 4
			g.edges() == 3
			g.containsAll( [0, 1, 2, 3] )
			g.edged( 3, 2 )
		when: "add 0->2"
			def e5 = g.add( 0, 2 )
		then: "expect 0->2"
			e5
			g.size() == 4
			g.edges() == 4
			g.edged( 0, 2 )
			g.outs( 0 ).size() == 2
	}

	def "add( V from, Collection<V> to )"() {
		when: "add first time"
			def e1 = g.add( 0, [1] )
		then: "expect 0,1 added & 0->[1]"
			e1
			g.size() == 2
			g.edges() == 1
			g.containsAll( [0, 1] )
			g.edged( 0, 1 )
		when: "add again"
			def e2 = g.add( 0, [1] )
		then: "expect not added"
			!e2
			g.size() == 2
			g.edges() == 1
			g.containsAll( [0, 1] )
			g.edged( 0, 1 )
		when: "add 1->[2]"
			def e3 = g.add( 1, [2] )
		then: "expect 1,2 added & 1->[2]"
			e3
			g.size() == 3
			g.edges() == 2
			g.containsAll( [0, 1, 2] )
			g.edged( 1, 2 )
		when: "add 3->[2]"
			def e4 = g.add( 3, [2] )
		then: "expect 3 added & 3->[2]"
			e4
			g.size() == 4
			g.edges() == 3
			g.containsAll( [0, 1, 2, 3] )
			g.edged( 3, 2 )
		when: "add 0->[2]"
			def e5 = g.add( 0, [2] )
		then: "expect 0->[2]"
			e5
			g.size() == 4
			g.edges() == 4
			g.edged( 0, 2 )
			g.outs( 0 ).size() == 2
		when: "add 0->[3,4,5]"
			def e6 = g.add( 0, [3, 4, 5] )
		then: "expect 0->[3,4,5]"
			e6
			g.size() == 6
			g.edges() == 7
			[1, 2, 4, 5].each {
				assert g.edged( 0, it )
			}
			g.outs( 0 ).size() == 5
		when: "add nothing"
			def e7 = g.add( 0, [] )
		then: "expect unchanged"
			!e7
			g.size() == 6
			g.edges() == 7
			g.outs( 0 ).size() == 5
	}

	def "add( Collection<V> from, to )"() {
		when: "add (3, -1)"
			def e0 = g.add( [3], -1 )
		then: "expect (3, -1)"
			e0
			g.size() == 2
			g.edges() == 1
			g.edged( 3, -1 )
			g.ins( -1 ).size() == 1
		when: "add no edge, but add node"
			def e1 = g.add( [], 10 )
		then: "expect node added"
			e1
			g.size() == 3
			g.edges() == 1
		when: "add no edge, node already there"
			def e2 = g.add( [], -1 )
		then: "expect unchanged"
			!e2
			g.size() == 3
			g.edges() == 1
		when: "add edge already there"
			def e3 = g.add( [3], -1 )
		then: "expect unchanged"
			!e3
			g.size() == 3
			g.edges() == 1
		when: "add [0,1]->(-2)"
			def e4 = g.add( [0, 1], -2 )
		then: "expect added"
			e4
			g.size() == 6
			g.edges() == 3
			[0, 1].each { assert g.edged( it, -2 ) }
			g.ins( -2 ).size() == 2
		when: "add [1, 2]->(-2), partial"
			def e5 = g.add( [1, 2], -2 )
		then: "expect 2 added"
			e5
			g.size() == 7
			g.edges() == 4
			[0, 1, 2].each { assert g.edged( it, -2 ) }
			g.ins( -2 ).size() == 3
	}

	def "remove( Object o )"() {
		when:
			add( 0..1 )
			def r1 = g.remove( 1 )
		then:
			r1
			g.size() == 1
			0 in g
			!(1 in g)
		when:
			def r2 = g.remove( 1 )
		then:
			!r2
			g.size() == 1
		when:
			def r3 = g.remove( 0 )
		then:
			r3
			g.size() == 0
			!(0 in g)
	}

	def "removeAll( Collection<?> c )"() {
		given: "add 0..9 to the graph"
			def l = (0..9).collect()
			g.addAll( l )
		when: "remove nothing"
			def r0 = g.removeAll( [] )
		then: "expect unchanged"
			!r0
			g.size() == l.size()
			l.each{ assert it in g }
		when: "remove [0] from the start"
			def r1 = g.removeAll( [0] )
		then: "expect [0] removed from graph"
			r1
			g.size() == l.size() - 1
			l.findAll { it > 0 }.each{ assert it in g }
			!(0 in g)
		when: "remove [5,6] from middle"
			def r2 = g.removeAll( [5, 6] )
		then: "expect [5,6] removed from graph"
			r2
			g.size() == 7
			((1..4).collect() + (7..9).collect()).each{ assert it in g }
			(5..6).collect().each { assert !(it in g) }
		when: "remove [5,6] again"
			def r3 = g.removeAll( [5, 6] )
		then: "expect unchanged"
			!r3
			g.size() == 7
			((1..4).collect() + (7..9).collect()).each{ assert it in g }
			(5..6).collect().each { assert !(it in g) }
	}

	def "retainAll( Collection<?> c )"() {
		given: "add 0..9 to the graph"
			def l = (0..9).collect()
			g.addAll( l )
		when: "retain all but 0"
			l.remove( 0 )
			def r0 = g.retainAll( l )
		then: "expect all except 0 in there"
			r0
			g.size() == 9
			l.findAll { it > 0 }.each{ assert it in g }
			!(0 in g)
		when: "retain all but [5, 6]"
			l.removeAll( [5, 6] )
			def r1 = g.retainAll( l )
		then: "expect all except [5, 6] in there"
			r1
			g.size() == 7
			((1..4).collect() + (7..9).collect()).each{ assert it in g }
			(5..6).collect().each { assert !(it in g) }
		when: "retain all but partial"
			l.remove( (Object) 7 )
			l << 6
			def r2 = g.retainAll( l )
		then: "expect all except [6, 7]"
			r2
			g.size() == 6
			((1..4).collect() + (8..9).collect()).each{ assert it in g }
			(5..7).collect().each { assert !(it in g) }
		when: "retain nothing"
			def rn = g.retainAll( [] )
		then: "expect all removed, size == 0"
			rn
			g.size() == 0
	}

	def "clear"() {
		when:
			g << 0
			g.clear()
		then:
			g.isEmpty()
		when:
			add0t9()
			g.clear()
		then:
			g.isEmpty()
		when:
			g.clear()
		then:
			g.isEmpty()
	}

	def "remove( V from, V to )"() {
		given:
			g.add( 0, 1 )
		when:
			def r1 = g.remove( 0, 1 )
		then:
			r1
			g.size() == 2
			g.edges() == 0
			!g.edged( 0, 1 )
		when:
			def r2 = g.remove( 0, 1 )
		then:
			!r2
			g.size() == 2
			g.edges() == 0
			!g.edged( 0, 1 )
	}

	def "remove( V from, Collection<V> to )"() {
		given:
			g.add( 0, 1..9 )
		when:
			def r1 = g.remove( 0, 1..2 )
		then:
			r1
			g.size() == 10
			g.edges() == 7
			(3..9).each { assert g.edged( 0, it ) }
			(1..2).each { assert !g.edged( 0, it ) }
		when:
			def r2 = g.remove( 0, [3] )
		then:
			r2
			g.size() == 10
			g.edges() == 6
			(4..9).each { assert g.edged( 0, it ) }
			(1..3).each { assert !g.edged( 0, it ) }
		when:
			def r3 = g.remove( 0, [1] )
		then:
			!r3
			g.size() == 10
			g.edges() == 6
		when:
			def r4 = g.remove( 0, 1..2 )
		then:
			!r4
			g.size() == 10
			g.edges() == 6
		when:
			def r5 = g.remove( 0, 3..4 )
		then:
			r5
			g.size() == 10
			g.edges() == 5
			(5..9).each { assert g.edged( 0, it ) }
			(1..4).each { assert !g.edged( 0, it ) }
	}

	def "remove( Collection<V> from, V to )"() {
		given:
			g.add( 0..8, 9 )
		when:
			def r1 = g.remove( [0], 9 )
		then:
			r1
			g.size() == 10
			g.edges() == 8
			(1..8).each { assert g.edged( it, 9 ) }
			!g.edged( 0, 9 )
		when:
			def r2 = g.remove( 0..1, 9 )
		then:
			r2
			g.size() == 10
			g.edges() == 7
			(2..8).each { assert g.edged( it, 9 ) }
			(0..1).each { assert !g.edged( it, 9 ) }
		when:
			def r3 = g.remove( 2..3, 9 )
		then:
			r3
			g.size() == 10
			g.edges() == 5
			(4..8).each { assert g.edged( it, 9 ) }
			(0..3).each { assert !g.edged( it, 9 ) }
		when:
			def r4 = g.remove( 0..3, 9 )
		then:
			!r4
			g.size() == 10
			g.edges() == 5
			(4..8).each { assert g.edged( it, 9 ) }
			(0..3).each { assert !g.edged( it, 9 ) }
		when:
			def r5 = g.remove( [0], 9 )
		then:
			!r5
			g.size() == 10
			g.edges() == 5
			(4..8).each { assert g.edged( it, 9 ) }
			(0..3).each { assert !g.edged( it, 9 ) }
	}

	def "clearEdges"() {
		given:
			g.add( 0..8, 9 )
		when:
			g.clearEdges()
		then:
			g.size() == 10
			g.edges() == 0
		when:
			g.clearEdges()
		then:
			g.size() == 10
			g.edges() == 0
	}

	def "size"() {
		when:
			add0t9()
			def s1 = g.size()
		then:
			s1 == 10
			s1 == g.edges.size()
	}

	def "edges"() {
		when:
			add0t9()
		then:
			g.size() == 10
			g.edges() == 0
		when:
			g.add( 0, [1, 2] )
			g.add( 1, [2, 3, 4] )
		then:
			g.size() == 10
			g.edges() == 5
	}

	def "isEmpty"() {
		when:
			add( 0..1 )
		then:
			!g.isEmpty()
		when:
			g.clear()
		then:
			g.isEmpty()
	}

	def "contains( Object vertex )"() {
		when:
			g << 0
		then:
			0 in g
			!(1 in g)
			!(null in g)
		when:
			g << 1
		then:
			0 in g
			1 in g
	}

	def "containsAll( Collection<?> c )"() {
		when:
			g << 0
		then:
			g.containsAll( [] )
			!g.containsAll( [0, 1] )
			g.containsAll( [0] )
		when:
			g << 1
		then:
			g.containsAll( [0, 1] )
	}

	def "containsAny( Collection<?> c )"() {
		when:
			g << 0
		then:
			!g.containsAny( [] )
			!g.containsAny( [1] )
			!g.containsAny( [1, 2] )
			g.containsAny( [0] )
		when:
			g << 1
		then:
			g.containsAny( [0, 1] )
			g.containsAny( [0, 2] )
	}

	def "edged( V from, V to )"() {
		when:
			g.add( 0, 1 )
		then:
			g.edged( 0, 1 )
			!g.edged( 0, 2 )
			!g.edged( 1, 2 )
			!g.edged( 2, 3 )
	}

	def "edgedAll( V from, Collection<V> to )"() {
		when:
			add( 0..5 )
			g.add( 0, 1 )
			g.add( 0, 2 )
		then:
			g.edgedAll( 0, [] )
			g.edgedAll( 0, [2] )
			g.edgedAll( 0, [1, 2] )
			!g.edgedAll( 0, [2, 3] )
			!g.edgedAll( 0, [3, 4] )
		when:
			g.add( 1, 2 )
		then:
			g.edgedAll( 1, [2] )
	}

	def "edgedAny( V from, Collection<V> to )"() {
		when:
			add( 0..5 )
			g.add( 0, 1 )
			g.add( 0, 2 )
		then:
			!g.edgedAny( 6, [] )
			!g.edgedAny( 0, [] )
			g.edgedAny( 0, [1, 2] )
			g.edgedAny( 0, [1, 2, 3] )
			g.edgedAny( 0, [1] )
		when:
			g.add( 1, 2 )
		then:
			g.edgedAny( 1, [2] )
	}

	def "toArray"() {
		when:
			def l = (0..5).collect()
			add( l )
		then:
			g.toArray().collect() == l
		when:
			g.clear()
		then:
			g.toArray().collect() == []
	}

	def "toArray( T[] )"() {
		when:
			def t = new Integer[0]
			def r = 0..5
			def l = r.collect()
			def larr = l.toArray( t )
			add( 0..5 )
			def garr = g.toArray( t ).collect()
		then:
			garr == larr
		when:
			g.clear()
			def earr = [].toArray( t )
			def garr2 = g.toArray( t ).collect()
		then:
			garr2 == earr
	}

	def "iterator"() {
		when:
			def l = (0..5).collect()
			add( l )
		then:
			g.iterator().collect() == l
	}

	def "outs( V vertex )"() {
		given:
			def l = (0..5).collect()
			add( l )
		when:
			g.add( 0, [1] )
			def o1 = g.outs( 0 ).collect()
		then:
			o1 == [1]
		when:
			g.add( 0, [2, 3] )
			def o2 = g.outs( 0 ).collect()
		then:
			o2 == [1, 2, 3]
	}

	def "ins( V vertex )"() {
		given:
			def l = (0..5).collect()
			add( l )
		when:
			(0..2).each { g.add( it, 5 ) }
		then:
			g.ins( 5 ).collect() == [0, 1, 2]
		when:
			g.add( 3, 4 )
		then:
			g.ins( 4 ).collect() == [3]
		when:
			def ins1 = g.ins( 4 )
			def ins11 = ins1.collect()
			ins1.add( 5 )
			def ins12 = g.ins( 4 ).collect()
		then:
			ins11 == ins12
	}

	def "outDegree( V vertex )"() {
		given:
			def l = (0..5).collect()
			add( l )
		when:
			def od1 = g.outDegree( 6 )
		then:
			od1 == -1
		when:
			g.add( 0, 1 )
			def od2 = g.outDegree( 0 )
		then:
			od2 == 1
		when:
			g.add( 1, [2, 3] )
			def od3 = g.outDegree( 1 )
		then:
			od3 == 2
	}

	def "outDegree"() {
		given:
			def l = (0..5).collect()
			add( l )
		when:
			def od1 = g.outDegree()
		then:
			od1.keySet().size() == 6
			od1.values().each { assert it == 0 }
		when:
			g.add( 0, [1] )
			g.add( 1, [2, 3] )
			g.add( 2, [4] )
			def od2 = g.outDegree()
		then:
			od2.keySet().size() == 6
			od2[1] == 2
			[0, 2].each { assert od2[it] == 1 }
			[3, 4, 5].each { assert od2[it] == 0 }
	}

	def "inDegree( V vertex )"() {
		given:
			def l = (0..5).collect()
			add( l )
		when:
			g.add( 0, 1 )
		then:
			g.inDegree( 6 ) == -1
			g.inDegree( 0 ) == 0
			g.inDegree( 1 ) == 1
		when:
			[1, 2, 3, 4].each { g.add( it, 5 ) }
		then:
			g.inDegree( 5 ) == 4
	}

	def "inDegree"() {
		given:
			def l = (0..5).collect()
			add( l )
		when:
			def id1 = g.inDegree()
		then:
			id1.keySet().size() == 6
			id1.values().each { assert it == 0 }
		when:
			g.add( 0, 1 )
			[1, 2, 3].each { g.add( it, 5 ) }
			[2, 3].each { g.add( it, 4 ) }
			def id2 = g.inDegree()
		then:
			id2.keySet().size() == 6
			id2[1] == 1
			[0, 2, 3].each { assert id2[it] == 0 }
			id2[4] == 2
			id2[5] == 3
	}
}
