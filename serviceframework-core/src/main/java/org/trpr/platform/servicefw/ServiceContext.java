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

package org.trpr.platform.servicefw;

import org.trpr.platform.model.event.PlatformAlert;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.model.event.PlatformNotification;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.common.ServiceException;
import org.trpr.platform.servicefw.impl.ServiceKeyImpl;
import org.trpr.platform.servicefw.spi.ServiceContainer;
import org.trpr.platform.servicefw.spi.ServiceKey;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;
import org.trpr.platform.spi.task.TaskManager;

/**
 * The <code>ServiceContext</code> class defines a set of methods that a service may use to communicate 
 * with its container - for example to dispatch events and alerts.
 * 
 * @see ServiceContainer
 * 
 * @author Regunath B
 * @version 1.0, 13/08/2012
 */
public class ServiceContext<T,S,P extends PlatformServiceRequest,R extends PlatformServiceResponse> {
	
	/**
	 * Endpoint URIs for various Event types published using this ServiceContext 
	 */
	public static final String SERVICE_EVENTS_ENDPOINT = "evt://service.events";
	public static final String SERVICE_ALERTS_ENDPOINT = "evt://service.alerts";
	public static final String SERVICE_NOTIFICATIONS_ENDPOINT = "evt://service.notifications";
	public static final String SERVICE_AUDITTRAILS_ENDPOINT = "evt://service.audittrails";
	public static final String SERVICE_TASKEVENT_ENDPOINT = "evt://service.taskevents";

	/**
	 * The ServiceContainer instance that initialized this ServiceContext
	 */
	private ServiceContainer<P,R> serviceContainer;
	
	/** The TaskManager implementation to use for Task execution*/
	private TaskManager taskManager;
	
	/**
	 * Signals start of execution of the specified service request by the service 
	 * @param request the ServiceRequest that is being executed
	 */
	public void notifyServiceExecutionStart(ServiceRequest<P> request) {
		ServiceKey key = new ServiceKeyImpl(request.getServiceName(), request.getServiceVersion());
		this.serviceContainer.getCompartment(key).notifyServiceExecutionStart(request);
	}
	
	/**
	 * Signals end of service execution for the specified service request
	 * @param request the ServiceRequest that was executed
	 * @param response the ServiceResponse for the executed service request
	 * @param executionStartTime the service execution start timestamp
	 * @param executionEndTime the service execution end timestamp
	 */
	public void notifyServiceExecutionEnd(ServiceRequest<P> request, ServiceResponse<R> response,
			long executionStartTime, long executionEndTime) {
		ServiceKey key = new ServiceKeyImpl(request.getServiceName(), request.getServiceVersion());
		this.serviceContainer.getCompartment(key).notifyServiceExecutionEnd(request, response, executionStartTime, executionEndTime);		
	}
	
	/**
	 * Publishes the specified event using the ServiceContainer that initialized this ServiceContext
	 * @param event the PlatformEvent to publish
	 */
	public void publishEvent(PlatformEvent event) {
		this.publishEvent(event, SERVICE_EVENTS_ENDPOINT);
	}

	/**
	 * Publishes the specified alert using the ServiceContainer that initialized this ServiceContext
	 * @param alert the PlatformAlert to publish
	 */
	public void publishAlert(PlatformAlert alert) {
		this.publishEvent(alert, SERVICE_ALERTS_ENDPOINT);
	}

	/**
	 * Publishes the specified notification using the ServiceContainer that initialized this ServiceContext
	 * @param event the PlatformNotification to publish
	 */
	public void publishNotification(PlatformNotification notification) {
		this.publishEvent(notification, SERVICE_NOTIFICATIONS_ENDPOINT);
	}

	/**
	 * Publishes the specified event to the specified endpoint URI using the ServiceContainer that 
	 * initialized this ServiceContext
	 * @param event the PlatformEvent to publish
	 * @param endpointURI the endpoint URI to publish the event to
	 */
	public void publishEvent(PlatformEvent event,String endpointURI) {		
		if (this.serviceContainer == null) {
			throw new ServiceException("Attempt to use ServiceContext in the absence of a ServiceContainer. ServiceContext is not initialized.");
		}
		this.serviceContainer.publishEvent(event, endpointURI);
	}
	
	/**
	 * Determines if the currently active ServiceContainer supports check-pointing service execution data.
	 * @return true if the ServiceContainer supports check-pointing service execution data, false otherwise
	 */
	public boolean doesContainerSupportCheckpointing() {
		return this.serviceContainer.isServiceExecutionCheckPointingRequired();
	}
	
	/** Setter/Getter methods*/
	
	/**
	 * Sets the ServiceContainer for this ServiceContext
	 * @param serviceContainer the ServiceContainer that initialized this ServiceContext
	 */
	public void setServiceContainer(ServiceContainer<P,R> serviceContainer) {
		this.serviceContainer = serviceContainer; 
	}
	/**
	 * Returns the ServiceContainer for this ServiceContext
	 * @return the ServiceContainer that initialized this ServiceContext
	 */
	public ServiceContainer<P,R> getServiceContainer() {
		return this.serviceContainer;
	}
	@SuppressWarnings("rawtypes")
	public TaskManager getTaskManager() {
		return this.taskManager;
	}
	@SuppressWarnings("rawtypes")
	public void setTaskManager(TaskManager taskManager) {
		this.taskManager = taskManager;
	}   
	/** End setter/getter methods*/
	
}
