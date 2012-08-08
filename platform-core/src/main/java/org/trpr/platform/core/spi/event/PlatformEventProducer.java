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

package org.trpr.platform.core.spi.event;

import org.trpr.platform.model.event.PlatformEvent;

/**
 * The <code>PlatformEventProducer</code> is an interface used to publish {@link PlatformEvent}. All event producers must implement this
 * interface to publish {@link PlatformEvent}.
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */
public interface PlatformEventProducer {

	/**
	 * Publishes the specified PlatformEvent to the destination configured for this event producer. Destinations may vary and could include
	 * messaging queues or intermediate containers like the Spring ApplicationContext to which event consumers may be registered using different
	 * subscription models - all events, specific events or endpoint URI based subscriptions.
	 * 
	 * @param event the PlatformEvent to publish
	 */
	public void publishEvent(PlatformEvent event);
	
}
