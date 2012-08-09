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
package org.trpr.platform.runtime.spi.bootstrapext;

import java.util.Map;

import org.trpr.platform.runtime.impl.bootstrapext.BootstrapExtensionInfo;

/**
 * The <code>BootstrapExtension</code> interface provides call back methods that implementations may use to participate in the runtime bootstrap activity.
 * Each bootstrap extension may veto the bootstrap process if required. Concrete implementations therefore need to use this facility with discretion.
 * 
 * Each BootstrapExtension is described by a {@link BootstrapExtensionInfo}. It may also be optionally dependent on one or more BootstrapExtensionS. The
 * dependencies are loaded and injected into a Bootstrap extension before it is initialized via {@link #init()}.
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */

public interface BootstrapExtension {
	
	/**
	 * Constants that influence the bootstrap activity based on the outcome of execution of 
	 * BootstrapExtension implementations
	 */
	public static final int CONTINUE_BOOTSTRAP = 0;
	public static final int VETO_BOOTSTRAP = 999999;
	
	/**
	 * Returns the name identifier for this BootstrapExtension
	 * @return the name of this BootstrapExtension
	 */
	public String getName();
	
	/**
	 * Sets the name for this BootstrapExtension
	 * @param name the name of this BootstrapExtension
	 */
	public void setName(String name);
	
	/**
	 * Initializes this BootstrapExtension
	 */
	public void init();

	/**
	 * Destroys this BootstrapExtension. This BootstarpExtension must release all resources held
	 * and perform de-initialization of any objects that it maintains. 
	 * This BootstrapExtension is considered unusable once this method is called. A subsequent call
	 * to init() can re-initialize this BootstrapExtension 
	 */
	public void destroy();
	
	/**
	 * Gets the outcome status indicator that determines the continuation of the bootstrap activity.
	 * @return outcome value from one of the valid values defined in this interface
	 */
	public int getOutcomeStatus();
	
	/**
	 * Injects the specified dependencies into this BootstrapExtension before this instance is initialized
	 * @param extensions BootstrapExtension instances that this one is dependent on
	 */
	public void addDependency(BootstrapExtension... extensions);
	
	/**
	 * Sets optional properties configured for this BootstrapExtension
	 * @param properties Map containing optional properties
	 */
	public void setProperties(Map<String, String> properties);

}
