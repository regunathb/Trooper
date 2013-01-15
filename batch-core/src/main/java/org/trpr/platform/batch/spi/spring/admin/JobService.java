package org.trpr.platform.batch.spi.spring.admin;

import java.util.Date;
import java.util.List;

import org.quartz.Scheduler;
import org.quartz.impl.SchedulerRepository;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * The <code>JobService</code> interface is an extension of {@link JobService} that holds {@link SchedulerRepository} which will have access to the trigger information
 * 
 * 
 * @author devashishshankar
 * @version 1.0, 10 Jan 2013
 */
public interface JobService extends org.springframework.batch.admin.service.JobService
{
	
	public String getCronExpression(String jobName);
	
	public Date getNextFireDate(String jobName);

	

}
