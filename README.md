[![badge: Download]][badge url: Download] [![badge: LICENSE]][LICENSE.md] [![badge: Semver 2.0.0]][badge url: Semver 2.0.0] [![badge: Build Status]][badge url: Build Status] [![badge: Coverage Status]][badge url: Coverage Status] [![badge: gratipay]][badge url: gratipay] 

# [gradle-plugin-robospock]

<!-- MarkdownTOC -->

- [Usage](#usage)
	- [Applying from a tester project](#applying-from-a-tester-project)
	- [Applying from an android project (Since 0.2.0)](#applying-from-an-android-project-since-020)
	- [Advanced options](#advanced-options)
- [Known Issues](#known-issues)
- [Changelog](#changelog)
- [Bugs / Issues / Feature requests / Contribution](#bugs--issues--feature-requests--contribution)
- [License](#license)
- [Authors](#authors)

<!-- /MarkdownTOC -->

**[gradle-plugin-robospock]** is a small gradle plugin for the sole purpose of setting up **[robospock]** (**[gradle]** + **[spock]** +  **[roboelectric]**) as easily as possible.

Robospock will basically let you do unit/integration testing of android specific code using the praised spock framework which uses specification driven testing and BDD (Behavior-Driven Development) using **[groovy]**.

And all of this happens using **[roboelectric]** which avoids the hassle and latency involved in doing testing on an actual android device or worse, an emulator.

## Usage

**Note:** Since 1.0.0, the plugin requires [gradle 2.2][gradle].
To use the plugin you must must load it into the buildscript like so:

```groovy
buildscript {
	repositories {
		jcenter()
	}
	dependencies {
		classpath 'com.jakewharton.sdkmanager:gradle-plugin:0.12.+'
  		classpath 'se.centril.robospock:gradle-plugin-robospock:1.0.1'
  	}
}
```

### Applying from a tester project

Given an android project with the path **`:app`**, and a test project with the
path **`:test`**, we can apply the plugin on **`:app`** with:

```groovy
apply plugin: 'robospock'

robospock {
	android = project( ':app' )
}
```

If your test project is named **`app-test`**, i.e the same as the application or library but with the suffix `-test` (to be exact, the regex: `/[^a-zA-Z0-9]?test/`), and if the app is the parent of the test project or is a sibling of it, then you may simply omit specifying the `robospock.android` part alltogether as it is automatically configured. Then it becomes:

```groovy
apply plugin: 'robospock'
```

When you have done this, you can use the **`test`** task as usual, or **`robospock`** task if you only want to run robospock tests.

### Applying from an android project (Since 0.2.0)

It is also possible to apply the plugin from an android project like so:

```groovy
apply plugin: 'robospock'
robospock {
	tester = project( ':app-test' )
}
```

If you have a project named **`test`** as a child or **`app-test`** as a child or a sibling of the android project, it will be automatically found and used. This can rid you of the need for a `build.gradle` file for the tester project altogether.

### Advanced options

The available options are:

```groovy
robospock {
	// Sets the android project to test, from a tester project.
	android = <gradle_android_project_object>

	// Sets the tester project to test with, from an android project.
	tester = <gradle_tester_project_object>

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

	// You can provide closures that will be run
	// after RoboSpockAction has finished its work.
	afterConfigured { RoboSpockConfiguration c ->
		// do some additional configuration.
	}

	// (Read Only) robospock testing task,
	// can only be read from afterConfigured closures.
	robospockTask

	// (Read Only) The project you applied this plugin from.
	perspective
}
```

By default, the plugin adds the much used optional spock-dependencies **`objenesis`** and **`cglib`** which are used for mocking, etc. You can change the version used, or disable them by setting an empty version.

## Known Issues

+ IntelliJ & Android Studio:s gradle plugin might trigger the error: `project ':x' is not an android project`. It should however work with running the tests via gradle manually. This is a bug in IntelliJ. See [issue #5].

## Changelog

See **[CHANGES.md]**

## Bugs / Issues / Feature requests / Contribution

Want to contribute? Great stuff! Please use the issue system that github provides to report bugs/issues or request an enhancement. Pull requests are also more than welcome.

## License

**[gradle-plugin-robospock]** is licensed under **Apache License 2.0**, see **[LICENSE.md]** for more information.

## Authors

See **[AUTHORS.md]**

<!-- references -->

[gradle-plugin-robospock]: https://github.com/Centril/gradle-plugin-robospock

[badge: Download]: https://api.bintray.com/packages/centril/maven/se.centril.robospock%3Agradle-plugin-robospock/images/download.svg
[badge url: Download]: https://bintray.com/centril/maven/se.centril.robospock%3Agradle-plugin-robospock/_latestVersion
[badge: Build Status]: http://img.shields.io/travis/Centril/gradle-plugin-robospock.svg
[badge url: Build Status]: https://travis-ci.org/Centril/gradle-plugin-robospock
[badge: Coverage Status]: http://img.shields.io/coveralls/Centril/gradle-plugin-robospock.svg
[badge url: Coverage Status]: https://coveralls.io/r/Centril/gradle-plugin-robospock
[badge: License]: http://img.shields.io/badge/license-ASL_2.0-blue.svg
[badge: Semver 2.0.0]: http://img.shields.io/badge/semver-2.0.0-blue.svg
[badge url: Semver 2.0.0]: http://semver.org/spec/v2.0.0.html
[badge: gratipay]: http://img.shields.io/gratipay/Centril.svg
[badge url: gratipay]: https://gratipay.com/Centril

[robospock]: https://github.com/Polidea/RoboSpock
[gradle]: http://www.gradle.org/
[spock]: https://github.com/spockframework/spock
[roboelectric]: http://robolectric.org
[groovy]: http://groovy.codehaus.org

[CHANGES.md]: CHANGES.md
[LICENSE.md]: LICENSE.md
[AUTHORS.md]: AUTHORS.md

[issue #5]: /../../issues/5

<!-- references -->

