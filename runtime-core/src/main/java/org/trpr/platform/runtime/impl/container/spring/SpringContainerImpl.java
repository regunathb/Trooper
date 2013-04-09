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
package org.trpr.platform.runtime.impl.container.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.common.RuntimeConstants;
import org.trpr.platform.runtime.common.RuntimeVariables;
import org.trpr.platform.runtime.impl.bootstrapext.BootstrapExtensionInfo;
import org.trpr.platform.runtime.impl.bootstrapext.spring.ApplicationContextFactory;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension;
import org.trpr.platform.runtime.spi.component.ComponentContainer;
import org.trpr.platform.runtime.spi.container.Container;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * The <code>SpringContainerImpl</code> is a concrete implementation of the {@link Container} interface. This implementation loads the configured {@link ComponentContainer},
 * if any, and any {@link BootstrapExtension} instances configured.
 * 
 * @author Regunath B
 * @version 1.0, 05/06/2012
 */
public class SpringContainerImpl implements Container {
	
	/** Name for this Container */
	private static final String NAME = SpringContainerImpl.class.getName();
	
	/** The prefix to be added to file absolute paths when loading Spring XMLs using the FileSystemXmlApplicationContext*/
	private static final String FILE_PREFIX = "file:";	

	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(SpringContainerImpl.class);

	/**  Component Container */
	private ComponentContainer componentContainer;
	
	/** Bootstrap Extensions */
	private BootstrapExtension[] bootstrapExtensions;
	
	/**
	 * No args constructor
	 */
	public SpringContainerImpl() {
	}
	
	/**
	 * Interface method implementation. Returns the fully qualified class name
	 * @see Container#getName()
	 */
	public String getName() {
		return SpringContainerImpl.NAME;
	}

	/**
	 * Interface method implementation. Loads the specified ComponentContainer, if any, and any BootstrapExtensionS
	 * @see Container#init()
	 */
	public void init() throws PlatformException {
		
		// Initialize the bootstrap extensions. This is done before the component container is loaded as components may depend on bootstrap
		// extensions like ApplicationContextFactory		
		initializeBootstrapExtensions();

		// instantiate the ComponentContainer if it has been configured in bootstrap config
	    String componentType = RuntimeVariables.getContainerType();		
		if(componentType != null){
    		try {
    			componentContainer = (ComponentContainer)Class.forName(componentType).newInstance();
    		} catch (Exception e) {
    			LOGGER.error("Error while instantiating component container : " + e.getMessage(),e);
    		}	
		}			
		if(this.componentContainer != null){
			LOGGER.info("** Starting a component container of type : " + this.componentContainer.getClass().getName() + " **");
			this.componentContainer.setLoadedBootstrapExtensions(this.bootstrapExtensions);
			this.componentContainer.init();
		} else {
			LOGGER.info("No component container configured for this runtime instance.");			
		}				
	}	

	/**
	 * Interface method implementation. Destroys this container and all components loaded by it
	 * Container#destroy()
	 */
	public void destroy() throws PlatformException {
		if(this.componentContainer != null){
			this.componentContainer.destroy();
		}
		this.destroyBootstrapExtensions();		
	}

	/**
	 * Interface method implementation. Gets the ComponentContainer, if any, loaded by this Container.
	 * @see Container#getComponentContainer()
	 */
	public ComponentContainer getComponentContainer() {
		return this.componentContainer;
	}

	/**
	 * Interface method implementation. Invokes the namesake method on the configured {@link ComponentContainer}
	 * @see org.trpr.platform.runtime.spi.container.Container#publishBootstrapEvent(org.trpr.platform.model.event.PlatformEvent)
	 */
	public void publishBootstrapEvent(PlatformEvent bootstrapEvent) {
		if (this.getComponentContainer() != null) { // check if a ComponentContainer has been set and publish the bootstrap event to it
			this.getComponentContainer().publishBootstrapEvent(bootstrapEvent);
		}
	}

	/**
	 * Helper method that locates and loads all Bootstrap extensions
	 */
	private void initializeBootstrapExtensions() {		
		File[] bootstrapExtensionFiles = FileLocator.findFiles(RuntimeConstants.BOOTSTRAP_EXTENSIONS_FILE);
		// Create the Bootstrap Extension dependency manager that will load all bootstrap extensions
		BootstrapExtensionDependencyManager beManager = new BootstrapExtensionDependencyManager(this);
		
		// add the ApplicationContextFactory as a default BE
		beManager.addBootstrapExtensionInfo(new BootstrapExtensionInfo(ApplicationContextFactory.COMMON_BEANS_CONTEXT_NAME, ApplicationContextFactory.class.getName(), true));
		
		for (File beFile : bootstrapExtensionFiles) {
			try {
				// add the "file:" prefix to file names to get around strange behavior of FileSystemXmlApplicationContext that converts absolute path 
				// to relative path
				AbstractApplicationContext beDefinitionsContext = new FileSystemXmlApplicationContext(FILE_PREFIX + beFile.getAbsolutePath());
				// All beans in the BE definitions context are expected to be of type BootstrapExtensionInfo. 
				// We look up and load only these to BootstrapExtensionDependencyManager
				String[] beInfos = beDefinitionsContext.getBeanNamesForType(BootstrapExtensionInfo.class);
				for (String beInfo : beInfos) {
					beManager.addBootstrapExtensionInfo((BootstrapExtensionInfo)beDefinitionsContext.getBean(beInfo));
				}
				// destroy the beDefinitionsContext as we dont need it anymore
				beDefinitionsContext.destroy();
			} catch (Exception e) {
				LOGGER.error("Error in loading BootStrap Extension File. Ignoring contents of : " + beFile.getAbsolutePath() + " .Error message : " + e.getMessage(), e);
			}
		}		
		this.bootstrapExtensions = (BootstrapExtension[]) beManager.loadBootstrapExtensions().toArray(new BootstrapExtension[0]);
	}
	
	/**
	 * Helper method that destroys all loaded Bootstrap extensions
	 */
	private void destroyBootstrapExtensions() {
		if (this.bootstrapExtensions != null) {
			for (BootstrapExtension be : bootstrapExtensions) {
				be.destroy();
			}
		}
	}	
	
	/**
	 * Helper class that loads Bootstrap extensions considering dependencies among them. Also validates if there are
	 * cyclical dependencies among the bootstrap extensions.
	 * The scope of all methods are intentionally kept to default level to prevent access from outside
	 */
	private class BootstrapExtensionDependencyManager {

		/** The ContainerImpl instance that creates this manager and holds all loaded BootstrapExtension instances*/
		private SpringContainerImpl container;
		
		/** HashMap containing Bootstrap extension information hashed by the name*/
		private HashMap<String, BootstrapExtensionInfo> beInfoMap = new HashMap<String, BootstrapExtensionInfo>();
		
		/** LinkedList containing loaded Bootstrap extension names*/
		private LinkedList<String> loadedBEListNames = new LinkedList<String>();
		
		/** LinkedList containing loaded Bootstrap extensions*/
		private List<BootstrapExtension> loadedBEList = new LinkedList<BootstrapExtension>();
		
		/**
		 * Constructor for this class
		 * @param container the ContainerImpl instance that created this BootstrapExtensionDependencyManager
		 */
		BootstrapExtensionDependencyManager(SpringContainerImpl container) {
			this.container = container;
		}
		
		/**
		 * Adds the specified Bootstrap Extension configuration for loading by this  BootstrapExtensionDependencyManager
		 * @param beInfo Bootstrap extension configuration data
		 */
		void addBootstrapExtensionInfo(BootstrapExtensionInfo beInfo) {
			this.beInfoMap.put(beInfo.getBeName(), beInfo);
		}

		/**
		 * Loads and returns the Bootstrap extensions whose configuration was added to this BootstrapExtensionDependencyManager
		 * @return List containing loaded BootstrapExtension instances
		 */
		List<BootstrapExtension> loadBootstrapExtensions() {
			ArrayList<BootstrapExtensionInfo> beInfoList = new ArrayList<BootstrapExtensionInfo>(beInfoMap.values());
			// sort by Bootstrap extension names and then load. Helps to ensure predictable order of loading when no
			// dependencies are specified. Useful when debugging
			Collections.sort(beInfoList);
			for (BootstrapExtensionInfo beInfo : beInfoList.toArray(new BootstrapExtensionInfo[0])) {
				try {
					checkAndloadBootstrapExtension(beInfo, new LinkedList<String>());
				} catch (PlatformException e) {
					LOGGER.error("Runtime exit. " + e.getMessage());
					try {
						this.container.destroy();
					} catch (PlatformException e1) {
						// do nothing as the VM would need to exit anyway
					}
					// kill the VM
					System.exit(0);
				}
			}
			return this.loadedBEList;
		}
		
		/**
		 * Helper method that recursively loads the bootstrap extension from specified BootstrapExtensionInfo.
		 * Uses the specified LinkedList containing the entire dependency chain for the bootstrap extension in order
		 * to determine cyclic dependencies when loading dependent bootstrap extensions
		 * @param beInfo BootstrapExtensionInfo for the bootstrap extension to be loaded
		 * @param dependencies LinkedList containing the entire dependency chain for a bootstrap extension including ones that refer to it 
		 * @throws PlatformException thrown in case a cyclical dependency is found
		 */
		private void checkAndloadBootstrapExtension(BootstrapExtensionInfo beInfo, LinkedList<String> dependencies) throws PlatformException{
			if (beInfo.getDependenciesList().size() == 0) {
				loadBootstrapExtension(beInfo);
				return;
			}
			if (dependencies.contains(beInfo.getBeName())) {
				StringBuffer cyclicalDependencyBuffer = new StringBuffer("Cyclical Dependency found for (" + beInfo.getBeName() + ") : ");
				for (String dependencyItem : dependencies) {
					cyclicalDependencyBuffer.append(dependencyItem + " --> ");
				}
				cyclicalDependencyBuffer.append(beInfo.getBeName());
				throw new PlatformException(cyclicalDependencyBuffer.toString());
			}
			for (String dependency : beInfo.getDependenciesList()) {
				dependencies.add(beInfo.getBeName());
				checkAndloadBootstrapExtension(this.beInfoMap.get(dependency), dependencies);
			}
			loadBootstrapExtension(beInfo);
			dependencies.add(beInfo.getBeName());
		}

		/**
		 * Loads the bootstrap extension using information contained in the specified BootstrapExtensionInfo
		 * @param beInfo the BootstrapExtensionInfo that holds information for loading the bootstrap extension
		 * @throws PlatformException thrown in case there are any errors in loading the BootstrapExtension
		 */
		private void loadBootstrapExtension(BootstrapExtensionInfo beInfo) throws PlatformException {
			if (loadedBEListNames.contains(beInfo.getBeName())) {
				return;
			}
			try {
				String className = beInfo.getBeClassName();
				// check if bootstrap extension is enabled. default is true
				if (beInfo.isEnabled()) {
					// Use the current thread's context class loader to find the extension class. All dependent classes
					// must be available to the context class loader for this dynamic loading to succeed
					BootstrapExtension be = (BootstrapExtension) Class.forName(className,true,Thread.currentThread().getContextClassLoader()).newInstance();
					be.setName(beInfo.getBeName());
					be.setProperties(beInfo.getBeProperties());
					LOGGER.info("Initializing Bootstrap extension .... "+ beInfo.getBeName());
					// add all dependency Bootstrap extensions that have been loaded already
					for (String dependencyBEInfo : beInfo.getDependenciesList()) {
						for (BootstrapExtension dependencyBE : this.loadedBEList) {
							if (dependencyBE.getName().equalsIgnoreCase(dependencyBEInfo)) {
								be.addDependency(dependencyBE);
							}
						}
					}
					// now init the Bootstrap Extension
					be.init();

					// check if this extension has vetoed the bootstrap process and kill the VM
					if (be.getOutcomeStatus() == BootstrapExtension.VETO_BOOTSTRAP) {
						LOGGER.error("Runtime exit. Bootstrap process vetoed by Extension : "+ beInfo.getBeName());
						this.container.destroy();
						// kill the VM
						System.exit(0);
					}
					this.loadedBEList.add(be);
				}
			} catch (Exception e) {
				LOGGER.error("Error instantiating boot strap extension : " + beInfo.getBeName() + ". Error is : " + e.getMessage(),e);
			}		
			loadedBEListNames.add(beInfo.getBeName());
		}
	}
	
}
