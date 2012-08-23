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
package org.trpr.platform.seda.impl.service;

import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.common.ServiceException;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.AbstractServiceImpl;
import org.trpr.platform.servicefw.impl.ServiceResponseImpl;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;

/**
 * The <code>MessagingAwareServiceImpl<code> class is a sub-type of the generic {@link AbstractServiceImpl} that is better suited for use in orchestrated
 * service usage scenarios that usually involve a messaging middle-ware to chain the services together.
 * The preferred behavior here is to acknowledge messages on successful execution of the service request but not do so in case the service execution
 * fails. The default behavior of the  {@link AbstractServiceImpl#processRequest(ServiceRequest)} is to consume all Exceptions
 * and return a service response. Exception processing of this nature results in messages getting consumed in success and failure scenarios.
 * 
 * This implementation throws a {@link ServiceException} in case of service responses with failed result codes. The Exception thrown may be processed by
 * the orchestration framework to not acknowledge/commit messages i.e. provide behavior similar to conventional TransactionManager implementations.
 *  
 * @author Regunath B
 * @version 1.0, 23/08/2012
 */
public abstract class MessagingAwareServiceImpl<T extends PlatformServiceRequest, S extends PlatformServiceResponse> extends AbstractServiceImpl<T, S> {

	/**
	 * Overriden superclass method.
	 * Invokes the super class implementation. Processes the response and throws a ServiceException in case of failed responses.
	 * @see AbstractServiceImpl#processRequest(ServiceRequest)
	 */
	public ServiceResponse<S> processRequest(ServiceRequest<T> request) throws ServiceException {	
		ServiceResponse<S> serviceResponse = super.processRequest(request);
		ServiceResponseImpl<S> serviceResponseImpl = (ServiceResponseImpl<S>) serviceResponse;
		PlatformServiceResponse platformResponse = (PlatformServiceResponse)serviceResponse.getResponseData();
		if (serviceResponseImpl.getStatusCode()!=null && Integer.parseInt(serviceResponseImpl.getStatusCode()) == ServiceFrameworkConstants.FAILURE_STATUS_CODE) {
			//throw a ServiceExcepion with details available for the orchestration framework to handle and provide rollback behavior at the messaging layer
			throw new ServiceException("Service execution error - " + serviceResponseImpl.getStatusCode() + ":" + platformResponse.getErrorSummary());
		}
		return serviceResponse;
	}
}
