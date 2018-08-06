# Standalone ignite
Library to reduce the code necessary to start and make a controlled shutdown of standalone applications. The scope of this library is to provide solutions for:
* Easy parse of command line parameters
* Easy start and stop daemons (standalone services) inside your standalone application
* Easy detection of O.S. stop requirement to make a graceful shutdown

## Motivation
Some times make something so simple as batch generates a lot of boilerplate source with this library we intend to make this startup easier and faster as well as keeping the control of all startup process.

## Requirements
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
1. Create the standalone application main class
```
package mypackage;
```

1. If some parameters are needed, create an enumerate with all necessary parameters
```
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

1. Into your main instantiate Standalone
```
package mypackage;

import java.util.Optional;
import java.util.function.Function;
import org.bytemechanics.standalone.ignite.beans.DefaultParameterContainer;


public final class StandaloneApp implements Ignitable{

	@Override
	public void beforeStartup() {
		System.out.println("before-startup");
	}
	@Override
	public void startup() {
		System.out.println("startup");
	}
	@Override
	public void afterStartup() {
		System.out.println("after-startup");
	}


	@Override
	public void beforeShutdown() {
		System.out.println("before-shutdown");
	}
	@Override
	public void shutdown() {
		System.out.println("shutdown");
	}
	@Override
	public void afterShutdown() {
		System.out.println("after-shutdown");
	}

	(...)

	public static final void main(final String... _args){
		Standalone.builder()
					.arguments(_args)
					.supplier(StandaloneApp::new)
					(...)
					.parameters(StandaloneAppTestParameter.class)
					(...)
				.build();
		(...)
	}
}
```

1. Into your main instantiate launch standalone calling ignite() method
```
package mypackage;

import java.util.Optional;
import java.util.function.Function;
import org.bytemechanics.standalone.ignite.beans.DefaultParameterContainer;


public final class StandaloneApp implements Ignitable{

	(...)

	public static final void main(final String... _args){
		Standalone.builder()
					.arguments(_args)
					.supplier(StandaloneApp::new)
					.parameters(StandaloneAppTestParameter.class)
					(...)
				.build()
					.ignite();
	}
}
```