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

package org.trpr.platform.core.impl.logging;

import java.io.File;

import org.trpr.platform.core.spi.logging.LogConfigurationException;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * The <code>LogFactory</code> is a factory for creating {@link Logger} instances. 
 * 
 * @see Logger
 * 
 * @author Regunath B
 * @version 1.0, 17/05/2012
 */
public abstract class LogFactory {
	
	/**
	 * Configures the underling logging system using the config file specified
	 * @param logConfigFile the logging configuration file
	 * @throws Exception in case of exceptions in configuring the logging system
	 */
	public static void configureLogging(File logConfigFile) throws Exception {
		ch.qos.logback.classic.LoggerContext context = (ch.qos.logback.classic.LoggerContext) org.slf4j.LoggerFactory.getILoggerFactory();
		ch.qos.logback.classic.joran.JoranConfigurator jc = new ch.qos.logback.classic.joran.JoranConfigurator();
		jc.setContext(context); 
		context.reset();
		jc.doConfigure(logConfigFile);
	}

	/**
	 * Returns a <code>Logger<code> instance for the specified Class. Delegates the call to the LogFactory instance of the underlying logging
	 * framework. 
	 * @param clazz the Class for which a Logger is to be returned
	 * @return Logger instance
	 * @throws LogConfigurationException in case a Logger instance cannot be returned for the specified Class
	 */
	 public static Logger getLogger(Class clazz)throws LogConfigurationException {
		 try {
			 org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(clazz);		 
			 //Return a wrapped instance for every call. This factory doesnot cache instances as the wrapper is very light-weight.
			 //Implementations that have significant resource costs should employ a suitable caching mechanism 		 
			 return new SLF4jLogWrapper(logger);
		 } catch (Exception e) {
			 // catch any exception and return wrapped as a LogConfigurationException
			 throw new LogConfigurationException("Error creating a Logger instance : " + e.getMessage(),e);
		 }
	 }
	 
	/**
	 * Returns a <code>Logger<code> instance for the specified name. Delegates the call to the LogFactory instance of the underlying logging
	 * framework. 
	 * @param name logical name for the Logger that is to be returned
	 * @return Logger instance
	 * @throws LogConfigurationException in case a Logger instance cannot be returned for the specified logical name
	 */
	 public static Logger getLogger(String name)throws LogConfigurationException {
		 try {
			 org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(name);
			 //Return a wrapped instance for every call. This factory doesnot cache instances as the wrapper is very light-weight.
			 //Implementations that have significant resource costs should employ a suitable caching mechanism 		 
			 return new SLF4jLogWrapper(logger);
		 } catch (Exception e) {
			 // catch any exception and return wrapped as a LogConfigurationException
			 throw new LogConfigurationException("Error creating a Logger instance : " + e.getMessage(),e);
		 }
	 }
	 
}
