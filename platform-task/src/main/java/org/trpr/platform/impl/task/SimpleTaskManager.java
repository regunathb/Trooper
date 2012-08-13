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
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.spi.task.Resource;
import org.trpr.platform.spi.task.Task;
import org.trpr.platform.spi.task.TaskContext;
import org.trpr.platform.spi.task.TaskData;
import org.trpr.platform.spi.task.TaskManager;
import org.trpr.platform.spi.task.TaskResult;

/**
 * 
 * The <code>SimpleTaskManager</code> class is an implementation of {@link org.trpr.platform.spi.task.TaskManager}.
 * SimpleTaskManager executes the tasks in a sequential manner where the order of execution 
 * is the order in which they are passed. Tasks are executed in-process i.e. within the same JVM.
 *  
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */

public class SimpleTaskManager<T extends TaskData<BusinessEntity, Resource>, S extends TaskResult<BusinessEntity>,
		R extends TaskContext<S, PlatformEvent>> implements TaskManager<T,S,R> {

	/**
	 * Interface method implementation. Executes the tasks sequentially in the order passed in. Creates a TaskContextImpl prior to task execution and
	 * passes it to each task being executed. 
	 * @see org.trpr.platform.spi.task.TaskManager#execute(org.trpr.platform.spi.task.Task<T,S,R>[])
	 */
	@SuppressWarnings("unchecked")
	public R execute(Task<T,S,R>[] tasks) {
		TaskContextImpl<S,PlatformEvent> taskContext = new TaskContextImpl<S,PlatformEvent>();
		for (Task<T,S,R> t : tasks) {
			t.init((R)taskContext);
			t.run();
			taskContext.addResult(t.getTaskId(), t.getResult());
		}
		return (R)taskContext;
	}
}
