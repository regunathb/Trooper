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

package org.trpr.platform.core.spi.logging;

import org.trpr.platform.core.PlatformException;

/**
 * The <code>LogConfigurationException</code> is sub-type of the PlatformException for use in the logging framework.  
 * 
 * @author Regunath B
 * @version 1.0, 17/05/2012
 */
public class LogConfigurationException extends PlatformException {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for LogConfigurationException.
	 * @param msg the detail message
	 */
	public LogConfigurationException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for LogConfigurationException.
	 * @param msg the detail message
	 * @param cause the root cause 
	 */
	public LogConfigurationException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
