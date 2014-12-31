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

import com.android.SdkConstants

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.FileCollectionDependency
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.testfixtures.ProjectBuilder

import static java.beans.Introspector.decapitalize

import static se.centril.robospock.internal.RoboSpockConstants.*
import se.centril.robospock.internal.VariantImpl
import se.centril.robospock.RoboSpockConfiguration
import se.centril.robospock.RoboSpockVariant
import se.centril.robospock.RoboSpockVersion
import se.centril.robospock.graph.internal.DirectedAcyclicGraphImpl

import se.centril.robospock.fixtures.ProductFlavor

import spock.lang.Shared
import spock.lang.Specification

/**
 * Tests {@link RoboSpockConfigurator}
 *
 * @author Centril <twingoow@gmail.com> / Mazdak Farrokhzad.
 * @since 2014-10-02
 */
class RoboSpockConfiguratorSpecification extends RoboSpockSpecification {
	RoboSpockConfigurator configurator
	@Shared def dver = new RoboSpockVersion()

	void cfgr( RoboSpockConfiguration config ) {
		this.config = config
		configurator = new RoboSpockConfigurator( config )
	}

	void usual( boolean library = false ) {
		setupDefault( library )
		test = testProject()
		cfgr( config )
	}

	def "addJCenter"() {
		given:
			cfgr( new RoboSpockConfiguration( testProject(), dver ) )
		when:
			configurator.addJCenter()
		then:
			checkJCenter( config.tester.repositories )
	}

	def "addJCenterBuildScript"() {
		given:
			config = new RoboSpockConfiguration( testProject(), dver )
		when:
			RoboSpockConfigurator.addJCenterBuildScript( config )
		then:
			checkJCenter( config.tester.buildscript.repositories )
	}

	def checkJCenter( RepositoryHandler rh ) {
		assert rh.find {
			it instanceof MavenArtifactRepository &&
			new URI( 'https://jcenter.bintray.com/' )
		}
		return true
	}

	def "applyGroovy"() {
		given:
			cfgr( new RoboSpockConfiguration( root, dver ) )
		when:
			configurator.applyGroovy()
		then:
			root.plugins.hasPlugin( 'groovy' )
	}

	def "librarySubprojects"() {
		given:
			usual()
			def libA = testProject( 'libraryA' )
			def libB = testProject( 'libraryB' )
			def libC = testProject( 'libraryC' )
			def appC = androidLibraryProject( false, 'appC' )
			libB.dependencies {
				compile libA
			}
			android.dependencies {
				compile 'com.example1:artifact1:1.0.0'
				compile libB
				compile appC
				provided libC
			}
		expect:
			configurator.librarySubprojects( android, 'compile' )
						.containsAll( [libB, libA] )
	}

	def "addDependencies"() {
		when:
			test = testProject()
			cfgr( new RoboSpockConfiguration( test, ver ) )
			configurator.addDependencies()
		then:
			checkConfs( test, ver )
		where:
			ver << [
				new RoboSpockVersion().with {
					groovy		= '1.0'
					spock		= '2.0'
					robospock	= '3.0'
					cglib		= '4.0'
					objenesis	= '5.0'
					return it
				},
				new RoboSpockVersion().with {
					minimum()
					groovy		= '1.0'
					spock		= '2.0'
					robospock	= '3.0'
					return it
				}
			]
	}

	boolean checkConfs( Project test, RoboSpockVersion ver ) {
		def d = test.configurations.find { it.name == 'testCompile' }
					.allDependencies.collect { depString( it ) }
		d == ver.dependencies()
	}

	String depString( Dependency dep ) {
		"${dep.group}:${dep.name}:${dep.version}"
	}

	def "addAndroidRepositories"() {
		given:
			usual()
			def sdk = android.android.sdkDirectory.toURI().toString()
			configurator.addAndroidRepositories()
		expect:
			test.repositories
				.findAll { it instanceof MavenArtifactRepository }
				.collect { ((MavenArtifactRepository) it).url.toString() }
				.containsAll( [
					test.file( "${sdk}/extras/android/m2repository" ).toURL().toString(),
					test.file( "${sdk}/extras/google/m2repository" ).toURL().toString()
				] )
	}

	def "setupGraph() without productFlavors"() {
		given:
			usual( library )
			config.setTester( test )
			// build types:
			def bts = setupGraphBT()
			// variants:
			def a = config.android.android
			def vars = library ? a.libraryVariants : a.applicationVariants
			bts.each { vars << variant( it, it ) }
		when:
			configurator.setupGraph()
			def l = config.graph.breadthFirst().collect()
		then:
			testVariants( l, 0..1, [''] )
			testVariants( l, 1..4 )
		where:
			library << [true, false]
	}

	def "setupGraph() with productFlavors"() {
		given:
			def (bts, pfsk) = setupGraphPf()
		when:
			configurator.setupGraph()
			def l = config.graph.breadthFirst().collect()
		then:
			testVariants( l, 0..1, [''] )
			testVariants( l, 1..4, bts )
			testVariants( l, 4..8, pfsk )
			testVariants( l, 8..20, [pfsk, bts].combinations { pf, bt -> pf + bt.capitalize() } )
			testVariants( l, 20..32 )
	}

	List<Collection<String>> setupGraphPf() {
		usual()

		// build types:
		def bts = setupGraphBT()

		// product flavors:
		def arc = ['x86', 'arm']
		def ver = ['pro', 'lite']
		def pfs = (arc + ver).collectEntries { [(it): [name: it] as ProductFlavor] }
		def pfsk = pfs.keySet()

		// variants:
		def a = config.android.android
		def av = a.applicationVariants
		[arc, ver]*.collect { pfs[it] }.eachCombination { x, y ->
			def pflist = [x, y]
			def name = pflist.inject( '' ) { acc, pf -> acc + pf.name.capitalize() }
			bts.each { bt ->
				def v = variant( name + bt.capitalize(), bt )
				v.productFlavors += pflist
				a.applicationVariants << v
			}
		}

		return [bts, pfsk]
	}

	List<String> setupGraphBT() {
		configurator.applyGroovy()
		// build types:
		def bts = ['debug', 'release', 'staging']
		config.buildTypes = bts
		return bts
	}

	void testVariants( List<RoboSpockVariant> vs, IntRange r ) {
		testVariants( vs, r, { v ->
			config.variants.find { n -> n.name == v.variant.name }
		}, { '' } )
	}

	void testVariants( List<RoboSpockVariant> vs, IntRange r, Collection<String> ns ) {
		testVariants( vs, r, { null }, { v ->
			ns.find { n -> ('test' + n.capitalize()) == v.sourceSet.name }
		} )
	}

	void testVariants( List<RoboSpockVariant> vs, IntRange r, Closure gv, Closure gn ) {
		vs.subList( r.from, r.to ).each {
			testVariant( it, gv( it ), gn( it ) )
		}
	}

	void testVariant( RoboSpockVariant v, Object variant, String name ) {
		assert v instanceof RoboSpockVariant
		name = (v.variant ? variant.name : name).capitalize()
		def prefix = 'test' + name
		assert v.variant == variant
		assert v.task.name == 'robospock' + name
		assert v.sourceSet.name == prefix
		assert v.testCompile.name == prefix + 'Compile'
		assert v.testRuntime.name == prefix + 'Runtime'
	}

	def "clampTargetVersion( String content )"() {
		given:
			usual()
		when:
			def r = configurator.clampTargetVersion( content )
		then:
			r == result
		where:
			content	<< ['random1="abc", android:targetSdkVersion="30" random2="xyz"',
						'random1="abc", android:targetSdkVersion="21" random2="xyz"',
						'random1="abc", android:targetSdkVersion="20" random2="xyz"']
			result	<< ['random1="abc", android:targetSdkVersion="21" random2="xyz"',
						'random1="abc", android:targetSdkVersion="21" random2="xyz"',
						'random1="abc", android:targetSdkVersion="20" random2="xyz"']
	}

	def "clampTargetVersion( int v )"() {
		given:
			usual()
			def c = configurator.&clampTargetVersion
		expect:
			c( ANDROID_FIX_VERSION - 1 )	== ANDROID_FIX_VERSION - 1
			c( ANDROID_FIX_VERSION )	 	== ANDROID_FIX_VERSION
			c( ANDROID_FIX_VERSION + 1 )	== ANDROID_FIX_VERSION
	}

	def "copyAndroidDependencies"() {
		given:
			def (bts, pfsk) = setupGraphPf()

			// Do everything needed before:
			RoboSpockConfigurator.addJCenterBuildScript( config )
			configurator.addJCenter()
			configurator.addAndroidRepositories()
			configurator.addDependencies()
		//	configurator.fixSupportLib() <-- can't be done...
			configurator.setupGraph()

			// A bunch of fields, etc:
			def tasks = config.tester.tasks,
				android = config.android,
				buildDir = android.buildDir,
				aarPath = new File( buildDir, AAR_PATH ).toString(),
				libsPath = new File( buildDir, LIBS_PATH ).toString()

			// Mock javaCompile & sourceSet & dependency on android.
			def vars = configurator.variants()
			vars.each { var ->
				def name = var.sourceSet.name - 'test'
				android.sourceSets.create( name )
				var.variant.javaCompile = tasks.create(
					name: name + 'Compile',
					type: JavaCompile
				)
			}
		when:
			configurator.copyAndroidDependencies()
		then:
			vars.each { var ->
				def deps = var.testCompile.dependencies,
					fcd = deps.findAll { it instanceof FileCollectionDependency }
				fileTreeAssert( fcd[0], { it == libsPath.toString() }, { it == [JAR_WILDCARD] } )
				fileTreeAssert( fcd[1], { it == aarPath.toString() }, {
					it == JAR_DIR_WILDCARD.collect { it + JAR_WILDCARD }
				} )
			}
	}

	def "variants"() {
		given:
			usual()
		when:
			def vers = []
			vers << new VariantImpl( test, config, variant( 'variant1', 'buildtype' ) )
			vers << new VariantImpl( test, config, variant( 'variant2', 'buildtype' ) )
			vers << new VariantImpl( test, 's1', '' )
			vers << new VariantImpl( test, config, variant( 'variant4', 'buildtype' ) )
			vers << new VariantImpl( test, 's2', '' )
			config.graph = new DirectedAcyclicGraphImpl<RoboSpockVariant>()
			config.graph.addAll( vers )
		then:
			configurator.variants() == vers.findAll { it.variant != null }
		when:
			config.graph.clear()
			config.graph.addAll( vers.findAll { it.variant == null } )
		then:
			configurator.variants() == []
	}

	def "variantCompile( RoboSpockVariant var, File path, List<String> includePrefixes )"() {
		when:
			usual()
			def v = new VariantImpl( test, config, variant( 'variant1', 'debug' ) )
			def i = ['abc/', 'xyz/']
			configurator.variantCompile( v, new File( '/fileTreeDir/' ), i )
		then:
			v.testCompile.dependencies.each { d -> fileTreeAssert( d,
				{ dir -> dir.contains( 'fileTreeDir' ) },
				{ inc -> inc == i.collect { it + JAR_WILDCARD } }
			) }
	}

	def fileTreeAssert( Dependency d, Closure<Boolean> dirTest, Closure<Boolean> includesTest ) {
		if ( d instanceof FileCollectionDependency ) {
			def ft = d.source.asFileTree
			assert dirTest( ft.dir.toString() )
			assert includesTest( ft.includes.collect() )
		}
	}

	def "variantCompile( RoboSpockVariant var, Object dep )"() {
		given:
			usual()
			def v = new VariantImpl( test, config, variant( 'variant', 'buildtype' ) )
			def deps = v.testCompile.dependencies
			def d = dep instanceof List ? dep : [dep]
		when:
			configurator.variantCompile( v, dep )
		then:
			d == deps.findResults { it.group == group ? depString( it ) : null }
		where:
			group			| dep
			'com.example1'	| 'com.example1:artifact:1.0.0'
			'com.example2'	| ['com.example2:artifact2:1.0.0', 'com.example2:artifact3:1.0.0']
	}

	def "testCompile"() {
		when:
			usual()
			configurator.testCompile( 'com.example:artifact:1.0.0' )
		then:
			'com.example:artifact:1.0.0' == depString(
				test.configurations.testCompile
					  .dependencies.find { it.group == 'com.example' }
			)
	}
}
