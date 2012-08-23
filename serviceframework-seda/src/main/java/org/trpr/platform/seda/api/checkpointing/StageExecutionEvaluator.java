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

package org.trpr.platform.seda.api.checkpointing;

import org.trpr.platform.core.spi.persistence.PersistentEntity;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;

/**
 * The <code>StageExecutionEvaluator</code> interface defines behavior for evaluating stage execution results and returning suitable checkpointing
 * data.  
 * 
 * @author Regunath B
 * @version 1.0, 23/08/2012
 */
public interface StageExecutionEvaluator<T,S> {
	
	/**
	 * Evaluates the specified ServiceRequest and ServiceResponse and returns an array of {@link PersistentEntity} instances that contain check-pointing data. 
	 * May return null if the specified service message objects cannot be evaluated by this StageExecutionEvaluator.
	 * @param ServiceRequest the ServiceRequest for stage execution
	 * @param serviceResponse the ServiceResponse from stage execution
	 * @return null or array of PersistentEntity instances containing check-pointing data for service execution
	 */
	public PersistentEntity[] evaluateStageExecutionResponse(ServiceRequest<T> serviceRequest, ServiceResponse<S> serviceResponse);

	/**
	 * Evaluates the specified ServiceRequest and ServiceResponse on whether the stage should be executed based on pre-conditions, which needs to be implemented by the implementation class.
	 * returns a boolean value based on which the decision can be made.
	 * @param ServiceRequest the ServiceRequest for stage execution
	 * @param serviceResponse the ServiceResponse from stage execution
	 * @return boolean boolean value indicating whether to proceed or not.
	 */
	public boolean canProceed(ServiceRequest<T> serviceRequest);
}
