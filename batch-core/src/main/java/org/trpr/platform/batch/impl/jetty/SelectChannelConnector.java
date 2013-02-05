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
package org.trpr.platform.batch.impl.jetty;

import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;

/**
 * An extension of {@link org.mortbay.jetty.nio.SelectChannelConnector}. Sets the 
 * port in {@link JobConfigurationService}
 * 
 * @author devashishshankar
 * @version 1.0, 31 Jan, 2013
 */
public class SelectChannelConnector extends
		org.mortbay.jetty.nio.SelectChannelConnector {
	
	/** Instance of {@link JobConfigurationService} */
	private JobConfigurationService jobConfigurationService;
	
	/** Setter method */
	public void setJobConfigService(JobConfigurationService jobConfigurationService){
		this.jobConfigurationService = jobConfigurationService;
	}
	
	/**
	 * Injects the port no. of the running Server into {@link JobConfigurationService}
	 */
	@Override
	public void setPort(int port) {
		this.jobConfigurationService.setPort(port);
		super.setPort(port);
	}
}
