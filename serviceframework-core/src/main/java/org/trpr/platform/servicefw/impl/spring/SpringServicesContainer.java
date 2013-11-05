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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.event.PlatformEventProducer;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.common.RuntimeConstants;
import org.trpr.platform.runtime.common.RuntimeVariables;
import org.trpr.platform.runtime.impl.bootstrapext.spring.ApplicationContextFactory;
import org.trpr.platform.runtime.impl.config.FileLocator;
import org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension;
import org.trpr.platform.runtime.spi.component.ComponentContainer;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.ServiceContext;
import org.trpr.platform.servicefw.ServiceRegistry;
import org.trpr.platform.servicefw.common.ServiceException;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.BrokerFactory;
import org.trpr.platform.servicefw.impl.ServiceCompartmentImpl;
import org.trpr.platform.servicefw.impl.ServiceKeyImpl;
import org.trpr.platform.servicefw.impl.ServiceStatisticsGatherer;
import org.trpr.platform.servicefw.impl.spring.admin.ConfigurationServiceImpl;
import org.trpr.platform.servicefw.spi.Service;
import org.trpr.platform.servicefw.spi.ServiceCompartment;
import org.trpr.platform.servicefw.spi.ServiceContainer;
import org.trpr.platform.servicefw.spi.ServiceInfo;
import org.trpr.platform.servicefw.spi.ServiceKey;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;
import org.trpr.platform.servicefw.spi.event.ServiceEventProducer;

/**
 * The <code>SpringServicesContainer</code> class is a {@link ServiceContainer} implementation that uses Spring to manage the service implementations.
 * This container locates and loads all service definitions contained in files named by the value of {@link ServiceFrameworkConstants#SPRING_SERVICES_CONFIG}. 
 * This container also ensures that all beans declared in {@link ServiceFrameworkConstants#COMMON_SPRING_SERVICES_CONFIG} and 
 * {@link ServiceFrameworkConstants#COMMON_SPRING_SERVICES_CONFIG} are available to the service beans by
 * specifying the common beans contexts as the parent for the services application context created by this container.
 * 
 * @see ServiceContainer
 * @author Regunath B
 * @version 1.0, 16/08/2012
 * @version 2.0, 08/11/2013
 */
public class SpringServicesContainer<T extends PlatformServiceRequest, S extends PlatformServiceResponse> implements ServiceContainer<T,S> {

	/** The logger for this class */
	private static final Logger LOGGER = LogFactory.getLogger(SpringServicesContainer.class);
	
	/** The separator char i.e '_' between service name and its version*/
	private static final String SERVICE_VERSION_SEPARATOR= "_";
	
	/**
	 * The default Event producer bean name 
	 */
	private static final String DEFAULT_EVENT_PRODUCER = "platformEventProducer";
	
	/** The bean names of the service framework classes initialized by this container*/
	private static final String SERVICE_STATISTICS_BEAN = "serviceStatisticsGatherer";
	private static final String SERVICE_CONTEXT_BEAN = "serviceContext";
	private static final String SERVICE_REGISTRY_BEAN = "serviceRegistry";
	
	/**
	 * The list of ServiceConfigInfo holding all service instances loaded by this container
	 */
    private List<ServiceConfigInfo> servicesContextList = new LinkedList<ServiceConfigInfo>();	
    
	/** The Thread's context class loader that is used in on the fly loading of Service definitions */
	private ClassLoader tccl;     
	
	/** The ServiceContext initialized by this ServiceContainer*/
	@SuppressWarnings("rawtypes")
	private ServiceContext serviceContext;
	
	/** The ServiceRegistry initialized by this ServiceContainer*/
	private ServiceRegistry serviceRegistry;
		
    /** The common batch beans context*/
    private static AbstractApplicationContext commonServiceBeansContext;    	
    		
	/** Collection of ServiceCompartments */
	private HashMap<ServiceKey, ServiceCompartment<T,S>> serviceCompartments = null;
	
	/** Collection of ServiceInfos */
	private HashMap<ServiceKey, ServiceInfo> serviceInfos = null;
	
    /** Local reference for all BootstrapExtensionS loaded by the Container and set on this ComponentContainer*/
    private BootstrapExtension[] loadedBootstrapExtensions;	

    /** The configuration Service instance */
    private ConfigurationServiceImpl configurationService;
    
    /**
     * Static accessor for the common service beans context
     * TODO : Find a better way to provide access to the common service beans. Exposing the ApplicationContext this way is not desirable.
     * @return the loaded common service beans context
     */
	public static AbstractApplicationContext getCommonServiceBeansContext() {
		return commonServiceBeansContext;
	}
	    
	/**
	 * Interface method implementation
	 * @see ServiceContainer#init()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() throws PlatformException {
		
		// store the thread's context class loader for later use in on the fly loading of Job app contexts
		this.tccl = Thread.currentThread().getContextClassLoader();
				
		// The common service beans context is loaded first using the Platform common beans context as parent
		// load this from classpath as it is packaged with the binaries
		ApplicationContextFactory defaultCtxFactory = null;
		for (BootstrapExtension be : this.loadedBootstrapExtensions) {
			if (ApplicationContextFactory.class.isAssignableFrom(be.getClass())) {
				defaultCtxFactory = (ApplicationContextFactory)be;
				break;
			}
		}
		
		SpringServicesContainer.commonServiceBeansContext =  new ClassPathXmlApplicationContext(new String[]{ServiceFrameworkConstants.COMMON_SPRING_SERVICES_CONFIG},
				defaultCtxFactory.getCommonBeansContext());			
		
		// add the common batch beans to the contexts list
		this.registerServiceContext(new ServiceConfigInfo(new File(ServiceFrameworkConstants.COMMON_SPRING_SERVICES_CONFIG), null, SpringServicesContainer.commonServiceBeansContext));

        // Load additional if runtime nature is "server". This context is the new common beans context
		if (RuntimeVariables.getRuntimeNature().equalsIgnoreCase(RuntimeConstants.SERVER)) {
			SpringServicesContainer.commonServiceBeansContext =  new ClassPathXmlApplicationContext(new String[]{ServiceFrameworkConstants.COMMON_SERVICES_SERVER_NATURE_CONFIG},
					SpringServicesContainer.commonServiceBeansContext);
			// now add the common server nature service beans to the contexts list
			this.registerServiceContext(new ServiceConfigInfo(new File(ServiceFrameworkConstants.COMMON_SERVICES_SERVER_NATURE_CONFIG), null, 
					SpringServicesContainer.commonServiceBeansContext));
			
		}
		
		// now initialize context, statistics gatherer and registry
		this.serviceContext = (ServiceContext)SpringServicesContainer.commonServiceBeansContext.getBean(SpringServicesContainer.SERVICE_CONTEXT_BEAN);
		this.serviceContext.setServiceContainer(this);
		((ServiceStatisticsGatherer)SpringServicesContainer.commonServiceBeansContext.getBean(SpringServicesContainer.SERVICE_STATISTICS_BEAN)).setServiceContainer(this);
        this.serviceRegistry = (ServiceRegistry)SpringServicesContainer.commonServiceBeansContext.getBean(SpringServicesContainer.SERVICE_REGISTRY_BEAN);

        this.configurationService = SpringServicesContainer.commonServiceBeansContext.getBean(ConfigurationServiceImpl.class);
        this.configurationService.setSpringServicesContainer(this);

        this.serviceCompartments = new HashMap<ServiceKey, ServiceCompartment<T,S>>();
        this.serviceInfos = new HashMap<ServiceKey, ServiceInfo>();
        
        // load the service bean contexts
        this.loadServiceContexts();

		// Set this ServiceContainer and ServiceRegistry on the BrokerFactory TODO : Need a better way of doing this
		BrokerFactory.setServiceContainer(this);
		BrokerFactory.setServiceRegistry(this.serviceRegistry);
	}

	/**
	 * Interface method implementation
	 * @see ServiceContainer#destroy()
	 */
	public void destroy() throws PlatformException {
		this.resetContainer();
	}
	
	/**
	 * Interface method implementation
	 * @see ServiceContainer#getAllLocalServices()
	 */
	public ServiceKey[] getAllLocalServices() {
		Set<ServiceKey> serviceKeys = serviceInfos.keySet();
		return (ServiceKey[])(serviceKeys.toArray(new ServiceKey[serviceKeys.size()]));
	}

	/**
	 * Interface method implementation
	 * @see ServiceContainer#getCompartment(ServiceKey)
	 */
	public ServiceCompartment<T,S> getCompartment(ServiceKey serviceKey) {
		// always get the service key resolved from the service registry
		if (this.serviceRegistry.getServiceInfo(serviceKey) == null) {
			throw new ServiceException(BrokerFactory.getMissingServiceMessage(serviceKey));
		}
		return (ServiceCompartment<T,S>)serviceCompartments.get(this.serviceRegistry.getServiceInfo(serviceKey).getServiceKey());
	}

	/**
	 * Interface method implementation
	 * @see ServiceContainer#getServiceInfo(ServiceKey)
	 */
	public ServiceInfo getServiceInfo(ServiceKey serviceKey) {
		// always get the service key resolved from the service registry
		return (ServiceInfo)serviceInfos.get(this.serviceRegistry.getServiceInfo(serviceKey).getServiceKey());
	}

	/**
	 * Interface method implementation
	 * @see ServiceContainer#invokeService(ServiceInfo, ServiceRequest)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ServiceResponse invokeService(ServiceInfo serviceInfo, ServiceRequest request) throws ServiceException {
		return getCompartment(serviceInfo.getServiceKey()).processRequest(request);
	}

	/**
	 * Interface method implementation. Publishes the specified event to the specified endpoint URI
	 * using a named bean DEFAULT_EVENT_PRODUCER looked up from the servicesContext (i.e. spring-services-config.xml).
	 * Note that the endpoint URI is ignored and typically no consumers are registered when running this container 
	 * @see ServiceEventProducer#publishEvent(PlatformEvent, String)
	 */
	public void publishEvent(PlatformEvent event, String endpointURI) {
		((PlatformEventProducer)SpringServicesContainer.commonServiceBeansContext.getBean(DEFAULT_EVENT_PRODUCER)).publishEvent(event);
	}

	/**
	 * Interface method implementation. Invokes {@link SpringServicesContainer#publishEvent(PlatformEvent, String)} with
	 * null for endpointURI.
	 * @see PlatformEventProducer#publishEvent(PlatformEvent)
	 */
	public void publishEvent(PlatformEvent event) {
		this.publishEvent(event, null);
	}
	
	/**
	 * Interface method implementation. Returns boolean "false" as check-pointing is not needed by this ServiceContainer which is unaware of
	 * service chaining and also lacks the ability
	 * @see ServiceContainer#isServiceExecutionCheckPointingRequired()
	 */
	public boolean isServiceExecutionCheckPointingRequired() {
		return false;
	}
	
    /**
     * Interface method implementation. Returns the fully qualified class name of this class
     * @see org.trpr.platform.runtime.spi.component.ComponentContainer#getName()
     */
    public String getName() {
    	return this.getClass().getName();
    }

	/**
	 * Interface method implementation. Publishes the specified event using the {@link #publishEvent(PlatformEvent)} method
	 * @see ComponentContainer#publishBootstrapEvent(PlatformEvent)
	 */
	public void publishBootstrapEvent(PlatformEvent bootstrapEvent) {
		this.publishEvent(bootstrapEvent);
	}

    /**
     * Interface method implementation. Stores local references to the specified BootstrapExtension instances.
     * @see org.trpr.platform.runtime.spi.component.ComponentContainer#setLoadedBootstrapExtensions(org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension[])
     */
    public void setLoadedBootstrapExtensions(BootstrapExtension...bootstrapExtensions) {
    	this.loadedBootstrapExtensions = bootstrapExtensions;
    }
    
	/**
	 * Interface method implementation. Loads/Reloads service(s) defined in the specified {@link FileSystemResource} 
	 * @see org.trpr.platform.runtime.spi.component.ComponentContainer#loadComponent(org.springframework.core.io.Resource)
	 */
	public void loadComponent(Resource resource) {
		if (!FileSystemResource.class.isAssignableFrom(resource.getClass()) || 
				!((FileSystemResource)resource).getFilename().equalsIgnoreCase(ServiceFrameworkConstants.SPRING_SERVICES_CONFIG)) {
			throw new UnsupportedOperationException("Services can be loaded only from files by name : " + 
					ServiceFrameworkConstants.SPRING_SERVICES_CONFIG + ". Specified resource is : " + resource.toString());
		}
		loadServiceContext(new ServiceConfigInfo(((FileSystemResource)resource).getFile()));
	}
	
	/**
	 * Locates and loads service contexts - one for each instance of {@link ServiceFrameworkConstants#SPRING_SERVICES_CONFIG} file found in the resources
	 * folders
	 * @throws PlatformException in case of exceptions initializing the service application contexts.
	 */
	protected void loadServiceContexts() throws PlatformException {
		// locate and load the individual service bean XML files using the common batch beans context as parent
		File[] serviceBeansFiles = FileLocator.findFiles(ServiceFrameworkConstants.SPRING_SERVICES_CONFIG);	
		for (File serviceBeansFile : serviceBeansFiles) {
			ServiceConfigInfo serviceConfigInfo = new ServiceConfigInfo(serviceBeansFile);
			// load the service's appcontext
			this.loadServiceContext(serviceConfigInfo);
		}				
	}
	
	/**
	 * Adds the specified ServiceConfigInfo to the list of configs loaded by this container
	 * @param serviceConfigInfo the ServiceConfigInfo to be registered
	 */
	protected void registerServiceContext(ServiceConfigInfo serviceConfigInfo) {
		this.servicesContextList.add(serviceConfigInfo);
		// register the loaded services with the registry
        String[] serviceBeanIds = serviceConfigInfo.getServiceContext().getBeanNamesForType(Service.class);
        for (String serviceBeanId : serviceBeanIds) {
            try {
                // find the project name from the Resource that was used to load the bean
            	BeanDefinition serviceBeanDefinition = serviceConfigInfo.getServiceContext().getBeanFactory().getBeanDefinition(serviceBeanId);
                String projectName = this.getProjectName((((AbstractBeanDefinition)serviceBeanDefinition).getResource()).getFile().getAbsolutePath());
                String[] serviceNameParts = serviceBeanId.split(SERVICE_VERSION_SEPARATOR);
                ServiceKey serviceKey = new ServiceKeyImpl(serviceNameParts[0], serviceNameParts[1]);
                // add the service configuration information
                this.configurationService.addService(serviceKey,new FileSystemResource(serviceConfigInfo.getServiceConfigXML()));
                // check if the service application context is actually initialized and the bean does exist 
                // before adding it to registry, service-infos and creating a usable service compartment
                if (serviceConfigInfo.getServiceContext().isActive() && serviceConfigInfo.getServiceContext().containsBean(serviceBeanId)) {
	                //Remove the service from registry, if found
	                if(this.serviceRegistry.contains(serviceKey)) {
	                    this.serviceRegistry.remove(serviceKey);
	                }
	                // TODO find a way to determine domain names for a service, using ServiceFrameworkConstants.DEFAULT_DOMAIN for now
	                this.serviceRegistry.addServiceInfoToRegistry(serviceNameParts[0], serviceNameParts[1], projectName, ServiceFrameworkConstants.DEFAULT_DOMAIN);
	                //Remove the service Key from serviceInfos and serviceComaprtments
	                if(serviceInfos.get(serviceKey)!=null) {
	                    serviceInfos.remove(serviceKey);
	                }
	                if(serviceCompartments.get(serviceKey)!=null) {
	                    serviceCompartments.remove(serviceKey).destroy();
	                }
	                //Service Key and compartment initing
	                ServiceInfo serviceInfo = this.serviceRegistry.getServiceInfo(serviceKey);
	                serviceInfos.put(serviceKey,serviceInfo);
	                ServiceCompartment<T,S> serviceCompartment= new ServiceCompartmentImpl<T,S>(serviceInfo, 
	                		(Service<T,S>)serviceConfigInfo.getServiceContext().getBean(serviceBeanId));
	                serviceCompartment.init();
	                serviceCompartments.put(serviceKey,serviceCompartment);
                }
                
            } catch (Exception ex) {
                // the service name is not as per standard naming convention of <serviceName>_<serviceVersion>. Throw an exception
                throw new ServiceException("Invalid service bean name? Convention is <serviceName>_<serviceVersion>. Offending bean name is : " + serviceBeanId, ex);
            }
        }				
	}
	
	/**
	 * Resets the constituents of this ComponentContainer
	 * @throws PlatformException
	 */
	protected void resetContainer() throws PlatformException {
		for (Iterator<ServiceKey> it = serviceCompartments.keySet().iterator(); it.hasNext();) { 
			ServiceKey key = (ServiceKey) it.next(); 
			ServiceCompartment<T,S> serviceCompartment = (ServiceCompartment<T,S>) serviceCompartments.get(key); 
			serviceCompartment.destroy();
		}
		serviceInfos.clear();
		serviceCompartments.clear();
		serviceInfos=null;
		serviceCompartments=null;
		// do not change the below order of closing app contexts. Always close children first before the parent context
		for (ServiceConfigInfo serviceConfigInfo : this.servicesContextList) {
			serviceConfigInfo.getServiceContext().close();
		}
		this.servicesContextList = null;				
		SpringServicesContainer.commonServiceBeansContext.close();
		SpringServicesContainer.commonServiceBeansContext = null;
	}
	
	/**
	 * Loads the service context from path specified in the ServiceConfigInfo. 
	 * @param serviceConfigInfo containing absolute path to the service's configuration location i.e. folder
	 */
	private void loadServiceContext(ServiceConfigInfo serviceConfigInfo) {
		// check if a context exists already for this config path 
		for (ServiceConfigInfo loadedServiceInfo : this.servicesContextList) {
			if (loadedServiceInfo.equals(serviceConfigInfo)) {
				serviceConfigInfo = loadedServiceInfo;
				break;
			}
		}
		if (serviceConfigInfo.getServiceContext() != null) {
			// close the context and remove from list
			serviceConfigInfo.getServiceContext().close();
			this.servicesContextList.remove(serviceConfigInfo);
		}
		ClassLoader serviceCL = this.tccl;
		// check to see if the service has service and dependent binaries deployed outside of the runtime class path. If yes, include them using a custom URL classloader.
		File customLibPath = new File (serviceConfigInfo.getServiceConfigXML().getParentFile(), ServiceConfigInfo.BINARIES_PATH);
		if (customLibPath.exists() && customLibPath.isDirectory()) {
			try {
				File[] libFiles = customLibPath.listFiles();
				URL[] libURLs = new URL[libFiles.length];
				for (int i=0; i < libFiles.length; i++) {
					libURLs[i] = new URL(ServiceConfigInfo.FILE_PREFIX + libFiles[i].getAbsolutePath());
				}
				serviceCL = new URLClassLoader(libURLs, this.tccl);
			} catch (MalformedURLException e) {
				throw new PlatformException(e);
			}
		} 
		// now load the service context and add it into the servicecontexts list
		serviceConfigInfo.loadServiceContext(serviceCL);
		this.registerServiceContext(serviceConfigInfo);		
	}	
	
	/**
	 * Helper method to return the project name derived from the config file path
	 */
	private String getProjectName(String configFilePath) {
		// get the project name i.e the first directory name relative to the projects root
		File projectsRoot = new File(RuntimeVariables.getProjectsRoot());
		String relativePath = configFilePath.substring(projectsRoot.getAbsolutePath().length());
		return new StringTokenizer(relativePath, File.separator).nextToken();		
	}

}
