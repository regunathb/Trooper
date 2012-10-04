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

package org.trpr.example.service.cep;

import java.util.HashSet;

import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.impl.task.AbstractTask;
import org.trpr.platform.spi.task.TaskResult;
import org.trpr.platform.spi.task.TaskResult.TaskResultCode;

import com.espertech.esper.client.EPServiceProvider;

/**
 * The CEPTask defines the Task that will be executed by the CEPService
 * 
 * @author Regunath B
 * @version 1.0, 03/10/2012
 * 
 */
public class CEPTask<T> extends AbstractTask {
	
	/** Name of the item list variable*/
	private static final String ITEM_LIST_VAR = "itemsList"; 

	/** Logger variable */
	private static final Logger LOGGER = LogFactory.getLogger(CEPTask.class);
	
	/** Default serial version UID*/
	private static final long serialVersionUID = 1L;
	
	/** Flag to see if the list of values has already been set. Hack just so as not to set the variable values for each Task execution*/
	private static boolean isVarSet;
	
	/**
	 * Constructor for this task
	 * @param earthling the Earthling to echo
	 * @param id the Task identifier
	 */
	public CEPTask(CEPTaskData<Earthling, CEPEngineResource<T>> taskData, String id) {
		super(id);
		this.setTaskData(taskData);
	}

	/**
	 * Implementation of doExecute method. This task sends the Earthling instance to the CEP and listens to any call-backs for matching events
	 */
	protected void doExecute() {
		Earthling earthling = (Earthling) this.getTaskData().getEntityByName(Earthling.class.getName())[0];
		try {
			EPServiceProvider epProvider = ((EPServiceProvider)((CEPEngineResource)this.getTaskData().getResourceByName(CEPEngineResource.CEP_ENGINE_BEAN_NAME)[0]).getObject());
			// set the variable list values for testing. This can be a dynamically constructed list 
			if (!isVarSet) {
				HashSet<String> itemsListSet = new HashSet<String>();
				itemsListSet.add("Trooper01");
				itemsListSet.add("Trooper010");
				epProvider.getEPRuntime().setVariableValue(ITEM_LIST_VAR, itemsListSet);
				isVarSet = true;
			}
			epProvider.getEPRuntime().sendEvent(earthling);
			this.result.setResultCode(TaskResultCode.SUCCESS);
		} catch (Exception e) {
			this.result.setResultCode(TaskResultCode.FAILURE);
			LOGGER.error("Error accessing (or) invoking method on CEP engine :" + e.getMessage(), e);
		}
	}
	
}
