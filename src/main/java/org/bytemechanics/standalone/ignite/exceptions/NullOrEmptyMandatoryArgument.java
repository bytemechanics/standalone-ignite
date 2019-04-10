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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bytemechanics.standalone.ignite.Parameter;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 *
 * @author afarre
 */
public class NullOrEmptyMandatoryArgument extends RuntimeException{
	
	private static final String MESSAGE="Mandatory argument {} is null or empty";
	
	/**
	 * Mandatory argument not provided exception constructor
	 * @param _argument necessary argument not provided
	 */
	public NullOrEmptyMandatoryArgument(final Parameter _argument) {
		super(SimpleFormat.format(MESSAGE, _argument.name(),Stream.of(_argument.getPrefixes())
																	.map(prefix -> String.valueOf(prefix)+":")
																	.collect(Collectors.toList())));	
	}
}
