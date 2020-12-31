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
package org.bytemechanics.standalone.ignite.shell.beans;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author afarre
 */
public class CommandExecutionTest {
	
	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> CommandExecutionTest >>>> setup");
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

	/**
	 * Test of getName method, of class CommandExecution.
	 */
	@Test
	public void testGetName() {
		CommandExecution instance = CommandExecution.from("my-test", null);
		String expResult = "my-test";
		String result = instance.getName();
		Assertions.assertEquals(expResult, result);
	}

	/**
	 * Test of getArguments method, of class CommandExecution.
	 */
	@Test
	public void testGetArguments() {
		System.out.println("getArguments");
		CommandExecution instance = CommandExecution.from(null, new String[]{"null",null,"my-test-arg"});
		String[] expResult = new String[]{"null",null,"my-test-arg"};
		String[] result = instance.getArguments();
		Assertions.assertArrayEquals(expResult, result);
	}
}
