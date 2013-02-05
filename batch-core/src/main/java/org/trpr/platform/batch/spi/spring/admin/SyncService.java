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
	 * and the final loading request to the given serverName.
	 * @return true on success, false on failure
	 */
	boolean pushJobToHost(String jobName, String serverName);
	
	/**
	 * Checks whether the jobs running in the current server exist in all the other servers,
	 * if not, pushes the missing jobs
	 */
	void syncAllHosts();
}
