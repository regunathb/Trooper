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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.trpr.platform.service.model.common.event.ServiceAlert;
import org.trpr.platform.service.model.common.event.ServiceEvent;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.ServiceContext;
import org.trpr.platform.servicefw.common.ServiceException;
import org.trpr.platform.servicefw.spi.Header;
import org.trpr.platform.servicefw.spi.Service;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * <code>SimpleAbstractServiceImpl<code> is an implementation of the {@link Service} interface that provides common behavior for all services that require 
 * simple service execution. Use the more involved {@link AbstractServiceImpl} for implementations that might require to break up service execution into a number
 * of tasks.
 * 
 * @author Regunath B
 * @version 1.0, 31/10/2012
 *
 */
public abstract class SimpleAbstractServiceImpl <T extends PlatformServiceRequest, S extends PlatformServiceResponse> implements Service<T, S> {

	/** Constants for string values used locally in this class	 */
	private static final String UNDERSCORE = "_";
	private static final String SERVICE_INVOCATION_TIMESTAMP = "serviceInvocationTimestamp";
	
	/** The ServiceContext to use for all interactions with the ServiceContainer*/
	@SuppressWarnings("rawtypes")
	private ServiceContext serviceContext;	

	/** The {@link Timer} object for this service, which publishes the response time metrics */
	private Timer responses = null;
	
	/** Getter/Setter methods */
	@SuppressWarnings("rawtypes")
	public ServiceContext getServiceContext() {
		return this.serviceContext;
	}
	@SuppressWarnings("rawtypes")
	public void setServiceContext(ServiceContext serviceContext) {
		this.serviceContext = serviceContext;
	}
	/** End Getter/Setter methods*/	
	
	/**
	 * Interface method implementation. 
	 * @see Service#processRequest(ServiceRequest)
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public ServiceResponse<S> processRequest(ServiceRequest<T> request) throws ServiceException {
		
		if (request == null) {
			throw new ServiceException("The Service Request may not be null");
		}
		if(this.responses==null) {
			this.responses = Metrics.newTimer(ServiceCompartmentImpl.class, 
					ServiceStatisticsGatherer.getMetricName(ServiceStatisticsGatherer.RESPONSE_TIME_ATTR_INDEX, request.getServiceName()+ServiceKeyImpl.SERVICE_VERSION_SEPARATOR+request.getServiceVersion()),
					TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
		}
		//Create a timer context, to time this request
		final TimerContext context = responses.time();		
		// add the service request time-stamp as a header
		Header[] headers = new Header[] {new Header(SERVICE_INVOCATION_TIMESTAMP, String.valueOf(System.currentTimeMillis()))};
		((ServiceRequestImpl<T>)request).addHeaders(headers);
		
		// signal start of execution to the service context
		this.serviceContext.notifyServiceExecutionStart(request);
		
		ServiceResponse<S> serviceResponse = null;

		// delegate the execution part.
        serviceResponse = processRequestInternal(request);
        
		if (serviceResponse == null) {
			throw new ServiceException("The Service Response may not be null");
		}

		// signal end of execution to the service context
		this.serviceContext.notifyServiceExecutionEnd(request, serviceResponse, Long.valueOf(request.getHeaderByKey(SERVICE_INVOCATION_TIMESTAMP).getValue()), System.currentTimeMillis());

		context.stop(); // stop the timer
        return serviceResponse;
	}
	
	/**
	 * Template method for delegating service execution to sub-types
	 * @param serviceRequest the ServiceRequest to process
	 * @return the ServiceResponse from service request processing
	 */
	protected abstract ServiceResponse<S> processRequestInternal(ServiceRequest<T> serviceRequest);

	/**
	 * Helper method to publish an event using the publisher configured.
	 */
	protected void publishEvent(ServiceEvent event, ServiceRequest<T> request){
		try {
	        event.setServiceKey(request.getServiceName()+ UNDERSCORE +request.getServiceVersion());
			event.setHostName(InetAddress.getLocalHost().getHostName());
			if (event.getStageStarttime() == null) {
				event.setStageStarttime(Calendar.getInstance());
			}
			if (event.getStageEndtime() == null) {
				event.setStageEndtime(Calendar.getInstance());
			}
		} catch (UnknownHostException e) {
			//ignore the exception
		}
		this.serviceContext.publishEvent(event);
	}
	
	/**
	 * Helper method to publish an alerts using the publisher configured.
	 */
	protected void publishAlert(ServiceAlert alert, ServiceRequest<T> request){
		try {
	        alert.setServiceKey(request.getServiceName()+ UNDERSCORE +request.getServiceVersion());
			alert.setHostName(InetAddress.getLocalHost().getHostName());
		} catch (UnknownHostException e) {
			//ignore the exception
		}
		this.serviceContext.publishAlert(alert);
	}	
}
