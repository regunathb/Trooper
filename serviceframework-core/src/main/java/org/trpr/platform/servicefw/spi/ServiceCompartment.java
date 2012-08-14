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

/**
*
* The <code>ServiceCompartment</code> is a section of the container. Every container has 
* one or more compartments.  There is one compartment per service.
* It holds information about the service instance such as the last time it was called,
* the number of requests processed e.t.c.
* This compartment also acts as the conduit for service invocation by the Broker.
* 
* @author  Regunath B
* @version 1.0, 13/08/2012
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
	public void notifyServiceExecutionEnd(ServiceRequest<T> request, ServiceResponse<S> response,	long executionStartTime, long executionEndTime);
	
	/**
	 * Gets the service information which was used to load and
	 * configure the associated service.
	 * @return Service information
	 */
	public ServiceInfo getServiceInfo();
	
	/**
	 * Gets the timestamp of when this service compartment was created. Useful to determine uptime of this service compartment.
	 * @return millsecond timestamp of the creation of this service compartment
	 */
	public long getStartupTimeStamp();
	
	/**
	 * Gets the timestamp of when the service was last called or zero
	 * if the service has never been called before.
	 * @return millsecond timestamp of last call
	 */
	public long getLastCalledTimestamp();
	
	/**
	 * Gets the number of requests active currently 
	 * @return number of active requests
	 */
	public long getActiveRequestsCount();

	/**
	 * Gets the total number of requests serviced via this compartment
	 * @return the total number of requests processed
	 */
	public long getTotalRequestsCount();	
	
	/**
	 * Gets the average response time for service requests invoked against the associated service
	 * @return average response time in milliseconds 
	 */
	public long getAverageResponseTime();
	
	/**
	 * Gets the minimum response time among all requests executed against the associated service
	 * @return minimum response time in milliseconds
	 */
	public long getMinimumResponseTime();
	
	/**
	 * Gets the maximum response time among all requests executed against the associated service
	 * @return maximum response time in milliseconds
	 */
	public long getMaximumResponseTime();
	
	/**
	 * Gets the response time of the last executed service request of associated service. Could be either success or failure as the case may be.
	 * Service failures due to exceptions or errors and validation or business logic errors in the service implementation are treated the same while
	 * determining the last executed service request. 
	 * @return last executed service request's response time in milliseconds
	 */
	public long getLastServiceRequestResponseTime();
	
	/**
	 * Gets the count of service requests that failed
	 * @return the failed service requests count 
	 */
	public long getErrorRequestsCount();
	
	/**
	 * Gets the count of service requests that succeeded
	 * @return the success service requests count 
	 */
	public long getSuccessRequestsCount();
	
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
