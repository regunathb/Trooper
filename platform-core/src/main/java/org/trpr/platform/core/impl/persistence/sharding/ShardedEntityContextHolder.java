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

package org.trpr.platform.core.impl.persistence.sharding;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.persistence.PersistenceManager;
import org.trpr.platform.core.spi.persistence.sharding.ShardedEntity;

/**
 * The <code>ShardedEntityContextHolder</code> holds the {@link ShardedEntity} during persistence calls made to the {@link PersistenceManager}.
 * The information contained by this holder is used to determine the shard used for persistence. This class uses a {@link ThreadLocal} to hold
 * the ShardedEntity such that information is local to the call being executed on the current thread.  
 * 
 * All methods in this class are static to permit accessing the ThreadLocal instance. Methods may be accessed from persistence framework classes
 * such as Spring and its Hibernate wrappers.  
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */

public class ShardedEntityContextHolder {

	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(ShardedEntityContextHolder.class);
	
	/** The ThreadLocal instance used to store the ShardedEntity that is to be persisted */
	private static final ThreadLocal<ShardedEntity> contextHolder = new ThreadLocal<ShardedEntity>();
	
	/**
	 * Sets the ShardedEntity to be used for shard identification
	 * @param shardedEntity the ShardedEntity to be used in shard identification during the persistence operation
	 */
	public static void setShardedEntity(ShardedEntity shardedEntity) {
		LOGGER.debug("Setting Sharded Persistent Entity into context. Type is : " + shardedEntity.getClass().getName() + " . Shard hint is : " + shardedEntity.getShardHint());
		contextHolder.set(shardedEntity);
	}
	
	/**
	 * Gets the ShardedEntity to use for shard identification in the persistence call
	 * @return ShardedEntity for shard identification
	 */
	public static ShardedEntity getShardedEntity() {
		LOGGER.debug(contextHolder.get() != null ? "Getting Sharded Persistent Entity from context. Type is : " + 
				contextHolder.get().getClass().getName() + " . Shard hint is : " + ((ShardedEntity)contextHolder.get()).getShardHint() : 
					"Getting Sharded Persistent Entity from context. No entity is set");
		return (ShardedEntity)contextHolder.get();
	}
	
	/**
	 * Clears this context holder
	 */
	public static void clearShardedEntity() {
		contextHolder.remove();
	}
	
}
