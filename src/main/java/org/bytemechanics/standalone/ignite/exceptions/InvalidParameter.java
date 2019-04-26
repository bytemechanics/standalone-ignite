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
 * Parameter is not valid by semantic reasons
 * @author afarre
 * @since 1.1.1
 */
public class InvalidParameter extends ParameterException{
	
	private static final String MESSAGE="Invalid parameter {} with value {}: {}";
	
	/**
	 * Invalid parameter
	 * @param _parameter invalid parameter
	 * @param _value value
	 * @param _reason failure cause
	 */
	public InvalidParameter(final Parameter _parameter,final Object _value,final String _reason) {
		super(_parameter,SimpleFormat.format(MESSAGE, _parameter.name(),_value,_reason));	
	}
}
