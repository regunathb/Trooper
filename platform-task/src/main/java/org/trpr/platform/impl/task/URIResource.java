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
package org.trpr.platform.impl.task;

import org.trpr.platform.spi.task.Resource;

/**
 * The <code>URIResource</code> class is an implementation of {@link Resource} that creates a convenient serialized form of a Resource that this was created with.
 * 
 * @author Regunath B
 * @version 1.0, 28/09/2012
 */
public class URIResource<T> implements Resource<String> {

	/** Default serial version UID*/
	private static final long serialVersionUID = 1L;
	
	/** The Resource that this creates a serialized representation for. Marked as transient to prevent the Resource from moving across JVMs */
	private transient Resource<T> resource;
	
	/**
	 * Constructor for this class
	 * @param resource the Resource that this URIResource encapsulates
	 */
	public URIResource(Resource<T> resource) {
		this.resource = resource;
	}
	
	/**
	 * Interface method implementation. Returns false always as only the serialized form of the wrapped Resource is guaranteed to exist.
	 * Clients should try to reconstruct the original resource from {@link #getSerializedForm()}
	 * @see org.trpr.platform.spi.task.Resource#exists()
	 */
	public boolean exists() {
		return false; // always returns false. 
	}

	/**
	 * Interface method implementation. Returns the name as the serialized form
	 * @see org.trpr.platform.spi.task.Resource#getSerializedForm()
	 */
	public String getSerializedForm() {
		return this.getName();
	}

	/**
	 * Interface method implementation. Returns the name of the Resource that this one wraps
	 * @see org.trpr.platform.spi.task.Resource#getName()
	 */
	public String getName() {
		return this.resource.getName();
	}

	/**
	 * Interface method implementation. Throws {@link UnsupportedOperationException}
	 * @see org.trpr.platform.spi.task.Resource#getDescription()
	 */
	public String getDescription() {
		throw new UnsupportedOperationException("Method not supported on this serialized Resource. Consider constructing the resource using #getSerializedForm()");
	}

	/**
	 * Interface method implementation. Returns true if the specified Resource is the same as the one that this was created with.
	 * @see org.trpr.platform.spi.task.Resource#equals(org.trpr.platform.spi.task.Resource)
	 */
	@Override
	public boolean equals(Resource<String> resource) {
		return resource == this.resource;
	}


}
