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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Mocked;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @author afarre
 */
public class IgnitableAdapterTest {

	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> IgnitableAdapterTest >>>> setup");
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
	@DisplayName("Set standalone should inform console and standalone")
	public void setStandalone(final @Mocked Standalone _standalone,final @Mocked OutConsole _console){

		IgnitableAdapter mock=new IgnitableAdapter() {};
		
		new Expectations() {{
			_standalone.getConsole(); times=1; result=_console;
		}};
		
		mock.setStandalone(_standalone);
		
		Assertions.assertEquals(_console,mock.console);
		Assertions.assertEquals(_standalone,mock.standalone);
	}
	
	@Test
	@DisplayName("Recover standalone when is informed should return an standalone filled")
	public void getStandaloneSetted(final @Mocked Standalone _standalone){

		IgnitableAdapter mock=new IgnitableAdapter() {};
		mock.standalone=_standalone;
		
		Optional<Standalone> actual=mock.getStandalone();
		
		Assertions.assertEquals(true, actual.isPresent());
		Assertions.assertEquals(_standalone, actual.get());
	}
	@Test
	@DisplayName("Recover standalone when is not informed should return an empty optional ")
	public void getStandaloneNotSetted(){

		IgnitableAdapter mock=new IgnitableAdapter() {};
		
		Optional<Standalone> actual=mock.getStandalone();
		
		Assertions.assertEquals(false, actual.isPresent());
	}

	@Test
	@DisplayName("Recover console when is informed should return the console filled")
	public void getConsoleSetted(final @Mocked Standalone _standalone,final @Mocked OutConsole _console){

		IgnitableAdapter mock=new IgnitableAdapter() {};
		mock.console=_console;
		
		Optional<Console> actual=mock.getConsole();
		
		Assertions.assertEquals(true, actual.isPresent());
		Assertions.assertEquals(_console, actual.get());
	}
	@Test
	@DisplayName("Recover console when is not informed should return an empty optional")
	public void getConsoleNotSetted(){

		IgnitableAdapter mock=new IgnitableAdapter() {};
		
		Optional<Console> actual=mock.getConsole();
		
		Assertions.assertEquals(false, actual.isPresent());
	}
}
