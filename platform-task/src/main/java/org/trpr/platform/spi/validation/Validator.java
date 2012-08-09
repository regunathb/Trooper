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

package org.trpr.platform.spi.validation;

import org.trpr.platform.spi.execution.ResultCode;

/**
 * The <code>Validator</code> interface exposes methods to be implemented by all
 * basic validator implementations i.e. ones that work with basic types such as
 * Java primitive and reference types.
 * 
 * @author Regunath B
 * @version 1.0, 24/05/2012
 */

public interface Validator {

	/**
	 * Validates the specified objects and returns one of more ValidationResult
	 * instances that contain the results of validation. Implementations may
	 * return null if the specified objects are valid.
	 * 
	 * @param optional expression or input parameter used in the validation. For e.g. date range values specified as an expression
	 * @param resultCode optional result code to uniquely identify the result
	 * @param inputObject object to be validated
	 * @return null or array of ValidationResult instances
	 */
	public ValidationResult[] validate(String expression, ResultCode resultCode, Object inputObject);
}
