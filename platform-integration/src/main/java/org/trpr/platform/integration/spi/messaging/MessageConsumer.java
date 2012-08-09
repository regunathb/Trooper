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
 * 
 */
package org.trpr.platform.integration.spi.messaging;

/**
 * The <code>MessageConsumer</code> provides methods to consume String and Object messages from a pre-configured queue destination on a messaging system.
 * This interface is suitable for simple message consumption needs like a queuing consumer that consumes one message at a time in a single thread. 
 * Specific implementations may support features like message consumption from a set of configured queues, error detection and possible recovery/retry.
 * 
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */

public interface MessageConsumer {

	/**
	 * Initializes this MessageConsumer
	 */
	public void initialize();
	
	/**
	 * Consumes a single message from the underlying messaging system and keeps the connection open for further requests. 
	 * Note that clients of this consumer must call {@link MessageConsumer#closeConnections()} when done using this MessageConsumer
	 * This consumer reconstructs the published message from the raw byte array as sent by {@link MessagePublisher#publish(Object)}
	 * @return message the Object message consumed from the queue 
	 * @throws MessagingException in case of errors in consuming a single available message
	 */
	public Object consume() throws MessagingException;
	
	/**
	 * Consumes a single String message from the underlying messaging system and keeps the connection open for further requests. 
	 * Note that clients of this consumer must call {@link MessageConsumer#closeConnections()} when done using this MessageConsumer
	 * This consumer reconstructs the published message String by decoding the UTF-8 byte array as sent by {@link MessagePublisher#publishString(String)}
	 * @return the String message consumed from the queue. 
	 * @throws MessagingException in case of errors in consuming a single available message
	 */
	public String consumeString() throws MessagingException;
		
	/**
	 * Closes connection related objects used by this MessageConsumer.
	 * @throws MessagingException in case of errors closing connections to the underlying messaging system.
	 */
	public void closeConnections() throws MessagingException;
	
	/**
	 * Returns queue depth of the currently active message queue configuration
	 * @return number of messages in the currently configured message queue 
	 * @throws MessagingException in case of errors in getting queue depth of the configured queue
	 */
	public int getQueueDepth() throws MessagingException;
		
}
