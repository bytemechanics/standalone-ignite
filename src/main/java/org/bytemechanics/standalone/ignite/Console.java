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

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Console abstraction
 * @author afarre
 */
public interface Console {

	/**
	 * Recover console formatter
	 * @return BiFuntion console formatter that converts a string and object array to a formated message
	 */
	public BiFunction<String, Object[], String> getFormatter();
	
	/**
	 * Return if the verbose flag is enabled 
	 * @return true if verbose is enabled
	 */
	public boolean isVerboseEnabled();
	
	/**
	 * Returns a consumer that prints the error message to console
	 * @return error message consumer
	 */
	public Consumer<String> getErrorPrinter();

	/**
	 * Returns a consumer that prints the info message to console
	 * @return info message consumer
	 */
	public Consumer<String> getInfoPrinter();

	/**
	 * Returns a consumer that prints the verbose message to console
	 * @return verbose message consumer
	 */
	public Consumer<String> getVerbosePrinter();
	
	/**
	 * Print error message
	 * @param _message message to print
	 * @param _args message arguments
	 */
	public default void error(final String _message, final Object... _args){
		Optional.ofNullable(_message)
				.map(message -> getFormatter().apply(message, _args))
				.ifPresent(message -> getErrorPrinter().accept(message));
	}

	/**
	 * Print info message
	 * @param _message message to print
	 * @param _args message arguments
	 */
	public default void info(final String _message, final Object... _args){
		Optional.ofNullable(_message)
				.map(message -> getFormatter().apply(message, _args))
				.ifPresent(message -> getInfoPrinter().accept(message));
	}

	/**
	 * Print verbose message
	 * @param _message message to print
	 * @param _args message arguments
	 */
	public default void verbose(final String _message, final Object... _args){
		Optional.ofNullable(_message)
				.filter(message -> isVerboseEnabled())
				.map(message -> getFormatter().apply(message, _args))
				.ifPresent(message -> getVerbosePrinter().accept(message));
	}
}
