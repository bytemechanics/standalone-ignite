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

import java.util.function.Consumer;

/**
 * @author afarre
 */
public class Console {

	protected final Consumer<String> verbose;
	protected final Consumer<String> info;
	protected final Consumer<String> error;
	
	
	public Console(final Consumer<String> _console,final boolean _verbose){
		this((_verbose)? _console : (message -> {}),_console,_console);
	}
	public Console(final Consumer<String> _verbose,final Consumer<String> _info,final Consumer<String> _error){
		this.verbose=_verbose;
		this.info=_info;
		this.error=_error;
	}
	
	
	public void verbose(final String _message){
		this.verbose.accept(_message);
	}
	public void info(final String _message){
		this.info.accept(_message);
	}
	public void error(final String _message){
		this.error.accept(_message);
	}
}
