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
package org.trpr.platform.core.impl.event;

import org.trpr.platform.model.event.PlatformEvent;
import org.springframework.context.ApplicationEvent;

/**
 * The <code>PlatformApplicationEvent</code> is a concrete Spring ApplicationEvent sub-type that wraps a PlatformEvent.
 * Also supports specifying an endpoint URI as destination for this event.
 * 
 * @see org.springframework.context.ApplicationEvent
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */
public class PlatformApplicationEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 1L;
	
	/** The end-point URI for this event*/
	private String endpointURI;

	/**
	 * Constructor for this class. 
	 * @param source the PlatformEvent source for this Spring ApplicationEvent 
	 */
	public PlatformApplicationEvent(PlatformEvent source) {
		super(source);
	}

	/** ======== Getter/Setter methods */
	public String getEndpointURI() {
		return endpointURI;
	}
	public void setEndpointURI(String endpointURI) {
		this.endpointURI = endpointURI;
	}
	
}
