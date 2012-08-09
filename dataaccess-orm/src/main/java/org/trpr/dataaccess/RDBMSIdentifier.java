
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

import org.trpr.platform.core.spi.persistence.Identifier;

/**
 * The <code>RDBMSIdentifier</code> is a  {@link Identifier} implementation for RDBMS that uses a {@link Long} as the entity identifier.
 * 
 * @author Regunath B
 * @version 1.0, 23/05/2012
 */

public class RDBMSIdentifier implements Identifier {

	/** The identifier value*/
	private Long objectId;

	/** 
	 * Constructor for this class
	 * @param objectId the Long object whose value uniquely defines an instance of this Identifier
	 */
	public RDBMSIdentifier(Long objectId) {
		this.objectId = objectId;
	}
	
	/**
	 * Overriden method. Returns the String representation of the {@link #getObjectId()}
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getObjectId().toString();
	}
	
	/** Getter/Setter methods*/
	public Long getObjectId() {
		return this.objectId;
	}
	public void setObjectId(Long objectId) {
		this.objectId = objectId;
	}

	/**
	 * Interface method implementation. Returns true if the Long value of this Identifier matches with that of the specified Identifier 
	 */
	public boolean equals(Identifier anotherIdentifier) {
		if (!anotherIdentifier.getClass().isAssignableFrom(RDBMSIdentifier.class)) {
			return false;
		}
		return (((RDBMSIdentifier)anotherIdentifier).getObjectId().longValue() == this.getObjectId().longValue());
	}	
	
}
