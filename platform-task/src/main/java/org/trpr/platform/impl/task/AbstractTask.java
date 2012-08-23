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

import org.trpr.platform.model.common.BusinessEntity;
import org.trpr.platform.spi.task.Task;
import org.trpr.platform.spi.task.TaskContext;
import org.trpr.platform.spi.task.TaskData;
import org.trpr.platform.spi.task.TaskResult;

/**
 * The <code>AbstractTask</code> is an implementation of the {@link Task} interface.
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */

@SuppressWarnings("rawtypes")
public abstract class AbstractTask implements Task {

	/** The default serial version UID */
	private static final long serialVersionUID = 1L;
	
	/** Member variables*/
	protected TaskContextImpl context;
	protected TaskData data;
	protected TaskResult result;
	protected String taskId;
	
	/**
	 * Constructor for this class
	 * @param taskId the task Identifier
	 */
	public AbstractTask(String taskId) {
		this.taskId = taskId;
		this.result = new TaskResult<BusinessEntity>();
	}
	
	/**
	 * Interface method implementation. Initializes this Task
	 * @see Task#init(TaskContext)
	 */
	public void init(TaskContext context) {
		this.context = (TaskContextImpl)context;
	}

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.spi.task.Task#getTaskId()
	 */
	public String getTaskId() {
		return this.taskId;
	}

	/**
	 * Interface method implementation
	 * @see Task#setTaskData(TaskData)
	 */
	public void setTaskData(TaskData data) {
		this.data = data;
	}
	
	/**
	 * Returns the TaskData for this Task
	 * @return the TaskData set on this Task
	 */
	public TaskData getTaskData() {
		return this.data;
	}

	/**
	 * Interface method implementation. Executes this Task
	 * @see java.lang.Runnable#run()
	 */
	@SuppressWarnings("unchecked")
	public void run() {
		this.doExecute();
		this.context.addResult(this.getTaskId(), this.result);
	}

	/**
	 * Interface method implementation. Returns the results from execution of this Task
	 * @see org.trpr.platform.spi.task.Task#getResult()
	 */
	public TaskResult getResult() {
		return this.result;
	}
	
	/**
	 * Task execution template method. Task execution logic is implemented here.
	 * Sub-types use the TaskContext, TaskData and TaskResult instances held by this type
	 */
	protected abstract void doExecute();

}
