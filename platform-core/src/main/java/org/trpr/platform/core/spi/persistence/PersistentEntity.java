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

package org.trpr.platform.core.spi.persistence;

/**
 * The <code>PersistentEntity</code> is a type identifier for any/all entities persisted to a data store. The PersistentEntity is usually derived from 
 * a business domain model (or) information schema. Each PersistentEntity instance is uniquely identified by a {@link Identifier} type.
 * 
 * Provides for a logical name for the PersistentEntity which is usually a common name. Also supports specifying a {@link Criteria} that may be
 * used to load this Persistent entity when an Identifier is not specified.
 * 
 * @author Ashok Ayengar
 * @author Regunath B
 * @version 1.0, 22/05/2012
 */

public interface PersistentEntity {

	/**
	 * Returns a logical/common name for the PersistentEntity sub-type
	 * @return name of the persistent entity
	 */
	public String getEntityName();

	/**
	 * Returns an Identifier that uniquely identifies an instance of this type in the underlying data store
	 * @return unique Identifier in the underlying data store 
	 */
	public Identifier getIdentifier();
	
	/**
	 * Returns the Criteria instance to use for loading this PersistentEntity from the underlying data store.
	 * This method is used when {@link #getIdentifier()} is not specified. Especially useful for entities that are 
	 * persisted using surrogate keys and not their own natural keys - e.g. sequence number instead of a business data column
	 * @return Criteria used for loading this PersistentEntity from persistent store
	 */
	public Criteria getCriteriaForLoad();
		
}
