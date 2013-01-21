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

import java.util.Date;
import java.util.List;

import org.quartz.impl.SchedulerRepository;
import org.springframework.web.multipart.MultipartFile;
import org.trpr.platform.runtime.spi.component.ComponentContainer;

/**
 * The <code>JobService</code> interface is an extension of {@link JobService} that holds {@link SchedulerRepository} which will have access to the 
 * trigger information
 * 
 * @author devashishshankar
 * @version 1.0, 10 Jan 2013
 */
public interface JobService extends org.springframework.batch.admin.service.JobService {
	
	/**
	 * Returns the CronExpression based on the jobName
	 * @param jobName The name of the job
	 * @return CronExpression in a String
	 */
	public String getCronExpression(String jobName);
	
	/**
	 * Returns the NextFireDate based on the jobName
	 * @param jobName The name of the job
	 * @return NextFireDate in a Date
	 */
	public Date getNextFireDate(String jobName);
	
	/**
	 * Gets the XML File path for given job. If not found, returns null.
	 * @param jobName
	 * @return Path of XMLFile. null if not found
	 */
	public String getXMLFile(String jobName);
	
	/**
	 * Add a job dependency for a given job. Also uploads the dependency file to the 
	 * @param jobName Name of the job
	 */
	public void addJobDependency(String jobName, MultipartFile file);
	
	/** 
	 * Returns the list of dependencies of given job. 
	 * @param jobName Name of the job
	 * @return List of dependencies. If not found, returns null
	 */
	public List<String> getJobDependencyList(String jobName);
	
	/**
	 * Gets the jobDirectory of a job, where all the config-files and dependencies are stored
	 */
	public String getJobDirectory(String jobName);
		
  /** 	
   * Gets the {@link ComponentContainer} that loaded this JobService
   * @return the ComponentContainer that loaded this JobService
   */
	public ComponentContainer getComponentContainer();

  /**
   * Sets the {@link ComponentContainer} that loaded this JobService
   * @param componentContainer the ComponentContainer that loaded this JobService
   */
	public void setComponentContainer(ComponentContainer componentContainer);
	
	/**
	 * Returns whether the job is in jobService
	 */
	public boolean contains(String jobName);

	
	 
  
}
