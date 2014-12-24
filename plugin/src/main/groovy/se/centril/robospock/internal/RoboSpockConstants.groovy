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

import org.gradle.api.plugins.JavaBasePlugin

/**
 * {@link RoboSpockConstants}: holds
 * all the constants for the plugin.
 *
 * @author Mazdak Farrokhzad <twingoow@gmail.com>
 * @since 2014-12-24
 */
class RoboSpockConstants {
	private static final String ROBOSPOCK_EXTENSION				= 'robospock'

	private static final String TASK_NAME_BASE					= 'robospock'
	private static final String TASK_GROUP						= 'robospock'
	private static final String TASK_DESCRIPTION_GROUP			= 'Runs all the unit tests using RoboSpock, for all variants'
	private static final String TASK_DESCRIPTION_UNIT			= 'Runs the unit test using RoboSpock, for variant: '
	private static final String TASK_DESCRIPTION_BUILD_TYPE		= 'build type'
	private static final String TASK_DESCRIPTION_PRODUCT_FLAVOR	= 'product flavor '
	private static final String TASK_DESCRIPTION_PF_BT			= '(product flavor, build type) '

	private static final String		  SOURCESET_NAME_PREFIX		= 'test'
	private static final List<String> SOURCESET_LANG			= ['java', 'groovy', 'resources']
	private static final List<String> SOURCESET_LANG_REQPLUGIN	= ['scala', 'kotlin']

	private static final String JAR_TASK_BASE					= 'robospockZip2Jar'
	private static final String JAR_TASK_GROUP					= 'robospockZip2Jar'
	private static final String JAR_TASK_DESCRIPTION			= "Zips for Robospock."

	private static final String SYS_ANDROID_MANIFEST			= 'android.manifest'
	private static final String SYS_ANDROID_RESOURCES			= 'android.resources'
	private static final String SYS_ANDROID_ASSETS				= 'android.assets'

	private static final String INTERMEDIATES_PATH				= 'intermediates/'
	private static final String MANIFEST_FILE					= 'AndroidManifest.xml'
	private static final String MANIFEST_FINAL_PATH				= 'manifests/full'
	private static final String MANIFEST_PATH					= INTERMEDIATES_PATH + 'manifests/full'
	private static final String AAR_PATH						= INTERMEDIATES_PATH + 'exploded-aar/'
	private static final String CLASSES_PATH					= INTERMEDIATES_PATH + 'classes/'
	private static final String RES_DIR							= 'res/'
	private static final String RES_PATH						= INTERMEDIATES_PATH + RES_DIR
	private static final String BUNDLES_PATH					= INTERMEDIATES_PATH + 'bundles/'
	private static final String LIBS_PATH						= 'libs'
	private static final String JAR_EXT							= 'jar'
	private static final String JAR_WILDCARD					= '*.' + JAR_EXT
	private static final List<String> JAR_DIR_WILDCARD			= ['*/*/*/', '*/*/*/*/']

	private static final List<String>	MAVEN_ANDROID_REPOS		= ['extras/android/m2repository', 'extras/google/m2repository']
	private static final String			MAVEN_ANDROID_SUPPORT	= 'com.android.support:support-v4:19.0.1'
	private static final String			MAVEN_GROOVY			= 'org.codehaus.groovy:groovy-all:'
	private static final String			MAVEN_SPOCK				= 'org.spockframework:spock-core:'
	private static final String			MAVEN_ROBOSPOCK			= 'org.robospock:robospock:'
	private static final String			MAVEN_CGLIB				= 'cglib:cglib-nodep:'
	private static final String			MAVEN_OBJNESIS			= 'org.objenesis:objenesis:'

	private static final String 		RE_FIXTASK_BASE			= 'robospockFixRobolectricBugs'
	private static final String 		RE_FIXTASK_GROUP		= 'robospockFixRobolectricBugs'
	private static final List<String>	ANDROID_TARGET_VERSION	= ['android:targetSdkVersion="', '"']
	private static final int			ANDROID_FIX_VERSION		= 18

	private static final String			TEST_JVM_MIN_HEAP_SIZE	= "128m"
	private static final String			TEST_JVM_MAX_HEAP_SIZE	= "1024m"
}