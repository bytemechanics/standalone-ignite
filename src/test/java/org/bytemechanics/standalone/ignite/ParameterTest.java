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

import org.bytemechanics.standalone.ignite.mocks.StandaloneAppTestParameter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.bytemechanics.standalone.ignite.exceptions.InvalidParameter;
import org.bytemechanics.standalone.ignite.exceptions.NullOrEmptyMandatoryParameter;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author afarre
 */
public class ParameterTest {

	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> ParameterTest >>>> setup");
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
	
	@ParameterizedTest(name = "ParseParameters {0} for StandaloneAppTestParameter.class must parse correctly the correct values")
	@ValueSource(strings = {"-booleanvalue:true,-intvalue:2234,-longvalue:3243321312,-floatvalue:3123.32,-doublevalue:3123.32,-stringvalue:TEST,-enumvalue:ENUMVALUE"})
	@SuppressWarnings("UnnecessaryUnboxing")
	public void parseParameters(final String _args){
		
		//Prepare
		final String[] arguments=_args.split(",");
		
		//Execute
		Parameter.parseParameters(StandaloneAppTestParameter.class,arguments);
		
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
	}

	@ParameterizedTest(name = "ParseParameters {0} for StandaloneAppTestParameter.class must fail with empty value")
	@ValueSource(strings = {"-booleanvalue:,-intvalue:,-longvalue:,-floatvalue: ,-doublevalue:,-stringvalue:,-enumvalue:"})
	@SuppressWarnings("ThrowableResultIgnored")
	public void parseEmptyParameters(final String _args){
		
		//Prepare
		final String[] arguments=_args.split(",");
		
		//Execute
		Assertions.assertThrows(NullOrEmptyMandatoryParameter.class, 
										() -> Parameter.parseParameters(StandaloneAppTestParameter.class,arguments));
	}
	
	@ParameterizedTest(name = "ValidateParameters {0} for StandaloneAppTestParameter.class must validate correctly the correct values")
	@ValueSource(strings = {"-booleanvalue:true,-intvalue:2234,-longvalue:3243321312,-floatvalue:3123.32,-doublevalue:3123.32,-stringvalue:TEST,-enumvalue:ENUMVALUE"})
	@SuppressWarnings("UnnecessaryUnboxing")
	public void validateParameters(final String _args){
		
		//Prepare
		final String[] arguments=_args.split(",");
		
		//Execute
		Parameter.parseParameters(StandaloneAppTestParameter.class,arguments);
		Parameter.validateParameters(StandaloneAppTestParameter.class);
			
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
	}
	
	@ParameterizedTest(name = "ValidateParameters {0} for StandaloneAppTestParameter.class must fail with wrong value")
	@ValueSource(strings = {"-booleanvalue:true,-intvalue:2234,-longvalue:3243321312,-floatvalue:3123.32,-doublevalue:3123.32,-stringvalue:semanticFailure,-enumvalue:ENUMVALUE"})
	@SuppressWarnings("ThrowableResultIgnored")
	public void validateWrongParameters(final String _args){
		
		//Prepare
		final String[] arguments=_args.split(",");
		
		//Execute
		Parameter.parseParameters(StandaloneAppTestParameter.class,arguments);
		Assertions.assertThrows(InvalidParameter.class
										,() -> Parameter.validateParameters(StandaloneAppTestParameter.class)
										,"Invalid parameter STRINGVALUE with value semanticFailure: semantic test error requested");
	}
	
	@Test
	@DisplayName("Test default generated help")
	public void getHelp() {

		//Prepare
		String help = "Usage:"
			+ "\n\t[-booleanvalue]: boolean value (Mandatory)"
			+ "\n\t[-intvalue]: int value (Mandatory)"
			+ "\n\t[-longvalue]: long value (Mandatory)"
			+ "\n\t[-floatvalue]: float value (Mandatory)"
			+ "\n\t[-doublevalue]: double value (Mandatory)"
			+ "\n\t[-stringvalue]: string value (Mandatory)"
			+ "\n\t[-enumvalue]: string value (Mandatory)\n";

		//when:
		String actual = Parameter.getHelp(StandaloneAppTestParameter.class);

		//then: 
		Assertions.assertEquals(help, actual);
	}
}
