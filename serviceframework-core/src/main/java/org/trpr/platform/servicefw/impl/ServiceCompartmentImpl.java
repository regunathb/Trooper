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

import java.util.concurrent.TimeUnit;

import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.service.model.common.platformexceptionresponse.ExceptionSummaryType;
import org.trpr.platform.service.model.common.platformexceptionresponse.PlatformExceptionResponse;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.service.model.common.status.Status;
import org.trpr.platform.servicefw.common.ServiceException;
import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.spi.Service;
import org.trpr.platform.servicefw.spi.ServiceCompartment;
import org.trpr.platform.servicefw.spi.ServiceInfo;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

/**
 * The <code>ServiceCompartmentImpl</code> class is an implementation of the {@link ServiceCompartment} interface. 
 * 
 * @see ServiceCompartment
 * @author Regunath B
 * @version 1.0, 16/08/2012
 */
@SuppressWarnings("rawtypes")
public class ServiceCompartmentImpl<T extends PlatformServiceRequest, S extends PlatformServiceResponse> implements ServiceCompartment {
		
	/** The log for this class */
	private static final Logger LOGGER = LogFactory.getLogger(ServiceCompartmentImpl.class);

	/** Constant for invalid response time in milliseconds execution counts*/
	public static final long INVALID_STATISTICS_VALUE = -1L;

	/**
	 * The timestamp when this service compartment was created
	 */
	private long startupTimeStamp = System.currentTimeMillis();

	/**
	 * Stores total requests processed.
	 */
	private long totalUsageCount = 0;

	/**
	 * Stores active request count.
	 */
	private long currentUsageCount = 0;

	/**
	 * Stores timeStamp when Service is last used.
	 */
	private long lastUsageTimeStamp = INVALID_STATISTICS_VALUE;
	
	/**
	 * The cumulative response time
	 */
	private long cumulativeResponseTime = 0;
	
	/**
	 * Minimum and maximum response times
	 */
	private long minimumResponseTime = INVALID_STATISTICS_VALUE;
	private long maximumResponseTime = INVALID_STATISTICS_VALUE;
	
	/**
	 * The last serviced request's response time
	 */
	private long lastServiceRequestResponseTime = INVALID_STATISTICS_VALUE;
	
	/**
	 * Counts of service requests that failed - either due to validations or underlying exceptions
	 */
	private long errorRequestsCounts = 0;
	
	/**
	 * The ServiceInfo that describes the Service routed through this
	 * ServiceCompartment
	 */
	protected ServiceInfo serviceInfo = null;

	private Timer responseTimer;
	
	/**
	 * Constructor for this class
	 * @param serviceInfo the ServiceInfo of the Service that this compartment is expected to handle
	 */
	public ServiceCompartmentImpl(ServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
		this.responseTimer = Metrics.newTimer(Service.class, "response:"+serviceInfo.getServiceKey().toString(), TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
	}

	/**
	 * Interface method implementation. Does nothing
	 * @see ServiceCompartment#init()
	 */
	public void init() throws PlatformException {
		// do nothing
	}
	
	/**
	 * Interface method implementation. Does nothing
	 * @see ServiceCompartment#destroy()
	 */
	public void destroy() throws PlatformException {
		// do nothing
	}

	@SuppressWarnings("unchecked")
	public ServiceResponse processRequest(Service service, ServiceRequest request) throws ServiceException {
		return invokeService(service, request);
	}

	/**
	 * Interface method implementation
	 * 
	 * @see ServiceCompartment#getServiceInfo()
	 */
	public ServiceInfo getServiceInfo() {
		return this.serviceInfo;
	}

	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getStartupTimeStamp()
	 */
	public long getStartupTimeStamp() {
		return this.startupTimeStamp;
	}
	
	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getLastCalledTimestamp()
	 */
	public long getLastCalledTimestamp() {
		return lastUsageTimeStamp;
	}

	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getActiveRequests()
	 */
	public long getActiveRequestsCount() {
		return currentUsageCount;
	}

	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getTotalRequests()
	 */
	public long getTotalRequestsCount() {
		return totalUsageCount;
	}
	
	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getAverageResponseTime()
	 */
	public long getAverageResponseTime() {
		return (this.getTotalRequestsCount() > 0 ? (this.cumulativeResponseTime / this.getTotalRequestsCount()) : 0);
	}
	
	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getMinimumResponseTime()
	 */
	public long getMinimumResponseTime() {
		return this.minimumResponseTime;
	}

	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getMaximumResponseTime()
	 */
	public long getMaximumResponseTime() {
		return this.maximumResponseTime;
	}

	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getLastServiceRequestResponseTime()
	 */
	public long getLastServiceRequestResponseTime() {
		return this.lastServiceRequestResponseTime;
	}
	
	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getErrorRequestsCount()
	 */
	public long getErrorRequestsCount() {
		return this.errorRequestsCounts;
	}
	
	/**
	 * Interface method implementation
	 * @see ServiceCompartment#getSuccessRequestsCount()
	 */
	public long getSuccessRequestsCount() {
		return this.getTotalRequestsCount() - this.errorRequestsCounts;
	}
	
	/**
	 * Interface method implementation. Simply increments current execution counter
	 * @see ServiceCompartment#notifyServiceExecutionStart(ServiceRequest)
	 */
	public void notifyServiceExecutionStart(ServiceRequest request) {
		incrementUsageCounter();		
	}
	
	/**
	 * Interface method implementation. Computes service invocation statistics
	 * @see ServiceCompartment#notifyServiceExecutionEnd(ServiceRequest, ServiceResponse, long, long)
	 */
	public void notifyServiceExecutionEnd(ServiceRequest request, ServiceResponse response, long executionStartTime, long executionEndTime) {
		decrementUsageCounter();		
		// synchronized block as this is a single instance through for all service bean instances
		// This class is not thread safe as a consequence and the instance variables need to be modified inside a synchronized block
		synchronized(this) {
			this.lastServiceRequestResponseTime = executionEndTime - executionStartTime;
			if (this.lastServiceRequestResponseTime < this.minimumResponseTime ||
				this.minimumResponseTime == INVALID_STATISTICS_VALUE) {
				this.minimumResponseTime = this.lastServiceRequestResponseTime;
			}
			if (this.lastServiceRequestResponseTime > this.maximumResponseTime) {
				this.maximumResponseTime = this.lastServiceRequestResponseTime;
			}
			this.cumulativeResponseTime += this.lastServiceRequestResponseTime;
			if (String.valueOf(ServiceFrameworkConstants.FAILURE_STATUS_CODE).equalsIgnoreCase(((ServiceResponseImpl)response).getStatusCode())) {
				this.errorRequestsCounts += 1;
			}
		}		
	}

	/**
	 * Invokes the service using the specfied service request. Sub classes
	 * provide concrete implementation for this method.
	 * 
	 * @param request
	 *            ServiceRequest which has the payload.
	 * @return ServiceResponse from the invoked service.
	 */
	protected ServiceResponse invokeService(Service<T,S> service, ServiceRequest<T> request) {
		ServiceResponse serviceResponse = null;
		final TimerContext context = responseTimer.time();
		try {
			serviceResponse = service.processRequest(request);
		} catch (Exception e) {
			LOGGER.error("ServiceCompartmentImpl :: Error Invoking service : "	+ request.getServiceName() + "_" + request.getServiceVersion(), e);
			// catch and return a ServiceResponse for all kinds of exceptions
			// that might arise when invoking a remote service
			return constructServiceResponseFromException(e);
		} finally {
			context.stop();
		}
		return serviceResponse;
	}

	/**
	 * Method which updates active request count and total request count.
	 * Method is synchronized as this is a single instance through which all calls to the associated service bean is routed.
	 * All modifications to instance variables of this class are synchronized.  
	 */
	private synchronized void incrementUsageCounter() {
		lastUsageTimeStamp = System.currentTimeMillis();
		++currentUsageCount;
		++totalUsageCount;
	}

	/**
	 * Method which updates active request count.
	 * Method is synchronized as this is a single instance through which all calls to the associated service bean is routed.
	 * All modifications to instance variables of this class are synchronized.  
	 */
	private synchronized void decrementUsageCounter() {
		// decrement only if use count is greater than zero. It might have been set to zero if #resetServiceStatistics() is called
		if (currentUsageCount > 0) {
			--currentUsageCount;
		}
	}

	/**
	 * Helper method to construct a ServiceResponse from the specified Throwable
	 * instance. Used to report exception trace back to the caller for generic
	 * exceptions
	 * 
	 * @param e
	 *            the Throwable instance to construct the response from
	 * @return ServiceResponse instance created from the specified throwable.
	 */
	@SuppressWarnings("unchecked")
	protected ServiceResponse constructServiceResponseFromException(Throwable e) {
		ServiceResponseImpl serviceResponseImpl = new ServiceResponseImpl(
				String.valueOf(ServiceFrameworkConstants.FAILURE_STATUS_CODE));
		PlatformExceptionResponse platformExceptionResponse = new PlatformExceptionResponse();
		Status status = new Status();
		status.setCode(ServiceFrameworkConstants.FAILURE_STATUS_CODE);
		status.setMessage(ServiceFrameworkConstants.FAILURE_STATUS_MESSAGE);
		platformExceptionResponse.setStatus(status);
		ExceptionSummaryType exceptionSummaryType = new ExceptionSummaryType();
		exceptionSummaryType.setErrorMessage(e.getMessage());
		StackTraceElement[] stackTraceElement = e.getStackTrace();
		StringBuffer stackTraceString = new StringBuffer();
		stackTraceString.append(e.getClass().getName());
		stackTraceString.append("\n");
		for (int i = 0; i < stackTraceElement.length; i++) {
			stackTraceString.append(stackTraceElement[i]);
			stackTraceString.append("\n");
		}
		exceptionSummaryType.setStackTrace(stackTraceString.toString());
		platformExceptionResponse.setExceptionSummary(exceptionSummaryType);
		serviceResponseImpl.setResponseData(platformExceptionResponse);
		return serviceResponseImpl;
	}

}
