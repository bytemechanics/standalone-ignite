/*
 * Copyright 2019 Byte Mechanics.
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

/**
 * Generic exception over parameters
 * @author afarre
 * @since 1.1.2
 */
public class ParameterException extends RuntimeException{
	
	private final Parameter parameter;
			
	/**
	 * Constructor without cause
	 * @param _parameter parameter that has generated the exception
	 * @param _message error message
	 */
	public ParameterException(final Parameter _parameter,final String _message){
		super(_message);
		this.parameter=_parameter;
	}
	/**
	 * Constructor with cause
	 * @param _parameter parameter that has generated the exception
	 * @param _message error message
	 * @param _cause error cause
	 */
	public ParameterException(final Parameter _parameter,final String _message,final Throwable _cause){
		super(_message,_cause);
		this.parameter=_parameter;
	}

	/**
	 * Returns the parameter that has generated the exception
	 * @return parameter
	 * @see Parameter
	 */
	public Parameter getParameter() {
		return parameter;
	}
}
