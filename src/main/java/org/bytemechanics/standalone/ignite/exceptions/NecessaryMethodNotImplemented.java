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

import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Exception launched when a mandatory method has not been implemented, usually for static methods that should been overrided
 * @author afarre
 * @since 1.0.0
 */
public class NecessaryMethodNotImplemented extends RuntimeException{
	
	private static final String MESSAGE="Necessary method {} not implemeted";
	
	/**
	 * Necessary method not implemented exception constructor
	 * @param _method method that should be implemented
	 */
	public NecessaryMethodNotImplemented(final String _method) {
		super(SimpleFormat.format(MESSAGE, _method));	
	}
}
