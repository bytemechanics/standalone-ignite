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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bytemechanics.standalone.ignite.exceptions.FontNotReadable;
import org.bytemechanics.standalone.ignite.exceptions.MandatoryParameterNotProvided;
import org.bytemechanics.standalone.ignite.exceptions.ParameterException;
import org.bytemechanics.standalone.ignite.internal.commons.string.Figlet;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Standalone configuration container
 * @author afarre
 */
public class Standalone{

	/** Latest standalone instantiated */
	private static Standalone self=null; 
	
	
	/** Standalone name. OPTIONAL*/
	private final String name;
	/** Banner font. OPTIONAL*/
	private final URL bannerFont;
	/** supplier for standalone implementation. MANDATORY*/
	private final Supplier<Ignitable> supplier;
	/** parameters enumeration class. OPTIONAL */
	private final List<Class<? extends Enum<? extends Parameter>>> parameters;
	/** Arguments from the command line execution. OPTIONAL */
	private final String[] arguments;
	/** console consumer by default java.util.logging. OPTIONAL*/
	private final Consumer<String> console;

	/** Internal ignitable instance */
	private Ignitable instance;

	
	protected Standalone(final Supplier<Ignitable> _supplier,final String _name,final List<Class<? extends Enum<? extends Parameter>>> _parameters,final String[] _arguments,final Consumer<String> _console,final URL _bannerFont){
		if(_supplier==null)
			throw new NullPointerException("Mandatory \"supplier\" can not be null");
		this.name=_name;
		this.bannerFont=(_bannerFont!=null)? _bannerFont : ClassLoader.getSystemResource("standard.flf");
		this.supplier=_supplier;
		this.arguments=((_arguments==null)||_arguments.length==0)? new String[0] : _arguments;
		this.parameters=_parameters;
		this.instance=null;
		this.console=(_console!=null)? _console : getDefaultConsole();
	}
	
	
	/**
	 * Initializes java logging to create a CONSOLE output without prefixes
	 * @return The same instance provided
	 */
	private Consumer<String> getDefaultConsole(){
		
		final Logger logger=Logger.getLogger("CONSOLE");
		
		logger.setUseParentHandlers(false);
		logger.addHandler(new Handler() {
			@Override
			public void publish(LogRecord record) {
				System.out.println(record.getMessage());
			}
			@Override
			public void flush() {
				System.out.flush();
			}
			@Override
			public void close() {
				// Do nothing because System.out can not be closed
			}
		});
		logger.setLevel(Level.INFO);
		
		return logger::info;
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
		
		final Standalone reply=this;
		
		this.instance=Optional.ofNullable(this.supplier)
								.map(Supplier::get)
								.orElseThrow(() -> new NullPointerException("Supplier can not be null and must provide a not null instance"));
		return reply;
	}
	

	/**
	 * Override this method to implement special tasks for startup
	 * If this instance implements Runnable, then the default implementation will call Runnable::run
	 * @return itself
	 * @see Runnable
	 */
	protected Standalone startup(){
		
		final Standalone reply=this;
		
		try{
			Optional.ofNullable(this.instance)
					.map(this::beforeStartupFunction)
					.map(this::startupFunction)
					.ifPresent(this::afterStartupFunction);
		}catch(Exception e){
			this.instance.startupException(e);
		}
		
		return reply;
	}

	/**
	 * Override this method to implement special tasks for graceful shutdown
	 * If this instance implements Closeable, then the default implementation will call Closeable::close
	 * @return itself
	 * @see Closeable
	 */
	protected Standalone shutdown(){
		
		final Standalone reply=this;
		
		try{
			Optional.ofNullable(this.instance)
					.map(this::beforeShutdownFunction)
					.map(this::shutdownFunction)
					.ifPresent(this::afterShutdownFunction);
		}catch(Exception e){
			this.instance.shutdownException(e);
		}

		return reply;
	}
	
	/**
	 * Parse all given parameters and stores in the parameter enumeration
	 * @return itself
	 */
	protected Standalone parseParameters(){
		
		final Standalone reply=this;
		
		try{
			this.parameters.stream()
							.filter(Objects::nonNull)
							.forEach(par -> Parameter.parseParameters(par, arguments));
		}catch(final ParameterException e){
			this.instance.parameterProcessingException(e);
		}
		
		return reply;
	} 

	protected Standalone validateParameters(){
		
		final Standalone reply=this;

		try{		
			this.parameters.stream()
							.filter(Objects::nonNull)
							.forEach(Parameter::validateParameters);
		}catch(final ParameterException e){
			this.instance.parameterProcessingException(e);
		}

		return reply;
	} 
	
	/**
	 * Registers the shutdown hook to perform a graceful shutdown by calling the Standalone::shudtdown method
	 * @return The same instance provided
	 */
	protected Standalone addShutdownHook(){
		
		final Standalone reply=this;
		
		Runtime
			.getRuntime()
				.addShutdownHook(new Thread(this::shutdown));
		
		return reply;
	} 

	/**
	 * Prints banner into CONSOLE logger at INFO level if name is informed
	 * @return The same instance provided
	 */
	protected Standalone printBanner(){
		
		final Standalone reply=this;
		
		if(this.name!=null){
			try(InputStream font=this.bannerFont.openStream()){
				Figlet figlet=new Figlet(font, Charset.forName("UTF-8"));
				String banner=figlet.print(this.name);
				this.console.accept(figlet.line(this.name,'='));
				this.console.accept(banner);
				this.console.accept(figlet.line(this.name,'-'));
				this.console.accept(SimpleFormat.format("\tJVM: {}",System.getProperty("java.version")));
				this.console.accept(SimpleFormat.format("\tCores: {}",Runtime.getRuntime().availableProcessors()));
				this.console.accept(SimpleFormat.format("\tMemory (bytes): {}/{}",Runtime.getRuntime().totalMemory(),Runtime.getRuntime().maxMemory()));
				this.console.accept(SimpleFormat.format("\tBase path: {}", new File(".").getCanonicalPath()));
				this.console.accept(SimpleFormat.format("\tVersion: {}/{}",
															Optional.of(this.instance)
																		.map(Object::getClass)
																		.map(Class::getPackage)
																		.map(Package::getSpecificationVersion)
																		.orElse("unknown"),
															Optional.of(this.instance)
																		.map(Object::getClass)
																		.map(Class::getPackage)
																		.map(Package::getImplementationVersion)
																		.orElse("unknown")));
				this.console.accept(figlet.line(this.name,'='));
			} catch (IOException e) {
				throw new FontNotReadable(this.bannerFont,e);
			}
		}
		
		return reply;
	} 
	
	/**
	 * Call this method to ignite the standalone application this method will:
	 * <ul>
	 *   <li>Get standalone instance</li>
	 *   <li>Register shutdown hook</li>
	 *   <li>Call startup</li>
	 * </ul>
	 * @return Standalone instance
	 * @see Standalone
	 */
	public Standalone ignite(){
		
		final Standalone reply=this;
		
		try{
			instantiate()
				.addShutdownHook()
					.parseParameters()
						.validateParameters()
							.printBanner()
								.startup();
		}catch(MandatoryParameterNotProvided e){
			this.console.accept(e.getMessage());
			this.console.accept(Parameter.getHelp(this.parameters));
		}
		
		return reply;
	}

	/**
	 * Call this method to programatically shutdown() the application. 
	 * @param _exitCode exit code to use when shutdown application this method force current jvm execution termination
	 * @see Standalone#shutdown() 
	 * @see System#exit(int) 
	 * @since 1.1.0
	 */	
	public void extinguish(final int _exitCode){
		shutdown();
		System.exit(_exitCode);
	}
	
	/**
	 * Standalone name. If present banner is printed at console. OPTIONAL
	 * @return Standalone name. OPTIONAL
	 */
	protected String getName() {
		return this.name;
	}

	/**
	 * Supplier for standalone implementation. MANDATORY
	 * @return supplier for standalone implementation. MANDATORY
	 */
	protected Supplier<Ignitable> getSupplier() {
		return this.supplier;
	}
	/**
	 * Parameters enumeration classes. OPTIONAL
	 * @return list of parameters enumeration class. OPTIONAL
	 */
	public List<Class<? extends Enum<? extends Parameter>>> getParameters() {
		return this.parameters;
	}
	/**
	 * Arguments from the command line execution. OPTIONAL
	 * @return arguments from the command line execution. OPTIONAL
	 */
	public String[] getArguments() {
		return this.arguments;
	}
	/**
	 * Internal ignitable instance
	 * @return internal ignitable instance
	 */
	protected Ignitable getInstance() {
		return this.instance;
	}


	@java.lang.SuppressWarnings("all")
	public static class StandaloneBuilder {

		private final List<Class<? extends Enum<? extends Parameter>>> parameters;
		private String name;
		private URL bannerFont;
		private Supplier<Ignitable> supplier;
		private String[] arguments;
		private Consumer<String> console;

		StandaloneBuilder(){
			this.parameters=new ArrayList<>();
		}
		
		public StandaloneBuilder name(final String _name) {
			this.name = _name;
			return this;
		}
		public StandaloneBuilder bannerFont(final URL _bannerFont) {
			this.bannerFont = _bannerFont;
			return this;
		}
		public StandaloneBuilder supplier(final Supplier<Ignitable> _supplier) {
			this.supplier = _supplier;
			return this;
		}
		public StandaloneBuilder parameters(final Class<? extends Enum<? extends Parameter>> _parameters) {
			this.parameters.add(_parameters);
			return this;
		}
		public StandaloneBuilder arguments(final String[] _arguments) {
			this.arguments = _arguments;
			return this;
		}
		public StandaloneBuilder console(final Consumer<String> _console) {
			this.console = _console;
			return this;
		}

		public Standalone build() {
			Standalone.self=new Standalone(supplier,name,Collections.unmodifiableList(parameters), arguments,console,bannerFont);
			return Standalone.self;
		}
	}

	/**
	 * Returns Standalone instance builder
	 * @return standalone builder
	 */
	public static StandaloneBuilder builder() {
		return new StandaloneBuilder();
	}
	
	/**
	 * Extinguish the <b>latest</b> Standalone instance created. 
	 * If no instance exist or has been created no action is done.
	 * @param _returnCode return code to pass to extinguish method
	 * @see Standalone#extinguish(int) 
	 */
	public static void selfExtinguish(final int _returnCode){
		Optional.ofNullable(Standalone.self)
				.ifPresent(standaloneInstance -> standaloneInstance.extinguish(_returnCode));
	}
}
