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

package org.trpr.platform.seda.common;

/**
 * 
 * The <code>SedaFrameworkConstants</code> class is a place-holder for all seda framework constants.
 * 
 * @author Regunath B
 * 
 */

public abstract class SedaFrameworkConstants {
	
	/** The common Mule config file. We expect to find this packaged in a jar i.e. on the classpath */
	public static final String COMMON_MULE_CONFIG = "packaged/common-mule-config.xml";
	
	/** File names for service configuration file names*/
	public static final String MULE_CONFIG = "mule-config.xml";

}
