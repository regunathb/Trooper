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

/**
 * The <code>RuntimeConstants</code> class is a placeholder for all runtime framework constants.
 * 
 * @author Regunath B
 * @version 1.0, 04/06/2012
 */
public abstract class RuntimeConstants {

	/**
	 * The environment variable that might hold the path to the config file
	 */
	public static final String CONFIG_FILE_VAR = "PLATFORM_CONFIG";

	/**
	 * The environment variable that might hold bean property values
	 */
	public static final String CONFIG_PROPERTIES_VAR = "PLATFORM_PROPERTIES";

	/** The variable token to identify the path to the bootstrap config file */
	public static final String CONFIG_FILE_NAME_TOKEN = "$RUNTIME_CONFIG_PATH";
	
	/**
	 * Constants for the conventions on config file names
	 */
	public static final String COMMON_SPRING_BEANS_CONFIG = "packaged/common-spring-beans.xml";	// its a file picked up from classpath
	public static final String BOOTSTRAP_EXTENSIONS_FILE = "bootstrap_extensions.xml";
	public static final String LOGGING_FILE = "logback.xml";
	
	/**
	 * Constant values for runtime nature
	 */
	public static final String STANDALONE = "STANDALONE";
	public static final String SERVER = "SERVER";
	public static final String TEST = "TEST";
	
	/** The Application name identifier JVM system property*/
	public static final String TRPR_APP_NAME="org.trpr.application.name";
	
	/** The runtime keep-alive background thread name */
	public static final String BOOTSTRAP_BACKGROUND_THREADNAME = "Trpr-Bgrnd-Thread";
	
	/**
	 * Identifiers for the framework variables as defined during Bootstrap
	 */
	public static final String PROJECTS_ROOT = "org.trpr.platform.runtime.projects.root";
	public static final String NATURE = "org.trpr.platform.runtime.nature";
	public static final String CONTAINER_TYPE = "org.trpr.platform.runtime.componentContainer";
	
	/**
	 * Constant to identify the resource folder names under different projects
	 */
	public static final String RESOURCES_SUFFIX = "resources";
	public static final String EXTERNAL_RESOURCES_SUFFIX = "external";
	
	/** Constants for Bootstrap Monitoring */	
	public static final String BOOTSTRAPMONITOREDEVENT="BootstrapMonitoredEvent";	
	public static final String BOOTSTRAP_START_STATE="started";	
	public static final String BOOTSTRAP_STOP_STATE="stopped";
	public static final String BOOTSTRAP_DESTROY_STATE="destroyed";	
	
	/** 
	 * Bean names from common Spring application context i.e. parent of all application contexts and 
	 * defined in {@link RuntimeConstants}{@link #COMMON_SPRING_BEANS_CONFIG}
	 */
	public static final String JMX_NOTIFICATION_BEAN="jmxNotificationBean";	
	public static final String BOOTSTRAP_MONITOR_BEAN="bootstrapMonitorBean";
	
}
