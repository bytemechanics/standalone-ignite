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

import java.util.Optional;
import org.bytemechanics.standalone.ignite.exceptions.ParameterException;
import org.bytemechanics.standalone.ignite.exceptions.ShutdownSystemFailure;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;

/**
 * Basic interface to provide a startup and shutdown hooks to avoid boilerplate
 * @author afarre
 * @since 1.0.0
 */
public interface Ignitable {

	/**
	 * Override this method to implement startup exception special treatment; otherwise is rethrown.
	 * @param <T> exception type
	 * @param _exception cause
	 */
	public default <T extends ParameterException> void parameterProcessingException(final T _exception){
		Optional.ofNullable(_exception)
				.ifPresent(LambdaUnchecker.uncheckedConsumer(ex -> {throw ex;}));
	}

	/**
	 * Override this method to implement special tasks before startup
	 */
	public default void beforeStartup(){}
	
	/**
	 * Override this method to implement the ignitable content. If this instance implements Runnable, then the default implementation will call Runnable::run
	 * @see Runnable
	 */
	public default void startup(){
		if(this instanceof Runnable){
			((Runnable)this).run();
		}
	}
	/**
	 * Override this method to implement special tasks after startup
	 */
	public default void afterStartup(){}

	/**
	 * Override this method to implement startup exception special treatment; otherwise is rethrown.
	 * @param <T> exception type
	 * @param _exception cause
	 */
	public default <T extends Exception> void startupException(final T _exception){
		Optional.ofNullable(_exception)
				.ifPresent(LambdaUnchecker.uncheckedConsumer(ex -> {throw ex;}));
	}

	/**
	 * Override this method to implement special tasks before shutdown is called.
	 */
	public default void beforeShutdown(){}
	
	/**
	 * Override this method to implement special tasks for graceful shutdown.
	 * If this instance implements AutoCloseable, then the default implementation will call AutoCloseable::close
	 * @see AutoCloseable
	 */
	public default void shutdown(){
		if(this instanceof AutoCloseable){
			try {
				((AutoCloseable)this).close();
			} catch (Exception e) {
				throw new ShutdownSystemFailure(e);
			}
		}
	}

	/**
	 * Return optional of console if standalone has been recovered via getStandalone Method
	 * @return Optional of console
	 * @see Console
	 * @see Ignitable#getStandalone() 
	 * @see Ignitable#setStandalone(org.bytemechanics.standalone.ignite.Standalone) 
	 * @since 2.0.0
	 */
	public default Optional<Console> getConsole(){
		return getStandalone()
					.map(Standalone::getConsole);
	}
	
	/**
	 * Override this method in order to retrieve Standalone instance from the ignitable class
	 * By default this method allways return an empty optional. Is responsibility of the implementation 
	 * to store the standalone instance overrinding setStandalone and override this method to retrieve the instance
	 * @return An optional of standalone instance that created this ignitable instance (by default always empty implementing class must override)
	 * @see Standalone
	 * @see Ignitable#setStandalone(org.bytemechanics.standalone.ignite.Standalone) 
	 * @since 2.0.0
	 */
	public default Optional<Standalone> getStandalone(){
		return Optional.empty();
	}
	
	/**
	 * Override this method in order to get access to your Standalone instance
	 * @param _standalone standalone instance that created this ignitable instance
	 * @see Standalone
	 * @since 2.0.0
	 */
	public default void setStandalone(final Standalone _standalone){
	}
	
	/**
	 * Override this method to implement special tasks after graceful shutdown
	 */
	public default void afterShutdown(){}

	/**
	 * Override this method to implement shutdown exception special treatment; otherwise is rethrown.
	 * @param <T> exception type
	 * @param _exception cause
	 */
	public default <T extends Exception> void shutdownException(final T _exception){
		Optional.ofNullable(_exception)
				.ifPresent(LambdaUnchecker.uncheckedConsumer(ex -> {throw ex;}));
	}
}
