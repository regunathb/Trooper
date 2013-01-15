package org.trpr.platform.batch.impl.spring.web;

import java.util.Date;

import org.springframework.batch.admin.web.JobController;
import org.springframework.batch.admin.web.JobInfo;


/**
 * The <code>ExtendedJobInfo</code> class is an extension of {@link JobInfo} that stores extra information about trigger, such as cronexpression, next fire time, etc.
 * 
 * @author devashishshankar
 * @version 1.0, 09 Jan 2013
 */
public class ExtendedJobInfo extends JobInfo {

	
	public final String cronExpression;
	
	public final Date nextFireTime;
	
	public ExtendedJobInfo(String name, int executionCount, Long jobInstanceId,
			boolean launchable, boolean incrementable, String cronExpression, Date nextFireTime) {
		super(name, executionCount, jobInstanceId, launchable, incrementable);
		
		this.cronExpression = cronExpression;
		this.nextFireTime = nextFireTime;
		
	}
	
	public String getcronExpression()
	{
		return cronExpression;
	}
	
	public String getnextFireTime()
	{
		return nextFireTime.toGMTString();
	}
	
	
	


}
