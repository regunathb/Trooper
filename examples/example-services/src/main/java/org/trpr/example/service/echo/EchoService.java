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
import org.trpr.example.model.service.echoservice.EchoServiceRequest;
import org.trpr.example.model.service.echoservice.EchoServiceResponse;
import org.trpr.platform.service.model.common.status.Status;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.AbstractServiceImpl;
import org.trpr.platform.servicefw.impl.ServiceResponseImpl;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;
import org.trpr.platform.spi.task.Resource;
import org.trpr.platform.spi.task.TaskContext;

/**
 * The <code>EchoService</code> is a simple echo service of the Earthling entity
 * @author Regunath B
 * @version 1.0, 23/08/2012
 */
public class EchoService extends AbstractServiceImpl<EchoServiceRequest, EchoServiceResponse> {
	
	/**
	 * String constant defining the Echo Task ID
	 */
	private static final String ECHO_TASK_ID = "echotask";
	
	/**
	 * Overriden super class method.
	 * @see AbstractServiceImpl#getAllTasks(ServiceRequest)
	 */
	protected EchoTask[] getAllTasks(ServiceRequest<EchoServiceRequest> request) {
		EchoTask echoTask = new EchoTask(new EchoTaskData<Earthling, Resource>(request.getRequestData().getEarthling()), ECHO_TASK_ID);
		return new EchoTask[]{echoTask};		
	}

	/**
	 * Overridden method that prepares response based on the result of the execution of the tasks
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected ServiceResponse prepareServiceResponse(
			TaskContext taskContext,
			ServiceRequest serviceRequest) {
		EchoServiceRequest echoServiceRequest = (EchoServiceRequest) serviceRequest.getRequestData();
	    EchoServiceResponse echoServiceResponse = new EchoServiceResponse();				
	    Earthling earthling = (Earthling)echoServiceRequest.getEarthling();

	    // This service completely ignores what the EchoTask does or returns. This is because the task simply echoes the request data and nothing can go wrong there
		String echo = "Echo " + earthling.getFirstName() + " " + earthling.getLastName() + " " + earthling.getDateOfBirth().getTime();
		echoServiceResponse.setEcho(echo);		
		populateStatus(true, echoServiceResponse);
					
		ServiceResponseImpl serviceResponse = new ServiceResponseImpl(String.valueOf(ServiceFrameworkConstants.SUCCESS_STATUS_CODE));
		serviceResponse.setResponseData(echoServiceResponse);
		return serviceResponse;		
	}
	
	/**
	 * Helper method to populate status in response 
	 */
	private void populateStatus(boolean success, EchoServiceResponse echoPersonServiceResponse) {
		Status status = new Status();
		status.setCode(success ? ServiceFrameworkConstants.SUCCESS_STATUS_CODE : ServiceFrameworkConstants.FAILURE_STATUS_CODE);
		status.setMessage(success ? ServiceFrameworkConstants.SUCCESS_STATUS_MESSAGE : ServiceFrameworkConstants.FAILURE_STATUS_MESSAGE);
		echoPersonServiceResponse.setStatus(status);		
	}


}
