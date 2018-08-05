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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.standalone.ignite.exceptions.MandatoryArgumentNotProvided;
import org.bytemechanics.standalone.ignite.exceptions.UnparseableParameter;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

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
	 * Retrieve the parameter prefixes, can have more than one (by default only: -[name()]:)
	 * @return the parameter prefixes
	 */
	public default String[] getPrefixes(){
		return new String[]{String.join("", "-",name().toLowerCase())};
	}
	/**
	 * Retrieve the class of this parameter
	 * @return the parameter class
	 */
	public Class getType(); 
	/**
	 * Retrieve the parser to use
	 * @return The parser to use
	 */
	public Function<String,Object> getParser();
	/**
	 * Retrieve the default value as string
	 * @return an optional with the default value as string
	 */
	public Optional<String> getDefaultValue();
	/**
	 * Retrieve mandatory flag for this parameter
	 * @return the mandatory flag for this parameter (if defaultValue is not informed)
	 */
	public default boolean isMandatory(){
		return !getDefaultValue().isPresent();
	}
	
	/**
	 * Returns the description for this parameter
	 * @return returns the description of this parameter
	 */
	public String getDescription();

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
	 * Look for the parameter into arguments filtering those parameters started with #getPrefix()
	 * @param _args provided arguments
	 * @return the value found without the getPrefix
	 */
	public default String findParameter(final String... _args){
		return Stream.of(_args)
						.filter(arg -> Stream.of(getPrefixes())
											.map(prefix -> prefix+":")
											.anyMatch(arg::startsWith))
						.map(value -> value.substring(value.indexOf(':')+1))
						.findAny()
						.orElseGet(() -> getDefaultValue()
											.orElseThrow(() -> new MandatoryArgumentNotProvided(this)));
	}
	/**
	 * Parse the provided value with the given parser supplier
	 * @param _value value to parse
	 * @return parsed value or the same value if no parser provided
	 * @throws UnparseableParameter if can not be parsed
	 */
	public default Object parseParameter(final String _value){
		try{
			return Optional.ofNullable(getParser())
							.map(supplier -> supplier.apply(_value))
							.orElse(_value);
		}catch(Exception e){
			throw new UnparseableParameter(this, _value, e);
		}
	}

	/**
	 * Search into arguments the parameter, parse and assign as value
	 * @param _args arguments where search the parameter
	 */
	public default void loadParameter(final String... _args){
		Optional.ofNullable(findParameter(_args))
						.map(this::parseParameter)
						.ifPresent(this::setValue);
	}

	/**
	 * Returns default help for this parameter
	 * @return default help
	 */
	public default String getHelp(){
		return SimpleFormat.format("[{}]: {} ({})"
												,Stream.of(getPrefixes())
													.collect(Collectors.joining(", "))
												,getDescription()
												,getDefaultValue()
														.map(def -> String.join(": ", "Default",def))
														.orElse("Mandatory"));
	}
	
	/**
	 * Parse all given parameters and stores in the parameter enumeration
	 * @param _parameters parameters enumeration class
	 * @param _args Arguments from the command line execution
	 */
	public static void parseParameters(final Class<? extends Enum<? extends Parameter>> _parameters,final String... _args){

		Stream.of(_parameters.getEnumConstants())
					.map(param -> (Parameter)param)
					.forEach(param -> param.loadParameter(_args));
	} 

	/**
	 * Returns the default help for all parameters of the given parameter class
	 * @param _parameters parameters enumeration class
	 * @return returns the list of 
	 */
	public static String getHelp(final Class<? extends Enum<? extends Parameter>> _parameters){

		return Stream.of(_parameters.getEnumConstants())
							.map(param -> (Parameter)param)
							.map(param -> param.getHelp())
							.collect(Collectors.joining("\n\t","Usage:\n\t","\n"));
	} 
}
