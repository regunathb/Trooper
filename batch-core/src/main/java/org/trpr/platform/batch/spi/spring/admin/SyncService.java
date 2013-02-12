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

/**
 * <code> SyncService </code> is an interface providing methods to perform synchronization
 * of trooper instances, i.e. pushing jobs, their configuration files, dependencies to other running
 * Trooper instances
 * 
 * @author devashishshankar
 * @version 1.0 31 Jan, 2013
 */
public interface SyncService {
	
	/**
	 * Pushes a job, including sending it's configuration files, dependency files
	 * and the final loading request to the given serverName. Also includes a retry 
	 * count, which retries the request a specified number of times on failure
	 * @param jobName name of the job
	 * @param serverName server name in format IP:port
	 * @param retryCount number of times the request should be retried on failure
	 * @return true on success, false on failure
	 */	
	boolean pushJobToHostWithRetry(String jobName, String serverName, int retryCount);
	
	/**
	 * Pushes a job, including sending it's configuration files, dependency files
	 * and the final loading request to the given serverName.
	 * @param jobName name of the job
	 * @param serverName server name in format IP:port
	 * @return true on success, false on failure
	 */
	boolean pushJobToHost(String jobName, String serverName);
	
	/**
	 * Checks whether the jobs running in the current server exist in all the other servers,
	 * if not, pushes the missing jobs
	 */
	void syncAllHosts();
	
	/** 
	 * Sends a request to a server to push all it's jobs to the current server
	 * @param serverName The server name in format IP:port t which the pull request has to be sent
	 */
	void pullRequest(String serverName);

	/**
	 * Deploys a job to all the known job hosts
	 * @param jobName name of the job
	 */
	public void deployJobToAllHosts(String jobName);
	
	/**
	 * Deploys all jobs in the current server a given host
	 * @param hostAddress in the format "IP:port"
	 */
	public void deployAllJobsToHost(String hostAddress);
}
