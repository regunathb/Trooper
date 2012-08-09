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
package org.trpr.platform.runtime.spi.component;

import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.spi.event.PlatformEventProducer;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.common.RuntimeConstants;

/**
 * The <code>ComponentContainer</code> is a container for Components in the Trooper runtime. A Component may represent any business feature or 
 * group of that tasks that represent a feature. A Component is typically managed by a container such as this one. Typical components are Services, Batch
 * jobs etc.
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */

public interface ComponentContainer extends PlatformEventProducer {
	
	/**
	 * Returns the name identifier for this ComponentContainer
	 * @return the name of this ComponentContainer
	 */
	public String getName();
	
	/**
	 * Initializes this ComponentContainer. 
	 * 
	 * @throws PlatformException in case of errors during init
	 */
	public void init() throws PlatformException;
	
	/**
	 * Destroys this ComponentContainer
	 * 
	 * @throws PlatformException in case of errors during destroy
	 */
	public void destroy() throws PlatformException;
	
	/**
	 * Publishes the specified event 
	 * @param bootstrapEvent PlatformEvent of type defined by {@link RuntimeConstants#BOOTSTRAPMONITOREDEVENT}
	 */
	public void publishBootstrapEvent(PlatformEvent bootstrapEvent);

}
