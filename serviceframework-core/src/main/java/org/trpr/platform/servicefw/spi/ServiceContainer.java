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

import org.trpr.platform.runtime.spi.component.ComponentContainer;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.common.ServiceException;
import org.trpr.platform.servicefw.spi.event.ServiceEventProducer;

/**
 * The <code>ServiceContainer</code> interface is a sub-type of the ComponentContainer
 * that serves as a container for services.
 * Provides methods to locate and initialize services during server startup and to invoke them
 * during run time.
 * 
 * @author  Regunath B
 * @version 1.0, 13/08/2012
 * 
 */

public interface ServiceContainer<T extends PlatformServiceRequest, S extends PlatformServiceResponse> extends ComponentContainer, ServiceEventProducer {

	/**
	 * Get an array of all local service keys. The order of the list is
	 * non-deterministic.
	 * 
	 * @return array of local service keys
	 */
	public ServiceKey[] getAllLocalServices();

	/**
	 * Get the compartment instance that is managing the service identified by
	 * the specified service key.
	 * 
	 * @param serviceKey
	 *            The key to the service
	 * @return the compartment that manages the service
	 */
	public ServiceCompartment<T,S> getCompartment(ServiceKey serviceKey);

	/**
	 * Get the serviceInfo instance identified by the specified service key.
	 * 
	 * @param serviceKey
	 *            The key to the service
	 * @return the serviceInfo for the serviceKey
	 */
	public ServiceInfo getServiceInfo(ServiceKey serviceKey);

	/**
	 * Invokes a service identified by the specified service info using the
	 * specified request
	 * 
	 * @param serviceInfo
	 *            the ServiceInfo object that identifies the service
	 * @param request
	 *            the ServiceRequest to use for invoking the service
	 * @return service response object relevant to the specified service
	 * @throws ServiceException
	 *             in case of errors during service invocation
	 */
	public ServiceResponse<? extends PlatformServiceResponse> invokeService(ServiceInfo serviceInfo,
			ServiceRequest<? extends PlatformServiceRequest> request) throws ServiceException;
	
	/**
	 * Determines if this ServiceContainer requires service execution check-point data to be persisted
	 * @return true if check-pointing is needed, false otherwise.
	 */
	public boolean isServiceExecutionCheckPointingRequired();
	
}
