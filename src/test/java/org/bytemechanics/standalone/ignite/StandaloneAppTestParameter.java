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

	BOOLEANVALUE(boolean.class,"boolean value"),
	INTVALUE(int.class,"int value"),
	LONGVALUE(long.class,"long value"),
	FLOATVALUE(float.class,"float value"),
	DOUBLEVALUE(double.class,"double value"),
	STRINGVALUE(String.class,"string value",val -> 
													(val.equals("semanticFailure")? 
													"semantic test error requested" 
													: null)),
	ENUMVALUE(StandaloneAppTestParameter.class,"string value"),
	;
	
	private final DefaultParameterContainer container;
	
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type,final String _description){
		this(_type,_description,null,null,null);
	}
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type,final String _description,final String _default){
		this(_type,_description,_default,null,null);
	}
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type,final String _description,final Function<Object,String> _validation){
		this(_type,_description,null,null,_validation);
	}
	<T extends Object> StandaloneAppTestParameter(final Class<T> _type,final String _description,final String _default,final Function<String,T> _parser,final Function<Object,String> _validation){
		this.container=DefaultParameterContainer.builder()
												.name(name())
												.type(_type)
												.validation(_validation)
												.description(_description)
												.defaultValue(_default)
												.parser((Function<String,Object>)_parser)
											.build();
	}

	@Override
	public Class getType() {
		return this.container.getType();
	}

	@Override
	public Function<String, Object> getParser() {
		return this.container.getParser();
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
	public Function<Object, String> getValidation() {
		return this.container.getValidation();
	}

	@Override
	public Optional<String> getDefaultValue() {
		return this.container.getDefaultValue();
	}

	@Override
	public String getDescription() {
		return this.container.getDescription();
	}
}
