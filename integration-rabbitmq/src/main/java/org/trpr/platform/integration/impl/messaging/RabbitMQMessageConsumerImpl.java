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

import java.util.List;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.util.PlatformUtils;
import org.trpr.platform.integration.spi.messaging.MessageConsumer;
import org.trpr.platform.integration.spi.messaging.MessagingException;
import org.springframework.beans.factory.DisposableBean;

import com.rabbitmq.client.QueueingConsumer;

/**
 * The <code>RabbitMQMessageConsumerImpl</code> class is an implementation of the {@link MessageConsumer} interface over the RabbitMQ messaging
 * system. This implementation is compatible with Java client API of RabbitMQ version 2.2.0. Backward/Forward compatibility with other versions 
 * requires verification.
 * 
 * This consumer will exhaust all RabbitMQConfiguration instances when trying to consume a message and fails only when every one of the
 * configurations fail to connect or fail otherwise.
 * 
 * This class implements the Spring {@link org.springframework.beans.factory.DisposableBean} and calls {@link #closeConnections()} method to 
 * cleanup connections when the application context is torn down. The dependence on Spring is justified by the need to close connections cleanly
 * during application shutdown.
 * 
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */
public class RabbitMQMessageConsumerImpl implements MessageConsumer, DisposableBean {
	
	/** Constant for the String literal UTF-8*/
	private static final String ENCODING = "UTF-8";
	
	/** The default timeout in milliseconds that this consumer will wait for a message - negative value to indicate infinite*/
	private static final long DEFAULT_WAIT_TIMEOUT = -1;

	/**
	 * The Logger instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(RabbitMQMessageConsumerImpl.class);

	/**
	 * List of message properties to be used for message publishing in different queues.
	 */
	private List<RabbitMQConfiguration> rabbitMQConfigurations;
	
	/**
	 * Array of RabbitConnectionHolder instances equalling the size of rabbitMQConfigurations.
	 */
	private RabbitConnectionHolder[] rabbitConnectionHolders;
	
	/**
	 * Count of total messages consumed by this consumer. Used for round-robin behavior
	 */
	private long totNoOfMessagesConsumed;
	
	/** The wait timeout duration in milliseconds*/
	private long waitTimeoutMillis = DEFAULT_WAIT_TIMEOUT;

	/**
	 * No-args constructor
	 */
	public RabbitMQMessageConsumerImpl()	{
	}
	
	/**
	 * Interface method implementation. Does nothing 
	 * @see MessageConsumer#initialize()
	 */
	public void initialize() {
		// do nothing as connections are created lazily
	}
	
	/** == Start Spring DI style Getters and setters methods definition. */	
	public List<RabbitMQConfiguration> getRabbitMQConfigurations() {
		return this.rabbitMQConfigurations;
	}
	public void setRabbitMQConfigurations(List<RabbitMQConfiguration> rabbitMQConfigurations) {
		this.rabbitMQConfigurations = rabbitMQConfigurations;
		// just initialize the array. Don't create the connections yet
		this.rabbitConnectionHolders = new RabbitConnectionHolder[rabbitMQConfigurations.size()];		
	}
	public long getWaitTimeoutMillis() {
		return this.waitTimeoutMillis;
	}
	public void setWaitTimeoutMillis(long waitTimeoutMillis) {
		this.waitTimeoutMillis = waitTimeoutMillis;
	}
	/** == End Spring DI style Getters and setters methods definition. */

	/**
	 * Interface method implementation. 
	 * @see MessageConsumer#consumeString()
	 */
	public String consumeString() throws MessagingException {
		return (String)consume(true);
	}

	/**
	 * Interface method implementation. 
	 * @see MessageConsumer#consume()
	 */
	public Object consume() throws MessagingException {
		return consume(false);
	}
	
	/**
	 * Interface method implementation
	 * @see MessageConsumer#closeConnections()
	 */
	public void closeConnections() throws MessagingException {
		for (int i = 0; i < this.rabbitConnectionHolders.length; i++) {
			if (this.rabbitConnectionHolders[i] != null && this.rabbitConnectionHolders[i].isValid()) {
				this.rabbitConnectionHolders[i].closeConnection();
				this.rabbitConnectionHolders[i] = null;
			}
		}
	}

	/**
	 * Interface method implementation. Returns the queue depth of the first usable configuration 
	 * @see MessageConsumer#getQueueDepth()
	 */
	public int getQueueDepth() throws MessagingException {
		int noOfQueues = rabbitMQConfigurations.size();
		int attempt = 0;
		RabbitMQConfiguration lastUsedConfiguration = null;
		while (attempt < noOfQueues) {
			int connectionIndex = (int)(totNoOfMessagesConsumed % noOfQueues);
			RabbitMQConfiguration rabbitMQConfiguration = lastUsedConfiguration = rabbitMQConfigurations.get(connectionIndex);

			RabbitConnectionHolder connectionHolder = this.rabbitConnectionHolders[connectionIndex];
			try {
				if (connectionHolder == null || !connectionHolder.isValid()) { // don't synchronize here as all calls will require monitor acquisition
					synchronized(rabbitMQConfiguration) { // synchronized to make connection creation for the configuration a thread-safe operation. 
						// check after monitor acquisition in order to ensure that multiple threads do not create
						// a connection for the same configuration. 
						if (connectionHolder == null || !connectionHolder.isValid()) { 
							connectionHolder = new RabbitConnectionHolder(rabbitMQConfiguration);
							connectionHolder.createConnection();
						}
					}
				}
				int count = connectionHolder.getMessageCount();
				this.rabbitConnectionHolders[connectionIndex] = connectionHolder; // the connection holder is working. set it to the array
				return count;
			} catch (Exception e) {
				LOGGER.error("Error while initializing Rabbit connection / getting message count. Will try others. Error is : " + e.getMessage(), e);
				this.rabbitConnectionHolders[connectionIndex] = null; // the connection holder is not working. Reset it so that in can be recreated for the next call
				continue;
			} finally {
				attempt++; // continue to try with the next configuration
				totNoOfMessagesConsumed++; // increment the count though the message might have failed. Used in determining the configuration in round-robin					
			}
		}
		throw new MessagingException("Error while getting queue depth. All configurations failed!. Last failed configuration : " + lastUsedConfiguration);
	}
	
	/**
	 * Interface method implementation. Calls the {@link #closeConnections()} method
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		this.closeConnections();
	}
	
	/**
	 * Helper method to consume message from queue and convert it to appropriate type - String or generic Object. Applies UTF-8 decoding
	 * if the type is String. Keeps the connection open after message consumption
	 * @param isString determines if the message must be converted to a String
	 * @return a message from the underlying queue
	 * @throws MessagingException in case of errors
	 */
	private Object consume(boolean isString) throws MessagingException {
		Object message = null;
		int noOfQueues = rabbitMQConfigurations.size();
		int attempt = 0;
		RabbitMQConfiguration lastUsedConfiguration = null;
		while (attempt < noOfQueues) {
			int connectionIndex = (int)(totNoOfMessagesConsumed % noOfQueues);
			RabbitMQConfiguration msgPubConfig = lastUsedConfiguration = rabbitMQConfigurations.get(connectionIndex);

			RabbitConnectionHolder connectionHolder = this.rabbitConnectionHolders[connectionIndex];
			if (connectionHolder == null || !connectionHolder.isValid()) { // don't synchronize here as all calls will require monitor acquisition
				try {
					synchronized(msgPubConfig) { // synchronized to make connection creation for the configuration a thread-safe operation. 
						// check after monitor acquisition in order to ensure that multiple threads do not create
						// a connection for the same configuration. 
						if (connectionHolder == null || !connectionHolder.isValid()) { 
							connectionHolder = new RabbitConnectionHolder(msgPubConfig);
							connectionHolder.createConnectionAndConsumer();
						}
					}
				} catch (Exception e) {
					LOGGER.error("Error while initializing Rabbit connection. Will try others. Error is : " + e.getMessage(), e);
					// continue to try with the next configuration
					attempt++;
					totNoOfMessagesConsumed++; // increment the count though the message failed. Used in determining the configuration in round-robin
					continue;
				} 
			}
			try {				
			    QueueingConsumer.Delivery delivery = getWaitTimeoutMillis() > 0 ? connectionHolder.getConsumer().nextDelivery(getWaitTimeoutMillis()) 
			    		: connectionHolder.getConsumer().nextDelivery();
			    if (delivery != null) { // check for null - possible in case of a timeout
				    message = isString ? new String(delivery.getBody(), ENCODING): PlatformUtils.toObject(delivery.getBody());
				    if (!msgPubConfig.isNoAck()) { // Client is expected to ack explicitly, else donot as per AMQP spec
				    	connectionHolder.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(),false);
				    }
			    } 
				// Check if the connection holder is working and has returned a message. Set it to the array. This assignment does not happen
		    	// if there are no messages in the queue and means that the connection is recreated the next time this method is called.
		    	// Essentially we only keep connections when a message is successfully retrieved from the queue.
				this.rabbitConnectionHolders[connectionIndex] = (message != null ? connectionHolder : null);
				if (message == null) { 
					continue; // try other configurations
				} else {
					return message;
				}
			} catch (Exception e) {
				this.rabbitConnectionHolders[connectionIndex] = null; // the connection holder is not working. Remove from array
				LOGGER.error("Error while consuming message from queue. Will try other configurations. Error is : " + e.getMessage(), e);
			} finally {
				attempt++; // try other configurations
				totNoOfMessagesConsumed++; // increment the count though the message consumption failed. Used in determining the configuration in round-robin				
			}
		}
		throw new MessagingException("No messages available for consumption in queue. All configurations failed!. Last failed configuration : " + lastUsedConfiguration);		
	}
	
}
