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

package org.trpr.platform.core.spi.logging;

/**
 * The <code>PerformanceMetricsLogger</code> interface exposes methods for logging performance metrics from application code block executions.
 *  
 * Typical usage of this logger :
 * <pre>
 * <code>
 * 
 * 		PerformanceMetricsLogger l = ...look up, inject or construct an implementation...;
 *		
 *		l.startPerformanceMetricsCapture();
 *		... code for inserting data to RDBMS using a Hibernate handler ......
 *		l.logPerformanceMetrics("HibernateHandler","Insert into RDBMS tables for entity ID : " + entityIdentifier);
 *
 *      .... code that we don't want to measure......
 *      
 *      l.startPerformanceMetricsCapture();
 *		... code for publishing events using the notification handler ......
 *		l.logPerformanceMetrics("NotificationHandler","Publish events for entity ID : " + entityIdentifier);
 *
 * </code>
 * </pre>
 * 
 * This is a simple logger that does not support nested {@link #startPerformanceMetricsCapture()} and expects each call to be followed by a 
 * {@link #logPerformanceMetrics(String, String)}.
 * For generic transaction based logging, use {@link TransactionDataLogger}. 
 * 
 * @see TransactionDataLogger
 *  
 * @author Regunath B
 * @version 1.0, 18/05/2012
 */
public interface PerformanceMetricsLogger {

	/**
	 * Informs this metrics logger to start metrics collection timer.
	 */
	public void startPerformanceMetricsCapture();
	
	/**
	 * Informs this metrics logger to log elapsed time since the last call to {@link #startPerformanceMetricsCapture()} along with the specified
	 * tag and message
	 * @param tag the tag used to identify the code block that was executed 
	 * @param message additional message detailing the metrics captured
	 */
	public void logPerformanceMetrics(String tag, String message);
	
	/**
	 * Determines if metrics logging is enabled on this logger
	 * @return true if metrics logging is enabled, false otherwise
	 */
	public boolean isMetricsCaptureEnabled();
	
	/**
	 * Sets the parameters that influence metrics capture.
	 * @param capturePerfMetrics flag to toggle metrics capture
	 * @param performanceLoggingThreshold threshold time in milliseconds that determines if collected metrics are logged.
	 */
	public void setMetricsCaptureParams(boolean capturePerfMetrics, long performanceLoggingThreshold);
	
}
