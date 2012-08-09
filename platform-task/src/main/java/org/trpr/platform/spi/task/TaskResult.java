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
package org.trpr.platform.spi.task;

import java.util.LinkedList;
import java.util.List;

import org.trpr.platform.model.common.BusinessEntity;
import org.trpr.platform.spi.execution.ResultCode;
import org.trpr.platform.spi.execution.Severity;

/**
 * The <code>TaskResult</code> contains results from execution of a {@link Task} 
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */

public abstract class TaskResult<T extends BusinessEntity> {
	
	/** The TaskResultCode denoting outcome of execution */
	private TaskResultCode resultCode;
	
	/** The list of entities containing task execution application data*/
	private List<T> entitiesList = new LinkedList<T>();	
	
	/**
	 * Adds the specified entity data to this TaskResult
	 * @param entity the data from task execution
	 */
	public void addEntity(T... entities) {
		for (T entity : entities) {
			this.entitiesList.add(entity);
		}
	}
	
	/**
	 * Gets zero or more entities identified by the specified name that this TaskResult holds
	 * @param name the name for the entity
	 * @return zero or more entities
	 */
	@SuppressWarnings("unchecked")
	public T[] getEntityByName(String name) {
		List<T> returnEntitiesList = new LinkedList<T>();
		for (T entity : this.entitiesList) {
			if (entity.getEntityName().equals(name)) {
				returnEntitiesList.add(entity);
			}
		}	
		return (T[])returnEntitiesList.toArray(new BusinessEntity[0]);
	}
	
	/** ===== Start Getter/Setter methods*/
	public TaskResultCode getResultCode() {
		return this.resultCode;
	}
	public void setResultCode(TaskResultCode resultCode) {
		this.resultCode = resultCode;
	}	
	/** ===== End Getter/Setter methods*/
	
	/** Task result codes */
	public static enum TaskResultCode implements ResultCode {
		SUCCESS(100,"Task execution success", Severity.INFO), // task execution is a success
		FAILURE(101,"Task execution failure", Severity.INFO), // task failed
		FAILURE_FATAL(102,"Task execution fatal failure", Severity.INFO), // task failed and may indicate futility of further task execution - of this one or others thereafter
		FAILURE_RETRY(103,"Task execution failure. Retry", Severity.INFO), // task execution may be retried to achieve successful execution
		;
		
		/** Member variable*/
		private int code;
		private String message;		
		private Severity severity;
		
		/** Private constructor*/
		private TaskResultCode(int code, String message,  Severity severity){
			this.code = code;
			this.message = message;		
			this.severity = severity;
		}

		/** Returns the task result code value*/
		public int getCode() {
			return this.code;
		}		

		/** Returns the task result message*/
		public String getMessage() {
			return this.message;
		}

		/** Returns the severity, almost always {@link Severity#INFO}*/
		public Severity getSeverity() {
			return this.severity;
		}
		
	}
	
}
