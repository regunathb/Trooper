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
package org.trpr.platform.integration.spi.messaging;

/**
 * The <code>RpcClient</code> provides methods for RPC like messaging. This client provides synchronous point-to-point message publishing and waiting
 * for a response that is then returned to the caller.   
 * Specific implementations may support features like distributed publishing across set of configured queues, error detection 
 * and possible recovery/retry.
 * 
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */
public interface RpcClient {

	/**
	 * Initializes this RpcClient
	 */
	public void initialize();
	
	/**
	 * Sends the specified object and waits for a response message (or) the specified timeout to occur. Keeps the connection open for further 
	 * send requests. Note that clients of this RPCClient must call {@link RPCClient#closeConnections()} when done using this RPCClient.
	 * This method performs to-and-from conversion of the specified object to a raw byte array when publishing it to the queue / consuming off it. 
	 * @param message the message to be sent
	 * @param timeout the timeout duration in milliseconds
	 * @return response Object from the RPC message call
	 * @throws MessagingTimeoutException in case the specified timeout occurs
	 * @throws MessagingException in case of errors in message publishing
	 */
	public Object send(Object message, int timeout) throws MessagingTimeoutException, MessagingException;

	/**
	 * Sends the specified String and waits for a response message (or) the specified timeout to occur. Keeps the connection open for further 
	 * send requests. Note that clients of this RPCClient must call {@link RPCClient#closeConnections()} when done using this RPCClient.
	 * This method performs to-and-from conversion of the specified object to UTF-8 encoded byte array when publishing it to the queue / consuming off it. 
	 * @param message the String message to be sent
	 * @param timeout the timeout duration in milliseconds
	 * @return response String from the RPC message call
	 * @throws MessagingTimeoutException in case the specified timeout occurs
	 * @throws MessagingException in case of errors in message publishing
	 */
	public String sendString(String message , int timeout) throws MessagingTimeoutException, MessagingException;
	
	/**
	 * Closes connection related objects used by this RpcClient.
	 * @throws MessagingException in case of errors closing connections to the underlying messaging system.
	 */
	public void closeConnections() throws MessagingException;
	
}
