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

import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * The <code>BatchConfigInfo</code> class is a structure that holds job configuration information and the ApplicationContext for the job
 * 
 * @author Regunath B
 */
public class BatchConfigInfo {

	/** The sub-folder containing Job and dependent binaries. This is used in addition to the batch runtime classpath.
	 *  This path is relative to the location where BatchFrameworkConstants.SPRING_BATCH_CONFIG file is found 
	 */
	public static final String BINARIES_PATH = "lib";

	/** The prefix to be added to file absolute paths when loading Spring XMLs using the FileSystemXmlApplicationContext*/
	public static final String FILE_PREFIX = "file:";
	
	/** The the BatchFrameworkConstants.SPRING_BATCH_CONFIG file containing job bean */
	private File jobConfigXML;
	
	/** The path to Job and dependent binaries*/
	private String binariesPath = BatchConfigInfo.BINARIES_PATH;
	
	/** The Spring ApplicationContext initialized using information contained in this BatchConfigInfo*/
	private AbstractApplicationContext jobContext;
	
	/**
	 * Constructors
	 */
	public BatchConfigInfo(File jobConfigXML) {
		this.jobConfigXML = jobConfigXML;
	}
	public BatchConfigInfo(File jobConfigXML, String binariesPath) {
		this(jobConfigXML);
		this.binariesPath = binariesPath;
	}
	public BatchConfigInfo(File jobConfigXML, String binariesPath,AbstractApplicationContext jobContext) {
		this(jobConfigXML,binariesPath);
		this.jobContext = jobContext;
	}

	/**
	 * Loads and returns an AbstractApplicationContext using data contained in this class
	 * @return the job's AbstractApplicationContext
	 */
	protected AbstractApplicationContext loadJobContext(ClassLoader classLoader) {
		ClassLoader existingTCCL = Thread.currentThread().getContextClassLoader();
		// set the custom classloader as the tccl for loading the job
		Thread.currentThread().setContextClassLoader(classLoader);
		// add the "file:" prefix to file names to get around strange behavior of FileSystemXmlApplicationContext that converts absolute path 
		// to relative path
		this.jobContext = new FileSystemXmlApplicationContext(new String[]{FILE_PREFIX + jobConfigXML.getAbsolutePath()}, 
				SpringBatchComponentContainer.getCommonBatchBeansContext());
		Thread.currentThread().setContextClassLoader(existingTCCL);
		return this.jobContext;
	}

	/**
	 * Overriden super type method. Returns true if the path to the job context is the same i.e. loaded from the same file
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		BatchConfigInfo otherConfigInfo = (BatchConfigInfo)object;
		return this.getJobConfigXML().getAbsolutePath().equalsIgnoreCase(otherConfigInfo.getJobConfigXML().getAbsolutePath());
	}
	
	/** Getter methods*/	
	/**
	 * Returns the job's ApplicationContext, if loaded, else null
	 * @return null or the job's AbstractApplicationContext
	 */
	public AbstractApplicationContext getJobContext() {
		return this.jobContext;
	}
	public File getJobConfigXML() {
		return this.jobConfigXML;
	}
	public String getBinariesPath() {
		return this.binariesPath;
	}
	
}
