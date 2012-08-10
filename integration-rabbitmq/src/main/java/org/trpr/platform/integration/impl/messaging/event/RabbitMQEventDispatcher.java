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
package org.trpr.platform.integration.impl.messaging.event;

import org.trpr.platform.core.impl.event.PlatformEventConsumerImpl;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.impl.management.jmx.JMXNotificationDispatcher;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.integration.impl.messaging.RabbitMQMessagePublisherImpl;
import org.trpr.platform.integration.spi.messaging.MessagingException;
import org.trpr.platform.model.event.PlatformEvent;
import org.springframework.beans.factory.DisposableBean;

/**
 * The <code>RabbitMQEventDispatcher</code> is a sub-type of the {@link PlatformEventConsumerImpl that receives {@link PlatformEvent} instances published 
 * to the same Spring ApplicationContext that this bean is registered in.
 * This event consumer dispatches the received event to configured RabbitMQ queues using the {@link RabbitMQMessagePublisherImpl}
 * 
 * Note that this class implements the Spring {@link DisposableBean} to get a callback when the ApplicationContext that created this Spring bean
 * is destroyed. This call-back is used to cleanly close any open RabbitMQ connections.
 *  
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */
public class RabbitMQEventDispatcher extends PlatformEventConsumerImpl implements DisposableBean {
	
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(RabbitMQEventDispatcher.class);
	
	/**
	 * The RabbitMQ message publisher
	 */
	private RabbitMQMessagePublisherImpl rabbitMessagePublisher;	
	
	/**
	 * The JMX notification dispatcher to use in case of event publishing failures
	 */
	private JMXNotificationDispatcher jmxNotificationDispatcher;	
	
	/**
	 * Interface call-back method. Closes any open RabbitMQ connections
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		try {
			this.rabbitMessagePublisher.closeConnections();
		} catch (MessagingException e) {
			// log a message. Cant do much more as we are closing anyway
			LOGGER.info("Error closing connections held by RabbitMQMessagePublisherImpl." + 
					"Ignoring it as this Event consumer is closing anyway. Error is : " + e.getMessage());
		}		
	}	

	/**
	 * Dispatches the specified PlatformEvent to the configured RabbitMQ queue
	 * @see org.trpr.platform.core.impl.event.PlatformEventConsumerImpl#processPlatformEvent(org.trpr.platform.model.event.PlatformEvent)
	 */
	protected void processPlatformEvent(PlatformEvent platformEvent) {
		try {
			this.rabbitMessagePublisher.publish(platformEvent);
		} catch (MessagingException e) {
			// Event publishing has failed. Nothing can be done apart from alerting the monitoring system and logging the error
			LOGGER.error("Error publishing Platform Event to handling queues. Event is of type : " + platformEvent.getEventType() +
					" . Source is : " + platformEvent.getEventSource(),e);
			this.jmxNotificationDispatcher.dispatchException(e, this.getClass().getName());
		}
	}

	/** Start Java bean style setters and getters*/
	public RabbitMQMessagePublisherImpl getRabbitMessagePublisher() {
		return this.rabbitMessagePublisher;
	}
	public void setRabbitMessagePublisher( RabbitMQMessagePublisherImpl rabbitMessagePublisher) {
		this.rabbitMessagePublisher = rabbitMessagePublisher;
	}
	public JMXNotificationDispatcher getJmxNotificationDispatcher() {
		return jmxNotificationDispatcher;
	}
	public void setJmxNotificationDispatcher( JMXNotificationDispatcher jmxNotificationDispatcher) {
		this.jmxNotificationDispatcher = jmxNotificationDispatcher;
	}	
	/** End Java bean style setters and getters*/
	
}
