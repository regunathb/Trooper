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
 * The <code>PersistenceHandler</code> provides methods to read/write {@link PersistentEntity} instances from respective underlying data stores. Implementations
 * of this interfaces are used by {@link PersistenceProvider} to perform the actual persistence operations. 
 * Multiple handler implementations can exist for a provider - for e.g. an RDBMS Persistence Provider may use one of these handlers - Hibernate handler, iBatis handler. 
 * 
 * The methods in this interface mimic one-to-one that of a PersistenceProvider
 * 
 * @author Ashok Ayengar
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public interface PersistenceHandler {

	/**
	 * Creates/Updates the specified PersistentEntity to the underlying data store
	 * @param entity the PersistentEntity to be persisted
	 * @return the PersistentEntity that was persisted. May contain additionally populated attributes like {@link Identifier} post create operation. 
	 * @throws PersistenceException or one of its relevant sub-types in case of errors during persistence. See PersistenceException type hierarchy. 
	 */
	public PersistentEntity makePersistent(PersistentEntity entity) throws PersistenceException;

	/**
	 * Deletes the specified PersistentEntity form the underyling data store. Returns quietly if the specified entity was not found (or) was already
	 * deleted.
	 * @param entity the PersistentEntity to be deleted
	 * @throws PersistenceException or one of its relevant sub-types in case of errors during persistence. See PersistenceException type hierarchy. 
	 */
	public void makeTransient(PersistentEntity entity) throws PersistenceException;

	/**
	 * Loads and returns a PersistentEntity using the Criteria specified. Throws relevant PersistenceException if number of returned entities is not
	 * exactly one.
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
	 * Method to fetch a collection of entities of type <code>PersistentEntity</code> from the underlying persistence mechanism.
	 * 
	 * @param entity - A entity instance of type <code>PersistentEntity</code>, similar to which the object has to be fetched.
	 * @return Collection of entities of type <code>PersistentEntity</code> matching the entity which was provided.
	 * @throws PersistenceException - In case of problems while accessing the persistence.
	 */
	public Collection<PersistentEntity> findObject(PersistentEntity entity) throws PersistenceException;
	
	
	
	

}