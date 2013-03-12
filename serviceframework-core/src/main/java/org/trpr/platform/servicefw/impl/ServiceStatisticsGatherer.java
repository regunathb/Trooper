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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.management.jmx.AppInstanceAwareMBean;
import org.trpr.platform.service.model.common.statistics.ServiceStatistics;
import org.trpr.platform.servicefw.spi.ServiceContainer;
import org.trpr.platform.servicefw.spi.ServiceKey;


/**
 * The <code>ServiceStatisticsGatherer</code> class gathers service invocation statistics from the JMX, and exposes them 
 * to any class accessing the getStats() method
 * 
 * @author Regunath B, devashishshankar
 * @version 1.1, 11 Mar 2011
 * 
 */
public class ServiceStatisticsGatherer extends AppInstanceAwareMBean {
	
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(ServiceStatisticsGatherer.class);

	/**
	 * The mBeansServer from which data has to be extracted
	 */
	private MBeanServer mbeanServer;

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
	public static final int LAST_SERVICE_TIME_ATTR_INDEX = 12;
	
	/** Attribute names assigned to the Metrics */
	public static final String[] ATTRIBUTE_NAMES = {
	    "startupTimeStamp",             //Gauge
	    "lastCalledTimestamp",          //Gauge
	    "totalRequestsCount",           //Counter
	    "activeRequestsCount",          //Counter
	    "errorRequestsCount",           //Counter
	    "responseTime",                 //Timer
	    "responseTime",                 //Timer
	    "responseTime",                 //Timer
	    "responseTime",                 //Timer
	    "responseTime",                 //Timer
	    "responseTime",                 //Timer
	    "responseTime",                 //Timer
	    "lastServiceRequestResponseTime",//Gauge
	    "responseTime",                 //Timer
		};

	/** SubAttribute names assigned to the Metrics */
	private static final String[] SUB_ATTRIBUTE_NAMES = {
	    "Value",                        //Gauge
	    "Value",                        //Gauge
	    "Count", 			            //Counter
	    "Count",			            //Counter
	    "Count",			            //Counter
	    "50thPercentile",               //Timer
	    "75thPercentile",            	//Timer
	    "99thPercentile",          		//Timer
	    "Mean",         		        //Timer
	    "OneMinuteRate",    	        //Timer
	    "FiveMinuteRate",               //Timer
	    "FifteenMinuteRate",            //Timer
	    "Value",                        //Gauge
	    "999thPercentile"       		//Timer
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
	public ServiceStatistics[] getStats(){
		if (serviceContainer == null) { // the service container has not been set. Return an empty array of service invocation statistics
			return new ServiceStatistics[0];
		}
		Map<String, Map<String,ObjectName>> serviceToPropertyToObject = this.preloadObjectNames(mbeanServer);
		ServiceKey[] serviceKeys = serviceContainer.getAllLocalServices();
		ServiceStatistics[] servicesStatistics = new ServiceStatistics[serviceKeys.length];
		
		for (int i=0; i<serviceKeys.length; i++) {
			Map<String, ObjectName> propertyToObject = serviceToPropertyToObject.get(serviceKeys[i].toString());
			servicesStatistics[i] = new ServiceStatistics();
			servicesStatistics[i].setServiceName(serviceKeys[i].getName());
			servicesStatistics[i].setServiceVersion(serviceKeys[i].getVersion());
			//Get values from MBeans
			Object[] mbeanValues = new Object[ATTRIBUTE_NAMES.length];
			for(int j=0;j<ATTRIBUTE_NAMES.length;j++) {
				try {
					ObjectName objName = propertyToObject.get(ServiceStatisticsGatherer.ATTRIBUTE_NAMES[j]);
					mbeanValues[j] = this.mbeanServer.getAttribute(objName, ServiceStatisticsGatherer.SUB_ATTRIBUTE_NAMES[j]);
					
				} catch (Exception e) {
					mbeanValues[j] = null;
				}
			}
			//Transfer values to serviceStatistics array
			Calendar startupTimeStamp = Calendar.getInstance();
			startupTimeStamp.setTimeInMillis((Long) mbeanValues[0]);
			servicesStatistics[i].setStartupTimeStamp(startupTimeStamp);
			Calendar lastCalledTimeStamp = null;
			lastCalledTimeStamp = Calendar.getInstance();
			lastCalledTimeStamp.setTimeInMillis((Long) mbeanValues[1]);
			if(lastCalledTimeStamp.getTimeInMillis()<1) {
				servicesStatistics[i].setLastCalledTimestamp(null);
			} else {
			servicesStatistics[i].setLastCalledTimestamp(lastCalledTimeStamp);
			}
			servicesStatistics[i].setTotalRequestsCount((Long) mbeanValues[2]);
			servicesStatistics[i].setActiveRequestsCount((Long) mbeanValues[3]);
			servicesStatistics[i].setErrorRequestsCount((Long) mbeanValues[4]);
			servicesStatistics[i].setP50ResponseTime((Double) mbeanValues[5]);
			servicesStatistics[i].setP75ResponseTime((Double) mbeanValues[6]);
			servicesStatistics[i].setP99ResponseTime((Double) mbeanValues[7]);
			servicesStatistics[i].setP999ResponseTime((Double) mbeanValues[13]);
			servicesStatistics[i].setMeanResponseTime((Double) mbeanValues[8]);
			servicesStatistics[i].setOneMinRate((Double) mbeanValues[9]);
			servicesStatistics[i].setFiveMinRate((Double) mbeanValues[10]);
			servicesStatistics[i].setFifteenMinRate((Double) mbeanValues[11]);
			servicesStatistics[i].setSuccessRequestsCount(servicesStatistics[i].getTotalRequestsCount()
					-servicesStatistics[i].getErrorRequestsCount());
		}
		return servicesStatistics;
	}
	
	/**
	 * Helper method. Preloads the ObjectName instances and sorts them into a Map indexed by
	 * the attribute name for the property, which is further indexed by the serviceName
	 */
	protected Map<String, Map<String,ObjectName>>  preloadObjectNames(MBeanServer server)
	{
		Map<String, Map<String,ObjectName>> serviceToPropertyToObject = new TreeMap<String, Map<String,ObjectName>>();
		try {
			Set<ObjectName> ons = server.queryNames(null,null);
			for(Iterator<ObjectName> i=ons.iterator(); i.hasNext(); ) {
				ObjectName name = (ObjectName) i.next();
				String domain = name.getDomain();
				if(!domain.equals(JMX_DOMAIN)) {
					continue;
				}
				String objDescription = name.getKeyProperty(PROPERTY_ID);
				String serviceName = objDescription.substring(objDescription.lastIndexOf(SERVICE_NAME_ATTRIBUTE_SEP)+1);
				serviceName = serviceName.replaceAll(QUOTES, "");
				String propertyName = objDescription.substring(0,objDescription.lastIndexOf(SERVICE_NAME_ATTRIBUTE_SEP));
				propertyName =propertyName.replaceAll(QUOTES, "");
				if(!serviceToPropertyToObject.containsKey(serviceName)) {
					Map<String, ObjectName> propertyToObject = new TreeMap<String, ObjectName>();
					propertyToObject.put(propertyName, name);
					serviceToPropertyToObject.put(serviceName, propertyToObject);
				} else {
					serviceToPropertyToObject.get(serviceName).put(propertyName, name);
				}
			}
		} catch(Exception e) {
			LOGGER.error("Exception while fetching data from JMX",e);
		}
		return serviceToPropertyToObject;
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
	public MBeanServer getMbeanServer() {
		return mbeanServer;
	}	
	public void setMbeanServer(MBeanServer mbeanServer) {
		this.mbeanServer = mbeanServer;
	}
	/** End Getter setter methods*/
}
