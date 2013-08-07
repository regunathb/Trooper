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
import java.util.HashMap;
import java.util.Map;

import javax.resource.spi.work.Work;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.lifecycle.LifecycleException;
import org.mule.api.service.Service;
import org.mule.api.transport.Connector;
import org.mule.api.transport.MessageAdapter;
import org.mule.api.transport.MessageReceiver;
import org.mule.transport.AbstractMessageReceiver;
import org.mule.util.StringUtils;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * The <code>RabbitMessageReceiver</code> is the {@link MessageReceiver} implementation for the RabbitMQ Mule transport.
 * Extended the Receiver behavior to support re-connect. This MessageReceiver spawns a thread that is otherwise idle and wakes up only when a shutdown signal 
 * is received. The thread repeatedly tries to reconnect in between sleep intervals using the RabbitConnector instance and re-initializes itself when the connect
 * is a success.
 *
 * Modified behavior to use Mule WorkManager to handle incoming messages via an implementation of the Work interface. This is done to handle
 * scenarios where messages are routed to this RabbitMQ Consumer just as the Mule instance is starting. Also moved the actual message consumption
 * to doStart(). Implemented most of the life-cycle call-back methods
 *
 * @author Regunath B
 * @version 1.0, 17/08/2012
*/

public class RabbitMessageReceiver extends AbstractMessageReceiver implements Consumer, Runnable {

	/**
	 * Re-connect states for this Receiver
	 */
	private static final int IDLE = 0;
	private static final int TRY_RECONNECT=1;
	private static final int EXIT = 2;

	/**
	 * The reconnect sleep interval in ms
	 */
	private static final long RECONNECT_SLEEP_INTERVAL = 5000;

	/**
	 * The re-connect state indicator for this Receiver
	 */
	private volatile int reconnectState = IDLE;

	/** The AMQP object variables*/
    private Channel channel;
    private String queue;
    private String exchange;
    private String routingKey;
    
    /** Flag to indicate whether to start consuming messages on connect*/
    private boolean startOnConnect = false;

    /**
     * Constructor for this class. 
     * @param connector the RabbitConnector instance that created this Receiver
     * @param service the Service instance
     * @param endpoint the Endpoint definition
     * @throws CreateException in case of errors in creating this Receiver
     */
    public RabbitMessageReceiver(Connector connector, Service service, InboundEndpoint endpoint) throws CreateException {
        super(connector, service, endpoint);
    }

    /**
     * Abstract method implementation. Creates the channel objects and starts consuming messages.
     * @see org.mule.transport.AbstractConnectable#doConnect()
     */
    protected void doConnect() throws Exception {
    	// local variables for dead letter queue and exchange
    	String dlQueue = null, dlExchange = null;
    	
        RabbitConnector conn = (RabbitConnector) connector;
        RabbitConnector.ChannelHolder ch = conn.createChannel(endpoint);
        channel = ch.getChannel();
        logger.debug("Receiver opened channel: " + channel);

        queue = EndpointUtils.getQueue(endpoint);
        routingKey = EndpointUtils.getRoutingKey(endpoint);
        if (EndpointUtils.getExchangeType(endpoint).equals("direct") && StringUtils.isEmpty(routingKey)) {
            routingKey = queue;
        }
        
        if (queue == null) {
            logger.debug("Declaring private queue");
            queue = channel.queueDeclare().getQueue();
            logger.debug("Private queue name: " + queue);
        } else {
            logger.debug("Declaring well-known queue: " + queue);      
            if (EndpointUtils.isDeadLetterEnabled(endpoint)) { // check if dead lettering is enabled on the endpoint for the queue 
            	dlQueue = queue + EndpointUtils.DEAD_SUFFIX; // append the DLQ suffix to the queue name declared in the endpoint
            	dlExchange = EndpointUtils.getExchange(endpoint) + EndpointUtils.DEAD_SUFFIX; // append the DLQ suffix to the exchange name declared in the endpoint
            	Map<String, Object> args = new HashMap<String, Object>(); //  map for DLQ arguments
            	args.put(EndpointUtils.RMQ_DL_ARGUMENT, dlExchange);  
            	args.put(EndpointUtils.RMQ_DL_RT_KEY, routingKey);
            	channel.queueDeclare(dlQueue,EndpointUtils.isDurable(endpoint),false,false,null); // create the dead letter queue for the one mentioned in the endpoint
	    		channel.queueDeclare(queue,EndpointUtils.isDurable(endpoint),false,false,args); // declare the queue by specifying the AMQP arguments to identify the DLQ routing key and the DLQ exchange
            } else {
	    		// Input Queue
	    		channel.queueDeclare(queue,EndpointUtils.isDurable(endpoint),false,false,null); // no dead lettering
            }
        }
        
        if (startOnConnect) { // start the consumer only if set to start on connect
            doStart();
        }
        
        exchange = EndpointUtils.declareExchange(channel, endpoint); // creates the exchange and DLQ exchange as well, if required by the endpoint
        logger.debug("Using exchange: " + exchange + ", routing key: " + routingKey);
        channel.queueBind(queue, exchange, routingKey);
        if (EndpointUtils.isDeadLetterEnabled(endpoint) && dlQueue != null && dlExchange != null) { // bind the DLQ to the DLQ exchange if dead lettering is enabled
            logger.debug("Using dl exchange: " + dlExchange + ", routing key: " + routingKey);
            channel.queueBind(dlQueue, dlExchange, routingKey);
        }
        
        // start the re-connect thread only if it is not started yet
        if (reconnectState != TRY_RECONNECT) {
        	Thread thread = new Thread(this);
        	// set the thread as a daemon so that it exits with the JVM
        	thread.setDaemon(true);
        	thread.start();
        }
    }

    /**
     * Abstract method implementation. Closes the channel quietly
     * @see org.mule.transport.AbstractConnectable#doDisconnect()
     */
    protected void doDisconnect() throws Exception {
    	this.closeChannelQuietly();
    }

    /**
     * Abstract method implementation. Shuts down the reconnect thread
     * @see org.mule.transport.AbstractConnectable#doDispose()
     */
    protected void doDispose() {
    	// stop the reconnect thread as this Receiver has been disposed off
        this.reconnectState = EXIT;
		synchronized(this) {
			notifyAll();
		}
    }

    /**
     * Abstract method implementation. Starts consuming messages from the channel
     * @see org.mule.transport.AbstractConnectable#doStart()
     */
    protected void doStart() throws MuleException {
        try {
            // We need to register the listener when start is called in order to only
            // start receiving messages after
            // start/
            // If the consumer is null it means that the connection strategy is being
            // run in a separate thread
            // And hasn't managed to connect yet.
            if (channel == null) {
                startOnConnect = true;
            } else {
                startOnConnect = false;
                channel.basicConsume(queue, this);                
            }
        } catch (Exception e) {
            throw new LifecycleException(e, this);
        }
    }

    /**
     * Abstract method implementation. Closes the channel
     * @see org.mule.transport.AbstractConnectable#doStop()
     */
    protected void doStop() throws MuleException {
    	this.closeChannelQuietly();
    }

    /**
     * Interface method implementation. Does nothing
     * @see com.rabbitmq.client.Consumer#handleCancelOk(java.lang.String)
     */
    public void handleCancelOk(String consumerTag) {
        logger.debug("Consumer tag cancelled: " + consumerTag);
    }

    /**
     * Interface method implementation. Does nothing
     * @see com.rabbitmq.client.Consumer#handleConsumeOk(java.lang.String)
     */
    public void handleConsumeOk(String consumerTag) {
        logger.debug("Consuming from tag: " + consumerTag);
    }

    /**
     * Interface method implementation. Does nothing
     * @see com.rabbitmq.client.Consumer#handleCancel(java.lang.String)
     */
	public void handleCancel(String consumerTag) throws IOException {
		// no op
	}

	/**
	 * Interface method implementation. Does nothing
	 * @see com.rabbitmq.client.Consumer#handleRecoverOk(java.lang.String)
	 */
	public void handleRecoverOk(String consumerTag) {
		// no op
	}

	/**
	 * Interface method implementation. Processes each delivery using a {@link RabbitMessageReceiver.AMQPWorker} instance
	 * @see com.rabbitmq.client.Consumer#handleDelivery(java.lang.String, com.rabbitmq.client.Envelope, com.rabbitmq.client.AMQP.BasicProperties, byte[])
	 */
    public void handleDelivery(final String consumerTag, final Envelope env, final BasicProperties props, final byte[] body) throws IOException {
    	 try {
             getWorkManager().scheduleWork(new AMQPWorker(this, consumerTag, env, props, body));
         } catch (Exception e){
             handleException(e);
         }
    }

    /**
     * Interface method implementation. Signals to the reconnect thread to try to re-establish the connection if the connector has not been stopped.
     * @see com.rabbitmq.client.Consumer#handleShutdownSignal(java.lang.String, com.rabbitmq.client.ShutdownSignalException)
     */
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        logger.debug("Shutdown signal received: " + sig);
        this.stop();
        ((RabbitConnector)(this.connector)).signalConnectionLost(); // inform the connector that the connection is lost
        if (this.connector.isStarted()) {
            // wake up the reconnect thread
        	this.reconnectState = TRY_RECONNECT;
        } else {
            this.reconnectState = EXIT;        	
        }
		synchronized(this) {
			notifyAll();
		}
    }

    /**
     * The Runnable run method of the reconnect thread
     */
	public void run() {
		while (true) {
			switch(reconnectState) {
				case IDLE:
					synchronized(this) {
						try {
							// sleep until woken up
							wait();
						}catch(InterruptedException ie) {
							// do nothing
						}
					}
					break;
				case TRY_RECONNECT:
					// sleep for a while and try to reconnect
					synchronized(this) {
						try {
							wait(RECONNECT_SLEEP_INTERVAL);							
						}catch(InterruptedException ie) {
							// do nothing
						}
					}
					try {
						((RabbitConnector)(this.connector)).attemptReconnect();
						try {
							// tell this Receiver to start on connect
							this.startOnConnect = true;
							this.doConnect();
							logger.info("Reconnect with Rabbit instance successful");
						} catch (InitialisationException ie) {
							logger.error("doConnect method failed on RabbitMessageReceiver during attempt to reconnect : " + ie.getMessage(), ie);
							handleException(ie);
						}
						this.reconnectState = IDLE;
					} catch (Exception e) {
						// ignore and go back to sleeping and retrying
						logger.info("Reconnect failed to Rabbit instance, will try again in : " + RECONNECT_SLEEP_INTERVAL + " ms");
					}
					break;
				case EXIT:
					return; // exit the run() method
				
			}
		}
	}
	
	/** The Work implementation for processing the message payload received */
    protected  class AMQPWorker implements Work {

    	/** The AMQP data objects */
    	private String consumerTag;
    	private Envelope env;
    	private BasicProperties props;
    	private byte[] body;

    	/**
    	 * Constructor for this class
    	 * @param receiver the Receiver that created this worker
    	 * @param consumerTag the consumer tag for the delivered message
    	 * @param env the message envelope
    	 * @param props the message properties
    	 * @param body the payload as byte array
    	 */
    	public AMQPWorker (AbstractMessageReceiver receiver, String consumerTag,
    			Envelope env, BasicProperties props, byte[] body) {
    		this.consumerTag = consumerTag;
    		this.env = env;
    		this.props = props;
    		this.body = body;
    	}

        /**
         * The run method for this worker
         * @see java.lang.Runnable#run()
         */
        public void run() {
            boolean synchronous = false;
            if ((props.getReplyTo() != null && !props.getReplyTo().equals(StringUtils.EMPTY)) || endpoint.isSynchronous()) {
            	synchronous = true;  
            }
            try {
	            MessageAdapter adapter = connector.getMessageAdapter(new Object[]{body, props});
	            adapter.setProperty(RabbitConnector.RABBIT_MQ_ENVELOPE_PROPERTY, env);
	            adapter.setProperty(RabbitConnector.RABBIT_MQ_CONSUMER_TAG_PROPERTY, consumerTag);
	
	            MuleMessage returnMessage = routeMessage(new DefaultMuleMessage(adapter), synchronous);
	            // send the response if the endpoint is synchronous and the replyTo property has been set
	            if (synchronous && (props.getReplyTo() != null && !props.getReplyTo().equals(StringUtils.EMPTY))) 
	            { // ignoring durability settings of the endpoint for synchronous messages as unconsumed messages will live for ever. 
	                channel.basicPublish("", props.getReplyTo(),
	                        new AMQP.BasicProperties(
	                                null, null, null, null,
	                                null, props.getCorrelationId(), null, null,
	                                null, null, null, null,
	                                null, null),
	                        returnMessage.getPayloadAsBytes());
	            }
	            // Do not ack the Rabbit message if the return Mule message has an exception pay load and the endpoint is synchronous.
	            // Not acking the message is consistent with behavior where an exception thrown while routing the message on the same thread
	            // would have errored out and code execution would not reach here. Use of Mule exception handling strategies and separate thread pools
	            // for receivers, service and dispatcher results in exceptions getting handled and bundled into the response Mule message. This necessitates
	            // processing of response to determine execution outcome of invoking the service component.
	            if (synchronous && returnMessage.getExceptionPayload() != null) {
	            	if (env.isRedeliver()) {
	            		channel.basicReject(env.getDeliveryTag(), false); // reject the message without re-queuing 
	            	} else {
	            		channel.basicReject(env.getDeliveryTag(), EndpointUtils.isMessageRequeued(endpoint)); // reject the message passing in the re-queuing flag declared on the endpoint
	            	}
	            } else {
	            	channel.basicAck(env.getDeliveryTag(), false);
	            }
            } catch (Exception e) {
            	// blanket catch for all exceptions. Let Mule handle the exception
            	handleException(e);
            }
            return;
        }

        /**
         * Interface method implementation. Does nothing as this Work is not holding on to any resources
         * @see javax.resource.spi.work.Work#release()
         */
		public void release() {
			// no op
		}
    }

    /**
     * Helper method to close the channel object held by this receiver
     */
	private void closeChannelQuietly() {
		if (this.channel != null) {
    		try {
    			this.channel.close();
    		}catch (Exception e) {
    			logger.info("Error closing channel. Ignoring it. Exception is : " + e.getMessage());
                // consume any exceptions as we are closing anyway
            }
    	}
	}

}
