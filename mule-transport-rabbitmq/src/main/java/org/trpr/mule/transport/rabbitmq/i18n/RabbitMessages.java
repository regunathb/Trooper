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

package org.trpr.mule.transport.rabbitmq.i18n;

import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;

/**
 * The <code>RabbitMessages</code> defines the minimalistic RabbitMQ specific i18n messages.
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */

public class RabbitMessages extends MessageFactory {
	
	/** Load the messages from META-INF.services.org.mule.i18n.rabbit-messages*/
    private static final String BUNDLE_PATH = getBundlePath("rabbit");

    /** Initialize this MessageFactory as a static class-loader wide instance*/
    private static final RabbitMessages factory = new RabbitMessages();

    /**
     * Returns a message to indicate that transactions are not supported on this transport
     * @param actualType the actual Java type specified
     * @param expectedType the expected Java type
     * @return Message reflecting non-avilability of transaction support
     */
    public static Message transactionNotSupported(Class actualType, Class expectedType) {
        return factory.createMessage(BUNDLE_PATH, 1, actualType.getName(), expectedType.getName());
    }

    /**
     * Returns a message to indicate that no queue has been defined for the specified end-point
     * @param ep the Endpoint reference depicting a queue
     * @return Message reflecting absence of suitable declared AMQP queue
     */
    public static Message noQueueDefined(ImmutableEndpoint ep) {
        return factory.createMessage(BUNDLE_PATH, 2, ep.toString());
    }
}
