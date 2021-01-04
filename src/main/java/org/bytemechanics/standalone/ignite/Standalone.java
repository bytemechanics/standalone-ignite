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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.bytemechanics.standalone.ignite.exceptions.FontNotReadable;
import org.bytemechanics.standalone.ignite.exceptions.MandatoryIgnitableSupplier;
import org.bytemechanics.standalone.ignite.exceptions.MandatoryParameterNotProvided;
import org.bytemechanics.standalone.ignite.exceptions.ParameterException;
import org.bytemechanics.standalone.ignite.internal.commons.functional.LambdaUnchecker;
import org.bytemechanics.standalone.ignite.internal.commons.string.Figlet;
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat;

/**
 * Standalone configuration container
 * @author afarre
 */
public class Standalone{

	public static final Pattern PATTERN=Pattern.compile("(?<argument>([^ ]*\".*\")(?:[ ])|([^ ]*\".*\")(?:$)|([^ ]*)(?:[ ])|([^ ]*)(?:$))");
	
	/** Latest standalone instantiated */
	protected static Standalone self=null; 
	
	
	/** Standalone name. OPTIONAL*/
	private final String name;
	/** Standalone descriptipn. OPTIONAL*/
	private final String description;
	/** Banner. OPTIONAL (default active)*/
	private final boolean showBanner;
	/** Banner font. OPTIONAL*/
	private final URL bannerFont;
	/** supplier for standalone implementation. MANDATORY*/
	private final Supplier<Ignitable> supplier;
	/** parameters enumeration class. OPTIONAL */
	private final List<Class<? extends Enum<? extends Parameter>>> parameters;
	/** Arguments from the command line execution. OPTIONAL */
	private final String[] arguments;
	/** console consumer by default java.util.logging. OPTIONAL*/
	private final Console console;

	/** Internal ignitable instance */
	private Ignitable instance;

	/**
	 * Standalone constructor
	 * @param _supplier ignitable supplier
	 * @param _name optional name for the banner
	 * @param _description optional description for the help
	 * @param _showBanner flag to determine if the name is informed should show the banner
	 * @param _bannerFont banner font
	 * @param _parameters List of parameters enum to parse
	 * @param _arguments arguments to use
	 * @param _console console to use
	 * @param _consoleFormat console format
	 * @param _verbose console verbose enabler
	 */
	protected Standalone(final Supplier<Ignitable> _supplier,final String _name,final String _description,final boolean _showBanner,final URL _bannerFont,final List<Class<? extends Enum<? extends Parameter>>> _parameters,final String[] _arguments,final Consumer<String> _console,final BiFunction<String,Object[],String> _consoleFormat,final boolean _verbose){
		this(_supplier
				, _name
				, _description
				, _showBanner
				, _bannerFont
				, _parameters
				, _arguments
				, new OutConsole((_console!=null)? _console : getDefaultConsole(),_consoleFormat,_verbose));
	}
	/**
	 * Standalone constructor
	 * @param _supplier ignitable supplier
	 * @param _name optional name for the banner
	 * @param _description optional description for the help
	 * @param _showBanner flag to determine if the name is informed should show the banner
	 * @param _bannerFont banner font
	 * @param _parameters List of parameters enum to parse
	 * @param _arguments arguments to use
	 * @param _console console to use
	 */
	protected Standalone(final Supplier<Ignitable> _supplier,final String _name,final String _description,final boolean _showBanner,final URL _bannerFont,final List<Class<? extends Enum<? extends Parameter>>> _parameters,final String[] _arguments,final Console _console){
		if(_supplier==null)
			throw new NullPointerException("Mandatory \"supplier\" can not be null");
		this.name=_name;
		this.description=_description;
		this.showBanner=_showBanner;
		this.bannerFont=(_bannerFont!=null)? _bannerFont : ClassLoader.getSystemResource("standard.flf");
		this.supplier=_supplier;
		this.arguments=((_arguments==null)||_arguments.length==0)? new String[0] : _arguments;
		this.parameters=_parameters;
		this.instance=null;
		this.console=_console;
	}
	
	/**
	 * Initializes java logging to create a CONSOLE output without prefixes
	 * @return The same instance provided
	 */
	private static Consumer<String> getDefaultConsole(){
		
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
			startupException(e);
		}
		
		return reply;
	}

	private void closeAutoCloseables(){
		Optional.ofNullable(this.instance)
					.filter(ignitable -> AutoCloseable.class.isAssignableFrom(ignitable.getClass()))
					.map(ignitable -> (AutoCloseable)ignitable)
					.ifPresent(LambdaUnchecker.uncheckedConsumer(closeableIgnitable -> closeableIgnitable.close()));
	}
	private void startupException(final Exception e){
		try{
			this.instance.startupException(e);
		}finally{
			closeAutoCloseables();
		}
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
		}finally{
			closeAutoCloseables();
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
		
		if((this.showBanner)&&(this.name!=null)){
			try(InputStream font=this.bannerFont.openStream()){
				Figlet figlet=new Figlet(font, Charset.forName("UTF-8"));
				String banner=figlet.print(this.name);
				this.console.info(figlet.line(this.name,'='));
				this.console.info(banner);
				this.console.info(figlet.line(this.name,'-'));
				this.console.info(SimpleFormat.format("\tJVM: {}",System.getProperty("java.version")));
				this.console.info(SimpleFormat.format("\tCores: {}",Runtime.getRuntime().availableProcessors()));
				this.console.info(SimpleFormat.format("\tMemory (bytes): {}/{}",Runtime.getRuntime().totalMemory(),Runtime.getRuntime().maxMemory()));
				this.console.info(SimpleFormat.format("\tBase path: {}", new File(".").getCanonicalPath()));
				this.console.info(SimpleFormat.format("\tVersion: {}/{}",
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
				this.console.info(figlet.line(this.name,'='));
			} catch (IOException e) {
				throw new FontNotReadable(this.bannerFont,e);
			}
		}
		
		return reply;
	} 
	
	/**
	 * Assigns standalone if possible
	 * @return The same instance provided
	 */
	protected Standalone registerStandalone(){

		final Standalone reply=this;
		
		if(IgnitableAdapter.class.isAssignableFrom(this.instance.getClass())){
			((IgnitableAdapter)reply.instance).setStandalone(this);
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
				.registerStandalone()
				.parseParameters()
				.validateParameters()
				.printBanner()
				.startup();
		}catch(MandatoryParameterNotProvided e){
			this.console.error(e.getMessage());
			this.console.error(Parameter.getHelp(this.parameters));
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
	 * Standalone description. Description to show into help. OPTIONAL
	 * @return Standalone description. OPTIONAL
	 * @since 2.0.0
	*/
	public String getDescription() {
		return description;
	}

	/**
	 * Standalone show banner flag. OPTIONAL (default true)
	 * @return Standalone show banner flag
	 */
	public boolean isShowBanner() {
		return showBanner;
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

	/**
	 * Return the configured console
	 * @return the configured console
	 */
	public Console getConsole(){
		return this.console;
	}

	/** Standalone builder helper class */
	@java.lang.SuppressWarnings("all")
	public static class StandaloneBuilder {

		protected final Supplier<Ignitable> supplier;
		protected final List<Class<? extends Enum<? extends Parameter>>> parameters;
		protected String name;
		protected String description;
		protected boolean showBanner=true;
		protected URL bannerFont;
		protected String[] arguments;
		protected boolean verbose=false;
		protected Consumer<String> console;
		protected Console consoleInstance=null;
		protected BiFunction<String,Object[],String> consoleFormat=((message,args) -> SimpleFormat.format(message,args));

		StandaloneBuilder(final Supplier<Ignitable> _supplier){
			this.supplier=_supplier;
			this.parameters=new ArrayList<>();
		}
		
		/**
		* Standalone name. If present banner is printed at console. OPTIONAL
		* @param _name name of the application to use as banner if no name shows no banner
		* @return StandaloneBuilder to chain other properties
		*/
		public StandaloneBuilder name(final String _name) {
			this.name = _name;
			return this;
		}
		/**
		* Standalone description for help purposes. OPTIONAL
		* @param _name name of the application to use as banner if no name shows no banner
		* @return StandaloneBuilder to chain other properties
		* @since 2.0.0
		*/
		public StandaloneBuilder description(final String _description) {
			this.description = _description;
			return this;
		}
		/**
		* Show banner. flag to activate the banner, by default is active but is only shown if the standalone has assigned name
		* @param _showBanner flag to show banner (by default true)
		* @return StandaloneBuilder to chain other properties
		* @see StandaloneBuilder#name(java.lang.String) 
		*/
		public StandaloneBuilder showBanner(final boolean _showBanner) {
			this.showBanner = _showBanner;
			return this;
		}
		/**
		* Baner font. Figlet font file URL by defautl use embeded standard.flf font.
		* @param _bannerFont font to write the banner
		* @return StandaloneBuilder to chain other properties
		*/
		public StandaloneBuilder bannerFont(final URL _bannerFont) {
			this.bannerFont = _bannerFont;
			return this;
		}
		/**
		* Parameters to load from the arguments. Can be invoked several times and will load every enum requested
		* @param _parameters parameters parser
		* @return StandaloneBuilder to chain other properties
		*/
		public StandaloneBuilder parameters(final Class<? extends Enum<? extends Parameter>> _parameters) {
			this.parameters.add(_parameters);
			return this;
		}
		/**
		* Arguments reveived to parse as parameters
		* @param _arguments arguments passed to standalone
		* @return StandaloneBuilder to chain other properties
		*/
		public StandaloneBuilder arguments(final String[] _arguments) {

			this.arguments=Stream.of(_arguments)
									.reduce((left,right) -> String.join(" ",left,right))
										.map(PATTERN::matcher)
										.map(matcher -> {
												List<String> reply=new ArrayList<>();

												while(matcher.find()){
													reply.add(matcher.group("argument")
																		.trim());
												}

												return reply;
											})
										.map(args -> args.toArray(new String[0]))
										.orElseGet(() -> new String[0]);
			return this;
		}

		/**
		* Verbose flag (default: false)
		* @param _verbose enable verbose output
		* @return StandaloneBuilder to chain other properties
		*/
		public StandaloneBuilder verbose(final boolean _verbose) {
			this.verbose = _verbose;
			return this;
		}
		
		/**
		* Console consumer where to write banner and standalone ignite default logs
		* @param _console console output channel
		* @return StandaloneBuilder to chain other properties
		*/
		public StandaloneBuilder console(final Consumer<String> _console) {
			this.console = _console;
			return this;
		}

		/**
		* Console consumer where to write banner and standalone ignite default logs
		* @param _console console output channel
		* @return StandaloneBuilder to chain other properties
		* @see Console
		* @since 2.0.0
		*/
		public StandaloneBuilder console(final Console _console) {
			this.consoleInstance = _console;
			return this;
		}

		/**
		* Console consumer where to write banner and standalone ignite default logs
		* @param _consoleFormat function to format output (by default usign {} as placeholder)
		* @return StandaloneBuilder to chain other properties
		* @since 2.0.0
		*/
		public StandaloneBuilder consoleformat(final BiFunction<String,Object[],String> _consoleFormat) {
			this.consoleFormat = _consoleFormat;
			return this;
		}

		public Standalone build() {
			if(this.consoleInstance!=null){
				Standalone.self=new Standalone(supplier,name,description,showBanner,bannerFont,Collections.unmodifiableList(parameters), arguments,consoleInstance);
			}else{
				Standalone.self=new Standalone(supplier,name,description,showBanner,bannerFont,Collections.unmodifiableList(parameters), arguments,console,consoleFormat,verbose);
			}
			return Standalone.self;
		}
	}

	/**
	 * Returns Standalone instance builder
	 * @param _supplier ignitable supplier to provide the ignitable instance (MANDATORY)
	 * @return standalone builder
	 */
	public static StandaloneBuilder builder(final Supplier<Ignitable> _supplier) {
		return Optional.ofNullable(_supplier)
							.map(StandaloneBuilder::new )
						.orElseThrow(MandatoryIgnitableSupplier::new);
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

	/**
	 * Return the list of parameter classes provided during creation of the <b>latest</b> standalone instantiation or empty list if no parameters added or no standalone instantiated
	 * @return list of parameter classes provided during creation or empty list if no parameters added or no standalone instantiated
	 * @see Standalone#getParameters()
	 */
	public static List<Class<? extends Enum<? extends Parameter>>> getParametersClasses(){
		return Optional.ofNullable(Standalone.self)
						.map(Standalone::getParameters)
						.orElse(Collections.emptyList());
	}

	/**
	 * Return the help for the <b>latest</b> standalone instantiation or empty string if no parameters added or no standalone instantiated
	 * @return string representing the help for the given parameters or empty string
	 * @see Parameter#getHelp(java.util.List) 
	 */
	public static String getHelp(){
		return Optional.of(getParametersClasses())
						.filter(parameters -> !parameters.isEmpty())
						.map(Parameter::getHelp)
						.map(params -> String.join("",Optional.ofNullable(Standalone.self)
																.map(Standalone::getDescription)
																.map(desc -> desc+"\n")
																.orElse("")
														,params))
						.orElse("");
	}
}
