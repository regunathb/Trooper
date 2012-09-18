/*
 * Copyright 2012-2015, the original author or authors.
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
package org.trpr.platform.integration.spi.json;

/**
 * The <code> JSONTranscoder </code> interface defines methods for Java-JSON marshalling/unmarshalling.
 * 
 * @author Regunath B
 * @version 1.0, 18/09/2012
 */
public interface JSONTranscoder {

	/**
	 * Converts specified Java Object to an equivalent JSON String. 
	 * @param Object the Java Object to be marshalled to JSON
	 * @return String JSON String equivalent to Java Object
	 * @throws JSONDataException in case of errors during marshalling 
	 */
	public String marshal(Object object) throws JSONDataException;
	
	/**
	 *Converts specified JSON string to an equivalent Java Object. 
	 * @param json JSON data as String
	 * @param clazz the Java Class that the unmarshalled Object belongs to
	 * @return Java Object unmarshalled from the specified JSON String
	 * @throws JSONDataException in case of errors during unmarshalling
	 */
	 public <T> T unmarshal(String json, Class<T> clazz) throws JSONDataException;
}
