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
package org.trpr.platform.core.impl.persistence;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.trpr.platform.core.PlatformConstants;
import org.trpr.platform.core.impl.logging.LogBasedMetricsLogger;
import org.trpr.platform.core.spi.logging.PerformanceMetricsLogger;
import org.trpr.platform.core.spi.management.jmx.InstanceAwareMBean;
import org.trpr.platform.core.spi.persistence.PersistenceHandler;

/**
 * <code>AbstractPersistenceHandler<code> is a convenience implementation of {@link PersistenceHandler} that has behavior common to all persistence handlers such
 * as JMX object naming behavior 
 *
 * @author Regunath B
 * @version 1.0, 11/10/2012
 */

public abstract class AbstractPersistenceHandler implements PersistenceHandler, InstanceAwareMBean {
	
	/** Performance metrics logging control attributes*/
	private boolean performanceMetricsEnabled = false;
	private long performanceLoggingThreshold = 0;

	/**
	 * The PerformanceMetricsLogger instance to use for capturing metrics of code block execution. 
	 */
	protected PerformanceMetricsLogger performanceMetricsLogger = new LogBasedMetricsLogger(this.performanceMetricsEnabled, this.performanceLoggingThreshold);
	
	/**
	 * No arg constructor.
	 */
	public AbstractPersistenceHandler(){
	}

	/**
	 * Interface method implementation. Returns a bean name suffix that comprises of: <escaped Trooper app name>,"handler=<beanKey>"
	 * @see InstanceAwareMBean#getMBeanNameSuffix(Object, String)
	 */
	public String getMBeanNameSuffix(Object managedBean, String beanKey) {
		return String.format(escapeForObjectName(System.getProperty(PlatformConstants.TRPR_APP_NAME)) + ",handler=%s", beanKey);
	}
	
	/**
	 * Enables performance metrics logging for this handler
	 * @param performanceLoggingThreshold the elapsed time threshold for code block execution
	 */
	@ManagedOperation
	public void startPerformanceMetricsLogging(long performanceLoggingThreshold) {
		this.performanceMetricsEnabled = true;
		this.performanceLoggingThreshold = performanceLoggingThreshold;
		this.performanceMetricsLogger.setMetricsCaptureParams(this.performanceMetricsEnabled, this.performanceLoggingThreshold);
	}
	
	/**
	 * Stops performance metrics logging, if any.
	 */
	@ManagedOperation
	public void stopPerformanceMetricsLogging() {
		this.performanceMetricsEnabled = false;
		this.performanceLoggingThreshold = 0;
		this.performanceMetricsLogger.setMetricsCaptureParams(false, 0); // threshold is set as zero. Value does not matter as logging is getting turned off
	}
	
	/** Getter/Setter methods */
	@ManagedAttribute
	public boolean isPerformanceMetricsEnabled() {
		return this.performanceMetricsEnabled;
	}
	@ManagedAttribute
	public long getPerformanceLoggingThreshold() {
		return this.performanceLoggingThreshold;
	}	
	
	/**
	 * Helper method to escape characters for JMX object naming
	 */
	private String escapeForObjectName(String value) {
		value = value.replaceAll(" ", "_");
		value = value.replaceAll(",", ";");
		value = value.replaceAll("=", "~");
		value = value.replaceAll(":", "@");
		value = value.replaceAll(",", ";");
		value = value.replaceAll("=", "~");
		return value;
	}	
	

}