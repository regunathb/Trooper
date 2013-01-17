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

package org.trpr.platform.batch;

/**
 * 
 * The <code>BatchFrameworkConstants</code> class is a placeholder for all batch framework
 * constants.
 * 
 * @author Regunath B
 * 
 */
public abstract class BatchFrameworkConstants {

	/**
	 * Constants for the conventions on config file names
	 */
	public static final String COMMON_BATCH_CONFIG = "packaged/common-batch-config.xml"; // its a file picked up from classpath
	public static final String COMMON_BATCH_SERVER_NATURE_CONFIG = "packaged/common-batch-server-nature-config.xml"; // its a file picked up from classpath
	public static final String SPRING_BATCH_CONFIG = "spring-batch-config.xml";
	
	/**
	 * Constants for framework beans
	 */
	public static final String JOB_SERVICE_BEAN = "jobService";
	
}
