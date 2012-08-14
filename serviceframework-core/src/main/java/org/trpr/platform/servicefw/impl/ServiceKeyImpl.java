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

package org.trpr.platform.servicefw.impl;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.trpr.platform.servicefw.spi.ServiceKey;

/**
*
* The <code>ServiceKeyImpl</code> is an implementation of the {@link ServiceKey} interface 
* 
* @see ServiceKey
* @author  Regunath B
* @version 1.0, 13/08/2012
*/
public class ServiceKeyImpl implements ServiceKey, Serializable {

	private static final long serialVersionUID = 4130555022337001738L;
	
	/** Name part of the this ServiceKey */
	private String name;
	
	/** Version part of the this ServiceKey */
	private String version;
	
	/**
	 * Constructs the Service key object 
	 * @param name name part for this ServiceKey
	 * @param version version part for this ServiceKey
	 */
	public ServiceKeyImpl(String name, String version) {
		this.name = name;
		if (StringUtils.isBlank(version)) {
			this.version = LATEST_VERSION;
		} else {
			this.version = version;
		}
	}
	
	/**
	 * Constructs the Service key object assuming version LATEST
	 * @param name name part for this ServiceKey
	 * @param version version part for this ServiceKey
	 */
	public ServiceKeyImpl(String name) {
		this.name = name;
		this.version = LATEST_VERSION;
	}
	
	/**
	 * @return Returns the service name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of the service   
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Returns the version of service.
	 */
	public String getVersion() {
		return version;
	}
	
	/**
	 * Sets the version of the service
	 * @param version The version to set.
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/** 
	 * Overriden Object superclass method 
	 * checks if the servicekey objects are equal
	 */
	public boolean equals(Object serviceKeyObject) {		
		if(this.name.equals(((ServiceKeyImpl)serviceKeyObject).getName()) 
				&& this.version.equals(((ServiceKeyImpl)serviceKeyObject).getVersion())) {			
			return true;
		} else {
			return false;
		}
	}		
	
	/** 
	 * Overriden Object superclass method.
	 * Returns a hashcode that is an exclusive OR of this object's hashcode and 
	 * the version's hashcode
	 * @return returns a hashcode for the class instance
	 */
	public int hashCode() {
		return this.name.hashCode() ^ this.version.hashCode();
	}
	/**
	 * Overriden Object superclass method.
	 * Returns a string which has ServiceName and ServiceKey appended.
	 * @return String serviceName and serviceVersion is appended.
	 */
	public String toString(){
		StringBuffer serviceKey = new StringBuffer();
		serviceKey.append(this.name);
		serviceKey.append(this.version);
		return serviceKey.toString();
		
	}
}
