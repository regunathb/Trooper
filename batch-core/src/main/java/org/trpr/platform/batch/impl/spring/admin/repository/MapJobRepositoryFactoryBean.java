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

import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.JobExecutionDao;
import org.springframework.batch.core.repository.dao.JobInstanceDao;
import org.springframework.batch.core.repository.dao.StepExecutionDao;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * A {@link FactoryBean} that automates the creation of a {@link SimpleJobRepository} using non-persistent in-memory 
 * DAO implementations. Based on {@link org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean} 
 * Modified to include {@link MapStepExecutionDao}, {@link MapJobInstanceDao}, {@link MapJobExecutionDao}, 
 * {@link MapExecutionContextDao} which don't do a deep copy of steps, making this implementation faster. 
 * Also this implementation has an ability to limit the number of JobInstances being stored.
 * 
 * @author devashishshankar
 * @version 1.0, 5th March, 2013
 */
public class MapJobRepositoryFactoryBean extends org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean {

	private MapJobExecutionDao jobExecutionDao;

	private MapJobInstanceDao jobInstanceDao;

	private MapStepExecutionDao stepExecutionDao;

	private MapExecutionContextDao executionContextDao;

	private static int DEFAULT_MAX_COUNT = 2000;

	private int maxJobInstanceCount = DEFAULT_MAX_COUNT;

	/**
	 * Create a new instance with a {@link ResourcelessTransactionManager}.
	 */
	public MapJobRepositoryFactoryBean() {
		this(new ResourcelessTransactionManager());
	}

	/**
	 * Create a new instance with the provided transaction manager.
	 * 
	 * @param transactionManager
	 */
	public MapJobRepositoryFactoryBean(PlatformTransactionManager transactionManager) {
		setTransactionManager(transactionManager);
	}

	/**
	 * Convenience method to clear all the map daos globally, removing all
	 * entities.
	 */
	public void clear() {
		jobInstanceDao.clear();
		jobExecutionDao.clear();
		stepExecutionDao.clear();
		executionContextDao.clear();
	}

	@Override
	protected JobExecutionDao createJobExecutionDao() throws Exception {
		jobExecutionDao = new MapJobExecutionDao();
		if(this.jobInstanceDao!=null) {
			this.jobInstanceDao.setJobExecutionDao(this.jobExecutionDao);
		}
		return jobExecutionDao;
	}

	@Override
	protected JobInstanceDao createJobInstanceDao() throws Exception {
		jobInstanceDao = new MapJobInstanceDao(this.maxJobInstanceCount);
		this.jobInstanceDao.setExecutionContextDao(executionContextDao);
		this.jobInstanceDao.setStepExecutionDao(stepExecutionDao);
		this.jobInstanceDao.setJobExecutionDao(this.jobExecutionDao);
		return jobInstanceDao;
	}

	@Override
	protected StepExecutionDao createStepExecutionDao() throws Exception {
		stepExecutionDao = new MapStepExecutionDao();
		if(this.jobInstanceDao!=null) {
			this.jobInstanceDao.setStepExecutionDao(stepExecutionDao);
		}
		return stepExecutionDao;
	}

	@Override
	protected ExecutionContextDao createExecutionContextDao() throws Exception {
		executionContextDao = new MapExecutionContextDao();
		if(this.jobInstanceDao!=null) {
			this.jobInstanceDao.setExecutionContextDao(executionContextDao);
		}
		return executionContextDao;
	}

	/** Getter/Setter methods **/
	public JobExecutionDao getJobExecutionDao() {
		return jobExecutionDao;
	}

	public JobInstanceDao getJobInstanceDao() {
		return jobInstanceDao;
	}

	public StepExecutionDao getStepExecutionDao() {
		return stepExecutionDao;
	}

	public ExecutionContextDao getExecutionContextDao() {
		return executionContextDao;
	}

	public int getMaxCount() {
		return maxJobInstanceCount;
	}

	public void setMaxCount(int maxCount) {
		this.maxJobInstanceCount = maxCount;
	}
}