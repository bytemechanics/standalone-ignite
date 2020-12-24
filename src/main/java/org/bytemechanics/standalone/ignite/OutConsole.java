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

import java.util.function.BiFunction;
import java.util.function.Consumer;
import org.bytemechanics.standalone.ignite.Console;

/**
 * Standalone default output console implementation
 * @author afarre
 */
public class OutConsole implements Console {

	protected final Consumer<String> verbose;
	protected final Consumer<String> info;
	protected final Consumer<String> error;

	protected final boolean verboseEnabled;
	protected final BiFunction<String,Object[],String> formatter;
	
	
	/**
	 * Out console constructor
	 * @param _console another console instance
	 * @param _verboseEnabled flag to indicate if the console must print verbose messages
	 */
	public OutConsole(final Console _console,final boolean _verboseEnabled){
		this(_console.getFormatter(),_verboseEnabled,_console.getVerbosePrinter(),_console.getInfoPrinter(),_console.getErrorPrinter());
	}
	/**
	 * Out console constructor
	 * @param _console consumer to write message to console
	 * @param _formatter console message formatter
	 * @param _verboseEnabled flag to indicate if the console must print verbose messages
	 */
	public OutConsole(final Consumer<String> _console,final BiFunction<String,Object[],String> _formatter,final boolean _verboseEnabled){
		this(_formatter,_verboseEnabled,_console,_console,_console);
	}
	/**
	 * Out console constructor
	 * @param _formatter console message formatter
	 * @param _verboseEnabled flag to indicate if the console must print verbose messages
	 * @param _verbose consumer to write verbose messages to console
	 * @param _info consumer to write info messages to console
	 * @param _error consumer to write error messages to console
	 */
	public OutConsole(final BiFunction<String,Object[],String> _formatter,final boolean _verboseEnabled,final Consumer<String> _verbose,final Consumer<String> _info,final Consumer<String> _error){
		this.formatter=_formatter;
		this.verboseEnabled=_verboseEnabled;
		this.verbose=_verbose;
		this.info=_info;
		this.error=_error;
	}

	@Override
	public BiFunction<String, Object[], String> getFormatter() {
		return this.formatter;
	}
	
	@Override
	public boolean isVerboseEnabled(){
		return this.verboseEnabled;
	}
	
	@Override
	public Consumer<String> getErrorPrinter(){
		return this.error;
	}
	@Override
	public Consumer<String> getInfoPrinter(){
		return this.info;
	}
	@Override
	public Consumer<String> getVerbosePrinter(){
		return this.verbose;
	}
}
