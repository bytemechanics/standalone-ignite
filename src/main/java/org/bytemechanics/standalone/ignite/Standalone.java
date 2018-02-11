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
import java.util.function.Supplier;
import org.bytemechanics.standalone.ignite.exceptions.NecessaryMethodNotImplemented;

/**
 * Basic interface to provide a startup and shutdown hooks to avoid boilerplate
 * @author afarre
 * @since 1.0.0
 */
public interface Standalone {

	/**
	 * Override this method to implement special tasks for startup
	 */
	public default void startup(){
	}

	/**
	 * Override this method to implement special tasks for graceful shutdown
	 */
	public default void shutdown(){
	}
	
		
	/**
	 * This method should return a supplier for standalone implementation
	 * @param <T> type of the supplied Standalone implementation instance
	 * @return Standalone implementation instance supplier
	 */
	public static <T extends Standalone> Optional<Supplier<T>> getSupplier(){
		return Optional.empty();
	}
	
	/**
	 * Override this method to provide special setup at the very beginning of the standalone startup
	 */
	public static void setup(){
	}
	
	public static void main(String... _args){
		
		Standalone instance;
		
		try{			
			setup();
			instance=getSupplier()
					.map(supplier -> supplier.get())
					.orElseThrow(() -> new NecessaryMethodNotImplemented("public static <T extends Standalone> Optional<Supplier<T>> getSupplier()"));
			Runtime
				.getRuntime()
					.addShutdownHook(new Thread() {
										@Override
										public void run(){ 
											instance.shutdown();
										}
									});
		}catch(NecessaryMethodNotImplemented e){
			throw e;
		}
	}
}
