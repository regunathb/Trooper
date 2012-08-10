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

/**
 * The <code>RabbitMQRpcConfiguration</code> class is a data holder of RabbitMQ connection details for RPC like messaging. This implementation is 
 * compatible with Java client API of RabbitMQ version 2.2.0. Backward/Forward compatibility with other versions requires verification. 
 * 
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */

public class RabbitMQRpcConfiguration {

	/**
	 * User credentials for RabbitMQ connection
	 */
	private String userName;
	private String password;

	/**
	 * Host Name for the RabbitMQ Broker
	 */
	private String hostName;

	/**
	 * Port Number where the Broker is listening to
	 */
	private int portNumber;
	
	/**
	 * Virtual Host name on the Broker
	 */
	private String virtualHost;
	
	/**
	 * Exchange Name on the virtual host
	 */
	private String exchangeName;
	
	/**
	 * The Exchange type. E.g. DIRECT
	 */
	private String exchangeType;
	
	/**
	 * Routing key used to identify designated messages for consumers
	 */
	private String routingKey;

	/**
	 * Heartbeat interval, in seconds for message request.
	 */
	private int requestHeartBeat;

	/**
	 * No args constructor
	 */
	public RabbitMQRpcConfiguration() {		
	}
	
	/**
	 * Overriden superclass method. Returns a string representation of this class's member variables
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append ("\n==Rabbit MQ RPC Configuration==");
		sb.append("\n[HostName = " + this.getHostName());
		sb.append(", VirtualHost = " + this.getVirtualHost());
		sb.append(", ExchangeName = " + this.getExchangeName());
		sb.append(", ExchageType = " + this.getExchangeType());
		sb.append(", RoutingKey = " + this.getRoutingKey());
		sb.append ("]\n==Rabbit MQ RPC Configuration==");
		return sb.toString();
	}
	
	/**==== Start Spring DI style setters/getters */
	public String getUserName() {
		return this.userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return this.password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getHostName() {
		return this.hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getPortNumber() {
		return this.portNumber;
	}
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}
	public String getVirtualHost() {
		return this.virtualHost;
	}
	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}
	public String getExchangeName() {
		return this.exchangeName;
	}
	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}	
	public String getExchangeType() {
		return this.exchangeType;
	}
	public void setExchangeType(String exchangeType) {
		this.exchangeType = exchangeType;
	}
	public boolean isDurable() {
		return false; // Always false for an RPC style exchange
	}	
	public String getRoutingKey() {
		return this.routingKey;
	}
	public void setRoutingKey(String routingKey) {
		this.routingKey = routingKey;
	}
	public int getRequestHeartBeat() {
		return this.requestHeartBeat;
	}
	public void setRequestHeartBeat(int requestHeartBeat) {
		this.requestHeartBeat = requestHeartBeat;
	}
	/**==== End Spring DI style setters/getters */
	
}
