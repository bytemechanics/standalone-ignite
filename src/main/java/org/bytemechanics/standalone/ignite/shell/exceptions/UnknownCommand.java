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
package org.bytemechanics.standalone.ignite.shell.exceptions;

import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Unknown command
 * @author afarre
 * @since 2.0.0
 */
public class UnknownCommand extends RuntimeException{
	
	protected static final String MESSAGE="Unknown command {} available commands are {}";
	
	/**
	 * Unknown command exception constructor
	 * @param _command command tried to execute
	 * @param _availableCommands available commands
	 */
	public UnknownCommand(final String _command,final String _availableCommands) {
		super(SimpleFormat.format(MESSAGE, _command,_availableCommands));	
	}
}
