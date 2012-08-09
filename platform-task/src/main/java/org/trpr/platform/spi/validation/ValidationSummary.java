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

import java.util.LinkedList;
import java.util.List;

import org.trpr.platform.spi.execution.ExecutionResult;
import org.trpr.platform.spi.execution.ExecutionSummary;
import org.trpr.platform.spi.execution.Severity;

/**
 * The <code>ValidationSummary</code> class is a sub-type of {@link ExecutionSummary}
 * used to store a collection of results from execution of validation processing.
 * 
 * @see ExecutionSummary
 * @author Regunath B 
 * @version 1.0, 24/05/2012
 */

public class ValidationSummary extends ExecutionSummary {

	/**
	 * Convenience method to determine overall status of a validation.
	 * Returns true if no results were added or if no results with severity of ERROR or FATAL were added to this validation summary
	 * @return true if the validation was a success i.e. empty results or severity of INFO for all contained results 
	 */
	public boolean isValidationSuccess() {
		return super.isExecutionSuccess();
	}
	
	/**
	 * Convenience method to see if there are any validation errors in the results
	 * @return true if any of the contained validation results are of severity ERROR and above
	 */
	public boolean hasValidationErrors() {
	    return super.hasExecutionErrors();		
	}
	
	/**
	 * Convenience method to see if there are any FATAL validation errors in the results
	 * @return true if any of the contained validation results are of severity FATAL
	 */
	public boolean hasFatalValidationErrors() {
	    return super.hasFatalExecutionErrors();				
	}

	/**
	 * Returns all the ValidationResult instances that were added to this execution summary. 
	 * Calls {@link ExecutionSummary#getAllResults()} and returns the results cast into an ValidationResult array
	 * @return array of ValidationResult instances contained in this execution summary
	 */
	public ValidationResult[] getAllResults() {
		return (ValidationResult[])this.results.toArray(new ValidationResult[0]);
	}
	
	/**
	 * Returns all the ValidationResult instances from this execution summary that match the specified
	 * severity. Calls {@link ExecutionSummary#getResultsBySeverity(int)} and returns the results cast into an ValidationResult array
	 * @param severity the severity of the ValidationResult. 
	 * @return array of ValidationResult instances that match the specified severity
	 */
	public ValidationResult[] getResultsBySeverity(Severity severity) {
		List<ExecutionResult> list = new LinkedList<ExecutionResult>();
	    for (ExecutionResult result : this.results) {
	    	if (result.getSeverity() == severity) {
	    		list.add(result);
	    	}
	    }
		return (ValidationResult[])list.toArray(new ValidationResult[0]);
	}
	
}
