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

import java.util.Collection;
import java.util.List;

import org.trpr.platform.batch.impl.spring.web.Host;
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
	 * Gets the absolute directory path of a job, where all the config-files and dependencies are stored.
	 * Returns a new directory based on jobName if job doesn't exist
	 * @param jobName job name identifier
	 * @return Path of job directory
	 */
	String getJobDirectory(String jobName);

	/**
	 * Gets the XML File path for given job. If not found, returns null.
	 * @param jobName job name identifier
	 * @return Path of XMLFile. null if not found
	 */
	String getXMLFilePath(String jobName);

	/**
	 * Sets the XML File. If the job doesn't have an XML file (new job), a new directory 
	 * is created and a new XML File is created there. Otherwise, the old file is overwritten.
	 * @param jobName the job name identifier
	 * @throws PlatformException in case of errors
	 */
	void setXMLFile(String jobName, String XMLFileContents) throws PlatformException;

	/**
	 * Removes the job configuration XML file for the specified job name. Restores the previos XML File,
	 * if found
	 * @param jobName the job name identifier
	 */
	void removeXMLFile(String jobName);

	/**
	 * Add a job dependency for a given job. Also uploads the dependency file to its directory
	 * @param jobName Name of the job
	 * @throws PlatformException in case of errors
	 */
	void addJobDependency(String jobName, String destFileName, byte[] fileContents) throws PlatformException;

	/** 
	 * Returns the list of dependencies of given job. 
	 * @param jobName Name of the job
	 * @return List of dependencies(filename, not the path). If not found, returns null
	 */
	List<String> getJobDependencyList(String jobName);

	/**
	 * Get the job name from a spring batch config file.
	 * @param XMLFileContents A byte array of the configuration file
	 * @return Job name if found, null otherwise
	 */
	String getJobNameFromXML(byte[] XMLFileContents);

	/**
	 * Gets the contents of a file in a single String
	 * @param filePath Path of the file
	 * @return String containing the file contents
	 */
	String getFileContents(String filePath);

	/**
	 * Inform JobConfiguratinService of successful deployment.
	 * Cleans up resources such as previous XML File
	 */
	void deploymentSuccess(String jobName);
	/**
	 * Sets the port of the current Trooper host. 
	 * And sets the hostName
	 * @param port port number
	 */
	void setPort(int port);

	/**
	 * Gets the list of hostnames on which a job is running
	 * @return List of {@link Host}, null if not running in HA mode.
	 */
	List<Host> getServerNames(String jobName);

	/**
	 * Gets a list of all the Trooper hosts
	 * @return List of {@link Host}, null if not running in HA mode.
	 */
	List<Host> getAllServerNames();

	/**
	 * Returns the current Trooper host
	 * @return @link{Host} the host name of current Trooper instance, null if not running in HA mode
	 */
	Host getCurrentServerName();

	/**
	 * Gets the list of HA jobs allocated on the current server
	 */
	Collection<String> getCurrentServerJobs();

	/**
	 * Adds a jobname and a server name
	 * @param jobName name of the job
	 * @param serverName @link{Host} servername
	 */
	void addJobInstance(String jobName, Host serverName);
}
