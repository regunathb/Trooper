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

import org.trpr.platform.model.common.BusinessEntity;
import org.trpr.platform.model.event.PlatformEvent;

/**
 * 
 * The <code>TaskManager</code> is an interface for executing {@link Task} instances. Results from the task executions are available through
 * the {@link TaskContext} instance. 
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */
public interface TaskManager<T extends TaskData<BusinessEntity, Resource>, S extends TaskResult<BusinessEntity>,
					R extends TaskContext<S, PlatformEvent>> {

	/**
	 * Executes the specified Task instances using the specified TaskContext as a mechanism for the tasks and this manager to interact with the underlying
	 * task framework. Sequencing the task execution or parallelizing it is implementation specific. 
	 * @param tasks the array of Task instances to execute. 
	 * @return the TaskContext that may be queried for outcome of task executions.
	 */
	public R execute(Task<T,S,R>[] tasks);
}
