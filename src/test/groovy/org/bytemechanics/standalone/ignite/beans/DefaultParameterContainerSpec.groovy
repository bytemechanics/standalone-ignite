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
package org.bytemechanics.standalone.ignite.beans

import spock.lang.*
import spock.lang.Specification
import spock.lang.Unroll

import java.util.function.*
import java.text.*
import java.net.*
import java.io.*
import java.nio.file.*
import java.util.logging.*
import java.time.*
import java.time.format.*
import org.bytemechanics.standalone.ignite.internal.commons.reflection.PrimitiveTypeConverter;
import org.bytemechanics.standalone.ignite.internal.commons.string.*
import org.bytemechanics.standalone.ignite.exceptions.MandatoryArgumentNotProvided
import org.bytemechanics.standalone.ignite.exceptions.UnparseableParameter
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat

/**
 * @author afarre
 */
class DefaultParameterContainerSpec extends Specification{

	def setupSpec(){
		println(">>>>> DefaultParameterContainerSpecs >>>> setupSpec")
		final InputStream inputStream = DefaultParameterContainer.class.getResourceAsStream("/logging.properties");
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
	
	def "Builder must throw NullPointerException if no name is provided"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Builder must throw NullPointerException if no name is provided")
		when:
			def DefaultParameterContainer container=DefaultParameterContainer.builder()
																				.type(boolean.class)
																				.description("")
																			.build()

		then:
			def e=thrown(NullPointerException) 
			e.getMessage()=="name"

	}
	def "Builder must throw NullPointerException if no type is provided"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Builder must throw NullPointerException if no type is provided")
		when:
			def DefaultParameterContainer container=DefaultParameterContainer.builder()
																				.name("name")
																				.description("")
																			.build()

		then:
			def e=thrown(NullPointerException) 
			e.getMessage()=="type"

	}
	def "Builder must throw NullPointerException if no description is provided"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Builder must throw NullPointerException if no type is provided")
		when:
			def DefaultParameterContainer container=DefaultParameterContainer.builder()
																				.type(boolean.class)
																				.name("name")
																			.build()

		then:
			def e=thrown(NullPointerException) 
			e.getMessage()=="description"

	}
	def "Constructor must throw NullPointerException if no name is provided"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Constructor must throw NullPointerException if no name is provided")
		when:
			def DefaultParameterContainer container=new DefaultParameterContainer(null,boolean.class,"",null,null);

		then:
			def e=thrown(NullPointerException) 
			e.getMessage()=="name"

	}
	def "Constructor must throw NullPointerException if no type is provided"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Constructor must throw NullPointerException if no type is provided")
		when:
			def DefaultParameterContainer container=new DefaultParameterContainer("name",null,"",null,null);

		then:
			def e=thrown(NullPointerException) 
			e.getMessage()=="type"

	}
	def "Constructor must throw NullPointerException if no description is provided"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Constructor must throw NullPointerException if no type is provided")
		when:
			def DefaultParameterContainer container=new DefaultParameterContainer("name",boolean.class,null,null,null);

		then:
			def e=thrown(NullPointerException) 
			e.getMessage()=="description"

	}
	def "Constructor with name and type must be created without problem being mandatory"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Constructor with name and type must be created without problem being mandatory")
		when:
			def DefaultParameterContainer container=new DefaultParameterContainer("name",boolean.class,"description",null,null);

		then:
			container.isMandatory()
			container.name()=="name"
			container.getType()==Boolean.class
			container.getDescription()=="description"
			container.getHelp()=="[-name]: description (Mandatory)"

	}
	
	@Unroll
	def "When new instance created with name:#name,type:#type,parser:null and defaultValue:#defaultValue then the value stored should be the default #parsed one"(){
		println(">>>>> DefaultParameterContainerSpec >>>> When new instance created with name:$name,type:$type,parser:null and defaultValue:$defaultValue the value stored should be the default $parsed one")
		when:
			DefaultParameterContainer container=new DefaultParameterContainer(name,type,"description",null,defaultValue)

		then: 
			container.name()==name
			container.getType()==typeLoaded
			container.getParser()!=null
			container.getDefaultValue().isPresent()
			container.getDefaultValue().get()==defaultValue
			container.getValue().isPresent()
			container.getValue().get()==parsed
		
		where:
			name								| type						| defaultValue							| parsed
			"true-false"						| boolean.class				| "true"								| true									
 			"false-false"						| boolean.class				| "false"								| false								
 			"true-true"							| boolean.class				| "true"								| true									
 			"true-false"						| Boolean.class				| "false"								| Boolean.FALSE
 			"true-TRUE"							| Boolean.class				| "TRUE"								| Boolean.TRUE									
 			"true-FALSE"						| Boolean.class				| "FALSE"								| Boolean.FALSE									
 			"char-a"							| char.class				| "a"									| (char)'a'										
 			"char-b"							| char.class				| "b"									| (char)'b'										
 			"char-c"							| Character.class			| "c"									| Character.valueOf((char)'c')										
 			"char-e"							| char.class				| "e"									| (char)'e'										
 			"char-d"							| char.class				| "d"									| (char)'d'										
 			"char-3"							| char.class				| "3"									| (char)'3'										
 			"char-@"							| Character.class			| "@"									| Character.valueOf((char)'@')									
 			"char-&"							| Character.class			| "&"									| Character.valueOf((char)'&')										
 			"char-/"							| Character.class			| "/"									| Character.valueOf((char)'/')										
 			"char-("							| char.class				| "("									| (char)'('										
 			"char-(df"							| char.class				| "(df"									| (char)'('									
			"short-min"							| short.class				| String.valueOf(Short.MIN_VALUE)		| (short)Short.MIN_VALUE			
 			"short-100"							| short.class				| "-100"								| (short)-100									
 			"short-0"							| short.class				| "0"									| (short)0										
 			"short-24125"						| Short.class				| "24125"								| Short.valueOf("24125")									
 			"short-max"							| Short.class				| String.valueOf(Short.MAX_VALUE)		| Short.MAX_VALUE			
			"int-min"							| int.class					| String.valueOf(Integer.MIN_VALUE)		| (int)Integer.MIN_VALUE		
 			"int-100"							| int.class					| "-100"								| -100									
 			"int-0"								| int.class					| "0"									| Integer.valueOf("0")										
 			"int-24125"							| Integer.class				| "24125"								| Integer.valueOf("24125")									
 			"int-max"							| Integer.class				| String.valueOf(Integer.MAX_VALUE)		| Integer.MAX_VALUE		
			"long-min"							| long.class				| String.valueOf(Long.MIN_VALUE)		| (long)Long.MIN_VALUE			
 			"long-100"							| long.class				| "-100"								| -100l									
 			"long-0"							| long.class				| "0"									| 0l										
 			"long-24125"						| Long.class				| "24125"								| Long.valueOf(24125l)									
 			"long-max"							| Long.class				| String.valueOf(Long.MAX_VALUE)		| Long.MAX_VALUE			
			"float-min"							| float.class				| String.valueOf(Float.MIN_VALUE)		| (float)Float.MIN_VALUE
 			"float-100.23"						| float.class				| "-100.23"								| -100.23f
 			"float-0.0"							| float.class				| "0.0"									| 0.0f								
 			"float-24125.32435"					| float.class				| "24125.32435"							| 24125.32435f
 			"float-max"							| float.class				| String.valueOf(Float.MAX_VALUE)		| (float)Float.MAX_VALUE			
			"double-min"						| double.class				| String.valueOf(Double.MIN_VALUE)		| (double)Double.MIN_VALUE		
 			"double-100.23"						| double.class				| "-100.32532"							| -100.32532d							
 			"double-0.0"						| double.class				| "0.0"									| 0.0d									
 			"double-24125.32435"				| double.class				| "24125.242"							| 24125.242d								
 			"double-max"						| double.class				| String.valueOf(Double.MAX_VALUE)		| (double)Double.MAX_VALUE		
			"BigDecimal--231,412,432.432423"	| BigDecimal.class			| "-231,412,432.432423"					| BigDecimal.valueOf(-231412432.432423d)					
 			"BigDecimal--100.32435"				| BigDecimal.class			| "-100.32435"							| BigDecimal.valueOf(-100.32435d)							
 			"BigDecimal-0.0"					| BigDecimal.class			| "0.0"									| BigDecimal.valueOf(0.0d)									
 			"BigDecimal-24,125.3211"			| BigDecimal.class			| "24,125.3211"							| BigDecimal.valueOf(24125.3211d)							
 			"BigDecimal-231,412,432.432423"		| BigDecimal.class			| "231,412,432.432423"					| BigDecimal.valueOf(231412432.432423d)					
			"String"							| String.class				| "myMessage"							| "myMessage"								
			"LocalTime-10:15"					| LocalTime.class			| "10:15"								| LocalTime.parse("10:15")									
			"LocalTime-10:15:30"				| LocalTime.class			| "10:15:30"							| LocalTime.parse("10:15:30")							
			"LocalTime-10:15:30.123"			| LocalTime.class			| "10:15:30.123"						| LocalTime.parse("10:15:30.123")						
			"LocalTime-2007-12-03"				| LocalDate.class			| "2007-12-03"							| LocalDate.parse("2007-12-03")					
			"LocalDAte-2007-12-03T10:15"		| LocalDateTime.class		| "2007-12-03T10:15"					| LocalDateTime.parse("2007-12-03T10:15")					
			"LocalDAte-2007-12-03T10:15:30"		| LocalDateTime.class		| "2007-12-03T10:15:30"					| LocalDateTime.parse("2007-12-03T10:15:30")				
			"enum-genericTextParser-ENUM"		| GenericTextParser.class	| "ENUM"								| GenericTextParser.ENUM									
			"enum-genericTextParser-LOCALDATE"	| GenericTextParser.class	| "LOCALDATE"							| GenericTextParser.LOCALDATE								
			
			typeLoaded=PrimitiveTypeConverter.convert(type)
	}
	
	@Unroll
	def "When new instance created with name:#name,type:#type,parser:null and no default value then when retrieve value should return null"(){
		println(">>>>> DefaultParameterContainerSpec >>>> When new instance created with name:$name,type:$type,parser:null and no default value then when retrieve value should return null")
		when:
			DefaultParameterContainer container=new DefaultParameterContainer(name,type,"description",null,null)

		then: 
			container.name()==name
			container.getType()==typeLoaded
			container.getParser()!=null
			!container.getDefaultValue().isPresent()
			!container.getValue().isPresent()

		where:
			name								| type						
			"true-false"						| boolean.class				
 			"false-false"						| boolean.class				
 			"true-true"							| boolean.class				
 			"true-false"						| Boolean.class				
 			"true-TRUE"							| Boolean.class				
 			"true-FALSE"						| Boolean.class				
 			"char-a"							| char.class				
 			"char-b"							| char.class				
 			"char-c"							| Character.class			
 			"char-e"							| char.class				
 			"char-d"							| char.class				
 			"char-3"							| char.class				
 			"char-@"							| Character.class			
 			"char-&"							| Character.class			
 			"char-/"							| Character.class			
 			"char-("							| char.class				
 			"char-(df"							| char.class				
			"short-min"							| short.class				
 			"short-100"							| short.class				
 			"short-0"							| short.class				
 			"short-24125"						| Short.class				
 			"short-max"							| Short.class				
			"int-min"							| int.class					
 			"int-100"							| int.class					
 			"int-0"								| int.class					
 			"int-24125"							| Integer.class				
 			"int-max"							| Integer.class				
			"long-min"							| long.class				
 			"long-100"							| long.class				
 			"long-0"							| long.class				
 			"long-24125"						| Long.class				
 			"long-max"							| Long.class				
			"float-min"							| float.class				
 			"float-100.23"						| float.class				
 			"float-0.0"							| float.class				
 			"float-24125.32435"					| float.class				
 			"float-max"							| float.class				
			"double-min"						| double.class				
 			"double-100.23"						| double.class				
 			"double-0.0"						| double.class				
 			"double-24125.32435"				| double.class				
 			"double-max"						| double.class				
			"BigDecimal--231,412,432.432423"	| BigDecimal.class			
 			"BigDecimal--100.32435"				| BigDecimal.class			
 			"BigDecimal-0.0"					| BigDecimal.class			
 			"BigDecimal-24,125.3211"			| BigDecimal.class			
 			"BigDecimal-231,412,432.432423"		| BigDecimal.class			
			"String"							| String.class				
			"LocalTime-10:15"					| LocalTime.class			
			"LocalTime-10:15:30"				| LocalTime.class			
			"LocalTime-10:15:30.123"			| LocalTime.class			
			"LocalTime-2007-12-03"				| LocalDate.class			
			"LocalDAte-2007-12-03T10:15"		| LocalDateTime.class		
			"LocalDAte-2007-12-03T10:15:30"		| LocalDateTime.class		
			"enum-genericTextParser-ENUM"		| GenericTextParser.class	
			"enum-genericTextParser-LOCALDATE"	| GenericTextParser.class	

			typeLoaded=PrimitiveTypeConverter.convert(type)
	}

	@Unroll
	def "When custom parser:#parser is provided must be used to parse the defaultValue:#defaultValue provided and resolved as #value"(){
		println(">>>>> DefaultParameterContainerSpec >>>> When custom parser:$parser is provided must be used to parse the defaultValue:$defaultValue provided and resolved as $value")
		when:
			DefaultParameterContainer container=DefaultParameterContainer.builder()
																			.name(defaultValue)
																			.type(boolean.class)
																			.description("description")
																			.parser(parser)
																			.defaultValue(defaultValue)
																		.build()

		then: 
			container.getDefaultValue().isPresent()
			container.getDefaultValue().get()==defaultValue
			container.getValue().isPresent()
			container.getValue().get()==value

		where:
			defaultValue	| value		| parser													
			"yes"			| true		| {value -> ("yes".equals(value))? true : false }	
 			"No"			| false		| {value -> ("yes".equals(value))? true : false }		
 			"YES"			| false		| {value -> ("yes".equals(value))? true : false }		
 			"Yes"			| true		| {value -> ("yes".equalsIgnoreCase((String)value))? true : false }		
 			"yES"			| true		| {value -> ("yes".equalsIgnoreCase((String)value))? true : false }		
 			"other"			| false		| {value -> ("yes".equalsIgnoreCase((String)value))? true : false }		
	}

	@Unroll
	def "Search #name,#description with #defaultValue default into #arguments must return #value"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Search $name,$description with $defaultValue default into $arguments must return $value")
		when:
			DefaultParameterContainer container=DefaultParameterContainer.builder()
																			.name(name)
																			.type(boolean.class)
																			.description(description)
																			.defaultValue(defaultValue)
																		.build()

		then: 
			container.findParameter(arguments)==value
			container.getDescription()==description
			container.getHelp()=="[-"+name+"]: "+description+" ("+mandatory+")"

		where:
			name	| defaultValue	| value				| description	 | mandatory
			"server"| null			| "other.com"		| "description1" | "Mandatory" 
			"port"	| null			| "2234"			| "description2" | "Mandatory" 
			"ip"	| "212.12.0.21"	| "212.12.0.21"		| "description3" | "Default: 212.12.0.21" 
			"name"	| "myname"		| "standalone"		| "description4" | "Default: myname" 
			"path"	| "/etc/bin"	| "/etc/bin"		| "description5" | "Default: /etc/bin" 
															
			arguments=["-server:other.com","-port:2234","-name:standalone"].toArray(new String[3])
	}
	
	@Unroll
	def "Search #name with customPrefixes #prefixes and #defaultValue default into #arguments must return #value"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Search $name with customPrefixes $prefixes and $defaultValue default into $arguments must return $value")
		when:
			DefaultParameterContainer container=DefaultParameterContainer.builder()
																			.name(name)
																			.prefixes(prefixes)
																			.type(boolean.class)
																			.description("description")
																			.defaultValue(defaultValue)
																		.build()

		then: 
			container.findParameter(arguments)==value

		where:
			name	| defaultValue	| value			| prefixes
			"server"| null			| "other.com"	| null
			"port"	| null			| "2234"		| null
			"ip"	| "212.12.0.21"	| "212.12.0.21"	| ["-i","-ip"].toArray(new String[2])
			"name"	| "myname"		| "standalone"	| ["-n","-name"].toArray(new String[2])
			"path"	| "/etc/bin"	| "/home/user"	| ["-p","-path"].toArray(new String[2])
															
			arguments=["-server:other.com","-port:2234","-name:standalone","-p:/home/user"].toArray(new String[4])
	}

	@Unroll
	def "Search #name with customPrefixes #prefixes and without defaultValue into #arguments must throw MandatoryArgumentNotProvided exception"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Search $name with customPrefixes $prefixes and without defaultValue into $arguments must throw MandatoryArgumentNotProvided exception")
		when:
			def DefaultParameterContainer container=DefaultParameterContainer.builder()
																				.name(name)
																				.type(boolean.class)
																				.description("description")
																				.prefixes(prefixes)
																			.build()
			container.findParameter(arguments)

		then: 
			def e=thrown(MandatoryArgumentNotProvided) 
			e.getMessage()==new MandatoryArgumentNotProvided(container).getMessage()

		where:
			name		| prefixes
			"ip"		| null
			"path"		| ["-p","-path"].toArray(new String[2])
			"server"	| ["-s"].toArray(new String[1])
																				
			arguments=["-server:other.com","-port:2234","-name:standalone"].toArray(new String[3])
	}
																			
	@Unroll
	def "Load #name with type:#type,customPrefixes:#prefixes,defaultValue:#defaultValue and parser:#parser from #arguments must load #value"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Load $name with type:$type,customPrefixes:$prefixes,defaultValue:$defaultValue and parser:$parser from $arguments must load $value")
		when:
			DefaultParameterContainer container=DefaultParameterContainer.builder()
																			.name(name)
																			.prefixes(prefixes)
																			.description("description")
																			.type(type)
																			.defaultValue(defaultValue)
																			.parser(parser)
																		.build()
			container.loadParameter(arguments)
		then: 
			container.getValue().isPresent()
			container.getValue().get()==value

		where:
			name	| type				| defaultValue				| value									| prefixes								| parser
			"server"| String.class		| null						| "other.com"							| null									| null
			"port"	| int.class			| null						| 2234									| null									| null
			"ip"	| String.class		| "212.12.0.21"				| "212.12.0.21"							| ["-i","-ip"].toArray(new String[2])	| null
			"name"	| String.class		| "myname"					| "standalone"							| ["-n","-name"].toArray(new String[2])	| null
			"path"	| Path.class		| "/etc/bin"				| Paths.get("/home/user")				| ["-p","-path"].toArray(new String[2])	| null
			"url"	| boolean.class		| "yes"						| true									| ["-u","-url"].toArray(new String[2])	| {value -> ("yes".equalsIgnoreCase((String)value))? true : false }	
															
			arguments=["-server:other.com","-port:2234","-name:standalone","-p:/home/user"].toArray(new String[4])
	}

	@Unroll
	def "Try to load #name with type:#type from #arguments must raise exception"(){
		println(">>>>> DefaultParameterContainerSpec >>>> Try to load $name with type:$type from $arguments must raise exception")
		when:
			def container=DefaultParameterContainer.builder()
														.name(name)
														.type(type)
														.description("description")
													.build()
			container.loadParameter(arguments)
		then: 
			def e=thrown(UnparseableParameter.class)
			e.getMessage()==new UnparseableParameter(container,extractedValue,null).getMessage()

		where:
			name	| type						| extractedValue	
			"port"	| int.class					| "22.34"
			"ip"	| float.class				| "other.com"
			"path"	| GenericTextParser.class	| "/home/user"		
															
			arguments=["-ip:other.com","-port:22.34","-name:standalone","-path:/home/user"].toArray(new String[4])
	}
}

