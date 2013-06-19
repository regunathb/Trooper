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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.trpr.platform.core.impl.persistence.sharding.AbstractMultiShardedPersistentEntity;
import org.trpr.platform.core.impl.persistence.sharding.ShardedEntityContextHolder;
import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistenceManager;
import org.trpr.platform.core.spi.persistence.PersistenceProvider;
import org.trpr.platform.core.spi.persistence.PersistentEntity;
import org.trpr.platform.core.spi.persistence.sharding.MultiShardAwareCriteria;
import org.trpr.platform.core.spi.persistence.sharding.ShardAwareCriteria;
import org.trpr.platform.core.spi.persistence.sharding.ShardedEntity;
import org.trpr.platform.core.spi.persistence.sharding.ShardedPersistentEntity;

/**
 * <code>PersistenceManagerProvider<code> is an implementation of {@link PersistenceManager} that provides the primary client interface to the 
 * persistence framework.
 * 
 * {@link PersistenceProvider} to {@link PersistentEntity} mapppings are maintained by this class. An appropriate PersistenceProvider is selected from
 * this map during persistence calls for a PersistentEntity type.
 * 
 * This PersistenceManager is shard-aware and supports persistence calls on sharded entities.
 * 
 * @see PersistenceManager
 * @see PersistentEntity
 * @see ShardedEntity
 * 
 * @author Ashok Ayengar, Raja S, Regunath B
 * @version 1.0, 23/05/2012
 */

public class PersistenceManagerProvider implements PersistenceManager {
	
	/**
	 * Mapping between entities and it corresponding <code>PersistenceProvider</code> instances
	 */
	private Map<String, PersistenceProvider> m_EntityToProviders;
	
	/** The PersistenceDelegate instance to use for all persistence calls involving PersistentEntity instances*/
	private PersistenceDelegate persistenceDelegate;

	/**
	 * no-arg constructor.
	 */
	public PersistenceManagerProvider(){
	}

	/**
	 * Spring DI setter method.
	 * @param entityToProviders - mapping between entities and its corresponding <code>PersistenceProvider</code> instances
	 */
	public void setProvidersForEntity(Map<String, PersistenceProvider> entityToProviders) {
		this.m_EntityToProviders = entityToProviders;
	}
	
	/**
	 * Interface method implementation. This method is transactional by default and implemented by calling {@link PersistenceDelegate#makePersistent(PersistentEntity, PersistenceProvider)}
	 * For multi-sharded entities, the TX guarantee is limited to per shard and DOES NOT span across multiple shards.
	 * @see PersistenceManager#makePersistent(PersistentEntity)
	 */
	public PersistentEntity makePersistent(PersistentEntity entity) throws PersistenceException {	
		if (null == entity) {
			// If no entity to persist then return whatever is passed and do not execute rest of the code.
			return entity;
		}
		return this.makePersistent(new PersistentEntity[]{entity})[0];
	}

	/**
	 * Interface method implementation. This method is transactional by default and implemented by calling {@link PersistenceDelegate#makePersistent(PersistentEntity[], PersistenceProvider[])}
	 * For multi-sharded entities, the TX guarantee is limited to per shard and DOES NOT span across multiple shards.
	 * @see PersistenceManager#makePersistent(PersistentEntity[])
	 */
	public PersistentEntity[] makePersistent(PersistentEntity[] entities) throws PersistenceException {
		if (null == entities || entities.length == 0) {
			// If no entities to persist then return whatever is passed and do not execute rest of the code.
			return entities;
		}
		entities = filterNullEntities(entities);		
		
		// place holder for the multi-sharded entity, if any, that has been passed into this persistence call
		AbstractMultiShardedPersistentEntity multiShardedEntity = null;
		
		for (PersistentEntity persistentEntity : entities) {
			// For Multi sharded persistent entities, check if all are multi-sharded and have the same shard count and values
			if (AbstractMultiShardedPersistentEntity.class.isAssignableFrom(persistentEntity.getClass())) {
				multiShardedEntity = (AbstractMultiShardedPersistentEntity)persistentEntity;
				// check for validity of multi-sharded persistent entities
				checkValidityOfMultiShardedPersistentities(multiShardedEntity, entities);
			}
		}
		
		if (multiShardedEntity != null) {
			// data is valid. Iterate through shards and invoke the persistence call on the delegate
			// once for each retrieved shard hint
			for (String shardHint : multiShardedEntity.getShardHints()) {
				// set the returned shards one at a time and make persistence calls on the delegate
				for (PersistentEntity entityToPersist : entities) {
					AbstractMultiShardedPersistentEntity shardedEntityToPersist = (AbstractMultiShardedPersistentEntity)entityToPersist;
					shardedEntityToPersist.setShardHint(shardHint);
				}
				checkAndPopulateShardedEntityContextHolder(entities);
				entities = this.persistenceDelegate.makePersistent(entities, findSuitableProviders(entities));
				// unset the context using the first entity
				checkAndUnsetShardedEntityContextHolder(entities[0]);
			}
			// return the persisted multi-sharded entities
			return entities;
		}
		
		// none of the passed in entities are multi-sharded. Proceed to deal with single sharded (or none) entities 
		checkAndPopulateShardedEntityContextHolder(entities);
		entities = this.persistenceDelegate.makePersistent(entities, findSuitableProviders(entities));
		// unset the context using the first entity
		checkAndUnsetShardedEntityContextHolder(entities[0]);
		return entities;
	}

	/**
	 * Interface method implementation. This method is transactional by default and implemented by calling {@link PersistenceDelegate#makeTransient(PersistentEntity, PersistenceProvider)}
	 * For multi-sharded entities, the TX guarantee is limited to per shard and DOES NOT span across multiple shards.
	 * @see PersistenceManager#makeTransient(PersistentEntity)
	 */
	public void makeTransient(PersistentEntity entity) throws PersistenceException {
		if (null == entity) {
			// If no entity to delete then return whatever is passed and do not execute rest of the code.
			return;
		}
		this.makeTransient(new PersistentEntity[]{entity});
	}
	
	/**
	 * Interface method implementation. This method is transactional by default and implemented by calling {@link PersistenceDelegate#makeTransient(PersistentEntity[], PersistenceProvider[])}
	 * For multi-sharded entities, the TX guarantee is limited to per shard and DOES NOT span across multiple shards.
	 * @see PersistenceManager#makeTransient(PersistentEntity[])
	 */
	public void makeTransient(PersistentEntity[] entities) throws PersistenceException {
		if (null == entities || entities.length == 0) {
			//If no entities to delete then return and do not execute rest of the code.
			return;
		}
		entities = filterNullEntities(entities);		
		
		// place holder for the multi-sharded entity, if any, that has been passed into this persistence call
		AbstractMultiShardedPersistentEntity multiShardedEntity = null;
		
		for (PersistentEntity persistentEntity : entities) {
			// For Multi sharded persistent entities, check if all are multi-sharded and have the same shard count and values
			if (AbstractMultiShardedPersistentEntity.class.isAssignableFrom(persistentEntity.getClass())) {
				multiShardedEntity = (AbstractMultiShardedPersistentEntity)persistentEntity;
				// check for validity of multi-sharded persistent entities
				checkValidityOfMultiShardedPersistentities(multiShardedEntity, entities);
			}
		}
		
		if (multiShardedEntity != null) {
			// data is valid. Iterate through shards and invoke the persistence call on the delegate
			// once for each retrieved shard hint
			for (String shardHint : multiShardedEntity.getShardHints()) {
				// set the returned shards one at a time and make persistence calls on the delegate
				for (PersistentEntity entityToPersist : entities) {
					AbstractMultiShardedPersistentEntity shardedEntityToPersist = (AbstractMultiShardedPersistentEntity)entityToPersist;
					shardedEntityToPersist.setShardHint(shardHint);
				}
				checkAndPopulateShardedEntityContextHolder(entities);
				this.persistenceDelegate.makeTransient(entities, findSuitableProviders(entities));
				// unset the context using the first entity
				checkAndUnsetShardedEntityContextHolder(entities[0]);
			}
			// return after persisting multi-sharded entities
			return;
		}
		
		// none of the passed in entities are multi-sharded. Proceed to deal with single sharded (or none) entities 
		checkAndPopulateShardedEntityContextHolder(entities);
		this.persistenceDelegate.makeTransient(entities, findSuitableProviders(entities));
		// unset the context using the first entity
		checkAndUnsetShardedEntityContextHolder(entities[0]);		
	}

	/**
	 * Interface method implementation. WARNING: this call does not support retrieval of the entity from shards i.e. this call is not shard-aware
	 * and therefore returns data from the default configured datasource.
	 * @see PersistenceManager#findEntity(Criteria)
	 */
	public PersistentEntity findEntity(Criteria criteria) throws PersistenceException {
		checkAndUnsetShardedEntityContextHolder(criteria);
		PersistentEntity entity =  findSuitableProvider(criteria.getManagedClass()).findEntity(criteria);
		checkAndUnsetShardedEntityContextHolder(criteria);
		return entity;
	}
	
	/**
	 * Interface method implementation. WARNING: this call does not support retrieval of the entity from shards i.e. this call is not shard-aware
	 * and therefore returns data from the default configured datasource.
	 * @see PersistenceManager#findEntity(PersistentEntity)
	 */
	public PersistentEntity findEntity(PersistentEntity entity) throws PersistenceException {
		checkAndUnsetShardedEntityContextHolder(entity);
		entity =  findSuitableProvider(entity).findEntity(entity);
		checkAndUnsetShardedEntityContextHolder(entity);
		return entity;
	}
	
	/**
	 * Interface method implementation. 
	 * For multi-sharded entities, returns data collected from each shard i.e. by execution of the query against each shard and collating the
	 * results into a single collection. Limits the search results by {@link Criteria#getMaxResults()} if specified.
	 * @see PersistenceManager#findEntities(Criteria)
	 */
	@SuppressWarnings("unchecked")	
	public Collection<PersistentEntity> findEntities(Criteria criteria) throws PersistenceException {

		// Linked list to collate results from queries executed on multiple shards, if any
		LinkedList returnedObjects = new LinkedList();
		
		if (MultiShardAwareCriteria.class.isAssignableFrom(criteria.getClass())) {
			// It is a multi-sharded criteria. Iterate through shards and invoke the persistence call on the delegate
			// once for each retrieved shard hint. Return the results collated from each shard
			MultiShardAwareCriteria multiShardAwareCriteria = (MultiShardAwareCriteria)criteria;
			for (String shardHint : multiShardAwareCriteria.getShardHints()) {
				// set the returned shards one at a time and make persistence calls on the delegate
				multiShardAwareCriteria.setShardHint(shardHint);
				checkAndPopulateShardedEntityContextHolder(new Criteria[]{multiShardAwareCriteria});
				Collection lookedUpEntities = findSuitableProvider(multiShardAwareCriteria.getManagedClass()).findEntities(multiShardAwareCriteria);
				for (Object entity : lookedUpEntities) {
					// check to see if the returned object is a ShardedEntity. Set the shard hint to denote the data store it was loaded from.
					// useful if the returned object is going to be persisted subsequently
					if (ShardedEntity.class.isAssignableFrom(entity.getClass())) {
						((ShardedEntity)entity).setShardHint(shardHint);
					}
				}
				returnedObjects.addAll(lookedUpEntities);
				// unset the context using the criteria
				checkAndUnsetShardedEntityContextHolder(multiShardAwareCriteria);
				// check to see if max results has been set and break loop if results count match or exceed this limit
				if (criteria.getMaxResults() > 0 && returnedObjects.size() >= criteria.getMaxResults()) {
					returnedObjects = (LinkedList)returnedObjects.subList(0, criteria.getMaxResults());
					break;
				}
			}
			// return the outcome of the multi-sharded criteria persistence call
			return returnedObjects;
		}
		
		// none of the passed in criteria are multi-sharded. Proceed to deal with single sharded (or none) entities		
		checkAndPopulateShardedEntityContextHolder(new Criteria[]{criteria});
		Collection entities = findSuitableProvider(criteria.getManagedClass()).findEntities(criteria);
		// unset the context using the first entity
		checkAndUnsetShardedEntityContextHolder(criteria);		
		return entities;		
	}
	
	/**
	 * Interface method implementation. This method is transactional by default and implemented by calling {@link PersistenceDelegate#update(PersistenceProvider[], Criteria...)} 
	 * For multi-sharded entities, the TX guarantee is limited to per shard and DOES NOT span across multiple shards.
	 * @see PersistenceManager#update(Criteria)
	 */
	public int update(Criteria criteria) throws PersistenceException {
		return this.update(new Criteria[] {criteria})[0];
	}
	
	/**
	 * Interface method implementation. This method is transactional by default and implemented by calling {@link PersistenceDelegate#update(PersistenceProvider[], Criteria...)} 
	 * For multi-sharded entities, the TX guarantee is limited to per shard and DOES NOT span across multiple shards.
	 * @see PersistenceManager#update(Criteria...)
	 */
	public int[] update(Criteria... criteria) throws PersistenceException {
		
		int[] returnValue = new int[criteria.length];

		// place holder for the multi-sharded criteria, if any, that has been passed into this persistence call
		MultiShardAwareCriteria multiShardAwareCriteria = null;
		
		for (Criteria criterion : criteria) {
			// For Multi sharded persistent criteria, check if all are multi-sharded and have the same shard count and values
			if (MultiShardAwareCriteria.class.isAssignableFrom(criterion.getClass())) {
				multiShardAwareCriteria = (MultiShardAwareCriteria)criterion;
				// check for validity of multi-sharded persistent entities
				checkValidityOfMultiShardedCriteria(multiShardAwareCriteria, criteria);
			}
		}
		
		if (multiShardAwareCriteria != null) {
			// data is valid. Iterate through shards and invoke the persistence call on the delegate
			// once for each retrieved shard hint
			for (String shardHint : multiShardAwareCriteria.getShardHints()) {
				// set the returned shards one at a time and make persistence calls on the delegate
				for (Criteria persistCriterion : criteria) {
					MultiShardAwareCriteria shardedPersistCriterion = (MultiShardAwareCriteria)persistCriterion;
					shardedPersistCriterion.setShardHint(shardHint);
				}
				checkAndPopulateShardedEntityContextHolder((ShardAwareCriteria[])criteria);
				returnValue = this.persistenceDelegate.update(findSuitableProviders(criteria),criteria);
				// unset the context using the first criteria
				checkAndUnsetShardedEntityContextHolder(criteria[0]);
			}
			// return the outcome of the multi-sharded criteria persistence call. The value is from last shard that was updated
			return returnValue;
		}
		
		// none of the passed in criteria are multi-sharded. Proceed to deal with single sharded (or none) entities		
		checkAndPopulateShardedEntityContextHolder(criteria);
		returnValue = this.persistenceDelegate.update(findSuitableProviders(criteria),criteria);
		// unset the context using the first entity
		checkAndUnsetShardedEntityContextHolder(criteria[0]);		
		checkAndUnsetShardedEntityContextHolder(criteria[0]);		
		return returnValue;
	}
			
	/** Java bean style setter-getter methods*/	
	public PersistenceDelegate getPersistenceDelegate() {
		return persistenceDelegate;
	}
	public void setPersistenceDelegate(PersistenceDelegate persistenceDelegate) {
		this.persistenceDelegate = persistenceDelegate;
	}
	/** End Java bean style setter-getter methods*/

	/**
	 * Helper method to locate the PersistenceProvider for the specified PersistentEntity
	 */
	private PersistenceProvider findSuitableProvider(PersistentEntity entity) throws PersistenceException {
		return findSuitableProvider(entity.getClass());
	}
	
	/**
	 * Helper method to validate multi-sharded persistent entities passed into a persistence call
	 * @param multiShardedEntity the AbstractMultiShardedPersistentEntity to be used for validation 
	 * @param entities the list of PersistentEntity to be validated for multi-shard persistence
	 * @throws PersistenceException in case of errors in count or value of shard-hints of the persistent entities being validated
	 */
	private void checkValidityOfMultiShardedPersistentities(AbstractMultiShardedPersistentEntity multiShardedEntity, PersistentEntity[] entities) 
		throws PersistenceException {
		for (PersistentEntity listPersistentEntity : entities) {
			if (!AbstractMultiShardedPersistentEntity.class.isAssignableFrom(listPersistentEntity.getClass())) {
				// attempt to mix multi sharded persistent entity with other types in same persistence call
				throw new PersistenceException(
						"Attempt to persist Multi-sharded persistent entity with other types. Multi-sharded entity found of type : (" + multiShardedEntity.getEntityName() + ")" +   
						multiShardedEntity.getClass().getName() + 
						" .One of the other types is : (" + listPersistentEntity.getEntityName() + ")" + listPersistentEntity.getClass().getName());
			} 
			// now check if the shard hint count and value match, else throw an exception
			AbstractMultiShardedPersistentEntity listMultiShardedEntity = (AbstractMultiShardedPersistentEntity)listPersistentEntity;
			if (multiShardedEntity.getShardHints().length != listMultiShardedEntity.getShardHints().length) {
				throw new PersistenceException("Multi-shard count mismatch between entities - (" + multiShardedEntity.getEntityName() + ")and("+ listPersistentEntity.getEntityName() + ")");
			}
			// also check if the individual shard hint values match, else throw an exception
			for (int i = 0; i < multiShardedEntity.getShardHints().length; i++) {
				if (!multiShardedEntity.getShardHints()[i].equalsIgnoreCase(listMultiShardedEntity.getShardHints()[i])) {
					throw new PersistenceException("Multi-shard values mismatch between entities - (" + multiShardedEntity.getEntityName() + ")and("+ listPersistentEntity.getEntityName() + ")");							
				}
			}
		}		
	}

	/**
	 * Helper method to validate multi-sharded persistent criteria passed into a persistence call
	 * @param multiShardAwareCriteria the MultiShardAwareCriteria to be used for validation 
	 * @param criteria the list of Criteria to be validated for multi-shard persistence
	 * @throws PersistenceException in case of errors in count or value of shard-hints of the criteria instances being validated
	 */
	private void checkValidityOfMultiShardedCriteria(MultiShardAwareCriteria multiShardAwareCriteria, Criteria[] criteria) 
		throws PersistenceException {
		for (Criteria criterion : criteria) {
			if (!MultiShardAwareCriteria.class.isAssignableFrom(criterion.getClass())) {
				// attempt to mix multi sharded criterion with other types in same persistence call
				throw new PersistenceException(
						"Attempt to persist Multi-sharded criterion with other types. Multi-sharded criterion found of type : (" + multiShardAwareCriteria.getQuery() + ")" +   
						multiShardAwareCriteria.getClass().getName() + 
						" .One of the other types is : (" + criterion.getQuery() + ")" + criterion.getClass().getName());
			} 
			// now check if the shard hint count and value match, else throw an exception
			MultiShardAwareCriteria multiShardedCriterion = (MultiShardAwareCriteria)criterion;
			if (multiShardAwareCriteria.getShardHints().length != multiShardedCriterion.getShardHints().length) {
				throw new PersistenceException("Multi-shard count mismatch between criteria - (" + multiShardAwareCriteria.getQuery() + ")and("+ multiShardedCriterion.getQuery() + ")");
			}
			// also check if the individual shard hint values match, else throw an exception
			for (int i = 0; i < multiShardAwareCriteria.getShardHints().length; i++) {
				if (!multiShardAwareCriteria.getShardHints()[i].equalsIgnoreCase(multiShardedCriterion.getShardHints()[i])) {
					throw new PersistenceException("Multi-shard values mismatch between criteria - (" + multiShardAwareCriteria.getQuery() + ")and("+ multiShardedCriterion.getQuery() + ")");							
				}
			}
		}		
	}
	
	/**
	 * Helper method to create an array of PersistentProvider instances for the specified PersistentEntity instances
	 */
	private PersistenceProvider[] findSuitableProviders(PersistentEntity[] entities) throws PersistenceException {
		PersistenceProvider[] providers = new PersistenceProvider[entities.length];
		for (int i=0; i<entities.length; i++) {
			providers[i] = findSuitableProvider(entities[i]);
		}
		// check to see if all persistence providers are the same, else throw an exception as it could affect TX behavior for data sources
		// that do not support semantics like distributed transactions
		LinkedHashSet<PersistenceProvider> providersSet = new LinkedHashSet<PersistenceProvider>();
		for (PersistenceProvider provider : providers) {
			providersSet.add(provider);
		}
		if (providersSet.size() > 1) {
			StringBuffer buffer = new StringBuffer();
			for (PersistenceProvider provider : providersSet) {
				buffer.append("\n");
				buffer.append(provider.getClass().getName());
			}
			throw new PersistenceException("Persistence call involves multiple Persistence Providers. Aborting operation. Provider instances are of types : "
					+ buffer.toString());
		}
		return providers;
	}

	/**
	 * Helper method to find a suitable <code>PersistenceProvider</code> for a given entity type.
	 * @param clazz - entity type for which a provider has to be located.
	 * @return - An instance of <code>PersistenceProvider</code> which can handle the given entity.
	 * @throws PersistenceException - In case no provider was found.
	 */
	private PersistenceProvider findSuitableProvider(Class<?> clazz) throws PersistenceException {
		PersistenceProvider provider = m_EntityToProviders.get(clazz.getName());
		if (provider == null) {
			throw new PersistenceException("No suitable provider found for: " + clazz.getName());
		}
		return provider;
	}

	/**
	 * Helper method to create an array of PersistentProvider instances for the specified Criteria instances
	 */
	private PersistenceProvider[] findSuitableProviders(Criteria[] criteria) throws PersistenceException {
		PersistenceProvider[] providers = new PersistenceProvider[criteria.length];
		for (int i=0; i<criteria.length; i++) {
			providers[i] = findSuitableProvider(criteria[i].getManagedClass());
		}
		return providers;		
	}	
	
	/**
	 * Helper method to set the first of specified PersistentEntity instances into the ShardedEntityContextHolder if it is of type ShardedPersistentEntity.
	 * Also checks to see if all the ShardedPersistentEntity instances belong to the same shard i.e. {@link ShardedPersistentEntity#getShardHint()} returns
	 * the same value. Throws a PersistenceException otherwise. This defensive check is done to avoid indeterministic transaction boundary behavior
	 * when shards are mapped to multiple database instances.
	 * @param entities the PersistentEntity instances, the first of which is to be set into the ShardedEntityContextHolder if it is of type ShardedPersistentEntity
	 * @throws PersistenceException in case the shard hints of specified ShardedPersistentEntity instances do not match
	 */
	private void checkAndPopulateShardedEntityContextHolder(PersistentEntity[] entities) throws PersistenceException {
		if(entities.length != 0){
			// check to see if sharded and non-sharded entities are part of the same call
			ShardedEntity shardedEntity = null;
			PersistentEntity nonShardedEntity = null;
			for (PersistentEntity entity : entities) {
				if (ShardedEntity.class.isAssignableFrom(entity.getClass())) {
					shardedEntity = (ShardedEntity)entity;
				} else {
					nonShardedEntity = entity;
				}
			}
			if (shardedEntity != null && nonShardedEntity != null) {
        		throw new PersistenceException("Attempt to use sharded and non-sharded persistent entities in the same call. " + 
        				"Mixed types are : " + shardedEntity.getClass().getName() + "," + nonShardedEntity.getClass().getName());				
			}			
			PersistentEntity firstEntity = entities[0];
			String shardHint = (ShardedEntity.class.isAssignableFrom(firstEntity.getClass())) ? ((ShardedEntity)firstEntity).getShardHint() : ShardedEntity.DEFAULT_SHARD;
			for (PersistentEntity entity : entities) {
		        if (ShardedEntity.class.isAssignableFrom(entity.getClass())) {
		        	if (!((ShardedEntity)entity).getShardHint().equals(shardHint)) {
		        		throw new PersistenceException("Shard hints do not match for the specified ShardedEntity instances." + 
		        				" Mismatched values are : " + shardHint + "," + ((ShardedEntity)entity).getShardHint());
		        	}
		        }
		        // Set the PersistentEntity into the ShardedEntityContextHolder if the type is ShardedPersistentEntity. Will be used in Datasource resolution
		        if (ShardedEntity.class.isAssignableFrom(firstEntity.getClass())) {
		        	ShardedEntityContextHolder.setShardedEntity((ShardedEntity)firstEntity);
		        }
			}
		}
	}
	
	/**
	 * Helper method to unset the specified PersistentEntity from the ShardedEntityContextHolder if it is of type ShardedPersistentEntity
	 * @param entity the PersistentEntity to unset from the ShardedEntityContextHolder if it is of type ShardedPersistentEntity
	 * @throws PersistenceException in case the shard hint of specified ShardedPersistentEntity does not match with the value of the entity held by ShardedEntityContextHolder
	 */
	private void checkAndUnsetShardedEntityContextHolder(PersistentEntity entity) throws PersistenceException {
		if(entity instanceof ShardedPersistentEntity){
			unsetShardedEntityContextHolder((ShardedPersistentEntity)entity);
		}
	}
	
	
	/**
	 * Helper method to set the first of specified Criteria instances into the ShardedEntityContextHolder if it is of type ShardedEntity. Throws an
	 * exception if shardedentities are mixed with non-sharded ones in the same call.
	 * Also checks to see if all the ShardedEntity instances belong to the same shard i.e. {@link ShardedEntity#getShardHint()} returns
	 * the same value. Throws a PersistenceException otherwise. This defensive check is done to avoid indeterministic transaction boundary behavior
	 * when shards are mapped to multiple database instances.
	 * when shards are mapped to multiple database instances.
	 * @param criteria the ShardedEntity instances, the first of which is to be set into the ShardedEntityContextHolder if it is of type ShardedEntity
	 * @throws PersistenceException in case the shard hints of specified ShardedEntity instances do not match
	 */
	private void checkAndPopulateShardedEntityContextHolder(Criteria[] criteria) throws PersistenceException {		
		if(criteria.length != 0){			
			// check to see if sharded and non-sharded entities are part of the same call
			ShardedEntity shardedEntity = null;
			Criteria nonShardedEntity = null;
			for (Criteria entity : criteria) {
				if (ShardedEntity.class.isAssignableFrom(entity.getClass())) {
					shardedEntity = (ShardedEntity)entity;
				} else {
					nonShardedEntity = entity;
				}
			}
			if (shardedEntity != null && nonShardedEntity != null) {
        		throw new PersistenceException("Attempt to use sharded and non-sharded criteria in the same call. " + 
        				"Mixed types are : " + shardedEntity.getClass().getName() + "," + nonShardedEntity.getClass().getName());				
			}			
			Criteria firstEntity = criteria[0];
			String shardHint = (ShardedEntity.class.isAssignableFrom(firstEntity.getClass())) ? ((ShardedEntity)firstEntity).getShardHint() : ShardedEntity.DEFAULT_SHARD;
			for (Criteria entity : criteria) {
		        if (ShardedEntity.class.isAssignableFrom(entity.getClass())) {
		        	if (!((ShardedEntity)entity).getShardHint().equals(shardHint)) {
		        		throw new PersistenceException("Shard hints do not match for the specified Criteria i.e. ShardedEntity instances." + 
		        				" Mismatched values are : " + shardHint + "," + ((ShardedEntity)entity).getShardHint());
		        	}
		        }
		        // Set the PersistentEntity into the ShardedEntityContextHolder if the type is ShardedPersistentEntity. Will be used in Datasource resolution
		        if (ShardedEntity.class.isAssignableFrom(firstEntity.getClass())) {
		        	ShardedEntityContextHolder.setShardedEntity((ShardedEntity)firstEntity);
		        }
			}
		}
	}
	
	/**
	 * Helper method to unset the specified Criteria from the ShardedEntityContextHolder if it is of type ShardedEntity
	 * @param entity the Crieria to unset from the ShardedEntityContextHolder if it is of type ShardedEntity
	 * @throws PersistenceException in case the shard hint of specified ShardedEntity does not match with the value of the entity held by ShardedEntityContextHolder
	 */
	private void checkAndUnsetShardedEntityContextHolder(Criteria criteria) throws PersistenceException {
		if(criteria instanceof ShardedEntity){
			unsetShardedEntityContextHolder((ShardedEntity)criteria);
		}
	}
	
	/**
	 * Helper method to unset the specified ShardedEntity from the ShardedEntityContextHolder 
	 * @param entity the ShardedEntity to unset from the ShardedEntityContextHolder
	 * @throws PersistenceException in case the shard hint of specified ShardedEntity does not match with the value of the entity held by ShardedEntityContextHolder
	 */
	private void unsetShardedEntityContextHolder(ShardedEntity entity) throws PersistenceException {
        // unset the ShardedEntity from the ShardedEntityContextHolder if the type is ShardedPersistentEntity. Cleans up the context that was 
        // populated earlier using the ShardedEntityContextHolder#setShardedPersistentEntity() call
        ShardedEntity contextEntity = ShardedEntityContextHolder.getShardedEntity();
        if (contextEntity != null && !contextEntity.getShardHint().equals(((ShardedEntity)entity).getShardHint())) {
        	throw new PersistenceException("Shard hints do not match for the specified ShardedEntity instance and the one held in the context" + 
        			" Mismatched values are : " + contextEntity.getShardHint() + "," + ((ShardedEntity)entity).getShardHint());        		
        }
        ShardedEntityContextHolder.clearShardedEntity();                		
	}
		
	/**
	 * Helper method to filter out null entitities
	 * @param entities entities to analyze for null
	 * @return array of PersistentEntity instances post filtering for null
	 */
	private PersistentEntity[] filterNullEntities(PersistentEntity[] entities) {
		if (entities == null) {
			return new PersistentEntity[0]; //return empty array
		}
		List<PersistentEntity> filteredEntities = new ArrayList<PersistentEntity>();
		for (PersistentEntity entity : entities) {
			if (entity != null) {
				filteredEntities.add(entity);
			}
		}		
		entities = filteredEntities.toArray(new PersistentEntity[0]);
		return entities;
	}
	
}