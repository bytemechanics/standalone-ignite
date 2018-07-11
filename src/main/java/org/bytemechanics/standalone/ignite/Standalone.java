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
package org.bytemechanics.standalone.ignite;

import java.io.Closeable;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * Standalone configuration container
 * @author afarre
 * @param <P> parameters class type
 */
@Data
@Builder
public class Standalone<P extends Enum & Parameter> implements Runnable{
	
	/** supplier for standalone implementation. MANDATORY*/
	@NonNull
	private final Supplier<? extends Ignitable> supplier;
	/** parameters enumeration class. OPTIONAL */
	private final Class<P> parameters;
	/** Arguments from the command line execution. OPTIONAL */
	@Builder.Default
	private final String[] arguments=new String[0];
	/** daemon flag, the main class has an infinite loop, therefore close method won't be called after right after startup. OPTIONAL (default false)*/
	private final boolean daemon;
	@Builder.Default
	/** Internal ignitable instance */
	private Ignitable instance=null;
	
	
	protected Standalone instantiate(){
		this.instance=Optional.ofNullable(this.supplier)
								.map(Supplier::get)
								.orElseThrow(() -> new NullPointerException("Supplier can not be null and must provide a not null instance"));
		return this;
	}
	

	/**
	 * Override this method to implement special tasks for startup
	 * If this instance implements Runnable, then the default implementation will call Runnable::run
	 * @return itself
	 * @see Runnable
	 */
	protected Standalone startup(){
		Optional.ofNullable(this.instance)
					.map(Ignitable::beforeStartup)
					.map(Ignitable::startup)
					.ifPresent(Ignitable::afterStartup);
		return this;
	}

	/**
	 * Override this method to implement special tasks for graceful shutdown
	 * If this instance implements Closeable, then the default implementation will call Closeable::close
	 * @return itself
	 * @see Closeable
	 */
	protected Standalone shutdown(){
		Optional.ofNullable(this.instance)
					.map(Ignitable::beforeShutdown)
					.map(Ignitable::shutdown)
					.ifPresent(Ignitable::afterShutdown);
		return this;
	}
	
	/**
	 * Parse all given parameters and stores in the parameter enumeration
	 * @return itself
	 */
	protected Standalone parseParameters(){
		
		Optional.ofNullable(this.parameters)
					.ifPresent(par -> Parameter.parseParameters(par, arguments));
		return this;
	} 
	
	/**
	 * Registers the shutdown hook to perform a graceful shutdown by calling the Standalone::shudtdown method
	 * @return The same instance provided
	 */
	protected Standalone addShutdownHook(){
		
		final Standalone self=this;
		Runtime
			.getRuntime()
				.addShutdownHook(new Thread(this));
		
		return self;
	} 
	
	/**
	 * Call this method to ignite the standalone application this method will:
	 * <ul>
	 *   <li>Get standalone instance</li>
	 *   <li>Register shutdown hook</li>
	 *   <li>Call startup</li>
	 *   <li>Call shutdown (if daemon is false)</li>
	 * </ul>
	 * @see Standalone
	 */
	public void ignite(){
		
		Optional.of(this)
				.map(Standalone::instantiate)
				.map(Standalone::addShutdownHook)
				.map(Standalone::parseParameters)
				.map(Standalone::startup)
				.map(config -> (!config.isDaemon())? config.shutdown() : config)
				.orElseThrow(() -> new NullPointerException("_configuration can not be null"));

	}

	@Override
	public void run() {
		try{
			this.instance.shutdown();
		}catch(Throwable e){
			e.printStackTrace();
		}
	}
}
