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

import org.trpr.platform.servicefw.spi.ServiceInfo;
import org.trpr.platform.servicefw.spi.ServiceKey;

/**
* The <code>ServiceInfoImpl</code> is an implementation of the {@link ServiceInfo} interface 
* 
* @see ServiceInfo 
* @author  Regunath B
* @version 1.0, 13/08/2012
*/
public class ServiceInfoImpl implements ServiceInfo {
	
	/** The ServiceKey for the service */
	private ServiceKey serviceKey;
	
	/** Indicator if the service is remote */
	private boolean remote;
	
	/** Project name that the service is implemented in */
	private String projectName;

	/** Domain name that the service belongs to */
	private String domainName;

	/**
	 * Constructs the service information object 
	 * @param projectName the name of the project that the service is implemented in
	 * @param domainName domain name of the referred service
	 * @param remote true if the service is remote, false otherwise
	 * @param serviceKey service key to identify the service
	 */
	public ServiceInfoImpl(String projectName, String domainName, 
			boolean remote, ServiceKey key) {	
		this.projectName = projectName;
		this.domainName = domainName;
		this.remote = remote;
		this.serviceKey = key;
	}

	
	/**
	 * @return Returns the remote.
	 */
	public boolean isRemote() {
		return remote;
	}
	
	/**
	 * @return Returns the serviceKey.
	 */
	public ServiceKey getServiceKey() {
		return this.serviceKey;
	}
	
	/**
	 * Interface method implementation
	 * @see ServiceInfo#getProjectName()
	 */	
	public String getProjectName() {
		return this.projectName;
	}
		
	/**
	 * @return Returns the domainName object.
	 */
	public String getDomainName() {
		return this.domainName;
	}
}
