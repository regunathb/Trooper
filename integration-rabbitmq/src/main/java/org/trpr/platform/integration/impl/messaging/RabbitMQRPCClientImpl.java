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
import org.trpr.platform.integration.spi.messaging.MessagingException;
import org.trpr.platform.integration.spi.messaging.MessagingTimeoutException;
import org.trpr.platform.integration.spi.messaging.RpcClient;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;

/**
 * The <code>RabbitMQRPCClientImpl</code> class is an implementation of {@link RpcClient} that uses RabbitMQ as the underlying messaging
 * provider. This implementation is compatible with Java client API of RabbitMQ version 2.2.0. Backward/Forward compatibility with other versions 
 * requires verification.
 * 
 * This RPC client will exhaust all {@link RabbitMQRpcConfiguration} instances when trying to publish a message and fails only when every one of the
 * configurations fail to connect or fail otherwise.
 * 
 * This class implements the Spring {@link org.springframework.beans.factory.DisposableBean} and calls {@link #closeConnections()} method to 
 * cleanup connections when the application context is torn down. The dependence on Spring is justified by the need to close connections cleanly
 * during application shutdown.
 * 
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */

public class RabbitMQRPCClientImpl implements RpcClient {

	/** Constant for the String literal UTF-8*/
	private static final String ENCODING = "UTF-8";
	
	/**
	 * The Logger instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(RabbitMQRPCClientImpl.class);

	/**
	 * List of RabbitMQ configurations available for this rpc client
	 */
	private List<RabbitMQRpcConfiguration> rabbitMQRpcConfigurations;
	
	/**
	 * Array of RabbitConnectionHolder instances equalling the size of RabbitMQRPCConfiguration.
	 */
	private RabbitConnectionHolder[] rabbitConnectionHolders;
	
	/**
	 * Tracks total number of messages queued for each instantiation of this rpc client class
	 */
	private long totNoOfMessagesQueued;
	
	/**
	 * No-args constructor to initialize member variables.
	 */
	public RabbitMQRPCClientImpl()	{
	}

	/**
	 * Interface method implementation. Does nothing 
	 * @see RpcClient#initialize()
	 */
	public void initialize() {
		// do nothing as connections are created lazily
	}
	
	/** == Start Spring DI style Getters and setters methods definition. */
	
	public List<RabbitMQRpcConfiguration> getRabbitMQRpcConfigurations() {
		return this.rabbitMQRpcConfigurations;
	}
	public void setRabbitMQRPCConfiguration(List<RabbitMQRpcConfiguration> rabbitMQRpcConfigurations) {
		this.rabbitMQRpcConfigurations = rabbitMQRpcConfigurations;
		// just initialize the array. Don't create the connections yet
		this.rabbitConnectionHolders = new RabbitConnectionHolder[rabbitMQRpcConfigurations.size()];
	}
	/** == End Spring DI style Getters and setters methods definition. */

	/**
	 * Interface method implementation. 
	 * @see RpcClient#sendString(String, int)
	 */
	public String sendString(String message, int timeout) throws MessagingTimeoutException, MessagingException {
		return (String)this.send(message, timeout);
	}

	/**
	 * Interface method implementation
	 * @see RpcClient#send(Object, int)
	 */
	public Object send(Object message, int timeout) throws MessagingTimeoutException, MessagingException {
		if (null == message) {
			throw new MessagingException("Message parameter cannot be null");
		}
		int noOfQueues = rabbitMQRpcConfigurations.size();
		int attempt = 0;
		RabbitMQRpcConfiguration lastUsedConfiguration = null;
		while (attempt < noOfQueues) {
			int connectionIndex = (int)(totNoOfMessagesQueued % noOfQueues);
			RabbitMQRpcConfiguration rabbitMQRpcConfiguration = lastUsedConfiguration = rabbitMQRpcConfigurations.get(connectionIndex);

			RabbitConnectionHolder connectionHolder = this.rabbitConnectionHolders[connectionIndex];
			if (connectionHolder == null || !connectionHolder.isValid()) { // don't synchronize here as all calls will require monitor acquisition
				try {
					synchronized(rabbitMQRpcConfiguration) { // synchronized to make connection creation for the configuration a thread-safe operation. 
						// check after monitor acquisition in order to ensure that multiple threads do not create
						// a connection for the same configuration. 
						if (connectionHolder == null || !connectionHolder.isValid()) { 
							connectionHolder = new RabbitConnectionHolder(rabbitMQRpcConfiguration);
							connectionHolder.createConnection();
						}
					}
				} catch (Exception e) {
					LOGGER.error("Error while initializing Rabbit connection. Will try others. Error is : " + e.getMessage(), e);
					// continue to try with the next configuration
					attempt++;
					totNoOfMessagesQueued++; // increment the count though the connection create failed. Used in determining the configuration in round-robin
					continue;
				} 
			}
			try {
				boolean isMessageOfTypeString = (message instanceof String);
				byte[] body = isMessageOfTypeString ? ((String)message).getBytes(ENCODING) : PlatformUtils.toBytes(message);
				AMQP.BasicProperties msgProps = new AMQP.BasicProperties("text/plain", null, null, 1,
	                    						null, null,connectionHolder.getConsumerQueueName(), null, null, null,null, null, null, null); 
				connectionHolder.getChannel().basicPublish(
						rabbitMQRpcConfiguration.getExchangeName(), 
						rabbitMQRpcConfiguration.getRoutingKey(), 
						msgProps, 
						body);
				
				QueueingConsumer.Delivery delivery = connectionHolder.getConsumer().nextDelivery(timeout);

				if (delivery != null) {
					// the connection holder is working. set it to the array
					this.rabbitConnectionHolders[connectionIndex] = connectionHolder;
					return (isMessageOfTypeString ? new String(delivery.getBody(), ENCODING): PlatformUtils.toObject(delivery.getBody()));
				} else {
					throw new MessagingTimeoutException(timeout);
				}
				
			} catch (Exception e) {
				this.rabbitConnectionHolders[connectionIndex] = null; // the connection holder is not working. Remove from array
				LOGGER.error("Error while publishing message into queue. Will try other configurations. Error is : " + e.getMessage(), e);				
			} finally {
				attempt++;
				totNoOfMessagesQueued++; // increment the count though the message publish might have failed. Used in determining the configuration in round-robin
			}
		}
		throw new MessagingException("Error while publishing message into queue. All configurations failed!. Last failed configuration : " + lastUsedConfiguration);
	}

	/**
	 * Interface method implementation
	 * @see org.trpr.platform.integration.spi.messaging.RpcClient#closeConnections()
	 */
	public void closeConnections() throws MessagingException {
		for (int i = 0; i < this.rabbitConnectionHolders.length; i++) {
			if (this.rabbitConnectionHolders[i] != null && this.rabbitConnectionHolders[i].isValid()) {
				this.rabbitConnectionHolders[i].closeConnection();
				this.rabbitConnectionHolders[i] = null;
			}
		}
	}
	
}
