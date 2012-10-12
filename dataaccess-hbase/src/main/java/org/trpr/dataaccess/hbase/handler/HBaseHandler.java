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
package org.trpr.dataaccess.hbase.handler;

import java.util.Collection;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.trpr.platform.core.impl.persistence.AbstractPersistenceHandler;
import org.trpr.platform.core.spi.logging.PerformanceMetricsLogger;
import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.PersistenceException;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * <code>HBaseHandler<code> is a sub-type for {@link AbstractPersistenceHandler} used for persisting entities to HBase.
 * This class uses Spring Data Hadoop project (http://www.springsource.org/spring-data/hadoop) libraries. 
 * 
 * This class is also instrumented to log performance metrics using the platform-core {@link PerformanceMetricsLogger}
 * 
 * @author Regunath B
 * @version 1.0, 11/10/2012
 */
@ManagedResource(objectName = "spring.application:type=Trooper,application=Performance-Metrics,name=HBaseMetrics-", description = "HBase Performance Metrics Logger")
public class HBaseHandler extends AbstractPersistenceHandler {

	/**
	 * No arg constructor.
	 */
	public HBaseHandler(){
	}

	@Override
	public PersistentEntity makePersistent(PersistentEntity entity)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void makeTransient(PersistentEntity entity)
			throws PersistenceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public PersistentEntity findEntity(Criteria criteria)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PersistentEntity findEntity(PersistentEntity entity)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<PersistentEntity> findEntities(Criteria criteria)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Criteria criteria) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}


}