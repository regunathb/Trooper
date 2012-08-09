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
package org.trpr.platform.spi.task;

import java.io.Serializable;

import org.trpr.platform.model.common.BusinessEntity;
import org.trpr.platform.model.event.PlatformEvent;

/**
 * The <code>Task</code> defines a unit of work that may be executed in a suitable environment/container. A Task is an implementation of the
 * Command Pattern {@linkplain http://en.wikipedia.org/wiki/Command_pattern} where {@link TaskData} contains all information required to execute the Task.
 * The environment/container must therefore support instantiation/reinstatement of {@link Resource} and {@link BusinessEntity} minimally when a Task
 * is executed on a network i.e. instantiated on one processing node but actually executes elsewhere.
 * 
 * The Task is a {@link Runnable} to indicate that multiple instances may be executed in parallel, if relevant and required. The {@link Runnable#run()} method
 * is invoked to signal execution of this Task.
 * 
 * It is recommended that all implementations of this interface are thread-safe, as instances are expected to maintain state for implementing call-back methods
 * like {@link #setTaskData(TaskData)} and {@link #getResult()}. Task instantiation is therefore always prototype and never singleton. 
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */

public interface Task <T extends TaskData<BusinessEntity, Resource>, S extends TaskResult<BusinessEntity>,
				R extends TaskContext<S, PlatformEvent>> extends Runnable, Serializable {
	
	/**
	 * Initialize this Task with the specified TaskContext
	 * @param context the TaskContext under which this Task executes
	 */
	public void init(R context);

	/** 
	 * Gets the task identifier for this Task
	 * @return String task identifier
	 */
	public String getTaskId();
	
	/**
	 * Sets the TaskData for this Task
	 * @param data the TaskData
	 */
	public void setTaskData(T data);
	
	/** 
	 * Gets the result of execution for this Task
	 * @return the task execution results
	 */
	public S getResult();
	
}
