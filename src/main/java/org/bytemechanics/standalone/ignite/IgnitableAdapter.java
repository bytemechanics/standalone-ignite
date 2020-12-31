/*
 * Copyright 2020 Byte Mechanics.
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
package org.bytemechanics.standalone.ignite;

import java.util.Optional;

/**
 * Optional abstract class to use as alternative to the Ignitable interface
 * @see Ignitable
 * @author afarre
 * @since 2.0.0
 */
public abstract class IgnitableAdapter implements Ignitable{

	protected Standalone standalone;
	protected Console console;

	@Override
	public Optional<Console> getConsole() {
		return Optional.ofNullable(this.console);
	}
	
	@Override
	public Optional<Standalone> getStandalone() {
		return Optional.ofNullable(this.standalone);
	}
	@Override
	public void setStandalone(final Standalone _standalone) {
		this.standalone=_standalone;
		this.console=getStandalone()
						.map(Standalone::getConsole)
						.orElse(null);
	}
}
