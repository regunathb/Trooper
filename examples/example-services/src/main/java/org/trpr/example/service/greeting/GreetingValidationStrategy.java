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
package org.trpr.example.service.greeting;

import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.impl.validation.AbstractBusinessEntityValidationStrategy;
import org.trpr.platform.spi.execution.ResultCode;
import org.trpr.platform.spi.execution.Severity;
import org.trpr.platform.spi.validation.ValidationSummary;

/**
 * The Validation Strategy to validate data sent to GreetingService.
 * @author Regunath B
 * @version 1.0, 17/08/2012
 */
public class GreetingValidationStrategy extends AbstractBusinessEntityValidationStrategy {
	
	/** Greeting Error code enum */
	public enum GreetingResult implements ResultCode {
		INVALID_FIRST_NAME (101, "First name should not exceed 10 characters", Severity.ERROR),
		INVALID_LAST_NAME  (102, "Last name should not exceed 10 characters", Severity.ERROR),
		INVALID_DATE_OF_BIRTH (103, "Date of birth cannot be later than current date", Severity.ERROR),
		;
		
		private int code;
		private String message;
		private Severity severity;
		
		private GreetingResult(int code, String message, Severity severity) {
			this.code = code;
			this.message = message;
			this.severity = severity;
		}
		@Override
		public int getCode() {
			return code;
		}

		@Override
		public String getMessage() {
			return this.message;
		}

		@Override
		public Severity getSeverity() {
			return this.severity;
		}
	}

	/**
	 * Constructor 
	 * @param earthling
	 */
	public GreetingValidationStrategy(Earthling earthling) {
		super(earthling);
	}

	/**
	 * Overridden method to validate the first name, last name and date of birth sent with the request.  
	 */
	protected ValidationSummary validate() {
		super.validate("firstName.length() < 10", GreetingResult.INVALID_FIRST_NAME, null);
		super.validate("lastName.length() < 10", GreetingResult.INVALID_LAST_NAME, null);
		super.validate("dateOfBirth.before(Calendar.getInstance())", GreetingResult.INVALID_DATE_OF_BIRTH,null);
		return super.getValidationResults();
	}

}
