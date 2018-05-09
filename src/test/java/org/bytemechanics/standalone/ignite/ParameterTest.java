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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author afarre
 */
public class ParameterTest {
	
	@BeforeClass
	public static void setup() throws IOException{
		System.out.println(">>>>> ParameterTest >>>> setupSpec");
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

	@Test
	public void defaultGeneratedHelp(){
		System.out.println(">>>>> ParameterTest >>>> defaultGeneratedHelp");
		
		//Prepare
			String help="Usage:"
							+ "\n\t[-booleanvalue]: boolean value (Mandatory)"
							+ "\n\t[-intvalue]: int value (Mandatory)"
							+ "\n\t[-longvalue]: long value (Mandatory)"
							+ "\n\t[-floatvalue]: float value (Mandatory)"
							+ "\n\t[-doublevalue]: double value (Mandatory)"
							+ "\n\t[-stringvalue]: string value (Mandatory)\n";

		//when:
			String actual=Parameter.getHelp(StandaloneAppTestParameter.class);

		//then: 
			Assert.assertEquals(help,actual);
	}
}
