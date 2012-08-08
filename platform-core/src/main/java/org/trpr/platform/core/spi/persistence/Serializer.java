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

package org.trpr.platform.core.spi.persistence;

/**
 * The <code>Serializer</code> defines methods to serialize an object into byte array and de-serialize a byte array back to Object.   
 * Useful when persisting to data stores that by default use byte array form.
 * 
 * @author Srikanth P Shreenivas
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */

public interface Serializer {
	
	/**
	 * De-serializes the specified byte array into an appropriate Java type instance 
	 * @param dataBytes byte array to use in de-serializing
	 * @return the de-serialized object
	 */
	public Object toObject(byte[] dataBytes);
	
	/**
	 * Serializes the specified Object into its byte array form
	 * @param data the Object to be serialized
	 * @return byte array form of the serialized object
	 */
	public byte[] toBytes(Object data);	
	
}
