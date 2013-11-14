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
import java.util.List;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.registry.Registry;
import org.mule.context.DefaultMuleContextFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.Resource;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.trpr.platform.seda.common.SedaFrameworkConstants;
import org.trpr.platform.seda.impl.mule.spring.SpringXmlConfigurationBuilder;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.spring.ServiceConfigInfo;
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
	 * The Spring application context that would hold all service declarations from all services
	 */
    private AbstractApplicationContext servicesContext;	
	
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
		LinkedList<String> fileNamesList = new LinkedList<String>();
		// add the common Mule beans file
		fileNamesList.add(SedaFrameworkConstants.COMMON_MULE_CONFIG);
		// add the Mule configurations containing Mule service definitions
		File[] serviceBeansFiles = FileLocator.findFiles(SedaFrameworkConstants.MULE_CONFIG);					
		for (File serviceBeansFile : serviceBeansFiles) {
			fileNamesList.add(serviceBeansFile.getAbsolutePath());			
		}
		String[] muleConfigPaths = (String[])fileNamesList.toArray(new String[0]);		
		try {
			SpringXmlConfigurationBuilder springConfigBuilder = new SpringXmlConfigurationBuilder(muleConfigPaths);
			springConfigBuilder.setUseDefaultConfigResource(false); // turn off using the default config resource as we have a custom config defined in SedaFrameworkConstants.COMMON_MULE_CONFIG
			springConfigBuilder.setParentContext(this.servicesContext);
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
	 * Overriden superclass method. Throws an operation not supported exception to indicate that dynamic loading/reloading of service beans is not supported
	 * @see org.trpr.platform.servicefw.impl.spring.SpringServicesContainer#loadComponent(org.springframework.core.io.Resource)
	 */
	public void loadComponent(Resource resource) {
		throw new UnsupportedOperationException("Dynamic loading/realoding of service beans is not supported by : " + this.getClass().getName());
	}

	/**
	 * Overriden superclass method. Creates a single application context containing all the service beans
	 * @throws PlatformException
	 */
	protected void loadServiceContexts() throws PlatformException {
		// locate and load the individual service bean XML files using the common batch beans context as parent
		File[] serviceBeansFiles = FileLocator.findFiles(ServiceFrameworkConstants.SPRING_SERVICES_CONFIG);	
		List<String> fileNamesList = new LinkedList<String>();		
		for (File serviceBeansFile : serviceBeansFiles) {
			// dont load the individual service context, just register an empty context for display purposes
			GenericApplicationContext nonRefreshedServiceContext = new GenericApplicationContext();
			XmlBeanDefinitionReader beanDefReader = new XmlBeanDefinitionReader(nonRefreshedServiceContext);
			// add the "file:" prefix to file names to explicitly state that it is on the file system and not a classpath resource
			beanDefReader.loadBeanDefinitions(ServiceConfigInfo.FILE_PREFIX + serviceBeansFile.getAbsolutePath());
			ServiceConfigInfo nonInitedServiceConfigInfo = new ServiceConfigInfo(serviceBeansFile, null, nonRefreshedServiceContext);
			super.registerServiceContext(nonInitedServiceConfigInfo); 
			// add the "file:" prefix to file names to get around strange behavior of FileSystemXmlApplicationContext that converts absolute path to relative path
            fileNamesList.add(ServiceConfigInfo.FILE_PREFIX + serviceBeansFile.getAbsolutePath());		            
		}	
		this.servicesContext = new FileSystemXmlApplicationContext((String[])fileNamesList.toArray(new String[0]),
				SpringServicesContainer.getCommonServiceBeansContext());
		super.registerServiceContext(new ServiceConfigInfo(new File(ServiceFrameworkConstants.SPRING_SERVICES_CONFIG), null, this.servicesContext));
	}

	
	/**
	 * Helper method to get the ServiceEventProducer from the Mule SpringRegistry configured as mule-config.xml
	 */
	private ServiceEventProducer getMuleServiceEventProducer() {
		return (ServiceEventProducer)((Registry)this.muleContext.getRegistry()).lookupObject(SERVICE_EVENT_PRODUCER);
	}
	
}
