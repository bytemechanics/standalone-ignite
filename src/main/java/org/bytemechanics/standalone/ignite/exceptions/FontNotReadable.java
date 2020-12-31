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

import java.net.URL;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Exception to report that provided font url is not valid
 * @author afarre
 */
public class FontNotReadable extends RuntimeException{
	
	protected static final String MESSAGE="Font {} not readable or not found";
	
	/**
	 * Mandatory argument not provided exception constructor
	 * @param _fontURL font url
	 * @param _cause exception cause
	 */
	public FontNotReadable(final URL _fontURL,final Throwable _cause) {
		super(SimpleFormat.format(MESSAGE, _fontURL),_cause);	
	}
}
