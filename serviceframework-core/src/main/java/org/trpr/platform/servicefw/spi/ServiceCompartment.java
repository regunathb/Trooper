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

import org.trpr.platform.core.PlatformException;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.common.ServiceException;

import com.yammer.metrics.Metrics;

/**
*
* The <code>ServiceCompartment</code> is a section of the container. Every container has 
* one or more compartments.  There is one compartment per service.
* It holds information about the service instance such as the last time it was called,
* the number of requests processed e.t.c. Using {@link Metrics}, this class publishes the statistics
* directly to JMX Mbeans server
* 
* @author  Regunath B, devashishshankar
* @version 1.1, 11/03/2013
*/
public interface ServiceCompartment<T extends PlatformServiceRequest, S extends PlatformServiceResponse> {
	
	/**
	 * Conduit point for calling the associated service.
	 * @param req the Service Request
	 * @return ServiceResponse the service response
	 * @throws ServiceException exception thrown in case of error in service invocation
	 */
	public ServiceResponse<S> processRequest(ServiceRequest<T> req) throws ServiceException;
	
	/**
	 * Signals service execution start
	 * @param request the ServiceRequest that has currently started to execute
	 */
	public void notifyServiceExecutionStart(ServiceRequest<T> request);

	/**
	 * Signals end of service execution for the specified service request
	 * @param request the ServiceRequest that was executed
	 * @param response the ServiceResponse for the service request
	 * @param executionStartTime the execution start timestamp
	 * @param executionEndTime the execution end timestamp
	 */
	public void notifyServiceExecutionEnd(ServiceRequest<T> request, ServiceResponse<S> response, long executionStartTime, long executionEndTime);
	
	/**
	 * Gets the service information which was used to load and
	 * configure the associated service.
	 * @return Service information
	 */
	public ServiceInfo getServiceInfo();
		
	/**
	 * Initializes this ServiceCompartment.
	 * Load all necessary info for ServiceCompartment.   
	 * @throws PlatformException in case of errors during init
	 */
	public void init() throws PlatformException;
	
	/**
	 * Destroys this ServiceContainer
	 * @throws PlatformException in case of errors during destroy 
	 */
	public void destroy() throws PlatformException;	
	
}
