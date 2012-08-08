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

package org.trpr.platform.core.impl.persistence;

import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.Identifier;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * The <code>AbstractPersistentEntity</code> is a simple implementation of the {@link PersistentEntity} that may be used to build a PersistentEntity 
 * type hierarchy implementation 
 *  
 * @author Ashok Ayengar
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */
public abstract class AbstractPersistentEntity implements PersistentEntity {

	/** Instance variables */
	protected Identifier identifier;
	protected Criteria criteriaForLoad;
	protected String entityName;
	
	/** Constructors for this class*/
	public AbstractPersistentEntity(String entityname, Identifier identifier) {
		this(entityname, identifier, null);
	}
	public AbstractPersistentEntity(String entityname, Criteria criteriaForLoad) {
		this(entityname, null, criteriaForLoad);
	}
	public AbstractPersistentEntity(Identifier identifier) {
		this(null, identifier, null);
	}
	public AbstractPersistentEntity(Criteria criteriaForLoad) {
		this(null, null, criteriaForLoad);
	}
	public AbstractPersistentEntity(String entityName, Identifier identifier, Criteria criteriaForLoad) {
		this.entityName = entityName;
		this.identifier = identifier;
		this.criteriaForLoad = criteriaForLoad;
	}
	
	/** Getter/Setter methods*/
	public Identifier getIdentifier() {
		return this.identifier;
	}
	public Criteria getCriteriaForLoad() {
		return this.criteriaForLoad;
	}
	public String getEntityName() {
		return this.entityName;
	}
	
}
