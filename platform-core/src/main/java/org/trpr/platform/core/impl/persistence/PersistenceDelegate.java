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

package org.trpr.platform.core.impl.persistence;

import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistenceProvider;
import org.trpr.platform.core.spi.persistence.PersistentEntity;
import org.trpr.platform.core.spi.persistence.sharding.ShardedPersistentEntity;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The <code>PersistenceDelegate</code> class implements persistence methods for use by the {@link PersistenceManagerProvider}.   
 * Persistence calls on {@link PersistentEntity} instances instances are delegated to this class by the PersistenceManagerProvider. 
 * This class is particularly useful when the PersistentEntity type is {@link ShardedPersistentEntity} where the PersistenceManagerProvider 
 * can populate the {@link ShardedEntityContextHolder} with appropriate values before making the persistence call on the {@link PersistenceProvider}. 
 * 
 * Transaction demarcation is done on methods of this class instead of on the PersistenceManagerProvider. This is done in order to avoid eager connection fetching 
 * issues experienced when using frameworks like Hibernate when the PersistentEntity type is ShardedPersistentEntity. The transaction interceptor
 * fetches the connection even before the shard hint has been set. Therefore the shard hint is typically set in the namesake methods of the 
 * PersistenceManagerProvider before invoking the methods of this class.
 *  
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public class PersistenceDelegate {

	/**
	 * Persists the specified PersistentEntity using the specified PersistenceProvider. Note that this method is transactional by default.
	 * It is advisable to use {@link PersistenceManagerProvider#makePersistent(PersistentEntity)} instead of calling this method directly.
	 * @param entity the PersistentEntity to persist
	 * @param provider the PersistenceProvider to use in persistence
	 * @return the PersistentEntity that was persisted
	 * @throws PersistenceException in case of persistence errors
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW,isolation=Isolation.DEFAULT ,rollbackForClassName={"Exception"})
	public PersistentEntity makePersistent(PersistentEntity entity, PersistenceProvider provider) throws PersistenceException {
		entity = provider.makePersistent(entity);
		return entity;
	}
	
	/**
	 * Deletes the specified PersistentEntity using the specified PersistenceProvider. Note that this method is transactional by default.
	 * It is advisable to use {@link PersistenceManagerProvider#makeTransient(PersistentEntity)} instead of calling this method directly.
	 * @param entity the PersistentEntity to delete
	 * @param provider the PersistenceProvider to use in deletion
	 * @throws PersistenceException in case of deletion errors
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW,isolation=Isolation.DEFAULT ,rollbackForClassName={"Exception"})
	public void makeTransient(PersistentEntity entity, PersistenceProvider provider) throws PersistenceException {
		provider.makeTransient(entity);
	}
	
	/**
	 * Persists the specified PersistentEntity instances using the specified PersistenceProvider instances, matched by index positions. 
	 * Note that this method is transactional by default.
	 * It is advisable to use {@link PersistenceManagerProvider#makePersistent(PersistentEntity[])} instead of calling this method directly.
	 * @param entities the PersistentEntity instances to persist
	 * @param providers the PersistenceProvider instances to use in persistence
	 * @return  PersistentEntity instances that were persisted
	 * @throws PersistenceException in case of persistence errors
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW,isolation=Isolation.DEFAULT ,rollbackForClassName={"Exception"})
	public PersistentEntity[] makePersistent(PersistentEntity[] entities, PersistenceProvider[] providers) throws PersistenceException {
		for (int i=0; i<entities.length; i++) {
			providers[i].makePersistent(entities[i]);
		}
		return entities;
	}
	
	/**
	 * Deletes the specified PersistentEntity instances using the specified PersistenceProvider instances, matched by index positions. 
	 * Note that this method is transactional by default.
	 * It is advisable to use {@link PersistenceManagerProvider#makeTransient(PersistentEntity[])} instead of calling this method directly.
	 * @param entities the PersistentEntity instances to delete
	 * @param providers the PersistenceProvider instances to use in delete
	 * @return  PersistentEntity instances that were deleted
	 * @throws PersistenceException in case of deletion errors
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW,isolation=Isolation.DEFAULT ,rollbackForClassName={"Exception"})
	public void makeTransient(PersistentEntity[] entities, PersistenceProvider[] providers) throws PersistenceException {
		for (int i=0; i<entities.length; i++) {
			providers[i].makeTransient(entities[i]);
		}
	}

	/**
	 * Updates the underlying data store with information available in the specified Criteria using the specified PersistenceProvider
	 * Note that this method is transactional by default.
	 * It is advisable to use {@link PersistenceManagerProvider#update(Criteria)} instead of calling this method directly.	 
	 * @param criteria the Criteria instance containing update data fields and values
	 * @param provider the PersistenceProvider to use for the persistence call
	 * @return int identifying the success status of update operation
	 * @throws PersistenceException in case of errors during persistence
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW,isolation=Isolation.DEFAULT ,rollbackForClassName={"Exception"})
	public int update(Criteria criteria, PersistenceProvider provider) throws PersistenceException {
		return provider.update(criteria);
	}
	
	/**
	 * Updates the underlying data store with information available in the specified Criteria instances using the specified PersistenceProvider
	 * instances, with matching indexes. Note that this method is transactional by default.
	 * It is advisable to use {@link PersistenceManagerProvider#update(Criteria...)} instead of calling this method directly.	 
	 * @param providers the PersistenceProvider instances to be used in persistence of the Criteria information, matched by indices
	 * @param criteria the Criteria instances whose information must be persisted
	 * @return int array containing the outcome status of update operation
	 * @throws PersistenceException in case of peristence errors
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW,isolation=Isolation.DEFAULT ,rollbackForClassName={"Exception"})
	public int[] update(PersistenceProvider[] providers, Criteria... criteria) throws PersistenceException {
		int[] returnValue = new int[criteria.length];
		for (int i=0; i<criteria.length; i++) {
			returnValue[i] = providers[i].update(criteria[i]);
		}
		return returnValue;
	}
	
}
