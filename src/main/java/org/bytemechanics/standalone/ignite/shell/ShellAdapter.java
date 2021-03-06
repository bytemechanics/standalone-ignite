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
import org.bytemechanics.standalone.ignite.Standalone;
import org.bytemechanics.standalone.ignite.internal.commons.functional.Tuple;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;
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

	/**
	 * Return the shell console
	 * @return the shell console
	 */
	protected ShellConsole getShell() {
		return (ShellConsole)getConsole()
					.filter(consoleInstance -> ShellConsole.class.isAssignableFrom(consoleInstance.getClass()))
					.map(consoleInstance -> consoleInstance)
					.orElseThrow(() -> new UnknownConsoleType(getConsole().orElse(null)));
	}
	
	/**
	 * Return a list of command name available separated by comma (included the standard ones)
	 * @return string with a list of available commands separated by comma
	 * @see ShellAdapter#getIgnitableShellCommands() 
	 */
	protected String getCommandList(){
		return getIgnitableShellCommands()
					.keySet()
						.stream()
							.map(Class::getSimpleName)
							.map(String::toLowerCase)
							.sorted()
						.collect(Collectors.joining(",","",",exit,help"));
	}
	
	/**
	 * Return a map with the available commands provided by getIgnitableShellCommands() and the standard commands: help and exit
	 * @return map with the command name as key and a biconsumer of arguments and shellConsole as value
	 * @see ShellAdapter#getIgnitableShellCommands() 
	 */
	protected Map<String,BiConsumer<String[],ShellConsole>> getAvailableCommands(){
		
		final Map<String,BiConsumer<String[],ShellConsole>> reply=new HashMap<>();
		
		getIgnitableShellCommands()
				.entrySet()
					.stream()
						.map(entry -> Tuple.of(entry.getKey(), entry.getValue()))
						.map(tuple -> tuple.left(tuple.left().getSimpleName()))
						.map(tuple -> tuple.left(tuple.left().toLowerCase()))
						.map(tuple -> tuple.right(tuple.right().showBanner(false)))
						.map(tuple -> tuple.right((BiConsumer<String[],ShellConsole>)
														(args,console) -> {
																		tuple.right()
																			.arguments(args)
																			.console(console)
																		.build()
																			.ignite();
																	}))
						.forEach(tuple -> reply.put(tuple.left(), tuple.right()));
		reply.put("exit", (args,console) -> stopExecution=true);
		reply.put("help", (args,console) -> console.info("Available commands are:\n"
															+ "{}"
															+ "To exit from shell please use 'exit' command\n"
															+ "To get help with a certain command write: <command> -help"
														,getIgnitableShellCommands()
																.entrySet()
																	.stream()
																		.map(entry -> SimpleFormat.format("{} - {}"
																											, entry.getKey().getSimpleName().toLowerCase()
																											, (entry.getValue().getDescription()!=null)? entry.getValue().getDescription() : 
																													(entry.getValue().getName()!=null)? entry.getValue().getName() : "no description provided"))
																		.collect(Collectors.joining("\n\t","\t","\n"))));
		
		return reply;
	}
	
	private String rebuildScapeSpaces(final String _arg){
		return Optional.ofNullable(_arg)
								.map(String::trim)
								.filter(arg -> !arg.isEmpty())
								.filter(arg -> arg.contains(" "))
								.filter(arg -> arg.contains(":"))
								.map(arg -> arg.replaceFirst("[\\:]", ":\""))
								.map(arg -> (arg.endsWith(";"))? arg.replaceFirst(";$", "\";") : arg.concat("\""))
								.orElse(_arg);
	}
	
	/**
	 * Split commands from the given arguments, to do this join all arguments and split per ; separator
	 * @param _arguments command line argument to shell process
	 * @return List of commands (with command name)
	 */
	protected List<String> splitCommands(final String[] _arguments){
		
		return Stream.of(_arguments)
							.map(this::rebuildScapeSpaces)
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
	/**
	 * Build a commandExecution for the given command
	 * @param _command command to execute (with the command name)
	 * @return Optional of CommandExecution from the given command
	 * @see CommandExecution
	 */
	protected Optional<CommandExecution> buildCommand(final String _command){
		
		return Optional.ofNullable(_command)
						.map(String::trim)
						.map(phrase -> phrase.split(" "))
						.filter(words -> words.length>0)
						.filter(words -> !words[0].trim().isEmpty())
						.map(words -> CommandExecution.from(words[0],Arrays.stream(words, 1, words.length)
																											.filter(commArg -> !commArg.isEmpty())
																								.map(String::trim)
																								.collect(Collectors.toList())
																									.toArray(new String[0])));
	}
	/**
	 * Execute the single command provided and given the available commands
	 * @param _availableCommands commands available for this shell
	 * @param _command command to execute (with the command name)
	 * @throws UnknownCommand if the command is unknown
	 */
	protected void executeCommand(final Map<String,BiConsumer<String[],ShellConsole>> _availableCommands,final String _command){
		
		buildCommand(_command)
				.ifPresent(command -> {
					Optional.of(command.getName())
								.map(String::toLowerCase)
								.map(_availableCommands::get)
								.orElseThrow(() -> new UnknownCommand(command.getName(), getCommandList()))
									.accept(command.getArguments(), getShell());
				});
	}
	
	/**
	 * Start batch execution of the given list of commands with the available commands provided
	 * @param _availableCommands commands available for this shell
	 * @param _commands list of commands to execute (with the command name)
	 */
	protected void batchExecution(final Map<String,BiConsumer<String[],ShellConsole>> _availableCommands,final List<String> _commands){
		
		_commands.stream()
					.map(String::trim)
					.peek(command -> getShell().info(">> "+command))
					.filter(command -> !command.isEmpty())
					.forEach(command -> executeCommand(_availableCommands,command));
	}
	/**
	 * Start interactive execution with the available commands provided
	 * @param _availableCommands commands available for this shell
	 */
	protected void interactiveExecution(final Map<String,BiConsumer<String[],ShellConsole>> _availableCommands){
		
		while(!this.stopExecution){
			getShell().write(">> ");
			try{
				Optional.ofNullable(getShell().read())
						.map(String::trim)
						.filter(command -> !command.isEmpty())
						.ifPresent(command -> executeCommand(_availableCommands,command));
			}catch(UnknownCommand e){
				getShell().error(e.getMessage());
			}
		}
	}	

	@Override
	public void startup()  {
		
		final Map<String,BiConsumer<String[],ShellConsole>> availableCommands=getAvailableCommands();
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
