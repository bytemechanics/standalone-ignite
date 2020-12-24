# Standalone ignite
[![Latest version](https://maven-badges.herokuapp.com/maven-central/org.bytemechanics/standalone-ignite/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.bytemechanics/standalone-ignite/badge.svg)
[![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=org.bytemechanics%3Astandalone-ignite&metric=alert_status)](https://sonarcloud.io/dashboard/index/org.bytemechanics%3Astandalone-ignite)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.bytemechanics%3Astandalone-ignite&metric=coverage)](https://sonarcloud.io/dashboard/index/org.bytemechanics%3Astandalone-ignite)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Library to reduce the code necessary to start and make a controlled shutdown of standalone applications. The scope of this library is to provide solutions for:
* Easy parse of command line parameters
* Easy start and stop daemons (standalone services) inside your standalone application
* Easy detection of O.S. stop requirement to make a graceful shutdown

## Motivation
Some times make something so simple as batch generates a lot of boilerplate source with this library we intend to make this startup easier and faster as well as keeping the control of all startup process.

## Requirements
JDK8

## Quick start
(Please read our [Javadoc](https://standalone-ignite.bytemechanics.org/javadoc/index.html) for further information)
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

2. Create the standalone application main class
```Java
package mypackage;
```

3. If some parameters are needed, create an enumerate with all necessary parameters
```Java
package mypackage;

import java.util.Optional;
import java.util.function.Function;
import org.bytemechanics.standalone.ignite.beans.DefaultParameterContainer;

public enum StandaloneAppTestParameter implements Parameter{

	BOOLEANVALUE(boolean.class,"boolean value"),
	INTVALUE(int.class,"int value"),
	LONGVALUE(long.class,"long value"),
	FLOATVALUE(float.class,"float value"),
	DOUBLEVALUE(double.class,"double value"),
	STRINGVALUE(String.class,"string value"),
	;
	
	private final DefaultParameterContainer container;
	
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type,final String _description){
		this(_type,_description,null,null);
	}
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type,final String _description,final String _default){
		this(_type,_description,_default,null);
	}
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type,final String _description,final String _default,final Function<String,T> _parser){
		this.container=DefaultParameterContainer.builder()
												.name(name())
												.type(_type)
												.description(_description)
												.defaultValue(_default)
												.parser((Function<String,Object>)_parser)
											.build();
	}

	@Override
	public Class getType() {
		return this.container.getType();
	}

	@Override
	public Function<String, Object> getParser() {
		return this.container.getParser();
	}

	@Override
	public Optional<Object> getValue() {
		return this.container.getValue();
	}

	@Override
	public Parameter setValue(Object _value) {
		return this.container.setValue(_value);
	}

	@Override
	public Optional<String> getDefaultValue() {
		return this.container.getDefaultValue();
	}

	@Override
	public String getDescription() {
		return this.container.getDescription();
	}
}
```

4. Into your main instantiate Standalone
* Option 1: Using `org.bytemechanics.standalone.ignite.Ignitable` interface
```Java
package mypackage;

import java.util.Optional;
import java.util.function.Function;
import org.bytemechanics.standalone.ignite.Ignitable;
import org.bytemechanics.standalone.ignite.beans.DefaultParameterContainer;


public final class StandaloneApp implements Ignitable{

	@Override
	public void startup() {
		// start your application
	}

	@Override
	public void shutdown() {
		// shutdown your application (optional)
	}

	(...)

	public static final void main(final String... _args){
		Standalone.builder(StandaloneApp::new)
					.arguments(_args)
					(...)
					.parameters(StandaloneAppTestParameter.class)
					(...)
				.build();
					.ignite();
	}
}
```
* Option 2: Using `org.bytemechanics.standalone.ignite.IgnitableAdapter` abstract class
```Java
package mypackage;

import java.util.Optional;
import java.util.function.Function;
import org.bytemechanics.standalone.ignite.IgnitableAdapter;


public final class StandaloneApp extends IgnitableAdapter{

	@Override
	public void startup() {
		// start your application
	}

	@Override
	public void shutdown() {
		// shutdown your application (optional)
	}
	(...)

	public static final void main(final String... _args){
		Standalone.builder(StandaloneApp::new)
					.arguments(_args)
					(...)
					.parameters(StandaloneAppTestParameter.class)
					(...)
				.build();
					.ignite();
	}
}
```
