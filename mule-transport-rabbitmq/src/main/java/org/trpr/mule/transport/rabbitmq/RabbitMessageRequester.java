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
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.transport.MessageAdapter;
import org.mule.api.transport.MessageRequester;
import org.mule.transport.AbstractMessageRequester;
import org.trpr.mule.transport.rabbitmq.i18n.RabbitMessages;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.RpcClient;

/**
 * The <code>RabbitMessageRequester</code> is the {@link MessageRequester} implementation for the RabbitMQ Mule transport.
 * This implementation is largely incomplete as explicit and arbitrary programmatic request to messages from the channel is discouraged. Prefer
 * subscription using endpoints instead.
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
*/

public class RabbitMessageRequester extends AbstractMessageRequester {

	/** The AMQP objects as variables*/
    private RpcClient rpcClient;
    private Channel channel;

    /**
     * Constructor for this class.
     * @param endpoint the Endpoint to create a MessageRequester for
     * @throws CreateException in case of errors in instantiating this MessageRequester
     */
    public RabbitMessageRequester(InboundEndpoint endpoint) throws CreateException {
        super(endpoint);
        RabbitConnector conn = (RabbitConnector) connector;
        try {
            RabbitConnector.ChannelHolder ch = conn.createChannel(endpoint);
            channel = ch.getChannel();

            logger.debug("RabbitMessageRequester opened channel: " + channel);
            String exchange = EndpointUtils.declareExchange(channel, endpoint);
            String routingKey = EndpointUtils.getRoutingKey(endpoint);
            String queue = EndpointUtils.getQueue(endpoint);
            if (queue != null) {
                exchange = "";
                routingKey = queue;
            }
            logger.debug("RpcClient initialised on exchange: " + exchange + ", routing key: " + routingKey);
            rpcClient = new RpcClient(channel, exchange, routingKey);

        } catch (Exception e) {
            e.printStackTrace();
            throw new CreateException(e, null);
        }
    }

    /**
     * Abstract method implementation. Tries to get a message from the queue identified by the endpoint until a timeout occurs.
     * @see org.mule.transport.AbstractMessageRequester#doRequest(long)
     */
    protected MuleMessage doRequest(long timeout) throws Exception {
        try {
            String queue = EndpointUtils.getQueue(endpoint);
            if (queue == null)
            {
                throw new IllegalArgumentException(RabbitMessages.noQueueDefined(endpoint).getMessage());
            }
            long time = System.currentTimeMillis() + timeout;
            long count = System.currentTimeMillis();
            while (count < time) {
                GetResponse response = rpcClient.getChannel().basicGet(queue, false);
                if (response == null)
                {
                    Thread.sleep(50);
                    count += 50;
                }
                else
                {
                    AMQP.BasicProperties props = response.getProps();
                    byte[] body = response.getBody();
                    MessageAdapter adapter = connector.getMessageAdapter(new Object[]{body, props});
                    return new DefaultMuleMessage(adapter);
                }
            }
            return null;
        } catch (Throwable e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Abstract method implementation. Does nothing as the connection is already established by the Connector
     * @see org.mule.transport.AbstractConnectable#doConnect()
     */
    public void doConnect() throws Exception {
    	// no op
    }

    /**
     * Abstract method implementation. Does nothing as the connection is already disconnected by the Connector
     * @see org.mule.transport.AbstractConnectable#doDisconnect()
     */
    public void doDisconnect() throws Exception {
    	// no op
    }

    /**
     * Abstract method implementation. Closes the RPC connection held by this MessageRequester
     * @see org.mule.transport.AbstractConnectable#doDispose()
     */
    protected void doDispose() {
        try {
            if (rpcClient != null){
                rpcClient.close();
            }
        }catch (IOException e){
            //ignore as we are shutting down anyway
        }
    }

}

