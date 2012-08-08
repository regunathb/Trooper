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
import org.trpr.platform.core.spi.persistence.sharding.MultiShardedEntity;
import org.trpr.platform.core.spi.persistence.sharding.MultiShardedPersistentEntity;
import org.trpr.platform.core.spi.persistence.sharding.ShardedEntity;
import org.trpr.platform.core.spi.persistence.sharding.ShardedPersistentEntity;

/**
 * The <code>AbstractMultiShardedPersistentEntity</code> is a sub-type of {@link AbstractPersistentEntity} and a marker implementation of the {@link MultiShardedPersistentEntity} 
 * interface provided as a convenience class for inheritance. Sub-types are required to override the {@link MultiShardedEntity#getShardHints()} method.
 * This MultiShardedPersistentEntity returns an array of shards of size one containing the default shard i.e. {@link ShardedEntity#DEFAULT_SHARD}}.
 * This class implements the {@link ShardedEntity#getShardHint()} as a simple Java Bean like getter method i.e. returns the value set using the corresponding
 * setter method.
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public abstract class AbstractMultiShardedPersistentEntity extends AbstractPersistentEntity implements MultiShardedPersistentEntity, ShardedPersistentEntity {
	
	/** The current shard hint set on this ShardedEntity initialized to empty string i.e. "" to indicate default shard*/
	private String currentShardHint = ShardedEntity.DEFAULT_SHARD;
	
	/** Constructors for this class*/
	public AbstractMultiShardedPersistentEntity(String entityname, Identifier identifier) {
		this(entityname, identifier, null);
	}
	public AbstractMultiShardedPersistentEntity(String entityname, Criteria criteriaForLoad) {
		this(entityname, null, criteriaForLoad);
	}
	public AbstractMultiShardedPersistentEntity(Identifier identifier) {
		this(null, identifier, null);
	}
	public AbstractMultiShardedPersistentEntity(Criteria criteriaForLoad) {
		this(null, null, criteriaForLoad);
	}
	public AbstractMultiShardedPersistentEntity(String entityName, Identifier identifier, Criteria criteriaForLoad) {
		super(entityName, identifier, criteriaForLoad);
	}
	
	/**
	 * Default implementation to return just the default shard i.e. {@link ShardedEntity#DEFAULT_SHARD}}.
	 * Sub-types must override to provide more meaningful implementations. This is just a convenience implementation.
	 * @see MultiShardedEntity#getShardHints()
	 */
	public String[] getShardHints() {
		return new String[]{ShardedEntity.DEFAULT_SHARD};
	}
	
	/**
	 * Returns the currently set shard hint or null if nothing is set.
	 * @see #setShardHint(String)
	 * @see ShardedEntity#getShardHint()
	 */
	public String getShardHint() {
		return this.currentShardHint;
	}
	
	/**
	 * Sets the current shard hint to the specified value
	 * @param currentShardHint the current shard hint
	 */
	public void setShardHint(String currentShardHint) {
		this.currentShardHint = currentShardHint;
	}
}