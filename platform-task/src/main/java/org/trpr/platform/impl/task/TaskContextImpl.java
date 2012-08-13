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

package org.trpr.platform.impl.task;

import java.util.LinkedHashMap;
import java.util.Map;

import org.trpr.platform.core.spi.event.PlatformEventProducer;
import org.trpr.platform.model.common.BusinessEntity;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.spi.task.TaskContext;
import org.trpr.platform.spi.task.TaskResult;

/**
 * The <code>TaskContextImpl</code> is the default implementation for TaskContext. 
 * This implementation stores the task execution results in an in-memory collection. This class also publishes events published to it using
 * the {@link PlatformEventProducer} instance set on it.
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */
public class TaskContextImpl <T extends TaskResult<BusinessEntity>, S extends PlatformEvent> implements TaskContext<T,S> {

	/** Collection for holding the task execution results*/
	private Map<String, T> results = new LinkedHashMap<String, T>();
	
	/** The PlatformEventProducer to use for event publishing*/
	private PlatformEventProducer eventProducer;

	/**
	 * Interface method implementation. Returns the task result for the task with the specified task ID.
	 * @see org.trpr.platform.spi.task.TaskContext#getTaskResult(java.lang.String)
	 */
	public T getTaskResult(String taskId) {
		return this.results.get(taskId);
	}
	
	/**
	 * Adds the specified TaskResult against the task Id specified.
	 * @param taskId Id of the Task that was executed
	 * @param taskResult the TaskResult from execution of the Task identified by the specified Id
	 */
	public void addResult(String taskId, T taskResult) {
		this.results.put(taskId, taskResult);
	}

	/**
	 * Interface method implementation. Publishes the events using the PlatformEventProducer set on this class
	 * @see org.trpr.platform.spi.task.TaskContext#publishEvent(java.lang.String, S[])
	 */
	public void publishEvent(String endpoint, PlatformEvent... events) {
		for (PlatformEvent event : events) {
			this.getEventProducer().publishEvent(event);
		}
	}
	
	/** Setter/Getter methods*/
	public PlatformEventProducer getEventProducer() {
		return this.eventProducer;
	}
	public void setEventProducer(PlatformEventProducer eventProducer) {
		this.eventProducer = eventProducer;
	}

	/** End setter/getter methods */
}
