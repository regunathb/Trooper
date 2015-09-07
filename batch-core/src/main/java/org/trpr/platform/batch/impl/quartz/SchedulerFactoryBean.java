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

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.batch.core.job.flow.FlowJob;
import org.springframework.context.ApplicationContext;
import org.trpr.platform.batch.impl.spring.SpringBatchComponentContainer;
import org.trpr.platform.batch.spi.quartz.ScheduleRepository;

/**
 * The <code>SchedulerFactoryBean</code> class is an extension of 
 * {@link org.springframework.scheduling.quartz.SchedulerFactoryBean } that injects extra information 
 * about trigger, such as cronexpression, next fire time, etc. into {@link ScheduleRepository}
 * 
 * @author devashishshankar
 * @version 1.0, 09 Jan 2013
 */
public class SchedulerFactoryBean extends org.springframework.scheduling.quartz.SchedulerFactoryBean {

	/**
	 * Overridden method to inject additional attributes post bean initialization
	 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean#afterPropertiesSet()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		//Calling the super class method to perform all the functions it was previously performing
		super.afterPropertiesSet();
		//Getting ApplicationContext
		ApplicationContext context = SpringBatchComponentContainer.getCommonBatchBeansContext();
		//Instance of bean SimpleScheduleRepsitory from ApplicationContext
		SimpleScheduleRepository rep = context.getBean(SimpleScheduleRepository.class);
		//Getting the scheduler from super
		Scheduler sch = super.getScheduler();
		for (JobKey jobKey : sch.getJobKeys(GroupMatcher.anyJobGroup())) {
		  //get job's JobDetail 
		  JobDetail jd = sch.getJobDetail(jobKey);
		  //Extract job's name from JobDetail
	      JobDataMap jdm = jd.getJobDataMap();
	      FlowJob fj = (FlowJob)jdm.get("jobName");
	      //Injecting into SimpleScheduleRepository
	      rep.addScheduler(fj.getName(), sch);
		}
	}
}

