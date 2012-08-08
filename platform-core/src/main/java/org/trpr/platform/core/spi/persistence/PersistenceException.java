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
package org.trpr.platform.core.spi.persistence;

import org.trpr.platform.core.PlatformException;

/**
 * Class <code>PersistenceException</code> is a sub-type of the {@link PlatformException} used in the Trooper persistence framework. 
 * Provides hierarchy and behavior identical to the Spring DAO project's DataAccessException
 *  
 * @author Regunath B
 * @version 1.0, 21/05/2012
 */

public class PersistenceException extends PlatformException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor 
	 * @param msg the detail message
	 */
	public PersistenceException(String msg) {
		super(msg);
	}

	/**
	 * Constructor 
	 * @param msg the detail message
	 * @param cause the root cause (usually from using a underlying data access API such as JDBC)
	 */
	public PersistenceException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
