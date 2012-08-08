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
 * The <code>EndpointEventProducer</code> is a sub-type of PlatformEventProducer used primarily to publish events to specific end-point URIs. 
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */

public interface EndpointEventProducer extends PlatformEventProducer {

	/**
	 * Publishes the specified PlatformEvent to the configured destination using the specified endpoint URI.
	 * 
	 * @param event the PlatformEvent to publish
	 * @param endPointURI the endpoint URI string that is used by event producers and consumers to publish and subscribe to events
	 */
	public void publishEvent(PlatformEvent event,String endpointURI);
		
}
