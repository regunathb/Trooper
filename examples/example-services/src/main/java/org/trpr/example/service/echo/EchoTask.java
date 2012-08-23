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

package org.trpr.example.service.echo;

import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.impl.task.AbstractTask;
import org.trpr.platform.spi.task.Resource;

/**
 * The EchoTask defines the Task that will be executed by the EchoService
 * 
 * @author Regunath B
 * @version 1.0, 23/08/2012
 * 
 */
public class EchoTask extends AbstractTask {

	/** Logger variable */
	private static final Logger LOGGER = LogFactory.getLogger(EchoTask.class);
	
	/** Default serial version UID*/
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for this task
	 * @param earthling the Earthling to echo
	 * @param id the Task identifier
	 */
	public EchoTask(EchoTaskData<Earthling, Resource> taskData, String id) {
		super(id);
		this.setTaskData(taskData);
	}

	/**
	 * Implementation of doExecute method. This task just returns the person object back
	 */
	protected void doExecute() {
		Earthling earthling = (Earthling) this.getTaskData().getEntityByName(Earthling.class.getName())[0];
		LOGGER.info("Echo " + earthling.getFirstName() + " " + earthling.getLastName() + " " + earthling.getDateOfBirth().getTime());
	}
	
}
