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

package org.trpr.platform.core.spi.persistence;

/**
 * The <code>Identifier</code> uniquely identifies an entity in a persistent data store such as RDBMS, File system or NoSQL data store.     
 *  
 * @author Regunath B
 * @version 1.0, 22/05/2012
 */
public interface Identifier {
	
	/**
	 * Equals method for another instance of a compatible Identifier type
	 * @param anotherIdentifier the Identifier against which equality is to be determined
	 * @return true if the specified Identifier is the same/equals this one
	 */
	public boolean equals (Identifier anotherIdentifier);
	
	/**
	 * Returns a string representation of this Identifier
	 * @return Identifier value as a String
	 */
	public String toString();
	
}
