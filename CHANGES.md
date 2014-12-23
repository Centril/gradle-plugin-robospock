CHANGELOG
=====================

1.0.1
---------------------
+ Fixed issue with gradle versions X.Y.Z where Z > 0. See **[pull request #13](/../../pull/13).**

1.0.0
---------------------
+ **NOTE:** This is the first major release, API won't change from now on
until the next major release.
+ **BREAKING:** Plugin now requires Gradle >= 2.2.
+ **BREAKING:** It is unfortunately no longer supported to automatically generate a tester project. Gradle doesn't support dynamic project creation.
+ **BREAKING:** Reading of `project.ext.robospockTester` removed. The plugin now always does its work in `afterEvaluate` and setting the tester project when applying the plugin on an android project via `robospock { tester = project(':app') }` now works.

0.3.2
---------------------
+ **DOCUMENTATION:** [README](README.md) now correctly specifies how to use the plugin. the android-sdk-manager plugin must be applied as well.
+ **PERFORMANCE:** Fix #8, Filtering out duplicate project dependencies.
+ **BUGFIX:** Removed all references to the android plugin, it is no longer needed as a buildscript classpath, for the plugin the plugin is applied on, for the plugin to run.
+ **BUGFIX:** Fix #10, Using URL/URI instead of straight path for maven URLs.

0.3.1
---------------------
+ **PERFORMANCE:** The robospock task won't be run if there are no changes now, instead it will become `UP-TO-DATE` as it should be. This should cut down on build times considerably when you modify some project but not another and the tests do not change.
+ **BUGFIX:** sdkmanager self-cast errror hopefully fixed, **[issue #6](/../../issues/6).**
+ **dev:** Travis CI should print out failures in tests verbosely.

0.3.0
---------------------
+ **BREAKING:** **String setters of properties `robospock.tester` and  `robospock.android` removed.** Wrap the old value with: `project( <old_value> )` See https://jira.codehaus.org/browse/GROOVY-2500 for why this was necessary.
+ **BUGFIX:** JDK1.7 compability hopefully for real this time.
+ **BUGFIX:** Improved compability for libraries and better handling of some Robolectric bugs. See **[issue #6](/../../issues/6).**

0.2.1
---------------------
+ **BUGFIX:** added jdk1.7 compatibility to build.

0.2.0
---------------------
+ **FEATURE:** **Now possible to apply robospock on android plugin.**
    + If you must specify what the tester project is, you must do so in:
	`project.ext.robospockTester`.
    + Otherwise the plugin looks for a child of android project named `test` or children according to the rules for `robospock.android`.
    + Next it looks for a sibling according to `robospock.android` rules.
    + Finally, it tries to look for files in the directory `test` which is a sibling of the parent of the first directory specified in `android.sourceSets.main.java.srcDirs`, which with the standard setup becomes: `{android-root}/src/test`. If that is occupied by the sourceSet `androidTest` it will instead use the directory named `unit-test`. This will also create a test project dynamically for you as a subproject of the android project and name it `{android-project-name}-test`
+ **FEATURE:** **Added afterConfigured closure to robospock extension.**
+ **FEATURE:** **Added robospockTask to robospock extension.**
+ **BREAKING:** **property `robospock.testing` renamed `robospock.android`.**

0.1.2
---------------------
+ Fixed Travis CI errors due to 64/32 bit conflicts.
  see: https://github.com/JakeWharton/sdk-manager-plugin/issues/13
+ Fixed an issue with Travis CI due to android support dependency on Roboelectric.

0.1.1
---------------------
+ Changed to jcenter() everywhere.
+ Everything in 1 gradle project hierarchy instead of 1 for plugin & 1 for testing.

0.1.0
---------------------
+ Initial version
+ As of [f7bb9c86d7055bf491fdc09ba327885f16540467](https://github.com/Centril/gradle-plugin-robospock/commit/9fedfc1393911ba0d10211ef6593e9447baa982b), the plugin is stable & is usable.
