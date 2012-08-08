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

/**
 * Class <code>DataRetrievalFailureException</code> is a sub-type of the {@link NonTransientPersistenceException} thrown if certain expected data 
 * could not be retrieved, e.g. when looking up specific data via a known identifier. This exception will be thrown either by O/R mapping tools 
 * or by DAO implementations
 *  
 * @author Regunath B
 * @version 1.0, 21/05/2012
 */
public class DataRetrievalFailureException extends NonTransientPersistenceException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructor for DataRetrievalFailureException.
	 * @param msg the detail message
	 */
	public DataRetrievalFailureException(String msg) {
		super(msg);
	}

	/**
	 * Constructor for DataRetrievalFailureException.
	 * @param msg the detail message
	 * @param cause the root cause from the data access API in use
	 */
	public DataRetrievalFailureException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
}
