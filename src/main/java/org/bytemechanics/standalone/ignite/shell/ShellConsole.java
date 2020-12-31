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

import java.util.function.BiFunction;
import java.util.function.Supplier;
import org.bytemechanics.standalone.ignite.Console;
import org.bytemechanics.standalone.ignite.OutConsole;

/**
 * Standalone shell console
 * @author afarre
 * @since 2.0.0
 */
public class ShellConsole extends OutConsole implements Console {

	protected final Supplier<String> reader;

	/**
	 * Shell console constructor
	 * @param _console java standard console instance
	 * @param _formatter console message formatter
	 * @param _verboseEnabled flag to indicate if the console must print verbose messages
	 */
	public ShellConsole(final java.io.Console _console,final BiFunction<String,Object[],String> _formatter,final boolean _verboseEnabled){
		super(_formatter,_verboseEnabled,_console::format,_console::format,_console::format);
		this.reader=_console::readLine;
	}

	/**
	 * Method to recover the text writed to console by the user until next carriage return
	 * @return read message
	 */
	public String read(){
		return this.reader.get();
	}


	@Override
	public int hashCode() {
		int hash = 3;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		return getClass() == obj.getClass();
	}
}
