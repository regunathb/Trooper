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
		return (String)consumeWithRoundRobinPolicy(true).message;
	}

	/**
	 * Interface method implementation. 
	 * @see MessageConsumer#consume()
	 */
	public Object consume() throws MessagingException {
		return consumeWithRoundRobinPolicy(false).message;
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
			try {
				if (this.rabbitConnectionHolders[connectionIndex] == null || !this.rabbitConnectionHolders[connectionIndex].isValid()) { // don't synchronize here as all calls will require monitor acquisition
					validateAndInitConnection(connectionIndex, rabbitMQConfiguration);
				}
				int count = this.rabbitConnectionHolders[connectionIndex].getMessageCount();
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
		throw new MessagingException("Unable to queue depth. All configurations failed!. Last failed configuration : " + lastUsedConfiguration, MessagingException.CONNECTION_FAILURE);
	}
	
	/**
	 * Interface method implementation. Calls the {@link #closeConnections()} method
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	public void destroy() throws Exception {
		this.closeConnections();
	}
	
	/**
	 * Consumes a message from the configured queue and converts it to appropriate type - String or generic Object. Applies UTF-8 decoding
	 * if the type is String. Keeps the connection open after message consumption. Follows a round robin policy for message consumption from all
	 * configurations. If connection is successful it returns the index of the configuration to which connection was successful.
	 * If connection to all provided configurations are unsuccessful then a Messaging Exception is thrown.
	 * @param isString determines if the message must be converted to a String
	 * @return a MessageHolder instance containing a message from the underlying queue and the connection index used to retrieve the message
	 * @throws MessagingException in case of errors
	 */
	protected MessageHolder consumeWithRoundRobinPolicy(boolean isString) throws MessagingException {
		MessageHolder messageHolder = null;
		int noOfQueues = rabbitMQConfigurations.size();
		int attempt = 0;
		RabbitMQConfiguration lastUsedConfiguration = null;
		Throwable consumptionRootCause = null;
		while (attempt < noOfQueues) {
			int connectionIndex = (int)(totNoOfMessagesConsumed % noOfQueues);
			RabbitMQConfiguration msgPubConfig = lastUsedConfiguration = rabbitMQConfigurations.get(connectionIndex);
			if (this.rabbitConnectionHolders[connectionIndex] == null || !this.rabbitConnectionHolders[connectionIndex].isValid()) { // don't synchronize here as all calls will require monitor acquisition
				try {
					validateAndInitConnection(connectionIndex, msgPubConfig);
				} catch (Exception e) {
					LOGGER.error("Error while initializing Rabbit connection. Will try others. Error is : " + e.getMessage(), e);
					// continue to try with the next configuration
					attempt++;
					totNoOfMessagesConsumed++; // increment the count though the message failed. Used in determining the configuration in round-robin
					// set the error root cause 
					consumptionRootCause = e;
					continue;
				} 
			}
			try {			
				messageHolder = consumeFromConnection(isString, connectionIndex);
				if (messageHolder == null) { 
					continue; // try other configurations
				} else {
					return messageHolder;
				}
			} catch (Exception e) {
				this.rabbitConnectionHolders[connectionIndex] = null; // the connection holder is not working. Remove from array
				LOGGER.error("Error while consuming message from queue. Will try other configurations. Error is : " + e.getMessage(), e);
				// set the error root cause
				consumptionRootCause = e;
			} finally {
				attempt++; // try other configurations
				totNoOfMessagesConsumed++; // increment the count though the message consumption failed. Used in determining the configuration in round-robin				
			}
		}
		if (consumptionRootCause != null) { // there is a connection (or) other exception by which we are unable to return a message
			throw new MessagingException("Error consuming message from queue. Last used configuration is : " + lastUsedConfiguration, consumptionRootCause, MessagingException.CONNECTION_FAILURE);
		} else {
			throw new MessagingException("No messages available for consumption in queue.",  MessagingException.QUEUE_EMPTY);
		}						
	}
	
	/**
	 * Consumes a single message from the connection identified by the specified connection index.
	 * @param isString boolean to indicate if the message is an Object or String
	 * @param connectionIndex the connection index identifier
	 * @return MessageHolder containing the consumed message and the connection index
	 * @throws Exception in case of errors from the underlying messaging system 
	 */
	protected MessageHolder consumeFromConnection(boolean isString, int connectionIndex) throws Exception {
		MessageHolder messageHolder = null;
		RabbitMQConfiguration msgPubConfig = rabbitMQConfigurations.get(connectionIndex);
	    QueueingConsumer.Delivery delivery = getWaitTimeoutMillis() > 0 ? this.rabbitConnectionHolders[connectionIndex].getConsumer().nextDelivery(getWaitTimeoutMillis()) 
	    		: this.rabbitConnectionHolders[connectionIndex].getConsumer().nextDelivery();
	    if (delivery != null) { // check for null - possible in case of a timeout
	    	messageHolder = isString ? new MessageHolder(connectionIndex, new String(delivery.getBody(), ENCODING))
	    		: new MessageHolder(connectionIndex,PlatformUtils.toObject(delivery.getBody()));
		    if (!msgPubConfig.isNoAck()) { // Client is expected to ack explicitly, else donot as per AMQP spec
		    	this.rabbitConnectionHolders[connectionIndex].getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(),false);
		    }
	    }			    
		return messageHolder;
	}
	
	/**
	 * Checks if the connection for the configuration is null or invalid. 
	 * If yes then creates a new connection as per the configuration.
	 * @param connectionIndex Index of the configuration and the connection
	 * @param rabbitMQConfiguration Configuration for which the connection needs to be validated
	 */
	private void validateAndInitConnection(int connectionIndex, RabbitMQConfiguration rabbitMQConfiguration) {
	    synchronized(rabbitMQConfiguration) { // synchronized to make connection creation for the configuration a thread-safe operation. 
	    	// check after monitor acquisition in order to ensure that multiple threads do not create
	    	// a connection for the same configuration. \
	    	if (this.rabbitConnectionHolders[connectionIndex] == null || !this.rabbitConnectionHolders[connectionIndex].isValid()) { //Added code to check if connection is valid ... otherwise create a new connection 
	    		this.rabbitConnectionHolders[connectionIndex] = new RabbitConnectionHolder(rabbitMQConfiguration);
	    		this.rabbitConnectionHolders[connectionIndex].createConnection();
	    	}
	    }
    }
	
	/**
	 * Wrapper class for holding the message consumed from queue and the connection index used to consume the message. 
	 */
	class MessageHolder {
		int connectionIndex;
		Object message;
		MessageHolder(int connectionIndex, Object message) {
			this.connectionIndex = connectionIndex;
			this.message = message;
		}
	}
		
}
