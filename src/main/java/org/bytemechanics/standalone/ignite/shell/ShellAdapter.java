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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.standalone.ignite.Ignitable;
import org.bytemechanics.standalone.ignite.IgnitableAdapter;
import org.bytemechanics.standalone.ignite.OutConsole;
import org.bytemechanics.standalone.ignite.Standalone;
import org.bytemechanics.standalone.ignite.internal.commons.functional.Tuple;
import org.bytemechanics.standalone.ignite.shell.beans.CommandExecution;
import org.bytemechanics.standalone.ignite.shell.exceptions.NoStandaloneInstance;
import org.bytemechanics.standalone.ignite.shell.exceptions.UnknownCommand;
import org.bytemechanics.standalone.ignite.shell.exceptions.UnknownConsoleType;

/**
 * Ignitable Adapter to create shells
 * @author afarre
 * @since 2.0.0
 */
public abstract class ShellAdapter extends IgnitableAdapter {
	
	/** flag to stop interactive executions */
	protected boolean stopExecution=false;

	/**
	 * Return a map of the available ignitable commands
	 * Must be override to point to the actual ignitable commands
	 * @return Map using key as ignitable classes and value the standalone builder
	 */
	protected abstract Map<Class<? extends Ignitable>,Standalone.StandaloneBuilder> getIgnitableShellCommands();

	protected ShellConsole getShell() {
		return (ShellConsole)getConsole()
					.filter(consoleInstance -> ShellConsole.class.isAssignableFrom(consoleInstance.getClass()))
					.map(consoleInstance -> consoleInstance)
					.orElseThrow(() -> new UnknownConsoleType(getConsole().orElse(null)));
	}
	
	
	
	protected String getCommandList(){
		return getIgnitableShellCommands()
					.keySet()
						.stream()
							.map(Class::getSimpleName)
							.map(String::toLowerCase)
							.sorted()
						.collect(Collectors.joining(","));
	}
	
	public Map<String,BiConsumer<String[],OutConsole>> getAvailableCommands(){
		
		final Map<String,BiConsumer<String[],OutConsole>> reply=new HashMap<>();
		
		getIgnitableShellCommands()
				.entrySet()
					.stream()
						.map(entry -> Tuple.of(entry.getKey(), entry.getValue()))
						.map(tuple -> tuple.left(tuple.left().getSimpleName()))
						.map(tuple -> tuple.left(tuple.left().toLowerCase()))
						.map(tuple -> tuple.right(tuple.right().showBanner(false)))
						.map(tuple -> tuple.right((BiConsumer<String[],OutConsole>)
														(args,console) -> tuple.right()
																			.arguments(args)
																			.console(console)
																		.build()
																			.ignite()))
						.forEach(tuple -> reply.put(tuple.left(), tuple.right()));
		reply.put("exit", (args,console) -> stopExecution=true);
		reply.put("help", (args,console) -> console.info("Available commands are: {}\n"
															+ "To exit from shell please use 'exit' command\n"
															+ "To get help with a certain command write: <command> -help"
														,getCommandList()));
		
		return reply;
	}
	
	protected List<String> splitCommands(final String[] _arguments){
		
		return Stream.of(_arguments)
							.reduce((left,right) -> String.join(" ",left,right))
								.map(String::trim)
								.filter(commandLines -> !commandLines.isEmpty())
								.map(commandLines -> commandLines.split(";"))
								.map(Arrays::asList)
								.orElseGet(Collections::emptyList)
									.stream()
										.map(String::trim)
										.filter(commandLines -> !commandLines.isEmpty())
										.collect(Collectors.toList());
	}
	protected Optional<CommandExecution> buildCommand(final String _command){
		
		return Optional.ofNullable(_command)
						.map(String::trim)
						.map(phrase -> phrase.split(" "))
						.filter(words -> words.length>0)
						.filter(words -> !words[0].trim().isEmpty())
						.map(words -> CommandExecution.from(words[0],Arrays.stream(words, 1, words.length)
																.map(String::trim)
																.filter(commArg -> !commArg.isEmpty())
																	.collect(Collectors.toList())
																		.toArray(new String[0])));
	}
	protected void executeCommand(final Map<String,BiConsumer<String[],OutConsole>> _availableCommands,final String _commands){
		
		buildCommand(_commands)
				.ifPresent(command -> {
					Optional.of(command.getName())
								.map(String::toLowerCase)
								.map(_availableCommands::get)
								.orElseThrow(() -> new UnknownCommand(command.getName(), getCommandList()))
									.accept(command.getArguments(), getShell());
				});
	}
	
	protected void batchExecution(final Map<String,BiConsumer<String[],OutConsole>> _availableCommands,final List<String> _commands){
		
		_commands.stream()
					.map(String::trim)
					.filter(command -> !command.isEmpty())
					.forEach(command -> executeCommand(_availableCommands,command));
	}
	protected void interactiveExecution(final Map<String,BiConsumer<String[],OutConsole>> _availableCommands){
		
		while(!this.stopExecution){
			getShell().info(">> ");
			Optional.ofNullable(getShell().read())
					.map(String::trim)
					.filter(command -> !command.isEmpty())
					.ifPresent(command -> executeCommand(_availableCommands,command));
		}
	}	

	@Override
	public void startup()  {
		
		final Map<String,BiConsumer<String[],OutConsole>> availableCommands=getAvailableCommands();
		final List<String> commands=getStandalone()
										.map(Standalone::getArguments)
										.map(this::splitCommands)
										.orElseThrow(NoStandaloneInstance::new);
		if(commands.isEmpty()){
			interactiveExecution(availableCommands);
		}else{
			batchExecution(availableCommands,commands);
		}
	}
}
