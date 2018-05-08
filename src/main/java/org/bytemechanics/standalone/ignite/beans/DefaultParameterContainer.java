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
import java.util.Optional;
import java.util.function.Function;
import lombok.Builder;
import org.bytemechanics.standalone.ignite.Parameter;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.internal.commons.string.GenericTextParser;

/**
 * Default implementation for parameter interface
 * @see Parameter
 * @author afarre
 */
public class DefaultParameterContainer implements Parameter{

	private final String name;
	private final Class type;
	private final String defaultValue;
	private final Function<String,Object> parserSupplier;
	private Object value;

	@Builder
	public  DefaultParameterContainer(final String name,final Class type,final Function<String,Object> parserSupplier,final String defaultValue) {
		this.name = name;
		this.type = type;
		this.defaultValue=defaultValue;
		this.parserSupplier=Optional.ofNullable(parserSupplier)
										.orElseGet(() -> newParserSupplier(type));
		this.value=Optional.ofNullable(this.defaultValue)
							.map(val -> parseParameter(val))
							.orElse(null);
	}
	
	
	
	@Override
	public String name() {
		return this.name;
	}

	@Override
	public Class getType() {
		return this.type;
	}

	protected <T> Function<String,T> newParserSupplier(final Class<T> _type){
		return LambdaUnchecker.uncheckedFunction(
									string -> ((_type.isEnum())? GenericTextParser.toValue(_type, string,_type.getName()) : GenericTextParser.toValue(_type, string))
												.orElseThrow(() -> new ParseException("",0)));
	}
	
	@Override
	public Function<String,Object> getParserSupplier() {
		return (Function<String,Object>)this.parserSupplier;
	}

	@Override
	public Optional<Object> getValue() {
		return Optional.ofNullable(this.value);
	}

	@Override
	public Parameter setValue(Object _value) {
		this.value=Optional.ofNullable(_value)
								.filter(newValue -> getType().isAssignableFrom(newValue.getClass()))
								.orElse(this.value);
		return this;
	}

	@Override
	public Optional<String> getDefaultValue() {
		return Optional.ofNullable(this.defaultValue);
	}
	
}
