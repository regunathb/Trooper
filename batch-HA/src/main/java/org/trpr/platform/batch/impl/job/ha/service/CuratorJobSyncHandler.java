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
package org.trpr.platform.batch.impl.job.ha.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.trpr.platform.batch.common.JobHost;
import org.trpr.platform.batch.impl.job.ha.JobInstanceDetails;
import org.trpr.platform.batch.impl.spring.JobRegistryBeanPostProcessor;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.SyncService;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.event.PlatformEventConsumer;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.common.RuntimeConstants;
import org.trpr.platform.runtime.impl.event.BootstrapProgressMonitor;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.state.ConnectionState;
import com.netflix.curator.x.discovery.ServiceCache;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceDiscoveryBuilder;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.details.JsonInstanceSerializer;
import com.netflix.curator.x.discovery.details.ServiceCacheListener;

/**
 * <code>CuratorJobSyncHandler </code> handles the sync with Curator. It performs the following tasks: 
 * Registering a new job to Zookeeper
 * Adding a listener to Zookeeper services
 * Informing jobConfigurationService about change in services
 * 
 * @author devashishshankar
 * @version 1.0, 7 Feb, 2013
 */
public class CuratorJobSyncHandler implements InitializingBean, PlatformEventConsumer {

	/** Instance of trooper @link{JobConfigurationService} */
	private JobConfigurationService jobConfigurationService;

	/** The curator client being used */
	private CuratorFramework curatorFramework;

	/** Instance of trooper {@link SyncService} */
	private SyncService syncService;

	/** ServiceDiscovery instance for registering and querying Zookeeper services */
	private ServiceDiscovery<JobInstanceDetails> serviceDiscovery = null;

	/** The map holding all the Service Caches */
	private Map <String,ServiceCache<JobInstanceDetails>> serviceCacheMap;

	/** The zookeeper path prefix for service creation */
	private static final String ZK_DEP_PATH_PREFIX = "/Batch/Deployment";

	/** The Log instance for this class */
	private static final Logger LOGGER = LogFactory.getLogger(CuratorJobSyncHandler.class);

	/** {@link BootstrapProgressMonitor} instance which is used to add a listener to Bootstrap start event */
	private BootstrapProgressMonitor bootstrapProgressMonitor;

	/** Autowired Constructor */
	@Autowired
	public CuratorJobSyncHandler(JobConfigurationService jobConfigurationService, CuratorFramework curatorFramework, 
			BootstrapProgressMonitor bootstrapProgressMonitor) {
		this.curatorFramework= curatorFramework;
		this.jobConfigurationService = jobConfigurationService;
		this.syncService = new SyncServiceImpl(this.jobConfigurationService);
		if((this.jobConfigurationService.getSyncService()==null)) {
			this.jobConfigurationService.setSyncService(this.syncService);
		}
		JsonInstanceSerializer<JobInstanceDetails> serializer = new JsonInstanceSerializer<JobInstanceDetails>(JobInstanceDetails.class);
		//Get serviceDiscovery
		this.serviceDiscovery = ServiceDiscoveryBuilder.builder(JobInstanceDetails.class)
				.client(this.curatorFramework)
				.basePath(ZK_DEP_PATH_PREFIX).serializer(serializer)
				.build();
		this.serviceCacheMap = new HashMap<String, ServiceCache<JobInstanceDetails>>();
		this.bootstrapProgressMonitor = bootstrapProgressMonitor;
	}

	/**
	 * This method updates the list of servers in the jobConfigurationService.
	 */
	public void updateHosts() {
		LOGGER.info("Updating list of servers");
		this.jobConfigurationService.clearJobInstances();
		try {
			//Get all the registered services in Zookeeper
			Collection<String> serviceNames = serviceDiscovery.queryForNames();
			//Remove any service not found in ZK
			for(String serviceName: this.serviceCacheMap.keySet()) {
				if(!serviceNames.contains(serviceName)) {
					this.serviceCacheMap.get(serviceName).close();
					this.serviceCacheMap.remove(serviceName);
				}
			}
			//Loop over all serviceNames and build the info in jobConfigurationService
			for(String serviceName: serviceNames) {	
				//Add listeners and cache to all the services, if not added
				if(!this.serviceCacheMap.containsKey(serviceName)) { //Service cache doesn't have this job
					ServiceCache<JobInstanceDetails> sc = this.serviceDiscovery.serviceCacheBuilder().name(serviceName).build();
					sc.start();
					sc.addListener(new ServiceListner());
					this.serviceCacheMap.put(serviceName, sc);
					LOGGER.info("Job: "+serviceName+"'s Cache listener removed.");
				}
				//Add all the hosts in the service to jobConfigService
				Collection<ServiceInstance<JobInstanceDetails>> instances = this.serviceCacheMap.get(serviceName).getInstances();     
				for (ServiceInstance<JobInstanceDetails> instance: instances) {
					JobHost instHost = new JobHost(instance.getPayload().getHostName(),instance.getAddress(),instance.getPort());
					this.jobConfigurationService.addJobInstance(serviceName, instHost);   
				}
			}
		}catch (Exception e) {
			LOGGER.error("Error while updating server list",e);
		}
	}

	/**
	 * Registers a job(service) to zookeeper (Curator) and adds a listener to its cache change
	 * Method is synchronized because jobConfigService is being updated (Parallel updates may lead to problems)
	 * @param jobName Name of the job
	 */
	public synchronized void addJobInstance(String jobName) {
		//Register the job to ZK
		//Get current host attributes
		JobHost currentHost = this.jobConfigurationService.getCurrentHostName();
		try {
			//First Check if job is already registered in ZooKeeper (Curator)
			boolean isRegistered = false;
			
			LOGGER.info("Querying zookepper to see if already there is an entry ");
			//NOTE: This might not send cache changed in case job is redeployed on a jobHost
			Collection<String> serviceNames = this.serviceDiscovery.queryForNames();
			if(serviceNames.contains(jobName)) {
				for(ServiceInstance<JobInstanceDetails> serviceInstance: this.serviceDiscovery.queryForInstances(jobName)) {
					if(serviceInstance.getAddress().equals(currentHost.getIP())) {
						if(serviceInstance.getPort()==currentHost.getPort()) {
							isRegistered = true;
							break;
						}
					}
				}
			}
			if(isRegistered==false) {
				//Register current job to current server
				this.serviceDiscovery.registerService(ServiceInstance.<JobInstanceDetails> builder()
						.name(jobName)
						.address(currentHost.getIP())
						.port(currentHost.getPort())
						.payload(new JobInstanceDetails(currentHost.getHostName()))
						.build());
				LOGGER.info("Registering "+jobName+" to "+currentHost.getAddress());
			}
			//Adding the listeners
			if(!this.serviceCacheMap.containsKey(jobName)) { //Service cache doesn't have this job
				ServiceCache<JobInstanceDetails> sc = this.serviceDiscovery.serviceCacheBuilder().name(jobName).build();
				sc.addListener(new ServiceListner());
				sc.start();
				this.serviceCacheMap.put(jobName, sc);
			}
			LOGGER.info("Added listener");
		} catch (Exception e) {
			LOGGER.error("Exception while adding job instance",e);
		}
		//Just in case. Because listener is added after registering, it may not activate
		this.updateHosts();
	}

	/**
	 * Closes the service cache and serviceDiscovery objects
	 */
	@Override
	protected void finalize() throws Throwable {
		for(String serviceName:this.serviceCacheMap.keySet()){
			this.serviceCacheMap.get(serviceName).close();
		}
		this.serviceDiscovery.close();
		super.finalize();
	}

	/**
	 * Method which finds the oldest running Job hosts and sends a pull request to that server
	 */
	private void sendPullRequests() {
		//Pull request code
		//Get the oldest jobHost
		try {
			long minTime=Long.MAX_VALUE;
			ServiceInstance<JobInstanceDetails> minInst = null;
			//For all services try to find the oldest host
			for(String serviceName : this.serviceDiscovery.queryForNames()) {
				for(ServiceInstance<JobInstanceDetails> inst :this.serviceDiscovery.queryForInstances(serviceName)) {
					if(inst.getRegistrationTimeUTC()<minTime){
						minInst = inst;
						minTime = inst.getRegistrationTimeUTC();					
					}
				}
			}
			LOGGER.info("The oldest host is: "+minInst);
			//minInst is the oldest host. send a pull request
			if(minInst!=null) {
				JobHost instHost = new JobHost(minInst.getPayload().getHostName(),minInst.getAddress(),minInst.getPort());
				if(!this.jobConfigurationService.getCurrentHostName().equals(instHost)) {
					LOGGER.info("Sending pull request to: "+minInst.getAddress()+":"+minInst.getPort());
					this.syncService.pullRequest(minInst.getAddress()+":"+minInst.getPort());
				}
			}			
		} catch (Exception e) {
			LOGGER.error("Exception while finding the oldest host and sending the pull request",e);
		}
		this.syncService.syncAllHosts();
	}

	/**
	 * Interface method implementation. Listens to ApplicationEvent
	 * @see PlatformEventConsumer#onApplicationEvent(ApplicationEvent)
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event.getSource() instanceof PlatformEvent) {
			PlatformEvent platformEvent = (PlatformEvent) event.getSource(); //Event should be platformEvent
			if(platformEvent.getEventType()!=null&&platformEvent.getEventType().equalsIgnoreCase(RuntimeConstants.BOOTSTRAPMONITOREDEVENT)){
				synchronized (BootstrapProgressMonitor.class) {
					if(platformEvent.getEventStatus() != null && platformEvent.getEventStatus().equalsIgnoreCase(RuntimeConstants.BOOTSTRAP_START_STATE)){
						LOGGER.info("Finding oldest host and Sending pull requests");
						this.sendPullRequests();  //This should only be called once, when server starts (during/after bootstrap)
					}
				}	  	
			}
		}		
	}

	/**
	 * Interface method implementation. Used to add a Bootstrap Event listener. This method is called after
	 * all the properties have been set
	 * @see InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		this.bootstrapProgressMonitor.addBootstrapEventListener(this);
	}

	/**
	 * Implementation of listener class that listens to change in Service cache	 
	 */
	private class ServiceListner implements ServiceCacheListener {
		@Override
		public void stateChanged(CuratorFramework arg0, ConnectionState arg1) {
			LOGGER.info("State changed");
		}

		/**
		 * Calls the {@link JobRegistryBeanPostProcessor#updateHosts()} to
		 * update the hosts as soon as cache is changed
		 */
		@Override
		public void cacheChanged() {
			LOGGER.info("Curator Cache changed");
			CuratorJobSyncHandler.this.updateHosts();
		}
	}
}
