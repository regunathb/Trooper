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
package org.trpr.platform.seda.impl.mule.exceptionhandling;

import org.mule.config.ExceptionHelper;
import org.mule.service.DefaultServiceExceptionStrategy;
import org.trpr.platform.core.impl.management.jmx.JMXNotificationDispatcher;

/**
 * The <code>ServiceExceptionStrategy</code> class is a sub-type of the Mule DefaultServiceExceptionStrategy that additionally sends a JMX
 * notification
 * 
 * @author Regunath B
 * @version 1.0, 23/08/2012
 */

public class ServiceExceptionStrategy extends DefaultServiceExceptionStrategy {
	
	/**
	 * The JMX notification dispatcher to use for sending JMX notifications
	 */
	private JMXNotificationDispatcher jmxNotificationDispatcher;	
	
	/**
	 * Overriden superclass method. Sends a JMX notification of the exception and also logs the contents. 
	 * @see org.mule.service.DefaultServiceExceptionStrategy#defaultHandler(java.lang.Throwable)
	 */
	protected void defaultHandler(Throwable t) {
		super.defaultHandler(t);
		this.jmxNotificationDispatcher.dispatchException(ExceptionHelper.getRootMuleException(t), this.getClass().getName());
	}

	/** Start Java bean style setters and getters*/
	public JMXNotificationDispatcher getJmxNotificationDispatcher() {
		return jmxNotificationDispatcher;
	}
	public void setJmxNotificationDispatcher(
			JMXNotificationDispatcher jmxNotificationDispatcher) {
		this.jmxNotificationDispatcher = jmxNotificationDispatcher;
	}	
	/** End Java bean style setters and getters*/
	
}
