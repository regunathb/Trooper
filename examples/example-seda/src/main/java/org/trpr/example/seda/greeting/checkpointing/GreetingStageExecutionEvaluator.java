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
package org.trpr.example.seda.greeting.checkpointing;

import org.trpr.example.model.service.greetingservice.GreetingServiceRequest;
import org.trpr.example.model.service.greetingservice.GreetingServiceResponse;
import org.trpr.example.seda.greeting.persistence.PersistentEarthling;
import org.trpr.platform.core.spi.persistence.PersistentEntity;
import org.trpr.platform.seda.api.checkpointing.StageExecutionEvaluator;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.ServiceResponseImpl;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;

/**
 * The <code>GreetingStageExecutionEvaluator</code> is an implementation of {@link StageExecutionEvaluator} used to demonstrate stage data check-pointing in the 
 * sample service.
 * 
 * @author Regunath B
 * @version 1.0, 28/12/2012
 */
public class GreetingStageExecutionEvaluator implements StageExecutionEvaluator<GreetingServiceRequest, GreetingServiceResponse> {

	/**
	 * Interface method implementation. Returns a single element array of PersistentEarthling instance from successful execution of the GreetingService
	 * @see org.trpr.platform.seda.api.checkpointing.StageExecutionEvaluator#evaluateStageExecutionResponse(org.trpr.platform.servicefw.spi.ServiceRequest, org.trpr.platform.servicefw.spi.ServiceResponse)
	 */
	public PersistentEntity[] evaluateStageExecutionResponse(ServiceRequest<GreetingServiceRequest> serviceRequest, ServiceResponse<GreetingServiceResponse> serviceResponse) {
		if (((ServiceResponseImpl<GreetingServiceResponse>)serviceResponse).getStatusCode().equalsIgnoreCase(String.valueOf(ServiceFrameworkConstants.FAILURE_STATUS_CODE))) {
			return null; // nothing to checkpoint as response is in error
		} else {
			return new PersistentEarthling[] {new PersistentEarthling(serviceRequest.getRequestData().getEarthling())};			
		}
	}

}
