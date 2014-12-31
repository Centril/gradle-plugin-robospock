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

package se.centril.robospock.internal

import com.android.SdkConstants

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import org.gradle.testfixtures.ProjectBuilder

import static se.centril.robospock.internal.RoboSpockConstants.*
import static se.centril.robospock.internal.VariantImpl.*

import se.centril.robospock.RoboSpockConfiguration
import se.centril.robospock.RoboSpockTest
import se.centril.robospock.RoboSpockVariant
import se.centril.robospock.RoboSpockVersion

import se.centril.robospock.fixtures.DTask
import se.centril.robospock.fixtures.BuildType
import se.centril.robospock.fixtures.Variant
import se.centril.robospock.RoboSpockSpecification

import spock.lang.Shared
import javax.inject.Inject;

/**
 * Tests {@link VariantImpl}
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-12-24
 */
class VariantImplSpecification extends RoboSpockSpecification {
	def "VariantImpl( Project p, RoboSpockConfiguration cfg, v )"() {
		given:
			def a = androidProject()
			def p = testProject()
			def c = new RoboSpockConfiguration( p, new RoboSpockVersion() )
			c.android = a
			def v = variant( 'debug', 'debug' )
			def nc = v.name.capitalize()
		when:
			def var = new VariantImpl( p, c, v )
			def t = var.task
			def ss = p.sourceSets[SOURCESET_NAME_PREFIX + nc]
		then:
			t == p.tasks.findByName( TASK_NAME_BASE + nc )
			t.name == TASK_NAME_BASE + nc
			t.description == TASK_DESCRIPTION_UNIT + v.name + '.'
			t.class.name == RoboSpockTest.name + '_Decorated'
			t.var == var
			t.config == c
			var.sourceSet == ss
			var.testCompile == p.configurations[ss.compileConfigurationName]
			var.testRuntime == p.configurations[ss.runtimeConfigurationName]
	}

	def "VariantImpl( Project p, String name, String taskDesc )"() {
		given:
			def p = testProject()
		when:
			def v = new VariantImpl( p, name, desc )
		then:
			v.sourceSet == p.sourceSets[SOURCESET_NAME_PREFIX + name]
			v.task == p.tasks.findByName( TASK_NAME_BASE + name )
			v.task.name == TASK_NAME_BASE + name
			v.task.description == descResult
			v.testCompile == p.configurations[v.sourceSet.compileConfigurationName]
			v.testRuntime == p.configurations[v.sourceSet.runtimeConfigurationName]
		where:
			name | desc	  | descResult
			'A'  | null	  | TASK_DESCRIPTION_GROUP + '.'
			'B'  | 'data' | TASK_DESCRIPTION_GROUP + ' with data.'
	}

	def "VariantImpl( Project p, SourceSet ss, String name, String taskDesc )"() {
		given:
			def p = testProject()
			def ss = p.sourceSets.create( TASK_NAME_BASE + name )
		when:
			def v = new VariantImpl( p, ss, name, desc )
		then:
			v.sourceSet == ss
			v.task == p.tasks.findByName( TASK_NAME_BASE + name )
			v.task.name == TASK_NAME_BASE + name
			v.task.description == descResult
			v.testCompile == p.configurations[ss.compileConfigurationName]
			v.testRuntime == p.configurations[ss.runtimeConfigurationName]
		where:
			name | desc	  | descResult
			'A'  | null	  | TASK_DESCRIPTION_GROUP + '.'
			'B'  | 'data' | TASK_DESCRIPTION_GROUP + ' with data.'
	}

	def "extendFrom"() {
		given:
			def p = testProject()
			def c = p.configurations
			def v1 = new VariantImpl( p, 'v1', null )
			def v2 = new VariantImpl( p, 'v2', null )
		when:
			def v = v2.extendFrom( p, v1 )
		then:
			v == v2
			v1.testCompile in v2.testCompile.extendsFrom
			v2.testCompile in v2.testRuntime.extendsFrom
			v1.testRuntime in v2.testRuntime.extendsFrom
			v2.task in v1.task.taskDependencies.getDependencies( v1.task )
	}

	def "createReferring"() {
		given:
			def p = testProject()
		when:
			def t = createReferring( p, name, desc )
		then:
			t == p.tasks.findByName( TASK_NAME_BASE + name )
			t.name == TASK_NAME_BASE + name
			t.description == descResult
		where:
			name  | desc   | descResult
			'xyz' | null   | TASK_DESCRIPTION_GROUP + '.'
			'zyx' | 'data' | TASK_DESCRIPTION_GROUP + ' with data.'
	}

	@Shared def c_counter = 0
	def "createTask"() {
		given:
			def p = testProject()
		when:
			c_counter = 0
			def t = createTask( p, name, desc, type, closure )
		then:
			t == p.tasks.findByName( TASK_NAME_BASE + name )
			t.name == TASK_NAME_BASE + name
			t.description == desc + '.'
			t.group == TASK_GROUP
			t.class.name == type.name + '_Decorated'
			c_counter == c_after
		where:
			name  | desc	| type		  | c_after | closure
			'lol' | 'xyz'	| DefaultTask | 1337	| { c_counter += 1337 }
			'abc' | 'zyx'	| DTask		  | 7331	| { c_counter += 7331 }
	}

	def "normalizeName"() {
		expect:
			normalizeName( name ) == r
		where:
			r		|| name
			null	|| null
			'Xyz'	|| 'xyz'
			'AbCd'	|| [name: 'abCd']
	}

	def "createSourceSet"() {
		given:
			def p = testProject()
			def sets = p.sourceSets
			def f = sets.create( SOURCESET_NAME_PREFIX + 'First' )
			def fc = createSourceSet( p, 'First' )
			def sc = createSourceSet( p, 'Second' )
			def s = sets[SOURCESET_NAME_PREFIX + 'Second']
		expect:
			f == fc
			s == sc
	}
}
