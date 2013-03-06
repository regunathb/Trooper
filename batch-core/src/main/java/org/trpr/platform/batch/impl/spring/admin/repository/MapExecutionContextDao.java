/*
 * Copyright 2006-2007 the original author or authors.
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

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.dao.ExecutionContextDao;
import org.springframework.batch.core.repository.dao.ExecutionContextStringSerializer;
import org.springframework.batch.core.repository.dao.XStreamExecutionContextStringSerializer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.support.transaction.TransactionAwareProxyFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * In-memory implementation of {@link ExecutionContextDao} backed by maps.
 * 
 * @author Robert Kasanicky
 * @author Dave Syer
 */
public class MapExecutionContextDao implements ExecutionContextDao{

	private Map<Long, ExecutionContext> contextsByStepExecutionId = TransactionAwareProxyFactory
			.createAppendOnlyTransactionalMap();

	private Map<Long, ExecutionContext> contextsByJobExecutionId = TransactionAwareProxyFactory
			.createAppendOnlyTransactionalMap();

	private ExecutionContextStringSerializer serializer;
	
	public MapExecutionContextDao() throws Exception {
		serializer = new XStreamExecutionContextStringSerializer();
		((XStreamExecutionContextStringSerializer) serializer).afterPropertiesSet();
	}
	
	public void clear() {
		contextsByJobExecutionId.clear();
		contextsByStepExecutionId.clear();
	}

	private ExecutionContext copy(ExecutionContext original) {
		Map<String, Object> m = new HashMap<String, Object>();
		for (java.util.Map.Entry<String, Object> me : original.entrySet()) {
			m.put(me.getKey(), me.getValue());
		}		
		ExecutionContext copy = new ExecutionContext();
		Map<String, Object> map = serializer.deserialize(serializer.serialize(m));
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}

	public ExecutionContext getExecutionContext(StepExecution stepExecution) {
		return copy(contextsByStepExecutionId.get(stepExecution.getId()));
	}

	public void updateExecutionContext(StepExecution stepExecution) {
		ExecutionContext executionContext = stepExecution.getExecutionContext();
		if (executionContext != null) {
			contextsByStepExecutionId.put(stepExecution.getId(), copy(executionContext));
		}
	}
	
	public void removeExecutionContext(StepExecution stepExecution) {
		contextsByStepExecutionId.remove(stepExecution.getId());
		
	}

	public ExecutionContext getExecutionContext(JobExecution jobExecution) {
		return copy(contextsByJobExecutionId.get(jobExecution.getId()));
	}

	public void updateExecutionContext(JobExecution jobExecution) {
		ExecutionContext executionContext = jobExecution.getExecutionContext();
		if (executionContext != null) {
			contextsByJobExecutionId.put(jobExecution.getId(), copy(executionContext));
		}
	}

	public void removeExecutionContext(JobExecution jobExecution) {
		contextsByJobExecutionId.remove(jobExecution.getId());
		//No point storing StepExecutionCOntext if jobexecutioncontext have been deleted
		for(StepExecution stepExecution : jobExecution.getStepExecutions()) {
			this.removeExecutionContext(stepExecution);
		}
	}

	public void saveExecutionContext(JobExecution jobExecution) {
		updateExecutionContext(jobExecution);
	}

	public void saveExecutionContext(StepExecution stepExecution) {
		updateExecutionContext(stepExecution);
	}

}