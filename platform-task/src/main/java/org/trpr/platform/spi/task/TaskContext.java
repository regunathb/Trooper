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
 * The <code>TaskContext</code> provides methods for a {@link Task} to communicate with its environment/container. 
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */

public interface TaskContext <T extends TaskResult<BusinessEntity>, S extends PlatformEvent> {

	/**
	 * Returns the {@link TaskResult} from prior execution of a {@link Task} identified by the specified task ID.
	 * @param taskId the task identifier
	 * @return TaskResult from execution of a Task, if available
	 */
	public T getTaskResult(String taskId);
	
	/**
	 * Publishes the specified events to the specified URI endpoints
	 * @param endpoint the URI endpoint for event publishing
	 * @param events PlatformEvent instances for publishing
	 */
	public void publishEvent(String endpoint, S... events);
	
}
