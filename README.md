LGIO, Gradle plugin
===================

LGIO *(__L__ightweight __G__radle __IO__)* is a plugin that solves the problem of
System.console() being null by using a GUI in those cases

Usage
-------------------

Using LGIO in your project is quite staightforward.

**Add the following to your buildscript:**
```groovy
buildscript {
  repositories {
  	maven {
  		url 'https://github.com/toxbee/mvn-repo/raw/master/maven-deploy'
  	}
  	mavenCentral()
  }
  dependencies {
  	classpath 'se.toxbee.lgio:gradle-plugin-lgio:0.1'
  }
}
```

**Henceforth you can use it like:**
```groovy
apply plugin 'lgio'

// In some task:
lgio.println( "Reading signing configuration" )

def storeFile = file( lgio.readLineReq( "Keystore file, rel path" ) )
def keyAlias = io.readLineReq( "Key alias" )
def storePw = io.readPasswordReq( "Keystore password" )
def keyPw = io.readPasswordReq( "Key password" )
```

A method that ends with Req, for example readLineReq does the same as readLine
but the *Req version throws an exception when an empty string is given by the user/client.

**Normally, lgio & io can be used interchangably, they are aliases.**

To stop lgio from io as an alias, set:
```groovy
project.ext.lgioAliasDisable = true
```

Bugs / Issues / Feature requests
--------------------------------
Please use the issue system that github provides to report bugs/issues or request an enhancement.

This project is in its infancy, so pull requests are more than welcome.

License
-------

LGIO is licensed under **Apache License 2.0**, see LICENSE for more information.

toxbee is the developing organisation and is currently maintained by Centril
