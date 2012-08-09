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
package org.trpr.platform.runtime.common;

import java.util.HashMap;

import org.trpr.platform.runtime.spi.component.ComponentContainer;

/**
 * The <code>ServerVariables</code> class is a container for core framework runtime meta data.

 * @author Regunath B
 * @version 1.0, 04/06/2012
 */
public class RuntimeVariables {

	/**
	 * Singleton instance of this class
	 */
	private static RuntimeVariables instance = new RuntimeVariables();

	/**
	 * Hashmap of environment values hashed by variable name
	 */
	private static final HashMap<String, String> configValues = new HashMap<String, String>();

	/**
	 * Private constructor to prevent instantiation
	 */
	private RuntimeVariables() {
	}

	/**
	 * Static accessor to get an instance of this class.
	 * @return the singleton instance for this class
	 */
	public static final RuntimeVariables getInstance() {
		return instance;
	}

	/**
	 * Sets the specified value for the specified key in this class
	 * @param key unique string identifying the environment variable
	 * @param value string value of the environment variable
	 */
	public void setVariable(String key, String value) {
		configValues.put(key, value);
	}

	/**
	 * Clears the configValues loaded during bootstrap
	 */
	public void clear() {
		configValues.clear();
	}

	/**
	 * Gets the environment variable identified by the specified key. Tries to
	 * retrieve value from Java runtime if the specified key does not exist.
	 * @param key unique string identifying the environment variable
	 * @return null or the value of the environment variable
	 */
	public static String getVariable(String key) {
		String value = (String) configValues.get(key);
		if (value == null)
			value = System.getProperty(key);
		return value;
	}

	/**
	 * Gets all the environment variables flattened out into one HashMap collection
	 * @return HashMap containing all property key value pairs as defined in bootstrap configuration
	 */
	public static HashMap<String, String> getAllProperties() {
		return configValues;
	}

	/**
	 * Convenience method to return the root directory of the projects
	 * @return null or the path to the projects
	 */
	public static String getProjectsRoot() {
		return getVariable(RuntimeConstants.PROJECTS_ROOT);
	}

	/**
	 * Convenience method to return the nature of the runtime - standalone, test etc.
	 * @return null or the nature of the runtime 
	 */
	public static String getRuntimeNature() {
		return getVariable(RuntimeConstants.NATURE).toUpperCase();
	}

	/**
	 * Convenience method to return the type of {@link ComponentContainer}
	 * @return null or the 
	 */
	public static String getContainerType() {
		return getVariable(RuntimeConstants.CONTAINER_TYPE);
	}

}
