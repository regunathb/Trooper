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

import org.trpr.platform.core.spi.event.PlatformEventProducer;
import org.trpr.platform.model.event.PlatformEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * The <code>PlatformEventProducerImpl</code> is an implementation of the PlatformEventProducer interface.
 * Wraps the PlatformEvent into a Spring ApplicationEvent and publishes it to the Spring ApplicationContext.
 * This event producer can work only within a Spring container as it is dependent on the ApplicationContext to publish events.
 * A Spring ApplicationEventMulticaster may be configured to route events published by this producer to consumers registered in 
 * the same ApplicationContext. 
 * 
 * @see org.springframework.context.ApplicationContext
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */
public class PlatformEventProducerImpl implements PlatformEventProducer, ApplicationContextAware  {

	/**
	 * The Spring ApplicationContext instance set by the container for this
	 * ApplicationContextAware implementation
	 */
	private ApplicationContext appContext;

	/**
	 * Interface method implementation
	 * @see org.trpr.platform.core.spi.event.PlatformEventProducer#publishEvent(PlatformEvent)
	 */
	public void publishEvent(PlatformEvent event) {
		this.appContext.publishEvent(new PlatformApplicationEvent(event));
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;
	}	
}
