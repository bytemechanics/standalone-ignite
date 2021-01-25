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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.bytemechanics.standalone.ignite.Console;
import org.bytemechanics.standalone.ignite.Ignitable;
import org.bytemechanics.standalone.ignite.Standalone;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;
import org.bytemechanics.standalone.ignite.mocks.MockedIgnitableAdapter;
import org.bytemechanics.standalone.ignite.mocks.MockedIgnitableAutocloseable;
import org.bytemechanics.standalone.ignite.mocks.MockedIgnitableRunnable;
import org.bytemechanics.standalone.ignite.shell.beans.CommandExecution;
import org.bytemechanics.standalone.ignite.shell.exceptions.NoStandaloneInstance;
import org.bytemechanics.standalone.ignite.shell.exceptions.UnknownCommand;
import org.bytemechanics.standalone.ignite.shell.exceptions.UnknownConsoleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 *
 * @author afarre
 */
public class ShellAdapterTest {
	
	@BeforeAll
	public static void setup() throws IOException {
		System.out.println(">>>>> ShellAdapterTest >>>> setup");
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
	public void getShell(@Mocked java.io.Console _console) {
		
		final ShellConsole shellConsole=new ShellConsole(_console, SimpleFormat::format, false);
		
		ShellAdapter adapter=new ShellAdapter() {
			
			@Override
			public Optional<Console> getConsole() {
				return Optional.ofNullable(shellConsole);
			}
			
			@Override
			protected Map<Class<? extends Ignitable>, Standalone.StandaloneBuilder> getIgnitableShellCommands() {
				return null;
			}
		};
		
		Assertions.assertEquals(shellConsole,adapter.getShell());
	}
	@Test
	@SuppressWarnings("ThrowableResultIgnored")
	public void getShellNull() {
		
		ShellAdapter adapter=new ShellAdapter() {
			
			@Override
			public Optional<Console> getConsole() {
				return Optional.empty();
			}
			
			@Override
			protected Map<Class<? extends Ignitable>, Standalone.StandaloneBuilder> getIgnitableShellCommands() {
				return null;
			}
		};
		
		Assertions.assertThrows(UnknownConsoleType.class
								,() -> adapter.getShell()
								,SimpleFormat.format("Unknown console {} type. Expected ShellConsole instance", "null"));
	}
	@Test
	@SuppressWarnings("ThrowableResultIgnored")
	public void getShellUnknown(@Mocked Console _console) {
		
		ShellAdapter adapter=new ShellAdapter() {
			
			@Override
			public Optional<Console> getConsole() {
				return Optional.ofNullable(_console);
			}
			
			
			@Override
			protected Map<Class<? extends Ignitable>, Standalone.StandaloneBuilder> getIgnitableShellCommands() {
				return null;
			}
		};
				
		Assertions.assertThrows(UnknownConsoleType.class
								,() -> adapter.getShell()
								,SimpleFormat.format("Unknown console {} type. Expected ShellConsole instance", _console.getClass()));
	}


	@Test
	@SuppressWarnings("empty-statement")
	public void getCommandList(@Tested ShellAdapter _adapter,@Mocked Standalone.StandaloneBuilder _builder) {
		
		class IgnitableCommand1 implements Ignitable{};
		class IgnitableCommand2 implements Ignitable{};

		new Expectations() {{
			_adapter.getIgnitableShellCommands(); times=1; result=Stream.of(IgnitableCommand1.class,IgnitableCommand2.class)
																		.collect(Collectors.toMap(val -> val,val -> _builder));
		}};
		
		Assertions.assertEquals("ignitablecommand1,ignitablecommand2,exit,help",_adapter.getCommandList());
	}
	
	@Test
	@SuppressWarnings("empty-statement")
	public void getAvailableCommands(@Tested ShellAdapter _adapter,@Mocked Standalone.StandaloneBuilder _builder1,@Mocked Standalone.StandaloneBuilder _builder2) {
		
		class IgnitableCommand1 implements Ignitable{};
		class IgnitableCommand2 implements Ignitable{};
		final Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> commands=new HashMap<>();
		commands.put(IgnitableCommand1.class,Standalone.builder(() -> new IgnitableCommand1()));
		commands.put(IgnitableCommand2.class,Standalone.builder(() -> new IgnitableCommand2()));

		new Expectations() {{
			_adapter.getIgnitableShellCommands(); times=1; result=commands;
		}};
		
		Map<String,BiConsumer<String[],ShellConsole>> actual=_adapter.getAvailableCommands();
		Assertions.assertEquals(4,actual.size());
		Assertions.assertTrue(actual.containsKey("ignitablecommand1"));
		Assertions.assertTrue(actual.containsKey("ignitablecommand2"));
		Assertions.assertTrue(actual.containsKey("exit"));
		Assertions.assertTrue(actual.containsKey("help"));
	}

	@SuppressWarnings("static-access")
	static Stream<Arguments> multilineCommandDatapack(){
		return Stream.of(Arguments.of(new String[0],Collections.emptyList())
							,Arguments.of(new String[]{""},Collections.emptyList())
							,Arguments.of(new String[]{"command","1;","command","2"},Stream.of("command 1","command 2").collect(Collectors.toList()))
							,Arguments.of(new String[]{"command","\"style 1\";","command","\"style 2\""},Stream.of("command \"style 1\"","command \"style 2\"").collect(Collectors.toList()))
							,Arguments.of(new String[]{"command","'style 1';","command","'style 2'"},Stream.of("command 'style 1'","command 'style 2'").collect(Collectors.toList()))
							,Arguments.of(new String[]{";command","'style 1';","command","2;","command","3",";","command4;;;command5"},Stream.of("command 'style 1'","command 2","command 3","command4","command5").collect(Collectors.toList()))
							,Arguments.of(new String[]{"command","'style","1';","command","2;","command","3",";","command4;;",";command5;"},Stream.of("command 'style 1'","command 2","command 3","command4","command5").collect(Collectors.toList()))
							,Arguments.of(new String[]{"","'style","1';command","2;","command","3",";","command4;;",";command5;"},Stream.of("'style 1'","command 2","command 3","command4","command5").collect(Collectors.toList()))
							,Arguments.of(new String[]{";"},Collections.emptyList())
							,Arguments.of(new String[]{";",";",";"},Collections.emptyList())
							,Arguments.of(new String[]{"command","-path:Allianz Brasil/myPath with spaces","-arg2:true;"
																,"command2","-path:Allianz Brasil/myPath with spaces;"
															,"-arg2:false"}
																	,Stream.of("command -path:\"Allianz Brasil/myPath with spaces\" -arg2:true"
																			,"command2 -path:\"Allianz Brasil/myPath with spaces\""
																		,"-arg2:false")
																			.collect(Collectors.toList()))
							,Arguments.of(new String[]{"command","-path:Allianz Brasil/myPath with spaces","-path2:Allianz España\\myPath with spaces","-arg2:true;"
																,"command2","-path:Allianz Brasil/myPath with spaces;"
															,"-arg2:false"}
															,Stream.of("command -path:\"Allianz Brasil/myPath with spaces\" -path2:\"Allianz España\\myPath with spaces\" -arg2:true"
																			,"command2 -path:\"Allianz Brasil/myPath with spaces\""
																,"-arg2:false")
																.collect(Collectors.toList()))
						);
	}	
	@ParameterizedTest(name="When try to split commands from {0} the result should be {1}")
	@MethodSource("multilineCommandDatapack")
	public void splitCommands(final String[] _arguments,final List<String> _expectedCommands,@Tested ShellAdapter _adapter) {
		Assertions.assertEquals(_expectedCommands, _adapter.splitCommands(_arguments));
	}

	@SuppressWarnings("static-access")
	static Stream<Arguments> commandBuilderDatapack(){
		return Stream.of(Arguments.of("",Optional.empty())
							,Arguments.of("command1",Optional.of(CommandExecution.from("command1",new String[0])))
							,Arguments.of("command2 2 \"dvbb   sdsfd\"",Optional.of(CommandExecution.from("command2",new String[]{"2","\"dvbb","sdsfd\""})))
							,Arguments.of("command3 2 'dvbb  sdsfd'",Optional.of(CommandExecution.from("command3",new String[]{"2","'dvbb","sdsfd'"})))
							,Arguments.of("command4 2 \"dvbb  sdsfd\"",Optional.of(CommandExecution.from("command4",new String[]{"2","\"dvbb","sdsfd\""})))
						);
	}	
	@ParameterizedTest(name="When build a command from {0} the result should be {1}")
	@MethodSource("commandBuilderDatapack")
	public void buildCommand(final String _commamd,final Optional<CommandExecution> _expected,@Tested ShellAdapter _adapter) {
		
		final Optional<CommandExecution> actual=_adapter.buildCommand(_commamd);
		Assertions.assertEquals(_expected.isPresent(),actual.isPresent());
		if(actual.isPresent()){
			Assertions.assertEquals(_expected.get().getName(),actual.get().getName());
			Assertions.assertArrayEquals(_expected.get().getArguments(),actual.get().getArguments());
		}
	}

	@Test
	@SuppressWarnings("empty-statement")
	public void executeCommand(@Mocked BiConsumer<String[],ShellConsole> _consumer,@Mocked ShellConsole _console) {
		
		Map<String,BiConsumer<String[],ShellConsole>> availableCommands=Stream.of("my-command1","my-command_2")
																			.collect(Collectors.toMap(command -> command, command -> _consumer));
		ShellAdapter instance=new ShellAdapter() {
			@Override
			protected ShellConsole getShell() {
				return _console;
			}	
			@Override
			public Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> getIgnitableShellCommands(){
				return null; 
			}
		};
		
		new Expectations() {{
			_consumer.accept(new String[]{"arg1","arg2","arg3"}, _console); times=1;
		}};

		instance.executeCommand(availableCommands, "my-Command_2 arg1 arg2 arg3");
	}
	@Test
	@SuppressWarnings({"ThrowableResultIgnored"})
	public void executeCommandEmpty(@Mocked BiConsumer<String[],ShellConsole> _consumer,@Mocked ShellConsole _console) {
		
		Map<String,BiConsumer<String[],ShellConsole>> availableCommands=Stream.of("my-command1","my-command_2")
																			.collect(Collectors.toMap(command -> command, command -> _consumer));
		ShellAdapter instance=new ShellAdapter() {
			@Override
			protected ShellConsole getShell() {
				return _console;
			}	
			@Override
			public Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> getIgnitableShellCommands(){
				return null; 
			}
		};
		
		new Expectations() {{
			_consumer.accept(new String[]{"arg1","arg2","arg3"}, _console); times=0;
		}};

		instance.executeCommand(availableCommands, "\n");
	}
	@Test
	@SuppressWarnings({"ThrowableResultIgnored"})
	public void executeCommandUnknown(@Mocked BiConsumer<String[],ShellConsole> _consumer,@Mocked ShellConsole _console) {
		
		Map<String,BiConsumer<String[],ShellConsole>> availableCommands=Stream.of("my-command1","my-command_2")
																			.collect(Collectors.toMap(command -> command, command -> _consumer));
		
		ShellAdapter instance=new ShellAdapter() {
			@Override
			protected String getCommandList() {
				return "my-command1,my-command_2";
			}	
			@Override
			protected ShellConsole getShell() {
				return _console;
			}	
			@Override
			public Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> getIgnitableShellCommands(){
				return null; 
			}
		};
		
		new Expectations() {{
			_consumer.accept(new String[]{"arg1","arg2","arg3"}, _console); times=0;
		}};

		Assertions.assertThrows(UnknownCommand.class
								, () -> instance.executeCommand(availableCommands, "my-Command_1 arg1 arg2 arg3")
								,SimpleFormat.format("Unknown command {} available commands are {}","my-Command_1","my-command1,my-command_2"));
	}

	@Test
	@SuppressWarnings({"ThrowableResultIgnored"})
	public void batchExecution(@Mocked ShellConsole _console,@Mocked BiConsumer<String[],ShellConsole> _consumer1,@Mocked BiConsumer<String[],ShellConsole> _consumer2) {
		
		Map<String,BiConsumer<String[],ShellConsole>> availableCommands=new HashMap<>();
		availableCommands.put("my-command1", _consumer1);
		availableCommands.put("my-command_2", _consumer2);
		
		ShellAdapter instance=new ShellAdapter() {
			@Override
			protected String getCommandList() {
				return "my-command1,my-command_2";
			}	
			@Override
			protected ShellConsole getShell() {
				return _console;
			}	
			@Override
			public Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> getIgnitableShellCommands(){
				return null; 
			}
		};
		
		new Expectations() {{
			_consumer1.accept(new String[]{"1","dsfd","fdf","d","fd"}, _console); times=1;
			_consumer2.accept(new String[]{"2","ds","df","gf"}, _console); times=1;
			_consumer2.accept(new String[]{"3","ds","df","gf"}, _console); times=1;
			_consumer2.accept(new String[]{"4","ds","df","gf"}, _console); times=1;
		}};
		
		instance.batchExecution(availableCommands, Stream.of("my-Command1 1 dsfd fdf d fd","My-Command_2 2 ds df gf","My-Command_2 3 ds df gf","My-Command_2 4 ds df gf")
																.collect(Collectors.toList()));
	}

	@Test
	@SuppressWarnings({"ThrowableResultIgnored"})
	public void interactiveExecution(@Mocked ShellConsole _console,@Mocked BiConsumer<String[],ShellConsole> _consumer1,@Mocked BiConsumer<String[],ShellConsole> _consumer2,@Mocked BiConsumer<String[],ShellConsole> _consumer3) {
		
		ShellAdapter instance=new ShellAdapter() {
			@Override
			protected String getCommandList() {
				return "my-command1,my-command_2";
			}	
			@Override
			protected ShellConsole getShell() {
				return _console;
			}	
			@Override
			public Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> getIgnitableShellCommands(){
				Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> reply=new HashMap<>();
				reply.put(MockedIgnitableAdapter.class, Standalone.builder(MockedIgnitableAdapter::new ).description("my-desc1"));
				reply.put(MockedIgnitableAutocloseable.class, Standalone.builder(MockedIgnitableAutocloseable::new ).name("my-name1"));
				reply.put(MockedIgnitableRunnable.class, Standalone.builder(MockedIgnitableRunnable::new ));
				return null; 
			}
		};

		Map<String,BiConsumer<String[],ShellConsole>> availableCommands=new HashMap<>();
		availableCommands.put("mockedignitableadapter", _consumer1);
		availableCommands.put("mockedignitableautocloseable", _consumer2);
		availableCommands.put("mockedignitablerunnable", _consumer3);
		availableCommands.put("exit", (args,console) -> instance.stopExecution=true);
		availableCommands.put("help", (args,console) -> console.info("Available commands are:\n"
															+ "{}"
															+ "To exit from shell please use 'exit' command\n"
															+ "To get help with a certain command write: <command> -help"
														,"\tmockedignitableadapter - my-desc1\n"
														+ "\tmockedignitableautocloseable - my-name1\n"
														+ "\tmockedignitablerunnable - no description provided\n"));
		
		new Expectations() {{
			_console.write(">> "); times=6;
			_console.info("Available commands are:\n{}To exit from shell please use 'exit' command\nTo get help with a certain command write: <command> -help","\tmockedignitableadapter - my-desc1\n\tmockedignitableautocloseable - my-name1\n\tmockedignitablerunnable - no description provided\n"); times=1;
			_console.read(); times=6; 
				result="mockedignitableadapter 1 dsfd fdf d fd";
				result="mockedignitableadapter 2 ds df gf";
				result="mockedignitableautocloseable 3 ds df gf";
				result="help";
				result="mockedignitablerunnable 4 ds df gf";
				result="exit";
			_consumer1.accept(new String[]{"1","dsfd","fdf","d","fd"}, _console); times=1;
			_consumer1.accept(new String[]{"2","ds","df","gf"}, _console); times=1;
			_consumer2.accept(new String[]{"3","ds","df","gf"}, _console); times=1;
			_consumer3.accept(new String[]{"4","ds","df","gf"}, _console); times=1;
		}};
		
		instance.interactiveExecution(availableCommands);
	}	

	@Test
	@SuppressWarnings({"ThrowableResultIgnored"})
	public void startupNoStandalone() {
		
		ShellAdapter instance=new ShellAdapter() {
			@Override
			public Map<String,BiConsumer<String[],ShellConsole>> getAvailableCommands() {
				return Collections.emptyMap();
			}	
			@Override
			public Optional<Standalone> getStandalone() {
				return Optional.empty();
			}	
			@Override
			public Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> getIgnitableShellCommands(){
				return null; 
			}
		};
		
		Assertions.assertThrows(NoStandaloneInstance.class
								, () -> instance.startup());
	}
	
	@Test
	@SuppressWarnings({"ThrowableResultIgnored"})
	public void startupBatch(final @Mocked Standalone _standalone) {
		
		final AtomicBoolean batchExecution=new AtomicBoolean(false);
		final AtomicBoolean interactiveExecution=new AtomicBoolean(false);
		
		ShellAdapter instance=new ShellAdapter() {
			@Override
			public Map<String,BiConsumer<String[],ShellConsole>> getAvailableCommands() {
				return Collections.emptyMap();
			}	
			@Override
			public Optional<Standalone> getStandalone() {
				return Optional.of(_standalone);
			}	

			@Override
			protected void interactiveExecution(Map<String, BiConsumer<String[], ShellConsole>> _availableCommands) {
				interactiveExecution.set(true);
			}
			@Override
			protected void batchExecution(Map<String, BiConsumer<String[], ShellConsole>> _availableCommands, List<String> _commands) {
				Assertions.assertEquals(Stream.of("my-Command1 1 dsfd fdf d fd","My-Command_2 2 ds df gf","My-Command_2 3 ds df gf","My-Command_2 4 ds df gf")
											.collect(Collectors.toList())
										,_commands);
				batchExecution.set(true);
			}
			@Override
			public Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> getIgnitableShellCommands(){
				return null; 
			}
		};
		
		new Expectations() {{
			_standalone.getArguments(); times=1; result=new String[]{"my-Command1","1","dsfd","fdf","d","fd;My-Command_2","2","ds","df","gf;My-Command_2","3","ds","df","gf;My-Command_2","4","ds","df","gf"};
		}};
		
		instance.startup();
		Assertions.assertTrue(batchExecution.get());
		Assertions.assertFalse(interactiveExecution.get());
	}	
	
	@Test
	@SuppressWarnings({"ThrowableResultIgnored"})
	public void startupInteractive(@Tested ShellAdapter _adapter,final @Mocked Standalone _standalone) {

		final AtomicBoolean batchExecution=new AtomicBoolean(false);
		final AtomicBoolean interactiveExecution=new AtomicBoolean(false);
		
		ShellAdapter instance=new ShellAdapter() {
			@Override
			public Map<String,BiConsumer<String[],ShellConsole>> getAvailableCommands() {
				return Collections.emptyMap();
			}	
			@Override
			public Optional<Standalone> getStandalone() {
				return Optional.of(_standalone);
			}	

			@Override
			protected void interactiveExecution(Map<String, BiConsumer<String[], ShellConsole>> _availableCommands) {
				interactiveExecution.set(true);
			}
			@Override
			protected void batchExecution(Map<String, BiConsumer<String[], ShellConsole>> _availableCommands, List<String> _commands) {
				Assertions.assertEquals(Stream.of("my-Command1 1 dsfd fdf d fd","My-Command_2 2 ds df gf","My-Command_2 3 ds df gf","My-Command_2 4 ds df gf")
											.collect(Collectors.toList())
										,_commands);
				batchExecution.set(true);
			}
			@Override
			public Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> getIgnitableShellCommands(){
				return null; 
			}
		};
		
		new Expectations() {{
			_standalone.getArguments(); times=1; result=new String[0];
		}};
		
		instance.startup();
		Assertions.assertFalse(batchExecution.get());
		Assertions.assertTrue(interactiveExecution.get());
	}	
}
