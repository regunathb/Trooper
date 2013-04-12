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

package org.trpr.platform.core;

/**
 * The <code>PlatformException</code> is a generic runtime exception for the framework that supports exception chaining.
 * Follows the Spring practice of using runtime exceptions instead of checked exceptions. 
 * 
 * @author Regunath B
 * @version 1.0, 15/05/2012
 */

public class PlatformException extends RuntimeException {

	/** The serial version UID*/
	private static final long serialVersionUID = -4064529933657504359L;
	
	/** Constants for error codes */
	public static final int NO_CODE = -1;

	/** Optional error code for this Exception */
	private int errorCode = NO_CODE;

	/**
	 * No args constructor
	 */
	public PlatformException() {
	}

	/**
	 * Constructs an instance of this Exception with the specified exception message
	 * @param message String message for this Exception
	 */
	public PlatformException(String message) {
		super(message);
	}

	/**
	 * Constructs an instance of this Exception with the specified exception message and error code
	 * @param message String message for this Exception
	 * @param errorCode the errorCode for the message
	 */
	public PlatformException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	/**
	 * Constructs an instance of this Exception with the specified cause
	 * @param cause the Throwable cause for this Exception
	 */
	public PlatformException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an instance of this Exception with the specified exception message and cause
	 * @param message String message for this Exception
	 * @param cause the Throwable cause for this Exception
	 */
	public PlatformException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an instance of this Exception with the specified exception message, error code and cause
	 * @param message String message for this Exception
	 * @param cause the Throwable cause for this Exception
	 * @param errorCode the exception error code
	 */
	public PlatformException(String message, Throwable cause, int errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}
	
	/**
	 * Gets the root cause for this Exception
	 * @return the root cause of this Exception
	 */
	public Throwable getRootCause() {
		Throwable e = getCause();
		Throwable eParent;
		for (eParent = this; e != null && e != eParent; e = e.getCause()) {
			eParent = e;
		}
		return eParent;
	}

	/**
	 * Returns the errorcode for this Exception or -1 if none was specified
	 * @return -1 or the error code this Exception was created with
	 */
	public int getErrorCode() {
		return errorCode;
	}

}
