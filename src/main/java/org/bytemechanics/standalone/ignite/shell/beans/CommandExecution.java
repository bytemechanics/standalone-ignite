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

/**
 *
 * @author afarre
 */
public class CommandExecution {
	
	private final String name;
	private final String[] arguments;
	
	public CommandExecution(final String _name, final String[] _arguments){
		this.name=_name;
		this.arguments=_arguments;
	}

	public String getName() {
		return name;
	}
	public String[] getArguments() {
		return arguments;
	}
	
	public static CommandExecution from(final String _name, final String[] _arguments){
		return new CommandExecution(_name, _arguments);
	}
}
