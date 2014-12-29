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

import org.gradle.api.artifacts.Configuration
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import se.centril.robospock.RoboSpockConfiguration
import se.centril.robospock.RoboSpockTest
import se.centril.robospock.RoboSpockVariant

import static se.centril.robospock.internal.RoboSpockConstants.*

/**
 * {@link VariantImpl} is the implementation
 * for {@link RoboSpockVariant}.
 *
 * @since 2014-12-22
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 */
public final class VariantImpl implements RoboSpockVariant {
	Configuration testCompile
	Configuration testRuntime
	SourceSet sourceSet
	Task task
	Object variant

	/**
	 * {@link VariantImpl} constructor for test task with variant.
	 *
	 * @param  p    the project.
	 * @param  cfg  the configuration.
	 * @param  v    the variant.
	 */
	public VariantImpl( Project p, RoboSpockConfiguration cfg, v ) {
		String name = normalizeName( v.name )
		variant = v
		sourceSet = createSourceSet( p, name )
		bindConfigurations( p )
		task = createTask( p, name, TASK_DESCRIPTION_UNIT + v.name, RoboSpockTest ) {
			config = cfg
			var = this
			configure()
		}
	}

	/**
	 * {@link VariantImpl} constructor for grouped task.
	 *
	 * @param  p         the project.
	 * @param  ss		 the source set.
	 * @param  taskName	 the task name.
	 * @param  taskDesc  the variant
	 */
	public VariantImpl( Project p, name, String taskDesc ) {
		name = normalizeName( name )
		sourceSet = createSourceSet( p, name )
		bindConfigurations( p )
		task = createReferring( p, name, taskDesc )
	}

	/**
	 * {@link VariantImpl} constructor for
	 * grouped task, but don't create source set.
	 *
	 * @param  p         the project.
	 * @param  ss		 the source set.
	 * @param  name		 the task name excluding "test" prefix.
	 * @param  taskDesc  the end of the description of the task.
	 */
	public VariantImpl( Project p, SourceSet ss, String name, String taskDesc ) {
		sourceSet = ss
		bindConfigurations( p )
		task = createReferring( p, name, taskDesc )
	}

	/**
	 * Make this extend from.
	 *
	 * @param  p    the project.
	 * @param  from the TV to extend from.
	 * @return      this.
	 */
	public VariantImpl extendFrom( Project p, RoboSpockVariant from ) {
		// Extend configurations.
		testCompile.extendsFrom( from.testCompile )
		testRuntime.extendsFrom( testCompile, from.testRuntime )

		// Task dependency.
		from.task.dependsOn this.task

		return this
	}

	/**
	 * Binds the configurations "testCompile",
	 * "testRuntime" from the source set.
	 *
	 * @param  p    the project.
	 */
	private void bindConfigurations( Project p ) {
		def confs = p.configurations
		testCompile = confs[sourceSet.compileConfigurationName]
		testRuntime = confs[sourceSet.runtimeConfigurationName]
	}

	/**
	 * Creates a referring task.
	 *
	 * @param  p         the project.
	 * @param  name      the name of the source set.
	 * @param  taskDesc  the end of the description of the task.
	 * @return           the task.
	 */
	private static Task createReferring( Project p, String name, String taskDesc ) {
		createTask( p, name, TASK_DESCRIPTION_GROUP + (taskDesc == null ? '' : ' with ' + taskDesc), DefaultTask, null )
	}

	/**
	 * Creates a task.
	 *
	 * @param  p        the project.
	 * @param  name     the name of the task.
	 * @param  desc     the description of the task.
	 * @param  type     the type of the task.
	 * @param  closure  the closure if any.
	 * @return          the task.
	 */
	private static Task createTask( Project p, String name, String taskDesc,
		Class<? extends Task> type, Closure closure ) {
		def options = [name: TASK_NAME_BASE + name, description: taskDesc + '.', group: TASK_GROUP, type: type]
		closure ? p.tasks.create( options, closure ) : p.tasks.create( options )
	}

	/**
	 * Normalize name.
	 *
	 * @param  name the name.
	 * @return      normalized inferred name.
	 */
	private static String normalizeName( name ) {
		if ( name ) {
			name = name instanceof String ? name : name.name
			name = name.capitalize()
		}
	}

	/**
	 * Creates a source set if not already present and returns it.
	 *
	 * @param  p    the project to add source set for.
	 * @param  name the name of the source set.
	 * @return      the source set.
	 */
	private static SourceSet createSourceSet( Project p, String name ) {
		p.sourceSets.maybeCreate( SOURCESET_NAME_PREFIX + name )
	}
}