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
package org.trpr.platform.runtime.spi.bootstrap;

import org.trpr.platform.runtime.spi.container.Container;

/**
 * The <code>BootstrapInfo</code> class is a configuration place-holder for bootstrapping the runtime
 * 
 * @author Regunath B
 * @version 1.0, 06/06/2012
 */
public class BootstrapInfo {

	/** Path to the application projects root */
	private String projectsRoot;
	
	/** The application name*/
	private String applicationName;
	
	/**The runtime nature */
	private String runtimeNature;
	
	/**The fully qualified ComponentContainer class name*/
	private String componentContainerClassName;
	
	/** The Container instance*/
	private Container container;
	
	/** == start Spring DI style setters/getters */
	public String getProjectsRoot() {
		return this.projectsRoot;
	}
	public void setProjectsRoot(String projectsRoot) {
		this.projectsRoot = projectsRoot;
	}
	public String getApplicationName() {
		return this.applicationName;
	}
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	public String getRuntimeNature() {
		return this.runtimeNature;
	}
	public void setRuntimeNature(String runtimeNature) {
		this.runtimeNature = runtimeNature;
	}
	public String getComponentContainerClassName() {
		return this.componentContainerClassName;
	}
	public void setComponentContainerClassName(String componentContainerClassName) {
		this.componentContainerClassName = componentContainerClassName;
	}	
	public Container getContainer() {
		return this.container;
	}
	public void setContainer(Container container) {
		this.container = container;
	}
	/** == end Spring DI style setters/getters */
	
}
