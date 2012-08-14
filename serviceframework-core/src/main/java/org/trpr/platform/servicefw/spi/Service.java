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

package org.trpr.platform.servicefw.spi;

import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.common.ServiceException;

/**
 * This interface is for developing services.All services which follow service architecture
 * must implement the service interface.The interface defines a single point entry into the service 
 * using processRequest method.Service calls from clients are handled using a request and response paradigm.
 * 
 * @author  Regunath B
* @version 1.0, 13/08/2012
 */
public interface Service<T extends PlatformServiceRequest, S extends PlatformServiceResponse> {
	  /**
	   * Executes a single request from the client. The method implements a request and response paradigm.
	   * The request object contains information about the service request, including parameters provided by 
	   * the client. The response object is used to return information to the client. 
	   * The request and response objects rely on the underlying network transport for quality 
	   * of service guarantees, such as reordering, duplication, privacy, and authentication
	   * @param request service request carrying the payload 
	   * @param response  response carrying response of the service processed.
	   */
	  public ServiceResponse<S> processRequest(ServiceRequest<T> request) throws ServiceException;
	
}
