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
package org.bytemechanics.standalone.ignite;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.bytemechanics.standalone.ignite.exceptions.MandatoryArgumentNotProvided;

/**
 * Interface to implement the parameters definition
 * Tipically this interface is applied to an enumeration
 * @author afarre
 */
public interface Parameter {
	
	/**
	 * Retrieve parameter name
	 * @return the parameter name
	 */
	public String name();
	/**
	 * Retrieve the parameter prefix by default: -[name()]:
	 * returns the parameter prefix
	 */
	public default String prefix(){
		return String.join("", "-",name(),":");
	}
	public Class getType(); 
	/**
	 * Returns the parser supplier
	 * @return The supplier for the parser
	 */
	public Function<String,Object> getParserSupplier();
	/**
	 * Returns the default value as string
	 * @return an optional with the default value as string
	 */
	public Optional<String> getDefaultValue();
	/**
	 * Returns the current value
	 * @return an optional of the current value
	 */
	public Optional<Object> getValue();
	/**
	 * Returns the current value casted to the given class
	 * @param <T> type of the class to be cast
	 * @param _class the class to be casted to
	 * @return an optional of the value casted to the given class
	 */
	public default <T> Optional<T> getValue(final Class<T> _class){
		return getValue()
					.map(value -> (T)value);
	}
	/**
	 * Replace the current parameter value
	 * @param _value the new value
	 * @return new parameter with the new value assigned
	 */
	public Parameter setValue(final Object _value);

	/**
	 * Look for the parameter into arguments filtering those parameters started with #prefix()
	 * @param _args provided arguments
	 * @return the value found without the prefix
	 */
	public default String findParameter(final String... _args){
		return Stream.of(_args)
						.map(arg -> arg.toLowerCase())
						.filter(arg -> arg.startsWith(this.prefix()))
						.findAny()
						.orElseGet(() -> getDefaultValue()
											.orElseThrow(() -> new MandatoryArgumentNotProvided(this)));
	}
	/**
	 * Parse the provided value with the given parser supplier
	 * @param _value value to parse
	 * @return parsed value or the same value if no parser provided
	 */
	public default Object parseParameter(final String _value){
		return Optional.ofNullable(getParserSupplier())
						.map(supplier -> supplier.apply(_value))
						.orElse(_value);
	}
	
	/**
	 * Parse all given parameters and stores in the parameter enumeration
	 * @param <P> type of the parameters enumeration
	 * @param _parameters parameters enumeration class
	 * @param _args Arguments from the command line execution
	 */
	public static <P extends Parameter> void parseParameters(final Class<P> _parameters,final String... _args){

		for(Parameter param:_parameters.getEnumConstants()){
			Optional.ofNullable(param.findParameter(_args))
						.map(value -> value.substring(param.prefix().length()))
						.map(value -> param.parseParameter(value))
						.ifPresent(object -> param.setValue(object));
		}
	} 
}
