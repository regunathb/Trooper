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
package org.trpr.dataaccess.hbase.persistence;

import java.util.Collection;

import org.trpr.platform.core.impl.persistence.AbstractPersistenceProvider;
import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * This class redefines those methods of {@link} AbstractPersistenceProvider
 * that are not supported for HBase persistence by throwing {@link}
 * UnsupportedOperationException.
 * 
 * @shashikant soni
 */
public class HBaseProvider extends AbstractPersistenceProvider {

	@Override
	public int update(Criteria criteria) throws PersistenceException {
		throw new UnsupportedOperationException("Operation not supported!!!!!");
	}

	@Override
	public PersistentEntity findEntity(Criteria criteria) throws PersistenceException {
		throw new UnsupportedOperationException("Method not supported");
	}

	@Override
	public PersistentEntity findEntity(PersistentEntity entity) throws PersistenceException {
		throw new UnsupportedOperationException("Method not supported");
	}

	@Override
	public Collection<PersistentEntity> findEntities(Criteria criteria) throws PersistenceException {
		throw new UnsupportedOperationException("Method not supported");
	}

}
