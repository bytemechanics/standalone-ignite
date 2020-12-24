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
package org.bytemechanics.standalone.ignite.shell;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Expectations;
import mockit.Mocked;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @author afarre
 */
public class ShellConsoleTest {
	
	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> ShellConsoleTest >>>> setup");
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
	public void testWriteVerboseEnabled(@Mocked java.io.Console _console) {
		
		final ShellConsole instance = new ShellConsole(_console, (message,args) -> SimpleFormat.format(message, args), true);
		
		new Expectations(){{
			_console.format("my message yeah as info message"); times=1;
			_console.format("my message yeah as error message"); times=1;
			_console.format("my message yeah as verbose message"); times=1;
		}};
		
		instance.info("my message {} as {} message", "yeah","info");
		instance.error("my message {} as {} message", "yeah","error");
		instance.verbose("my message {} as {} message", "yeah","verbose");
	}
	@Test
	public void testWriteVerboseDisabled(@Mocked java.io.Console _console) {
		
		final ShellConsole instance = new ShellConsole(_console, (message,args) -> SimpleFormat.format(message, args), false);
		
		new Expectations(){{
			_console.format("my message yeah as info message"); times=1;
			_console.format("my message yeah as error message"); times=1;
			_console.format("my message yeah as verbose message"); times=0;
		}};
		
		instance.info("my message {} as {} message", "yeah","info");
		instance.error("my message {} as {} message", "yeah","error");
		instance.verbose("my message {} as {} message", "yeah","verbose");
	}	

	@Test
	public void testRead(@Mocked java.io.Console _console) {
		
		final ShellConsole instance = new ShellConsole(_console, (message,args) -> SimpleFormat.format(message, args), true);
		
		new Expectations(){{
			_console.format("my message yeah as info message"); times=1;
			_console.format("my message yeah as error message"); times=1;
			_console.format("my message yeah as verbose message"); times=1;
			_console.readLine(); times=1; result="my read message";
		}};
		
		instance.info("my message {} as {} message", "yeah","info");
		instance.error("my message {} as {} message", "yeah","error");
		instance.verbose("my message {} as {} message", "yeah","verbose");
		Assertions.assertEquals("my read message", instance.read()); 
	}	
}
