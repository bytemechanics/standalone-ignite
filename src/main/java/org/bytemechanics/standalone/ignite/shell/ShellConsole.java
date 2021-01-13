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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytemechanics.standalone.ignite.Console;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Standalone shell console
 * @author afarre
 * @since 2.0.0
 */
public class ShellConsole implements Console {

	protected final Consumer<String> verbose;
	protected final Consumer<String> info;
	protected final Consumer<String> error;
	
	protected final Consumer<String> writer;
	protected final Supplier<String> reader;

	protected final boolean verboseEnabled;
	protected final BufferedReader inputReader;
	protected final BiFunction<String,Object[],String> formatter;

	/**
	 * Default shell console simple formatter with {} as placeholder and no verbose
	 * @since 2.0.5
	 */
	public ShellConsole(){
		this(System.console(),(message,args) -> SimpleFormat.format(message, args) ,false);
	}
	/**
	 * Shell console constructor with the default system console
	 * @param _formatter console message formatter
	 * @param _verboseEnabled flag to indicate if the console must print verbose messages
	 */
	public ShellConsole(final BiFunction<String,Object[],String> _formatter,final boolean _verboseEnabled){
		this(System.console(),_formatter,_verboseEnabled);
	}
	/**
	 * Shell console constructor
	 * @param _console java standard console instance
	 * @param _formatter console message formatter
	 * @param _verboseEnabled flag to indicate if the console must print verbose messages
	 */
	public ShellConsole(final java.io.Console _console,final BiFunction<String,Object[],String> _formatter,final boolean _verboseEnabled){
		if(_console!=null){
			this.writer=_console::printf;
			this.inputReader=null;
			this.reader=_console::readLine;
		}else{
			this.writer=System.out::println;
			this.inputReader=new BufferedReader(new InputStreamReader(System.in));
			this.reader=LambdaUnchecker.uncheckedSupplier(this.inputReader::readLine);
		}
		this.formatter=_formatter;
		this.verboseEnabled=_verboseEnabled;
		this.verbose=(message) -> write(message+"\n");
		this.info=(message) -> write(message+"\n");
		this.error=(message) -> write(message+"\n");
	}

	/**
	 * Method to write to console without new-line
	 * @since 2.0.2
	 */
	public void write(final String _message,final Object... _args){
		this.writer.accept(formatter.apply(_message, _args));
	}
	
	/**
	 * Method to recover the text writed to console by the user until next carriage return
	 * @return read message
	 */
	public String read(){
		return this.reader.get();
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

	
	@Override
	public void close(){
		if(this.inputReader!=null){
			try {
				this.inputReader.close();
			} catch (IOException ex) {
				Logger.getLogger(ShellConsole.class.getName()).log(Level.WARNING, null, ex);
			}
		}
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
