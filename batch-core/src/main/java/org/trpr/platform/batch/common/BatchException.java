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

package org.trpr.platform.batch.common;

import org.trpr.platform.core.PlatformException;

/**
*
* The <code>BatchException</code> class is a generic exception class for the Trooper Batch profile runtime
* @author  Regunath B
* @version 1.0, 28/08/2012
*/
public class BatchException extends PlatformException {

	/**
	 * The serial version UID
	 */
	private static final long serialVersionUID = 612618001407882432L;

	/**
	 * No args constructor
	 */
	public BatchException() {
	}

	/**
	 * Constructs an instance of this Exception with the specified exception message
	 * @param message String message for this Exception
	 */
	public BatchException(String message) {
		super(message);
	}

	/**
	 * Constructs an instance of this Exception with the specified cause
	 * @param cause the Throwable cause for this Exception
	 */
	public BatchException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs an instance of this Exception with the specified exception message
	 * and cause
	 * @param message String message for this Exception
	 * @param cause the Throwable cause for this Exception
	 */
	public BatchException(String message, Throwable cause){
		super(message, cause);
	}

}
