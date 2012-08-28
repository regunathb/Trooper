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

package org.trpr.platform.seda.impl.service;

import java.io.File;
import java.util.LinkedList;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.registry.Registry;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.trpr.platform.seda.common.SedaFrameworkConstants;
import org.trpr.platform.servicefw.impl.spring.SpringServicesContainer;
import org.trpr.platform.servicefw.spi.ServiceContainer;
import org.trpr.platform.servicefw.spi.event.ServiceEventProducer;

/**
 * The <code>MuleServicesContainer</code> class is a sub-type of the SpringServicesContainer implementation that creates a MuleContext using
 * services loaded by the parent container.
 * 
 * @see SpringServicesContainer
 * @author Regunath B
 * @version 1.0, 23/08/2012
 */
@SuppressWarnings("rawtypes")
public class MuleServicesContainer extends SpringServicesContainer {
	
	/** The logger for this class */
	private static final Logger LOGGER = LogFactory.getLogger(MuleServicesContainer.class);
	
	/**
	 * The service Event producer bean name 
	 */
	private static final String SERVICE_EVENT_PRODUCER = "serviceEventProducer";
	
	/**
	 * The MuleContext instance
	 */
	private MuleContext muleContext;

	/**
	 * Overriden superclass method. Calls super.init() and also initializes the MuleContext
	 * @see SpringServicesContainer#init()
	 */
	public void init() throws PlatformException {
		super.init();				
		// load the mule configurations and set the SpringServicesContainer's servicesContext bean context as the parent
		File[] serviceBeansFiles = FileLocator.findFiles(SedaFrameworkConstants.MULE_CONFIG);					
		LinkedList<String> fileNamesList = new LinkedList<String>();
		for (File serviceBeansFile : serviceBeansFiles) {
			fileNamesList.add(serviceBeansFile.getAbsolutePath());			
		}
		String[] muleConfigPaths = (String[])fileNamesList.toArray(new String[0]);		
		try {
			SpringXmlConfigurationBuilder springConfigBuilder = new SpringXmlConfigurationBuilder(muleConfigPaths);
			springConfigBuilder.setParentContext(super.getServicesContext());
			this.muleContext = new DefaultMuleContextFactory().createMuleContext(springConfigBuilder);
			this.muleContext.start();
		} catch (Exception e) {
			LOGGER.error("Fatal error loading Mule configurations : " + e.getMessage(),e);
			throw new PlatformException("Fatal error loading Mule configurations : " + e.getMessage(),e);
		}
	}
	
	/**
	 * Overriden superclass method. Closes the MuleContext and then invokes {@link #resetContainer()}
	 * @see SpringServicesContainer#destroy()
	 */
	public void destroy() throws PlatformException {
		try {
			this.muleContext.stop();
		} catch (MuleException e) {
			LOGGER.error("Fatal error stopping Mule : " + e.getMessage(),e);
			throw new PlatformException("Fatal error stopping Mule : " + e.getMessage(),e);
		}
		this.muleContext.dispose();
		this.muleContext = null;
		// now invoke superclass clean up code
		this.resetContainer();
	}
	
	/**
	 * Overriden superclass method. Send the specified event to the specified endpoint URI using the ServiceEventProducer looked up from
	 * the Mule SpringRegistry i.e. mule-config.xml
	 */
	public void publishEvent(PlatformEvent event, String endpointURI) {
		getMuleServiceEventProducer().publishEvent(event, endpointURI);
	}

	/**
	 * Overriden superclass method. Sends the specified event to the default endpoint URI defined for the configured ServiceEventProducer. 
	 */
	public void publishEvent(PlatformEvent event) {
		getMuleServiceEventProducer().publishEvent(event);
	}
	
	/**
	 * Overriden method implementation. Returns boolean "true" as check-pointing is supported by this ServiceContainer for Mule model definitions
	 * @see ServiceContainer#isServiceExecutionCheckPointingRequired()
	 */
	public boolean isServiceExecutionCheckPointingRequired() {
		return true;
	}	
	
	/**
	 * Helper method to get the ServiceEventProducer from the Mule SpringRegistry configured as mule-config.xml
	 */
	private ServiceEventProducer getMuleServiceEventProducer() {
		return (ServiceEventProducer)((Registry)this.muleContext.getRegistry()).lookupObject(SERVICE_EVENT_PRODUCER);
	}
	
}
