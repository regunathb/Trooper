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
package org.trpr.platform.batch.spi.spring.admin;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.springframework.core.io.Resource;
import org.trpr.platform.batch.common.JobHost;
import org.trpr.platform.core.PlatformException;

/**
 * <code>JobConfigurationService</code> provides methods for job configuration such as adding, 
 * removing configuration files and dependencies. It also holds the list of running Trooper instances
 * and the list of deployed jobs in each of them (for HA mode)
 * 
 * @author devashishshankar
 * @version 1.1, 31 Jan 2013
 */
public interface JobConfigurationService {	
	
	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getSyncService()
	 */
	public SyncService getSyncService();

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#setSyncService(SyncService)
	 */
	public void setSyncService(SyncService syncService);

	/**
	 * Interface method implementation.
	 * Also sets the hostName
	 * @see JobConfigurationService#setPort(int)
	 */
	public void setPort(int port);

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getCurrentHostName()
	 */
	public JobHost getCurrentHostName();

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getCurrentHostJobs()
	 */
	public Collection<String> getCurrentHostJobs();

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#addJobInstance(String, JobHost)
	 */
	public void addJobInstance(String jobName, JobHost hostName);

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#clearJobInstances()
	 */
	public void clearJobInstances();

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getHostNames(String)
	 */
	public List<JobHost> getHostNames(String jobName);

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getAllHostNames()
	 */
	public List<JobHost> getAllHostNames();

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#getJobStoreURI(String)
	 */
	public URI getJobStoreURI(String jobName);

	/**
	 * Interface method implementation. 
	 * @see JobConfigurationService#addJobDependency(String, String, byte[])
	 */
	public void addJobDependency(String jobName, String destFileName, byte[] fileContents);

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getJobDependencyList
	 */
	public List<String> getJobDependencyList(String jobName);

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#getJobConfig(String)
	 */
	public Resource getJobConfig(String jobName);

	/**
	 * Interface method implementation. After setting an XML File, also saves the previous file.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#setJobconfig(String, byte[])
	 */
	public void setJobConfig(String jobName, Resource jobConfigFile) throws PlatformException;

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#deployJob(String)
	 */
	public void deployJob(String jobName);

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.JobConfigurationService#deployJobToAllHosts(String)
	 */
	public void deployJobToAllHosts(String jobName);

}
