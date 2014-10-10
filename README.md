# gradle-plugin-robospock

gradle-plugin-robospock is a small gradle plugin for the
sole purpose of configuring **robospock** (**gradle** + **spock** + **roboelectric**)
in an as-easy-as-possible fashion.

Robospock will basically let you do unit/integration testing of android
specific code using the praised spock framework which uses specification
driven testing and BDD (Behavior-Driven Development) using groovy.
And all of this happens using Roboelectric which avoids the hassle and
latency involved in doing testing on an actual android device or worse,
an emulator.

## Links
<table>
<tr><td>Robospock</td><td>https://github.com/Polidea/RoboSpock</td></tr>
<tr><td>Roboelectric</td><td>http://robolectric.org</td></tr>
<tr><td>Spock-framework</td><td>https://github.com/spockframework/spock</td></tr>
<tr><td>Groovy</td><td>http://groovy.codehaus.org</td></tr>
</table>

## Usage

**Using the plugin requires you to have your android application or library
in one project, and your testing code in another project.** This is a
restriction that the android gradle plugin puts on us, and it is not
possible to circumvent it today, as doing: `apply plugin: 'groovy'`
conflicts with the android plugin.

Given an android project with the path **:app**, and a test project with the
path **:test**, we can configure the app-test project to use this plugin like so:

```groovy
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
  		classpath 'se.toxbee.robospock:gradle-plugin-robospock:0.1.0'
  	}
}

apply plugin: 'robospock'

robospock {
	testing = ':app'
}
```

If your test project is named **app-test**, i.e the same as the application
or library but with the suffix `-test` (to be exact, the regex: `/[^a-zA-Z0-9]?test/`), and if the app is the parent of the test project or is a sibling of it, then you may simply omit specifying the `robospock.testing` part alltogether as it is automatically configured.
Then it becomes:

```groovy
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
  		classpath 'se.toxbee.robospock:gradle-plugin-robospock:0.1.0'
  	}
}
apply plugin: 'robospock'
```

When you have done this, you can use the `test` task as usual,
or `robospock` task if you only want to run robospock tests.

### Advanced options

The available options are:

```groovy
robospock {
	// Sets the android project to test
	android = <gradle_project_object>
	// The same as doing: android = project( '<gradle_android_project_path>' )
	testing = '<gradle_android_project_path>'

	// One of the build-types specified in android_project_build_type,
	// default: 'debug'
	buildType = '<android_project_build_type>'

	// Version of robospock to use as dependency.
	// default: '0.5.+'
	robospockVersion = '<dependency_version>'

	// Version of spock to use as dependency.
	// default: '0.7-groovy-2.0'
	spockVersion = '<dependency_version>'

	// Version of groovy to use as dependency.
	// default: '2.3.6'
	groovyVersion = '<dependency_version>'

	// Version of cglib to use as dependency.
	// default: '3.1'
	// If the dependency is unwanted, set the string to empty.
	cglibVersion = '<dependency_version>'

	// Version of objenesis to use as dependency.
	// default: '2.1'
	// If the dependency is unwanted, set the string to empty.
	objenesisVersion = '<dependency_version>'
}
```

By default, the plugin adds the much used optional spock-dependencies
objenesis and cglib which are used for mocking, etc. You can
change the version used, or disable them by setting an empty version.

## Changelog

See [CHANGES.md](CHANGES.md)

## Bugs / Issues / Feature requests

Please use the issue system that github provides to report bugs/issues or request an enhancement.

## Contribution

This project is in its infancy, so pull requests are more than welcome.

## License

gradle-plugin-robospock is licensed under **Apache License 2.0**,
see [LICENSE.md](LICENSE.md) for more information.
