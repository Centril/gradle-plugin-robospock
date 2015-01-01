package com.example;

import spock.lang.Specification

class TransitiveDependencySpecification extends Specification {
    def "should create TransitiveDependency"() {
        when:
        def x = new TransitiveDependency()

        then:
        noExceptionThrown()
    }
}
