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

import java.util.Optional;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Unknown console type exception. Raised when the received console is not a ShellConsole instance
 * @author afarre
 * @since 2.0.0
 * @see ShellConsole
 */
public class UnknownConsoleType extends RuntimeException{
	
	protected static final String MESSAGE="Unknown console {} type. Expected ShellConsole instance";
	
	/**
	 * Unknown console type exception constructor
	 * @param _console received console instance
	 */
	public UnknownConsoleType(final Object _console) {
		super(SimpleFormat.format(MESSAGE, Optional.ofNullable(_console)
														.map(Object::getClass)
													.orElse(null)));	
	}
}
