/*
 * Copyright 2020 Byte Mechanics.
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
package org.bytemechanics.standalone.ignite.exceptions;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mockit.Expectations;
import mockit.Mocked;
import org.bytemechanics.standalone.ignite.Parameter;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author afarre
 */
public class MandatoryParameterNotProvidedTest {
	
	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> MandatoryParameterNotProvidedTest >>>> setup");
		try (InputStream inputStream = LambdaUnchecker.class.getResourceAsStream("/logging.properties")) {
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (final IOException e) {
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}

	@BeforeEach
	void beforeEachTest(final TestInfo _testInfo) {
		System.out.println(">>>>> " + this.getClass().getSimpleName() + " >>>> " + _testInfo.getTestMethod().map(Method::getName).orElse("Unkown") + "" + _testInfo.getTags().toString() + " >>>> " + _testInfo.getDisplayName());
	}

	@Test
	public void constructor(@Mocked Parameter _parameter) throws MalformedURLException {
		
		final String NAME="my-parameter";
		final String[] PREFIXES=new String[]{"prefix1","prefix2"};
		
		new Expectations() {{
			_parameter.name(); result=NAME;
			_parameter.getPrefixes(); result=PREFIXES;
		}};
		
		final MandatoryParameterNotProvided instance=new MandatoryParameterNotProvided(_parameter);
		Assertions.assertEquals(SimpleFormat.format(MandatoryParameterNotProvided.MESSAGE,NAME,Stream.of(PREFIXES)
																									.map(prefix -> prefix+":")
																									.collect(Collectors.toList())) 
								,instance.getMessage());
	}
	
	@Test
	public void constructor() throws MalformedURLException {
		
		final MandatoryParameterNotProvided instance=new MandatoryParameterNotProvided(null);
		Assertions.assertEquals(SimpleFormat.format(MandatoryParameterNotProvided.MESSAGE,null,Collections.emptyList()) 
								,instance.getMessage());
	}
}
