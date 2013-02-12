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
 * and the list of deployed jobs in each of them (for HA mode).
 * 
 * @author devashishshankar
 * @version 1.1, 31 Jan 2013
 * 
 * Change log:
 * Allows multiple jobs to be linked to a single job store and configuration file.
 */
public interface JobConfigurationService {	
	
	/**
	 * Gets the syncService
	 * @return <code>SyncService</code>
	 */
	public SyncService getSyncService();

	/**
	 * Sets the syncService
	 * @param syncService <code>SyncService</code>
	 */
	public void setSyncService(SyncService syncService);

	/**
	 * Sets the port
	 * @param port port no. of the current Trooper host
	 */
	public void setPort(int port);

	/**
	 * Gets the current trooper host information
	 * @return <code>JobHost</code> current server JobHost, null if not running in HA mode
	 */
	public JobHost getCurrentHostName();

	/**
	 * Gets the list of jobs allocated on the current server
	 * @return A list of <code>String</code>, where each String is a job name, null null if not running in HA mode
	 */
	public Collection<String> getCurrentHostJobs();

	/**
	 * Adds a jobName and a host instance
	 * @param jobName name of the job
	 * @param hostName <code>JobHost</code> host name
	 */
	public void addJobInstance(String jobName, JobHost hostName);

	/**
	 * Clears all the jobnames and hostnames from {@link JobConfigurationService} 
	 */
	public void clearJobInstances();

	/**
	 * Gets the hosts on which a job is running
	 * @param jobName name of the job
	 * @return List of <code>JobHost</code>
	 */
	public List<JobHost> getHostNames(String jobName);

	/**
	 * Gets all the known job hosts
	 * @return List of <code>JobHost</code>
	 */
	public List<JobHost> getAllHostNames();

	/**
	 * Gets the URI of the directory where job, including it's configuration and dependencies
	 * is stored
	 * @param jobName name of the job
	 * @return directory path as a URI
	 */
	public URI getJobStoreURI(String jobName);

	/**
	 * Adds a dependency to a job
	 * @param jobNames List of jobNames to which the dependency has to be linked
	 * @param destFileName name of the dependency file
	 * @param fileContents contents of dependency file as <code>byte[]</code>
	 */
	public void addJobDependency(List<String> jobName, String destFileName, byte[] fileContents);

	/**
	 * Gets the dependencies associated with a job
	 * @param jobName name of the job
	 * @return list of job names
	 */
	public List<String> getJobDependencyList(String jobName);

	/**
	 * Gets the job Configuration as a <code>Resource</code>
	 * @param jobName name of the job
	 * @return job configuration contents as a <code>Resource</code>
	 */
	public Resource getJobConfig(String jobName);

	/**
	 * Sets the job configuration for a job.
	 * @param jobName List of job names for which config file is being set
	 * @param jobConfigFile
	 * @throws PlatformException in case of errors
	 */
	public void setJobConfig(List<String> jobName, Resource jobConfigFile) throws PlatformException;

	/**
	 * Deploys jobs to current host. 
	 * @param List of jobnames for which deployement has to take place
	 */
	public void deployJob(List<String> jobName);
}
