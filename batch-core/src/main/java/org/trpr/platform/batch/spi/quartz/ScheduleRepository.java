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
package org.trpr.platform.batch.spi.quartz;

import java.util.Date;

import org.quartz.Scheduler;
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
	 * Checks whether the job is in the repository.
	 * @param jobName Name of the job
	 */
	public boolean doesJobExists(String jobName);
	
	/**
	 * Injector function to inject the Scheduler containing Trigger information related to 
	 * the job in the repository
	 * @param jobName Name of the job
	 * @param scheduler Quartz scheduler related to the job
	 */
	public void addScheduler(String jobName, Scheduler scheduler);
}
