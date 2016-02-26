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
package org.trpr.platform.runtime.spi.container;

import java.util.List;

import org.trpr.platform.core.PlatformException;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.common.RuntimeConstants;
import org.trpr.platform.runtime.spi.component.ComponentContainer;

/**
 * The <code>Container</code> manages the Trooper runtime. It is initialized during the bootstrapping process and in turn initializes the various
 * providers. It supports life cycle methods for the runtime.
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */
public interface Container {

	/**
	 * Returns the name identifier for this Container
	 * @return the name of this Container
	 */
	public String getName();
	
	/**
	 * Initializes this Container
	 * @throws PlatformException in case of errors during initialization process
	 */
	public void init() throws PlatformException;	
	
	/**
	 * Destroys this Container
	 * @throws PlatformException in case of errors during destroy 
	 */
	public void destroy() throws PlatformException;
		
	/**
	 * Get the list of {@link ComponentContainer}S, if any, that has been initialized by this Container
	 * @return ComponentContainerS, if any, that has been configured to be loaded by this Container
	 */
	public List<ComponentContainer> getComponentContainers();
	
	/**
	 * Publishes the specified event using the {@link ComponentContainer} interface, if any, that has been configured to be loaded by this
	 * Container. 
	 * @param bootstrapEvent PlatformEvent of type defined by {@link RuntimeConstants#BOOTSTRAPMONITOREDEVENT}
	 */
	public void publishBootstrapEvent(PlatformEvent bootstrapEvent);
	
}
