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
package org.trpr.platform.batch.impl.quartz;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.batch.core.job.flow.FlowJob;
import org.trpr.platform.batch.impl.spring.web.JobConfigController;
import org.trpr.platform.batch.spi.quartz.ScheduleRepository;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;


/**
 * The <code>SimpleScheduleRepository</code> class is an implementation of {@link ScheduleRepository} and holds 
 * scheduler information
 * 
 * @author devashishshankar
 * @version 1.1 10 Jan 2013
 */
public class SimpleScheduleRepository implements ScheduleRepository {
	
	/**
	 * A Map holding the Trigger information related to the job. The key is the jobName 
	 * and the value is org.quartz.Trigger
	 */
	private Map<String, Scheduler> jobScheculer;
	
	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(JobConfigController.class);
	
	/**
	 * Default constructor. Initializes jobTrigger Map as a new HashMap
	 */
	public SimpleScheduleRepository() {		
		this.jobScheculer = new HashMap<String, Scheduler> ();
	}
	
	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.ScheduleRepository#getCronExpression
	 */
	@Override
	public String getCronExpression(String jobName) {
		String cronExpr = null;		
		if(this.jobScheculer.containsKey(jobName)) {			
			Trigger trigger = this.getTriggerFromScheduler(this.jobScheculer.get(jobName), jobName);
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
		if(this.jobScheculer.containsKey(jobName)){
			Trigger trigger = this.getTriggerFromScheduler(this.jobScheculer.get(jobName), jobName);
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
		if(this.jobScheculer.containsKey(jobName)) {
			return true;
		}
		return false;
	}

	/**
	 * Interface Method Implementation.
	 * @see org.trpr.platform.batch.spi.spring.admin.ScheduleRepository#addScheduler
	 */
	@Override
	public void addScheduler(String jobName, Scheduler scheduler) {
		this.jobScheculer.put(jobName, scheduler);
	}
	
	/**
	 * Helper method to get the Trigger from the Scheduler
	 */
	private Trigger getTriggerFromScheduler(Scheduler sch, String requiredJobName) {
		try {
			for (String groupName : sch.getJobGroupNames()) {
				//loop all jobs by groupname
				for (String jobName : sch.getJobNames(groupName)) {
			      //get job's trigger
				  Trigger[] triggers = sch.getTriggersOfJob(jobName,groupName);	
				  //get job's JobDetail 
				  JobDetail jd = sch.getJobDetail(jobName, groupName);
				  //Extract job's name from JobDetail
			      JobDataMap jdm = jd.getJobDataMap();
			      FlowJob fj = (FlowJob)jdm.get("jobName");
			      String fjName = fj.getName();
			      //Injecting into SimpleScheduleRepository
				  if(fjName.equals(requiredJobName)) {
					  return triggers[0];
				  }
				}
			}
		} catch (SchedulerException e) {
			LOGGER.error("Error getting Trigger from scheduler",e);
		}
		return null;
	}
}
