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
package org.trpr.platform.integration.spi.messaging;

/**
 * The <code>MessagePublisher</code> provides methods to publish String and Object messages to a pre-configured queue destination on a messaging system.
 * This interface is suitable for simple message publishing needs. Specific implementations may support features like distributed publishing across
 * set of configured queues, error detection and possible recovery/retry.
 * 
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */

public interface MessagePublisher {

	/**
	 * Initializes this MessagePublisher
	 */
	public void initialize();
	
	/**
	 * Publishes the specified object and keeps the connection open for further requests. Note that clients of this
	 * publisher must call {@link MessagePublisher#closeConnections()} when done using this MessagePublisher
	 * This method converts the specified object to a raw byte array and then publishes it to the queue. 
	 * @param message the message to be enqueued
	 * @throws MessagingException in case of errors in message publishing
	 */
	public void publish(Object message) throws MessagingException;

	/**
	 * Publishes the specified String object and keeps the connection open for further requests. Note that clients of this
	 * publisher must call {@link MessagePublisher#closeConnections()} when done using this MessagePublisher
	 * This method converts the specified String object to a UTF-8 encoded byte array and then publishes it to the queue. 
	 * @param message the String to be published.
	 * @throws MessagingException in case of errors in message publishing
	 */
	public void publishString(String message) throws MessagingException;
	
	/**
	 * Closes connection related objects used by this MessagePublisher.
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
