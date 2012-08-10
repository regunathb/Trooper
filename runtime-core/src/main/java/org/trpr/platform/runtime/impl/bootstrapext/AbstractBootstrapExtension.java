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
package org.trpr.platform.runtime.impl.bootstrapext;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.trpr.platform.core.PlatformException;
import org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension;
import org.trpr.platform.runtime.spi.config.ConfigurationException;

/**
 * The <code>AbstractBootstrapExtension</code> class is an implementation of the {@link BootstrapExtension} that provides default implementation
 * for some of the interface methods.
 * 
 * @author Regunath B
 * @version 1.0, 06/06/2012
 */
public abstract class AbstractBootstrapExtension implements BootstrapExtension {
	
	/** The name place-holder*/
	private String name;

	/** List of dependency BootstrapExtensionS that have been initialized and are ready to use*/
	private List<BootstrapExtension> dependencyBEList = new LinkedList<BootstrapExtension>();
	
	/** The optional properties place-holder*/
	private Map<String, String> properties = new HashMap<String, String>();

	/**
	 * Interface method implementation. Returns the name of this BootstrapExtension.
	 * @see org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension#getName()
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Interface method implementation. Sets the specified name to this BootstrapExtension 
	 * @see org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Interface method implementation. Adds the specified dependency bootstrap extensions
	 * @see org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension#addDependency(org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension[])
	 */
	public void addDependency(BootstrapExtension... extensions) {
		for (BootstrapExtension be : extensions) {
			this.dependencyBEList.add(be);
		}
	}
	
	/**
	 * Interface method implementation. Sets the optional properties for this BootstrapExtension
	 * @see org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension#setProperties(java.util.Map)
	 */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	/**
	 * Returns the optional property identified by the specified property name
	 * @param propertyName the property name identifier
	 * @return the property value
	 * @throws ConfigurationException in case the property has not been configured
	 */
	protected String getBootstrapExtensionProperty(String propertyName) throws ConfigurationException {
		if (this.properties.containsKey(propertyName)) {
			return this.properties.get(propertyName);
		}
		throw new ConfigurationException("Requested property found/configured for BootstrapExtension : " + this.getName() + ". Property name is : " + propertyName);
	}
	
	/**
	 * Returns the optional properties map for this BootstrapExtension
	 * @return the optional properties map
	 */
	protected Map<String, String> getAllProperties() {
		return this.properties;
	}
	
	/**
	 * Returns the dependency BootstrapExtension identified by the specified name
	 * @param beName the name of the BootstrapExtension as specified in {@link BootstrapExtensionInfo#getBeName()}
	 * @return the dependency BootstrapExtension
	 * @throws PlatformException in case no matching BootstrapExtension with specified name exists
	 */
	protected BootstrapExtension getDependencyBootstrapExtension(String beName) throws PlatformException {
		for (BootstrapExtension be : this.dependencyBEList) {
			if (be.getName().equalsIgnoreCase(beName)) {
				return be;
			}
		}
		throw new PlatformException("Dependency BootstrapExtension not found/not loaded. Check configuration and stratup log. Failed to load dependency is : " + beName);
	}
	
}
