package org.trpr.platform.batch.spi.spring.admin;

import java.util.Date;
import java.util.Map;

import org.quartz.Scheduler;
import org.quartz.Trigger;


/**
 * AN interface to hold information about the JobScheduler and trigger.
 * 
 * @author devashishshankar
 * @version 1.0 10 Jan 2013
 */


public interface ScheduleRepository 
{

	
	public String getCronExpression(String jobName);
	
	public Date getNextFireDate(String jobName);

	boolean hasName(String jobName);

	void addTrigger(String jobName, Trigger trigger);

}
