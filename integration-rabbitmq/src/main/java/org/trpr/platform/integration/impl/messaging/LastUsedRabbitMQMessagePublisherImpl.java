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

package org.trpr.platform.integration.impl.messaging;

import org.trpr.platform.integration.spi.messaging.MessagePublisher;
import org.trpr.platform.integration.spi.messaging.MessagingException;

/**
 * The <code>RabbitMQMessagePublisherImpl</code> class is an implementation of {@link MessagePublisher} that uses
 * RabbitMQ as the underlying messaging
 * provider. This implementation is compatible with Java client API of RabbitMQ version 2.2.0. Backward/Forward
 * compatibility with other versions
 * requires verification.
 * This publisher tries to reuse the last used successful configuration to publish a message. 
 * If the last used configuration fails then this publisher will exhaust all {@link RabbitMQConfiguration} instances 
 * when trying to publish a message and fails only when every one of the configurations fail to connect or fail otherwise.
 * This class implements the Spring {@link org.springframework.beans.factory.DisposableBean} and calls
 * {@link #closeConnections()} method to
 * cleanup connections when the application context is torn down. The dependence on Spring is justified by the need to
 * close connections cleanly
 * during application shutdown.
 * @author Jagadeesh Huliyar
 * @version 1.0, 26/06/2013
 */

public class LastUsedRabbitMQMessagePublisherImpl extends RabbitMQMessagePublisherImpl {
	/**
	 * The index of the rabbit mq configuration that was last used for connection.
	 */
	private int lastUsedConfigurationIndex = 0;
	
	/**
	 * Publishes the message to the last successful configuration that was used to publish a message. 
	 * If the last used configuration fails or does not exist then this publisher will exhaust all {@link RabbitMQConfiguration} instances 
	 * when trying to publish a message and fails only when every one of the configurations fail to connect or fail otherwise.
	 * It also remembers the successful configuration and this us used in future publishes. 
	 * @param message Message that needs to be published
	 */
	public void publish(Object message) throws MessagingException {
		validateMessage(message);
		try {
			publishToConnection(message,lastUsedConfigurationIndex);
		} catch(Exception e) {
			lastUsedConfigurationIndex = publishWithRoundRobinPolicy(message);
		}
	}


}