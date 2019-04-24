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
package org.bytemechanics.standalone.ignite.beans;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.bytemechanics.standalone.ignite.Parameter;
import org.bytemechanics.standalone.ignite.internal.EnumParseExceptionParameter;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.internal.commons.reflection.PrimitiveTypeConverter;
import org.bytemechanics.standalone.ignite.internal.commons.string.GenericTextParser;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Default implementation for parameter interface
 * @see Parameter
 * @author afarre
 */
public class DefaultParameterContainer implements Parameter{

	private final String name;
	private final String description;
	private final String[] prefixes;
	private final Class<? extends Object> type;
	private final String defaultValue;
	private final Function<String,Object> parser;
	private final Function<Object,String> validation;
	private final boolean caseSensitive;
	private Object value;

	/**
	 * Complete constructor for this container, preferably use the existent builder
	 * @param name parameter name (mandatory)
	 * @param type parameter class (mandatory)
	 * @param description parameter description (mandatory)
	 * @param parser parser supplier
	 * @param validation validation 
	 * @param caseSensitive flag to indicate that enum parameters parse must be case sensitive. (by default case sensitive)
	 * @param defaultValue default value
	 * @param prefixes prefixes available to use for this parameter
	 */
	public DefaultParameterContainer(final String name,final Class<? extends Object> type,final String description,final Function<String,Object> parser,final Function<Object,String> validation,final boolean caseSensitive,final String defaultValue,final String... prefixes) {
		if(name==null)
			throw new NullPointerException("Mandatory \"name\" can not be null");
		this.name = name;
		if(description==null)
			throw new NullPointerException("Mandatory \"description\" can not be null");
		this.description=description;
		if(type==null)
			throw new NullPointerException("Mandatory \"type\" can not be null");
		this.type = PrimitiveTypeConverter
								.convert(type);
		this.defaultValue=defaultValue;
		this.caseSensitive=caseSensitive;
		this.validation=validation;
		this.parser=Optional.ofNullable(parser)
										.orElseGet(() -> getDefaultParser(this.name,(Class<Object>)this.type,this.caseSensitive));
		this.value=Optional.ofNullable(this.defaultValue)
							.map(this::parseParameter)
							.orElse(null);
		this.prefixes=Optional.ofNullable(prefixes)
								.filter(prefix -> prefix.length>0)
								.orElse(Parameter.super.getPrefixes());
	}
	
	
	private <T> T buildCustomParser(final String _name,final Class<T> _type,final String _value,final boolean _isCaseSensitive) throws ParseException{
		if(_type.isEnum()){
			if(_isCaseSensitive){
				return GenericTextParser.toValue(_type, _value,_type.getName())
										.orElseThrow(() -> new EnumParseExceptionParameter(_value,_type));
			}else{
				return Stream.of(_type.getEnumConstants())
								.map(enumConstant -> (Enum)enumConstant)
								.filter(enumConstant -> enumConstant.name().equalsIgnoreCase(_value))
								.findAny()
									.map(enumConstant -> (T)enumConstant)
									.orElseThrow(() -> new EnumParseExceptionParameter(_value,_type));
			}
		}else{
			return GenericTextParser.toValue(_type, _value)
									.orElseThrow(() -> new ParseException(SimpleFormat.format("Unable to parse value {} for parameter {}",_value,_name),0));
		}
	}
	
	/**
	 * Returns the default parser provider from the given class, this provider throws a ParseException if is not possible to parse the value
	 * @param <T> type to parse
	 * @param _name parameter name to parse
	 * @param _type class to parse
	 * @param _isCaseSensitive flag to indicate that the returning parser must be case sensitive
	 * @return Parser provider for the given class
	 */
	protected <T> Function<String,T> getDefaultParser(final String _name,final Class<T> _type,final boolean _isCaseSensitive){
		return LambdaUnchecker.uncheckedFunction( string ->	buildCustomParser(_name,_type,string, _isCaseSensitive));
										
	}

	/**
	 * @see Parameter#name() 
	 */
	@Override
	public String name() {
		return this.name;
	}

	/**
	 * @see Parameter#getType() 
	 */
	@Override
	public Class getType() {
		return this.type;
	}

	/**
	 * @see Parameter#getDescription() 
	 */
	@Override
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * @see Parameter#getPrefixes() 
	 */
	@Override
	public String[] getPrefixes() {
		return this.prefixes;
	}

	/**
	 * @see Parameter#getParser() 
	 */
	@Override
	public Function<String,Object> getParser() {
		return this.parser;
	}

	@Override
	public Function<Object, String> getValidation() {
		return this.validation;
	}

	/**
	 * @see Parameter#getValue() 
	 */
	@Override
	public Optional<Object> getValue() {
		return Optional.ofNullable(this.value);
	}

	/**
	 * Flag to indicate if the parser must be case sensitive or unsensitive (only for Enum parameters)
	 * @return if the parser must be case sensitive or unsensitive
	 */
	public boolean isCaseSensitive() {
		return this.caseSensitive;
	}

	/**
	 * @see Parameter#setValue(java.lang.Object) 
	 */
	@Override
	public Parameter setValue(Object _value) {
		this.value=Optional.ofNullable(_value)
								.filter(newValue -> getType().isAssignableFrom(newValue.getClass()))
								.orElse(this.value);
		return this;
	}

	/**
	 * @see Parameter#getDefaultValue() 
	 */
	@Override
	public Optional<String> getDefaultValue() {
		return Optional.ofNullable(this.defaultValue);
	}


	public static class DefaultParameterContainerBuilder {

		private String name;
		private Class<? extends Object> type;
		private String description;
		private Function<String, Object> parser;
		private Function<Object,String> validation;
		private String defaultValue;
		private String[] prefixes;
		private boolean caseSensitive;

		DefaultParameterContainerBuilder() {
		}

		public DefaultParameterContainerBuilder name(final String name) {
			this.name = name;
			return this;
		}
		public DefaultParameterContainerBuilder type(final Class<? extends Object> type) {
			this.type = type;
			return this;
		}
		public DefaultParameterContainerBuilder description(final String description) {
			this.description = description;
			return this;
		}
		public DefaultParameterContainerBuilder parser(final Function<String, Object> parser) {
			this.parser = parser;
			return this;
		}
		public DefaultParameterContainerBuilder validation(final Function<Object,String> validation) {
			this.validation = validation;
			return this;
		}
		public DefaultParameterContainerBuilder defaultValue(final String defaultValue) {
			this.defaultValue = defaultValue;
			return this;
		}
		public DefaultParameterContainerBuilder caseSensitive(boolean caseSensitive) {
			this.caseSensitive = caseSensitive;
			return this;
		}
		public DefaultParameterContainerBuilder prefixes(final String[] prefixes) {
			this.prefixes = prefixes;
			return this;
		}

		public DefaultParameterContainer build() {
			return new DefaultParameterContainer(name, type, description, parser,validation,caseSensitive, defaultValue, prefixes);
		}

		@Override
		public java.lang.String toString() {
			return SimpleFormat.format("DefaultParameterContainer.DefaultParameterContainerBuilder(name={}, type={}, description={}, parser={}, validation={}, caseSensitive={}, defaultValue={}, prefixes={})", 
										this.name ,this.type,this.description,this.parser,this.validation,this.caseSensitive,this.defaultValue,Arrays.deepToString(this.prefixes));
		}
	}

	public static DefaultParameterContainerBuilder builder() {
		return new DefaultParameterContainerBuilder();
	}	
}
