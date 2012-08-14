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
* The <code>ServiceKey</code> contains information that may be used to identify
* a service such as its name and version.  
* 
* @author  Regunath B
* @version 1.0, 13/08/2012
*/
public interface ServiceKey extends Serializable {
	
	/**
	 * Version to be used to access latest version of the service.
	 * This is default version if no version is specified.
	 */
	public final String LATEST_VERSION = "LATEST";
		
	/**
	 * The name of the service
	 * @return the service name
	 */
	public String getName();
	
	/**
	 * The version of the service
	 * @return the service version
	 */
	public String getVersion();
	
}
