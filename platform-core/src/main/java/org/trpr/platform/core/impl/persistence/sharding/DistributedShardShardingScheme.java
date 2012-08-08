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

import org.trpr.platform.core.spi.persistence.sharding.ShardedEntity;
import org.trpr.platform.core.spi.persistence.sharding.ShardingScheme;

/**
 * The <code>DistributedShardShardingScheme</code> is an implementation of the ShardingScheme that returns a shard from the list of shard hints
 * held by an instance of this class. This implementation does a round-robin selection of the shards held by the instance of this class.
 * Does not have any intelligence like load distribution based on number of entities contained by each shard or other factors.
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public class DistributedShardShardingScheme implements ShardingScheme {

	/** Place holder for shard hints, initialized with the default shard value*/
	private String[] shardHints = new String[] {ShardedEntity.DEFAULT_SHARD};
	
	/** Index to choose the shard hint*/
	private int shardHintIndex = -1;
	
	/**
	 * Interface method implementation. Returns a shard hint determined by round-robin selection from shard hints held by an instance of this class.
	 * @see ShardingScheme#getShardHint()
	 */
	public String getShardHint() {
		shardHintIndex += 1;
		shardHintIndex = (shardHintIndex % getShardHints().length);
		return getShardHints()[shardHintIndex];
	}

	/** Getter/Setter methods*/
	public String[] getShardHints() {
		return this.shardHints;
	}
	public void setShardHints(String[] shardHints) {
		this.shardHints = shardHints;
	}
	
}
