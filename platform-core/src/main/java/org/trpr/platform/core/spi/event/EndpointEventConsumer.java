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

/**
 * Interface <code>EndpointEventConsumer</code> is a event consumer interface for processing events published to URI endpoints.
 * This interface permits event consumers to register subscriptions to events using end-point URIs that are declared or used by event publishers.  
 * 
 * @see EndpointEventProducer
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */
public interface EndpointEventConsumer extends PlatformEventConsumer {

	/**
	 * Return the list of URIs that indicate subscriptions for this event consumer
	 * @return String array of event subscription URIs
	 */
    public String[] getSubscriptions();

    /**
     * Sets the event subscription URIs 
     * @param subscriptions String array containing event subscription URIs
     */
    public void setSubscriptions(String[] subscriptions);
	
}

