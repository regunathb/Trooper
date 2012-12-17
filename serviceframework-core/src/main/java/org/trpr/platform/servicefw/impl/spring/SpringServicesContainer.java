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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.UrlResource;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.spi.event.PlatformEventProducer;
import org.trpr.platform.model.event.PlatformEvent;
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
import org.trpr.platform.servicefw.impl.ServiceStatisticsGatherer;
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
 * This container locates and loads all service definitions contained in files named by the value of ServiceFrameworkConstants.SPRING_SERVICES_CONFIG. 
 * This container also ensures that all beans declared in ServiceFrameworkConstants.COMMON_SPRING_SERVICES_CONFIG and 
 * ServerConstants.COMMON_SPRING_BEANS_CONFIG are available to the service beans by
 * specifying the common beans contexts as the parent for the services application context created by this container.
 * 
 * @see ServiceContainer
 * @author Regunath B
 * @version 1.0, 16/08/2012
 */
public class SpringServicesContainer<T extends PlatformServiceRequest, S extends PlatformServiceResponse> implements ServiceContainer<T,S> {

	/** The prefix to be added to file absolute paths when loading Spring XMLs using the FileSystemXmlApplicationContext*/
	private static final String FILE_PREFIX = "file:";
	
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
	
	/** The ServiceContext initialized by this ServiceContainer*/
	@SuppressWarnings("rawtypes")
	private ServiceContext serviceContext;
	
	/** The ServiceRegistry initialized by this ServiceContainer*/
	private ServiceRegistry serviceRegistry;
		
    /** The common batch beans context*/
    private AbstractApplicationContext commonServiceBeansContext;    	
    
	/**
	 * The Spring application context that would hold all service declarations from all services
	 */
    private AbstractApplicationContext servicesContext;	
		
	/** Collection of ServiceCompartments */
	private HashMap<ServiceKey, ServiceCompartment<T,S>> serviceCompartments = null;
	
	/** Collection of ServiceInfos */
	private HashMap<ServiceKey, ServiceInfo> serviceInfos = null;
	
    /** Local reference for all BootstrapExtensionS loaded by the Container and set on this ComponentContainer*/
    private BootstrapExtension[] loadedBootstrapExtensions;	
	
	/**
	 * Interface method implementation
	 * @see ServiceContainer#init()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void init() throws PlatformException {
		// The common service beans context is loaded first using the Platform common beans context as parent
		// load this from classpath as it is packaged with the binaries
		ApplicationContextFactory defaultCtxFactory = null;
		for (BootstrapExtension be : this.loadedBootstrapExtensions) {
			if (ApplicationContextFactory.class.isAssignableFrom(be.getClass())) {
				defaultCtxFactory = (ApplicationContextFactory)be;
				break;
			}
		}
		
		this.commonServiceBeansContext =  new ClassPathXmlApplicationContext(new String[]{ServiceFrameworkConstants.COMMON_SPRING_SERVICES_CONFIG},
				defaultCtxFactory.getCommonBeansContext());	
		
		// load the service beans and set the commons bean context as the parent
		File[] serviceBeansFiles = FileLocator.findFiles(ServiceFrameworkConstants.SPRING_SERVICES_CONFIG);	
		List<String> fileNamesList = new LinkedList<String>();
		for (File serviceBeansFile : serviceBeansFiles) {
			// add the "file:" prefix to file names to get around strange behavior of FileSystemXmlApplicationContext that converts absolute path 
			// to relative path
			fileNamesList.add(FILE_PREFIX + serviceBeansFile.getAbsolutePath());
		}
		this.servicesContext = new FileSystemXmlApplicationContext((String[])fileNamesList.toArray(new String[0]),
				this.commonServiceBeansContext);		
		
		// now initialize context, statistics gatherer and registry
		this.serviceContext = (ServiceContext)this.commonServiceBeansContext.getBean(SpringServicesContainer.SERVICE_CONTEXT_BEAN);
		this.serviceContext.setServiceContainer(this);
		((ServiceStatisticsGatherer)this.commonServiceBeansContext.getBean(SpringServicesContainer.SERVICE_STATISTICS_BEAN)).setServiceContainer(this);
		
		this.serviceRegistry = (ServiceRegistry)this.commonServiceBeansContext.getBean(SpringServicesContainer.SERVICE_REGISTRY_BEAN);
		
		// Set this ServiceContainer and ServiceRegistry on the BrokerFactory TODO : Need a better way of doing this
		BrokerFactory.setServiceContainer(this);
		BrokerFactory.setServiceRegistry(this.serviceRegistry);
		
		// register all service infos with the registry
		String[] serviceBeanIds = this.servicesContext.getBeanNamesForType(Service.class);
		for (String serviceBeanId : serviceBeanIds) {
			try {
				// find the project name from the Resource that was used to load the bean
				String projectName = this.getProjectName(((UrlResource)((AbstractBeanDefinition)
						this.servicesContext.getBeanFactory().getBeanDefinition(serviceBeanId)).getResource()).getFile().getAbsolutePath());
				String[] serviceNameParts = serviceBeanId.split(SERVICE_VERSION_SEPARATOR);
				// TODO find a way to determine domain names for a service, using ServiceFrameworkConstants.DEFAULT_DOMAIN for now
				this.serviceRegistry.addServiceInfoToRegistry(serviceNameParts[0], serviceNameParts[1], projectName, ServiceFrameworkConstants.DEFAULT_DOMAIN);		
			} catch (Exception ex) {
				// the service name is not as per standard naming convention of <serviceName>_<serviceVersion>. Throw an exception
				throw new ServiceException("Invalid service bean name? Convention is <serviceName>_<serviceVersion>. Offending bean name is : " + serviceBeanId, ex);
			}
		}
		
		// now initialize ServiceCompartmentS for all ServiceS
		// create ServiceCompartment instances for the located services
		this.serviceCompartments = new HashMap<ServiceKey, ServiceCompartment<T,S>>();
		this.serviceInfos = new HashMap<ServiceKey, ServiceInfo>();
		for(ServiceInfo serviceInfo : this.serviceRegistry.getAllServiceInfos()) {
			ServiceKey 	serviceKey=serviceInfo.getServiceKey();
			serviceInfos.put(serviceKey,serviceInfo);
			ServiceCompartment<T,S> serviceCompartment= new ServiceCompartmentImpl<T,S>(serviceInfo);
			serviceCompartment.init();
			serviceCompartments.put(serviceKey,serviceCompartment);
		}
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
	public ServiceResponse invokeService(ServiceInfo serviceInfo,
			ServiceRequest request) throws ServiceException {
		ServiceCompartment serviceCompartment = getCompartment(serviceInfo.getServiceKey());
		Service service = (Service)this.servicesContext.getBean(serviceInfo.getServiceKey().toString());
		return serviceCompartment.processRequest(service,request);
	}

	/**
	 * Interface method implementation. Publishes the specified event to the specified endpoint URI
	 * using a named bean DEFAULT_EVENT_PRODUCER looked up from the servicesContext (i.e. spring-services-config.xml).
	 * Note that the endpoint URI is ignored and typically no consumers are registered when running this container 
	 * @see ServiceEventProducer#publishEvent(PlatformEvent, String)
	 */
	public void publishEvent(PlatformEvent event, String endpointURI) {
		((PlatformEventProducer)this.servicesContext.getBean(DEFAULT_EVENT_PRODUCER)).publishEvent(event);
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
	 * Returns the services context.
	 * @return the services application context
	 */
	protected  AbstractApplicationContext getServicesContext() {
		return this.servicesContext;
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
		this.servicesContext.close();
		this.servicesContext = null;				
		this.commonServiceBeansContext.close();
		this.commonServiceBeansContext = null;
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
