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

import org.trpr.platform.core.impl.persistence.AbstractPersistentEntity;
import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.Identifier;
import org.trpr.platform.core.spi.persistence.sharding.ShardedEntity;
import org.trpr.platform.core.spi.persistence.sharding.ShardedPersistentEntity;

/**
 * The <code>AbstractShardedPersistentEntity</code> is a sub-type of {@link AbstractPersistentEntity} and a marker implementation of the {@link ShardedPersistentEntity} 
 * interface provided as a convenience class for inheritance. Sub-types are required to implement the {@link ShardedEntity#getShardHint()} method.
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public abstract class AbstractShardedPersistentEntity extends AbstractPersistentEntity implements ShardedPersistentEntity {

	/**
	 *  Place holder for holding the shard hint. May be optionally used by sub-types or persistence handlers - the latter for returning the shard
	 *  hint post persistence, if any.
	 * */
	private String persistedShardHint = ShardedEntity.DEFAULT_SHARD;
		
	/** Constructors for this class*/
	public AbstractShardedPersistentEntity(String entityname, Identifier identifier) {
		this(entityname, identifier, null);
	}
	public AbstractShardedPersistentEntity(String entityname, Criteria criteriaForLoad) {
		this(entityname, null, criteriaForLoad);
	}
	public AbstractShardedPersistentEntity(Identifier identifier) {
		this(null, identifier, null);
	}
	public AbstractShardedPersistentEntity(Criteria criteriaForLoad) {
		this(null, null, criteriaForLoad);
	}
	public AbstractShardedPersistentEntity(String entityName, Identifier identifier, Criteria criteriaForLoad) {
		super(entityName, identifier, criteriaForLoad);
	}
	
	/**
	 * Interface method implementation. Returns the value stored in member variable. Sub-types may override this method to return different
	 * values.
	 * @see ShardedEntity#getShardHint()
	 */
	public String getShardHint() {
		return this.persistedShardHint;
	}

	/**
	 * Sets the shard hint for this ShardedEntity. May be used by persistence handlers to return the shard in which this PersistentEntity was saved
	 * @param shardHint the shard hint for this ShardedEntity
	 */
	public void setShardHint(String shardHint) {
		this.persistedShardHint = shardHint;
	}	
	
}
