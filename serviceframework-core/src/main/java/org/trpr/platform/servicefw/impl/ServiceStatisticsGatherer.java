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
import java.util.HashMap;
import java.util.Map;

import org.trpr.platform.core.spi.management.jmx.AppInstanceAwareMBean;
import org.trpr.platform.service.model.common.statistics.ServiceStatistics;
import org.trpr.platform.servicefw.spi.ServiceContainer;
import org.trpr.platform.servicefw.spi.ServiceKey;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;


/**
 * The <code>ServiceStatisticsGatherer</code> class gathers service invocation statistics from the {@link MetricsRegistry}
 * to any class accessing the getStats() method
 * 
 * @author Regunath B, devashishshankar
 * @version 1.1, 11 Mar 2011
 * 
 */
public class ServiceStatisticsGatherer extends AppInstanceAwareMBean {

	/** The domain name of Mbean in which data is stored */
	public static final String JMX_DOMAIN = "\"org.trpr.platform.servicefw.impl\"";

	/** The types of Mbeans (Class names from which beans have been registered) */
	public static final String[] JMX_TYPES = {
		"ServiceCompartmentImpl",
		"AbstractServiceImpl",
		"SimpleAbstractServiceImpl"
	};

	public static final int STARTUP_TIME_ATTR_INDEX = 0;
	public static final int LAST_CALLED_TIME_ATTR_INDEX = 1;
	public static final int TOTAL_REQUEST_COUNT_ATTR_INDEX = 2;
	public static final int ACTIVE_REQUEST_COUNT_ATTR_INDEX = 3;
	public static final int ERROR_REQUEST_COUNT_ATTR_INDEX = 4;
	public static final int RESPONSE_TIME_ATTR_INDEX = 5;
	public static final int LAST_SERVICE_TIME_ATTR_INDEX = 6;
	public static final int ERROR_REQUEST_RATE_ATTR_INDEX = 7;

	/** Attribute names assigned to the Metrics */
	private static final String[] ATTRIBUTE_NAMES = {
		"startupTimeStamp",             //Gauge
		"lastCalledTimestamp",          //Gauge
		"totalRequestsCount",           //Counter
		"activeRequestsCount",          //Counter
		"errorRequestsCount",           //Counter
		"responseTime",                 //Timer
		"lastServiceRequestResponseTime",//Gauge
		"errorRequestRate",             //Meter
	};

	/** Seperator for Attribute name and Service name in JMX */
	public static final String SERVICE_NAME_ATTRIBUTE_SEP = ":";

	/** The property id of ObjectName Class that holds the service and attribute name */
	public static final String PROPERTY_ID = "name";

	/** Redundant character that has to be removed from Bean names */
	public static final String QUOTES = "\"";

	/** The ServiceContainer for this class*/
	@SuppressWarnings("rawtypes")
	private ServiceContainer serviceContainer;

	/**
	 * Returns the ServiceStatistics array for all services deployed locally
	 * @return ServiceStatistics array containing one instance per service deployed locally
	 */
	@SuppressWarnings("rawtypes")
	public ServiceStatistics[] getStats(){
		if (serviceContainer == null) { // the service container has not been set. Return an empty array of service invocation statistics
			return new ServiceStatistics[0];
		}
		MetricsRegistry metricsRegistry = Metrics.defaultRegistry();
		Map<MetricName, Metric> metricsMap = metricsRegistry.allMetrics();
		ServiceKey[] serviceKeys = serviceContainer.getAllLocalServices();
		ServiceStatistics[] servicesStatistics = new ServiceStatistics[serviceKeys.length];
		for (int i=0; i<serviceKeys.length; i++) {
			//Get Metrics from registry
			Gauge startupTime = (Gauge) metricsMap.get(new MetricName(ServiceCompartmentImpl.class,
					getMetricName(STARTUP_TIME_ATTR_INDEX, serviceKeys[i].toString())));
			Gauge lastUsageTime = (Gauge) metricsMap.get(new MetricName(ServiceCompartmentImpl.class,
					getMetricName(LAST_CALLED_TIME_ATTR_INDEX, serviceKeys[i].toString())));
			Gauge lastResponseTime = (Gauge) metricsMap.get(new MetricName(ServiceCompartmentImpl.class,
					getMetricName(LAST_SERVICE_TIME_ATTR_INDEX, serviceKeys[i].toString())));
			Counter totalRequestCount = (Counter) metricsMap.get(new MetricName(ServiceCompartmentImpl.class,
					getMetricName(TOTAL_REQUEST_COUNT_ATTR_INDEX, serviceKeys[i].toString())));
			Counter activeRequestCount = (Counter) metricsMap.get(new MetricName(ServiceCompartmentImpl.class,
					getMetricName(ACTIVE_REQUEST_COUNT_ATTR_INDEX, serviceKeys[i].toString())));
			Counter errorRequestCount = (Counter) metricsMap.get(new MetricName(ServiceCompartmentImpl.class,
					getMetricName(ERROR_REQUEST_COUNT_ATTR_INDEX, serviceKeys[i].toString())));
			Timer responseTimes = (Timer) metricsMap.get(new MetricName(ServiceCompartmentImpl.class,
					getMetricName(RESPONSE_TIME_ATTR_INDEX, serviceKeys[i].toString())));
			Meter errorRequestRate = (Meter) metricsMap.get(new MetricName(ServiceCompartmentImpl.class,
					getMetricName(ERROR_REQUEST_RATE_ATTR_INDEX, serviceKeys[i].toString())));
			//Populate Metrics to serviceStatistics
			servicesStatistics[i] = new ServiceStatistics();
			servicesStatistics[i].setServiceName(serviceKeys[i].getName());
			servicesStatistics[i].setServiceVersion(serviceKeys[i].getVersion());
			//Transfer values to serviceStatistics array
			Calendar startupTimeStamp = Calendar.getInstance();
			startupTimeStamp.setTimeInMillis((Long) startupTime.value());
			servicesStatistics[i].setStartupTimeStamp(startupTimeStamp);
			Calendar lastCalledTimeStamp = null;
			lastCalledTimeStamp = Calendar.getInstance();
			lastCalledTimeStamp.setTimeInMillis((Long) lastUsageTime.value());
			if(lastCalledTimeStamp.getTimeInMillis()<1) {
				servicesStatistics[i].setLastCalledTimestamp(null);
			} else {
				servicesStatistics[i].setLastCalledTimestamp(lastCalledTimeStamp);
			}
			servicesStatistics[i].setTotalRequestsCount((Long) totalRequestCount.count());
			servicesStatistics[i].setActiveRequestsCount((Long) activeRequestCount.count());
			servicesStatistics[i].setErrorRequestsCount((Long) errorRequestCount.count());
			if(responseTimes!=null) {
				servicesStatistics[i].setP50ResponseTime((Double) responseTimes.getSnapshot().getMedian());
				servicesStatistics[i].setP75ResponseTime((Double) responseTimes.getSnapshot().get75thPercentile());
				servicesStatistics[i].setP99ResponseTime((Double) responseTimes.getSnapshot().get99thPercentile());
				servicesStatistics[i].setP999ResponseTime((Double) responseTimes.getSnapshot().get999thPercentile());
				servicesStatistics[i].setMeanResponseTime((Double) responseTimes.mean());
				servicesStatistics[i].setOneMinRate((Double) responseTimes.oneMinuteRate());
				servicesStatistics[i].setFiveMinRate((Double) responseTimes.fiveMinuteRate());
				servicesStatistics[i].setFifteenMinRate((Double) responseTimes.fifteenMinuteRate());
				servicesStatistics[i].setOneMinErrorRate((Double) errorRequestRate.fifteenMinuteRate());
				servicesStatistics[i].setFiveMinErrorRate((Double) errorRequestRate.fifteenMinuteRate());
				servicesStatistics[i].setFifteenMinErrorRate((Double) errorRequestRate.fifteenMinuteRate());
				
			}
			servicesStatistics[i].setSuccessRequestsCount(servicesStatistics[i].getTotalRequestsCount()
					-servicesStatistics[i].getErrorRequestsCount());
		}
		return servicesStatistics;
	}
	
	/** 
	 * Returns the ServiceStatistics map with ServiceKey(toString) as key, for all services deployed locally
	 */
	public Map<String, ServiceStatistics> getStatsAsMap() {
		ServiceStatistics[] serviceStatistics = this.getStats();
		Map<String, ServiceStatistics> serviceStatisticsMap = new HashMap<String, ServiceStatistics>();
		for(ServiceStatistics serviceStatistic : serviceStatistics) {
			serviceStatisticsMap.put(serviceStatistic.getServiceName()+ServiceKeyImpl.SERVICE_VERSION_SEPARATOR+serviceStatistic.getServiceVersion(), serviceStatistic);
		}
		return serviceStatisticsMap;
	}

	/** Helper method that generates name of a metric given its attributeID and service name */
	public static String getMetricName(int attributeID, String serviceName) {
		return ServiceStatisticsGatherer.ATTRIBUTE_NAMES[attributeID]+ServiceStatisticsGatherer.SERVICE_NAME_ATTRIBUTE_SEP+serviceName;
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
	/** End Getter setter methods*/
}
