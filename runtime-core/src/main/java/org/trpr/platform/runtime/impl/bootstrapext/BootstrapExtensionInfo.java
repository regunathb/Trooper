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
import java.util.Map;

import org.trpr.platform.runtime.spi.bootstrapext.BootstrapExtension;

/**
 * The <code>BootstrapExtensionInfo</code> is a data container that defines a {@link BootstrapExtension}
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */
public class BootstrapExtensionInfo implements Comparable<BootstrapExtensionInfo> {
	
	/** Name of the bootstrap extension*/
	private String beName;
	
	/** The fully qualified BootstrapExtension implementation class*/
	private String beClassName;
	
	/** The boolean override to enable/disable the BootstrapExtension. Default is enabled*/
	private boolean enabled = true;
	
	/** List of bootstrap extension names that the bootstrap extension is dependent on*/
	private LinkedList<String> dependenciesList =  new LinkedList<String>();
	
	/** Map containing optional additional properties*/
	private Map<String, String> beProperties = new HashMap<String, String>();
	
	/**
	 * No args Constructor for this class
	 */
	public BootstrapExtensionInfo() {			
	}
	
	/**
	 * Constructor for this class
	 * @param beName the name of the BootstrapExtension
	 * @param beClassName the BootstrapExtension class name
	 * @param enabled boolean flag denoting if BootstrapExtension is enabled
	 */
	public BootstrapExtensionInfo(String beName, String beClassName,  boolean enabled) {
		this.beName = beName;
		this.beClassName = beClassName;
		this.enabled = enabled;
	}
	
	/**
	 * Interface method implementation used during sorting
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(BootstrapExtensionInfo beInfo) {
		return this.beName.compareTo(beInfo.beName);
	}

	/** === Start Spring DI style getters & setters */
	public String getBeName() {
		return this.beName;
	}
	public void setBeName(String beName) {
		this.beName = beName;
	}
	public String getBeClassName() {
		return this.beClassName;
	}
	public void setBeClassName(String beClassName) {
		this.beClassName = beClassName;
	}
	public boolean isEnabled() {
		return this.enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public LinkedList<String> getDependenciesList() {
		return this.dependenciesList;
	}
	public void setDependenciesList(LinkedList<String> dependenciesList) {
		this.dependenciesList = dependenciesList;
	}
	public Map<String, String> getBeProperties() {
		return this.beProperties;
	}
	public void setBeProperties(Map<String, String> beProperties) {
		this.beProperties = beProperties;
	}
	/** === End Spring DI style getters & setters */

	
}	

