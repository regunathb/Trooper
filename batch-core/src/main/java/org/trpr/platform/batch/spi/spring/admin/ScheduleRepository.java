package org.trpr.platform.batch.spi.spring.admin;

import java.util.Date;

import org.quartz.Trigger;


/**
 * An interface to hold information about the JobScheduler and trigger.
 * 
 * @author devashishshankar
 * @version 1.0 10 Jan 2013
 */

public interface ScheduleRepository {
	/**
	 * Returns the cron expression based on the jobName.
	 * @param jobName Name of the job
	 * @return CronExpression in String. Returns null if the requested job is not found in the repository.
	 */
	public String getCronExpression(String jobName);
	
	/**
	 * Returns the next fire time based on the jobName.
	 * @param jobName Name of the job
	 * @return NextFireTime in Date. Returns null if the requested job is not found in the repository.
	 */
	public Date getNextFireDate(String jobName);
	
	/**
	 * Function to check whether the job is in the repository.
	 * @param jobName Name of the job
	 */
	public boolean hasName(String jobName);
	
	/**
	 * Injector function to inject the Trigger information related to the job in the repository
	 * @param jobName Name of the job
	 * @param trigger Quartz trigger related to the job
	 */
	public void addTrigger(String jobName, Trigger trigger);

}
