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

import org.trpr.platform.core.spi.logging.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * The <code>SLF4jLogWrapper</code> is a composite over the Logger returned by SLF4J LoggerFactory {@linkplain http://www.slf4j.org/} implementation.
 * This provides a ready-to-use good implementation for early use in Trooper until an alternative becomes available.
 * 
 * @see Logger
 * 
 * @author Regunath B
 * @version 1.0, 16/05/2012
 */
public class SLF4jLogWrapper extends MarkerIgnoringBase implements Logger {
	
	private static final long serialVersionUID = 1L;
	
	/** The SL4J Logger instance that this wrapper delegates all calls to */
	private org.slf4j.Logger sl4jLogger;
	
	/**
	 * Constructor
	 * @param logger the SLF4j Logger instance returned by the LoggerFactory
	 */
	public SLF4jLogWrapper(org.slf4j.Logger logger) {
		this.sl4jLogger = logger;
	}

	/** Interface method implementations. All calls are delegated to the SLF4j Logger instance*/
	public void debug(String arg0) { this.sl4jLogger.debug(arg0); }
	public void debug(String arg0, Object arg1) { this.sl4jLogger.debug(arg0, arg1); }
	public void debug(String arg0, Object[] arg1) { this.sl4jLogger.debug(arg0, arg1);	}
	public void debug(String arg0, Throwable arg1) { this.sl4jLogger.debug(arg0, arg1);	}
	public void debug(String arg0, Object arg1, Object arg2) { this.sl4jLogger.debug(arg0, arg1, arg2);	}
	
	public void error(String arg0) { this.sl4jLogger.error(arg0);}
	public void error(String arg0, Object arg1) { this.sl4jLogger.error(arg0, arg1);}
	public void error(String arg0, Object[] arg1) { this.sl4jLogger.error(arg0, arg1);}
	public void error(String arg0, Throwable arg1) { this.sl4jLogger.error(arg0, arg1);}
	public void error(String arg0, Object arg1, Object arg2) { this.sl4jLogger.error(arg0, arg1, arg2);}

	public void info(String arg0) { this.sl4jLogger.info(arg0);}
	public void info(String arg0, Object arg1) { this.sl4jLogger.info(arg0, arg1);}
	public void info(String arg0, Object[] arg1) { this.sl4jLogger.info(arg0, arg1);}
	public void info(String arg0, Throwable arg1) { this.sl4jLogger.info(arg0, arg1);}
	public void info(String arg0, Object arg1, Object arg2) { this.sl4jLogger.info(arg0, arg1, arg2);}

	public boolean isDebugEnabled() { return this.sl4jLogger.isDebugEnabled();}
	public boolean isErrorEnabled() { return this.sl4jLogger.isErrorEnabled();}
	public boolean isInfoEnabled() { return this.sl4jLogger.isInfoEnabled();}
	public boolean isTraceEnabled() { return this.sl4jLogger.isTraceEnabled();}
	public boolean isWarnEnabled() { return this.sl4jLogger.isWarnEnabled();}

	public void trace(String arg0) { this.sl4jLogger.trace(arg0);}
	public void trace(String arg0, Object arg1) { this.sl4jLogger.trace(arg0, arg1);}
	public void trace(String arg0, Object[] arg1) { this.sl4jLogger.trace(arg0, arg1);}
	public void trace(String arg0, Throwable arg1) { this.sl4jLogger.trace(arg0, arg1);}
	public void trace(String arg0, Object arg1, Object arg2) { this.sl4jLogger.trace(arg0, arg1, arg2);}

	public void warn(String arg0) { this.sl4jLogger.warn(arg0);}
	public void warn(String arg0, Object arg1) { this.sl4jLogger.warn(arg0, arg1);}
	public void warn(String arg0, Object[] arg1) { this.sl4jLogger.warn(arg0, arg1);}
	public void warn(String arg0, Throwable arg1) { this.sl4jLogger.warn(arg0, arg1);}
	public void warn(String arg0, Object arg1, Object arg2) { this.sl4jLogger.warn(arg0, arg1, arg2);}

}
