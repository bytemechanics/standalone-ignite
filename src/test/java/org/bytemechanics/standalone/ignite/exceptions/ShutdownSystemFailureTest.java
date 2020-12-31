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
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author afarre
 */
public class ShutdownSystemFailureTest {
	
	private static final String CAUSE_MESSAGE="my-cause-message";
	
	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> ShutdownSystemFailureTest >>>> setup");
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

	private Throwable cause=new Exception("my-cause-message");
	
	@Test
	public void constructor() {
		final ShutdownSystemFailure instance=new ShutdownSystemFailure(cause);
		Assertions.assertEquals(ShutdownSystemFailure.MESSAGE,instance.getMessage());
		Assertions.assertSame(cause, instance.getCause());
	}
}
