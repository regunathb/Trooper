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
package org.trpr.platform.batch.impl.spring.admin.repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Entity;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * A modification of @see {org.springframework.batch.core.repository.dao.MapStepExecutionDao}, to improve 
 * efficiency by not doing a full serialization and deserialization while copying.
 * 
 * @author devashishshankar
 * @version 1.0, 5th March, 2013
 */
public class MapStepExecutionDao extends org.springframework.batch.core.repository.dao.MapStepExecutionDao {

	private Map<Long, Map<Long, StepExecution>> executionsByJobExecutionId = new ConcurrentHashMap<Long, Map<Long,StepExecution>>();

	private Map<Long, StepExecution> executionsByStepExecutionId = new ConcurrentHashMap<Long, StepExecution>();

	private AtomicLong currentId = new AtomicLong();

	public void clear() {
		executionsByJobExecutionId.clear();
		executionsByStepExecutionId.clear();
	}

	/**
	 * Returns a copy of {@link StepExecution}, by adding new Objects for every field(no references are passed)
	 * 
	 * @param original StepExecution to be copied
	 * @return StepExecution copy
	 */
	private static StepExecution copy(StepExecution original) {
		StepExecution copy = new StepExecution(original.getStepName(), original.getJobExecution());
		copy.setCommitCount(original.getCommitCount());
		if(original.getEndTime()!=null) {
			copy.setEndTime((Date) original.getEndTime().clone());
		}
		//Warning: no deep copy
		if(original.getExitStatus()!=null) {
			copy.setExitStatus(new ExitStatus(original.getExitStatus().getExitCode(), original.getExitStatus().getExitDescription()));
		}
		copy.setFilterCount(original.getFilterCount());
		copy.setId(original.getId());
		if(original.getLastUpdated()!=null) {
			copy.setLastUpdated((Date) original.getLastUpdated().clone());
		}
		copy.setProcessSkipCount(original.getProcessSkipCount());
		copy.setReadCount(original.getReadCount());
		copy.setReadSkipCount(original.getReadSkipCount());
		copy.setRollbackCount(original.getRollbackCount());
		if(original.getStartTime()!=null) {
			copy.setStartTime((Date)original.getStartTime().clone());
		}
		if(original.getStatus()!=null) {
			copy.setStatus(BatchStatus.valueOf(original.getStatus().name()));
		}
		if(original.isTerminateOnly()) {
			copy.setTerminateOnly();
		}
		copy.setVersion(original.getVersion());
		copy.setWriteCount(original.getWriteCount());
		copy.setWriteSkipCount(original.getWriteSkipCount());
		return copy;
	}

	private static void copy(final StepExecution sourceExecution, final StepExecution targetExecution) {
		// Cheaper than full serialization is a reflective field copy, which is
		// fine for volatile storage
		ReflectionUtils.doWithFields(StepExecution.class, new ReflectionUtils.FieldCallback() {
			@Override
			public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
				field.setAccessible(true);
				field.set(targetExecution, field.get(sourceExecution));
			}
		});
	}
	
	@Override
	public void saveStepExecution(StepExecution stepExecution) {

		Assert.isTrue(stepExecution.getId() == null);
		Assert.isTrue(stepExecution.getVersion() == null);
		Assert.notNull(stepExecution.getJobExecutionId(), "JobExecution must be saved already.");
		
		Long jobExecutionID = stepExecution.getJobExecutionId();

		Map<Long, StepExecution> executions = executionsByJobExecutionId.get(stepExecution.getJobExecutionId());
		if (executions == null) {
			executions = new ConcurrentHashMap<Long, StepExecution>();
			executionsByJobExecutionId.put(stepExecution.getJobExecutionId(), executions);
		}

		stepExecution.setId(currentId.incrementAndGet());
		stepExecution.incrementVersion();
		StepExecution copy = copy(stepExecution);
		executions.put(stepExecution.getId(), copy);
		executionsByStepExecutionId.put(stepExecution.getId(), copy);

	}

	@Override
	public void updateStepExecution(StepExecution stepExecution) {

		Assert.notNull(stepExecution.getJobExecutionId());
		
		//If the job execution data doesn't exist, can't update	
		if(!executionsByJobExecutionId.containsKey(stepExecution.getJobExecutionId())) {
			return;
		}

		Map<Long, StepExecution> executions = executionsByJobExecutionId.get(stepExecution.getJobExecutionId());
		Assert.notNull(executions, "step executions for given job execution are expected to be already saved");

		final StepExecution persistedExecution = executionsByStepExecutionId.get(stepExecution.getId());
		Assert.notNull(persistedExecution, "step execution is expected to be already saved");

		synchronized (stepExecution) {
			if (!persistedExecution.getVersion().equals(stepExecution.getVersion())) {
				throw new OptimisticLockingFailureException("Attempt to update step execution id="
						+ stepExecution.getId() + " with wrong version (" + stepExecution.getVersion()
						+ "), where current version is " + persistedExecution.getVersion());
			}

			stepExecution.incrementVersion();
			StepExecution copy = new StepExecution(stepExecution.getStepName(), stepExecution.getJobExecution());
			copy(stepExecution, copy);
			executions.put(stepExecution.getId(), copy);
			executionsByStepExecutionId.put(stepExecution.getId(), copy);
		}
	}

	@Override
	public StepExecution getStepExecution(JobExecution jobExecution, Long stepExecutionId) {
		return executionsByStepExecutionId.get(stepExecutionId);
	}

	@Override
	public void addStepExecutions(JobExecution jobExecution) {
		Map<Long, StepExecution> executions = executionsByJobExecutionId.get(jobExecution.getId());
		if (executions == null || executions.isEmpty()) {
			return;
		}
		List<StepExecution> result = new ArrayList<StepExecution>(executions.values());
		Collections.sort(result, new Comparator<Entity>() {

			@Override
			public int compare(Entity o1, Entity o2) {
				return Long.signum(o2.getId() - o1.getId());
			}
		});

		List<StepExecution> copy = new ArrayList<StepExecution>(result.size());
		for (StepExecution exec : result) {
			copy.add(copy(exec));
		}
		jobExecution.addStepExecutions(copy);
	}

	/**
	 * Removes all the stepExecutions from the DAO
	 * @param jobExecution JobExecution whose StepExecutions have to be deleted
	 */
	public void removeStepExecutions(JobExecution jobExecution) {
		Map<Long, StepExecution> executions = executionsByJobExecutionId.get(jobExecution.getId());
		if (executions == null || executions.isEmpty()) {
			return;
		}
		for(StepExecution step: executions.values()) {
			executionsByStepExecutionId.remove(step.getId());
		}
		executionsByJobExecutionId.remove(jobExecution.getId());
	}
}
