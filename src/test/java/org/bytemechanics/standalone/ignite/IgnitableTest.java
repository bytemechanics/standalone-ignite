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
package org.bytemechanics.standalone.ignite;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import mockit.Tested;
import mockit.Verifications;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.mocks.MockedIgnitableAutocloseable;
import org.bytemechanics.standalone.ignite.mocks.MockedIgnitableRunnable;
import org.bytemechanics.standalone.ignite.mocks.MockedIgnitableRunnableAutocloseable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 *
 * @author E103880
 */
public class IgnitableTest {
	
	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> IgnitableTest >>>> setup");
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
	public <RUNNABLE extends Runnable & Ignitable> void igniteRunnable(@Tested MockedIgnitableRunnable _ignitable){
	
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();

		new Verifications(){{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.run(); times=1; 
			_ignitable.afterStartup(); times=1;
		}};
	}
	
	@Test
	public <RUNNABLE extends Runnable & Ignitable> void igniteAutocloseable(@Tested MockedIgnitableAutocloseable _ignitable) throws Exception{
	
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();

		new Verifications(){{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1;
			_ignitable.close(); times=1;
			_ignitable.shutdown(); times=1;
			_ignitable.afterShutdown(); times=1;
		}};
	}
	@Test
	public <RUNNABLE extends Runnable & Ignitable> void igniteRunnableAutocloseable(@Tested MockedIgnitableRunnableAutocloseable _ignitable) throws Exception{
	
		Standalone.builder(() -> _ignitable)
					.build()
						.ignite();

		new Verifications(){{
			_ignitable.beforeStartup(); times=1;
			_ignitable.startup(); times=1; 
			_ignitable.run(); times=1; 
			_ignitable.afterStartup(); times=1;
			_ignitable.beforeShutdown(); times=1;
			_ignitable.close(); times=1;
			_ignitable.shutdown(); times=1;
			_ignitable.afterShutdown(); times=1;
		}};
	}


	@Test
	@SuppressWarnings("ThrowableResultIgnored")
	public <RUNNABLE extends Runnable & Ignitable> void failBeforeStartup(){
	
		final String ERRORMESSAGE="errot";
		
		final Ignitable ignitable=new Ignitable() {
			@Override
			public void beforeStartup() {
				throw new RuntimeException(ERRORMESSAGE);
			}
		};
		
		Standalone standalone=Standalone.builder(() -> ignitable)
										.build();
		
		Assertions.assertThrows(RuntimeException.class
								, () -> standalone.ignite()
								, ERRORMESSAGE);
	}
	@Test
	@SuppressWarnings("ThrowableResultIgnored")
	public <RUNNABLE extends Runnable & Ignitable> void failStartup(){
	
		final String ERRORMESSAGE="errot";
		
		final Ignitable ignitable=new Ignitable() {
			@Override
			public void startup() {
				throw new RuntimeException(ERRORMESSAGE);
			}
		};
		
		Standalone standalone=Standalone.builder(() -> ignitable)
										.build();
		
		Assertions.assertThrows(RuntimeException.class
								, () -> standalone.ignite()
								, ERRORMESSAGE);
	}
	@Test
	@SuppressWarnings("ThrowableResultIgnored")
	public <RUNNABLE extends Runnable & Ignitable> void failAfterStartup(){
	
		final String ERRORMESSAGE="errot";

		final Ignitable ignitable=new Ignitable() {
			@Override
			public void afterStartup() {
				throw new RuntimeException(ERRORMESSAGE);
			}
		};
		
		Standalone standalone=Standalone.builder(() -> ignitable)
										.build();
		
		Assertions.assertThrows(RuntimeException.class
								, () -> standalone.ignite()
								, ERRORMESSAGE);
	}

	@Test
	@SuppressWarnings("ThrowableResultIgnored")
	public <RUNNABLE extends Runnable & Ignitable> void failBeforeShutdown(){
	
		final String ERRORMESSAGE="errot";
		
		final Ignitable ignitable=new Ignitable() {
			@Override
			public void beforeShutdown() {
				throw new RuntimeException(ERRORMESSAGE);
			}
		};
		
		Standalone standalone=Standalone.builder(() -> ignitable)
										.build()
											.ignite();
		
		Assertions.assertThrows(RuntimeException.class
								, () -> standalone.shutdown()
								, ERRORMESSAGE);
	}
	
	@Test
	@SuppressWarnings("ThrowableResultIgnored")
	public <RUNNABLE extends Runnable & Ignitable> void failShutdown(){
	
		final String ERRORMESSAGE="errot";
		
		final Ignitable ignitable=new Ignitable() {
			@Override
			public void shutdown() {
				throw new RuntimeException(ERRORMESSAGE);
			}
		};
		
		Standalone standalone=Standalone.builder(() -> ignitable)
										.build()
											.ignite();
		
		Assertions.assertThrows(RuntimeException.class
								, () -> standalone.shutdown()
								, ERRORMESSAGE);
	}
	@Test
	@SuppressWarnings("ThrowableResultIgnored")
	public <RUNNABLE extends Runnable & Ignitable> void failAfterShutdown(){
	
		final String ERRORMESSAGE="errot";

		final Ignitable ignitable=new Ignitable() {
			@Override
			public void afterShutdown() {
				throw new RuntimeException(ERRORMESSAGE);
			}
		};
		
		Standalone standalone=Standalone.builder(() -> ignitable)
										.build()
											.ignite();
		
		Assertions.assertThrows(RuntimeException.class
								, () -> standalone.shutdown()
								, ERRORMESSAGE);
	}
}
