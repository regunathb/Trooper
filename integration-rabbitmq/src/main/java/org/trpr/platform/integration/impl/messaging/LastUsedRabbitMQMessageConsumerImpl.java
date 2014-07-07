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

import org.trpr.platform.integration.spi.messaging.MessagingException;

/**
 * <code>LastUsedRabbitMQMessageConsumerImpl<code> is a sub-type of the {@link RabbitMQMessageConsumerImpl} that tries to reuse the last used successful 
 * configuration to consume a message. If the last used configuration fails then this consumer will exhaust all {@link RabbitMQConfiguration} instances 
 * when trying to consume a message and fails only when every one of the configurations fail to connect or fail otherwise.
 * 
 * @author Regunath B
 * @version 1.0, 07/07/2014
 */
public class LastUsedRabbitMQMessageConsumerImpl extends RabbitMQMessageConsumerImpl {

	/**
	 * The last used connection index
	 */
	private int lastUsedConfigurationIndex = 0;

	/**
	 * Interface method implementation. Consumes a single message from the last successful configuration that was used to consume a message. 
	 * If the last used configuration fails or does not exist then this consumer will exhaust all {@link RabbitMQConfiguration} instances 
	 * when trying to consume a message and fails only when every one of the configurations fail to connect or fail otherwise.
	 * It also remembers the successful configuration and this us used in future publishes. 
	 * @see org.trpr.platform.integration.impl.messaging.RabbitMQMessageConsumerImpl#consume()
	 */
	public Object consume() throws MessagingException {
		return consume(false);
	}
	
	/**
	 * Interface method implementation. Behavior is identical to {@link #consume()} except that the returned message is of type {@link String}
	 * @see org.trpr.platform.integration.impl.messaging.RabbitMQMessageConsumerImpl#consumeString()
	 */
	public String consumeString() throws MessagingException {
		return (String)consume(true);
	}
	
	/**
	 * Helper method to consume message from the last used configuration and in case of errors, from any of the available configurations.
	 * Remembers the last successfully used configuration in message consumption.
	 */
	private Object consume(boolean isString) throws MessagingException {
		try {
			return consumeFromConnection(isString, lastUsedConfigurationIndex).message;
		} catch(Exception e) {
			MessageHolder messageHolder = consumeWithRoundRobinPolicy(isString);
			this.lastUsedConfigurationIndex = messageHolder.connectionIndex;
			return messageHolder.message;
		}				
	}
}
