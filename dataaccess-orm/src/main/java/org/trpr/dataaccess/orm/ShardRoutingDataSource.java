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

package org.trpr.dataaccess.orm;

import javax.sql.DataSource;

import org.trpr.platform.core.impl.persistence.sharding.ShardedEntityContextHolder;
import org.trpr.platform.core.spi.persistence.sharding.ShardedEntity;
import org.springframework.core.InfrastructureProxy;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

/**
 * The <code>ShardRoutingDataSource</code> is a {@link AbstractRoutingDataSource} that routes
 * the {@link #getConnection()} method to one of the configured target DataSources based on a lookup
 * key determined by the {@link ShardedEntity#getShardHint()} call.
 * The ShardedPersistentEntity is retrieved from {@link ShardedEntityContextHolder#getShardedEntity()} 
 * This data source is a Spring {@link InfrastructureProxy} that returns the actual data source from which a connection was/could have been 
 * returned. This behavior is very useful when this data source is used in persistence calls that require transaction support.
 * Note that all target data sources configured on this shard routing data source must be proxied using the {@link TransactionAwareDataSourceProxy}
 * bean to participate in Spring defined transactions using transaction managers like the {@link HibernateTransactionManager} 
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */

public class ShardRoutingDataSource extends AbstractRoutingDataSource implements InfrastructureProxy {

	/**
	 * Overriden superclass method. Determines the lookup key by calling {@link ShardedEntity#getShardHint()} 
	 * on the object returned from {@link ShardedEntityContextHolder#getShardedEntity()}
	 * @see org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource#determineCurrentLookupKey()
	 */
	protected Object determineCurrentLookupKey() {
		// Returns an empty string as the shard hint if the context holder doesnot have any sharded entity. Usually the case during server startup
		// when used in conjunction with frameworks like Hibernate. The default data source will be used in this case.
		return ShardedEntityContextHolder.getShardedEntity()== null ? "" : ShardedEntityContextHolder.getShardedEntity().getShardHint();
	}
	
	/**
	 * Interface method implementation. This InfrastructureProxy returns the underlying target data source - unwrapping it if it is a
	 * TransactionAwareDataSourceProxy instead. This implementation is required for transaction support in sharded data source access.
	 * @see org.springframework.core.InfrastructureProxy#getWrappedObject()
	 */
	public Object getWrappedObject() {
		DataSource ds = this.determineTargetDataSource();
		return (ds instanceof TransactionAwareDataSourceProxy) ? ((TransactionAwareDataSourceProxy)ds).getTargetDataSource() : ds;
	}
	
}
