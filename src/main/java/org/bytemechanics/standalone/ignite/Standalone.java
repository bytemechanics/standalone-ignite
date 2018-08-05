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
import org.bytemechanics.standalone.ignite.exceptions.MandatoryArgumentNotProvided;

/**
 * Standalone configuration container
 * @author afarre
 */
@Data
public class Standalone{
	
	/** supplier for standalone implementation. MANDATORY*/
	@NonNull
	private final Supplier<? extends Ignitable> supplier;
	/** parameters enumeration class. OPTIONAL */
	private final Class<? extends Enum<? extends Parameter>> parameters;
	/** Arguments from the command line execution. OPTIONAL */
	private final String[] arguments;
	/** Internal ignitable instance */
	private Ignitable instance;

	@Builder
	public Standalone(final Supplier<? extends Ignitable> supplier,final Class<? extends Enum<? extends Parameter>> parameters,final String[] arguments){
		this.supplier=supplier;
		this.arguments=((arguments==null)||arguments.length==0)? new String[0] : arguments;
		this.parameters=parameters;
		this.instance=null;
	}
	
	/** 
	 * Helper function to allow chaining without force users to return the ignitable instance. Internally calls _ignitable#beforeStartup()
	 * @param _ignitable ignitable instance
	 * @return the given ignitable instance
	 * @see Ignitable#beforeStartup() 
	 */
	private Ignitable beforeStartupFunction(final Ignitable _ignitable){
		_ignitable.beforeStartup();
		return _ignitable;
	}
	/** 
	 * Helper function to allow chaining without force users to return the ignitable instance. Internally calls _ignitable#startup()
	 * @param _ignitable ignitable instance
	 * @return the given ignitable instance
	 * @see Ignitable#startup() 
	 */
	private Ignitable startupFunction(final Ignitable _ignitable){
		_ignitable.startup();
		return _ignitable;
	}
	/** 
	 * Helper function to allow chaining without force users to return the ignitable instance. Internally calls _ignitable#afterStartup()
	 * @param _ignitable ignitable instance
	 * @return the given ignitable instance
	 * @see Ignitable#afterStartup() 
	 */
	private Ignitable afterStartupFunction(final Ignitable _ignitable){
		_ignitable.afterStartup();
		return _ignitable;
	}
	/** 
	 * Helper function to allow chaining without force users to return the ignitable instance. Internally calls _ignitable#beforeShutdown()
	 * @param _ignitable ignitable instance
	 * @return the given ignitable instance
	 * @see Ignitable#beforeShutdown() 
	 */
	private Ignitable beforeShutdownFunction(final Ignitable _ignitable){
		_ignitable.beforeShutdown();
		return _ignitable;
	}
	/** 
	 * Helper function to allow chaining without force users to return the ignitable instance. Internally calls _ignitable#shutdown()
	 * @param _ignitable ignitable instance
	 * @return the given ignitable instance
	 * @see Ignitable#shutdown() 
	 */
	private Ignitable shutdownFunction(final Ignitable _ignitable){
		_ignitable.shutdown();
		return _ignitable;
	}
	/** 
	 * Helper function to allow chaining without force users to return the ignitable instance. Internally calls _ignitable#afterShutdown()
	 * @param _ignitable ignitable instance
	 * @return the given ignitable instance
	 * @see Ignitable#afterShutdown() 
	 */
	private Ignitable afterShutdownFunction(final Ignitable _ignitable){
		_ignitable.afterShutdown();
		return _ignitable;
	}
	
	/**
	 * Instantiate ignitable from supplier and stores in an internal variable
	 * @return Standalone instance
	 */
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
					.map(this::beforeStartupFunction)
					.map(this::startupFunction)
					.ifPresent(this::afterStartupFunction);
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
					.map(this::beforeShutdownFunction)
					.map(this::shutdownFunction)
					.ifPresent(this::afterShutdownFunction);
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
				.addShutdownHook(new Thread(() -> this.shutdown()));
		
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
		
		try{
			Optional.of(this)
					.map(Standalone::instantiate)
					.map(Standalone::addShutdownHook)
					.map(Standalone::parseParameters)
					.map(Standalone::startup)
					//.map(config -> (!config.isDaemon())? config.shutdown() : config)
					.orElseThrow(() -> new NullPointerException("_configuration can not be null"));
		}catch(MandatoryArgumentNotProvided e){
			System.out.println(e.getMessage());
			System.out.println(Parameter.getHelp(this.parameters));
		}
	}
}
