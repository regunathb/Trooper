package org.trpr.platform.batch.spi.spring.admin;

import java.util.Date;

import org.quartz.impl.SchedulerRepository;

/**
 * The <code>JobService</code> interface is an extension of {@link JobService} that holds {@link SchedulerRepository} which will have access to the trigger information
 * 
 * 
 * @author devashishshankar
 * @version 1.0, 10 Jan 2013
 */
public interface JobService extends org.springframework.batch.admin.service.JobService {
	
	/**
	 * Returns the CronExpression based on the jobName
	 * @param jobName The name of the job
	 * @return CronExpression in a String
	 */
	public String getCronExpression(String jobName);
	
	/**
	 * Returns the NextFireDate based on the jobName
	 * @param jobName The name of the job
	 * @return NextFireDate in a Date
	 */
	public Date getNextFireDate(String jobName);
}
