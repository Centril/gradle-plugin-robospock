CHANGELOG
=====================

0.2.0 (Not released)
---------------------
+ FEATURE: Now possible to apply robospock on android plugin.
  if you must specify what the tester project is, you must do so in:
	`project.ext.robospockTester`.
  Otherwise the plugin looks for a child of android project named
  `test` or children according to the rules for `robospock.android`.
  Next it looks for a sibling according to `robospock.android` rules.
+ BREAKING:	property `robospock.testing` removed, now `robospock.android`.

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
