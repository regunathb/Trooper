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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.util.Assert;

/**
 * Trooper implementation of {@link org.springframework.batch.core.repository.dao.MapJobExecutionDao}
 * Faster implementation that doesn't rely on serialisation for deep copy. Added an ability to remove 
 * JobExecutions
 * 
 * @author devashishshankar
 * @version 1.0, 5th March, 2013
 */
public class MapJobExecutionDao implements JobExecutionDao {

	// JDK6 Make this into a ConcurrentSkipListMap: adds and removes tend to be very near the front or back
	private final ConcurrentMap<Long, JobExecution> executionsById = new ConcurrentHashMap<Long, JobExecution>();

	private final AtomicLong currentId = new AtomicLong(0L);
	
	public void clear() {
		executionsById.clear();
	}

	/** Method for adding a new Execution to the DAO **/
	private void addNewExecution(Long ID, JobExecution jobExecution) {
		executionsById.put(ID, jobExecution);
	}
	
	/**
	 * Returns a copy of {@link JobExecution}, by adding new Objects for every field(no references are passed)
	 * 
	 * @param original JobExecution to be copied
	 * @return JobExecution copy
	 */
	private static JobExecution copy(JobExecution original) {
		JobInstance jobInstance = original.getJobInstance();
		JobExecution copy;
		if(jobInstance==null) {
			copy = new JobExecution(original.getId());			
		}
		copy = new JobExecution(jobInstance, original.getId());
		if(original.getStartTime()!=null) {
			copy.setStartTime((Date) original.getStartTime().clone());		
		}
		if(original.getEndTime()!=null) {
			copy.setEndTime((Date) original.getEndTime().clone());	
		}
		if(original.getStatus()!=null) {
			copy.setStatus(BatchStatus.valueOf(original.getStatus().name()));
		}
		if(original.getExitStatus()!=null) {
			copy.setExitStatus(new ExitStatus(original.getExitStatus().getExitCode(), original.getExitStatus().getExitDescription()));
		}
		if(original.getCreateTime()!=null) {
			copy.setCreateTime((Date) original.getCreateTime().clone());
		}
		if(original.getLastUpdated()!=null) {
			copy.setLastUpdated((Date) original.getLastUpdated().clone());
		}
		copy.setVersion(original.getVersion());
		return copy;
	}

	@Override
	public void saveJobExecution(JobExecution jobExecution) {
		Assert.isTrue(jobExecution.getId() == null);
		Long newId = currentId.getAndIncrement();
		jobExecution.setId(newId);
		jobExecution.incrementVersion();
		this.addNewExecution(newId, copy(jobExecution));
	}

	@Override
	public List<JobExecution> findJobExecutions(JobInstance jobInstance) {
		List<JobExecution> executions = new ArrayList<JobExecution>();
		for (JobExecution exec : executionsById.values()) {
			if (exec.getJobInstance().equals(jobInstance)) {
				executions.add(copy(exec));
			}
		}
		Collections.sort(executions, new Comparator<JobExecution>() {
			// sort by descending order of ID
			public int compare(JobExecution e1, JobExecution e2) {
				return Long.signum(e2.getId() - e1.getId());
			}
		});
		return executions;
	}

	@Override
	public void updateJobExecution(JobExecution jobExecution) {
		Long id = jobExecution.getId();
		Assert.notNull(id, "JobExecution is expected to have an id (should be saved already)");
		JobExecution persistedExecution = executionsById.get(id);
		Assert.notNull(persistedExecution, "JobExecution must already be saved");

		synchronized (jobExecution) {
			if (!persistedExecution.getVersion().equals(jobExecution.getVersion())) {
				throw new OptimisticLockingFailureException("Attempt to update step execution id=" + id
						+ " with wrong version (" + jobExecution.getVersion() + "), where current version is "
						+ persistedExecution.getVersion());
			}
			jobExecution.incrementVersion();
			this.addNewExecution(id, copy(jobExecution));
		}
	}

	@Override
	public JobExecution getLastJobExecution(JobInstance jobInstance) {
		JobExecution lastExec = null;
		for (JobExecution exec : executionsById.values()) {
			if (!exec.getJobInstance().equals(jobInstance)) {
				continue;
			}
			if (lastExec == null) {
				lastExec = exec;
			}
			if (lastExec.getCreateTime().before(exec.getCreateTime())) {
				lastExec = exec;
			}
		}
		return copy(lastExec);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seeorg.springframework.batch.core.repository.dao.JobExecutionDao#
	 * findRunningJobExecutions(java.lang.String)
	 */
	@Override
	public Set<JobExecution> findRunningJobExecutions(String jobName) {
		Set<JobExecution> result = new HashSet<JobExecution>();
		for (JobExecution exec : executionsById.values()) {
			if (!exec.getJobInstance().getJobName().equals(jobName) || !exec.isRunning()) {
				continue;
			}
			result.add(copy(exec));
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.batch.core.repository.dao.JobExecutionDao#getJobExecution
	 * (java.lang.Long)
	 */
	@Override
	public JobExecution getJobExecution(Long executionId) {
		return copy(executionsById.get(executionId));
	}

	@Override
	public void synchronizeStatus(JobExecution jobExecution) {
		JobExecution saved = getJobExecution(jobExecution.getId());
		if (saved.getVersion().intValue() != jobExecution.getVersion().intValue()) {
			jobExecution.upgradeStatus(saved.getStatus());
			jobExecution.setVersion(saved.getVersion());
		}
	}

	/**
	 * Method removes an execution from the DAO
	 * @param ID ID of the execution to be deleted
	 */
	public void removeExecution(Long ID) {
		executionsById.remove(ID);
	}
	
}