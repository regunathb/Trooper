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

import java.util.Collection;

/**
 * The <code>PersistenceManager</code> is the primary client facing interface of the Persistence framework. Provides methods to perform mostly  
 * CRUDL (Create, Read, Update, Delete, List) operations on data. The operations listed is a superset for the framework and the ones actually supported 
 * might vary depending on type of {@link PersistentEntity} and the active {@link PersistenceProvider} mapped to it. 
 * 
 * This interface also supports grouped operations where relevant - for e.g. persisting an array of PersistentEntity instances. The respective persistence
 * providers may decorate these operations suitably - for e.g. the RDBMS provider may create a transaction boundary for each create/update/delete method call.
 * 
 * @author Ashok Ayengar
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */

public interface PersistenceManager {

	/**
	 * Creates/Updates the specified PersistentEntity to the underlying data store
	 * @param entity the PersistentEntity to be persisted
	 * @return the PersistentEntity that was persisted. May contain additionally populated attributes like {@link Identifier} post create operation. 
	 * @throws PersistenceException or one of its relevant sub-types in case of errors during persistence. See PersistenceException type hierarchy. 
	 */
	public PersistentEntity makePersistent(PersistentEntity entity) throws PersistenceException;
	
	/**
	 * Variant of {@link #makePersistent(PersistentEntity)} that supports an array of PersistentEntity in method parameter and return type 
	 */
	public PersistentEntity[] makePersistent(PersistentEntity[] entities) throws PersistenceException;	
	
	/**
	 * Deletes the specified PersistentEntity form the underyling data store. Returns quietly if the specified entity was not found (or) was already
	 * deleted.
	 * @param entity the PersistentEntity to be deleted
	 * @throws PersistenceException or one of its relevant sub-types in case of errors during persistence. See PersistenceException type hierarchy. 
	 */
	public void makeTransient(PersistentEntity entity) throws PersistenceException;
	
	/**
	 * Variant of {@link #makeTransient(PersistentEntity)} that supports an array of PersistentEntity in method parameter 
	 */
	public void makeTransient(PersistentEntity[] entities) throws PersistenceException;	
	
	/**
	 * Loads and returns a PersistentEntity using the Criteria specified. Throws relevant PersistenceException if number of returned entities is not
	 * exactly one (or) returns the first successfully retrieved entity.
	 * @param criteria the Criteria to use for retrieving the PersistentEntity from underlying data store
	 * @return PersistentEntity retrieved from the underlying data store
	 * @throws PersistenceException or one of its relevant sub-types in case of errors during persistence. See PersistenceException type hierarchy. 
	 */
	public PersistentEntity findEntity(Criteria criteria) throws PersistenceException;
	
	/**
	 * Loads and returns a PersistentEntity using {@link PersistentEntity#getIdentifier()} if specified, or {@link PersistentEntity#getCriteriaForLoad()} using that 
	 * order of preference. Throws relevant PersistenceException if number of returned entities is not exactly one.
	 * @param entity the PersistentEntity to use for retrieving the PersistentEntity data from underlying data store
	 * @return PersistentEntity retrieved from the underlying data store
	 * @throws PersistenceException or one of its relevant sub-types in case of errors during persistence. See PersistenceException type hierarchy. 
	 */
	public PersistentEntity findEntity(PersistentEntity entity) throws PersistenceException;
	
	/**
	 * Retrieves and returns a Collection of PersistentEntity instances from underlying data store using the specified Criteria.  
	 * @param criteria the Criteria for loading entities from persistent store
	 * @return Collection of PersistentEntity instances
	 * @throws PersistenceException or one of its relevant sub-types in case of errors during persistence. See PersistenceException type hierarchy.
	 */
	public Collection<PersistentEntity> findEntities(Criteria criteria) throws PersistenceException;
	
	/**
	 * Updates the underlying data store using data in the specified Criteria. This method violates Object-Persistence mapping by providing access
	 * to the data store using query constructs as supported by the data store. Use of this method is generally discouraged.
	 * @param criteria the Criteria containing information for update operation
	 * @return count of records/documents/columns updated in the underlying data store
	 * @throws PersistenceException or one of its relevant sub-types in case of errors during persistence. See PersistenceException type hierarchy.
	 */
	public int update(Criteria criteria) throws PersistenceException;

	/**
	 * Variant of {@link #update(Criteria)} that supports multiple Criteria in method parameter and array in return type 
	 */	
	public int[] update(Criteria...criteria) throws PersistenceException;
	
}
