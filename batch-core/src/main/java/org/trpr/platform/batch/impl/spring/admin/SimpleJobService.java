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
package org.trpr.platform.batch.impl.spring.admin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.admin.service.NoSuchStepExecutionException;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.step.NoSuchStepException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;


/**
 * The <code>SimpleJobService</code> class is an implementation of {@link JobService} that delegates most of its work to the standard Spring Batch 
 * components
 * 
 * @author Regunath B
 * @version 1.0, 19 Sep 2012
 */
public class SimpleJobService implements JobService, DisposableBean {
	
	/** Default shutdown timeout - 60 seconds */
	private static final int DEFAULT_SHUTDOWN_TIMEOUT = 60 * 1000;
	
	/** Max executions limit - 10000*/
	private static final int MAX_EXECUTIONS = 10000;
	
	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(SimpleJobService.class);
	
	/** The shutdown timeout*/
	private int shutdownTimeout = DEFAULT_SHUTDOWN_TIMEOUT;

	/** List of active JobExecutionS*/
	private Collection<JobExecution> activeExecutions = Collections.synchronizedList(new ArrayList<JobExecution>());
	
	/** The JobRepository component*/
	private JobRepository jobRepository;

	/** The JobRepository component*/
	private JobRegistry jobRegistry;

	/** The JobOperator component*/
	private JobOperator jobOperator;	
	
	/** The JobLauncher component*/
	private JobLauncher jobLauncher;

	/** The JobExplorer component*/
	private JobExplorer jobExplorer;
	
	/**
	 * Constructor for this class
	 * @param jobRepository the JobRepository
	 */
	public SimpleJobService(JobRepository jobRepository, JobOperator jobOperator, JobExplorer jobExplorer, JobRegistry jobRegistry, JobLauncher jobLauncher) {
		this.jobRepository = jobRepository;
		this.jobOperator = jobOperator;
		this.jobExplorer = jobExplorer;
		this.jobRegistry = jobRegistry;
		this.jobLauncher = jobLauncher;
	}

	/**
	 * Interface method implementation.Stops all the active jobs and wait for them (up to a time out) to finish
	 * processing.
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		Exception firstException = null;
		for (JobExecution jobExecution : activeExecutions) {
			try {
				if (jobExecution.isRunning()) {
					stop(jobExecution.getId());
				}
			} catch (JobExecutionNotRunningException e) {
				LOGGER.info("JobExecution is not running so it cannot be stopped");
			} catch (Exception e) {
				LOGGER.error("Unexpected exception stopping JobExecution", e);
				if (firstException == null) {
					firstException = e;
				}
			}
		}
		int count = 0;
		int maxCount = (shutdownTimeout + 1000) / 1000;
		while (!activeExecutions.isEmpty() && ++count < maxCount) {
			LOGGER.error("Waiting for " + activeExecutions.size() + " active executions to complete");
			removeInactiveExecutions();
			Thread.sleep(1000L);
		}
		if (firstException != null) {
			throw firstException;
		}		
	}

	/**
	 * Check all the active executions and see if they are still actually
	 * running. Remove the ones that have completed.
	 */
	@Scheduled(fixedDelay = 60000)
	public void removeInactiveExecutions() {
		for (Iterator<JobExecution> iterator = activeExecutions.iterator(); iterator.hasNext();) {
			JobExecution jobExecution = iterator.next();
			try {
				jobExecution = getJobExecution(jobExecution.getId());
			} catch (NoSuchJobExecutionException e) {
				LOGGER.error("Unexpected exception loading JobExecution", e);
			}
			if (!jobExecution.isRunning()) {
				iterator.remove();
			}
		}
	}
	
	/**
	 * Interface method implementation. 
	 * @see org.springframework.batch.admin.service.JobService#abandon(java.lang.Long)
	 */
	public JobExecution abandon(Long jobExecutionId) throws NoSuchJobExecutionException, JobExecutionAlreadyRunningException {
		JobExecution jobExecution = getJobExecution(jobExecutionId);
		if (jobExecution.getStatus().isLessThan(BatchStatus.STOPPING)) {
			throw new JobExecutionAlreadyRunningException(
					"JobExecution is running or complete and therefore cannot be aborted");
		}

		LOGGER.info("Aborting job execution: " + jobExecution);
		jobExecution.upgradeStatus(BatchStatus.ABANDONED);
		jobExecution.setEndTime(new Date());
		jobRepository.update(jobExecution);
		return jobExecution;		
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#countJobExecutions()
	 */
	public int countJobExecutions() {
		int count = 0;
		for (String jobName : this.jobOperator.getJobNames()) {
			for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) {
				count += this.jobExplorer.getJobExecutions(jobInstance).size();
			}
			
		}
		return count;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#countJobExecutionsForJob(java.lang.String)
	 */
	public int countJobExecutionsForJob(String jobName) throws NoSuchJobException {
		int count = 0;
		for (String name : this.jobOperator.getJobNames()) {
			if (name.equalsIgnoreCase(jobName)) {
				for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) {
					count += this.jobExplorer.getJobExecutions(jobInstance).size();
				}
				break;
			}
		}
		return count;
	}

	/**
	 * Interface method implementation.
	 * @see org.springframework.batch.admin.service.JobService#countJobInstances(java.lang.String)
	 */
	public int countJobInstances(String jobName) throws NoSuchJobException {
		int count = 0;
		for (String name : this.jobOperator.getJobNames()) {
			if (name.equalsIgnoreCase(jobName)) {
				count += this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS).size(); 
				break;
			}
		}		
		return count;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#countJobs()
	 */
	public int countJobs() {
		return  this.jobOperator.getJobNames().size();
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#countStepExecutionsForStep(java.lang.String, java.lang.String)
	 */
	public int countStepExecutionsForStep(String jobName, String stepName) throws NoSuchStepException {
		int count = 0;
		for (String name : this.jobOperator.getJobNames()) {
			if (name.contains(jobName)) {
				for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) { 
					for (JobExecution jobExecution : this.jobExplorer.getJobExecutions(jobInstance)) {
						Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
						for (StepExecution step : stepExecutions) {	
							if (step.getStepName().contains(stepName)) {
								count += 1;
							}
						}
					}
				}
			}
		}				
		return count;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#getJobExecution(java.lang.Long)
	 */
	public JobExecution getJobExecution(Long jobExecutionId) throws NoSuchJobExecutionException {
		for (String jobName : this.jobOperator.getJobNames()) {
			for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) { 
				for (JobExecution jobExecution : this.jobExplorer.getJobExecutions(jobInstance)) {
					if (jobExecution.getId() == jobExecutionId) {
						return jobExecution;
					}
				}
			}
		}						
		return null;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#getJobExecutionsForJobInstance(java.lang.String, java.lang.Long)
	 */
	public Collection<JobExecution> getJobExecutionsForJobInstance(String jobName, Long jobInstanceId) throws NoSuchJobException {
		for (String name : this.jobOperator.getJobNames()) {
			if (name.contains(jobName)) {
				for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) { 
					if (jobInstance.getId() == jobInstanceId) {
						return this.jobExplorer.getJobExecutions(jobInstance);
					}
				}
			}
		}				
		return null;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#getJobInstance(long)
	 */
	public JobInstance getJobInstance(long jobInstanceId) throws NoSuchJobInstanceException {
		for (String jobName : this.jobOperator.getJobNames()) {
			for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) { 
				if (jobInstance.getId() == jobInstanceId) {
					return jobInstance;
				}
			}
		}						
		return null;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#getLastJobParameters(java.lang.String)
	 */
	public JobParameters getLastJobParameters(String jobName) throws NoSuchJobException {
		for (String name : this.jobOperator.getJobNames()) {
			if (name.contains(jobName)) {
				// get the last run JobInstance if any
				for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, 1)) {  // end is set as 1 to get a single element List
					return jobInstance.getJobParameters();
				}
			}
		}			
		return null;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#getStepExecution(java.lang.Long, java.lang.Long)
	 */
	public StepExecution getStepExecution(Long jobExecutionId, Long stepExecutionId) throws NoSuchStepExecutionException, NoSuchJobExecutionException {
		for (String jobName : this.jobOperator.getJobNames()) {
			for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) { 
				for (JobExecution jobExecution : this.jobExplorer.getJobExecutions(jobInstance)) {
					if (jobExecution.getId() == jobExecutionId) {
						for (StepExecution step : jobExecution.getStepExecutions()) {	
							if (step.getId() == stepExecutionId) {
								return step;
							}
						}
					}
				}
			}
		}				
		return null;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#getStepExecutions(java.lang.Long)
	 */
	public Collection<StepExecution> getStepExecutions(Long jobExecutionId) throws NoSuchJobExecutionException {
		for (String jobName : this.jobOperator.getJobNames()) {
			for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) { 
				for (JobExecution jobExecution : this.jobExplorer.getJobExecutions(jobInstance)) {
					if (jobExecution.getId() == jobExecutionId) {
						return jobExecution.getStepExecutions();
					}
				}
			}
		}				
		return null;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#getStepNamesForJob(java.lang.String)
	 */
	public Collection<String> getStepNamesForJob(String jobName) throws NoSuchJobException {
		Collection<String> stepNames = new LinkedHashSet<String>();
		for (JobExecution jobExecution : listJobExecutionsForJob(jobName, 0, 100)) {
			for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
				stepNames.add(stepExecution.getStepName());
			}
		}
		return Collections.unmodifiableList(new ArrayList<String>(stepNames));
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#isIncrementable(java.lang.String)
	 */
	public boolean isIncrementable(String jobName) {
		try {
			return this.jobRegistry.getJobNames().contains(jobName) && this.jobRegistry.getJob(jobName).getJobParametersIncrementer() != null;
		} catch (NoSuchJobException e) {
			// Should not happen
			throw new IllegalStateException("Unexpected non-existent job: " + jobName);
		}
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#isLaunchable(java.lang.String)
	 */
	public boolean isLaunchable(String jobName) {
		return this.jobRegistry.getJobNames().contains(jobName);
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#launch(java.lang.String, org.springframework.batch.core.JobParameters)
	 */
	public JobExecution launch(String jobName, JobParameters jobParameters) throws NoSuchJobException,
		JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {

		Job job = this.jobRegistry.getJob(jobName);
		JobExecution lastJobExecution = this.jobRepository.getLastJobExecution(jobName, jobParameters);
		boolean restart = false;
		if (lastJobExecution != null) {
			BatchStatus status = lastJobExecution.getStatus();
			if (status.isUnsuccessful() && status!=BatchStatus.ABANDONED) {
				restart = true;
			}
		}
		if (job.getJobParametersIncrementer() != null && !restart) {
			jobParameters = job.getJobParametersIncrementer().getNext(jobParameters);
		}
		JobExecution jobExecution = this.jobLauncher.run(job, jobParameters);
		if (jobExecution.isRunning()) {
			this.activeExecutions.add(jobExecution);
		}
		return jobExecution;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#listJobExecutions(int, int)
	 */
	public Collection<JobExecution> listJobExecutions(int start, int count) {
		List<JobExecution> executionList = new LinkedList<JobExecution>();
		for (String jobName : this.jobOperator.getJobNames()) {
			for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) {
				executionList.addAll(this.jobExplorer.getJobExecutions(jobInstance));				
			}
		}
		if (start >= executionList.size()) {
			start = executionList.size();
		}
		if (start + count >= executionList.size()) {
			count = executionList.size() - start;
		}			
		return executionList.subList(start, count);
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#listJobExecutionsForJob(java.lang.String, int, int)
	 */
	public Collection<JobExecution> listJobExecutionsForJob(String jobName, int start, int count) throws NoSuchJobException {
		for (String name : this.jobOperator.getJobNames()) {
			if (name.contains(jobName)) {
				for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) { 
					List<JobExecution> executionList = this.jobExplorer.getJobExecutions(jobInstance);
					if (start >= executionList.size()) {
						start = executionList.size();
					}
					if (start + count >= executionList.size()) {
						count = executionList.size() - start;
					}								
					return executionList.subList(start, count);
				}
			}
		}				
		return null;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#listJobInstances(java.lang.String, int, int)
	 */
	public Collection<JobInstance> listJobInstances(String jobName, int start, int count) throws NoSuchJobException {
		for (String name : this.jobOperator.getJobNames()) {
			if (name.contains(jobName)) {
				List<JobInstance> instanceList = this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS);
				if (start >= instanceList.size()) {
					start = instanceList.size();
				}
				if (start + count >= instanceList.size()) {
					count = instanceList.size() - start;
				}								
				return instanceList.subList(start, count);
			}
		}				
		return null;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#listJobs(int, int)
	 */
	public Collection<String> listJobs(int start, int count) {
		List<String> jobNames = new LinkedList<String>();
		jobNames.addAll(this.jobOperator.getJobNames());
		if (start >= jobNames.size()) {
			start = jobNames.size();
		}
		if (start + count >= jobNames.size()) {
			count = jobNames.size() - start;
		}		
		return jobNames.subList(start, count);
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#listStepExecutionsForStep(java.lang.String, java.lang.String, int, int)
	 */
	public Collection<StepExecution> listStepExecutionsForStep(String jobName, String stepName, int start, int count) throws NoSuchStepException {
		if (this.countStepExecutionsForStep(jobName, stepName) == 0) {
			throw new NoSuchStepException("No step executions exist with this step name: " + stepName);
		}
		List<StepExecution> steps = new LinkedList<StepExecution>();
		for (String name : this.jobOperator.getJobNames()) {
			if (name.contains(jobName)) {
				for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) { 
					for (JobExecution jobExecution : this.jobExplorer.getJobExecutions(jobInstance)) {
						Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
						for (StepExecution step : stepExecutions) {	
							if (step.getStepName().contains(stepName)) {
								steps.add(step);
							}
						}
					}
				}
			}
		}						
		if (start >= steps.size()) {
			start = steps.size();
		}
		if (start + count >= steps.size()) {
			count = steps.size() - start;
		}		
		return steps.subList(start, count);
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#restart(java.lang.Long)
	 */
	public JobExecution restart(Long jobExecutionId) throws NoSuchJobExecutionException, JobExecutionAlreadyRunningException, JobRestartException,
			JobInstanceAlreadyCompleteException, NoSuchJobException, JobParametersInvalidException {		
		JobExecution target = getJobExecution(jobExecutionId);
		JobInstance lastInstance = target.getJobInstance();
		Job job = this.jobRegistry.getJob(lastInstance.getJobName());
		JobExecution jobExecution = this.jobLauncher.run(job, lastInstance.getJobParameters());
		if (jobExecution.isRunning()) {
			this.activeExecutions.add(jobExecution);
		}
		return jobExecution;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#stop(java.lang.Long)
	 */
	public JobExecution stop(Long jobExecutionId) throws NoSuchJobExecutionException, JobExecutionNotRunningException {
		JobExecution jobExecution = getJobExecution(jobExecutionId);
		if (!jobExecution.isRunning()) {
			throw new JobExecutionNotRunningException("JobExecution is not running and therefore cannot be stopped");
		}
		LOGGER.info("Stopping job execution: " + jobExecution);
		jobExecution.stop();
		jobRepository.update(jobExecution);
		return jobExecution;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.batch.admin.service.JobService#stopAll()
	 */
	public int stopAll() {
		List<JobExecution> allExecutions = new LinkedList<JobExecution>();
		for (String jobName : this.jobOperator.getJobNames()) {
			for (JobInstance jobInstance : this.jobExplorer.getJobInstances(jobName, 0, MAX_EXECUTIONS)) { 
				for (JobExecution jobExecution : this.jobExplorer.getJobExecutions(jobInstance)) {
					if (jobExecution.isRunning()) {
						allExecutions.add(jobExecution);
					}
				}
			}
		}				
		for (JobExecution jobExecution : allExecutions) {
			jobExecution.stop();
			this.jobRepository.update(jobExecution);
		}
		return allExecutions.size();
	}
	
	/** Getter/Setter methods*/
	/**
	 * Timeout for shutdown waiting for jobs to finish processing.
	 * @param shutdownTimeout in milliseconds (default 60 secs)
	 */
	public void setShutdownTimeout(int shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}	
	/** End getter/setter methods*/

}
