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
 */
package org.trpr.platform.integration.impl.messaging;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>RabbitMQConfiguration</code> class is a data holder for RabbitMQ connection details. This implementation is 
 * compatible with Java client API of RabbitMQ version 2.2.0. Backward/Forward compatibility with other versions requires verification. 
 * 
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */

public class RabbitMQConfiguration extends RabbitMQRpcConfiguration {

	/** The constant referring to the suffix used for naming dead letter exchanges and queues*/
	public static final String DEAD_SUFFIX = ".dead";
	
	/** The Dead letter exchange arguments for RabbitMQ */
	public static final String RMQ_DL_ARGUMENT = "x-dead-letter-exchange";
	public static final String RMQ_DL_RT_KEY = "x-dead-letter-routing-key";
	
	/** The default durable commit count*/
	public static final int DEFAULT_DURABLE_MSG_COMMIT_COUNT = 1;
	
	/**
	 * Durable true if we are declaring a durable exchange (the exchange will
	 * survive a server restart). 
	 */
	private boolean durable;	
	
	/**
	 * The RabbitMQ queue name
	 */
	private String queueName;
	
	/**
	 * DLQ settings for the connection
	 */
	private boolean dlqEnabled;

	/**
	 * noAck to acknowledge message receipt and processing explicitly.
	 */
	private boolean noAck;
	
	/** Flag to disable TX. May be used to override durability settings. Even durable messages will not be persisted if this is set to true*/
	private boolean disableTX;
	
	/**
	 * The commit count for durable messages. Used only when RabbitMQConfiguration#durable is set to true. Default value is 1 i.e.
	 * every published message will be committed. Consider setting a higher value for better performance.
	 */
	private int durableMessageCommitCount = DEFAULT_DURABLE_MSG_COMMIT_COUNT;

	/** Setting to indicate use of pre-declared queues default behavior is to create the queues using properties specified*/
	private boolean usePredeclaredQueues;
	
	/** Override properties for individual queues, if any*/
	private Map<String,Object> queueOverrideProperties = new HashMap<String, Object>();
	
	/**
	 * No args constructor
	 */
	public RabbitMQConfiguration() {		
	}

	/**
	 * Constructor to initialize member variables.
	 * @param parent the RabbitMQConfiguration to inherit values from. Values inherited from parent may be overriden
	 * using the explicit variable mutator methods of this class
	 */
	public RabbitMQConfiguration(RabbitMQConfiguration parent) {
		this.setUserName(parent.getUserName());
		this.setPassword(parent.getPassword());
		this.setHostName(parent.getHostName());
		this.setVirtualHost(parent.getVirtualHost());
		this.setRoutingKey(parent.getRoutingKey());
		this.setPortNumber(parent.getPortNumber());
		this.setRequestHeartBeat(parent.getRequestHeartBeat());		
		this.setExchangeName(parent.getExchangeName());
		this.setExchangeType(parent.getExchangeType());
		
		this.durable=parent.isDurable();
		
		this.queueName = parent.getQueueName();
		this.dlqEnabled = parent.isDlqEnabled();
		this.noAck=parent.isNoAck();
		this.durableMessageCommitCount = parent.getDurableMessageCommitCount();
		this.disableTX = parent.isDisableTX();
		this.usePredeclaredQueues = parent.isUsePredeclaredQueues();
		this.queueOverrideProperties = parent.getQueueOverrideProperties();
	}

	/**
	 * Overriden superclass method. Returns a string representation of this class's member variables
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append ("\n==Rabbit MQ Configuration==");
		sb.append("\n[HostName = " + this.getHostName());
		sb.append(", VirtualHost = " + this.getVirtualHost());
		sb.append(", Port = " + this.getPortNumber());
		sb.append(", ExchangeName = " + this.getExchangeName());
		sb.append(", QueueName = " + this.getQueueName());
		sb.append(", ExchageType = " + this.getExchangeType());
		sb.append(", RoutingKey = " + this.getRoutingKey());
		sb.append ("]\n==Rabbit MQ Configuration==");
		return sb.toString();
	}
	
	/**==== Start Spring DI style setters/getters */
	public String getQueueName() {
		return this.queueName;
	}
	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}
	public boolean isDurable() {
		return this.durable;
	}		
	public void setDurable(boolean durable) {
		this.durable = durable;
	}
	public boolean isNoAck() {
		return this.noAck;
	}
	public void setNoAck(boolean noAck) {
		this.noAck = noAck;
	}
	public boolean isDisableTX() {
		return this.disableTX;
	}
	public void setDisableTX(boolean disableTX) {
		this.disableTX = disableTX;
	}
	public int getDurableMessageCommitCount() {
		return this.durableMessageCommitCount;
	}
	public void setDurableMessageCommitCount(int durableMessageCommitCount) {
		this.durableMessageCommitCount = durableMessageCommitCount;
	}
	public boolean isDlqEnabled() {
		return this.dlqEnabled;
	}
	public void setDlqEnabled(boolean dlqEnabled) {
		this.dlqEnabled = dlqEnabled;
	}
	public boolean isUsePredeclaredQueues() {
		return this.usePredeclaredQueues;
	}
	public void setUsePredeclaredQueues(boolean usePredeclaredQueues) {
		this.usePredeclaredQueues = usePredeclaredQueues;
	}
	public Map<String, Object> getQueueOverrideProperties() {
		return this.queueOverrideProperties;
	}
	public void setQueueOverrideProperties(Map<String, Object> queueOverrideProperties) {
		this.queueOverrideProperties = queueOverrideProperties;
	}			
	/**==== End Spring DI style setters/getters */
	
}
