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
 * The <code>MultiShardedEntity</code> interface defines behavior common to all entities implementations that are shard-aware i.e. 
 * whose data might be persisted in a partitioned manner and have the potential to exist in more than one schema instance. It differs from the
 * {@link ShardedEntity} by supporting multiple shard hints to indicate that the entity might exist in more than one shard, especially in the following
 * scenarios:
 * <pre>
 * 	In case of updates, there is only one instance of the entity but location of the same cannot be determined i.e. the data could potentially
 * 	reside in a set of configured schemas.
 * 	In case of range queries, the entity instances might be spread across one or more configured schemas  
 * <pre>
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */

public interface MultiShardedEntity {

	/**
	 * Returns the shard identification hints where the entity might exist. The hint may or may not translate directly to the physical shard identifiers.
	 * The hint to shard mapping is maintained by configurations used by the PersistenceProvider, often using virtual shards i.e. a 
	 * mapping between a shard hint and the corresponding physical shard Id.
	 * @return the shard identification hints derived from this PersistentEntity's Identifier(s).
	 */
	public String[] getShardHints();

}
