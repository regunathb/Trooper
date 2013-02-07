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

import org.springframework.beans.factory.annotation.Autowired;
import org.trpr.platform.batch.common.JobHost;
import org.trpr.platform.batch.impl.job.ha.JobInstanceDetails;
import org.trpr.platform.batch.impl.spring.JobRegistryBeanPostProcessor;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.SyncService;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.state.ConnectionState;
import com.netflix.curator.x.discovery.ServiceCache;
import com.netflix.curator.x.discovery.ServiceDiscovery;
import com.netflix.curator.x.discovery.ServiceDiscoveryBuilder;
import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.details.JsonInstanceSerializer;
import com.netflix.curator.x.discovery.details.ServiceCacheListener;

/**
 * <code>ZKSyncHandler </code> handles the sync with Zookeeper. It performs the folowing tasks: 
 * Registering a new job to Zookeeper
 * Adding a listener to Zookeeper services
 * Informing jobConfigurationService about change in services
 * 
 * @author devashishshankar
 * @version 1.0, 7 Feb, 2013
 */
public class ZKSyncHandler {

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
	private static final Logger LOGGER = LogFactory.getLogger(JobRegistryBeanPostProcessor.class);

	/** Autowired Constructor */
	@Autowired
	public ZKSyncHandler(JobConfigurationService jobConfigurationService, CuratorFramework curatorFramework) {
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
	}

	/**
	 * This method updates the list of servers in the jobConfigurationService. 
	 * Also calls syncservice to sync all the servers
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
		//Sync all servers according to the info fed in jobConfigService
		this.syncService.syncAllHosts();
	}

	/**
	 * Registers a job(service) to zookeeper and adds a listener to its cache change
	 * @param jobName Name of the job
	 */
	public void addJobInstance(String jobName) {
		//Register the job to ZK
		//Get current host attributes
		JobHost currentHost = this.jobConfigurationService.getCurrentHostName();
		try {
			//Register current job to current server
			this.serviceDiscovery.registerService(ServiceInstance.<JobInstanceDetails> builder()
					.name(jobName)
					.address(currentHost.getIP())
					.port(currentHost.getPort())
					.payload(new JobInstanceDetails(currentHost.getHostName()))
					.build());
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
			LOGGER.info("Cache changed");
			ZKSyncHandler.this.updateHosts();
		}
	}
}
