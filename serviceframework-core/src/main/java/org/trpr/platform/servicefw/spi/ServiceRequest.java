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

import org.trpr.platform.servicefw.security.SecurityContext;

/**
*
* The <code>ServiceRequest</code> interface defines behavior common to all platform service requests.  
* 
* @author  Regunath B
* @version 1.0, 13/08/2012
*/
public interface ServiceRequest<T> extends Serializable {
	
	/**
	 * Gets the service name for this request
	 * @return the valid service name
	 */
	public String getServiceName();
	
	/**
	 * Gets the service version.
	 * @return the service version as string
	 */
	public String getServiceVersion();
	
	/**
	 * Gets the request body data aka paylod for the request
	 * @return the request data
	 */
	public T getRequestData();
	
	/**
	 * Returns the security context around this request.
	 * @return the security context for this request
	 */
	public SecurityContext getSecurityContext();
	
	/**
	 * Returns all headers.
	 * @return the array of headers
	 */
	public Header[] getHeaders();
	
	/**
	 * Returns the header object if the header with a specified key is present.
	 * @param key The key for which value has to be returned
	 * @return the value of the specified key
	 */
	public Header getHeaderByKey(String key);	
}
