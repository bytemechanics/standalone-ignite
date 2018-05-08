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
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;
import org.bytemechanics.standalone.ignite.exceptions.ShutdownSystemFailure;

/**
 * Basic interface to provide a startup and shutdown hooks to avoid boilerplate
 * @author afarre
 * @since 1.0.0
 */
public interface Standalone {

	/**
	 * Override this method to implement special tasks for startup
	 * If this instance implements Runnable, then the default implementation will call Runnable::run
	 * @return The same instance
	 * @see Runnable
	 */
	public default Standalone startup(){
		if(this instanceof Runnable){
			((Runnable)this).run();
		}
		return this;
	}

	/**
	 * Override this method to implement special tasks for graceful shutdown
	 * If this instance implements Closeable, then the default implementation will call Closeable::close
	 * @return The same instance
	 * @see Closeable
	 */
	public default Standalone shutdown(){
		if(this instanceof Closeable){
			try {
				((Closeable)this).close();
			} catch (IOException e) {
				throw new ShutdownSystemFailure(e);
			}
		}
		return this;
	}
	
	/**
	 * Parse all given parameters and stores in the parameter enumeration
	 * @param <T> type of the supplied Standalone implementation instance
	 * @param <P> type of the parameters enumeration
	 * @param _instance instance of Standalone implementation
	 * @param _parameters parameters enumeration class
	 * @param _args Arguments from the command line execution
	 * @return The same instance provided
	 */
	public static <T extends Standalone,P extends Enum<? extends Parameter>> T parseParameters(final T _instance,final Class<P> _parameters,final String... _args){
		
		return _instance;
	} 
	
	/**
	 * Registers the shutdown hook to perform a graceful shutdown by calling the Standalone::shudtdown method
	 * @param <T> type of the Standalone implementation provided
	 * @param _instance instance of Standalone implementation
	 * @return The same instance provided
	 */
	public static <T extends Standalone> T addShutdownHook(final T _instance){
		Runtime
			.getRuntime()
				.addShutdownHook(new Thread() {
									@Override
									public void run(){ 
										try{			
											_instance.shutdown();
										}catch(Throwable e){
											e.printStackTrace();
										}
									}
								});
		
		return _instance;
	} 
	
	/**
	 * Call this method to ignite the standalone application
	 * @param <T> type of the supplied Standalone implementation instance
	 * @param _supplier supplier for standalone implementation
	 * @param _args Arguments from the command line execution
	 */
	public static <T extends Standalone> void ignite(final Supplier<T> _supplier,final String... _args){
		ignite(_supplier, null, _args);
	}

	/**
	 * Call this method to ignite the standalone application
	 * @param <T> type of the supplied Standalone implementation instance
	 * @param <P> type of the parameters enumeration
	 * @param _supplier supplier for standalone implementation
	 * @param _parameters parameters enumeration class
	 * @param _args Arguments from the command line execution
	 */
	public static <T extends Standalone,P extends Enum<? extends Parameter>> void ignite(final Supplier<T> _supplier,final Class<P> _parameters,final String... _args){
		
		Optional.ofNullable(_supplier)
						.map(Supplier::get)
						.map(Standalone::addShutdownHook)
						.map(standalone -> Standalone.parseParameters(standalone, _parameters, _args))
						.map(Standalone::startup)
						.orElseThrow(() -> new NullPointerException("_supplier can not be null and must provide a not null instance"));
	}
}
