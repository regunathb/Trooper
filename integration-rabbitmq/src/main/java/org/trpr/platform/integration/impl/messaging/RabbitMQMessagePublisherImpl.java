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

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.DisposableBean;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.util.PlatformUtils;
import org.trpr.platform.integration.spi.messaging.MessagePublisher;
import org.trpr.platform.integration.spi.messaging.MessagingException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.MessageProperties;

/**
 * The <code>RabbitMQMessagePublisherImpl</code> class is an implementation of {@link MessagePublisher} that uses RabbitMQ as the underlying messaging
 * provider. This implementation is compatible with Java client API of RabbitMQ version 2.2.0. Backward/Forward compatibility with other versions 
 * requires verification.
 * 
 * This publisher will exhaust all {@link RabbitMQConfiguration} instances when trying to publish a message and fails only when every one of the
 * configurations fail to connect or fail otherwise.
 * 
 * This class implements the Spring {@link org.springframework.beans.factory.DisposableBean} and calls {@link #closeConnections()} method to 
 * cleanup connections when the application context is torn down. The dependence on Spring is justified by the need to close connections cleanly
 * during application shutdown.
 * 
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */
public class RabbitMQMessagePublisherImpl implements MessagePublisher, DisposableBean {
	
	/** Constant for the String literal UTF-8*/
	private static final String ENCODING = "UTF-8";
	
	/**
	 * The Logger instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(RabbitMQMessagePublisherImpl.class);

	/**
	 * List of RabbitMQ configurations available for this message publisher
	 */
	private List<RabbitMQConfiguration> rabbitMQConfigurations;
	
	/**
	 * Array of RabbitConnectionHolder instances equalling the size of rabbitMQConfigurations.
	 */
	private RabbitConnectionHolder[] rabbitConnectionHolders;
	
	/**
	 * Tracks total number of messages queued for each instantiation of this publisher class
	 */
	private long totNoOfMessagesQueued;
	
	/**
	 * No-args constructor to initialize member variables.
	 */
	public RabbitMQMessagePublisherImpl()	{
	}
	
	/**
	 * Interface method implementation. Does nothing 
	 * @see MessagePublisher#initialize()
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
	/** == End Spring DI style Getters and setters methods definition. */

	/**
	 * Interface method implementation. Converts the specified String to a UTF-8 encoded byte array and publishes it.
	 * @see MessagePublisher#publishString(String)
	 */
	public void publishString(String string) throws MessagingException {
		this.publish(string);
	}
		
	/**
	 * Interface method implementation. Converts the specified Object to a raw byte array and publishes it.
	 * @see MessagePublisher#publish(Object)
	 */
	public void publish(Object message) throws MessagingException {
		validateMessage(message);
		publishWithRoundRobinPolicy(message);
	}

	protected int publishWithRoundRobinPolicy(Object message) throws MessagingException
    {
	    int noOfQueues = rabbitMQConfigurations.size();
		int attempt = 0;
		RabbitMQConfiguration lastUsedConfiguration = null;
		while (attempt < noOfQueues) {
			int connectionIndex = (int)(totNoOfMessagesQueued % noOfQueues);
			RabbitMQConfiguration rabbitMQConfiguration = lastUsedConfiguration = rabbitMQConfigurations.get(connectionIndex);

			if (this.rabbitConnectionHolders[connectionIndex] == null || !this.rabbitConnectionHolders[connectionIndex].isValid()) { // don't synchronize here as all calls will require monitor acquisition
				try {
					validateAndInitConnection(connectionIndex, rabbitMQConfiguration);
				} catch (Exception e) {
					LOGGER.error("Error while initializing Rabbit connection." + "\n" + "Failed Configuration is " + rabbitMQConfigurations.get(connectionIndex)+ "\n" + "Will try other configurations. Error is : " + e.getMessage(), e);
					// continue to try with the next configuration
					attempt++;
					totNoOfMessagesQueued++; // increment the count though the connection create failed. Used in determining the configuration in round-robin
					continue;
				} 
			}
			try {
				publishToConnection(message, connectionIndex);
				return connectionIndex;
			} catch (Exception e) {
				LOGGER.error("Error while publishing message into queue. Failed Configuration is " + rabbitMQConfigurations.get(connectionIndex)+ "\n" + "Will try other configurations. Error is : " + e.getMessage(), e);				
				this.rabbitConnectionHolders[connectionIndex] = null; // the connection holder is not working. Remove from array
			} finally {
				attempt++;
				totNoOfMessagesQueued++; // increment the count though the message publish might have failed. Used in determining the configuration in round-robin
			}
		}
		throw new MessagingException("Error while publishing message into queue. All configurations failed!. Last failed configuration : " + lastUsedConfiguration);
    }

	protected void validateMessage(Object message)
    {
	    if (null == message) {
			throw new MessagingException("Message parameter cannot be null");
		}
    }

	/**
	 * Checks if the connection is for the configuration is null or invalid. If yes then creates a new connection as per the configuration
	 * @param connectionIndex
	 * @param rabbitMQConfiguration
	 */
	private void validateAndInitConnection(int connectionIndex, RabbitMQConfiguration rabbitMQConfiguration)
    {
	    synchronized(rabbitMQConfiguration) { // synchronized to make connection creation for the configuration a thread-safe operation. 
	    	// check after monitor acquisition in order to ensure that multiple threads do not create
	    	// a connection for the same configuration. \
	    	if (this.rabbitConnectionHolders[connectionIndex] == null || !this.rabbitConnectionHolders[connectionIndex].isValid()) //Added code to check if connection is valid ... otherwise create a new connection
	    	{ 
	    		this.rabbitConnectionHolders[connectionIndex] = new RabbitConnectionHolder(rabbitMQConfiguration);
	    		this.rabbitConnectionHolders[connectionIndex].createConnection();
	    	}
	    }
    }

	/**
	 * Publishes on a provided connection as per the connection configuration index. If the connection is null or if publishing fails it throws an Exception.
	 * @param message
	 * @param connectionIndex
	 * @throws Exception
	 */
	protected void publishToConnection(Object message, int connectionIndex) throws Exception {
		RabbitMQConfiguration rabbitMQConfiguration = rabbitMQConfigurations.get(connectionIndex); 

	    if(this.rabbitConnectionHolders[connectionIndex] == null)
	    {
	    	throw new MessagingException("Connection not initialized");
	    }
	    
	    boolean isMessageOfTypeString = (message instanceof String);
	    byte[] body = isMessageOfTypeString ? ((String)message).getBytes(ENCODING) : PlatformUtils.toBytes(message);
	    AMQP.BasicProperties msgProps = rabbitMQConfiguration.isDurable() ? 
	    		(isMessageOfTypeString ? MessageProperties.PERSISTENT_TEXT_PLAIN : MessageProperties.PERSISTENT_BASIC) : 
	    			(isMessageOfTypeString ? MessageProperties.TEXT_PLAIN : MessageProperties.BASIC); 

	    if (rabbitMQConfiguration.isDurable()) {
	    	synchronized(this.rabbitConnectionHolders[connectionIndex].getChannel()) {
	        	// synchronized on the channel to avoid the below RabbitMQ client exception, caused in multi-threaded execution using the same channel:
	        	// java.lang.IllegalStateException: cannot execute more than one synchronous AMQP command at a time
	    		this.rabbitConnectionHolders[connectionIndex].getChannel().basicPublish(
	    				rabbitMQConfiguration.getExchangeName(), 
	    				rabbitMQConfiguration.getRoutingKey(), 
	    				msgProps, 
	    				body);					
	    		// Commit the message if it is durable and the commit count is reached. 
	    		// The channel should and would be in txSelect mode when it was created using the RabbitMQConfiguration details
	    		// increment totNoOfMessagesQueued by 1 and check as it is post incremented after publishing the message
	    		if ((totNoOfMessagesQueued + 1) % rabbitMQConfiguration.getDurableMessageCommitCount() == 0) {
	    			if (rabbitMQConfiguration.isDisableTX()) {
	    				// error out, as explicitly disabling TX will not make the message durable
	    				LOGGER.error("Configuration conflict. TX disabled for message publishing on durable queue. Message will not be published.");
	    				return;
	    			}					
	    			this.rabbitConnectionHolders[connectionIndex].getChannel().txCommit();
	    		}
	    	}
	    } else {
	    	this.rabbitConnectionHolders[connectionIndex].getChannel().basicPublish(
	    			rabbitMQConfiguration.getExchangeName(), 
	    			rabbitMQConfiguration.getRoutingKey(), 
	    			msgProps, 
	    			body);					
	    }
    }

	/**
	 * Interface method implementation.
	 * @see MessagePublisher#closeConnections()
	 */
	public void closeConnections() throws MessagingException {
		for (int i = 0; i < this.rabbitConnectionHolders.length; i++) {
			if (this.rabbitConnectionHolders[i] != null && this.rabbitConnectionHolders[i].isValid()) {
				// Commit any non-committed messages if it is durable and TX has not been disabled.
				// This is needed for persisting messages when configured commit count is not reached and the client closes connections
				if (rabbitMQConfigurations.get(i).isDurable() && !rabbitMQConfigurations.get(i).isDisableTX()) {
					try {
			        	// synchronized on the channel to avoid the below RabbitMQ client exception, caused in multi-threaded execution using the same channel:
			        	// java.lang.IllegalStateException: cannot execute more than one synchronous AMQP command at a time
			        	synchronized(this.rabbitConnectionHolders[i].getChannel()) {
			        		this.rabbitConnectionHolders[i].getChannel().txCommit();
			        	}
					} catch (IOException e) {
						// Can't do much except log the error
						LOGGER.error ("Error committing remaining durable messages. Messages will be lost. Continuing to close connection for this configuration : " + rabbitMQConfigurations.get(i));
					}
				}			
				this.rabbitConnectionHolders[i].closeConnection();
				this.rabbitConnectionHolders[i] = null;
			}
		}
	}
	
	/**
	 * Interface method implementation. Returns the queue depth of the first usable configuration 
	 * @see MessagePublisher#getQueueDepth()
	 */
	public int getQueueDepth() throws MessagingException {
		int noOfQueues = rabbitMQConfigurations.size();
		int attempt = 0;
		RabbitMQConfiguration lastUsedConfiguration = null;
		while (attempt < noOfQueues) {
			int connectionIndex = (int)(totNoOfMessagesQueued % noOfQueues);
			RabbitMQConfiguration RabbitMQConfiguration = lastUsedConfiguration = rabbitMQConfigurations.get(connectionIndex);

			try {
				if (this.rabbitConnectionHolders[connectionIndex] == null || !this.rabbitConnectionHolders[connectionIndex].isValid()) { // don't synchronize here as all calls will require monitor acquisition
					validateAndInitConnection(connectionIndex, RabbitMQConfiguration);
				}
				int count = this.rabbitConnectionHolders[connectionIndex].getMessageCount();
				return count;
			} catch (Exception e) {
				LOGGER.error("Error while initializing Rabbit connection / getting message count. Will try others. Error is : " + e.getMessage(), e);
				this.rabbitConnectionHolders[connectionIndex] = null; // the connection holder is not working. Reset it so that in can be recreated for the next call
				continue;
			} finally {
				attempt++; // continue to try with the next configuration
				totNoOfMessagesQueued++; // increment the count though the message might have failed. Used in determining the configuration in round-robin					
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
	 * Gets ConnectionHolder instances for each of the RabbitMQConfiguration configured on this publisher
	 * @return array of initialized RabbitConnectionHolder instances
	 */
	public RabbitConnectionHolder[] getRabbitConnectionHolders() {
		for (int i = 0; i < this.rabbitConnectionHolders.length; i++) {
			if (this.rabbitConnectionHolders[i] == null || !this.rabbitConnectionHolders[i].isValid()) {
				try {
					synchronized(this.rabbitMQConfigurations.get(i)) { // synchronized to make connection creation for the configuration a thread-safe operation. 
						// check after monitor acquisition in order to ensure that multiple threads do not create
						// a connection for the same configuration. 
						if (this.rabbitConnectionHolders[i] == null) { 
							this.rabbitConnectionHolders[i] = new RabbitConnectionHolder(this.rabbitMQConfigurations.get(i));
							this.rabbitConnectionHolders[i].createConnection();
						}
					}
				} catch (MessagingException e) {
					LOGGER.error("Error initiazlizing Rabbit connection. Connection not available for configuration : " + this.rabbitMQConfigurations.get(i), e);
				}
			}
		}
		return this.rabbitConnectionHolders;
	}
}
