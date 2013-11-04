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
package org.trpr.platform.servicefw.impl.spring;

import java.io.File;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;

/**
 * The <code>ServiceConfigInfo</code> class is a structure that holds service configuration information and the ApplicationContext for the service(s)
 * 
 * @author Regunath B
 */
public class ServiceConfigInfo {

	/** The sub-folder containing Service and dependent binaries. This is used in addition to the service runtime classpath.
	 *  This path is relative to the location where {@link ServiceFrameworkConstants#SPRING_SERVICES_CONFIG} file is found 
	 */
	public static final String BINARIES_PATH = "lib";

	/** The prefix to be added to file absolute paths when loading Spring XMLs using the FileSystemXmlApplicationContext*/
	public static final String FILE_PREFIX = "file:";
	
	/** The the {@link ServiceFrameworkConstants#SPRING_SERVICES_CONFIG} file containing service bean */
	private File serviceConfigXML;
	
	/** The path to Service and dependent binaries*/
	private String binariesPath = ServiceConfigInfo.BINARIES_PATH;
	
	/** The Spring ApplicationContext initialized using information contained in this ServiceConfigInfo*/
	private AbstractApplicationContext serviceContext;
	
	/**
	 * Constructors
	 */
	public ServiceConfigInfo(File serviceConfigXML) {
		this.serviceConfigXML = serviceConfigXML;
	}
	public ServiceConfigInfo(File serviceConfigXML, String binariesPath) {
		this(serviceConfigXML);
		this.binariesPath = binariesPath;
	}
	public ServiceConfigInfo(File serviceConfigXML, String binariesPath,AbstractApplicationContext serviceContext) {
		this(serviceConfigXML,binariesPath);
		this.serviceContext = serviceContext;
	}

	/**
	 * Loads and returns an AbstractApplicationContext using data contained in this class
	 * @return the service's AbstractApplicationContext
	 */
	protected AbstractApplicationContext loadServiceContext(ClassLoader classLoader) {
		ClassLoader existingTCCL = Thread.currentThread().getContextClassLoader();
		// set the custom classloader as the tccl for loading the service
		Thread.currentThread().setContextClassLoader(classLoader);
		// add the "file:" prefix to file names to get around strange behavior of FileSystemXmlApplicationContext that converts absolute path 
		// to relative path
		this.serviceContext = new FileSystemXmlApplicationContext(new String[]{FILE_PREFIX + serviceConfigXML.getAbsolutePath()}, 
				SpringServicesContainer.getCommonServiceBeansContext());
		// now reset the thread's TCCL to the one that existed prior to loading the service
		Thread.currentThread().setContextClassLoader(existingTCCL);
		return this.serviceContext;
	}

	/**
	 * Overriden super type method. Returns true if the path to the service context is the same i.e. loaded from the same file
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		ServiceConfigInfo otherConfigInfo = (ServiceConfigInfo)object;
		return this.getServiceConfigXML().getAbsolutePath().equalsIgnoreCase(otherConfigInfo.getServiceConfigXML().getAbsolutePath());
	}
	
	/** Getter methods*/	
	/**
	 * Returns the service's ApplicationContext, if loaded, else null
	 * @return null or the service's AbstractApplicationContext
	 */
	public AbstractApplicationContext getServiceContext() {
		return this.serviceContext;
	}
	public File getServiceConfigXML() {
		return this.serviceConfigXML;
	}
	public String getBinariesPath() {
		return this.binariesPath;
	}
	
}
