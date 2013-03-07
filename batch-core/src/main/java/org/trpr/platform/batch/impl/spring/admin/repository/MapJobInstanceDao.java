/*
 * Copyright 2006-2013 the original author or authors.
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

package org.trpr.platform.batch.impl.spring.admin.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.util.Assert;
import org.trpr.platform.batch.impl.spring.admin.SimpleJobConfigurationService;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * Trooper implementation of {@link org.springframework.batch.core.repository.dao.MapJobInstanceDao}.
 * Has a maxJobinstance count. When the count is exceeded, removes the oldest JobInstance, including all
 * its executions, steps and executionContexts.
 */
public class MapJobInstanceDao implements JobInstanceDao {

	private Queue<JobInstance> jobInstances = new ConcurrentLinkedQueue<JobInstance>();

	private long currentId = 0;
	
	private int maxJobInstanceCount;

	private MapJobExecutionDao jobExecutionDao;

	private MapStepExecutionDao stepExecutionDao;

	private MapExecutionContextDao executionContextDao;

	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(MapJobInstanceDao.class);
	
	public MapJobInstanceDao(int maxExecutionCount) {
		this.maxJobInstanceCount = maxExecutionCount;
	}

	public void clear() {
		jobInstances.clear();
	}

	public JobInstance createJobInstance(String jobName, JobParameters jobParameters) {

		Assert.state(getJobInstance(jobName, jobParameters) == null, "JobInstance must not already exist");

		JobInstance jobInstance = new JobInstance(currentId++, jobParameters, jobName);
		jobInstance.incrementVersion();
		//Removes the older jobInstances
		if(this.jobInstances.size()>=maxJobInstanceCount) {
			JobInstance toRemove = this.jobInstances.remove();
			LOGGER.info("Removing jobInstance: "+toRemove.toString());
			List<JobExecution> executions = this.jobExecutionDao.findJobExecutions(toRemove);
			for(JobExecution execution : executions) {
				//Remove job executions
				LOGGER.info("Removing JobExecution: "+execution.toString());
				this.jobExecutionDao.removeExecution(execution.getId());
				this.stepExecutionDao.removeStepExecutions(execution);
				//Remove execution contexts
				this.executionContextDao.removeExecutionContext(execution);
			}
		}
		jobInstances.add(jobInstance);
		return jobInstance;
	}

	public JobInstance getJobInstance(String jobName, JobParameters jobParameters) {

		for (JobInstance instance : jobInstances) {
			if (instance.getJobName().equals(jobName) && instance.getJobParameters().equals(jobParameters)) {
				return instance;
			}
		}
		return null;

	}

	public JobInstance getJobInstance(Long instanceId) {
		for (JobInstance instance : jobInstances) {
			if (instance.getId().equals(instanceId)) {
				return instance;
			}
		}
		return null;
	}

	public List<String> getJobNames() {
		List<String> result = new ArrayList<String>();
		for (JobInstance instance : jobInstances) {
			result.add(instance.getJobName());
		}
		Collections.sort(result);
		return result;
	}

	public List<JobInstance> getJobInstances(String jobName, int start, int count) {
		List<JobInstance> result = new ArrayList<JobInstance>();
		for (JobInstance instance : jobInstances) {
			if (instance.getJobName().equals(jobName)) {
				result.add(instance);
			}
		}
		Collections.sort(result, new Comparator<JobInstance>() {
			// sort by ID descending
			public int compare(JobInstance o1, JobInstance o2) {
				return Long.signum(o2.getId() - o1.getId());
			}
		});

		int startIndex = Math.min(start, result.size());
		int endIndex = Math.min(start + count, result.size());
		return result.subList(startIndex, endIndex);
	}

	public JobInstance getJobInstance(JobExecution jobExecution) {
		return jobExecution.getJobInstance();
	}
	
	public MapJobExecutionDao getJobExecutionDao() {
		return jobExecutionDao;
	}

	public void setJobExecutionDao(MapJobExecutionDao jobExecutionDao) {
		this.jobExecutionDao = jobExecutionDao;
	}

	public MapStepExecutionDao getStepExecutionDao() {
		return stepExecutionDao;
	}

	public void setStepExecutionDao(MapStepExecutionDao stepExecutionDao) {
		this.stepExecutionDao = stepExecutionDao;
	}

	public MapExecutionContextDao getExecutionContextDao() {
		return executionContextDao;
	}

	public void setExecutionContextDao(MapExecutionContextDao executionContextDao) {
		this.executionContextDao = executionContextDao;
	}
}