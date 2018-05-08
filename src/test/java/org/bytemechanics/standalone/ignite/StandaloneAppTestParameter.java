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
import org.bytemechanics.standalone.ignite.beans.DefaultParameterContainer;

/**
 *
 * @author afarre
 */
public enum StandaloneAppTestParameter implements Parameter{

	BOOLEANVALUE(boolean.class),
	INTVALUE(int.class),
	LONGVALUE(long.class),
	FLOATVALUE(float.class),
	DOUBLEVALUE(double.class),
	STRINGVALUE(String.class),
	;
	
	private final DefaultParameterContainer container;
	
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type){
		this(_type,null,null);
	}
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type,final String _default){
		this(_type,_default,null);
	}
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type,final String _default,final Function<String,T> _parserSupplier){
		this.container=DefaultParameterContainer.builder()
												.parserSupplier((Function<String,Object>)_parserSupplier)
												.name(name())
												.type(_type)
												.defaultValue(_default)
											.build();
	}

	@Override
	public Class getType() {
		return this.container.getType();
	}

	@Override
	public Function<String, Object> getParserSupplier() {
		return this.container.getParserSupplier();
	}

	@Override
	public Optional<Object> getValue() {
		return this.container.getValue();
	}

	@Override
	public Parameter setValue(Object _value) {
		return this.container.setValue(_value);
	}

	@Override
	public Optional<String> getDefaultValue() {
		return this.container.getDefaultValue();
	}
}
