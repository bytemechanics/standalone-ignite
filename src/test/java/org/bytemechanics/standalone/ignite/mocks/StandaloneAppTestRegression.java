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
package org.bytemechanics.standalone.ignite.mocks;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import org.bytemechanics.standalone.ignite.Parameter;
import org.bytemechanics.standalone.ignite.beans.DefaultParameterContainer;

/**
 *
 * @author afarre
 */
@SuppressWarnings("unchecked")
public enum StandaloneAppTestRegression implements Parameter{
	
	PATH(Path.class,"path"),
	PATTERNS(String.class,"patterns list","*.txt,*.bat"),
	EXCLUDE(String.class,"Exclusions list"," "),
	VERBOSE(boolean.class,"Verbose flag","false"),
	;
	
	private final DefaultParameterContainer container;
	
	<T extends Object> StandaloneAppTestRegression(final Class<T> _type,final String _description){
		this(_type,_description,null,null);
	}
	<T extends Object> StandaloneAppTestRegression(final Class<T> _type,final String _description,final String _default){
		this(_type,_description,_default,null);
	}
	<T extends Object> StandaloneAppTestRegression(final Class<T> _type,final String _description,final String _default,final Function<String,T> _parser){
		this.container=DefaultParameterContainer.builder()
												.name(name())
												.type(_type)
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
	public Optional<String> getDefaultValue() {
		return this.container.getDefaultValue();
	}

	@Override
	public String getDescription() {
		return this.container.getDescription();
	}
}
