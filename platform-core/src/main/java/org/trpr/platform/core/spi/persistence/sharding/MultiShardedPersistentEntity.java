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

import org.trpr.platform.core.spi.persistence.PersistenceManager;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * The <code>MultiShardedPersistentEntity</code> interface is a sub-type of the {@link PersistentEntity} and {@link MultiShardedEntity}that defines behavior common to all
 * PersistentEntity implementations that are shard-aware i.e. whose data might be persisted in a partitioned manner.
 * The sharding implementation is specific to the {@link PersistenceManager} instance that handles all persistence operations on 
 * PersistentEntity instances.
 * This PersistentEntity might exist in more than one shard and in this behavior differs from {@link ShardedPersistentEntity} 
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public interface MultiShardedPersistentEntity extends PersistentEntity, MultiShardedEntity {
}
