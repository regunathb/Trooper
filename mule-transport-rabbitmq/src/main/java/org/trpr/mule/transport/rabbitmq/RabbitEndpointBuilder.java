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

import org.mule.api.endpoint.MalformedEndpointException;
import org.mule.endpoint.ResourceNameEndpointURIBuilder;

import java.net.URI;
import java.util.Properties;

/**
 * The <code>RabbitEndpointBuilder</code> is a endpoint builder using a URI and a relevant set of properties
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */

public class RabbitEndpointBuilder extends ResourceNameEndpointURIBuilder {

	/**
	 * Overriden method implementation. Creates the endpoint using the super class implementation. Also validates the URI specified for required format and
	 * properties.
	 * @see org.mule.endpoint.ResourceNameEndpointURIBuilder#setEndpoint(java.net.URI, java.util.Properties)
	 */
	protected void setEndpoint(URI uri, Properties properties) throws MalformedEndpointException {
        super.setEndpoint(uri, properties);
        if (uri.getAuthority() != null && !address.equals(uri.getAuthority())) {
            String exchange = uri.getAuthority();
            int i = exchange.indexOf(":");
            if (i >= 0) {
                properties.setProperty("exchange", exchange.substring(0, i));
                properties.setProperty("exchange-type", exchange.substring(i + 1));
            } else {
                throw new MalformedEndpointException("You must specify the exchange name and type i.e. MySampleExchange:direct");
            }
            userInfo = null;
            if (address.length() == 0) {
                address = uri.getQuery();
            }
        } else if (address.length() == 0) {
            throw new MalformedEndpointException("You must at least specify a queue name or an exchange");
        }
    }
}
