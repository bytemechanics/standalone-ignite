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

import java.util.function.*
import java.text.*
import java.io.*
import java.io.*
import java.util.logging.*
import java.time.*
import java.time.format.*
import org.bytemechanics.standalone.ignite.internal.commons.string.*
import org.bytemechanics.standalone.ignite.exceptions.MandatoryArgumentNotProvided
import org.bytemechanics.standalone.ignite.exceptions.NullOrEmptyMandatoryArgument
import org.bytemechanics.standalone.ignite.internal.commons.string.SimpleFormat

/**
 * @author afarre
 */
class ParameterSpec extends Specification{

	def setupSpec(){
		println(">>>>> ParameterSpec >>>> setupSpec")
		final InputStream inputStream = Parameter.class.getResourceAsStream("/logging.properties");
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
	def "ParseParameters #arguments for #parameters must parse correctly the correct values"(){
		println(">>>>> DefaultParameterContainerSpec >>>> ParseParameters $arguments for $parameters must parse correctly the correct values")
		when:
			Parameter.parseParameters(parameters,arguments)

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

	@Unroll
	def "ParseParameters #arguments for #parameters must fail with empty value"(){
		println(">>>>> DefaultParameterContainerSpec >>>> ParseParameters $arguments for $parameters must fail with empty value")
		when:
			Parameter.parseParameters(parameters,arguments)

		then: 
			def e=thrown(NullOrEmptyMandatoryArgument) 
	
		where:
			parameters=StandaloneAppTestParameter.class
			arguments=["-booleanvalue:","-intvalue: ","-longvalue:   ","-floatvalue: ","-doublevalue:      ","-stringvalue: "].toArray(new String[4])
	}
}

