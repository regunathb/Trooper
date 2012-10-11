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
package org.trpr.dataaccess;

import org.trpr.platform.core.impl.persistence.AbstractPersistenceHandler;

/**
 * <code>RDBMSHandler<code> is a sub-tyep of {@link AbstractPersistenceHandler} caters to persisting data into an RDBMS.
 * This class is abstract and may be extended to create specific framework handlers - for example to persist using Hibernate or JPA or iBatis. 

 * @author Regunath B
 * @version 1.0, 23/05/2012
 */

public abstract class RDBMSHandler extends AbstractPersistenceHandler {
	
	/**
	 * No arg constructor.
	 */
	public RDBMSHandler(){
	}
}
