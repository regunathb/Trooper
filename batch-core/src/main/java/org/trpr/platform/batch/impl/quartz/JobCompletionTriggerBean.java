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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SimpleTriggerBean;
import org.springframework.util.Assert;
import org.trpr.platform.batch.impl.spring.SpringBatchComponentContainer;
import org.trpr.platform.batch.impl.spring.job.ChainingJobExecutionListener;

/**
 * The <code>JobCompletionTriggerBean</code> class is a sub-type of the Spring {@link SimpleTriggerBean} that has the following functionality:
 * <pre>
 * <li>Requires a Job Name identifying the preceding job (followJob) that on execution completion, will trigger the job configured on this trigger</li>
 * <li>Sets properties on this trigger such that it never fires. This trigger is therefore used simply as consistent way to schedule jobs</li>
 * <pre>
 * Note that the {@link SimpleTriggerBean#getNextFireTime()} for this Trigger will be indicative only and not accurate as the real fire time will be
 * after the preceding job completes.
 * 
 * @author Regunath B
 * @version 1.0, 22 Aug 2014
 */
public class JobCompletionTriggerBean extends SimpleTriggerBean {

	/** Default serialversion UID */
	private static final long serialVersionUID = 1L;
	
	/** The preceding job whose execution will cause this Trigger to fire */
	private String followJob;
	
	/** The start delay, if any*/
	private long startDelayInMs;
		
	/**
	 * Overriden method. Invokes the super type's implementation and sets the {@link SimpleTriggerBean#setStartTime(java.util.Date)} to Epoch i.e. not to fire.
	 * Also checks if {@link #getFollowJob()} is set and throws exception when not set.
	 * @see org.springframework.scheduling.quartz.SimpleTriggerBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws ParseException {
		Assert.notNull(followJob, "'followJob' must be set to a valid job name");		
		super.afterPropertiesSet();
		// add this job completion trigger to the list of triggers maintained by the ChainingJobExecutionListener
		ApplicationContext commonBeansContext = SpringBatchComponentContainer.getCommonBatchBeansContext();
		((ChainingJobExecutionListener)commonBeansContext.getBean(ChainingJobExecutionListener.class)).addJobCompletionTrigger(this);
		this.setStartTime(new Date(0)); // set start time as Epoch i.e. we dont want it to fire now
		this.setRepeatCount(0);	// set repeat count to 0 i.e. no repeat
		this.setMisfireInstruction(MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT); // ignore misfires
		// now set the next fire time to 1000 years later i.e. dont intend to fire this trigger
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1000);
		this.setNextFireTime(cal.getTime());
	}
	
	/**
	 * Overriden superclass method. Stores the delay in a local variable for use when rescheduling this trigger
	 * @see org.springframework.scheduling.quartz.SimpleTriggerBean#setStartDelay(long)
	 */
	public void setStartDelay(long startDelay) {
		super.setStartDelay(startDelay);
		this.startDelayInMs = startDelay;
	}
	
	/**
	 * Overriden method. Tries to get the next fire time from the "followJob"'s trigger. Returns null if trigger cannot be found
	 * @see org.quartz.SimpleTrigger#getNextFireTime()
	 */
	public Date getNextFireTime() {		
		ApplicationContext commonBeansContext = SpringBatchComponentContainer.getCommonBatchBeansContext();
		Date nextFireTime = ((SimpleScheduleRepository)commonBeansContext.getBean(SimpleScheduleRepository.class)).getNextFireDate(this.getFollowJob());	
		if (nextFireTime != null) {
			nextFireTime = new Date(nextFireTime.getTime() + this.startDelayInMs);
			return nextFireTime;
		}
		return super.getNextFireTime();
	}
	
	/** Getter/Setter methods */
	public String getFollowJob() {
		return followJob;
	}
	public void setFollowJob(String followJob) {
		this.followJob = followJob;
	}
	public long getStartDelay() {
		return startDelayInMs;
	}	
}
