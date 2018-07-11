# Standalone ignite
Library to reduce the code necessary to start and make a controlled shutdown of standalone applications. The scope of this library is to provide solutions for:
* Easy parse of command line parameters
* Easy start and stop daemons (standalone services) inside your standalone application
* Easy detection of O.S. stop requirement to make a graceful shutdown

## Motivation
Some times make something so simple as batch generates a lot of boilerplate source with this library we intend to make this startup easier and faster as well as keeping the control of all startup process.

##Requirements
JDK8

## Quick start
1. First of all include the Jar file in your compile and execution classpath.
### Maven
```Maven
	<dependency>
		<groupId>org.bytemechanics</groupId>
		<artifactId>standalone-ignite</artifactId>
		<version>X.X.X</version>
	</dependency>
```
### Graddle
```Gradle
dependencies {
    compile 'org.bytemechanics:standalone-ignite:X.X.X'
}
```
