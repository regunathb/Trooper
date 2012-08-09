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

import org.trpr.platform.core.impl.persistence.AbstractPersistenceProvider;
import org.trpr.platform.core.spi.persistence.PersistenceProvider;

/**
 * The <code>RDBMSProvider</code> is an implementation of {@link PersistenceProvider}. This implementation uses the default behavior 
 * of the {@link AbstractPersistenceProvider}
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public class RDBMSProvider extends AbstractPersistenceProvider {
	
	/**
	 * No-arg constructor.
	 */
	public RDBMSProvider(){	
	}	

}
