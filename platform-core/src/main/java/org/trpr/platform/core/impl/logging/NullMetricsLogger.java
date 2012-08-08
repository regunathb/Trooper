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

import org.trpr.platform.core.spi.logging.PerformanceMetricsLogger;

/**
 * The <code>NullMetricsLogger</code> class is an implementation of the {@link PerformanceMetricsLogger} that defines a "No Op"
 * performance metric logger. 
 *
 * This implementation consumes method calls, discards data and does not perform any activity or logic. Not a useful implementation and provides just
 * interface compatibility.
 *
 * @author Ashok Ayengar
 * @author Regunath B
 * @version 1.0, 18/05/2012
 */

public class NullMetricsLogger implements PerformanceMetricsLogger {

	/**
	 * Interface method implementation. Does nothing.
	 * @see PerformanceMetricsLogger#startPerformanceMetricsCapture()
	 */
	public void startPerformanceMetricsCapture() { 
		// do nothing
	}

	/**
	 * Interface method implementation. Does nothing.
	 * The method just consumes the call and does not perform any logic.
	 * @see PerformanceMetricsLogger#logPerformanceMetrics(String, String)
	 */
	public void logPerformanceMetrics(String tag, String message) {
		// do nothing
	}

	/**
	 * Interface method implementation. Returns false always
	 * @see PerformanceMetricsLogger#isMetricsCaptureEnabled()
	 */
	public boolean isMetricsCaptureEnabled() {
		return false;
	}

	/**
	 * Interface method implementation. Does nothing
	 * @see PerformanceMetricsLogger#setMetricsCaptureParams(boolean, long)
	 */
	public void setMetricsCaptureParams(boolean capturePerfMetrics, long performanceLoggingThreshold) {
		// do nothing
	}
	
}
