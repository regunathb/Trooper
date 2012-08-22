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
    public static boolean getDurable(ImmutableEndpoint e) {
        return MapUtils.getBooleanValue(e.getProperties(), "durable", false);
    }

    /**
     * Declares an Exchange on the specified Channel using the exchange details derived from the specified Endpoint
     */
    public static String declareExchange(Channel channel, ImmutableEndpoint e) throws IOException {
        String exchange = EndpointUtils.getExchange(e);
        if (exchange != null) {
        	channel.exchangeDeclare(exchange, EndpointUtils.getExchangeType(e),getDurable(e));
        }
        return exchange;
    }
}
