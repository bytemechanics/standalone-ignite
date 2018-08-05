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
package org.bytemechanics.standalone.ignite

import spock.lang.*
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.*

/**
 *
 * @author afarre
 */
class StandaloneSpec extends Specification{

	def setupSpec(){
		println(">>>>> StandaloneSpec >>>> setupSpec")
		final InputStream inputStream = Standalone.class.getResourceAsStream("/logging.properties");
		try{
			LogManager.getLogManager().readConfiguration(inputStream);
		}catch (final IOException e){
			Logger.getAnonymousLogger().severe("Could not load default logging.properties file");
			Logger.getAnonymousLogger().severe(e.getMessage());
		}finally{
			if(inputStream!=null)
				inputStream.close();
		}
	}
	
	@Unroll
	def "Ignite call should call ignitable startup (before and after)"(){
		println(">>>>> StandaloneSpec >>>> Ignite call should call startup (before and after)")
		setup:
			Ignitable ignitable=Mock()
		
		when:
			Standalone.builder()
							.supplier({ -> ignitable})
						.build()
							.ignite()

		then: 
			1 * ignitable.beforeStartup()
			1 * ignitable.startup()  
			1 * ignitable.afterStartup() 
	}

	@Unroll
	def "When shutdown is call before and after must be executed"(){
		println(">>>>> StandaloneSpec >>>> When shutdown is call before and after must be executed")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build();
		
		when:
			standalone.ignite()
			standalone.shutdown()

		then: 
			1 * ignitable.beforeStartup() 
			1 * ignitable.startup() 
			1 * ignitable.afterStartup() 
			1 * ignitable.beforeShutdown() 
			1 * ignitable.shutdown() 
			1 * ignitable.afterShutdown()  
	}

	def "ParseParameters must do nothing if no parameters defined"(){
		println(">>>>> StandaloneSpec >>>> ParseParameters must do nothing if no parameters defined")
		when:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
											.build()
			Standalone standalone2=standalone.parseParameters()
		then:
			standalone==standalone2
	}

	@Unroll
	def "ParseParameters #arguments for #parameters must parse correctly the correct values"(){
		println(">>>>> StandaloneSpec >>>> ParseParameters $arguments for $parameters must parse correctly the correct values")
		setup:
			Ignitable ignitable=Mock()
			Standalone standalone=Standalone.builder()
												.supplier({ -> ignitable})
												.parameters(StandaloneAppTestParameter.class)
												.arguments(arguments)
											.build();

		when:
			standalone.parseParameters()

		then: 
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(boolean.class).isPresent()
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(boolean.class).get()==true
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(int.class).isPresent()
			StandaloneAppTestParameter.INTVALUE.getValue(int.class).get()==2234
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(long.class).isPresent()
			StandaloneAppTestParameter.LONGVALUE.getValue(long.class).get()==3243321312
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(float.class).isPresent()
			StandaloneAppTestParameter.FLOATVALUE.getValue(float.class).get()==3123.32f
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(double.class).isPresent()
			StandaloneAppTestParameter.DOUBLEVALUE.getValue(double.class).get()==3123.32d
			StandaloneAppTestParameter.BOOLEANVALUE.getValue(String.class).isPresent()
			StandaloneAppTestParameter.STRINGVALUE.getValue(String.class).get()=="TEST"
	
		where:
			parameters=StandaloneAppTestParameter.class
			arguments=["-booleanvalue:true","-intvalue:2234","-longvalue:3243321312","-floatvalue:3123.32","-doublevalue:3123.32","-stringvalue:TEST"].toArray(new String[4])
	}

}

