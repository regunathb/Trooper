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
package org.trpr.platform.integration.spi.marshalling;


/**
 * The <code> Marshaller </code> interface defines generic methods for marshalling from Java to other data transfer friendly formats.
 * 
 * @author Regunath B
 * @version 1.0, 7 Feb 2013
 */
public interface Marshaller {

	/**
	 * Converts specified Java Object to an equivalent marshalled String. 
	 * @param Object the Java Object to be marshalled
	 * @return String marshalled String equivalent to Java Object
	 * @throws MarshallingException in case of errors during marshalling 
	 */
	public String marshal(Object object) throws MarshallingException;
	
	/**
	 *Converts specified string to an equivalent Java Object. 
	 * @param data the data as String
	 * @param clazz the Java Class that the unmarshalled Object belongs to
	 * @return Java Object unmarshalled from the specified data String
	 * @throws MarshallingException in case of errors during unmarshalling
	 */
	 public <T> T unmarshal(String data, Class<T> clazz) throws MarshallingException;

}
