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
package org.bytemechanics.standalone.ignite;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;
import org.bytemechanics.standalone.ignite.exceptions.InvalidParameter;
import org.bytemechanics.standalone.ignite.exceptions.MandatoryIgnitableSupplier;
import org.bytemechanics.standalone.ignite.exceptions.MandatoryParameterNotProvided;
import org.bytemechanics.standalone.ignite.exceptions.ParameterException;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.internal.commons.string.Figlet;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;
import org.bytemechanics.standalone.ignite.mocks.MockedIgnitableAdapter;
import org.bytemechanics.standalone.ignite.mocks.StandaloneAppTestParameter;
import org.bytemechanics.standalone.ignite.mocks.StandaloneAppTestParameter2;
import org.bytemechanics.standalone.ignite.mocks.StandaloneAppTestParameter3;
import org.bytemechanics.standalone.ignite.mocks.StandaloneAppTestRegression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author afarre
 */
public class StandaloneTest {

	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> StandaloneTest >>>> setup");
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
	@DisplayName("When supplier is not provided a NullPointerException is raised")
	@SuppressWarnings("ThrowableResultIgnored")
	public void supplierNotProvided() {

		Assertions.assertThrows(MandatoryIgnitableSupplier.class
										,() -> Standalone.builder(null).build().ignite());
	}
	
	@Test
	@DisplayName("When shutdown is called, ignitable startup (before and after) and ignitable shutdown (before and after) must be called")
	public void igniteStartup(final @Mocked Ignitable _ignitable) {

		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1;
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1;
			_ignitable.shutdown(); times=1;
			_ignitable.afterShutdown(); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite()
						.shutdown();
	}	

	@Test
	@DisplayName("On Autocloseable Ignitables must call close after shutdown")
	public <AutoCloseableIgnitable extends AutoCloseable & Ignitable> void igniteStartupAutoCloseable(final @Mocked AutoCloseableIgnitable _ignitable) throws Exception {

		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1;
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1;
			_ignitable.shutdown(); times=1;
			_ignitable.afterShutdown(); times=1;
			_ignitable.close(); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite()
						.shutdown();
	}	

	@Test
	@DisplayName("ParseParameters must do nothing if no parameters defined")
	public void igniteNoParameters(final @Mocked Ignitable _ignitable) {

		Standalone standalonePreParameters=Standalone.builder(() -> _ignitable)
														.build();
		Standalone standalonePostParameters=standalonePreParameters.parseParameters();
		Assertions.assertEquals(standalonePreParameters,standalonePostParameters);
	}
	
	@ParameterizedTest(name = "ParseParameters {0} for StandaloneAppTestParameter.class must parse correctly the correct values")
	@ValueSource(strings = {"-booleanvalue:true,-intvalue:2234,-longvalue:3243321312,-floatvalue:3123.32,-doublevalue:3123.32,-stringvalue:TEST,-enumvalue:ENUMVALUE,-additionalbooleanvalue:false,-additionalintvalue:2234,-additionallongvalue:3243321312,-additionalfloatvalue:3123.32,-additionaldoublevalue:3123.32,-additionalstringvalue:TEST,-additionalenumvalue:ADDITIONALENUMVALUE"})
	@SuppressWarnings("UnnecessaryUnboxing")
	public void parseParameters(final String _args,final @Mocked Ignitable _ignitable){
		
		//Prepare
		final String[] arguments=_args.split(",");
		
		//Execute
		Standalone.builder(() -> _ignitable)
							.parameters(StandaloneAppTestParameter.class)
							.parameters(StandaloneAppTestParameter2.class)
							.arguments(arguments)
						.build()
							.ignite();
		
		//Verify
		Assertions.assertEquals(true, StandaloneAppTestParameter.BOOLEANVALUE.getValue(boolean.class).isPresent());
		Assertions.assertEquals(true, StandaloneAppTestParameter.BOOLEANVALUE.getValue(boolean.class).get().booleanValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter.INTVALUE.getValue(int.class).isPresent());
		Assertions.assertEquals(2234, StandaloneAppTestParameter.INTVALUE.getValue(int.class).get().intValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter.LONGVALUE.getValue(long.class).isPresent());
		Assertions.assertEquals(3243321312l, StandaloneAppTestParameter.LONGVALUE.getValue(long.class).get().longValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter.FLOATVALUE.getValue(float.class).isPresent());
		Assertions.assertEquals(3123.32f, StandaloneAppTestParameter.FLOATVALUE.getValue(float.class).get().floatValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter.DOUBLEVALUE.getValue(double.class).isPresent());
		Assertions.assertEquals(3123.32d, StandaloneAppTestParameter.DOUBLEVALUE.getValue(double.class).get().doubleValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter.STRINGVALUE.getValue(String.class).isPresent());
		Assertions.assertEquals("TEST", StandaloneAppTestParameter.STRINGVALUE.getValue(String.class).get());
		Assertions.assertEquals(true, StandaloneAppTestParameter.ENUMVALUE.getValue(StandaloneAppTestParameter.class).isPresent());
		Assertions.assertEquals(StandaloneAppTestParameter.ENUMVALUE, StandaloneAppTestParameter.ENUMVALUE.getValue(StandaloneAppTestParameter.class).get());

		Assertions.assertEquals(true, StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE.getValue(boolean.class).isPresent());
		Assertions.assertEquals(false, StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE.getValue(boolean.class).get().booleanValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter2.ADDITIONALINTVALUE.getValue(int.class).isPresent());
		Assertions.assertEquals(2234, StandaloneAppTestParameter2.ADDITIONALINTVALUE.getValue(int.class).get().intValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter2.ADDITIONALLONGVALUE.getValue(long.class).isPresent());
		Assertions.assertEquals(3243321312l, StandaloneAppTestParameter2.ADDITIONALLONGVALUE.getValue(long.class).get().longValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter2.ADDITIONALFLOATVALUE.getValue(float.class).isPresent());
		Assertions.assertEquals(3123.32f, StandaloneAppTestParameter2.ADDITIONALFLOATVALUE.getValue(float.class).get().floatValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter2.ADDITIONALDOUBLEVALUE.getValue(double.class).isPresent());
		Assertions.assertEquals(3123.32d, StandaloneAppTestParameter2.ADDITIONALDOUBLEVALUE.getValue(double.class).get().doubleValue());
		Assertions.assertEquals(true, StandaloneAppTestParameter2.ADDITIONALSTRINGVALUE.getValue(String.class).isPresent());
		Assertions.assertEquals("TEST", StandaloneAppTestParameter2.ADDITIONALSTRINGVALUE.getValue(String.class).get());
		Assertions.assertEquals(true, StandaloneAppTestParameter2.ADDITIONALENUMVALUE.getValue(StandaloneAppTestParameter.class).isPresent());
		Assertions.assertEquals(StandaloneAppTestParameter2.ADDITIONALENUMVALUE, StandaloneAppTestParameter2.ADDITIONALENUMVALUE.getValue(StandaloneAppTestParameter2.class).get());
	}

	
	@ParameterizedTest(name = "ParseParameters {0} for StandaloneAppTestParameter3.class must parse correctly parameters with spaces")
	@ValueSource(strings = {"-stringvalue:\"this is my parameter with spaces\",other,again,-stringvalue2:\"*- this is my parameter with spaces2\"","other,again,-stringvalue2:\"*- this is my parameter with spaces2\",-stringvalue:\"this is my parameter with spaces\""})
	@SuppressWarnings("UnnecessaryUnboxing")
	public void parseParametersWithSpaces(final String _args,final @Mocked Ignitable _ignitable){
		
		//Prepare
		final String[] arguments=_args.split(",");
		
		//Execute
		Standalone.builder(() -> _ignitable)
							.parameters(StandaloneAppTestParameter3.class)
							.arguments(arguments)
						.build()
							.ignite();
		
		//Verify
		Assertions.assertEquals("this is my parameter with spaces", StandaloneAppTestParameter3.STRINGVALUE.get(String.class));
		Assertions.assertEquals("*- this is my parameter with spaces2", StandaloneAppTestParameter3.STRINGVALUE2.get(String.class));
	}
	
	@ParameterizedTest(name = "ParseParameters {0} for StandaloneAppTestRegression.class must parse correctly parameters with spaces")
	@ValueSource(strings = {"-path:\"c:\\tmp a\\\" -exclude:\"*- copia.txt\" -verbose:true","-path:\"c:\\tmp a\\\" -patterns:*.txt,*.bat -exclude:\"*- copia.txt\" -verbose:true"})
	@SuppressWarnings("UnnecessaryUnboxing")
	public void parseParametersWithSpacesRegression(final String _args,final @Mocked Ignitable _ignitable){
		
		//Prepare
		final String[] arguments=_args.split(" ");
		
		//Execute
		Standalone.builder(() -> _ignitable)
							.parameters(StandaloneAppTestRegression.class)
							.arguments(arguments)
						.build()
							.ignite();
		
		//Verify
		Assertions.assertEquals(Paths.get("c:\\tmp a\\"), StandaloneAppTestRegression.PATH.get(Path.class));
		Assertions.assertEquals("*.txt,*.bat", StandaloneAppTestRegression.PATTERNS.get(String.class));
		Assertions.assertEquals("*- copia.txt", StandaloneAppTestRegression.EXCLUDE.get(String.class));
		Assertions.assertEquals(true, StandaloneAppTestRegression.VERBOSE.get(boolean.class));
	}
	
	@ParameterizedTest(name = "ParseParameters {0} for StandaloneAppTestParameter.class must parse correctly the correct values")
	@ValueSource(strings = {"-booleanvalue:true,-intvalue:2234,-longvalue:3243321312,-floatvalue:3123.32,-doublevalue:3123.32,-stringvalue:semanticFailure,-enumvalue:ENUMVALUE"})
	@SuppressWarnings({"ThrowableResultIgnored","unchecked"})
	public <T extends ParameterException> void validateParameters(final String _args,final @Mocked Ignitable _ignitable){
		
		//Prepare
		final String[] arguments=_args.split(",");
		new Expectations() {{
			_ignitable.parameterProcessingException((T)any); 
				result=new Delegate<T>() {
							public void delegate(T _exception) throws Exception {
								throw _exception;
							}
						};
				times=1;
		}};
		
		//Execute
		Standalone standalone=Standalone.builder(() -> _ignitable)
											.parameters(StandaloneAppTestParameter.class)
											.arguments(arguments)
										.build();
			
		Assertions.assertThrows(InvalidParameter.class
										,() -> standalone.ignite()
										,"Invalid parameter STRINGVALUE with value semanticFailure: semantic test error requested");
	}

	@Test
	@DisplayName("When exception happens on beforeStartup then startupException must be called")
	public void igniteBeforeStartupException(final @Mocked Ignitable _ignitable) {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1; result=expectedException;
			_ignitable.startup(); times=0;
			_ignitable.afterStartup(); times=0;
			_ignitable.startupException(expectedException); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();
	}
	@Test
	@DisplayName("On Autocloseable Ignitables must call close after startupException on beforeStartup Errors")
	public <AutoCloseableIgnitable extends AutoCloseable & Ignitable> void autocloseableIgniteBeforeStartupException(final @Mocked AutoCloseableIgnitable _ignitable) throws Exception {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1; result=expectedException;
			_ignitable.startup(); times=0;
			_ignitable.afterStartup(); times=0;
			_ignitable.startupException(expectedException); times=1;
			_ignitable.close(); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();
	}	
	@Test
	@DisplayName("When exception happens on startup then startupException must be called")
	public void igniteStartupException(final @Mocked Ignitable _ignitable) {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; result=expectedException;
			_ignitable.afterStartup(); times=0;
			_ignitable.startupException(expectedException); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();
	}
	@Test
	@DisplayName("On Autocloseable Ignitables must call close after startupException on startup Errors")
	public <AutoCloseableIgnitable extends AutoCloseable & Ignitable> void autocloseableIgniteStartupException(final @Mocked AutoCloseableIgnitable _ignitable) throws Exception {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1; 
			_ignitable.startup(); times=1; result=expectedException; 
			_ignitable.afterStartup(); times=0;
			_ignitable.startupException(expectedException); times=1;
			_ignitable.close(); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();
	}	
	
	@Test
	@DisplayName("On IgnitableAdapter must assign the current standalone instance")
	public void ignitableAdapterStandaloneInitialization(final @Mocked MockedIgnitableAdapter _ignitable) {

		new Expectations() {{
			_ignitable.setStandalone((Standalone)any); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();
	}	

	@Test
	@DisplayName("When exception happens on afterStartup then startupException must be called")
	public void igniteAfterStartupException(final @Mocked Ignitable _ignitable) {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.afterStartup(); times=1; result=expectedException;
			_ignitable.startupException(expectedException); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();
	}
	@Test
	@DisplayName("On Autocloseable Ignitables must call close after startupException on afterStartup Errors")
	public <AutoCloseableIgnitable extends AutoCloseable & Ignitable> void autocloseableIgniteAfterStartupException(final @Mocked AutoCloseableIgnitable _ignitable) throws Exception {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1; 
			_ignitable.startup(); times=1; 
			_ignitable.afterStartup(); times=1; result=expectedException; 
			_ignitable.startupException(expectedException); times=1;
			_ignitable.close(); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();
	}	
	
	@Test
	@DisplayName("When exception happens on beforeShutdown then shutdownException must be called")
	public void igniteBeforeShutdownException(final @Mocked Ignitable _ignitable) {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1; result=expectedException;
			_ignitable.shutdown(); times=0;
			_ignitable.afterShutdown(); times=0;
			_ignitable.shutdownException(expectedException); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite()
						.shutdown();
	}	
	@Test
	@DisplayName("On Autocloseable Ignitables must call close after shutdownException on beforeShutdown Errors")
	public <AutoCloseableIgnitable extends AutoCloseable & Ignitable> void autocloseableIgniteBeforeShutdownException(final @Mocked AutoCloseableIgnitable _ignitable) throws Exception {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1; result=expectedException;
			_ignitable.shutdown(); times=0;
			_ignitable.afterShutdown(); times=0;
			_ignitable.shutdownException(expectedException); times=1;
			_ignitable.close(); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite()
						.shutdown();
	}	

	@Test
	@DisplayName("When exception happens on shutdown then shutdownException must be called")
	public void igniteShutdownException(final @Mocked Ignitable _ignitable) {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1;
			_ignitable.shutdown(); times=1; result=expectedException;
			_ignitable.afterShutdown(); times=0;
			_ignitable.shutdownException(expectedException); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite()
						.shutdown();
	}	
	@Test
	@DisplayName("On Autocloseable Ignitables must call close after shutdownException on shutdown Errors")
	public <AutoCloseableIgnitable extends AutoCloseable & Ignitable> void autocloseableIgniteShutdownException(final @Mocked AutoCloseableIgnitable _ignitable) throws Exception {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1;
			_ignitable.shutdown(); times=1; result=expectedException;
			_ignitable.afterShutdown(); times=0;
			_ignitable.shutdownException(expectedException); times=1;
			_ignitable.close(); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite()
						.shutdown();
	}	

	@Test
	@DisplayName("When exception happens on afterShutdown then shutdownException must be called")
	public void igniteAfterShutdownException(final @Mocked Ignitable _ignitable) {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1; 
			_ignitable.shutdown(); times=1; 
			_ignitable.afterShutdown(); times=1; result=expectedException;
			_ignitable.shutdownException(expectedException); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite()
						.shutdown();
	}
	@Test
	@DisplayName("On Autocloseable Ignitables must call close after shutdownException on afterShutdown Errors")
	public <AutoCloseableIgnitable extends AutoCloseable & Ignitable> void autocloseableIgniteAfterShutdownException(final @Mocked AutoCloseableIgnitable _ignitable) throws Exception {

		Exception expectedException=new RuntimeException("ouch");
		
		new Expectations() {{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1;
			_ignitable.shutdown(); times=1;
			_ignitable.afterShutdown(); times=1; result=expectedException;
			_ignitable.shutdownException(expectedException); times=1;
			_ignitable.close(); times=1;
		}};
		
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite()
						.shutdown();
	}	
	
	@SuppressWarnings("static-access")
	static Stream<Arguments> bannerDatapack(){
		return Stream.of(	Arguments.of("testerot-null",null,null)
								,Arguments.of("testerot2-null",null,null)
								,Arguments.of("testerot-standard","standard.flf",Standalone.class.getClassLoader().getSystemResource("standard.flf"))
								,Arguments.of("testerot2-standard","standard.flf",Standalone.class.getClassLoader().getSystemResource("standard.flf"))
								,Arguments.of("testerot-universe","basic_1.flf",ParameterTest.class.getClassLoader().getSystemResource("basic_1.flf"))
								,Arguments.of("testerot2-universe","basic_1.flf",ParameterTest.class.getClassLoader().getSystemResource("basic_1.flf"))
							);
	}
	
	@ParameterizedTest(name = "Ignite with name should print a banner with the given {0} name and {1} font")
	@MethodSource("bannerDatapack")
	@SuppressWarnings({"UnnecessaryUnboxing","static-access","unchecked"})
	public void banner(final String _name,final String _font,final URL _fontURL,final @Mocked Ignitable _ignitable) throws IOException{

		Queue console=new LinkedList();
		List<String> expectedLines=new ArrayList<>();
		URL effectiveFontURL=(_fontURL==null)? Standalone.class.getClassLoader().getSystemResource("standard.flf") : _fontURL;
		try(InputStream effectiveFontURLStream=effectiveFontURL.openStream()){
			final Figlet figlet=new Figlet(effectiveFontURLStream,Charset.forName("UTF-8"));
			expectedLines.add(figlet.line(_name,(char)'='));
			expectedLines.add(figlet.print(_name));
			expectedLines.add(figlet.line(_name,(char)'-'));
			expectedLines.add(SimpleFormat.format("\tJVM: {}",System.getProperty("java.version")));
			expectedLines.add(SimpleFormat.format("\tCores: {}",Runtime.getRuntime().availableProcessors()));
			expectedLines.add("\tMemory \\(bytes\\): \\d*/\\d*");
			expectedLines.add(SimpleFormat.format("\tBase path: {}",new File(".").getCanonicalPath()));
			expectedLines.add("\tVersion: unknown/unknown");
			expectedLines.add(figlet.line(_name,(char)'='));
		}

		new Expectations(){{
			_ignitable.getClass(); 
		}};

		Standalone.builder(() -> _ignitable)
						.name(_name)
						.bannerFont(_fontURL)
						.console(message -> console.add(message))
					.build()
						.ignite();

		Assertions.assertLinesMatch(expectedLines, new ArrayList<String>(console));
	}

	@ParameterizedTest(name = "Ignite with name but showbanner at false shouldn't print a banner")
	@MethodSource("bannerDatapack")
	@SuppressWarnings({"UnnecessaryUnboxing","static-access","unchecked"})
	public void bannerFalse(final String _name,final String _font,final URL _fontURL,final @Mocked Ignitable _ignitable) throws IOException{
			Queue console=new LinkedList();

			new Expectations(){{
				_ignitable.getClass(); 
			}};
			Standalone.builder(() -> _ignitable)
							.name(_name)
							.showBanner(false)
							.bannerFont(_fontURL)
							.console(message -> console.add(message))
						.build()
							.ignite();
			
			Assertions.assertNull(console.poll());
	}

	@Test
	@DisplayName("Provide a wrong file format font should raise a NoFigletFontFormatException exception")
	@SuppressWarnings("ThrowableResultIgnored")
	public void figletWrongFileFormat(final @Mocked Ignitable _ignitable) throws MalformedURLException{
		
		//Execute
		Standalone standalone=Standalone.builder(() -> _ignitable)
											.name("name")
											.bannerFont(Paths.get("./src/test/resources/logging.properties").toUri().toURL())
										.build();
			
		Assertions.assertThrows(Figlet.NoFigletFontFormatException.class
										,() -> standalone.ignite()
										,"Input has not figlet font file format (.flf)");
	}

	@Test
	@DisplayName("If no mandatory parameter provided show error message and print help")
	@SuppressWarnings({"ThrowableResultIgnored","unchecked"})
	public <T extends ParameterException> void noMandatoryParameter(final @Mocked Ignitable _ignitable){
		
		Queue console=new LinkedList();
		new Expectations() {{
			_ignitable.parameterProcessingException((T)any); 
				result=new Delegate<T>() {
							public void delegate(T _exception) throws Exception {
								throw _exception;
							}
						};
				times=1;
		}};
		
		//Execute
		Standalone standalone=Standalone.builder(() -> _ignitable)
											.parameters(StandaloneAppTestParameter.class)
											.console(message -> console.add(message))
										.build()
											.ignite();
			
		Assertions.assertEquals(new MandatoryParameterNotProvided(StandaloneAppTestParameter.BOOLEANVALUE).getMessage(),console.poll());
		Assertions.assertEquals(Parameter.getHelp(StandaloneAppTestParameter.class),console.poll());
	}

	@Test
	@DisplayName("If no mandatory parameter of multiple provided show error message and print help")
	@SuppressWarnings({"ThrowableResultIgnored","unchecked"})
	public <T extends ParameterException> void noMandatoryMultipleParameter(final @Mocked Ignitable _ignitable){
		
		Queue console=new LinkedList();
		new Expectations() {{
			_ignitable.parameterProcessingException((T)any); 
				result=new Delegate<T>() {
							public void delegate(T _exception) throws Exception {
								throw _exception;
							}
						};
				times=1;
		}};

		//Execute
		Standalone standalone=Standalone.builder(() -> _ignitable)
											.parameters(StandaloneAppTestParameter.class)
											.parameters(StandaloneAppTestParameter2.class)
											.console(message -> console.add(message))
											.arguments(new String[]{"-booleanvalue:true","-intvalue:2234","-longvalue:3243321312","-floatvalue:3123.32","-doublevalue:3123.32","-stringvalue:TEST","-enumvalue:ENUMVALUE"})
										.build()
											.ignite();

		Assertions.assertEquals(new MandatoryParameterNotProvided(StandaloneAppTestParameter2.ADDITIONALBOOLEANVALUE).getMessage(),console.poll());
		Assertions.assertEquals(Parameter.getHelp(Stream.of(StandaloneAppTestParameter.class,StandaloneAppTestParameter2.class).collect(Collectors.toList())),console.poll());
	}

	@Test
	@DisplayName("Static selfExtinguish method should do nothing if no instance exist")
	public void selfExtinguishNoInstance(final @Mocked Ignitable _ignitable){

		Standalone.self=null;
		Standalone.selfExtinguish(2);
	}
	
	@Test
	@DisplayName("Static selfExtinguish method should call the referenced self instance if exist")
	public void selfExtinguishInstance(final @Mocked Ignitable _ignitable){
		
		AtomicBoolean called=new AtomicBoolean(false);
		
		Standalone.self=new Standalone(() -> _ignitable, null, null,true, null, null, null,null,null, false){
			@Override
			public void extinguish(int _exitCode) {
				Assertions.assertEquals(2,_exitCode);
				called.set(true);
			}
		};
		
		Standalone.selfExtinguish(2);
	}

	@Test
	@DisplayName("Static getParametersClasses method should return the list of parameter classes provided")
	@SuppressWarnings("unchecked")
	public void getParameters(final @Mocked Ignitable _ignitable){
		
		final List parameters=Stream.of(StandaloneAppTestParameter.class,StandaloneAppTestParameter2.class).collect(Collectors.toList());
		
		Standalone.self=new Standalone(() -> _ignitable, null, null,true,null, parameters, null, null,null,false);

		List actualParameters=Standalone.getParametersClasses();
			
		Assertions.assertEquals(parameters, actualParameters);
	}
	
	@Test
	@DisplayName("Static getDescription method should return the description provided")
	@SuppressWarnings("unchecked")
	public void getDescription(final @Mocked Ignitable _ignitable){
		
		Assertions.assertEquals( "my-description", Standalone.builder(() -> _ignitable)
																	.description("my-description")
																.build()
																	.getDescription());
	}
	
	@Test
	@DisplayName("Static getParametersClasses method should return empty list if not any parameter added")
	@SuppressWarnings("unchecked")
	public void getEmptyParameters(final @Mocked Ignitable _ignitable){
		
		final List parameters=Collections.emptyList();
		Standalone.self=new Standalone(() -> _ignitable, null,null, true, null, parameters, null,null, null,false);
		
		List actualParameters=Standalone.getParametersClasses();
			
		Assertions.assertEquals(parameters, actualParameters);
	}

	@Test
	@DisplayName("Static getParametersClasses method should return empty list if null parameters")
	public void getNullParameters(final @Mocked Ignitable _ignitable){
		
		final List parameters=Collections.emptyList();
		Standalone.self=new Standalone(() -> _ignitable, null, null,true, null, null, null,null, null,false);
		
		List actualParameters=Standalone.getParametersClasses();
			
		Assertions.assertEquals(parameters, actualParameters);
	}

	@Test
	@DisplayName("Static getHelp method should return the help of all parameters provided")
	@SuppressWarnings("unchecked")
	public void getHelp(final @Mocked Ignitable _ignitable){
		
		final List parameters=Stream.of(StandaloneAppTestParameter.class,StandaloneAppTestParameter2.class).collect(Collectors.toList());
		Standalone.self=new Standalone(() -> _ignitable, null,null, true, null, parameters, null,null, null,false);
		
		String actualHelp=Standalone.getHelp();
			
		Assertions.assertEquals(Parameter.getHelp(parameters), actualHelp);
	}
	@Test
	@DisplayName("Static getHelp method should return the help of all parameters provided and using the description if present")
	@SuppressWarnings("unchecked")
	public void getHelpWithDesc(final @Mocked Ignitable _ignitable){
		
		final List parameters=Stream.of(StandaloneAppTestParameter.class,StandaloneAppTestParameter2.class).collect(Collectors.toList());
		Standalone.self=new Standalone(() -> _ignitable, null,"mydesc", true, null, parameters, null,null, null,false);
		
		String actualHelp=Standalone.getHelp();
			
		Assertions.assertEquals("mydesc\n"+Parameter.getHelp(parameters), actualHelp);
	}
	
	@Test
	@DisplayName("Static getHelp method should return empty string if not any parameter added")
	@SuppressWarnings("unchecked")
	public void getEmptyHelp(final @Mocked Ignitable _ignitable){
		
		final List parameters=Collections.emptyList();
		Standalone.self=new Standalone(() -> _ignitable, null, null,true, null, parameters, null,null, null,false);
		
		String actualHelp=Standalone.getHelp();
			
		Assertions.assertEquals("", actualHelp);
	}

	@Test
	@DisplayName("Static getHelp method should return empty string if null parameters")
	public void getNullHelp(final @Mocked Ignitable _ignitable){
		
		Standalone.self=new Standalone(() -> _ignitable, null, null,true, null, null, null,null, null,false);

		String actualHelp=Standalone.getHelp();
			
		Assertions.assertEquals("", actualHelp);
	}
	
	@Test
	@DisplayName("Console can be overrided and used")
	@SuppressWarnings("unchecked")
	public void consoleTest(final @Mocked Ignitable _ignitable){
		
		Queue console=new LinkedList();
		Standalone standalone=Standalone.builder(() -> _ignitable)
											.console(message -> console.add(message))
											.verbose(true)
										.build();

		standalone.getConsole().info("info-message-1");
		standalone.getConsole().error("error-message-2");
		standalone.getConsole().verbose("verbose-message-3");
			
		Assertions.assertEquals("info-message-1", console.poll());
		Assertions.assertEquals("error-message-2", console.poll());
		Assertions.assertEquals("verbose-message-3", console.poll());
	}
}
