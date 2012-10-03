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

import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.spi.task.Resource;
import org.trpr.platform.spi.task.TaskData;

/**
 * The CEPTaskData is the task data that will be returned executing the CEPTask
 * 
 * @author Regunath B
 * @version 1.0, 28/09/2012
 */
@SuppressWarnings("rawtypes")
public class CEPTaskData<T extends Earthling, S extends Resource> extends TaskData {

	/**
	 * Constructor for this TaskData
	 * @param earthling the Earthling instance
	 */
	@SuppressWarnings("unchecked")
	public CEPTaskData(Earthling earthling, CEPEngineResource resource) {
		super();
		this.addEntity(earthling);		
		this.addResource(resource);
	}
}
