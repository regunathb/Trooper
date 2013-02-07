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
package org.trpr.platform.integration.spi.marshalling;

import org.trpr.platform.core.PlatformException;

/**
 * Class <code>MarshallingException</code> is a sub-type of the {@link PlatformException} used in the data marshalling modules of the platform.
 *  
 * @author Regunath B
 * @version 1.0, 18/09/2012
 */

public class MarshallingException extends PlatformException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for MarshallingException.
	 * @param msg the detail message
	 */
	public MarshallingException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for MarshallingException.
	 * @param msg the detail message
	 * @param cause the root cause 
	 */
	public MarshallingException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
