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
 * Class <code>MessagingTimeoutException</code> is a sub-type of the {@link MessagingException} used to signal timeout during messaging calls
 *  
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */
public class MessagingTimeoutException extends MessagingException {

	private static final long serialVersionUID = 1L;
	
	/** The timeout duration in milli-seconds */
	private long timeoutMillis;
	
	/**
	 * Constructor for this Exception
	 * @param timeout the timeout duration in milli seconds that has occurred
	 */
	public MessagingTimeoutException(long timeout) {
		super("Timeout ocurred in messaging call. Timeout duration : " + timeout);
		this.timeoutMillis = timeout;
	}
	
	/**
	 * Constructor for this Exception
	 * @param message the Exception message as String
	 * @param timeout the timeout duration in milli seconds that has occurred
	 */
	public MessagingTimeoutException(String message, long timeout) {
		super(message);
		this.timeoutMillis = timeout;
	}
	
	/**
	 * Returns the timeout duration in millis
	 * @return timeout that ocurred in millis
	 */
	public long getTimeoutMillis() {
		return this.timeoutMillis;
	}
	
}
