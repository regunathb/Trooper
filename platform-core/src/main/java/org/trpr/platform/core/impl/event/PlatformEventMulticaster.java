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

import java.util.Iterator;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.event.EndpointEventConsumer;
import org.trpr.platform.core.spi.logging.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.AbstractApplicationEventMulticaster;

/**
 * The <code>PlatformEventMulticaster</code> is a sub-type of the Spring {@link AbstractApplicationEventMulticaster} that permits specifying 
 * URI endpoints of all subscriptions that this multi-caster entertains. 
 * 
 * Platform {@link EndpointEventConsumer} instances registered in the same ApplicationContext may specify subscription URIs. This multi-caster
 * supports routing only {@link PlatformApplicationEvent} instances to registered ApplicationListener instances where subscriptions match the
 * endpointURI contained in the published PlatformApplicationEvent.
 * 
 * This multi-caster does a synchronous multi-cast of the events. By default, all listeners are invoked in the calling thread.
 * This allows the danger of a rogue listener blocking the entire application. 
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */
public class PlatformEventMulticaster extends AbstractApplicationEventMulticaster {

	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(PlatformEventMulticaster.class);
	
	/** List of subscriptions recognized by this multi-caster*/
	private String[] subscriptions;

	/**
	 * Interface method implementation. Forwards the published event to all event consumers whose subscriptions match the endpointURI
	 * contained in the specified PlatformApplicationEvent.
	 * Note that Spring ApplicationEvent instances that are not of type PlatformApplicationEvent are ignored and a warning message is logged.
	 * @see org.springframework.context.event.ApplicationEventMulticaster#multicastEvent(org.springframework.context.ApplicationEvent)
	 */
	public void multicastEvent(ApplicationEvent event) {
		if (event instanceof PlatformApplicationEvent) {
			PlatformApplicationEvent platformApplicationEvent = (PlatformApplicationEvent)event;
			String eventEndpointURI = platformApplicationEvent.getEndpointURI();
			if (eventEndpointURI == null) {
				LOGGER.debug("End-point URI of PlatformApplicationEvent is null. Event will not be forwarded. Event type is : " + platformApplicationEvent.getClass().getName()); 
				return;
			}
			if (!isSubscriptionMatch(eventEndpointURI, subscriptions)) {
				LOGGER.debug("Endpoint URI doesnot match any of the subscriptions specified on this multi-caster. Event will not be forwarded. Event URI is : " + eventEndpointURI); 
				return;				
			}
			for (Iterator iterator = getApplicationListeners().iterator(); iterator.hasNext();) {
	            ApplicationListener listener = (ApplicationListener) iterator.next();	
	            if (listener instanceof EndpointEventConsumer) {
	            	if (isSubscriptionMatch(eventEndpointURI, ((EndpointEventConsumer)listener).getSubscriptions())) {
	            		listener.onApplicationEvent(event);
	            	}
	            }
			}
		} else {
			// log a warning and ignore the event
			LOGGER.debug("Spring ApplicationEvent of un-supported type received : " + event.getClass().getName() + ". Only PlatformApplicationEvent instances with be forwarded.");
		}
	}
	
	/** === Start Getter/Setter methods*/
	public String[] getSubscriptions() {
		return subscriptions;
	}
	public void setSubscriptions(String[] subscriptions) {
		this.subscriptions = subscriptions;
	}
	/** === End Getter/Setter methods*/	
	
    /**
     * Matches a subscription to the current event endpointURI. Performs an exact match.
     * 
     * @param endpoint endpoint
     * @param subscriptions subscriptions
     * @return true if there's a match
     */	
	private boolean isSubscriptionMatch(String endpoint, String[] subscriptions) {
		for (String subscription : subscriptions) {
			if (endpoint.equalsIgnoreCase(subscription)) {
				return true;
			}
		}
		return false;
	}

}
