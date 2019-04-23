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
public class InvalidParameter extends RuntimeException{
	
	private static final String MESSAGE="Invalid parameter {} with value {}: {}";
	
	/**
	 * Mandatory argument not provided exception constructor
	 * @param _argument necessary argument not provided
	 * @param _value value
	 * @param _reason failure cause
	 */
	public InvalidParameter(final Parameter _argument,final Object _value,final String _reason) {
		super(SimpleFormat.format(MESSAGE, _argument.name(),_value,_reason));	
	}
}
