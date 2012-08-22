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

import java.util.Iterator;
import java.util.Map;

import org.mule.api.ThreadSafeAccess;
import org.mule.api.transport.MessageAdapter;
import org.mule.api.transport.MessageTypeNotSupportedException;
import org.mule.transport.AbstractMessageAdapter;

import com.rabbitmq.client.AMQP;

/**
 * The <code>RabbitMessageAdapter</code> is the {@link MessageAdapter} implementation for the RabbitMQ Mule transport.
 * 
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */

public class RabbitMessageAdapter extends AbstractMessageAdapter {
	
    /** Default serial version UID*/
	private static final long serialVersionUID = 1L;
	
	/** The message payload */
	private byte[] payload;
	
	/** The AMQP Basic properties*/
    private AMQP.BasicProperties props;

    /**
     * Constructor for this MessageAdapter. 
     * @param message the payload
     * @throws MessageTypeNotSupportedException thrown in case the payload is not of a supported type
     */
    @SuppressWarnings("rawtypes")
	public RabbitMessageAdapter(Object message) throws MessageTypeNotSupportedException {
        if (message instanceof Object[]) {
            this.payload = (byte[]) ((Object[]) message)[0];
            if (((Object[]) message).length > 1) {
                props = (AMQP.BasicProperties) ((Object[]) message)[1];
                if (props.getHeaders()!= null) {
                    for (Iterator iterator = props.getHeaders().entrySet().iterator(); iterator.hasNext();) {
                        Map.Entry e = (Map.Entry) iterator.next();
                        String key = (String) e.getKey();
                        Object value = e.getValue();
                        // skip incoming null values
                        if (value != null) {
                            setProperty(key, value);
                        }
                    }
                }
            }
        } else {
            throw new MessageTypeNotSupportedException(message, getClass());
        }
    }

    /**
     * Constructor from another instance of this same type
     */
    protected RabbitMessageAdapter(RabbitMessageAdapter template) {
		super(template);
	    payload = template.payload;
	    props = template.props;
	}

    /**
     * Returns the payload
     */
    public Object getPayload() {
        return this.payload;
    }

    /**
     * Returns the payload as byte array
     */
    public byte[] getPayloadAsBytes() throws Exception {
        return payload;
    }

    /**
     * Returns the payload as String
     */
    public String getPayloadAsString(String s) throws Exception {
        return new String(payload, s);
    }

    /**
     * Overriden method. Gets the messade ID as the unique ID.
     * @see org.mule.transport.AbstractMessageAdapter#getUniqueId()
     */
    public String getUniqueId() {
        return (props.getMessageId()== null ? id : props.getMessageId());
    }

    /**
     * Overriden method. Gets the replyTo queue name
     * @see org.mule.transport.AbstractMessageAdapter#getReplyTo()
     */
    public Object getReplyTo() {
        return props.getReplyTo();
    }

    /**
     * Overriden method. Gets the content encoding
     * @see org.mule.transport.AbstractMessageAdapter#getEncoding()
     */
    public String getEncoding() {
        return (props.getContentEncoding() == null ? super.getEncoding() : props.getContentEncoding());
    }

    /**
     * Overriden method. Gets the properties' correlation ID
     * @see org.mule.transport.AbstractMessageAdapter#getCorrelationId()
     */
    public String getCorrelationId() {
        return props.getCorrelationId();
    }

    /**
     * Overriden method. Creates a copy of this MessageAdapter
     * @see org.mule.transport.AbstractMessageAdapter#newThreadCopy()
     */
    public ThreadSafeAccess newThreadCopy() {
        return new RabbitMessageAdapter(this);
    }

}
