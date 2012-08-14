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

/**
*
* The <code>ServiceInfo</code> contains information about a service runtime
* such as the domain that it is part of, the service key and whether it is a 
* remote service. 
* 
* @author  Regunath B
* @version 1.0, 13/08/2012
*/
public interface ServiceInfo {

	/**
	 * Gets the service key for the associated service
	 * @return the service key of the service
	 */
	public ServiceKey getServiceKey();

	/**
	 * Get the project name for the associated service. Every service is implemented as part of a
	 * project.   
	 * @return the project name for the associated service
	 */	
	public String getProjectName();	

	/**
	 * Indicator if the service is local or remote
	 * @return true if the service is remote
	 */
	public boolean isRemote();	
	
	/**
	 * Get the domain name for the associated service.  This provides
	 * needed information if the service is remote such as how
	 * to get to the remote server (e.g., what adapter to use and
	 * the remote machine IP or host name) 
	 * @return the service domain information for the associated service
	 */	
	public String getDomainName();	
	
}
