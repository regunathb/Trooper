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

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.transport.MessageDispatcher;
import org.mule.transport.AbstractMessageDispatcher;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.RpcClient;

/**
 * The <code>RabbitDispatcher</code> is the Mule {@link MessageDispatcher} implementation for RabbitMQ.
 * Modified behavior to create the RabbitMQ RpcClient in the doConnect() method. This takes care of scenarios where RabbitMQ instance fails
 * and is restarted even as Mule instance is running.
 * This Dispatcher depends on the RabbitConnector life-cycle.The connection related class instances of the RabbitConnector are recreated
 * or disposed as per the RabbitMQ Consumer callback interface methods implemented in the RabbitMessageReceiver. This behavior is relevant
 * only in re-connect scenarios i.e. when the Rabbit instance undergoes a restart when Mule transport classes (such as this Dispatcher)
 * are instantiated and active.
 * 
 * TX Commits messages based on configured durable message commit count on the RabbitConnector if the endpoint is durable.
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */

public class RabbitDispatcher extends AbstractMessageDispatcher {
	
	/** The RpcClient for dispatching messages */
    private RpcClient rpcClient;
    
    /** The Channel object*/
    private Channel channel;
    
    /** The message dispatched count. Used in TX commit of durable messages. */
    private long dispatchedMessageCount;

    /**
     * Constructor for this class
     * @param endpoint the dispatch endpoint
     * @throws InitialisationException in case of errors in instantiating this Dispatcher 
     */
    public RabbitDispatcher(OutboundEndpoint endpoint) throws InitialisationException {
        super(endpoint);
    }

    /**
     * Abstract method implementation. Creates the RPC client if required.
     * @see org.mule.transport.AbstractConnectable#doConnect()
     */
    protected void doConnect() throws Exception   {
        RabbitConnector conn = (RabbitConnector) connector;
        try {
        	// check to see if the Connector is indeed connected - to address issues where the Connector becomes unusable in case of a 
        	// Rabbit restart whilst Mule is still running.
        	if (channel != null) { // a doConnect() has been invoked already
        		if (channel.getConnection() != conn.getConnection()) { // if the connection objects are different, this dispatcher is holding on to a stale connection
        			rpcClient = null; // set the RpcCleint to null. Will be recreated in the next steps.
        		}
        	}
        	if (rpcClient == null) {
	            RabbitConnector.ChannelHolder ch = conn.createChannel(endpoint);
	            channel = ch.getChannel();	
	            logger.debug("Dispatcher opened channel: " + channel);
	            //EndpointURI e = endpoint.getEndpointURI();
	            String exchange = EndpointUtils.declareExchange(channel, endpoint);
	            String routingKey = EndpointUtils.getRoutingKey(endpoint);
	            String queue = EndpointUtils.getQueue(endpoint);
	            if (queue != null) {
	                exchange = "";
	                routingKey = queue;
	            }
	            logger.debug("RpcClient initialised on exchange: " + exchange + ", routing key: " + routingKey);
	            rpcClient = new RpcClient(channel, exchange, routingKey);
        	}
        } catch (IOException e) {
            e.printStackTrace(); // cant assume existense of any logging libraries. Hence just directing the stack trace to default error output
            throw new InitialisationException(e, null);
        }
    }

    /**
     * Abstract method implementation. Does nothing as lifecycle of this Dispatcher is controlled by the {@link RabbitConnector}
     * @see org.mule.transport.AbstractConnectable#doDisconnect()
     */
    protected void doDisconnect() throws Exception {
    	// no op
    }

    /**
     * Abstract method implementation. Dispatches the specified MuleEvent using the RPC client
     * @see org.mule.transport.AbstractMessageDispatcher#doDispatch(org.mule.api.MuleEvent)
     */
    protected void doDispatch(MuleEvent event) throws Exception {
        MuleMessage msg = event.getMessage();
        AMQP.BasicProperties msgProps = EndpointUtils.isDurable(endpoint) ? MessageProperties.PERSISTENT_BASIC : MessageProperties.BASIC;
        rpcClient.publish(msgProps, msg.getPayloadAsBytes());
        dispatchedMessageCount++;
        // commit the message if the endpoint is durable and the commit count is reached. 
        // The channel should and would have been created with txSelect in the RabbitConnector
        if (EndpointUtils.isDurable(endpoint) && (dispatchedMessageCount % ((RabbitConnector)connector).getDurableMessageCommitCount() == 0)) {
        	// synchronized on the channel to avoid the below RabbitMQ client exception, caused in multi-threaded execution using the same channel:
        	// java.lang.IllegalStateException: cannot execute more than one synchronous AMQP command at a time
        	synchronized(channel) {
        		channel.txCommit();
        	}
        }
    }

    /**
     * Abstract method implementation. Disposes the RPC client used for dispatching messages
     * @see org.mule.transport.AbstractConnectable#doDispose()
     */
    protected void doDispose() {
        try {
        	// Commit any non-committed durable messages because the commit count was not reached. This is done before this dispatcher goes down
            // The channel should and would have been created with txSelect in the RabbitConnector
            if (EndpointUtils.isDurable(endpoint)) {
            	// synchronized on the channel to avoid the below RabbitMQ client exception, caused in multi-threaded execution using the same channel:
            	// java.lang.IllegalStateException: cannot execute more than one synchronous AMQP command at a time
            	synchronized(channel) {
            		channel.txCommit();
            	}
            }        	
            if (rpcClient != null) {
                rpcClient.close();
            }
        } catch (IOException e){
            //ignore as we are shutting down anyway
        }
    }

    /**
     * Abstract method implementation. Sends the specified MuleEvent synchronously and waits for the response to return it - uses the RPC client for message calls
     * @see org.mule.transport.AbstractMessageDispatcher#doSend(org.mule.api.MuleEvent)
     */
    protected MuleMessage doSend(MuleEvent event) throws Exception {	
        final MuleMessage msg = event.getMessage();
    	// durable settings are NOT ideal for synchronous calls as non-consumed messages will remain for ever. Throw exception if the endpoint is 
        // configured as durable
        if (EndpointUtils.isDurable(endpoint)) {
        	throw new Exception("Durable end-points not supported for synchronous calls. Configuration mis-match.");
        }
        byte[] response = rpcClient.primitiveCall(msg.getPayloadAsBytes());
        return new DefaultMuleMessage(response);
    }
    
}
