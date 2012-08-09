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

import java.util.LinkedList;
import java.util.List;

/**
 * The <code>ExecutionSummary</code> class is a collection of results from execution of code blocks in a Task or Validation.  
 * 
 * @author Regunath B 
 * @version 1.0, 24/05/2012
 */

public class ExecutionSummary {

	/** Collection of execution results*/
	protected List<ExecutionResult> results = new LinkedList<ExecutionResult>();

	/**
	 * Adds the specified ExecutionResult to this execution summary
	 * @param result the ExecutionResult to be added to this execution summary
	 */
	public ExecutionSummary addResult(ExecutionResult result) {
		this.results.add(result);
		return this;
	}

	/**
	 * Adds the specified ExecutionResult instances to this execution summary
	 * @param results the ExecutionResult instances to be added to this execution summary
	 */
	public ExecutionSummary addResults(ExecutionResult[] results) {
		for (ExecutionResult result : results) {
			this.addResult(result);
		}
		return this;
	}

	/**
	 * Removes the specified ExecutionResult from this execution summary
	 * @param result the ExecutionResult to be removed from this execution summary
	 */
	public void removeResult(ExecutionResult result) {
		this.results.remove(result);
	}
	
	/**
	 * Removes all ExecutionResult instances that were added to this execution summary
	 */
	public void removeAllResults() {
		this.results.clear();
	}
	
	/**
	 * Convenience method to determine overall status of a execution.
	 * Returns true if no results were added or if no results with severity of ERROR or FATAL were added to this execution summary
	 * @return true if the execution was a success i.e. empty results or severity of INFO for all contained results 
	 */
	public boolean isExecutionSuccess() {
		if (this.results.isEmpty()) {
			return true;
		}
		return !hasExecutionErrors();
	}
	
	/**
	 * Convenience method to see if there are any execution errors in the results
	 * @return true if any of the contained execution results are of severity ERROR and above
	 */
	public boolean hasExecutionErrors() {
	    for (ExecutionResult result : this.results) {
	    	if (result.getSeverity().getCode() >= Severity.ERROR.getCode()) {
	    		return true;
	    	}
	    }
	    return false;		
	}
	
	/**
	 * Convenience method to see if there are any FATAL execution errors in the results
	 * @return true if any of the contained execution results are of severity FATAL
	 */
	public boolean hasFatalExecutionErrors() {
	    for (ExecutionResult result : this.results) {
	    	if (result.getSeverity() == Severity.FATAL) {
	    		return true;
	    	}
	    }
	    return false;				
	}

	/**
	 * Returns all the ExecutionResult instances that were added to this execution summary
	 * @return array of ExecutionResult instances contained in this execution summary
	 */
	public ExecutionResult[] getAllResults() {
		return (ExecutionResult[])this.results.toArray(new ExecutionResult[0]);
	}
	
	/**
	 * Returns all the ExecutionResult instances from this execution summary that match the specified
	 * severity. 
	 * @param severity the severity of the ExecutionResult. 
	 * @return array of ExecutionResult instances that match the specified severity
	 */
	public ExecutionResult[] getResultsBySeverity(Severity severity) {
		List<ExecutionResult> list = new LinkedList<ExecutionResult>();
	    for (ExecutionResult result : this.results) {
	    	if (result.getSeverity() == severity) {
	    		list.add(result);
	    	}
	    }
		return (ExecutionResult[])list.toArray(new ExecutionResult[0]);
	}
	
	/**
	 * Convenience method to see if there are any FATAL execution errors in the results
	 * @return true if any of the contained execution results are of severity FATAL
	 */
	public boolean hasThisExecutionResult(ResultCode rCode) {
	    for (ExecutionResult result : this.results) {
	    	if (result.getCodeName().equalsIgnoreCase(rCode.toString())) {
	    		return true;
	    	}
	    }
	    return false;				
	}
}
