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

import org.trpr.platform.core.spi.event.PlatformEventConsumer;
import org.trpr.platform.model.event.PlatformEvent;

/**
 * The <code>PlatformEventProducerImpl</code> is a simple implementation of the PlatformEventConsumer interface.
 * Checks if the passed in event is of type {@link PlatformApplicationEvent} and calls abstract method for the actual event processing.
 * The eventual event processing is implemented by sub-types of this class.
 * This event consumer can work only within a Spring container as it is dependent on the ApplicationContext to receive events.
 * 
 * @see org.springframework.context.ApplicationContext
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */

public abstract class PlatformEventConsumerImpl implements PlatformEventConsumer {

	/**
	 * Interface method implementation. Extract the PlatformEvent and calls #processPlatformEvent(PlatformEvent) for subsequent processing.
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	public void onApplicationEvent(PlatformApplicationEvent event) {
		// process only PlatformApplicationEvent type events
		if (event instanceof PlatformApplicationEvent) {
			processPlatformEvent((PlatformEvent)event.getSource());
		}
	}

	/**
	 * Processes the supplied PlatformEvent
	 * @param event the PlatformEvent routed to this EventConsumer
	 */
	protected abstract void processPlatformEvent(PlatformEvent event);
}
