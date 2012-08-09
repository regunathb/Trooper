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

import java.util.Date;

import org.trpr.platform.core.util.DateUtils;

/**
 * The <code>ExecutionResult</code> class represents a single result reported by the task execution framework classes.
 * Each result has a message (usually designed to be accessible to the end user), a label (usually used to associate
 * the failure with an UI component / domain object field, though a label is not required), a severity to indicate whether the execution result is one of
 * information, warning or error. 
 * Interface code (such as a web framework or service) can use instances of this class to display the execution failures.  
 * There should be sufficient information associated with each execution result to apply the message to the correct location 
 * within the interface.
 * 
 * @author Regunath B
 * @version 1.0, 24/05/2012
 */

public class ExecutionResult {

	/** Severity place holder*/
	private Severity severity = Severity.ERROR;
	
	/** Execution result code place holder*/
	private int resultCode;

	/** Execution result code place holder*/
	private String codeName;

	/** Execution result  place holder*/
	private ResultCode result;
	
	/** Message that may be one of the following in order : localized message --> message code --> default message*/
	private String message;
	
	/** Label that identifies the UI field or domain object field*/
	private String label;
	
	/** Date time that identifies when this result was created*/
	private Date createdTime = DateUtils.getCurrentTime();

	/** No args constructor*/
	public ExecutionResult() {}
	
	/** Other constructors*/ 
	// start ///
	public ExecutionResult(Severity severity) {
		this.severity = severity;
	}

	public ExecutionResult(String message) {
		this.message = message;
	}
	
	public ExecutionResult(ResultCode result) {
		this.result = result;
		this.severity = result.getSeverity();
		this.resultCode = result.getCode();
		this.codeName = result.toString();
		this.message = result.getMessage();
	}
	
	public ExecutionResult(ResultCode result, String message) {
		this.result = result;
		this.severity = result.getSeverity();
		this.resultCode = result.getCode();
		this.codeName = result.toString();
		this.message = message;
	}
	
	public ExecutionResult(Severity severity, ResultCode result) {
		this.severity = severity;
		this.result = result;
		this.resultCode = result.getCode();
		this.codeName = result.toString();
		this.message = result.getMessage();
	}

	public ExecutionResult(Severity severity, ResultCode result, String message, String label) {
		this.severity = severity;
		this.result = result;
		this.resultCode = result.getCode();
		this.codeName = result.toString();
		this.message = message;
		this.label = label;
	}
	// constructors end//

	/**
	 * Overriden superclass method. Calls super class method and returns if true.
	 * Else, returns true if severity, result code and label matches. Message is ignored.
	 * @param the ExecutionResult object to be checked for equality
	 * @return true if objects are the same or if severity and label of the specified ExecutionResult are the same as the instance of this class
	 */
	public boolean equals(Object object) {
		boolean equal = super.equals(object);
		if (!equal) {
			ExecutionResult result = (ExecutionResult)object;
			return (this.severity == result.getSeverity() && 
					this.resultCode == result.getResultCode() &&
					this.label.equalsIgnoreCase(result.getLabel()));
		}
		return equal;
	}
	
	/*==== Javabean like getter and setter methods start ===*/
	public Severity getSeverity() {
		return severity;
	}
	public void setSeverity(Severity severity) {
		this.severity = severity;
	}
	public int getResultCode() {
		return resultCode;
	}
	public ResultCode getResult() {
		return result;
	}
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}
	public String getCodeName() {
		return codeName;
	}
	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Date getCreatedTime() {
		return createdTime;
	}
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	
	/*==== Javabean like getter and setter methods end ===*/
	
	/**
	 * Override default impl of Object
	 */
	public int hashCode() {
		return severity.hashCode() + resultCode;
	}
}
