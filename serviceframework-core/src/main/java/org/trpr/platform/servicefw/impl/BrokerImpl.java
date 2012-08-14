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

package org.trpr.platform.servicefw.impl;

import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.common.ServiceException;
import org.trpr.platform.servicefw.security.CallContext;
import org.trpr.platform.servicefw.spi.Broker;
import org.trpr.platform.servicefw.spi.ServiceContainer;
import org.trpr.platform.servicefw.spi.ServiceInfo;
import org.trpr.platform.servicefw.spi.ServiceKey;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;

/**
 * 
 * The <code>BrokerImpl</code> is an implementation of the {@link Broker} interface. Invokes the service via the {@link ServiceContainer} set on this
 * Broker. Use the {@link BrokerFactory} to get an instance of this Broker.
 * 
 * @see Broker
 * @author Regunath B
 * @version 1.0, 14/08/2012
 */
public class BrokerImpl<T extends PlatformServiceRequest, S extends PlatformServiceResponse> implements Broker {
	
	/** The ServiceContainer instance for all service look up */
	private ServiceContainer<T,S> serviceContainer;

	/** Service Info of the service that is brokered by this instance */
	private ServiceInfo serviceInfo;
	
	/** The Default Constructor */
	protected BrokerImpl(ServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}

	/**
	 * Interface Method implementation to process the service request in Assured service response
	 * mode or in a synchronous mode and return the service response
	 * 
	 * @param request
	 *            The service request to be processed by the Broker
	 * @return ServiceResponse
	 */
	public ServiceResponse<? extends PlatformServiceResponse> invokeService(ServiceRequest<? extends PlatformServiceRequest> request)
			throws ServiceException {

		ServiceResponse<? extends PlatformServiceResponse> response = null;		
		ServiceKey serviceKey = new ServiceKeyImpl(request.getServiceName(),
				request.getServiceVersion());
		
		response = getServiceResponse(request,serviceKey);
		return response;
	}
	
	/** Setter/Getter methods */
	public ServiceContainer<T, S> getServiceContainer() {
		return this.serviceContainer;
	}
	public void setServiceContainer(ServiceContainer<T, S> serviceContainer) {
		this.serviceContainer = serviceContainer;
	}	
	/** End setter/getter methods*/

	/**
	 * Processes the service request in synchronous mode and returns the service response.
	 * 
	 * @param request The service request to be processed by the Broker
	 * @return ServiceResponse
	 * @throws ServiceException
	 */
	private ServiceResponse<? extends PlatformServiceResponse> getServiceResponse(ServiceRequest<? extends PlatformServiceRequest> request,ServiceKey serviceKey)
			throws ServiceException {

		// Set the invoking service key into the service hierarchy.
		setServiceHierarchy(serviceInfo.getServiceKey());
		
		ServiceResponse<? extends PlatformServiceResponse> response = this.serviceContainer.invokeService(serviceInfo, request);
		
		// Unset the invoking service key from the service hierarchy.
		resetServiceHierarchy(serviceInfo.getServiceKey());
		
		return response;
	}

	/**
	 * Sets the service key into Service Hierarchy Linked List.
	 * 
	 * @param request
	 *            Service Request for determining service key.
	 */
	private void setServiceHierarchy(ServiceKey serviceKey) {
		CallContext callContext = CallContext.getCurrentCallContext();
		callContext.addToServiceHierarchy(serviceKey);
	}

	/**
	 * Resets the service key from the service invocation hierarchy
	 */
	private void resetServiceHierarchy(ServiceKey serviceKey) {
		CallContext callContext = CallContext.getCurrentCallContext();
		callContext.removeFromServiceHierarchy(serviceKey);
	}
	
}