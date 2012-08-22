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

import org.mule.api.MuleEvent;
import org.mule.api.MuleException;
import org.mule.api.MuleMessage;
import org.mule.api.transport.ReplyToHandler;
import org.mule.transport.DefaultReplyToHandler;

/**
 * The <code>RabbitReplyToHandler</code> is the {@link ReplyToHandler} implementation for the RabbitMQ Mule transport.
 * An empty implementation of a ReplyTo handler. RabbitMQ has a clean way of managing RPC style calls so Mule
 * doesn't have to intervene
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */
public class RabbitReplyToHandler extends DefaultReplyToHandler {
	
	/**
	 * No args constructor
	 */
    public RabbitReplyToHandler() {
        super(null);
    }

    /**
     * Abstract method implementation. Does nothing as the {@link RabbitMessageReceiver} handles it anyway
     * @see org.mule.transport.DefaultReplyToHandler#processReplyTo(org.mule.api.MuleEvent, org.mule.api.MuleMessage, java.lang.Object)
     */
    public void processReplyTo(MuleEvent event, MuleMessage returnMessage, Object replyTo) throws MuleException {
        //do nothing
    }
    
}