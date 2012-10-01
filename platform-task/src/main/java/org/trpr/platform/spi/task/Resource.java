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
package org.trpr.platform.spi.task;

import java.io.Serializable;

/**
 * The <code>Resource</code> interface is a resource descriptor that abstracts from the actual type of underlying resource, such as a file, classpath
 * resource, a POJO instance or database connection.
 * Implementations of this type may optionally be serializable i.e. sent over the network and reconstructed entirely at the receiving end.
 * Resource types that are not directly serializable may return a proxy instead that is serializable and which in turn reconstructs the original Resource   
 * at the receiving end.
 * 
 * Parts of behavior of this interface is inspired by the Spring framework's Core I/O interfaces.
 * 
 * @author Regunath B
 * @version 1.0, 01/06/2012
 */
public interface Resource<T> extends Serializable {

	/**
	 * Determine if this Resource actually exists in physical form. This method performs a definitive existence check, whereas the
	 * existence of a <code>Resource</code> handle only guarantees a valid descriptor handle
	 * @return boolean true if this resource exists, false if it is only a handle
	 */
	public boolean exists();
	
	/**
	 * Returns a form of this Resource - proxy or itself that may be sent over the network
	 * @return this Resource or a handle/proxy that can be used to reconstruct this Resource
	 */
	public T getSerializedForm();
	
	/**
	 * Returns a name for this Resource. The name may be simple or represent a unique reference to this resource in the context that manages this
	 * Resource. For e.g. it could be of URL form in a Resource registry. 
	 * @return often unique string representation of this Resource in the context that manages this Resource
	 */
	public String getName();
	
	/**
	 * Return a description for this resource, to be used for error output when working with the resource.
	 * Implementations are also encouraged to return this value from their <code>toString</code> method.
	 * @see java.lang.Object#toString()
	 * @return a meaningful description of this Resource
	 */
	public String getDescription();
	
	/**
	 * Determines if this Resource can be deemed equivalent to the specified Resource. Interpretations of equality are implementation specific
	 * @return boolean true if the specified Resource may be considered equal, false otherwise 
	 */
	public boolean equals(Resource<T> resource);
	
}
