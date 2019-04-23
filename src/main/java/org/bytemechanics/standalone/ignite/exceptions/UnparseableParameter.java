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
package org.bytemechanics.standalone.ignite.exceptions;

import org.bytemechanics.standalone.ignite.Parameter;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 *
 * @author afarre
 */
public class UnparseableParameter extends RuntimeException{
	
	private static final String MESSAGE="Unparseable parameter {} with value {}";
	
	/**
	 * Mandatory argument not provided exception constructor
	 * @param _argument necessary argument not provided
	 * @param _value value
	 * @param _cause exception cause
	 */
	public UnparseableParameter(final Parameter _argument,final String _value,final Throwable _cause) {
		super(SimpleFormat.format(MESSAGE, _argument.name(),_value),_cause);	
	}

	/**
	 * Mandatory argument not provided exception constructor
	 * @param _argument necessary argument not provided
	 * @param _cause exception cause
	 */
	public UnparseableParameter(final Parameter  _argument,final Throwable _cause) {
		super(SimpleFormat.format("Unparseable parameter {}. {}", _argument.name(), _cause.getMessage()),_cause);	
	}
}
