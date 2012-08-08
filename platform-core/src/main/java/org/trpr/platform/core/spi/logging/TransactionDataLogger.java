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
 * <code>TransactionDataLogger<code> is a logger for transaction details. 
 * A Transaction is typically a set of operations performed by an application in a synchronous manner.  
 * This logger assumes that the entire transaction/request is processed by a single thread. 
 * 
 * Note that spawning new threads as part of request processing would require different considerations when using this API as 
 * this logger captures execution times only on the currently executing thread.
 * 
 * Typical usage of this logger :
 * <pre>
 * <code>
 * 
 * 		TransactionDataLogger l = ...look up, inject or construct an implementation...;
 *		
 *		l.startTrackingTimeFor("BREAKFAST");
 *		...cook breakfast...
 *		l.stopTrackingTimeFor("BREAKFAST");
 *		
 *		l.startTrackingTimeFor("LUNCH");
 *		...go out for lunch...
 *		l.stopTrackingTimeFor("LUNCH");
 *		
 *		l.startTrackingTimeFor("DINNER");
 *		...order a dinner...
 *		l.stopTrackingTimeFor("DINNER");
 *
 *		l.recordTransactionAttribute("DAY_OF_WEEK", "Monday :( ");
 *		
 *		l.log(); // Analyze metrics to identify most time consuming activity
 *
 * </code>
 * </pre>
 * 
 * @author Srikanth Shreenivas
 * @author Regunath B
 * @version 1.0, 21/05/2012
 */

public interface TransactionDataLogger {

	/**
	 * Records free form transaction attribute. Useful in logs to identify transaction and its key attributes.
	 * @param attrName the attribute name identifier
	 * @param attrValue the attribute value
	 */
	public void recordTransactionAttribute(String attrName, String attrValue);
	
	/**
	 * Starts tracking time of execution for the specified metric name
	 * @param metricName the logical & distinct metric name registered on this logger
	 */
	public void startTrackingTimeFor(String metricName);
	
	/**
	 * Notes the finish of an operation denoted by metricName
	 * @param metricName the logical & distinct metric name registered on this logger
	 */
	public void stopTrackingTimeFor(String metricName);
	
	/**
	 * Logs the metrics.  Implementations can provide different
	 * logging mechanisms such as write to log file, save to DB,
	 * publish an event etc.
	 * 
	 * Note that this method should b ecalled ONCE and ONLY ONCE for a request/transaction.
	 */
	public void log();

}
