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
package org.trpr.platform.batch.impl.spring.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;


/**
 * The <code>BatchJob</code> class is a wrapper around the Spring Batch {@link StatefulJob} that is used for launching a Job execution.
 * This class is stateful i.e. a new job execution will not execute if one is running already.
 * 
 * @author Regunath B
 * @version 1.0, 1 Sep 2012
 */
public class BatchJob implements StatefulJob {

	/**
	 * Constant to be used for Job Parameter configuration.
	 */
	private static final String TIMESTAMP = "TIMESTAMP";

	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(BatchJob.class);

	/**
	 * The Job Name and Job Launcher constants to be accessed from Bean are
	 * declared.
	 */
	private static final String JOB_NAME = "jobName";
	private static final String JOB_LAUNCHER = "jobLauncher";
	
	/**
	 * Interface method implementation. Launches the job specified by the "jobName" job data parameter
	 * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			/** Running the batch. */
			JobParametersBuilder builder = new JobParametersBuilder();
			builder.addLong(TIMESTAMP, System.currentTimeMillis());
			((JobLauncher) context.getJobDetail().getJobDataMap().get(
					JOB_LAUNCHER)).run(
					(org.springframework.batch.core.Job) context.getJobDetail()
							.getJobDataMap().get(JOB_NAME), builder
							.toJobParameters());
		} catch (JobExecutionAlreadyRunningException e) {
			LOGGER.error("Specified job is already running : " + e);
		} catch (JobRestartException e) {
			LOGGER.error("Unable to restart specified batch job : " + e);
		} catch (JobInstanceAlreadyCompleteException e) {
			LOGGER.error("Specified job is already complete : " + e);
		} catch (Exception e) {
			LOGGER.error("Job execution failed : " + e);
		}
	}

}
