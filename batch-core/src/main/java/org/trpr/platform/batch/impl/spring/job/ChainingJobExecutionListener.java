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
package org.trpr.platform.batch.impl.spring.job;

import java.util.LinkedList;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.trpr.platform.batch.impl.quartz.JobCompletionTriggerBean;
import org.trpr.platform.batch.impl.spring.jmx.JobAdministrator;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * The <code>ChainingJobExecutionListener</code> class is an implementation of the Spring Batch {@link JobExecutionListener} that matches the names of jobs that 
 * complete execution with {@link JobCompletionTriggerBean#getFollowJob()} of registered triggers set on this listener. For matching triggers, it then uses the
 * {@link JobAdministrator} to schedule the next execution, adding a start delay if specified.
 * 
 * @author Regunath B
 * @version 1.0, 21 Aug 2014
 */

public class ChainingJobExecutionListener implements JobExecutionListener {
	
	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(ChainingJobExecutionListener.class);
	
	/** List of JobCompletionTriggerBean instances that are to be triggered when job executions complete*/
	private List<JobCompletionTriggerBean> jobCompletionTriggers = new LinkedList<JobCompletionTriggerBean>();
	
	/** The JobAdministrator instance */
	private JobAdministrator jobAdministrator;
	
	/**
	 * Interface method implementation. Does nothing
	 * @see org.springframework.batch.core.JobExecutionListener#beforeJob(org.springframework.batch.core.JobExecution)
	 */
	public void beforeJob(JobExecution je) {
		// no op
	}
	
	/**
	 * Adds the specified JobCompletionTriggerBean to the list of Job completion triggers maintained by this listener
	 * @param jobCompletionTriggerBean the JobCompletionTriggerBean to add
	 */
	public void addJobCompletionTrigger(JobCompletionTriggerBean jobCompletionTriggerBean) {
		if (!this.jobCompletionTriggers.contains(jobCompletionTriggerBean)) {
			this.jobCompletionTriggers.add(jobCompletionTriggerBean);
		}
	}

	/**
	 * Interface method implementation. Matches the name of the job with any registered {@link JobCompletionTriggerBean#getFollowJob()} and accordingly sets the
	 * start trigger time.
	 * @see org.springframework.batch.core.JobExecutionListener#afterJob(org.springframework.batch.core.JobExecution)
	 */
	public void afterJob(JobExecution je) {
		String jobName = je.getJobInstance().getJobName();
		for (JobCompletionTriggerBean jobCompletionTriggerBean : this.jobCompletionTriggers) {			
			if (jobCompletionTriggerBean.getFollowJob().equalsIgnoreCase(jobName)) {
				if (jobCompletionTriggerBean.getStartDelay() > 0) {
					LOGGER.info("Using Job administrator to run job : {} on completion of job : {} with delay (in ms) : " + jobCompletionTriggerBean.getStartDelay(), 
							((Job)jobCompletionTriggerBean.getJobDetail().getJobDataMap().get(BatchJob.JOB_NAME)).getName(),jobName);
					try {
						Thread.sleep(jobCompletionTriggerBean.getStartDelay()); // add the delay to the same scheduler worker thread that did the callback
					} catch (InterruptedException e) {
						LOGGER.info("Sleep interrupted for running job : {} on completion of job : {}. Running job now.", 
								((Job)jobCompletionTriggerBean.getJobDetail().getJobDataMap().get(BatchJob.JOB_NAME)).getName(),jobName);
					}
					this.jobAdministrator.runJob(((Job)jobCompletionTriggerBean.getJobDetail().getJobDataMap().get(BatchJob.JOB_NAME)).getName());					
				} else {
					LOGGER.info("Using Job administrator to run job : {} on completion of job : {}", 
							((Job)jobCompletionTriggerBean.getJobDetail().getJobDataMap().get(BatchJob.JOB_NAME)).getName(),jobName);
					this.jobAdministrator.runJob(((Job)jobCompletionTriggerBean.getJobDetail().getJobDataMap().get(BatchJob.JOB_NAME)).getName());
				}
			}
		}
	}

	/** Getter/Setter methods */
	public JobAdministrator getJobAdministrator() {
		return jobAdministrator;
	}
	public void setJobAdministrator(JobAdministrator jobAdministrator) {
		this.jobAdministrator = jobAdministrator;
	}
	
}
