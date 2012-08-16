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

package org.trpr.platform.batch.impl.spring;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.trpr.platform.batch.BatchFrameworkConstants;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.spi.event.PlatformEventProducer;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.impl.bootstrapext.spring.ApplicationContextFactory;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension;
import org.trpr.platform.runtime.spi.component.ComponentContainer;

/**
 * The <code>SpringBatchComponentContainer</code> class is a ComponentContainer implementation that loads and manages Spring Batch 
 * job implementations.
 * This container locates and loads all job definitions contained in files named by the value of BatchFrameworkConstants.SPRING_BATCH_CONFIG.
 * This container also loads all common batch related Spring beans contained in BatchFrameworkConstants.COMMON_BATCH_CONFIG and 
 * ensures that all beans declared in ServerConstants.COMMON_SPRING_BEANS_CONFIG are available to the batch job beans by 
 * specifying the common beans context as the parent for the job app context created by this container.
 * 
 * @see ComponentContainer
 * @author Regunath B
 */
public class SpringBatchComponentContainer implements ComponentContainer {

	/** The prefix to be added to file absolute paths when loading Spring XMLs using the FileSystemXmlApplicationContext*/
	private static final String FILE_PREFIX = "file:";

	/**
	 * The default Event producer bean name 
	 */
	private static final String DEFAULT_EVENT_PRODUCER = "platformEventProducer";
	
    /** The common batch beans context*/
    private AbstractApplicationContext commonBatchBeansContext;    
    
	/**
	 * The list of Spring application contexts that would hold all job instances loaded by this container
	 */
    private List<AbstractApplicationContext> jobsContextList = new LinkedList<AbstractApplicationContext>();	
    
    /** Local reference for all BootstrapExtensionS loaded by the Container and set on this ComponentContainer*/
    private BootstrapExtension[] loadedBootstrapExtensions;
    
    /**
     * Interface method implementation. Returns the fully qualified class name of this class
     * @see org.trpr.platform.runtime.spi.component.ComponentContainer#getName()
     */
    public String getName() {
    	return this.getClass().getName();
    }
    
    /**
     * Interface method implementation. Stores local references to the specified BootstrapExtension instances.
     * @see org.trpr.platform.runtime.spi.component.ComponentContainer#setLoadedBootstrapExtensions(org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension[])
     */
    public void setLoadedBootstrapExtensions(BootstrapExtension...bootstrapExtensions) {
    	this.loadedBootstrapExtensions = bootstrapExtensions;
    }
	
	/**
	 * Interface method implementation. Locates and loads all configured jobs.
	 * @see ComponentContainer#init()
	 */
	public void init() throws PlatformException {
		
		// add the "file:" prefix to file name to get around strange behavior of FileSystemXmlApplicationContext that converts absolute path 
		// to relative path
		// The common batch beans context is loaded first using the Platform common beans context as parent
		String commonContextFile = FILE_PREFIX + FileLocator.findUniqueFile(BatchFrameworkConstants.COMMON_BATCH_CONFIG).getAbsolutePath();
		// get the commons beans context by locating the ApplicationContextFactory bootstrap extension
		ApplicationContextFactory appContextFactory = null;
		for (BootstrapExtension be : this.loadedBootstrapExtensions) {
			if (be.getClass().isAssignableFrom(BootstrapExtension.class)) {
				appContextFactory = (ApplicationContextFactory)be;
				break;
			}
		}
		this.commonBatchBeansContext = new FileSystemXmlApplicationContext(new String[]{commonContextFile},
				appContextFactory.getCommonBeansContext());	
		// add the common batch beans to the contexts list
		this.jobsContextList.add(this.commonBatchBeansContext);
		
		// locate and load the individual job bean XML files using the common batch beans context as parent
		File[] jobBeansFiles = FileLocator.findFiles(BatchFrameworkConstants.SPRING_BATCH_CONFIG);					
		for (File jobBeansFile : jobBeansFiles) {
			// add the "file:" prefix to file names to get around strange behavior of FileSystemXmlApplicationContext that converts absolute path 
			// to relative path
			this.jobsContextList.add(new FileSystemXmlApplicationContext(new String[]{FILE_PREFIX + jobBeansFile.getAbsolutePath()},
					this.commonBatchBeansContext));
		}
		
	}

	/**
	 * Interface method implementation. Destroys the Spring application context containing loaded job definitions.
	 * @see ComponentContainer#destroy()
	 */
	public void destroy() throws PlatformException {
		for (AbstractApplicationContext context : this.jobsContextList) {
			context.close();
		}
		this.jobsContextList = null;		
	}
	
	/**
	 * Interface method implementation. Publishes the specified event to using a named bean DEFAULT_EVENT_PRODUCER looked up from the 
	 * commons jobs context (i.e. BatchFrameworkConstants.COMMON_BATCH_CONFIG).
	 * Note that typically no consumers are registered when running this container
	 */ 
	public void publishEvent(PlatformEvent event) {
		PlatformEventProducer publisher= (PlatformEventProducer)this.commonBatchBeansContext.getBean(DEFAULT_EVENT_PRODUCER);
		publisher.publishEvent(event);
	}
	    
	/**
	 * Interface method implementation. Publishes the specified event using the {@link #publishEvent(PlatformEvent)} method
	 * @see ComponentContainer#publishBootstrapEvent(PlatformEvent)
	 */
	public void publishBootstrapEvent(PlatformEvent bootstrapEvent) {	
		this.publishEvent(bootstrapEvent);
	}
    
}
