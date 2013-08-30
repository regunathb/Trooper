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
 * This implementation is loosely based on the (now defunct) RabbitMQ transport codebase for Mule (version 1.x.x and above) using the 
 * RabbitMQ Java client (version 1.7.1) : http://svn.muleforge.org/mule-transport-rabbitmq/branches/upgrading-to-rabbitmq-client-1.7/
 */

package org.trpr.mule.transport.rabbitmq;

import java.io.IOException;
import java.util.List;

import org.mule.api.MuleException;
import org.mule.api.component.Component;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.service.Service;
import org.mule.api.transport.Connector;
import org.mule.api.transport.ReplyToHandler;
import org.mule.config.i18n.CoreMessages;
import org.mule.transport.AbstractConnector;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * The <code>RabbitConnector</code> is the Mule {@link Connector} implementation for RabbitMQ.
 * This Connector supports the following features :
 * <pre>
 *  - Reconnects for infinite duration by trying periodically to re-establish a failed connection.
 *  - Specifying message commit counts - useful when used with durable end-points.
 *  - Heartbeat interval - useful when connecting to Broker across firewalls that have timeout setting enabled
 * <pre>
 * 
 * @author Regunath
 * @version 1.0, 17/08/2012
 */

public class RabbitConnector extends AbstractConnector {
	
	/** String literals as constants relevant for this Connector*/
    public static final String AMQP = "amqp";
    public static final String RABBIT_MQ_ENVELOPE_PROPERTY = "rabbitmq.envelope";
    public static final String RABBIT_MQ_CONSUMER_TAG_PROPERTY = "rabbitmq.consumerTag";
    
    /** The socket close timeout milliseconds*/
    private static final int CLOSE_TIMEOUT = 1000;
    
	/** The default durable commit count*/
	private static final int DEFAULT_DURABLE_MSG_COMMIT_COUNT = 1;
	
	/** The default pre-fetch count. Negative value to indicate no explicit setting*/
	private static final int DEFAULT_PREFETCH_COUNT = -1;

	/** Connection related variables*/
    private Connection connection;
    private int durableMessageCommitCount = DEFAULT_DURABLE_MSG_COMMIT_COUNT;
    private int prefetchCount = DEFAULT_PREFETCH_COUNT;
    
    private List<RabbitMQConfiguration> rabbitMQConfigurations;
    
	/** Heartbeat interval, in seconds for message request.*/
	private int requestHeartBeat;    

	/** The ReplyToHandler */
    private RabbitReplyToHandler repyToHandler;
    
    private int lastUsedConnectionIndex = -1;

    /**
     * Interface method implementation. Returns the string "amqp" as the supported protocol
     * @see org.mule.api.transport.Connector#getProtocol()
     */
    public String getProtocol() {
        return AMQP;
    }

    /**
     * Creates and returns a ChannelHolder object for the specified endpoint.
     * @param endpoint declared Mule endpoint
     * @return a ChannelHolder instance containing the Connection
     * @throws IOException in case of I/O errors
     * @throws InitialisationException in case of initialization errors
     */
    public synchronized ChannelHolder createChannel(ImmutableEndpoint endpoint)
            throws IOException, InitialisationException {
        Channel channel = connection.createChannel();
        if (channel == null) {
            throw new InitialisationException(
                    CoreMessages.failedToCreate(Channel.class.getName()),
                    null);
        }
        // check if a pre-fetch count has been explicitly set and set it on the channel, else ignore        
        if (this.getPrefetchCount() != RabbitConnector.DEFAULT_PREFETCH_COUNT) {
        	channel.basicQos(this.getPrefetchCount());
        }
        
        // set the newly created channel in txSelect mode if the endpoint is marked as durable and is of 
        // type OutboundEndPoint. TX is not supported for inbound end-points. Acking is preferred mechanism
        // for control over message consumption i.e. in RabbitMessageReceiver
        if (EndpointUtils.isDurable(endpoint) && endpoint instanceof OutboundEndpoint) {
        	channel.txSelect();
        }
        return new ChannelHolder(channel);
    }

    /** The ChannelHolder object*/
    public class ChannelHolder {
        private Channel channel;
        public ChannelHolder(Channel channel) {
            this.channel = channel;
        }
        public Channel getChannel() {
            return channel;
        }
    }
    
    /**
     * Gets the receiver identifier key. Returns a unique key constructed from the service's name and the enpoint's URI
     * @see org.mule.transport.AbstractConnector#getReceiverKey(org.mule.api.service.Service, org.mule.api.endpoint.InboundEndpoint)
     */
    protected Object getReceiverKey(Service service, InboundEndpoint endpoint) {
    	/*
    	 * Not using the service attributes results in an error like : There is already a listener registered on this connector on endpointUri: amqp://enrol:direct/?queue=input2
    	 * Returns a receiverkey that is unique to each configuration of an endpoint URI instead
    	 */
    	//return endpoint.getEndpointURI().getAddress() + ":" + endpoint.getEndpointURI().getResourceInfo();
    	return service.getName() + "~" + endpoint.getEndpointURI().getAddress();
    }

    /**
     * Abstract method implementation. Initializes this Connector and instantiates the ReplyToHandler
     * @see org.mule.transport.AbstractConnector#doInitialise()
     */
    protected void doInitialise() throws InitialisationException {
        repyToHandler = new RabbitReplyToHandler();
    }

    /**
     * Abstract method implementation. Closes the connection when this Connector is disposed.
     * @see org.mule.transport.AbstractConnector#doDispose()
     */
    protected synchronized void doDispose() {
        closeConnection();
    }

    /**
     * Abstract method implementation. Does nothing.
     * @see org.mule.transport.AbstractConnector#doStart()
     */
    protected void doStart() throws MuleException {
    	// no op
    }

    /**
     * Abstract method implementation. Does nothing.
     * @see org.mule.transport.AbstractConnector#doStop()
     */
    protected void doStop() throws MuleException {
    	// no op
    }

    /**
     * Abstract method implementation. Creates the AMQP connection
     * @see org.mule.transport.AbstractConnector#doConnect()
     */
    protected void doConnect() throws Exception {
		if(connection == null) {
			int totalNumberOfNodes = rabbitMQConfigurations.size(); int tries = 0; 
			while(tries <= totalNumberOfNodes) {
				lastUsedConnectionIndex = (lastUsedConnectionIndex + 1)%totalNumberOfNodes;
                RabbitMQConfiguration rabbitMQConfiguration = null;
				try {
                    ConnectionFactory factory = new ConnectionFactory();
                    rabbitMQConfiguration = rabbitMQConfigurations.get(lastUsedConnectionIndex);
            		factory.setUsername(rabbitMQConfiguration.getUserName());
            		factory.setPassword(rabbitMQConfiguration.getPassword());
            		factory.setVirtualHost(rabbitMQConfiguration.getVirtualHost());
            		factory.setRequestedHeartbeat(rabbitMQConfiguration.getRequestHeartBeat());
            		factory.setHost(rabbitMQConfiguration.getHostName());
            		factory.setPort(rabbitMQConfiguration.getPortNumber());
                    connection = factory.newConnection();
                    logger.info("Connection successfully created to configuration = " + rabbitMQConfiguration);
                    return;
				}
				catch(Exception e) {
					logger.info("Failed to connect to Rabbit MQ Node. Configuration is " + rabbitMQConfiguration + ". Will try other configurations");
				}
				tries ++;
			}
			logger.error("Failed to connect to all configured Rabbit MQ nodes");
			throw new Exception("Failed to connect to all configured Rabbit MQ nodes");
		}
    }
    
    /**
     * Signals to this Connector that the connection resources held by it are invalid. Called by the 
     * {@link RabbitMessageReceiver#handleShutdownSignal(String, com.rabbitmq.client.ShutdownSignalException)} when it is intimated of a connection
     * being broken.
     * @throws Exception
     */
    protected void signalConnectionLost() {
    	/**
    	 * the connection resources held by this connector are no longer valid. A subsequent call to 
    	 * #attemptReconnect() or #doConnect() will re-initialize the connection
    	 */
    	try {
    		if(this.connection != null){
    			this.connection.close(CLOSE_TIMEOUT); // wait for a max of one second before forcing the socket to close.
    		}
		} catch (Exception e) {
    		logger.info("Error closing existing Connection instance. Continuing by marking Connection instance as null. Error is : " + e.getMessage());
			// consume and ignore the exception as the connection is useless anyway.
		}
    	this.connection = null; 
    }

    /**
     * Reconnect method called by Receiver instances in case of Rabbit restart. This method is synchronized on this Connector in order to
     * serialize calls from multiple Receiver instances at the same time
     * @throws Exception
     */
    protected void attemptReconnect() throws Exception {
    	synchronized(this) { // synchronize access across multiple Receiver instances of this Connector
    		if (this.connection == null) {
    			this.doConnect();
    		}
    	}
    }

    /**
     * Abstract method implementation. Does nothing.
     * @see org.mule.transport.AbstractConnector#doDisconnect()
     */
    protected void doDisconnect() throws Exception {
    	// no op
    }
    
    /**
     * Returns the receiver key identifier. Simply returns the toString() of the endpoint URI
     */
    protected Object getReceiverKey(Component component, InboundEndpoint endpoint) {
        return endpoint.getEndpointURI().toString();
    }

    /**
     * Helper method to close the connection cleanly.
     */
    private synchronized void closeConnection() {
        try {
            connection.close(200, "Goodbye");
        } catch (Exception e) {
            // Ignore it, we're shutting down anyway.
        }
        connection = null;
    }
   
    /** == Getter/setter methods ==*/
    public ReplyToHandler getReplyToHandler() {
        return this.repyToHandler;
    }
    public Connection getConnection() {
        return this.connection;
    }
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
	public int getDurableMessageCommitCount() {
		return this.durableMessageCommitCount;
	}
	public void setDurableMessageCommitCount(int durableMessageCommitCount) {
		this.durableMessageCommitCount = durableMessageCommitCount;
	}
	public int getPrefetchCount() {
		return this.prefetchCount;
	}
	public void setPrefetchCount(int prefetchCount) {
		this.prefetchCount = prefetchCount;
	}
	public int getRequestHeartBeat() {
		return this.requestHeartBeat;
	}
	public void setRequestHeartBeat(int requestHeartBeat) {
		this.requestHeartBeat = requestHeartBeat;
	}

	public List<RabbitMQConfiguration> getRabbitMQConfigurations()
	{
		return rabbitMQConfigurations;
	}

	public void setRabbitMQConfigurations(List<RabbitMQConfiguration> rabbitMQConfigurations)
	{
		this.rabbitMQConfigurations = rabbitMQConfigurations;
	}
	
}
