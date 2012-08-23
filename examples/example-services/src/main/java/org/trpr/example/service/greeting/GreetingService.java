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
import org.trpr.example.model.service.greetingservice.GreetingServiceRequest;
import org.trpr.example.model.service.greetingservice.GreetingServiceResponse;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.service.model.common.status.Status;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.AbstractServiceImpl;
import org.trpr.platform.servicefw.impl.ServiceResponseImpl;
import org.trpr.platform.servicefw.impl.validation.ValidationServiceResponseGenerator;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;
import org.trpr.platform.spi.task.Resource;
import org.trpr.platform.spi.task.TaskContext;
import org.trpr.platform.spi.task.TaskResult;
import org.trpr.platform.spi.validation.ValidationSummary;

/**
 * The <code>GreetingService</code> provides a reference implementation for  
 * services.
 * @author Regunath B
 */
public class GreetingService extends AbstractServiceImpl<GreetingServiceRequest, GreetingServiceResponse> {

	/**
	 * String constant defining the Greeting Task ID
	 */
	private static final String GREETING_TASK_ID = "greetingtask";

	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(GreetingService.class);
	
	/**
	 *No args Constructor 
	 */
	public GreetingService() {
		super();
	}	

	/**
	 * Overridden method that returns an array of Tasks to be executed by GreetingService
	 */
	protected GreetingTask[] getAllTasks(ServiceRequest<GreetingServiceRequest> request) {		
		GreetingTask greetingTask = new GreetingTask(new GreetingTaskData<Earthling, Resource>(request.getRequestData().getEarthling()), GREETING_TASK_ID);
		return new GreetingTask[]{greetingTask};		
	}

	/**
	 * Overridden method that prepares response based on the result of the execution of the tasks
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected ServiceResponse<GreetingServiceResponse> prepareServiceResponse(TaskContext taskContext, ServiceRequest<GreetingServiceRequest> serviceRequest) {
		GreetingServiceRequest greetingServiceRequest = serviceRequest.getRequestData();
	    GreetingServiceResponse greetingServiceResponse = new GreetingServiceResponse();				
	    Earthling earthling = greetingServiceRequest.getEarthling();	    
	    
	    TaskResult<Earthling> result = taskContext.getTaskResult(GREETING_TASK_ID);
	    ValidationSummary validationSummary = (ValidationSummary)result.getExecutionSummary();	    
	    
	    if (validationSummary.hasValidationErrors()) {
			LOGGER.info("**** GreetingService execution : " + ServiceFrameworkConstants.FAILURE_STATUS_MESSAGE + " ****");
			ValidationServiceResponseGenerator<GreetingServiceRequest, GreetingServiceResponse> responseGenerator = 
					new ValidationServiceResponseGenerator<GreetingServiceRequest, GreetingServiceResponse>(earthling);
			return responseGenerator.populateResponseFromValidationResults(validationSummary, greetingServiceResponse, greetingServiceRequest);
		}	   

	    // Greet the earthling
		String header = "Hello" + ": " + earthling.getFirstName() + " " + earthling.getLastName() + "; Your date of birth is " + earthling.getDateOfBirth().getTime();
		greetingServiceResponse.setHeader(header);
		LOGGER.debug("**** GreetingService execution : " + ServiceFrameworkConstants.SUCCESS_STATUS_MESSAGE + " ****");
		LOGGER.info(header);
			
		populateStatus(true, greetingServiceResponse);
							
		// set the request in the response always for its use in service chaining
		greetingServiceResponse.setPlatformServiceRequest(greetingServiceRequest);
		
		ServiceResponseImpl<GreetingServiceResponse> serviceResponse = new ServiceResponseImpl<GreetingServiceResponse>(String.valueOf(ServiceFrameworkConstants.SUCCESS_STATUS_CODE));
		serviceResponse.setResponseData(greetingServiceResponse);
		return serviceResponse;		
	}
	
	/**
	 * Helper method to populate status in response 
	 */
	private void populateStatus(boolean success, GreetingServiceResponse greetingServiceResponse) {
		Status status = new Status();
		status.setCode(success ? ServiceFrameworkConstants.SUCCESS_STATUS_CODE : ServiceFrameworkConstants.FAILURE_STATUS_CODE);
		status.setMessage(success ? ServiceFrameworkConstants.SUCCESS_STATUS_MESSAGE : ServiceFrameworkConstants.FAILURE_STATUS_MESSAGE);
		greetingServiceResponse.setStatus(status);		
	}
		
}
