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
import java.util.function.Consumer;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Mocked;
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
public class ConsoleTest {

	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> ConsoleTest >>>> setup");
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
	
	@ParameterizedTest(name = "Console log with verbose disabled")
	public void verboseDisabled(final String _args, final @Mocked Consumer<String> _underlayingConsole){

		Console console=new Console(_underlayingConsole, false);

		new Expectations(){{
			_underlayingConsole.accept("error-message"); times=1;
			_underlayingConsole.accept("info-message"); times=1;
			_underlayingConsole.accept("verbose-message"); times=0;
		}};
		
		console.error("error-message");
		console.info("info-message");
		console.verbose("verbose-message");
	}
	
	@ParameterizedTest(name = "Console log with verbose enabled")
	public void verboseEnabled(final String _args, final @Mocked Consumer<String> _underlayingConsole){

		Console console=new Console(_underlayingConsole, true);

		new Expectations(){{
			_underlayingConsole.accept("error-message"); times=1;
			_underlayingConsole.accept("info-message"); times=1;
			_underlayingConsole.accept("verbose-message"); times=1;
		}};
		
		console.error("error-message");
		console.info("info-message");
		console.verbose("verbose-message");
	}
}
