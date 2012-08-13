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

/**
 * The <code>TaskData</code> defines an implementation for passing data used in execution of a {@link Task}.
 * In its most basic form, the TaskData provides for one or more {@link BusinessEntity} and a set of {@link Resource}
 * 
 * @author Regunath B
 * @version 1.0, 01/06/2012
 */
public abstract class TaskData <T extends BusinessEntity,S extends Resource> {
	
	/** The list of entities containing task execution application data*/
	private List<T> entitiesList = new LinkedList<T>();	

	/** The list of resources for task execution */
	private List<S> resourcesList = new LinkedList<S>();	

	/**
	 * Adds the specified entity data to this TaskData
	 * @param entity the data for task execution
	 */
	public void addEntity(T... entities) {
		for (T entity : entities) {
			this.entitiesList.add(entity);
		}		
	}
	
	/**
	 * Adds the specified resource to this TaskData
	 * @param resource the resource used by the task
	 */
	public void addResource(S... resources) {
		for (S resource : resources) {
			this.resourcesList.add(resource);
		}		
	}
	
	/**
	 * Gets zero or more entities identified by the specified name that this TaskData holds
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
	
	/**
	 * Gets zero or more resources identified by the specified name that this TaskData holds
	 * @param name the name for the resource
	 * @return zero or more resources
	 */
	@SuppressWarnings("unchecked")	
	public S[] getResourceByName(String name) {
		List<S> returnResourcesList = new LinkedList<S>();
		for (S resource : this.resourcesList) {
			if (resource.getName().equals(name)) {
				returnResourcesList.add(resource);
			}
		}	
		return (S[])returnResourcesList.toArray(new Resource[0]);				
	}
	
}
