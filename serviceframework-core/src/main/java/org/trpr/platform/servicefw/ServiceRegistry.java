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

import java.util.LinkedList;
import java.util.List;

import org.trpr.platform.servicefw.common.ServiceFrameworkConstants;
import org.trpr.platform.servicefw.impl.ServiceInfoImpl;
import org.trpr.platform.servicefw.impl.ServiceKeyImpl;
import org.trpr.platform.servicefw.spi.ServiceContainer;
import org.trpr.platform.servicefw.spi.ServiceInfo;
import org.trpr.platform.servicefw.spi.ServiceKey;

/**
 * The <code>ServiceRegistry</code> discovers and maintains a registry of all services deployed in a given {@link ServiceContainer}.
 * Provides lookup methods to find {@link ServiceInfo} using a {@link ServiceKey}
 * 
 * @author Regunath B
 * @version 1.0, 14/08/2012
 */
public class ServiceRegistry {

	/** List of ServiceInfo instances that form this registry*/
	private LinkedList<ServiceInfo> serviceInfoList = new LinkedList<ServiceInfo>();

    /**
     * Checks whether a Service is present in the registry
     * @param key ServiceKey to be checked
     * @return true, if found, false otherwise
     */
    public boolean contains(ServiceKey key) {
        if(this.getServiceInfo(key)!=null) {
            return true;
        }
        return false;
    }

    /**
     * Removes a Service with the specified ServiceKey from the registry, if found
     */
    public void remove(ServiceKey key) {
        ServiceInfo toRemove = this.getServiceInfo(key);
        if(toRemove!=null) {
            this.serviceInfoList.remove(toRemove);
        }
    }
	/**
	 * Adds a ServiceInfo to this registry using the specified service meta data
	 * @param serviceName the service name identifier
	 * @param serviceVersion the service version
	 * @param projectName project/module name that the service belongs to
	 * @param domainName name of the domain that the service is hosted on
	 */
	public void addServiceInfoToRegistry(String serviceName, String serviceVersion, String projectName, String domainName) {
		this.serviceInfoList.add(new ServiceInfoImpl(projectName, domainName, !domainName.equals(ServiceFrameworkConstants.DEFAULT_DOMAIN), 
				new ServiceKeyImpl(serviceName, serviceVersion)));		
	}
	
	/**
	 * Returns a list of all ServiceInfo instances loaded by this service registry
	 * @return List containing ServiceInfo instances
	 */
	public List<ServiceInfo> getAllServiceInfos() {
		return this.serviceInfoList;
	}
	
	/**
	 * Returns the ServiceInfo identified by the specified ServiceKey
	 * If service key specifies version as <code>ServiceKey.LATEST_VERSION</code>, then,
	 * this method will return that ServiceInfo object that corresponds to service with
	 * highest version number value.
	 * @param serviceKey ServiceKey identifying the ServiceInfo needed
	 * @return null or the ServiceInfo identified by the specified service key
	 */
	public ServiceInfo getServiceInfo(ServiceKey serviceKey) {
		for (ServiceInfo serviceInfo : this.serviceInfoList) {
			if (serviceInfo.getServiceKey().getName().equals(serviceKey.getName())) {
				if (ServiceKey.LATEST_VERSION.equalsIgnoreCase(serviceKey.getVersion())) {
					// If latest version is being requested, then, loop through rest of service info and find the one
					// with highest service version
					if (serviceInfo == null || (Float.parseFloat(serviceInfo.getServiceKey().getVersion()) >= 
						Float.parseFloat(serviceInfo.getServiceKey().getVersion())) ) {
							return serviceInfo;
					}
				}
				else {
					// If specific version is being requested, then, return break from loop if version matches.
					if (serviceInfo.getServiceKey().getVersion().equals(serviceKey.getVersion())) {
						return serviceInfo;
					}
				}
			}
		}
		return null;
	}	
}
