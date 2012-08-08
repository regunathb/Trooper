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

import org.trpr.platform.core.spi.event.EndpointEventProducer;
import org.trpr.platform.model.event.PlatformEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * The <code>EndpointEventProducerImpl</code> is an implementation of the EndpointEventProducer interface.
 * Wraps the PlatformEvent into a PlatformApplicationEvent and publishes it to the Spring ApplicationContext.
 * 
 * @see org.springframework.context.ApplicationContext
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */
public class EndpointEventProducerImpl implements EndpointEventProducer, ApplicationContextAware {

	/**
	 * The Spring ApplicationContext instance set by the container for this
	 * ApplicationContextAware implementation
	 */
	private ApplicationContext appContext;

	/**EndpointURI configured for this event producer for sending MuleApplicationEvent(s)*/
	private String defaultEndpointURI;

	/**
	 * Interface method implementation
	 * @see org.trpr.platform.core.spi.event.PlatformEventProducer#publishEvent(PlatformEvent)
	 */
	public void publishEvent(PlatformEvent event) {
		publishPlatformEvent(event, this.defaultEndpointURI);
	}

	/**
	 * Interface method implementation
	 * @see org.trpr.platform.core.spi.event.PlatformEventProducer#publishEvent(PlatformEvent, java.lang.String)
	 */
	public void publishEvent(PlatformEvent event, String endpointURI) {
		publishPlatformEvent(event, endpointURI);
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;
	}
	
	/**
	 * Gets the defaultEndpointURI
	 * @return Returns the defaultEndpointURI used by this event producer
	 */
	public String getDefaultEndpointURI() {
		return defaultEndpointURI;
	}

	/**
	 * Sets the defaultEndpointURI
	 * @param defaultEndpointURI the endpoint URI to be used by this event producer for publishing events where the endpoint URI is not explicitly specified 
	 */
	public void setDefaultEndpointURI(String defaultEndpointURI) {
		this.defaultEndpointURI = defaultEndpointURI;
	}	

	/**
	 * Helper method to publish a {@link PlatformEvent} to corresponding
	 * implementation based on the specified endpoint. It publishes the event to the {@link ApplicationContext}
	 */
	private void publishPlatformEvent(final PlatformEvent event, String endpointURI){
		PlatformApplicationEvent platformEvent = new PlatformApplicationEvent(event);
		platformEvent.setEndpointURI(endpointURI);
		// publishes the PlatformEvent to the configured endpoint.
		appContext.publishEvent(platformEvent);
	}	
	
}
