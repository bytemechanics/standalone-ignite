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

import java.util.Optional;
import org.bytemechanics.standalone.ignite.Parameter;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Parameter can not be parsed because has wrong format
 * @author afarre
 * @since 1.0.0
 */
public class UnparseableParameter extends ParameterException{
	
	protected static final String MESSAGE_WITH_VALUE="Unparseable parameter {} with value {} root cause {}.";
	protected static final String MESSAGE_WITHOUT_VALUE="Unparseable parameter {} root cause {}";
	
	/**
	 * Constructor with the parameter value
	 * @param _parameter parameter not parseable
	 * @param _value value
	 * @param _cause exception cause
	 */
	public UnparseableParameter(final Parameter _parameter,final String _value,final Throwable _cause) {
		super(_parameter
				,SimpleFormat.format(MESSAGE_WITH_VALUE
										, Optional.ofNullable(_parameter)
														.map(Parameter::name)
													.orElse(null)
										,_value
										, Optional.ofNullable(_cause)
														.map(Throwable::getMessage)
													.orElse(null))
				,_cause);	
	}

	/**
	 * Constructor without the parameter value
	 * @param _parameter parameter not parseable
	 * @param _cause exception cause
	 */
	public UnparseableParameter(final Parameter  _parameter,final Throwable _cause) {
		super(_parameter
				,SimpleFormat.format(MESSAGE_WITHOUT_VALUE
										, Optional.ofNullable(_parameter)
														.map(Parameter::name)
													.orElse(null)
										, Optional.ofNullable(_cause)
														.map(Throwable::getMessage)
													.orElse(null))
				,_cause);	
	}
}
