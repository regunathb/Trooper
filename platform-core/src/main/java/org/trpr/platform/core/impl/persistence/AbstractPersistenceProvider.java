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

import java.util.Collection;

import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistenceHandler;
import org.trpr.platform.core.spi.persistence.PersistenceProvider;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * The <code>AbstractPersistenceProvider</code> is a simple implementation of the {@link PersistenceProvider} that delegates all persistence calls  
 * to the {@link PersistenceHandler} injected into this provider. 
 * 
 * @author Ashok Ayengar
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public abstract class AbstractPersistenceProvider implements PersistenceProvider {

	/** The PersistenceHandler instance to delegate all persistence calls to*/
	private PersistenceHandler handler;
	
	/**
	 * Interface method implementation. Delegates the call to PersistenceHandler.
	 * @see PersistenceProvider#findEntities(Criteria)
	 */
	@Override
	public Collection<PersistentEntity> findEntities(Criteria criteria) throws PersistenceException {
		return getHandler().findEntities(criteria);
	}

	/**
	 * Interface method implementation. Delegates the call to PersistenceHandler.
	 * @see PersistenceProvider#findEntity(Criteria)
	 */
	@Override
	public PersistentEntity findEntity(Criteria criteria) throws PersistenceException {
		return getHandler().findEntity(criteria);
	}

	/**
	 * Interface method implementation. Delegates the call to PersistenceHandler.
	 * @see PersistenceProvider#findEntity(PersistentEntity)
	 */
	@Override
	public PersistentEntity findEntity(PersistentEntity entity) throws PersistenceException {
		return getHandler().findEntity(entity);
	}
	
	/**
	 * Interface method implementation. Delegates the call to PersistenceHandler.
	 * @see PersistenceProvider#makePersistent(PersistentEntity)
	 */
	@Override
	public PersistentEntity makePersistent(PersistentEntity entity) throws PersistenceException {
		return getHandler().makePersistent(entity);
	}

	/**
	 * Interface method implementation. Delegates the call to PersistenceHandler.
	 * @see PersistenceProvider#makeTransient(PersistentEntity)
	 */
	@Override
	public void makeTransient(PersistentEntity entity) throws PersistenceException {
		getHandler().makePersistent(entity);
	}

	/**
	 * Interface method implementation. Delegates the call to PersistenceHandler.
	 * @see PersistenceProvider#update(Criteria)
	 */
	@Override
	public int update(Criteria criteria) throws PersistenceException {
		return getHandler().update(criteria);
	}
	
	
	/**
	 * Interface method implementation. Delegates the call to PersistenceHandler.
	 * @see PersistenceProvider#findObject(PersistentEntity)
	 */
	@Override
	public Collection<PersistentEntity> findObject(PersistentEntity entity) throws PersistenceException {
		return getHandler().findObject(entity);
	}
	

	/** Getter/Setter methods*/
	public PersistenceHandler getHandler() {
		return this.handler;
	}
	public void setHandler(PersistenceHandler handler) {
		this.handler = handler;
	}
	
}
