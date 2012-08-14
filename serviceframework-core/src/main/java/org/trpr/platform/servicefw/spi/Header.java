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

package org.trpr.platform.servicefw.spi;
 
import java.io.Serializable;

/**
 * The <code>Header</code> is a class with a getter/setters for variables i.e., key and value.
 * This Header may be used in Service request-response payload wrappers for passing meta-dat information.
 * 
 * @author  Regunath B
* @version 1.0, 13/08/2012
 */
public class Header implements Serializable{

	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = 1295944436267635802L;
	private String key;
	private String value;
	
	/**
	 * Default constructor
	 */
	public Header(){
	}
	
	/**
	 * Constructor to initialize key and value variables.
	 * @param key
	 * @param value
	 */
	public Header(String key,String value){
		this.key = key;
		this.value = value;
	}
	
	/**
	 * Returns the key.
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Sets the key.
	 * @param key
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * Returns the value.
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Sets the value.
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}	
}
