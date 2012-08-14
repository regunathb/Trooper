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

package org.trpr.platform.servicefw.security;

import java.util.LinkedList;

import org.trpr.platform.servicefw.spi.ServiceKey;


/**
 * The <code>CallContext</code> class holds information about the current call into the service framework.
 * It holds service information details like whether the call is presently inside a service invocation hierarchy.
 * It also holds information on the currently logged in Principal, if any.
 * 
 * @author  Regunath B
 * @version 1.0, 13/08/2012
 */

public final class CallContext implements SecurityContext {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -1213718823598161592L;

	/** new Thread local*/
	private static ThreadLocal<CallContext> currentThreadLocal = new ThreadLocal<CallContext>();
	
    /** The Linked List holds a list of serviceHierarchy */
	private LinkedList<ServiceKey> serviceHierarchy = new LinkedList<ServiceKey>();
	
	/**
	 * Default constructor.
	 */ 
	private CallContext() {
	}
	
	/**
	 * Gets the current CallContext. Creates an returns a new one if one doesnot exist
	 * @return callContext the current CallContext instance
	 */
	public static CallContext getCurrentCallContext() {
		CallContext callContext = (CallContext)currentThreadLocal.get();
		if(callContext == null){
			callContext = new CallContext();
			setCallContext(callContext);
		}
		return callContext;
	}

	
	/**
	 * The method returns a LinkedList containing service keys of services within a service
	 * invocation hierarchy. 
	 * @return LinkedList containing service keys
	 */
	public LinkedList<ServiceKey> getServiceHierarchy() {
		return serviceHierarchy;
	}
	
	/**
	 * Sets the specified service hierarchy as the active hierarchy for this CallContext
	 * @param serviceHierarchy non null LinkedList containing service keys
	 */
	public void setServiceHierarchy(LinkedList<ServiceKey> serviceHierarchy) {
		this.serviceHierarchy = serviceHierarchy;
	}
	
	/**
	 * Adds the specified Service Key to the service invocation hierarchy.
	 * @param ServiceKey of the service that is going to be executed 
	 */
	public void addToServiceHierarchy(ServiceKey serviceKey){
		this.serviceHierarchy.add(serviceKey);
	}

	/**
	 * Removes the specified Service Key from the service invocation hierarchy.
	 * @param ServiceKey of the service that has completed execution
	 */
	public void removeFromServiceHierarchy(ServiceKey serviceKey){
		this.serviceHierarchy.remove(serviceKey);
	}
	
	/**
	 * Sets the current CallContext
	 * @param callContext the CallContext to set as the current one
	 */
	private static void setCallContext(CallContext callContext) {
		currentThreadLocal.set(callContext);
	}
	
	
}
