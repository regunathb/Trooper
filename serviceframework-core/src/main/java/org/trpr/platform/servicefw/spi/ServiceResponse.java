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
*
* The <code>ServiceResponse</code> interface defines the behavior common to all 
* responses from platform service requests.  
* 
* @author  Regunath B
* @version 1.0, 13/08/2012
*/
public interface ServiceResponse<T> extends Serializable {
			
	/**
	 * Get the object representing the data the service has returned. 
	 * @return The data returned by the service
	 */
	public T getResponseData();
	
}
