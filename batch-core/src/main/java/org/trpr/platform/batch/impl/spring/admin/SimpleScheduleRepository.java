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

package org.trpr.platform.batch.impl.spring.admin;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.Trigger;
import org.trpr.platform.batch.spi.spring.admin.ScheduleRepository;

/**
 * The <code>SimpleScheduleRepository</code> class is an implementation of {@link ScheduleRepository} and holds 
 * scheduler information
 * 
 * @author devashishshankar
 * @version 1.1 10 Jan 2013
 */

public class SimpleScheduleRepository implements ScheduleRepository {
	
	/**
	 * A Map holding the Trigger information related to the job. The key is the jobName and the value is org.quartz.Trigger
	 */
	private Map<String, Trigger> jobTrigger;
	private Map<String, String> jobBeanName;
	
	/**
	 * Default constructor. Initializes jobTrigger Map as a new HashMap
	 */
	public SimpleScheduleRepository() {		
		this.jobTrigger = new HashMap<String, Trigger> ();
		this.jobBeanName = new HashMap<String, String>();
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.ScheduleRepository#addTrigger
	 */
	@Override
	public void addTrigger(String jobName, String jobBeanName, Trigger trigger) {
		this.jobTrigger.put(jobName, trigger);		
		this.jobBeanName.put(jobName, jobBeanName);		
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.ScheduleRepository#getCronExpression
	 */
	@Override
	public String getCronExpression(String jobName) {
		String cronExpr = null;		
		if(jobTrigger.containsKey(jobName)) {			
			Trigger trigger = jobTrigger.get(jobName);		
			if (trigger instanceof CronTrigger) {
		        CronTrigger cronTrigger = (CronTrigger) trigger;
		        cronExpr = cronTrigger.getCronExpression();
		    }
		}
		return cronExpr;
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.ScheduleRepository#getNextFireDate
	 */
	@Override
	public Date getNextFireDate(String jobName) {		
		Date nextFireTime = null;		
		if(jobTrigger.containsKey(jobName)){
			Trigger trigger = jobTrigger.get(jobName);
			nextFireTime = trigger.getNextFireTime();
		}
		return nextFireTime;
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.ScheduleRepository#doesJobExists(String)
	 */
	@Override
	public boolean doesJobExists(String jobName) {
		if(this.jobTrigger.containsKey(jobName)) {
			return true;
		}
		return false;
	}
	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.ScheduleRepository#getJobBeanName
	 */
	@Override
	public String getJobBeanName(String jobName) {
		if(this.jobBeanName.containsKey(jobName))
			return this.jobBeanName.get(jobName);
		
		return null;
	}
}
