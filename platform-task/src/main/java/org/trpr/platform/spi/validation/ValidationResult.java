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

import org.trpr.platform.spi.execution.ExecutionResult;
import org.trpr.platform.spi.execution.ResultCode;
import org.trpr.platform.spi.execution.Severity;

/**
 * The <code>ValidationResult</code> class is a sub-type of the {@link ExecutionResult} used for reporting validation
 * errors in the validation framework.
 * 
 * @see ExecutionResult
 * 
 * @author Regunath B
 * @version 1.0, 24/05/2012
 */
public class ValidationResult extends ExecutionResult{

	/** Constructors */
	public ValidationResult() {
		super();
	}
	public ValidationResult(Severity severity) {
		super(severity);
	}
	public ValidationResult(String message) {
		super(message);
	}
	public ValidationResult(ResultCode result) {
		super(result);
	}
	
	public ValidationResult(ResultCode result, String message) {
		super(result, message);
	}
	
	public ValidationResult(Severity severity, ResultCode result) {
		super(severity, result);
	}
	
	public ValidationResult(Severity severity, ResultCode result, String message, String label) {
		super(severity, result, message, label);
	}
	/** constructors end */

}
