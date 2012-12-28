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
package org.trpr.example.seda.greeting.persistence;

import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.core.spi.persistence.Criteria;
import org.trpr.platform.core.spi.persistence.Identifier;
import org.trpr.platform.core.spi.persistence.PersistentEntity;

/**
 * The <code>PersistentEarthling</code> is an implementation of {@link PersistentEntity} used to demonstrate stage data check-pointing in the 
 * sample service.
 * 
 * @author Regunath B
 * @version 1.0, 28/12/2012
 */
public class PersistentEarthling implements PersistentEntity{

	/** The Earthling that this PersistentEntity encapsulates*/
	private Earthling earthling;
	
	/**
	 * Constructor for this class
	 * @param earthling the Earthling instance that this PersistentEntity encapsulates
	 */
	public PersistentEarthling(Earthling earthling) {
		this.earthling = earthling;
	}

	/**
	 * Interface method implementation. Returns the Earthling class name
	 * @see org.trpr.platform.model.common.BusinessEntity#getEntityName()
	 */
	public String getEntityName() {
		return Earthling.class.getSimpleName();
	}

	/**
	 *  Interface method implementation. Returns a {@link PersistentEarthling.PersistentEarthlingIdentifier} created from the Earthling attributes
	 * @see org.trpr.platform.core.spi.persistence.PersistentEntity#getIdentifier()
	 */
	public Identifier getIdentifier() {
		return new PersistentEarthling.PersistentEarthlingIdentifier(this.earthling.getFirstName(), this.earthling.getLastName());
	}

	/**
	 * Interface method implementation. Returns a Criteria instance with {@link Criteria#setIdentifier(Identifier)} set to a {@link PersistentEarthling.PersistentEarthlingIdentifier} 
	 * created from the Earthling attributes
	 * @see org.trpr.platform.core.spi.persistence.PersistentEntity#getCriteriaForLoad()
	 */
	public Criteria getCriteriaForLoad() {
		Criteria loadCriteria = new Criteria();
		loadCriteria.setIdentifier(this.getIdentifier());
		return loadCriteria;
	}
	
	/**
	 * Returns the Earthling instance
	 * @return the Earthling instance
	 */
	public Earthling getEarthling() {
		return this.earthling;
	}
	
	static class PersistentEarthlingIdentifier implements Identifier {
		
		/** The identifier string */
		private String identifier;
		
		/**
		 * Constructor for this class
		 * @param firstName the Earthling's first name
		 * @param lastName the Earthling's last name
		 */
		public PersistentEarthlingIdentifier(String firstName, String lastName) {
			this.identifier = firstName + "_" + lastName;
		}
		
		/**
		 * Returns true if the {@link #toString()} values match
		 * @see org.trpr.platform.core.spi.persistence.Identifier#equals(org.trpr.platform.core.spi.persistence.Identifier)
		 */
		public boolean equals(Identifier anotherIdentifier) {
			return this.toString().equalsIgnoreCase(anotherIdentifier.toString());
		}
		
		/**
		 * Returns the string representation of this PersistentEarthlingIdentifier
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			return this.identifier;
		}
		
	}

}
