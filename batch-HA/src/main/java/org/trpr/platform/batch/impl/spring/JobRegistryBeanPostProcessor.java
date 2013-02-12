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
package org.trpr.platform.batch.impl.spring;

import org.trpr.platform.batch.impl.job.ha.service.CuratorJobSyncHandler;

/**
 * <code> JobRegistryBeanPostProcessor</code> is an extension of 
 * @link {org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor}.
 * Registers jobs to the Zookeeper service using methods from {@link CuratorJobSyncHandler}
 * 
 * @author devashishshankar
 * @version 1.0, 31 Jan, 2013
 */
public class JobRegistryBeanPostProcessor extends 
org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor {

	/** The jobName for which the current instance is called */
	private String jobName;

	/** The zookeeper sync handler instance */
	private CuratorJobSyncHandler curatorJobSyncHandler;

	/** Setter methods */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public void setCuratorJobSyncHandler(CuratorJobSyncHandler curatorJobSyncHandler) {
		this.curatorJobSyncHandler = curatorJobSyncHandler;
	}
	/** End Setter methods **/
	
	/**
	 * Overriden method from {@link org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor#afterPropertiesSet()}
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		this.curatorJobSyncHandler.addJobInstance(jobName);		
	}
}
