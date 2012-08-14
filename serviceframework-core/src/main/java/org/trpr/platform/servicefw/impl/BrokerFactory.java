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

import org.trpr.platform.servicefw.ServiceRegistry;
import org.trpr.platform.servicefw.common.ServiceException;
import org.trpr.platform.servicefw.spi.Broker;
import org.trpr.platform.servicefw.spi.ServiceInfo;
import org.trpr.platform.servicefw.spi.ServiceKey;

/**
 * The class <code>BrokerFactory.java</code> is a factory implementation that determines the
 * {@link Broker} to be returned. Also validates the service key against service configurations loaded during server startup 
 * to determine if the service invoked indeed exists.
 * This factory creates a suitable Broker instance for services that are within the same project i.e. visible by the same class loader, local machine
 * or in relevant cases - a client side proxy to a remote service.
 * 
 * @author  Regunath B
 * @version 1.0, 14/08/2012
 */
public class BrokerFactory {
	
	/** Exception message for calling a non-existent service */
	private static final String SERVICE_DOESNOT_EXIST = "\nThe specified service is not available on this server (or) service configuration error";
	private static final String YOU_INVOKED = "\nYou invoked : ";
	private static final String SERVICES_AVAILABLE = "\n\nServices deployed on this server(Note case-sensitive) : \n";
	private static final String SERVICE_NAME = "SERVICE NAME : ";
	private static final String PROJECT = " [<PROJECT : ";
	private static final String DOMAIN = " DOMAIN : ";
	private static final String CLOSING_BRACES = ">]";
	private static final String SECTION_DEMARCATION = "\n******************************************************";
	
	/** The ServiceRegistry instance for looking up ServiceInfo details*/
	private ServiceRegistry serviceRegistry;
	
	/**
	 * Returns a Broker implementation that is relevant to the specified service key.
	 * @param ServiceKey based on which a Broker instance is returned
	 * @return Broker implementation relevant to the service key specified
	 */
	public Broker getBroker(ServiceKey serviceKey) throws ServiceException {
		return findBroker(serviceKey);
	}

	/** Helper method to get broker that is relevant to the specified service request */
	@SuppressWarnings("rawtypes")
	private Broker findBroker(ServiceKey serviceKey)throws ServiceException {
		Broker brokerToReturn = null;
		
		ServiceInfo serviceInfo = this.serviceRegistry.getServiceInfo(serviceKey);
		// if service info not found i.e not a valid service key or the service configuration is in error, throw a
		// ServiceException
		if (serviceInfo == null) {
			throw new ServiceException(getMissingServiceMessage(serviceKey));
		}		
		
		brokerToReturn = new BrokerImpl(serviceInfo);
		return brokerToReturn;		
	}
	
	/** Getter/Setter methods */
	public ServiceRegistry getServiceRegistry() {
		return this.serviceRegistry;
	}
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}	
	/** End getter/setter methods */
	
	/**
	 * Helper method to construct a ServiceException message for invocation of a
	 * non-existent service
	 */
	private String getMissingServiceMessage(ServiceKey serviceKey) {
		StringBuffer buffer = new StringBuffer();
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			// ignore the exception, we will not log the host name.
		}
		buffer.append(SERVICE_DOESNOT_EXIST
				+ (hostName == null ? "" : (" : " + hostName)));
		buffer.append(SECTION_DEMARCATION);
		buffer.append(YOU_INVOKED + serviceKey);
		buffer.append(SERVICES_AVAILABLE);
		for (ServiceInfo serviceInfo : this.serviceRegistry.getAllServiceInfos()) {
			buffer.append("\n");
			buffer.append(SERVICE_NAME + serviceInfo.getServiceKey());
			buffer.append(PROJECT + serviceInfo.getProjectName());
			buffer.append(DOMAIN + serviceInfo.getDomainName());
			buffer.append(CLOSING_BRACES);
		}
		buffer.append(SECTION_DEMARCATION);
		buffer.append("\n");
		return buffer.toString();
	}
	
}
