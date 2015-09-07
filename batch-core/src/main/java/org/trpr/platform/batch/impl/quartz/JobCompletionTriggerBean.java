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

import java.util.Calendar;
import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.impl.triggers.SimpleTriggerImpl;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.trpr.platform.batch.impl.spring.SpringBatchComponentContainer;
import org.trpr.platform.batch.impl.spring.job.ChainingJobExecutionListener;

/**
 * The <code>JobCompletionTriggerBean</code> is a subtype of the Quartz {@link SimpleTriggerImpl} that has the following functionality:
 * <pre>
 * <li>Requires a Job Name identifying the preceding job (followJob) that on execution completion, will trigger the job configured on this trigger</li>
 * <li>Sets properties on this trigger such that it never fires. This trigger is therefore used simply as consistent way to schedule jobs</li>
 * <pre>
 * Note that the {@link SimpleTrigger#getNextFireTime()} for this Trigger will be indicative only and not accurate as the real fire time will be
 * after the preceding job completes.
 * 
 * @author Regunath B
 * @version 1.0, 27 Aug 2015
 */
public class JobCompletionTriggerBean extends SimpleTriggerImpl implements BeanNameAware, InitializingBean {
	
	/** Default serial version*/
	private static final long serialVersionUID = 1L;

	/** The preceding job whose execution will cause this Trigger to fire */
	private String followJob;
	
	/** The name of an instance of this bean*/
	private String beanName;
	
	/** Delay in starting this Trigger*/
	private long startDelay;
	
	/** The JobDetail*/
	private JobDetail jobDetail;
	
	/**
	 * Overriden method. Invokes the super type's implementation and sets the {@link SimpleTriggerBean#setStartTime(java.util.Date)} to Epoch i.e. not to fire.
	 * Also checks if {@link #getFollowJob()} is set and throws exception when not set.
	 * @see org.springframework.scheduling.quartz.SimpleTriggerBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		Assert.notNull(followJob, "'followJob' must be set to a valid job name");
		if (this.getName() == null) {
			this.setName(this.beanName);
		}
		if (this.getGroup() == null) {
			this.setGroup(Scheduler.DEFAULT_GROUP);
		}
		if (this.jobDetail != null) {
			this.getJobDataMap().put("jobDetail", this.jobDetail);
		}		
		if (this.getStartDelay() > 0 || this.getStartTime() == null) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(System.currentTimeMillis() + this.getStartDelay());
			this.setStartTime(c.getTime());
		}		
		this.setJobKey(this.jobDetail.getKey());
		this.setStartTime(new Date(0)); // we set start date to Epoch i.e much earlier to current time
		this.setRepeatInterval(Long.MAX_VALUE); // very large repeat interval to ensure that it doesnot repeat on its own
		
		// add this job completion trigger to the list of triggers maintained by the ChainingJobExecutionListener
		ApplicationContext commonBeansContext = SpringBatchComponentContainer.getCommonBatchBeansContext();
		((ChainingJobExecutionListener)commonBeansContext.getBean(ChainingJobExecutionListener.class)).addJobCompletionTrigger(this);
		this.setStartTime(new Date(0)); // set start time as Epoch i.e. we dont want it to fire now
		this.setRepeatCount(0);	// set repeat count to 0 i.e. no repeat
		this.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT); // ignore misfires
		// now set the next fire time to 1000 years later i.e. dont intend to fire this trigger
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1000);
		this.setNextFireTime(cal.getTime());
	}
		
	/**
	 * Overriden method. Tries to get the next fire time from the "followJob"'s trigger. Returns null if trigger cannot be found
	 * @see org.quartz.SimpleTrigger#getNextFireTime()
	 */
	public Date getNextFireTime() {		
		ApplicationContext commonBeansContext = SpringBatchComponentContainer.getCommonBatchBeansContext();
		Date nextFireTime = ((SimpleScheduleRepository)commonBeansContext.getBean(SimpleScheduleRepository.class)).getNextFireDate(this.getFollowJob());	
		if (nextFireTime != null) {
			nextFireTime = new Date(nextFireTime.getTime() + this.startDelay);
			return nextFireTime;
		}
		return super.getNextFireTime();
	}
	
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	/** Getter/Setter methods */
	public String getFollowJob() {
		return followJob;
	}
	public void setFollowJob(String followJob) {
		this.followJob = followJob;
	}
	public void setJobDetail(JobDetail jobDetail) {
		this.jobDetail = jobDetail;
	}
	public JobDetail getJobDetail() {
		return this.jobDetail;
	}
	public void setStartDelay(long startDelay) {
		this.startDelay = startDelay;
	}
	public long getStartDelay() {
		return this.startDelay;
	}
}
