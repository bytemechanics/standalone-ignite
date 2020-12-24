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
import java.util.logging.LogManager;
import java.util.logging.Logger;
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
public class UnparseableParameterTest {
	
	private static final String CAUSE_MESSAGE="my-cause-message";
	
	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> UnparseableParameterTest >>>> setup");
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

	private Throwable cause=new Exception(CAUSE_MESSAGE);
	
	@Test
	public void constructor(@Mocked Parameter _parameter) {
		
		final String NAME="my-parameter";
		
		new Expectations() {{
			_parameter.name(); result=NAME;
		}};
		
		final UnparseableParameter instance=new UnparseableParameter(_parameter,cause);
		Assertions.assertEquals(SimpleFormat.format(UnparseableParameter.MESSAGE_WITHOUT_VALUE,NAME,CAUSE_MESSAGE) ,instance.getMessage());
	}
	
	@Test
	public void constructor() {
		
		final UnparseableParameter instance=new UnparseableParameter(null,cause);
		Assertions.assertEquals(SimpleFormat.format(UnparseableParameter.MESSAGE_WITHOUT_VALUE,null,CAUSE_MESSAGE) ,instance.getMessage());
	}

	@Test
	public void constructorNullCause(@Mocked Parameter _parameter) {
		
		final String NAME="my-parameter";
		
		new Expectations() {{
			_parameter.name(); result=NAME;
		}};
		
		final UnparseableParameter instance=new UnparseableParameter(_parameter,null);
		Assertions.assertEquals(SimpleFormat.format(UnparseableParameter.MESSAGE_WITHOUT_VALUE,NAME,null) ,instance.getMessage());
	}
	
	@Test
	public void constructorWithValue(@Mocked Parameter _parameter) {
		
		final String NAME="my-parameter";
		final String VALUE="my-value-parameter";
		
		new Expectations() {{
			_parameter.name(); result=NAME;
		}};
		
		final UnparseableParameter instance=new UnparseableParameter(_parameter,VALUE,cause);
		Assertions.assertEquals(SimpleFormat.format(UnparseableParameter.MESSAGE_WITH_VALUE,NAME,VALUE,CAUSE_MESSAGE) ,instance.getMessage());
	}
	@Test
	public void constructorWithValue() {
		
		final String VALUE="my-value-parameter";
		
		final UnparseableParameter instance=new UnparseableParameter(null,VALUE,cause);
		Assertions.assertEquals(SimpleFormat.format(UnparseableParameter.MESSAGE_WITH_VALUE,null,VALUE,CAUSE_MESSAGE) ,instance.getMessage());
	}
	@Test
	public void constructorWithValueNullCause(@Mocked Parameter _parameter) {
		
		final String NAME="my-parameter";
		final String VALUE="my-value-parameter";
		
		new Expectations() {{
			_parameter.name(); result=NAME;
		}};
		
		final UnparseableParameter instance=new UnparseableParameter(_parameter,VALUE,null);
		Assertions.assertEquals(SimpleFormat.format(UnparseableParameter.MESSAGE_WITH_VALUE,NAME,VALUE,null) ,instance.getMessage());
	}
}
