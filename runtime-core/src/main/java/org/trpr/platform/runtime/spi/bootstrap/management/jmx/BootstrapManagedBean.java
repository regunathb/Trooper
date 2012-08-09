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

package org.trpr.platform.runtime.spi.bootstrap.management.jmx;

import org.trpr.platform.core.spi.management.jmx.InstanceAwareMBean;

/**
 * The <code>BootstrapManagedBean</code> provides interface methods for bootstrapping and managing the runtime. 
 * 
 * @author Regunath B
 * @version 1.0, 06/06/2012
 */
public interface BootstrapManagedBean extends InstanceAwareMBean {

	/**
	 * Starts the runtime 
	 * @throws Exception in case of errors during startup
	 */
	public void start() throws Exception;

	/**
	 * Stops the runtime
	 * @throws Exception in case of errors while stopping the runtime
	 */
	public void stop() throws Exception;
	
	/**
	 * Shuts down this runtime
	 * @throws in case of errors during destroy
	 */
	public void destroy() throws Exception;

	/**
	 * Reloads logging configurations for the runtime  
	 */
	public void reloadLoggingConfigurations();
	
	/**
	 * Returns the configuration file path used to configure the runtime
	 * @return String the configuration file path 
	 */
	public String getBootstrapConfigPath();

	/**
	 * Sets the configuration file path used to configure the runtime
	 * @param bootstrapConfigPath the path to start up configuration file
	 */
	public void setBootstrapConfigPath(String bootstrapConfigPath);

}

