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

package org.trpr.platform.servicefw.common;

/**
 * 
 * The <code>ServiceFrameworkConstants</code> class is a place-holder for all service framework constants.
 * 
 * @author Regunath B
 * 
 */

public abstract class ServiceFrameworkConstants {
	
	/** File names for service configuration file names*/
	public static final String COMMON_SPRING_SERVICES_CONFIG =  "common-spring-services-config.xml";
	public static final String SPRING_SERVICES_CONFIG = "spring-services-config.xml";

	/**
	 * Default domain name
	 */
	public static final String DEFAULT_DOMAIN = "default";
	
	/**
	 * status code for Service Success or Failure
	 */
	public static final int SUCCESS_STATUS_CODE = 1;
	public static final int FAILURE_STATUS_CODE = 0;
	
	
	/**
	 * Status messages for Service Success or Failure
	 */
	public static final String SUCCESS_STATUS_MESSAGE = "SUCCESS";
	public static final String FAILURE_STATUS_MESSAGE = "FAILURE";

}
