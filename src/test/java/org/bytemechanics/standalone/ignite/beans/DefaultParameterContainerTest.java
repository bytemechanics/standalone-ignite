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
package org.bytemechanics.standalone.ignite.beans;

import org.bytemechanics.standalone.ignite.mocks.StandaloneAppTestParameter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Stream;
import org.bytemechanics.commons.tests.junit5.ArgumentsUtils;
import org.bytemechanics.standalone.ignite.exceptions.MandatoryParameterNotProvided;
import org.bytemechanics.standalone.ignite.exceptions.UnparseableParameter;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.internal.commons.reflection.PrimitiveTypeConverter;
import org.bytemechanics.standalone.ignite.internal.commons.string.GenericTextParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author afarre
 */
public class DefaultParameterContainerTest {

	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> DefaultParameterContainerTest >>>> setup");
		try (InputStream inputStream = LambdaUnchecker.class.getResourceAsStream("/logging.properties")) {
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (final IOException e) {
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}

	@BeforeEach
	void beforeEachTest(final TestInfo testInfo) {
		System.out.println(">>>>> " + this.getClass().getSimpleName() + " >>>> " + testInfo.getTestMethod().map(Method::getName).orElse("Unkown") + "" + testInfo.getTags().toString() + " >>>> " + testInfo.getDisplayName());
	}

	@Test
	@DisplayName("Builder must throw NullPointerException if no name is provided")
	@SuppressWarnings("ThrowableResultIgnored")
	public void builderMandatoryName(){
		Assertions.assertThrows(NullPointerException.class
										,() ->  DefaultParameterContainer.builder()
																				.type(boolean.class)
																				.description("")
																			.build()
										,"Mandatory \"name\" can not be null");
	}
	@Test
	@DisplayName("Constructor must throw NullPointerException if no name is provided")
	@SuppressWarnings("ThrowableResultIgnored")
	public void constructorMandatoryName(){
		Assertions.assertThrows(NullPointerException.class
										,() -> new DefaultParameterContainer(null,boolean.class,"",null,null,true,null)
										,"Mandatory \"name\" can not be null");
	}
	@Test
	@DisplayName("Builder must throw NullPointerException if no type is provided")
	@SuppressWarnings("ThrowableResultIgnored")
	public void builderMandatoryType(){
		Assertions.assertThrows(NullPointerException.class
										,() ->  DefaultParameterContainer.builder()
																				.name("name")
																				.description("")
																			.build()
										,"Mandatory \"type\" can not be null");
	}
	@Test
	@DisplayName("Constructor must throw NullPointerException if no type is provided")
	@SuppressWarnings("ThrowableResultIgnored")
	public void constructorMandatoryType(){
		Assertions.assertThrows(NullPointerException.class
										,() ->  new DefaultParameterContainer("name",null,"",null,null,true,null)
										,"Mandatory \"type\" can not be null");
	}
	@Test
	@DisplayName("Builder must throw NullPointerException if no description is provided")
	@SuppressWarnings("ThrowableResultIgnored")
	public void builderMandatoryDescription(){
		Assertions.assertThrows(NullPointerException.class
										,() ->  DefaultParameterContainer.builder()
																				.type(boolean.class)
															.name("name")
																				.build()
										,"Mandatory \"description\" can not be null");
	}
	@Test
	@DisplayName("Constructor must throw NullPointerException if no description is provided")
	public void constructorMandatoryDescription(){
		Assertions.assertThrows(NullPointerException.class
										,() -> new DefaultParameterContainer("name",boolean.class,null,null,null,true,null)
										,"Mandatory \"description\" can not be null");
	}

	@Test
	@DisplayName("Builder must throw NullPointerException if no description is provided")
	public void builderNameTypeMandatory(){
		DefaultParameterContainer container=DefaultParameterContainer.builder()
																															.type(boolean.class)
																				.name("name")
																				.description("description")
																									.build();
		Assertions.assertAll(() -> Assertions.assertTrue(container.isMandatory())
									,() -> Assertions.assertEquals("name",container.name())
									,() -> Assertions.assertEquals(Boolean.class,container.getType())
									,() -> Assertions.assertEquals("description",container.getDescription())
									,() -> Assertions.assertEquals("[-name]: description (Mandatory)",container.getHelp()));
	}
	@Test
	@DisplayName("Constructor with name and type must be created without problem being mandatory")
	public void constructorNameTypeMandatory(){
		DefaultParameterContainer container=new DefaultParameterContainer("name",boolean.class,"description",null,null,true,null);
		Assertions.assertAll(() -> Assertions.assertTrue(container.isMandatory())
									,() -> Assertions.assertEquals("name",container.name())
									,() -> Assertions.assertEquals(Boolean.class,container.getType())
									,() -> Assertions.assertEquals("description",container.getDescription())
									,() -> Assertions.assertEquals("[-name]: description (Mandatory)",container.getHelp()));
	}

	@SuppressWarnings("static-access")
	static Stream<Arguments> parserDatapack(){
		return Stream.of(	Arguments.of("true-false",boolean.class,"true",true)
								,Arguments.of("false-false",boolean.class,"false",false)
								,Arguments.of("true-true",boolean.class,"true",true)
								,Arguments.of("true-false",Boolean.class,"false",Boolean.FALSE)
								,Arguments.of("true-TRUE",Boolean.class,"TRUE",Boolean.TRUE)		
								,Arguments.of("true-FALSE",Boolean.class,"FALSE",Boolean.FALSE)			
								,Arguments.of("char-a",char.class,"a",(char)'a')
								,Arguments.of("char-b",char.class,"b",(char)'b')
								,Arguments.of("char-c",Character.class,"c",Character.valueOf((char)'c'))	
								,Arguments.of("char-e",char.class,"e",(char)'e')
								,Arguments.of("char-d",char.class,"d",(char)'d')
								,Arguments.of("char-3",char.class,"3",(char)'3')
								,Arguments.of("char-@",Character.class,"@",Character.valueOf((char)'@'))
								,Arguments.of("char-&",Character.class,"&",Character.valueOf((char)'&'))	
								,Arguments.of("char-/",Character.class,"/",Character.valueOf((char)'/'))	
								,Arguments.of("char-(",char.class,"(",(char)'(')
								,Arguments.of("char-(df",char.class	,"(df",(char)'(')
								,Arguments.of("short-min",short.class,String.valueOf(Short.MIN_VALUE),(short)Short.MIN_VALUE)		
								,Arguments.of("short-100",short.class,"-100",(short)-100)
								,Arguments.of("short-0"	,short.class,"0",(short)0)
								,Arguments.of("short-24125",Short.class,"24125",Short.valueOf("24125"))						
								,Arguments.of("short-max",Short.class,String.valueOf(Short.MAX_VALUE),Short.MAX_VALUE)
								,Arguments.of("int-min",int.class,String.valueOf(Integer.MIN_VALUE),(int)Integer.MIN_VALUE)	
								,Arguments.of("int-100",int.class,"-100",-100)
								,Arguments.of("int-0",int.class,"0"	,Integer.valueOf("0"))
								,Arguments.of("int-24125",Integer.class,"24125",Integer.valueOf("24125"))						
								,Arguments.of("int-max",Integer.class,String.valueOf(Integer.MAX_VALUE),Integer.MAX_VALUE)
								,Arguments.of("long-min",long.class,String.valueOf(Long.MIN_VALUE),(long)Long.MIN_VALUE)
								,Arguments.of("long-100",long.class,"-100",-100l)
								,Arguments.of("long-0",long.class,"0",0l)
								,Arguments.of("long-24125",Long.class,"24125",Long.valueOf(24125l))					
								,Arguments.of("long-max",Long.class,String.valueOf(Long.MAX_VALUE),Long.MAX_VALUE)
								,Arguments.of("float-min",float.class,String.valueOf(Float.MIN_VALUE),(float)Float.MIN_VALUE)
								,Arguments.of("float-100.23",float.class,"-100.23",-100.23f)
								,Arguments.of("float-0.0",float.class,"-100.23",-100.23f)
								,Arguments.of("float-24125.32435",float.class,"24125.32435",24125.32435f)
								,Arguments.of("float-max",float.class,String.valueOf(Float.MAX_VALUE),(float)Float.MAX_VALUE)		
								,Arguments.of("double-min",double.class,String.valueOf(Double.MIN_VALUE),(double)Double.MIN_VALUE)	
								,Arguments.of("double-100.23",double.class,"-100.32532",-100.32532d)
								,Arguments.of("double-0.0",double.class,"0.0",0.0d)
								,Arguments.of("double-24125.32435",double.class,"24125.242",24125.242d)	
								,Arguments.of("double-max",double.class,String.valueOf(Double.MAX_VALUE),(double)Double.MAX_VALUE)	
								,Arguments.of("BigDecimal--231,412,432.432423",BigDecimal.class,"-231,412,432.432423",BigDecimal.valueOf(-231412432.432423d))					
								,Arguments.of("BigDecimal--100.32435",BigDecimal.class,"-100.32435",BigDecimal.valueOf(-100.32435d))
								,Arguments.of("BigDecimal-0.0",BigDecimal.class,"0.0"	,BigDecimal.valueOf(0.0d))
								,Arguments.of("BigDecimal-24,125.3211"	,BigDecimal.class,"24,125.3211",BigDecimal.valueOf(24125.3211d))
								,Arguments.of("BigDecimal-231,412,432.432423",BigDecimal.class,"231,412,432.432423",BigDecimal.valueOf(231412432.432423d))	
								,Arguments.of("String",String.class,"myMessage","myMessage")
								,Arguments.of("LocalTime-10:15",LocalTime.class	,"10:15",LocalTime.parse("10:15"))
								,Arguments.of("LocalTime-10:15:30",LocalTime.class,"10:15:30",LocalTime.parse("10:15:30")	)
								,Arguments.of("LocalTime-10:15:30.123",LocalTime.class,"10:15:30.123",LocalTime.parse("10:15:30.123"))
								,Arguments.of("LocalTime-2007-12-03",LocalDate.class,"2007-12-03"		,LocalDate.parse("2007-12-03"))
								,Arguments.of("LocalDAte-2007-12-03T10:15",LocalDateTime.class,"2007-12-03T10:15",LocalDateTime.parse("2007-12-03T10:15"))	
								,Arguments.of("LocalDAte-2007-12-03T10:15:30",LocalDateTime.class,"2007-12-03T10:15:30",LocalDateTime.parse("2007-12-03T10:15:30"))	
								,Arguments.of("enum-genericTextParser-ENUM",GenericTextParser.class,"ENUM",GenericTextParser.ENUM)
								,Arguments.of("enum-genericTextParser-LOCALDATE",GenericTextParser.class,"LOCALDATE",GenericTextParser.LOCALDATE)
							);
	}
	
	@ParameterizedTest(name = "When new instance created with name:{0},type:{1} and defaultValue:{2} then the value stored should be the default {3} one")
	@MethodSource("parserDatapack")
	@SuppressWarnings("unchecked")
	public void valueStored(final String _name,final Class _type,final String _defaultValue,final Object _parsedValue){
		DefaultParameterContainer container=new DefaultParameterContainer(_name,_type,"description",null,null,true,_defaultValue);
		
		Assertions.assertAll(() -> Assertions.assertFalse(container.isMandatory())
									,() -> Assertions.assertEquals(_name,container.name())
									,() -> Assertions.assertEquals(PrimitiveTypeConverter.convert(_type),container.getType())
									,() -> Assertions.assertEquals("description",container.getDescription())
									,() -> Assertions.assertNotNull(container.getParser())
									,() -> Assertions.assertTrue(container.getDefaultValue().isPresent())
									,() -> Assertions.assertEquals(_defaultValue,container.getDefaultValue().get())
									,() -> Assertions.assertTrue(container.getValue().isPresent())
									,() -> Assertions.assertEquals(_parsedValue,container.getValue().get()));
	}

	@ParameterizedTest(name = "When new instance created with name:{0},type:{1} and no defaultValue then when retrieve value should return null")
	@MethodSource("parserDatapack")
	@SuppressWarnings("unchecked")
	public void valueStoredNoDefault(final String _name,final Class _type){
		DefaultParameterContainer container=new DefaultParameterContainer(_name,_type,"description",null,null,true,null);
		
		Assertions.assertAll(() -> Assertions.assertTrue(container.isMandatory())
									,() -> Assertions.assertEquals(_name,container.name())
									,() -> Assertions.assertEquals(PrimitiveTypeConverter.convert(_type),container.getType())
									,() -> Assertions.assertEquals("description",container.getDescription())
									,() -> Assertions.assertNotNull(container.getParser())
									,() -> Assertions.assertFalse(container.getDefaultValue().isPresent())
									,() -> Assertions.assertThrows(NoSuchElementException.class,() -> container.getDefaultValue().get())
									,() -> Assertions.assertFalse(container.getValue().isPresent())
									,() -> Assertions.assertThrows(NoSuchElementException.class,() -> container.getValue().get()));
	}

	@SuppressWarnings("static-access")
	static Stream<Arguments> customParserDatapack(){
		return Stream.of(	Arguments.of("yes",true,(Function<String,Object>)(value -> ("yes".equals(value))))
								,Arguments.of("No",false,(Function<String,Object>)(value -> ("yes".equals(value))))
								,Arguments.of("YES",false,(Function<String,Object>)(value -> ("yes".equals(value))))
								,Arguments.of("Yes",true,(Function<String,Object>)(value -> ("yes".equalsIgnoreCase((String)value))))
								,Arguments.of("yES",true,(Function<String,Object>)(value -> ("yes".equalsIgnoreCase((String)value))))
								,Arguments.of("other",false,(Function<String,Object>)(value -> ("yes".equalsIgnoreCase((String)value))))
							);
	}

	@ParameterizedTest(name = "When custom parser:{2} is provided must be used to parse the defaultValue:{0} provided and resolved as {1}")
	@MethodSource("customParserDatapack")
	public void customParser(final String _defaultValue,final Object _value,final Function<String,Object> _parser){
		
		DefaultParameterContainer container=DefaultParameterContainer.builder()
																				.name(_defaultValue)
																				.type(boolean.class)
																				.description("description")
																				.parser(_parser)
																				.defaultValue(_defaultValue)
																			.build();
		
		Assertions.assertAll(() -> Assertions.assertFalse(container.isMandatory())
									,() -> Assertions.assertEquals(_defaultValue,container.name())
									,() -> Assertions.assertEquals(Boolean.class,container.getType())
									,() -> Assertions.assertEquals("description",container.getDescription())
									,() -> Assertions.assertNotNull(container.getParser())
									,() -> Assertions.assertTrue(container.getDefaultValue().isPresent())
									,() -> Assertions.assertEquals(_defaultValue,container.getDefaultValue().get())
									,() -> Assertions.assertTrue(container.getValue().isPresent())
									,() -> Assertions.assertEquals(_value,container.getValue().get()));
	}

	@SuppressWarnings("static-access")
	static Stream<Arguments> searchDatapack(){
		return Stream.of(	Arguments.of("server",null,"other.com","description1","Mandatory" )
								,Arguments.of("port",null,"2234"	,"description2","Mandatory" )
								,Arguments.of("ip","212.12.0.21","212.12.0.21","description3","Default: 212.12.0.21" )
								,Arguments.of("name","myname","standalone","description4","Default: myname" )
								,Arguments.of("path","/etc/bin","/etc/bin","description5","Default: /etc/bin" )
							)
						.map(args -> ArgumentsUtils.aggregate(args,new String[]{"-server:other.com","-port:2234","-name:standalone"}));	
	}
	
	@ParameterizedTest(name = "Search {0},{3} with {1} default into {5} must return {2}")
	@MethodSource("searchDatapack")
	public void search(final String _name,final String _defaultValue,final String _value,final String _description,final String _mandatoryHelp,final String[] _arguments){
		
		DefaultParameterContainer container=DefaultParameterContainer.builder()
																								.name(_name)
																								.type(String.class)
																								.description(_description)
																								.defaultValue(_defaultValue)
																							.build();
		
		Assertions.assertAll(() -> Assertions.assertEquals(_value,container.findParameter(_arguments))
									,() -> Assertions.assertEquals(_description,container.getDescription())
									,() -> Assertions.assertEquals("[-"+_name+"]: "+_description+" ("+_mandatoryHelp+")",container.getHelp()));
	}

	@SuppressWarnings("static-access")
	static Stream<Arguments> searchPrefixesDatapack(){
		return Stream.of(	Arguments.of("server",null	,"other.com",null)
								,Arguments.of("port",null,"2234",null)
								,Arguments.of("ip","212.12.0.21"	,"212.12.0.21",new String[]{"-i","-ip"})
								,Arguments.of("name","myname"	,"standalone",new String[]{"-n","-name"})
								,Arguments.of("path"	,"/etc/bin"	,"/home/user",new String[]{"-p","-path"})
							)
						.map(args -> ArgumentsUtils.aggregate(args,new String[]{"-server:other.com","-port:2234","-name:standalone","-p:/home/user"}));	
	}
	
	@ParameterizedTest(name = "Search {0} with customPrefixes {3} and {1} default into {4} must return {2}")
	@MethodSource("searchPrefixesDatapack")
	public void searchPrefixes(final String _name,final String _defaultValue,final String _value,final String[] _prefixes,final String[] _arguments){
		
		DefaultParameterContainer container=DefaultParameterContainer.builder()
																								.name(_name)
															.prefixes(_prefixes)
																								.type(String.class)
																								.description("description")
																								.defaultValue(_defaultValue)
																							.build();
		Assertions.assertEquals(_value,container.findParameter(_arguments));
	}
	@ParameterizedTest(name = "Search {0} with customPrefixes {3} and without defaultValue into {4} must throw MandatoryParameterNotProvided exception")
	@MethodSource("searchPrefixesDatapack")
	@SuppressWarnings("ThrowableResultIgnored")
	public void searchPrefixesWithoutDefault(final String _name,final String _defaultValue,final String _value,final String[] _prefixes,final String[] _arguments){
		
		DefaultParameterContainer container=DefaultParameterContainer.builder()
																								.name(_name)
															.prefixes(_prefixes)
																								.type(String.class)
																								.description("description")
																							.build();

		Assertions.assertThrows(MandatoryParameterNotProvided.class
										,() -> container.findParameter(new String[0])
										,new MandatoryParameterNotProvided(container).getMessage());
	}
	
	@SuppressWarnings("static-access")
	static Stream<Arguments> parseEnumDatapack(){
		return Stream.of(	Arguments.of(StandaloneAppTestParameter.class,"DOUBLEVALUE",true,StandaloneAppTestParameter.DOUBLEVALUE)
								,Arguments.of(StandaloneAppTestParameter.class,"DoubleVALUE",false,StandaloneAppTestParameter.DOUBLEVALUE)
							)
						.map(args -> ArgumentsUtils.aggregate(args,new String[]{"-server:other.com","-port:2234","-name:standalone","-p:/home/user","-enum-class:"+args.get()[1]}));	
	}
	
	
	@ParameterizedTest(name = "Parse enum {0} with value {1} and case sensitive {2} should result {3}")
	@MethodSource("parseEnumDatapack")
	@SuppressWarnings("unchecked")
	public void parseEnum(final Class _enumClass,final String _value,final boolean _caseSensitive,final StandaloneAppTestParameter _result,final String[] _arguments){
		
		DefaultParameterContainer container=DefaultParameterContainer.builder()
																			.name("enum-class")
																			.description("description")
																			.type(_enumClass)
																			.caseSensitive(_caseSensitive)
																		.build();
		container.loadParameter(_arguments);

		Assertions.assertAll(() -> Assertions.assertTrue(container.getValue().isPresent())
									,() -> Assertions.assertEquals(_result,container.getValue().get()));
	}

	@SuppressWarnings("static-access")
	static Stream<Arguments> parseFailEnumDatapack(){
		return Stream.of(	Arguments.of(StandaloneAppTestParameter.class,"doublevalue",true,"Unparseable parameter enum-class with value doublevalue")
								,Arguments.of(StandaloneAppTestParameter.class,"DoubleVALUE",true,"Unparseable parameter enum-class with value DoubleVALUE")
								,Arguments.of(StandaloneAppTestParameter.class,"doublevalues",false,"Unparseable parameter enum-class. Unable to parse value doublevalues valid values are [BOOLEANVALUE, INTVALUE, LONGVALUE, FLOATVALUE, DOUBLEVALUE, STRINGVALUE, ENUMVALUE]")
								,Arguments.of(StandaloneAppTestParameter.class,"DoubleVALUEs",false,"Unparseable parameter enum-class. Unable to parse value DoubleVALUEs valid values are [BOOLEANVALUE, INTVALUE, LONGVALUE, FLOATVALUE, DOUBLEVALUE, STRINGVALUE, ENUMVALUE]")
							)
						.map(args -> ArgumentsUtils.aggregate(args,new String[]{"-server:other.com","-port:2234","-name:standalone","-p:/home/user","-enum-class:"+args.get()[1]}));	
	}

	@ParameterizedTest(name = "Parse enum {0} with value {1} and case sensitive {2} should result {3}")
	@MethodSource("parseFailEnumDatapack")
	@SuppressWarnings({"ThrowableResultIgnored","unchecked"})
	public void parseFailEnum(final Class _enumClass,final String _value,final boolean _caseSensitive,final String _message,final String[] _arguments){
		
		DefaultParameterContainer container=DefaultParameterContainer.builder()
																			.name("enum-class")
																			.description("description")
																			.type(_enumClass)
																			.caseSensitive(_caseSensitive)
																		.build();

		Assertions.assertThrows(UnparseableParameter.class
										,() -> container.loadParameter(_arguments)
										,_message);
	}
	
	@SuppressWarnings("static-access")
	static Stream<Arguments> parseOtherValuesDatapack(){
		return Stream.of(	Arguments.of("server",String.class	,null,"other.com",null,null)
								,Arguments.of("port",int.class,null,2234,null,null)
								,Arguments.of("ip",String.class,"212.12.0.21","212.12.0.21",new String[]{"-i","-ip"},null)
								,Arguments.of("name",String.class,"myname","standalone",new String[]{"-n","-name"},null)
								,Arguments.of("path",Path.class,"/etc/bin",Paths.get("/home/user"),new String[]{"-p","-path"},null)
								,Arguments.of("url",boolean.class,"yes",true,new String[]{"-u","-url"},(Function<String,Object>)(value -> ("yes".equalsIgnoreCase((String)value)) ))
							)
						.map(args -> ArgumentsUtils.aggregate(args,new String[]{"-server:other.com","-port:2234","-name:standalone","-p:/home/user"}));	
	}
	
	
	@ParameterizedTest(name = "Load {0} with type:{1},customPrefixes:{4},defaultValue:{2} and parser:{5} from #arguments must load {3}")
	@MethodSource("parseOtherValuesDatapack")
	@SuppressWarnings("unchecked")
	public void parseOtherValues(final String _name,final Class _type,final String _defaultValue,final Object _value, final String[] _prefixes, final Function<String,Object> _parser,final String[] _arguments){
		
		DefaultParameterContainer container=DefaultParameterContainer.builder()
																				.name(_name)
																				.prefixes(_prefixes)
																				.description("description")
																				.type(_type)
																				.defaultValue(_defaultValue)
																				.parser(_parser)
																		.build();
		container.loadParameter(_arguments);

		Assertions.assertAll(() -> Assertions.assertTrue(container.getValue().isPresent())
									,() -> Assertions.assertEquals(_value,container.getValue().get()));
	}

	@SuppressWarnings("static-access")
	static Stream<Arguments> parseOtherValuesFailDatapack(){
		return Stream.of(	Arguments.of("port",int.class,"22.34")
								,Arguments.of("ip",float.class,"other.com")
								,Arguments.of("path",double.class,"/home/user")
							)
						.map(args -> ArgumentsUtils.aggregate(args,new String[]{"-ip:other.com","-port:22.34","-name:standalone","-path:/home/user"}));	
	}
	
	
	@ParameterizedTest(name = "Try to load {0} with type:{1} from {3} must raise exception")
	@MethodSource("parseOtherValuesFailDatapack")
	@SuppressWarnings({"ThrowableResultIgnored","unchecked"})
	public void parseOtherValuesFail(final String _name,final Class _type,final String extractedValue,final String[] _arguments){
		
		DefaultParameterContainer container=DefaultParameterContainer.builder()
																				.name(_name)
																				.type(_type)
																				.description("description")
																			.build();

		Assertions.assertThrows(UnparseableParameter.class
										,() -> container.loadParameter(_arguments)
										,new UnparseableParameter(container,extractedValue,null).getMessage());
	}
}
