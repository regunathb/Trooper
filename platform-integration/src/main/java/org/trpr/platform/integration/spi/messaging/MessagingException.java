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

import org.trpr.platform.core.PlatformException;

/**
 * Class <code>MessagingException</code> is a sub-type of the {@link PlatformException} used in the messaging module of the platform.
 *  
 * @author Regunath B
 * @version 1.0, 28/05/2012
 */

public class MessagingException extends PlatformException {

	/** The serial version UID*/
	private static final long serialVersionUID = 1L;
	
	/** Relevant error codes for this Exception */
	public static final int CONNECTION_FAILURE = 100;
	public static final int QUEUE_EMPTY = 101;
	
	/**
	 * Constructor for MessagingException.
	 * @param msg the detail message
	 */
	public MessagingException(String msg) {
		super(msg);
	}

	/**
	 * Constructs an instance of this MessagingException with the specified exception message and error code
	 * @param message String message for this Exception
	 * @param errorCode the errorCode for the message
	 */
	public MessagingException(String message, int errorCode) {
		super(message, errorCode);
	}
	
	/**
	 * Constructor for MessagingException.
	 * @param msg the detail message
	 * @param cause the root cause 
	 */
	public MessagingException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * Constructs an instance of this MessagingException with the specified exception message, error code and cause
	 * @param message String message for this Exception
	 * @param cause the Throwable cause for this Exception
	 * @param errorCode the exception error code
	 */
	public MessagingException(String message, Throwable cause, int errorCode) {
		super(message, cause, errorCode);
	}
	
}
