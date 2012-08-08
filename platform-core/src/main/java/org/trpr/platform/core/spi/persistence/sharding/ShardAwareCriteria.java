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

package org.trpr.platform.core.spi.persistence.sharding;

import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * This class <code>ShardAwareCriteria</code> is a {@link Criteria} sub-type that may be used in persistence operations of application partitioned data.
 * 
 * This ShardAwareCriteria is initialized with the default shard i.e. {@link ShardedEntity#DEFAULT_SHARD}}.
 * 
 * @author Bharath Varna
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public class ShardAwareCriteria extends Criteria implements ShardedEntity {
	
	/**
	 * The Shard Hint
	 */
	private String shardHint = ShardedEntity.DEFAULT_SHARD;
	
	/**
	 * Constructor for this class
	 * @see {@link Criteria}
	 */
	public ShardAwareCriteria(Class<? extends PersistentEntity> managedClass, String query, int type) {
		super(managedClass, query, type);
	}
	
	/**
	 * Constructor for this class
	 * @see {@link Criteria}
	 */
	public ShardAwareCriteria(Class<? extends PersistentEntity> managedClass, String query) {
		super(managedClass, query);
	}
	
	/** Setter and Getter for shard hint. */	
	public void setShardHint(String shardHint) {
		this.shardHint = shardHint;
	}
	public String getShardHint() {
		return this.shardHint;
	}
		
}
