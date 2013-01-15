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

package org.trpr.platform.batch.impl.spring.web;

import java.util.Date;


/**
 * The <code>JobInfo</code> class is an extension of {@link JobInfo} that stores extra information about 
 * trigger, such as cronexpression, next fire time, etc.
 * 
 * 
 * @author devashishshankar
 * @version 1.0, 09 Jan 2013
 */
public class JobInfo extends org.springframework.batch.admin.web.JobInfo {

	//Holds the Cron Expression of the job trigger
	private final String cronExpression;
	//Holds the next fire time of the job
	private final Date nextFireTime;
	
	/**
	 * Constructor for initialising JobInfo
	 */
	public JobInfo(String name, int executionCount, Long jobInstanceId,
			boolean launchable, boolean incrementable, String cronExpression, Date nextFireTime) {
		
		//Calling super constructor to initialise the remaining attributes
		super(name, executionCount, jobInstanceId, launchable, incrementable);
		
		this.cronExpression = cronExpression;
		this.nextFireTime = nextFireTime;
		
	}
	
	/**
	 * Getter methods
	 */
	
	public String getcronExpression() {
		return cronExpression;
	}
	
	public String getnextFireTime() {
		return nextFireTime.toGMTString();
	}
	
	/**
	 * End of getter methods
	 */

}
