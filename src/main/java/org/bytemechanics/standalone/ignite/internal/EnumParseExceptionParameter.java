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
package org.bytemechanics.standalone.ignite.internal;

import java.util.Arrays;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Parse exceptions for enums
 * @author afarre
 */
public class EnumParseExceptionParameter extends RuntimeException{
	
	private static final String MESSAGE="Unable to parse value {} valid values are {}";
	
	/**
	 * Mandatory argument not provided exception constructor
	 * @param _value value
	 * @param _enumerated enumerated class
	 */
	public EnumParseExceptionParameter(final String _value,final Class _enumerated) {
		super(SimpleFormat.format(MESSAGE, _value,Arrays.toString(_enumerated.getEnumConstants())));	
	}
}
