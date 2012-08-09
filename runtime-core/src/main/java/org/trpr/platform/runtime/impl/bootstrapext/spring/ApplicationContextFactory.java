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
package org.trpr.platform.runtime.impl.bootstrapext.spring;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.runtime.common.RuntimeConstants;
import org.trpr.platform.runtime.impl.bootstrapext.AbstractBootstrapExtension;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * The <code>ApplicationContextFactory</code> class is a sub-type of {@link AbstractBootstrapExtension} used for creating
 * Spring {@link ApplicationContext} instances in a thread-safe manner during Runtime bootstrap.
 * 
 * @author Regunath B
 * @version 1.0, 06/06/2012
 */
public class ApplicationContextFactory extends AbstractBootstrapExtension {

	/** The prefix to be added to file absolute paths when loading Spring XMLs using the FileSystemXmlApplicationContext*/
	private static final String FILE_PREFIX = "file:";
	
	/**
	 * The pre-defined name for the Commons bean application context. String should be unique and not the same as any name defined by users for
	 * application contexts loaded via this BootstrapExtension
	 */
	private static final String COMMON_BEANS_CONTEXT_NAME = "~~~CcellPlatformCommonBeansContext~~~";
	
	/** The Logger instance for this class  */
	private static final Logger LOGGER = LogFactory.getLogger(ApplicationContextFactory.class);
	
	/** Bootstrap outcome indicator. Initialzed by default to continue. May be overriden by outcome of {@link #init()} */
	private int bootstrapOutcome = BootstrapExtension.CONTINUE_BOOTSTRAP;

	/**
	 * Map of created Spring application contexts.
	 */
	private Map<String, AbstractApplicationContext> appContextMap = new HashMap<String, AbstractApplicationContext>();
	
	/**
	 * Interface method implementation. Creates and loads a Spring ApplicationContext for each property specified in {@link #getAllProperties()}
	 * @see org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension#init()
	 */
	public void init() {
		// Iterate through the properties to get ApplicationContext name and the corresponding file name
		for (String key : this.getAllProperties().keySet()) {
			String fileName = this.getAllProperties().get(key);
			try {
				File springBeansFile = FileLocator.findUniqueFile(fileName);
				// add the "file:" prefix to file names to get around strange behavior of FileSystemXmlApplicationContext that converts 
				// absolute path to relative path
				
				// Set the commons beans context as the parent of all application contexts created through this ApplicationContextFactory
				AbstractApplicationContext appContext = new FileSystemXmlApplicationContext(new String[] {FILE_PREFIX + 
						springBeansFile.getAbsolutePath()}, getCommonBeansContext());
				this.appContextMap.put(key.toLowerCase(), appContext);
				
			} catch (Exception e) { // blanket catch for all checked and unchecked exceptions
				LOGGER.error("Error loading ApplicationContext. [Name][Path] : [" + key + "][" + fileName + "].Error is : "+ e.getMessage(),e);
				this.bootstrapOutcome = BootstrapExtension.VETO_BOOTSTRAP;
				return;
			}			
		}
	}

	/**
	 * Interface method implementation. 
	 * @see org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension#getOutcomeStatus()
	 */
	public int getOutcomeStatus() {
		return this.bootstrapOutcome;
	}
	
	/**
	 * Interface method implementation. Closes all Spring application contexts that are maintained by this BootstrapExtension
	 * @see org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension#destroy()
	 */
	public void destroy() {
		for (AbstractApplicationContext appContext : this.appContextMap.values()) {
			appContext.close();
			appContext = null;
		}
		appContextMap.clear();
	}

	/**
	 * Returns the common Spring beans application context that is intended as parent of all application contexts created by the runtime
	 * @return the AbstractApplicationContext for the XML identified by {@link RuntimeConstants#COMMON_SPRING_BEANS_CONFIG}
	 */
	private AbstractApplicationContext getCommonBeansContext() {
		// commonBeansContext is the base context for all application contexts, so load it if not loaded already.
		AbstractApplicationContext commonBeansContext = (AbstractApplicationContext)appContextMap.get((COMMON_BEANS_CONTEXT_NAME).toLowerCase());
		if (commonBeansContext == null) { 
			File springBeansFile = FileLocator.findUniqueFile(RuntimeConstants.COMMON_SPRING_BEANS_CONFIG);
			// add the "file:" prefix to file names to get around strange behavior of FileSystemXmlApplicationContext that converts absolute path 
			// to relative path
			commonBeansContext = new FileSystemXmlApplicationContext(FILE_PREFIX + 
					springBeansFile.getAbsolutePath());
			appContextMap.put(COMMON_BEANS_CONTEXT_NAME.toLowerCase(), commonBeansContext);
		}
		return commonBeansContext;
	}
	
}
