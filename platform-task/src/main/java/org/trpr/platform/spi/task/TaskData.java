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

import org.trpr.platform.model.common.BusinessEntity;

/**
 * The <code>TaskData</code> defines an interface for passing data used in execution of a {@link Task}.
 * In its most basic form, the TaskData provides for one or more {@link BusinessEntity} and a set of {@link Resource}
 * 
 * @author Regunath B
 * @version 1.0, 01/06/2012
 */
public interface TaskData <T,S> {
	
	/**
	 * Adds the specified entity data to this TaskData
	 * @param entity the data for task execution
	 */
	public void addEntity(T... entities);
	
	/**
	 * Adds the specified resource to this TaskData
	 * @param resource the resource used by the task
	 */
	public void addResource(S... resources);
	
	/**
	 * Gets zero or more entities identified by the specified name that this TaskData holds
	 * @param name the name for the entity
	 * @return zero or more entities
	 */
	public T[] getEntityByName(String name);
	
	/**
	 * Gets zero or more resources identified by the specified name that this TaskData holds
	 * @param name the name for the resource
	 * @return zero or more resources
	 */
	public S[] getResourceByName(String name);
	
}
