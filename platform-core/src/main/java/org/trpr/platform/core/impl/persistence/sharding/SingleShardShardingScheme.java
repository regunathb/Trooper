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
 * The <code>SingleShardShardingScheme</code> is an implementation of the ShardingScheme that returns the single shard value held by an instance
 * of this class or {@link ShardedEntity#DEFAULT_SHARD} if none is specified.
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */

public class SingleShardShardingScheme implements ShardingScheme {
	
	/** The single shard hint identifier */
	private String singleShard = ShardedEntity.DEFAULT_SHARD;

	/**
	 * Interface implementation. Returns the default shard {@link ShardedEntity#DEFAULT_SHARD} if none is set.
	 * @see ShardingScheme#getShardHint()
	 */
	public String getShardHint() {
		return this.singleShard;
	}

	/**
	 * Sets the shard hint for this ShardingScheme
	 * @param singleShard the single shard identifier
	 */
	public void setShardHint(String singleShard) {
		this.singleShard = singleShard;
	}	

}
