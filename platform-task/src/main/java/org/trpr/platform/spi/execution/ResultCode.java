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
package org.trpr.platform.spi.execution;

/**
 * The <code>ResultCode</code> defines a common interface for returning result metadata from execution of a unit of work such
 * as a Task or Validation. It holds a {@link Severity}, a distinct result code and the corresponding message. Concrete implementations
 * may extend this behavior.
 * 
 * @author Ashok Ayengar
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */

public interface ResultCode {

	/** The  Severity identifier for this ResultCode */
	public Severity getSeverity();
	
	/**
	 * Returns a distinct identifier for this ResultCode
	 * @return int the code identifier
	 */
	public int getCode();
	
	/**
	 * Returns the message associated with the ResultCode.
	 * @return String descriptive message for this ResultCode
	 */
	public String getMessage();
	
}
