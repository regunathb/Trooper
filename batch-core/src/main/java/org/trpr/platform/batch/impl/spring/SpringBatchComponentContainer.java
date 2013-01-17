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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.trpr.platform.batch.BatchFrameworkConstants;
import org.trpr.platform.batch.spi.spring.admin.JobService;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.spi.event.PlatformEventProducer;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.common.RuntimeConstants;
import org.trpr.platform.runtime.common.RuntimeVariables;
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

	/**
	 * The default Event producer bean name 
	 */
	private static final String DEFAULT_EVENT_PRODUCER = "platformEventProducer";
	
    /** The common batch beans context*/
    private static AbstractApplicationContext commonBatchBeansContext;    
    
	/**
	 * The list of BatchConfigInfo holding all job instances loaded by this container
	 */
    private List<BatchConfigInfo> jobsContextList = new LinkedList<BatchConfigInfo>();	
    
    /** Local reference for all BootstrapExtensionS loaded by the Container and set on this ComponentContainer*/
    private BootstrapExtension[] loadedBootstrapExtensions;
    
	/** The Thread's context class loader that is used in on the fly loading of Job definitions */
	private ClassLoader tccl; 
    
	/**
	 * Returns the common Batch Spring beans application context that is intended as parent of all batch application contexts 
	 * WARN : this method can retun null if this ComponentContainer is not suitably initialized via a call to {@link #init()}
	 * @return null or the common batch AbstractApplicationContext
	 */
	public static AbstractApplicationContext getCommonBatchBeansContext() {
		return SpringBatchComponentContainer.commonBatchBeansContext;
	}
	
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
		
		// store the thread's context class loader for later use in on the fly loading of Job app contexts
		this.tccl = Thread.currentThread().getContextClassLoader();
		
		// The common batch beans context is loaded first using the Platform common beans context as parent
		// load this from classpath as it is packaged with the binaries
		ApplicationContextFactory defaultCtxFactory = null;
		for (BootstrapExtension be : this.loadedBootstrapExtensions) {
			if (ApplicationContextFactory.class.isAssignableFrom(be.getClass())) {
				defaultCtxFactory = (ApplicationContextFactory)be;
				break;
			}
		}

		SpringBatchComponentContainer.commonBatchBeansContext = new ClassPathXmlApplicationContext(new String[]{BatchFrameworkConstants.COMMON_BATCH_CONFIG}, 
				defaultCtxFactory.getCommonBeansContext());
		
		// Look up the JobService bean and set this class instance as the ComponentContainer that loaded it
		((JobService)SpringBatchComponentContainer.commonBatchBeansContext.getBean(BatchFrameworkConstants.JOB_SERVICE_BEAN)).setComponentContainer(this);
		
		// add the common batch beans to the contexts list
		this.jobsContextList.add(new BatchConfigInfo(new File(BatchFrameworkConstants.COMMON_BATCH_CONFIG), null, SpringBatchComponentContainer.commonBatchBeansContext));

		// Load additional if runtime nature is "server". This context is the new common beans context
		if (RuntimeVariables.getRuntimeNature().equalsIgnoreCase(RuntimeConstants.SERVER)) {
			SpringBatchComponentContainer.commonBatchBeansContext = new ClassPathXmlApplicationContext(new String[]{BatchFrameworkConstants.COMMON_BATCH_SERVER_NATURE_CONFIG},
					SpringBatchComponentContainer.commonBatchBeansContext);
			// now add the common server nature batch beans to the contexts list
			this.jobsContextList.add(new BatchConfigInfo(new File(BatchFrameworkConstants.COMMON_BATCH_SERVER_NATURE_CONFIG), null, 
					SpringBatchComponentContainer.commonBatchBeansContext));
		}
		
		// locate and load the individual job bean XML files using the common batch beans context as parent
		File[] jobBeansFiles = FileLocator.findFiles(BatchFrameworkConstants.SPRING_BATCH_CONFIG);					
		for (File jobBeansFile : jobBeansFiles) {
			BatchConfigInfo jobConfigInfo = new BatchConfigInfo(jobBeansFile);
			// load the job's appcontext
			this.loadJobContext(jobConfigInfo);
		}
		
	}

	/**
	 * Interface method implementation. Destroys the Spring application context containing loaded job definitions.
	 * @see ComponentContainer#destroy()
	 */
	public void destroy() throws PlatformException {
		for (BatchConfigInfo batchConfigInfo : this.jobsContextList) {
			batchConfigInfo.getJobContext().close();
		}
		this.jobsContextList = null;		
	}
	
	/**
	 * Interface method implementation. Publishes the specified event to using a named bean DEFAULT_EVENT_PRODUCER looked up from the 
	 * commons jobs context (i.e. BatchFrameworkConstants.COMMON_BATCH_CONFIG).
	 * Note that typically no consumers are registered when running this container
	 */ 
	public void publishEvent(PlatformEvent event) {
		PlatformEventProducer publisher= (PlatformEventProducer)SpringBatchComponentContainer.commonBatchBeansContext.getBean(DEFAULT_EVENT_PRODUCER);
		publisher.publishEvent(event);
	}
	    
	/**
	 * Interface method implementation. Publishes the specified event using the {@link #publishEvent(PlatformEvent)} method
	 * @see ComponentContainer#publishBootstrapEvent(PlatformEvent)
	 */
	public void publishBootstrapEvent(PlatformEvent bootstrapEvent) {	
		this.publishEvent(bootstrapEvent);
	}
	
	/**
	 * Interface method implementation. Loads/Reloads batch job(s) defined in the specified {@link FileSystemResource} 
	 * @see org.trpr.platform.runtime.spi.component.ComponentContainer#loadComponent(org.springframework.core.io.Resource)
	 */
	public void loadComponent(Resource resource) {
		if (!FileSystemResource.class.isAssignableFrom(resource.getClass()) || 
				!((FileSystemResource)resource).getFilename().equalsIgnoreCase(BatchFrameworkConstants.SPRING_BATCH_CONFIG)) {
			throw new UnsupportedOperationException("Batch jobs can be loaded only from files by name : " + BatchFrameworkConstants.SPRING_BATCH_CONFIG);
		}
		loadJobContext(new BatchConfigInfo(((FileSystemResource)resource).getFile()));
	}
	
	/**
	 * Loads the job context from path specified in the BatchConfigInfo. Looks for file by name BatchFrameworkConstants.SPRING_BATCH_CONFIG. 
	 * @param batchConfigInfo containing absolute path to the job's configuration location i.e. folder
	 */
	public void loadJobContext(BatchConfigInfo batchConfigInfo) {
		// check if a context exists already for this config path 
		for (BatchConfigInfo loadedJobInfo : this.jobsContextList) {
			if (loadedJobInfo.equals(batchConfigInfo)) {
				batchConfigInfo = loadedJobInfo;
				break;
			}
		}
		if (batchConfigInfo.getJobContext() != null) {
			// close the context and remove from list
			batchConfigInfo.getJobContext().close();
			this.jobsContextList.remove(batchConfigInfo);
		}
		ClassLoader jobCL = this.tccl;
		// check to see if the job has job and dependent binaries deployed outside of the runtime class path. If yes, include them using a custom URL classloader.
		File customLibPath = new File (batchConfigInfo.getJobConfigXML().getParentFile(), BatchConfigInfo.BINARIES_PATH);
		if (customLibPath.exists() && customLibPath.isDirectory()) {
			try {
				File[] libFiles = customLibPath.listFiles();
				URL[] libURLs = new URL[libFiles.length];
				for (int i=0; i < libFiles.length; i++) {
					libURLs[i] = new URL(BatchConfigInfo.FILE_PREFIX + libFiles[i].getAbsolutePath());
				}
				jobCL = new URLClassLoader(libURLs, this.tccl);
			} catch (MalformedURLException e) {
				throw new PlatformException(e);
			}
		} 
		// now load the job context and add it into the jobcontexts list
		batchConfigInfo.loadJobContext(jobCL);
		this.jobsContextList.add(batchConfigInfo);
	}	
}
