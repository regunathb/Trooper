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

import org.trpr.platform.core.spi.persistence.PersistenceProvider;

/**
 * The <code>ShardingScheme</code> interface defines behavior common to all persistence providers and handlers that implement sharding behavior
 * for {@link ShardedEntity}. Useful when the shard information is not available beforehand and the ShardedEntity returns {@value ShardedEntity#UNDEFINED_SHARD}
 * in {@link ShardedEntity#getShardHint()}. In such cases the shard determination is external to the ShardedEntity being persisted and may therefore be
 * defined/populated by the {@link PersistenceProvider} - for example when appending logging records to the currently available/configured data store 
 * node/location. 
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public interface ShardingScheme {
	
	/**
	 * Returns the shard hint to use in a persistence call
	 * @return String indicating the shard
	 */
	public String getShardHint();
	
}
