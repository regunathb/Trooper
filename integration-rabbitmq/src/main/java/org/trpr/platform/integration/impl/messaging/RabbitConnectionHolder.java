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

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.integration.spi.messaging.MessagingException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * The <code>RabbitConnectionHolder</code> class is a convenience class for creating and holding RabbitMQ connection related objects.
 * This utility class is used by {@link RabbitMQMessagePublisherImpl} , {@link RabbitMQMessageConsumerImpl} and the {@link RabbitMQRPCClientImpl}  
 * 
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */

public class RabbitConnectionHolder implements ShutdownListener {
	
	/**
	 * The Logger instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(RabbitConnectionHolder.class);
	
	/** Connection related objects*/
	private Connection conn;
	private Channel channel;
	private QueueingConsumer consumer;
	
	/** The RPC replyTo queue name if this class was created with RabbitMQRpcConfiguration */
	private String rpcReplyToQueueName;
	
	/** RabbitMQ configuration details*/
	private RabbitMQConfiguration rabbitMQConfiguration;

	/** RabbitMQ configuration details for RPC interaction*/
	private RabbitMQRpcConfiguration rabbitMQRpcConfiguration;
	
	/**
	 * Constructor for a connection holder using full-fledged RabbitMQConfiguration details
	 * @param rabbitMQConfiguration the RabbitMQConfiguration containing connection details
	 */
	public RabbitConnectionHolder(RabbitMQConfiguration rabbitMQConfiguration) {
		this.rabbitMQConfiguration = rabbitMQConfiguration;
	}

	/**
	 * Constructor for a connection holder using RPC RabbitMQRPCConfiguration details
	 * @param rabbitMQConfiguration the RabbitMQRPCConfiguration containing connection details
	 */
	public RabbitConnectionHolder(RabbitMQRpcConfiguration rabbitMQRpcConfiguration) {
		this.rabbitMQRpcConfiguration = rabbitMQRpcConfiguration;
	}
	
	/**
	 * Creates the connection objects using the RabbitMQConfiguration held by this class
	 * @throws MessagingException in case of errors
	 */
	public void createConnection() throws MessagingException {
		this.createConnection(false); //  do not use the disableTX override. Let durability settings drive TX behavior instead
	}
	
	/**
	 * Creates the connection objects using the RabbitMQConfiguration held by this class
	 * @param disableTX when true, does not enable TX on the channel (usually the recommended case for Message consumers) even for durable queues
	 * @throws MessagingException in case of errors
	 */
	public void createConnection(boolean disableTX) throws MessagingException {
		
		if (this.rabbitMQRpcConfiguration != null) {
			this.createConnection(this.rabbitMQRpcConfiguration); // set up a RPC style connection
			return;
		}
		
		if (this.rabbitMQConfiguration != null) {
			
			this.createConnection(this.rabbitMQConfiguration); // set up a full-fledged connection			
			
			synchronized (this) { // all code blocks that mutate the connection and channel objects held by this class are synchronized
			
				try {
					// Setting the pre-fetch value as 1. This is set to achieve message distribution across multiple consumers. 
					// The value "1" is set as default for the RabbitMQMessageConsumerImpl that uses this class. 
					// So any application using the RabbitMQMessageConsumerImpl will have prefetch value set as "1".
					this.channel.basicQos(1);
					// enable TX mode on the channel if messages are durable and the disableTX override flag has not been set (typically for message consumers) and
					// if no TX disabling override has been specified
					if (rabbitMQConfiguration.isDurable() && !rabbitMQConfiguration.isDisableTX() && !disableTX) {
						this.channel.txSelect();
					}
					this.channel.queueDeclare(rabbitMQConfiguration.getQueueName(),rabbitMQConfiguration.isDurable(),false,false,null);	
					
					this.channel.queueBind(rabbitMQConfiguration.getQueueName(),
							rabbitMQConfiguration.getExchangeName(),
							rabbitMQConfiguration.getRoutingKey());
				} catch (Exception e) {
					LOGGER.error("Error initializing RabbitMQ connection : " + e.getMessage() + rabbitMQConfiguration.toString(), e);
					throw new MessagingException(
							"Error initializing RabbitMQ connection : " + e.getMessage()); //not passing the root cause as it is logged here
				}
			}
		}
		LOGGER.info("Connection created for configuration : " + this.rabbitMQConfiguration.toString());
	}	
	
	/**
	 * Creates the connection objects, including a Consumer, using the RabbitMQConfiguration held by this class
	 * @throws MessagingException in case of errors
	 */
	public void createConnectionAndConsumer() throws MessagingException {
		this.createConnection(true); // set the disable TX override to true as this is clearly intended for use by a consumer
		this.createConsumer();
	}
	
	/**
	 * Creates only the Consumer. Throws an exception if the Connection was not created already
	 * @throws MessagingException in case of errors or if the Connection is null
	 * @see RabbitConnectionHolder#createConnection()
	 */
	public void createConsumer() throws MessagingException {
		if (this.rabbitMQRpcConfiguration != null) {
			// set up the reply queue
			setupReplyQueue(this.rabbitMQRpcConfiguration);
			this.createConsumer(this.rabbitMQRpcConfiguration, this.rpcReplyToQueueName, true); // set up a RPC style consumer
			return;
		}		
		if (this.rabbitMQConfiguration != null) {
			this.createConsumer(this.rabbitMQConfiguration, 
					this.rabbitMQConfiguration.getQueueName(), this.rabbitMQConfiguration.isNoAck()); // set up a regular consumer
		}			
	}
	
	/**
	 * Returns the Channel created by this class
	 * @return the Channel instance
	 */
	public Channel getChannel() {
		return this.channel;
	}
	
	/**
	 * Returns the queue name that the Consumer(if created using {@link #createConnectionAndConsumer()} or {@link #createConsumer()}) is listening to.
	 * Returns the RPC "replyTo" queue in case the instance of this class was created with {@link RabbitMQRpcConfiguration},
	 * else {@link RabbitMQConfiguration#getQueueName()} when created with {@link RabbitMQConfiguration}
	 * @return queue name as String - 
	 */
	public String getConsumerQueueName() {
		return (this.rabbitMQRpcConfiguration != null ? this.rpcReplyToQueueName : this.rabbitMQConfiguration.getQueueName());
	}
	
	/**
	 * Returns the number of messages contained in the queue. Note that this method can return a count that is less than the actual number of
	 * messages in the queue if invoked after a call to {@link RabbitConnectionHolder#createConnectionAndConsumer()}. The delta is determined
	 * by the QoS i.e. pre-fetch count set on the channel instance created.
	 * Users of this method are required to handle exceptions thrown by Rabbit. This behavior is to enable clients to provide appropriate
	 * behavior - like retry, one time error etc.
	 * @return the message count in queue
	 * @throws Exception if there are exceptions in the underlying Rabbit API calls.
	 */
	public int getMessageCount() throws Exception {
		try{
			return channel.queueDeclare(rabbitMQConfiguration.getQueueName(),
					rabbitMQConfiguration.isDurable(),false,false,null).getMessageCount();	
		} catch (IOException e) {
			LOGGER.error("Error retrieving message count for queue. Returning 0. Configuration is : " + rabbitMQConfiguration);
		}
		return 0;
	}
	
	/**
	 * Returns the QueueingConsumer created by this class
	 * @return the QueueingConsumer instance
	 */
	public QueueingConsumer getConsumer() {
		return this.consumer;
	}
	
	/**
	 * Closes the connection related objects of this class
	 * @throws MessagingException in case of errors
	 */
	public void closeConnection() throws MessagingException {
		try {
			synchronized (this) { // all code blocks that mutate the connection and channel object references held by this class are synchronized
				if (this.consumer != null) {
					this.channel.basicCancel(this.consumer.getConsumerTag());
					this.consumer = null;
				}
				if (this.channel != null) {
					this.channel.close();
					this.channel = null;
				}
				if (this.conn != null) {
					this.conn.close();
					this.conn = null;
				}
			}
		} catch (IOException e) {
			LOGGER.error("Error while closing resources for : " + rabbitMQConfiguration.toString(), e);
			throw new MessagingException(
					"Error while closing resources for : " + rabbitMQConfiguration.toString());//not passing the root cause as it is logged here
		}
	}

	/**
	 * Interface method implementation. Resets the connection and channel held by this holder if the shutdown signal is received
	 * @see com.rabbitmq.client.ShutdownListener#shutdownCompleted(com.rabbitmq.client.ShutdownSignalException)
	 */
	public void shutdownCompleted(ShutdownSignalException sse) {
		LOGGER.info("Connection terminated for configuration : " + this.rabbitMQConfiguration.toString());
		synchronized (this) { // all code blocks that mutate the connection and channel object references held by this class are synchronized
			this.conn = null;
			this.channel = null;
			this.consumer = null;
		}
	}
	
	/**
	 * Checks if this connection holder is holding on to a valid connection
	 * @return boolean true if the connection and channel are still valid, false otherwise
	 */
	public boolean isValid() {
		return (this.conn != null && this.channel != null); // not synchronizing this access as this can get called multiple times and across threads
	}
	
	/**
	 * Helper method to create RabbitMQ {@link Connection} and {@link Channel} for the specified {@link RabbitMQRpcConfiguration}
	 * @param configuration the RabbitMQRpcConfiguration or one of its sub-types to create connection objects for
	 * @throws MessagingException in case of errors during connection & channel creation
	 */
	private void createConnection(RabbitMQRpcConfiguration configuration) throws MessagingException {
		
		synchronized (this) { // all code blocks that mutate the connection and channel objects held by this class are synchronized
			
			ConnectionFactory factory = new ConnectionFactory();
			factory.setUsername(configuration.getUserName());
			factory.setPassword(configuration.getPassword());
			factory.setVirtualHost(configuration.getVirtualHost());
			factory.setRequestedHeartbeat(configuration.getRequestHeartBeat());
			factory.setHost(configuration.getHostName());
			factory.setPort(configuration.getPortNumber());
			
			try {
				// create the connection
				this.conn = factory.newConnection();
				// add a shutdown listener to the newly created connection
				this.conn.addShutdownListener(this);
				// create the channel
				this.channel = this.conn.createChannel();
				this.channel.exchangeDeclare(
						configuration.getExchangeName(), 
						configuration.getExchangeType(),
						configuration.isDurable());
				
			} catch (Exception e) {
				LOGGER.error("Error initializing RabbitMQ connection for : " + configuration.toString(), e);
				throw new MessagingException(
						"Error initializing RabbitMQ connection for : " + configuration.toString()); //not passing the root cause as it is logged here
			}
		}
		
	}
	
	/**
	 * Helper method for creating the Consumer for the specified {@link RabbitMQRpcConfiguration}
	 * @param rabbitMQRpcConfiguration the RabbitMQRpcConfiguration or one of its sub-types to create consumer for
	 * @param queueName the name of the queue that the Consumer is connected to
	 * @param noAck is auto message acking turned on
	 */
	private void createConsumer(RabbitMQRpcConfiguration rabbitMQRpcConfiguration, String queueName, boolean noAck) {
		synchronized (this) { // all code blocks that mutate the connection and channel objects held by this class are synchronized
			if (this.getChannel() == null) {
				throw new MessagingException("Attempt to create Consumer before calling RabbitConnectionHolder#createConnection(). Consumer will not be created.");
			}
			this.consumer = new QueueingConsumer(this.getChannel());
			try {
				this.getChannel().basicConsume(queueName, noAck, this.consumer);
			} catch (IOException ioe) {
				LOGGER.error("Error setting up consumer on channel for : " + rabbitMQRpcConfiguration.toString(), ioe);
				throw new MessagingException("Error setting up consumer on channel for : " + rabbitMQRpcConfiguration.toString()); //not passing the root cause as it is logged here
			}
		}		
	}
		
	/**
	 * Creates a server-named exclusive autodelete queue to use for receiving replies to RPC requests.
	 * @param rabbitMQRpcConfiguration RabbitMQRpcConfiguration containing channel connection details
	 * @throws MessagingException if an error is encountered
	 */
	private void setupReplyQueue(RabbitMQRpcConfiguration rabbitMQRpcConfiguration) throws MessagingException {
		try {
			this.rpcReplyToQueueName = this.getChannel().queueDeclare("", false, true, true, null).getQueue();
		} catch (IOException ioe) {
			LOGGER.error("Error setting up RPC reply queue on channel for : " + rabbitMQRpcConfiguration.toString(), ioe);
			throw new MessagingException("Error setting up RPC reply queue on channel for : " + rabbitMQRpcConfiguration.toString()); //not passing the root cause as it is logged here
		}
	}
	
}

