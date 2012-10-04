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
import org.trpr.example.model.service.cepservice.CEPServiceRequest;
import org.trpr.example.model.service.cepservice.CEPServiceResponse;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.AbstractServiceImpl;
import org.trpr.platform.servicefw.impl.ServiceResponseImpl;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;
import org.trpr.platform.spi.task.Resource;
import org.trpr.platform.spi.task.TaskContext;
import org.trpr.platform.spi.task.TaskResult;
import org.trpr.platform.spi.task.TaskResult.TaskResultCode;

/**
 * The <code>CEPService</code> receives Earthling instances and sends it to the CEP engine for processing.
 * @author Regunath B
 * @version 1.0, 03/10/2012
 */
public class CEPService extends AbstractServiceImpl<CEPServiceRequest, CEPServiceResponse> {
	
	/**
	 * String constant defining the CEP Task ID
	 */
	private static final String CEP_TASK_ID = "ceptask";
	
	/** The CEPEngineResource i.e. CEP engine resource */
	private CEPEngineResource cepEngineResource;
	
	/**
	 * Overriden super class method.
	 * @see AbstractServiceImpl#getAllTasks(ServiceRequest)
	 */
	protected CEPTask[] getAllTasks(ServiceRequest<CEPServiceRequest> request) {
		CEPTask cepTask = new CEPTask(new CEPTaskData<Earthling, Resource>(request.getRequestData().getEarthling(), this.cepEngineResource), CEP_TASK_ID);
		return new CEPTask[]{cepTask};		
	}

	/**
	 * Overridden method that prepares response based on the result of the execution of the tasks
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected ServiceResponse<CEPServiceResponse> prepareServiceResponse(
			TaskContext taskContext,
			ServiceRequest<CEPServiceRequest> serviceRequest) {
		
		TaskResult<Earthling> result = taskContext.getTaskResult(CEP_TASK_ID);		
		ServiceResponseImpl serviceResponse = new ServiceResponseImpl(result.getResultCode().equals(TaskResultCode.SUCCESS) ? 
				String.valueOf(ServiceFrameworkConstants.SUCCESS_STATUS_CODE):
					String.valueOf(ServiceFrameworkConstants.FAILURE_STATUS_CODE));
		
		serviceResponse.setResponseData(new CEPServiceResponse());
		return serviceResponse;		
	}

	/** Getter/Setter methods*/
	public CEPEngineResource getCepEngineResource() {
		return this.cepEngineResource;
	}
	public void setCepEngineResource(CEPEngineResource cepEngineResource) {
		this.cepEngineResource = cepEngineResource;
	}
	/** End Getter/Setter methods*/

}
