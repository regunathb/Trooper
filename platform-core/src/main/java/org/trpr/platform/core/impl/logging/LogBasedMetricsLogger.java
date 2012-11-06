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

import org.trpr.platform.core.PlatformConstants;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.logging.PerformanceMetricsLogger;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;

/**
 * The <code>LogBasedMetricsLogger</code> class is an implementation of the {@link PerformanceMetricsLogger} that directs metrics logging
 * output to the Platform's {@link Logger} using the default appender identified by {@link PlatformConstants#PERF_LOGGER_CATEGORY}.
 * This implementation uses the perf4j Java library {@link http://perf4j.codehaus.org/}.
 *  
 * @author Regunath B
 * @version 1.0, 18/05/2012
 */
public class LogBasedMetricsLogger implements PerformanceMetricsLogger {
	
	/** The performance logger stop watch*/
	private StopWatch stopWatch = null;

	/** Flag to turn on/off performance metrics collection*/
	protected boolean capturePerfMetrics = false;

	/** Elapsed time threshold to turn on/off performance logging*/
	protected long performanceLoggingThreshold;
	
	/**
	 * No args constructor
	 */
	public LogBasedMetricsLogger() {		
	}
	
	/**
	 * Constructor for this class
	 * @param capturePerfMetrics boolean to indicate capture of performance metrics. Default is false
	 * @param performanceLoggingThreshold threshold in milliseconds for performanace metrics logging. Default is zero i.e. all elapsed times will be logged
	 */
	public LogBasedMetricsLogger(boolean capturePerfMetrics, long performanceLoggingThreshold){
		this.capturePerfMetrics = capturePerfMetrics;
		this.performanceLoggingThreshold = performanceLoggingThreshold;
	}

	/**
	 * Interface method implementation.
	 * Starts the performance logger for capturing time of subsequent code execution until {@link #logPerformanceMetrics(String, String)} is called. 
	 * @see PerformanceMetricsLogger#startPerformanceMetricsCapture()
	 */
	public void startPerformanceMetricsCapture() {
		if (this.capturePerfMetrics) {
			if (this.stopWatch == null) {
				// we try to create the stop watch only once even when concurrent access happens on a singleton instance of this handler
				synchronized(this) {
					if (this.stopWatch == null) {
						this.stopWatch = new Slf4JStopWatch(LogFactory.getLogger(PlatformConstants.PERF_LOGGER_CATEGORY));
					}
				}
			} else {
				this.stopWatch.start();
			}
		}
	}

	/**
	 * Interface method implementation.
	 * Logs the specified tag, message and the elapsed time to the Platform logger. 
	 * @see PerformanceMetricsLogger#logPerformanceMetrics(String, String)
	 */
	public void logPerformanceMetrics(String tag, String message) {
		if (this.capturePerfMetrics && this.stopWatch.getElapsedTime() > this.performanceLoggingThreshold) {
			this.stopWatch.stop(tag, message);
		}
	}

	/**
	 * Interface method implementation.
	 * Returns value of the capture metrics flag maintained by this class
	 * @see PerformanceMetricsLogger#isMetricsCaptureEnabled()
	 */
	public boolean isMetricsCaptureEnabled() {
		return this.capturePerfMetrics;
	}
	
	/**
	 * Interface method implementation. Uses the specified params to control logging by the perf4j stop watch.
	 * @see PerformanceMetricsLogger#setMetricsCaptureParams(boolean, long)
	 */
	public void setMetricsCaptureParams(boolean capturePerfMetrics, long performanceLoggingThreshold) {
		this.capturePerfMetrics = capturePerfMetrics;
		this.performanceLoggingThreshold = performanceLoggingThreshold;
	}
}
