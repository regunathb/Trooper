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
 * The <code>Broker</code> interface is the point of invoking requests on any
 * deployed service. It is a run time entity that mediates between clients and
 * service implementations through the methods that a service supports
 * 
 * @author  Regunath B
 * @version 1.0, 13/08/2012
 */
public interface Broker {

	/**
	 * Method to invoke a service request on a deployed service.
	 * @param request the ServiceRequest containing suitable service addressing and payload
	 * @return ServiceResponse from the service implementation
	 * @throws ServiceException in case of errors locating or in invoking a service
	 */
	public ServiceResponse<? extends PlatformServiceResponse> invokeService(ServiceRequest<? extends PlatformServiceRequest> request) throws ServiceException;

}
