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
 * This class <code>MultiShardAwareCriteria</code> is a sub-type of {@link ShardAwareCriteria} and also implements the {@link MultiShardedEntity} interface.
 * It is used to define the criteria for a PersistentEntity that might exist across multiple shards. This Criteria class is typically used
 * in reading or updating bulk instances of PersistentEntity.
 * 
 * This MultiShardAwareCriteria is initialized with an array of shards of size one containing the default shard i.e. {@link ShardedEntity#DEFAULT_SHARD}}.
 * Sub-types may override the {@link #getShardHints()} to provide more meaningful and invocation specific values, as required.
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public class MultiShardAwareCriteria extends ShardAwareCriteria implements MultiShardedEntity {
	
	/**
	 * The Shard hints
	 */
	private String[] shardHints = new String[]{ShardedEntity.DEFAULT_SHARD};
	
	/**
	 * Constructor for this class
	 * @see {@link Criteria}
	 */
	public MultiShardAwareCriteria(Class<? extends PersistentEntity> managedClass, String query, int type) {
		super(managedClass,query,type);
	}
	
	/**
	 * Constructor for this class
	 * @see {@link Criteria}
	 */
	public MultiShardAwareCriteria(Class<? extends PersistentEntity> managedClass, String query) {
		super(managedClass,query);
	}

	/** == Setter and Getter for shard hints. */	
	public void setShardHints(String[] shardHints) {
		this.shardHints = shardHints;
	}
	public String[] getShardHints() {
		return this.shardHints;
	}
	
	
}
