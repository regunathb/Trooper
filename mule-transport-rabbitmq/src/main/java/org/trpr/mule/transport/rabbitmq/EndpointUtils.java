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

import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.util.MapUtils;
import org.mule.util.StringUtils;

import com.rabbitmq.client.Channel;

/**
 * The <code>EndpointUtils</code> provides utility methods around Endpoint properties
 * 
 * @author Regunath
 * @version 1.0, 17/08/2012
 */
public class EndpointUtils {
	
	/** The constant referring to the suffix used for naming dead letter exchanges and queues*/
	public static final String DEAD_SUFFIX = ".dead";
	
	/** The Dead letter exchange arguments for RabbitMQ */
	public static final String RMQ_DL_ARGUMENT = "x-dead-letter-exchange";
	public static final String RMQ_DL_RT_KEY = "x-dead-letter-routing-key";

	/**
	 * Returns a String realm if specified
	 */
    public static String getRealm(ImmutableEndpoint e) {
        return MapUtils.getString(e.getProperties(), "realm", "/data");
    }

    /**
     * Returns the queue name if specified as a property on the endpoint
     */
    public static String getQueue(ImmutableEndpoint e) {
        return MapUtils.getString(e.getProperties(), "queue", null);
    }

    /**
     * Returns the exchange name if specified as a property on the endpoint
     */
    public static String getExchange(ImmutableEndpoint e) {
        return MapUtils.getString(e.getProperties(), "exchange", null);
    }

    /**
     * Returns the exchange type if specified as a property on the endpoint
     */
    public static String getExchangeType(ImmutableEndpoint e) {
        return MapUtils.getString(e.getProperties(), "exchange-type", "direct");
    }

    /**
     * Returns the routing key string if specified as a property on the endpoint
     */
    public static String getRoutingKey(ImmutableEndpoint e) {
        return MapUtils.getString(e.getProperties(), "routing-key", StringUtils.EMPTY);
    }

    /**
     * Returns the durability setting on the endpoint
     */
    public static boolean isDurable(ImmutableEndpoint e) {
        return MapUtils.getBooleanValue(e.getProperties(), "durable", false);
    }

    /**
     * Returns the dead-letter setting on the endpoint
     */
    public static boolean isDeadLetterEnabled(ImmutableEndpoint e) {
        return MapUtils.getBooleanValue(e.getProperties(), "dead-lettered", false);
    }

    /**
     * Returns the message-re-queued setting on the endpoint
     */
    public static boolean isMessageRequeued(ImmutableEndpoint e) {
        return MapUtils.getBooleanValue(e.getProperties(), "message-requeued", false);
    }
    
    /**
     * Returns if this end-point uses a pre-existing queue i.e. should not create one
     */
	public static boolean isUsePredeclaredQueue(ImmutableEndpoint e) {
        return MapUtils.getBooleanValue(e.getProperties(), "isUsePredeclaredQueue", false);
	}
    
    /**
     * Declares an Exchange on the specified Channel using the exchange details derived from the specified Endpoint
     */
    public static String declareExchange(Channel channel, ImmutableEndpoint e) throws IOException {
        String exchange = EndpointUtils.getExchange(e);
        if (exchange != null) {
        	channel.exchangeDeclare(exchange, EndpointUtils.getExchangeType(e),isDurable(e));
        	if (EndpointUtils.isDeadLetterEnabled(e)) { // create a dead letter exchange if the endpoint has enabled dead lettering
            	channel.exchangeDeclare(exchange+EndpointUtils.DEAD_SUFFIX, EndpointUtils.getExchangeType(e),isDurable(e));        		
        	}
        }
        return exchange;
    }
}
