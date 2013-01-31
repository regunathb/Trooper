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
package org.trpr.platform.batch.impl.spring.job.ha;

import java.util.Collection;

import org.trpr.platform.batch.impl.spring.web.Host;
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
 * <code> JobRegistryBeanPostProcessor</code> is an extension of 
 * @link {org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor}.
 * Registers jobs to the Zookeeper service. Updates hostnames in the jobConfigurationService
 * @author devashishshankar
 *
 */
public class JobRegistryBeanPostProcessor extends 
org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor {

	private JobConfigurationService jobConfigurationService;
	private CuratorFramework curatorFramework;
	private String jobName;
	private SyncService syncService;

	ServiceDiscovery<InstanceDetails> serviceDiscovery = null;
	private static final String ZK_DEP_PATH_PREFIX = "/Batch/Deployment";
	private ServiceCache<InstanceDetails> sc;
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(JobRegistryBeanPostProcessor.class);

	/**Setter methods **/
	public void setJobConfigService(JobConfigurationService jobConfigurationService) {
		this.jobConfigurationService = jobConfigurationService;
	}

	public void setCuratorClient(CuratorFramework curatorFramework) {
		this.curatorFramework= curatorFramework;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public void setSyncService(SyncService syncService) {
		System.out.println("setting syncservice");
		this.syncService = syncService;
	}

	/**This method updates the list of servers in the jobConfigurationService **/
	public void updateHosts() {
		LOGGER.info("updating list of servers");
		try {
			Collection<String> serviceNames = serviceDiscovery.queryForNames();
			for(String serviceName: serviceNames) {
				System.out.println("servicename: "+serviceName);
				//Add all the hosts in the service to jobConfigService
				Collection<ServiceInstance<InstanceDetails>> instances = this.serviceDiscovery.queryForInstances(serviceName);     
				for (ServiceInstance<InstanceDetails> instance: instances) {
					Host instHost = new Host(instance.getPayload().getDescription(),instance.getAddress(),instance.getPort());
					this.jobConfigurationService.addJobInstance(serviceName, instHost);   
				}
			}
		}catch (Exception e) {
			LOGGER.error("Error while updating server list",e);
		}
		//Sync all servers
		this.syncService.syncAllServers();
	}
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		try
		{
			if(!this.curatorFramework.isStarted())
				this.curatorFramework.start();
			//For storing metadata, the class is InstanceDetails
			JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<InstanceDetails>(InstanceDetails.class);
			//Get serviceDiscovery
			this.serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class)
					.client(this.curatorFramework)
					.basePath(ZK_DEP_PATH_PREFIX).serializer(serializer)
					.build();
			this.serviceDiscovery.start();
			//Get current host attributes
			Host currentHost = this.jobConfigurationService.getCurrentServerName();
			String hostName = currentHost.getHostName();
			String hostAddress = currentHost.getIP();
			int hostPort = currentHost.getPort();
			//Register current job to current server
			this.serviceDiscovery.registerService(ServiceInstance.<InstanceDetails> builder()
					.name(this.jobName)
					.address(hostAddress)
					.port(hostPort)
					.payload(new InstanceDetails(hostName))
					.build());
			//Add instance to jobConfigService
			this.jobConfigurationService.addJobInstance(jobName, currentHost);   
			Collection<String> serviceNames = serviceDiscovery.queryForNames();
			//Add a listener for all services
			for(String serviceName: serviceNames) {
				this.sc = this.serviceDiscovery.serviceCacheBuilder().name(serviceName).build();
				sc.start();
				sc.addListener(new ServiceListner());
				//Put this in jobConfigService to all existing   	
			}
			//Update
			this.updateHosts();
			//Some useful info for Logs
			LOGGER.info("jobConfigService: current server: "+this.jobConfigurationService.getCurrentServerName());
			LOGGER.info("ALl servers: "+this.jobConfigurationService.getAllServerNames());
			LOGGER.info("jobs in current server: "+this.jobConfigurationService.getCurrentServerJobs());
		}
		catch(Exception e) {
			LOGGER.error("Exception while registring jobService", e);
		}
	}
	/**
	 * Implementation of listener class that listens to change in Service cache	 *
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
			JobRegistryBeanPostProcessor.this.updateHosts();
		}
	}
}
