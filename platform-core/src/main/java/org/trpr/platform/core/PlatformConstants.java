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

package org.trpr.platform.core;

/**
 * The <code>PlatformConstants</code> is a place holder for all core framework constants
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */

public abstract class PlatformConstants {
	
	/** Constant for system line separator */
	public static final String LINE_SEPARATOR_VALUE = "line.separator";

	/** Constant for system file seperator */
	public static final String FILE_SEPARATOR_VALUE = "file.separator";

	/** The Application name identifier as a JVM system property*/
	public static final String CCELL_APP_NAME="org.trpr.application.name";
	
	/** The generic logger logical names for loggers*/
	public static final String PERF_LOGGER_CATEGORY="org.trpr.platform.core.spi.logging.PerformanceLogger";
	public static final String TX_LOGGER_CATEGORY="org.trpr.platform.core.spi.logging.TransactionDataLogger";
	
}
