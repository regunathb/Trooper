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

package org.trpr.example.service.greeting;

import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.impl.task.AbstractTask;
import org.trpr.platform.spi.task.Resource;

/**
 * The GreetingTask defines the Task to be executed by the GeetingService
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 * 
 */
public class GreetingTask extends AbstractTask {
	
	/** Default serial version UID*/
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for this task
	 * @param earthling the Earthling to greet
	 * @param id the Task identifier
	 */
	public GreetingTask(GreetingTaskData<Earthling, Resource> taskData, String id) {
		super(id);
		this.setTaskData(taskData);
	}

	/**
	 * Implementation of doExecute method. Work is delegated to the {@link GreetingValidationStrategy}
	 */
	protected void doExecute() {
		Earthling earthling = (Earthling) this.data.getEntityByName(Earthling.class.getName())[0];
		GreetingValidationStrategy greetingValidationStrategy = new GreetingValidationStrategy(earthling);
		this.result.setExecutionSummary(greetingValidationStrategy.validate());	
	}
	
}
