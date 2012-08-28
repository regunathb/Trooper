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

import java.util.Calendar;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.management.jmx.AppInstanceAwareMBean;
import org.trpr.platform.service.model.common.statistics.ServiceStatistics;
import org.trpr.platform.servicefw.spi.ServiceCompartment;
import org.trpr.platform.servicefw.spi.ServiceContainer;
import org.trpr.platform.servicefw.spi.ServiceKey;


/**
 * The <code>ServiceStatisticsGatherer</code> class gathers service invocation statistics from the UID service framework classes.
 * This class does not collect the statistics itself. It collates the statistics from the different framework classes, as required.
 * 
 * This class exposes the collected statistics via JMX interface and wraps the data as JMX supported types. 
 * For moniroting agents that do not provide complete support for JMX interface data types like Composite and TabularType, this class also makes the
 * service statistics available as a reference in a persistence store where the metric has been captured.
 * 
 * @see ServiceStatisticsGatherer#getServiceInvocationStatistics() for data returned using JMX supported types
 * @see ServiceStatisticsGatherer#getServiceStatsReference() for reference to persisted service statistics 
 * 
 * @author Regunath B
 * @version 1.0, 18 Jan 2011
 */
@ManagedResource(objectName = "spring.application:type=Trooper,application=Service-Statistics,name=ServiceStatisticsGatherer-", description = "Service Invocation Statistics")
public class ServiceStatisticsGatherer extends AppInstanceAwareMBean {
	
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(ServiceStatisticsGatherer.class);
	
	/** Data values used in constructing the JMX CompositeData and TabularData type instances*/
	private static String[] attributeNames = {
		"serviceName", 
		"serviceVersion", 
		"startupTimeStamp", 
		"lastCalledTimestamp",
		"activeRequestsCount",
		"totalRequestsCount",
		"averageResponseTime",
		"minimumResponseTime",
		"maximumResponseTime",
		"lastServiceRequestResponseTime",
		"errorRequestsCount",
		"successRequestsCount",
		};
	private static String[] attributeDescriptions = {
		"Service name", 
		"Service version", 
		"Service startup timestamp", 
		"Service last called timestamp",
		"Service active requests count",
		"Service total requests count",
		"Service average response time",
		"Service minimum response time",
		"Service maximum response time",
		"Service last request's response time",
		"Service error requests count",
		"Service success requests count",
		};
	private static OpenType[] attributeTypes = {
		SimpleType.STRING, 
		SimpleType.STRING, 
		SimpleType.DATE, 
		SimpleType.DATE, 
		SimpleType.LONG, 
		SimpleType.LONG, 
		SimpleType.LONG, 
		SimpleType.LONG, 
		SimpleType.LONG, 
		SimpleType.LONG, 
		SimpleType.LONG, 
		SimpleType.LONG,
		};
	private static CompositeType compositeType;
	
	private static String[] indexNames = {"serviceName", "serviceVersion",};
	private static TabularType tableType;
	
	// static initializer block for composite and table type initialization
	static {
		try {
			compositeType = new CompositeType("serviceStatistics", "Service statistics", attributeNames, attributeDescriptions, attributeTypes);
			tableType = new TabularType("listOfServiceStatistics", "List of Service statistics", compositeType, indexNames);
		} catch (Exception e) {
			// ideally we should never get this error as it uses statically defined data elements
			LOGGER.error("Error initializing JMX types used in service statistics monitoring : " + e.getMessage(),e);
		}
	}
	
	/** The Spring JMX managed attribute that returns the service stats as a JMX supported type*/
	private TabularDataSupport serviceInvocationStatistics;
	
	/** The ServiceContainer for this class*/
	@SuppressWarnings("rawtypes")
	private ServiceContainer serviceContainer;
	
	public ServiceStatisticsGatherer() {
		serviceInvocationStatistics = new TabularDataSupport(tableType);
	}

	/**
	 * The JMX interface method for reading the managed attribute containing service invocation statistics.
	 * @return TabularDataSupport JMX type containing a row each of {@link ServiceStatistics} wrapped as JMX type CompositeData
	 */
	@ManagedAttribute
	public TabularDataSupport getServiceInvocationStatistics() {
		this.populateServiceStatistics();
		return serviceInvocationStatistics;
	}
	
	/**
	 * The JMX interface method for reading the managed attribute containing service invocation statistics and resetting the statistics after invocation. 
	 * @return TabularDataSupport JMX type containing a row each of {@link ServiceStatistics} wrapped as JMX type CompositeData
	 */
	@ManagedAttribute
	public TabularDataSupport getServiceInvocationStatisticsAndReset() {
		this.populateServiceStatistics(true);
		return serviceInvocationStatistics;
	}
	
	/**
	 * Wraps the service statistics into JMX types
	 */
	private void populateServiceStatistics() {
		populateServiceStatistics(false);
	}
	
	/**
	 * Wraps the service statistics into JMX types
	 */
	private void populateServiceStatistics(boolean clearCollectedStats){
		// clear existing stats in the TabularDataSupport and re-populate it with current data extracted from service framework classes
		serviceInvocationStatistics.clear();
		ServiceStatistics[] stats = getStats(clearCollectedStats);
		for (ServiceStatistics stat : stats) {
			Object[] statValues = new Object[attributeNames.length];
			statValues[0] = stat.getServiceName();
			statValues[1] = stat.getServiceVersion();
			statValues[2] = stat.getStartupTimeStamp().getTime();
			statValues[3] = stat.getLastCalledTimestamp().getTime();
			statValues[4] = stat.getActiveRequestsCount();
			statValues[5] = stat.getTotalRequestsCount();
			statValues[6] = stat.getAverageResponseTime();
			statValues[7] = stat.getMinimumResponseTime();
			statValues[8] = stat.getMaximumResponseTime();
			statValues[9] = stat.getLastServiceRequestResponseTime();
			statValues[10] = stat.getErrorRequestsCount();
			statValues[11] = stat.getSuccessRequestsCount();
			CompositeData compositeData;
			try {
				compositeData = new CompositeDataSupport(compositeType, attributeNames, statValues);
				serviceInvocationStatistics.put(compositeData);
			} catch (OpenDataException e) {
				// ideally we should not get this exception
				LOGGER.error("Error constructing JMX data type from service statistics. Error is : " + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Returns the ServiceStatistics array for all services deployed locally
	 * @param clearcollectedStats boolean variable to reset the collected statistics.
	 * @return ServiceStatistics array containing one instance per service deployed locally
	 */
	@SuppressWarnings("rawtypes")
	private ServiceStatistics[] getStats(boolean clearCollectedStats){
		if (serviceContainer == null) { // the service container has not been set. Return an empty array of service invocation statistics
			return new ServiceStatistics[0];
		}
		ServiceKey[] serviceKeys = serviceContainer.getAllLocalServices();
		ServiceStatistics[] servicesStatistics = new ServiceStatistics[serviceKeys.length];
		for (int i=0; i<serviceKeys.length; i++) {
			ServiceCompartment serviceCompartment = serviceContainer.getCompartment(serviceKeys[i]);
			servicesStatistics[i] = new ServiceStatistics();
			servicesStatistics[i].setServiceName(serviceKeys[i].getName());
			servicesStatistics[i].setServiceVersion(serviceKeys[i].getVersion());
			Calendar startupTimeStamp = Calendar.getInstance();
			startupTimeStamp.setTimeInMillis(serviceCompartment.getStartupTimeStamp());
			servicesStatistics[i].setStartupTimeStamp(startupTimeStamp);
			Calendar lastCalledTimeStamp = Calendar.getInstance();
			lastCalledTimeStamp.setTimeInMillis(serviceCompartment.getLastCalledTimestamp() <= 0 ? null : 
				serviceCompartment.getLastCalledTimestamp());
			servicesStatistics[i].setLastCalledTimestamp(lastCalledTimeStamp);
			servicesStatistics[i].setActiveRequestsCount(serviceCompartment.getActiveRequestsCount());
			servicesStatistics[i].setTotalRequestsCount(serviceCompartment.getTotalRequestsCount());
			servicesStatistics[i].setAverageResponseTime(serviceCompartment.getAverageResponseTime());
			servicesStatistics[i].setMinimumResponseTime(serviceCompartment.getMinimumResponseTime());
			servicesStatistics[i].setMaximumResponseTime(serviceCompartment.getMaximumResponseTime());
			servicesStatistics[i].setLastServiceRequestResponseTime(serviceCompartment.getLastServiceRequestResponseTime());
			servicesStatistics[i].setErrorRequestsCount(serviceCompartment.getErrorRequestsCount());
			servicesStatistics[i].setSuccessRequestsCount(serviceCompartment.getSuccessRequestsCount());
		}
		return servicesStatistics;
	}
	
	/** Getter setter methods*/
	@SuppressWarnings("rawtypes")
	public ServiceContainer getServiceContainer() {
		return serviceContainer;
	}
	@SuppressWarnings("rawtypes")
	public void setServiceContainer(ServiceContainer serviceContainer) {
		this.serviceContainer = serviceContainer;
	}
	public void setServiceInvocationStatistics(TabularDataSupport serviceInvocationStatistics) {
		this.serviceInvocationStatistics = serviceInvocationStatistics;
	}
	/** End Getter setter methods*/

}
