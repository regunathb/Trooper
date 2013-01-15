package org.trpr.platform.batch.impl.spring.admin;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.batch.core.job.flow.FlowJob;
import org.trpr.platform.batch.spi.spring.admin.JobService;
import org.trpr.platform.batch.spi.spring.admin.ScheduleRepository;
import org.quartz.Scheduler;


/**
 * The <code>SimpleScheduleRepository</code> class is an implementation of {@link ScheduleRepository} and holds 
 * scheduler information
 * 
 * @author devashishshankar
 * @version 1.1 10 Jan 2013
 */


public class SimpleScheduleRepository implements ScheduleRepository
{
	private Map<String, Trigger> jobTrigger;
	
	public SimpleScheduleRepository() {
		
		this.jobTrigger = new HashMap<String, Trigger> ();
	}

	
	@Override
	public void addTrigger(String jobName, Trigger trigger) {
		
		//System.out.println ("Putting: "+jobName+ " "+trigger);
		
		this.jobTrigger.put(jobName, trigger);
		
	}

	@Override
	public String getCronExpression(String jobName) {
		
		String cronExpr = null;
		
		if(jobTrigger.containsKey(jobName))
		{
			Trigger trigger = jobTrigger.get(jobName);
		
			if (trigger instanceof CronTrigger) 
			{
		        CronTrigger cronTrigger = (CronTrigger) trigger;
		        cronExpr = cronTrigger.getCronExpression();
		    }
		}

		return cronExpr;
		
	}

	@Override
	public Date getNextFireDate(String jobName) {
		
		
		Date nextFireTime = null;
		
		if(jobTrigger.containsKey(jobName))
		{
			Trigger trigger = jobTrigger.get(jobName);
			nextFireTime = trigger.getNextFireTime();
		}
		return nextFireTime;
	}

	@Override
	public boolean hasName(String jobName) {
		
		if(this.jobTrigger.containsKey(jobName))
			return true;
		return false;
	}


}
