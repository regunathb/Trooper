package org.trpr.platform.batch.impl.spring.web;

import java.util.Arrays;
import java.util.List;

import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.springframework.batch.admin.web.JobController;
import org.springframework.batch.core.job.flow.FlowJob;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.trpr.platform.batch.impl.spring.SpringBatchComponentContainer;
import org.trpr.platform.batch.impl.spring.admin.SimpleScheduleRepository;
import org.trpr.platform.batch.spi.spring.admin.ScheduleRepository;



/**
 * The <code>SchedulerFactoryBean</code> class is an extension of {@link org.springframework.scheduling.quartz.SchedulerFactoryBean } that injects extra information about trigger, such as cronexpression, next fire time, etc. into {@link ScheduleRepository}
 * 
 * @author devashishshankar
 * @version 1.0, 09 Jan 2013
 */

public class SchedulerFactoryBean extends
		org.springframework.scheduling.quartz.SchedulerFactoryBean 

{
	private List triggers;
	
	private List jobDetails;
	
	public void setJobDetails(JobDetail[] jobDetails) {
		
		super.setJobDetails(jobDetails);
		this.jobDetails = Arrays.asList(jobDetails);
	}
	
	public void setTriggers(Trigger[] triggers) {
		
		super.setTriggers(triggers);
		
		this.triggers = Arrays.asList(triggers);
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();
		
		ApplicationContext context = SpringBatchComponentContainer.getCommonBatchBeansContext();
		
		
		//Instance of bean SimpleScheduleRepsitory
		SimpleScheduleRepository rep = context.getBean(SimpleScheduleRepository.class);
		
		
		System.out.print(this.triggers.get(0));
		
	
	      
		Scheduler sch = super.getScheduler();
		try
		{
				for (String groupName : sch.getJobGroupNames()) 
				{

					//loop all jobs by groupname
					for (String jobName : sch.getJobNames(groupName)) 
					{
				 
				      //get job's trigger
					  Trigger[] triggers = sch.getTriggersOfJob(jobName,groupName);				 
					  JobDetail jd = sch.getJobDetail(jobName, groupName);
				      JobDataMap jdm = jd.getJobDataMap();
				      FlowJob fj = (FlowJob)jdm.get("jobName");
				      
				      //System.out.println("Adding to scheduleRep: "+fj.getName()+" "+triggers[0]);
				      
				      //Injecting into SimpleScheduleRepository
					  rep.addTrigger(fj.getName(), triggers[0]);
			 
					}
			 
			    }
			
		}
		catch(SchedulerException temp)
		{
			System.out.println("Exception while fetching info from scheduler : "+temp.getMessage());
		}
	}

}
