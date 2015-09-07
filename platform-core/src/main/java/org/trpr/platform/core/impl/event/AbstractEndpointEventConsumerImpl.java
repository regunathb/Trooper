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

import org.springframework.context.event.ApplicationEventMulticaster;
import org.trpr.platform.core.spi.event.EndpointEventConsumer;
import org.trpr.platform.model.event.PlatformEvent;

/**
 * The <code>AbstractEndpointEventConsumerImpl</code> is an implementation of {@link EndpointEventConsumer}, which is registered with the
 * {@link ApplicationEventMulticaster} and therefore subscribes to events that match the subscription end-point URIs. This event consumer may be sub-classed
 * to cater to subscription needs of specific PlatformEvent types - Service Events, Batch events, Alerts, Notifications etc.
 * 
 * @see PlatformEvent
 *  
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */

public abstract class AbstractEndpointEventConsumerImpl implements EndpointEventConsumer {

	/**
	 * The set of subscription endpoint URIs that this event consumer is subscribed to 
	 */
	private String[] subscriptions;

	/**
	 * Interface method implementation
	 * @see org.trpr.platform.core.spi.event.EndpointEventConsumer#getSubscriptions()
	 */
	public String[] getSubscriptions() {
		return this.subscriptions;
	}

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.core.spi.event.EndpointEventConsumer#setSubscriptions(String[])
	 */
	public void setSubscriptions(String[] subscriptions) {
		this.subscriptions = subscriptions;
	}

	/**
	 * Interface method implementation. Processes only if the event source is of type {@link PlatformEvent}
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	public void onApplicationEvent(PlatformApplicationEvent springEvent) {
		if (springEvent.getSource() instanceof PlatformEvent) {
			handlePlatformEvent((PlatformEvent)springEvent.getSource());
		}
	}
	
	/**
	 * Handles the specified PlatformEvent. Sub-types of this class process the event as appropriate - for e.g. send it to a RabbitMQ queue.
	 * @param platformEvent the PlatformEvent to process.
	 */
	protected abstract void handlePlatformEvent(PlatformEvent platformEvent);

}
