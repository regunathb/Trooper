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

/**
 * The <code>ShardedEntity</code> interface defines behavior common to all persistence framework entities that are shard-aware i.e. 
 * whose data might be persisted in a partitioned manner. 
 * 
 * @author Bharath Varna
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public interface ShardedEntity {
	
	/** Constant to indicate default shard*/
	public static final String DEFAULT_SHARD = "";
	
	/** Constant to indicate undefined shard*/
	public static final String UNDEFINED_SHARD = "UNDEFINED";
	
	/**
	 * Returns the shard identification hint. The hint may or may not translate directly to the physical shard identifier.
	 * The hint to shard mapping is maintained by configurations used by the PersistenceProvider, often using virtual shards i.e. a 
	 * mapping between a shard hint and the corresponding physical shard Id.
	 * @return the shard identification hint derived from this PersistentEntity's Identifier.
	 */
	public String getShardHint();
	
	/**
	 * Sets the shard identification hint. 
	 * @param shardHint the shard hint used to eventually identify the data store where this ShardedEntity is persisted or retrieved from.
	 */
	public void setShardHint(String shardHint);

}
