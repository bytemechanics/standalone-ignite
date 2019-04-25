/*
 * Copyright 2018 Byte Mechanics.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bytemechanics.standalone.ignite

import org.bytemechanics.standalone.logger.*
import org.bytemechanics.standalone.ignite.internal.commons.string.*
import spock.lang.*
import spock.lang.Specification
import spock.lang.Unroll
import java.nio.charset.Charset
import java.io.*
import java.nio.file.*
import java.util.Queue
import java.util.LinkedList
import org.bytemechanics.standalone.ignite.exceptions.*

import java.util.logging.*

/**
 *
 * @author afarre
 */
class StandaloneSpec extends Specification{

	def setupSpec(){
		println(">>>>> StandaloneSpec >>>> setupSpec")
		final InputStream inputStream = Standalone.class.getResourceAsStream("/logging.properties");
		try{
			LogManager.getLogManager().readConfiguration(inputStream);
		}catch (final IOException e){
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}finally{
			if(inputStream!=null)
				inputStream.close();
		}
	}
	
	@Unroll
	def "When supplier is not provided a NullPointerException is raised"(){
		println(">>>>> StandaloneSpec >>>> When supplier is not provided a NullPointerException is raised")
		setup:
			Ignitable ignitable=Mock()
		
		when:
			Standalone.builder()
						.build()
							.ignite()

		then: 
			def e=thrown(NullPointerException) 
			e.getMessage()=="Mandatory \"supplier\" can not be null"
	}
	
	@Unroll
	def "Ignite call should call ignitable startup (before and after)"(){
		println(">>>>> StandaloneSpec >>>> Ignite call should call startup (before and after)")
		setup:
			Ignitable ignitable=Mock()
		
		when:
			Standalone.builder()
							.supplier({ -> ignitable})
						.build()
							.ignite()

		then: 
			1 * ignitable.beforeStartup()
			1 * ignitable.startup()  
			1 * ignitable.afterStartup() 
	}

	@Unroll
	def "When shutdown is call before and after must be executed"(){
		println(">>>>> StandaloneSpec >>>> When shutdown is call before and after must be executed")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build();
		
		when:
			standalone.ignite()
			standalone.shutdown()

		then: 
			1 * ignitable.beforeStartup() 
			1 * ignitable.startup() 
			1 * ignitable.afterStartup() 
			1 * ignitable.beforeShutdown() 
			1 * ignitable.shutdown() 
			1 * ignitable.afterShutdown()  
	}

	def "ParseParameters must do nothing if no parameters defined"(){
		println(">>>>> StandaloneSpec >>>> ParseParameters must do nothing if no parameters defined")
		when:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build()
			Standalone standalone2=standalone.parseParameters()
		then:
			standalone==standalone2
	}

	@Unroll
	def "ParseParameters #arguments for #parameters must parse correctly the correct values"(){
		println(">>>>> StandaloneSpec >>>> ParseParameters $arguments for $parameters must parse correctly the correct values")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
												.parameters(StandaloneAppTestParameter.class)
												.parameters(StandaloneAppTestParameter2.class)
												.arguments(arguments)
											.build();

		when:
			standalone.parseParameters()

		then: 
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(boolean.class).isPresent()
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(boolean.class).get()==true
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(int.class).isPresent()
			StandaloneAppTestParameter.INTVALUE.getValue(int.class).get()==2234
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(long.class).isPresent()
			StandaloneAppTestParameter.LONGVALUE.getValue(long.class).get()==3243321312
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(float.class).isPresent()
			StandaloneAppTestParameter.FLOATVALUE.getValue(float.class).get()==3123.32f
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(double.class).isPresent()
			StandaloneAppTestParameter.DOUBLEVALUE.getValue(double.class).get()==3123.32d
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(String.class).isPresent()
			StandaloneAppTestParameter.STRINGVALUE.getValue(String.class).get()=="TEST"
			StandaloneAppTestParameter.ENUMVALUE.getValue(StandaloneAppTestParameter.class).get()==StandaloneAppTestParameter.ENUMVALUE
			StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE.getValue(boolean.class).isPresent()
			StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE.getValue(boolean.class).get()==true
			StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE.getValue(int.class).isPresent()
			StandaloneAppTestParameter2.ADDITIONALINTVALUE.getValue(int.class).get()==2234
			StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE.getValue(long.class).isPresent()
			StandaloneAppTestParameter2.ADDITIONALLONGVALUE.getValue(long.class).get()==3243321312
			StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE.getValue(float.class).isPresent()
			StandaloneAppTestParameter2.ADDITIONALFLOATVALUE.getValue(float.class).get()==3123.32f
			StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE.getValue(double.class).isPresent()
			StandaloneAppTestParameter2.ADDITIONALDOUBLEVALUE.getValue(double.class).get()==3123.32d
			StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE.getValue(String.class).isPresent()
			StandaloneAppTestParameter2.ADDITIONALSTRINGVALUE.getValue(String.class).get()=="TEST"
			StandaloneAppTestParameter2.ADDITIONALENUMVALUE.getValue(StandaloneAppTestParameter.class).get()==StandaloneAppTestParameter2.ADDITIONALENUMVALUE
	
		where:
			parameters=StandaloneAppTestParameter.class
			arguments=["-booleanvalue:true","-intvalue:2234","-longvalue:3243321312","-floatvalue:3123.32","-doublevalue:3123.32","-stringvalue:TEST","-enumvalue:ENUMVALUE",
						"-additionalbooleanvalue:true","-additionalintvalue:2234","-additionallongvalue:3243321312","-additionalfloatvalue:3123.32","-additionaldoublevalue:3123.32","-additionalstringvalue:TEST","-additionalenumvalue:ADDITIONALENUMVALUE"]
					.toArray(new String[4])
	}
	@Unroll
	def "ParseParameters #arguments for #parameters must validate the parameters"(){
		println(">>>>> StandaloneSpec >>>> ParseParameters $arguments for $parameters must validate the parameters")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
												.parameters(StandaloneAppTestParameter.class)
												.arguments(arguments)
											.build();

		when:
			standalone.ignite()

		then: 
			def e=thrown(InvalidParameter)
			e.getMessage()=="Invalid parameter STRINGVALUE with value semanticFailure: semantic test error requested"
	
		where:
			parameters=StandaloneAppTestParameter.class
			arguments=["-booleanvalue:true","-intvalue:2234","-longvalue:3243321312","-floatvalue:3123.32","-doublevalue:3123.32","-stringvalue:semanticFailure","-enumvalue:ENUMVALUE"].toArray(new String[4])
	}

	def "When exception happens on beforeStartup startupException must be called"(){
		println(">>>>> StandaloneSpec >>>> When exception happens on beforeStartup startupException must be called")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build();
		
		when:
			ignitable.beforeStartup() >> { throw new RuntimeException("ouch") }
			standalone.ignite()

		then: 
			1 * ignitable.startupException(_)
	}
	def "When exception happens on startup startupException must be called"(){
		println(">>>>> StandaloneSpec >>>> When exception happens on startup startupException must be called")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build();
		
		when:
			ignitable.startup() >> { throw new RuntimeException("ouch") }
			standalone.ignite()

		then: 
			1 * ignitable.startupException(_)
	}
	def "When exception happens on afterStartup startupException must be called"(){
		println(">>>>> StandaloneSpec >>>> When exception happens on afterStartup startupException must be called")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build();
		
		when:
			ignitable.afterStartup() >> { throw new RuntimeException("ouch") }
			standalone.ignite()

		then: 
			1 * ignitable.startupException(_)
	}

	def "When exception happens on beforeShutdown shutdownException must be called"(){
		println(">>>>> StandaloneSpec >>>> When exception happens on beforeShutdown shutdownException must be called")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build();
		
		when:
			ignitable.beforeShutdown() >> { throw new RuntimeException("ouch") }
			standalone.ignite().shutdown()

		then: 
			1 * ignitable.shutdownException(_)
	}
	def "When exception happens on shutdown shutdownException must be called"(){
		println(">>>>> StandaloneSpec >>>> When exception happens on shutdown shutdownException must be called")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build();
		
		when:
			ignitable.shutdown() >> { throw new RuntimeException("ouch") }
			standalone.ignite().shutdown()

		then: 
			1 * ignitable.shutdownException(_)
	}
	def "When exception happens on afterShutdown shutdownException must be called"(){
		println(">>>>> StandaloneSpec >>>> When exception happens on afterShutdown shutdownException must be called")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build();
		
		when:
			ignitable.afterShutdown() >> { throw new RuntimeException("ouch") }
			standalone.ignite().shutdown()

		then: 
			1 * ignitable.shutdownException(_)
	}
	
	@Unroll
	def "Ingnite with name should print a banner with the given #name name and #font font"(){
		println(">>>>> StandaloneSpec >>>> Ingnite with name should print a banner with the given $name name and $font font")
		setup:
			Ignitable ignitable=Mock()
			Queue console=new LinkedList();
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
												.name(name)
												.bannerFont(fontURL)
												.console({message -> console.add(message)})
											.build();
			def javaVersion=SimpleFormat.format("\tJVM: {}",System.getProperty("java.version"));
			def basePath=SimpleFormat.format("\tBase path: {}",new File(".").getCanonicalPath());
			def cores=SimpleFormat.format("\tCores: {}",Runtime.getRuntime().availableProcessors());
			def memory=SimpleFormat.format("\tMemory (bytes): {}/{}",Runtime.getRuntime().totalMemory(),Runtime.getRuntime().maxMemory());
			final Package instancePackage=ignitable.getClass().getPackage();
			def version=SimpleFormat.format("\tVersion: {}/{}",(instancePackage!=null)? instancePackage.getSpecificationVersion() : "unknown",
															(instancePackage!=null)? instancePackage.getImplementationVersion() : "unknown");
			
		when:
			standalone.ignite()

		then: 
			console.poll()==line
			console.poll()==banner
			console.poll()==linesimple
			console.poll()==javaVersion
			console.poll()==cores
			console.poll()==memory
			console.poll()==basePath
			console.poll()==version
			console.poll()==line

		where:
			name				| font				| fontURL
			"testerot-null"		| null				| null	
			"testerot2-null"	| null				| null
			"testerot-standard"	| "standard.flf"	| Standalone.class.getClassLoader().getSystemResource("standard.flf")
			"testerot2-standard"| "standard.flf"	| Standalone.class.getClassLoader().getSystemResource("standard.flf")
			"testerot-universe"	| "basic_1.flf"		| ParameterTest.class.getClassLoader().getSystemResource("basic_1.flf")
			"testerot2-universe"| "basic_1.flf"		| ParameterTest.class.getClassLoader().getSystemResource("basic_1.flf")
			
			effectiveFontURL=(fontURL==null)? Standalone.class.getClassLoader().getSystemResource("standard.flf") : fontURL
			banner=	(new Figlet(
								effectiveFontURL.openStream()
								,Charset.forName("UTF-8")))
							.print(name)
			line=(new Figlet(
								effectiveFontURL.openStream()
								,Charset.forName("UTF-8")))
							.line(name,(char)'=')
			linesimple=(new Figlet(
								effectiveFontURL.openStream()
								,Charset.forName("UTF-8")))
							.line(name,(char)'-')
	}
	
	def "Provide a wrong file format font should raise a NoFigletFontFormatException exception"(){
		println(">>>>> StandaloneSpec >>>> Provide a wrong file format font should raise a NoFigletFontFormatException exception")
		setup:
			Ignitable ignitable=Mock()
			Queue console=new LinkedList();
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
												.name("name")
												.bannerFont(Paths.get("./src/test/resources/logging.properties").toUri().toURL())
												.console({message -> console.add(message)})
											.build();
		when:
			standalone.ignite()

		then: 
			def e=thrown(Figlet.NoFigletFontFormatException)
			e.getMessage()=="Input has not figlet font file format (.flf)"
	}
	
	@Unroll
	def "If no mandatory parameter provided show error message and print help"(){
		println(">>>>> StandaloneSpec >>>> If no mandatory parameter provided show error message and print help")
		setup:
			Ignitable ignitable=Mock()
			Queue console=new LinkedList();
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
												.parameters(StandaloneAppTestParameter.class)
												.console({message -> console.add(message)})
											.build();
			
		when:
			standalone.ignite()

		then: 
			console.poll()==new MandatoryArgumentNotProvided(StandaloneAppTestParameter.BOOLEANVALUE).getMessage()
			console.poll()==Parameter.getHelp(StandaloneAppTestParameter.class)
	}

	@Unroll
	def "If no mandatory parameter of multiple provided show error message and print help"(){
		println(">>>>> StandaloneSpec >>>> If no mandatory parameter of multiple provided show error message and print help")
		setup:
			Ignitable ignitable=Mock()
			Queue console=new LinkedList();
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
												.parameters(StandaloneAppTestParameter.class)
												.parameters(StandaloneAppTestParameter2.class)
												.console({message -> console.add(message)})
												.arguments(["-booleanvalue:true","-intvalue:2234","-longvalue:3243321312","-floatvalue:3123.32","-doublevalue:3123.32","-stringvalue:TEST","-enumvalue:ENUMVALUE"].toArray(new String[4]))
											.build();
			
		when:
			standalone.ignite()

		then: 
			console.poll()==new MandatoryArgumentNotProvided(StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE).getMessage()
			console.poll()==Parameter.getHelp([StandaloneAppTestParameter.class,StandaloneAppTestParameter2.class])
	}

	def "Static selfExtinguish method should do nothing if no instance exist"(){
		println(">>>>> StandaloneSpec >>>> Static extinguish method should do nothing if no instance exist")
		setup:
			Standalone standalone=Mock()
			Standalone.self=null
			
		when:
			Standalone.selfExtinguish(2)

		then: 
			0 * standalone.extinguish(_) >> { }
	}

	def "Static selfExtinguish method should call the referenced self instance if exist"(){
		println(">>>>> StandaloneSpec >>>> Static selfExtinguish method should call the referenced self instance if exist")
		setup:
			Standalone standalone=Mock()
			Standalone.self=standalone
			
		when:
			Standalone.selfExtinguish(2)

		then: 
			1 * standalone.extinguish(2) >> { }
	}
}

