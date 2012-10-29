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
package org.trpr.dataaccess.hbase.persistence.entity;

import org.trpr.platform.core.impl.persistence.sharding.AbstractShardedPersistentEntity;

/**
 * <p>
 * This class is used by all the hbase handlers in the system to manage
 * persistence of business objects which need to be persisted in
 * <code>HBase</code>.
 * <p>
 * This entity does not provide other methods of setting and getting criteria,
 * identifier, etc for CRUD of any object. The complete configuration and
 * storage mappings are defined in the <code>*.hbase.xml</code> files. For more
 * details on the structure of these files refer hbase-mapping.xsd
 * 
 * @author Regunath B
 * @version 2.0
 * 
 *          ====@version 2.0 changelog ==== Extends
 *          AbstractShardedPersistentEntity to permit sharding
 * 
 */
public class HBaseEntity extends AbstractShardedPersistentEntity {

}
