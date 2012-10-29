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

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.trpr.platform.core.PlatformConstants;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.logging.TransactionDataLogger;

/**
 * <code>LogBasedTransactionDataLogger<code> is an implementation of the {@link TransactionDataLogger} that collects and logs transaction details to
 * the configured underlying logging system.
 * 
 * Logs the transaction details to a logger category identified by {@link PlatformConstants#TX_LOGGER_CATEGORY} that may be configured as shown below in
 * log4j:
 * 
 * <pre><code>
   		log4j.logger.org.trpr.platform.core.spi.logging.TransactionDataLogger=INFO, txn-data-appender
 * </pre></code>
 * Subsequently define an appender with the name "txn_data_appender".  Typically it is a rolling file appender.
 * Also, ensure that "layout" property of appender is set to fully qualified name of this class.  Example below
 * <pre><code>
		log4j.appender.txn_data_appender=org.apache.log4j.RollingFileAppender
		log4j.appender.txn_data_appender.File=${LOG.FILE.PATH}/trpr-<app-name>-monitor.csv
		log4j.appender.txn_data_appender.layout=org.trpr.platform.core.impl.logging.LogBasedTransactionDataLogger
		log4j.appender.txn_data_appender.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss:SSS},%t,%m%n
		log4j.appender.txn_data_appender.MaxFileSize=10000KB
		log4j.appender.txn_data_appender.MaxBackupIndex=100
		log4j.appender.txn_data_appender.Header=true
 * </pre></code> 
 * 
 * This implementation uses a Java {@link ThreadLocal} to store the transaction metrics for the currently active Thread. This implementation
 * protects from runaway loggers by imposing default and configurable thresholds for metrics logged for a transaction. This logger truncates
 * metrics logged after the threshold is reached and data truncation is suitably logged using the transaction attribute : {@link LogBasedTransactionDataLogger#DATA_TRUNCATED_ATTR}, 
 * value : {@link LogBasedTransactionDataLogger#DATA_TRUNCATED_VALUE} when {@link #log()} is invoked. 
 * 
 * @author Srikanth Shreenivas
 * @author Regunath B
 * @version 1.0, 21/05/2012
 */
public class LogBasedTransactionDataLogger implements TransactionDataLogger {

	/** The threshold for number of metrics/attributes collected per transaction*/
	private static final int METRICS_THRESHOLD = 100; 
	
	/** String constant for unavailable metrics value*/
	private static String NOT_AVAILABLE = "NA";
	
	/** Transaction attribute and value to indicate*/
	public static final String DATA_TRUNCATED_ATTR = "DATA_TRUNCATION";
	public static final String DATA_TRUNCATED_VALUE = "Data truncated[Threshold Exceeded]";
	
	/** Logger for logging the transaction details to the underlying logging configuration*/
	private static final Logger LOGGER = LogFactory.getLogger(PlatformConstants.TX_LOGGER_CATEGORY);
	
	/** ThreadLocal instance for holding metrics, attributes and their values*/
	private static ThreadLocal<Map<String, String>> metricNameToValueMap = new ThreadLocal<Map<String, String>>() {
		@Override
		protected Map<String, String> initialValue() {
			return new TreeMap<String, String>();
		}
	};
	
	/** The threshold setting*/
	private int metricsCountThreshold = METRICS_THRESHOLD;
	
	/**
	 * Interface method implementation. 
	 * @see org.trpr.platform.core.spi.logging.TransactionDataLogger#recordTransactionAttribute(java.lang.String, java.lang.String)
	 */
	public void recordTransactionAttribute(String attrName, String attrValue) {
		addTXData(attrName, attrValue);
	}
	
	/**
	 * Interface method implementation. 
	 * @see org.trpr.platform.core.spi.logging.TransactionDataLogger#startTrackingTimeFor(java.lang.String)
	 */
	public void startTrackingTimeFor(String metricName) {
		addTXData(metricName, String.valueOf(System.currentTimeMillis()));
	}

	/**
	 * Interface method implementation. 
	 * @see org.trpr.platform.core.spi.logging.TransactionDataLogger#stopTrackingTimeFor(String)
	 */
	public void stopTrackingTimeFor(String metricName) {
		String metric = metricName;
		
		// Store the difference between previous value (timestamp of start of tracking) and current time.
		String previousValue = metricNameToValueMap.get().get(metric);
		
		if (StringUtils.isNotBlank(previousValue) && StringUtils.isNumeric(previousValue)) {
			long diff = System.currentTimeMillis() - Long.valueOf(previousValue);
			metricNameToValueMap.get().put(metric, String.valueOf(diff));
		} else {
			//If no previous value, lets set the value to -1 to indicate that developer forgot to call the start
			metricNameToValueMap.get().put(metric, String.valueOf(-1));
		}
	}

	/**
	 * Interface method implementation.
	 * @see org.trpr.platform.core.spi.logging.TransactionDataLogger#log()
	 */
	public void log() {
		StringBuffer sb = new StringBuffer();
		
		for (String k : metricNameToValueMap.get().keySet()) {
			sb.append(decorateForCsv(k) + ",");
			sb.append(decorateForCsv(metricNameToValueMap.get().get(k)) + ",");
		}
	
		LOGGER.info(sb.toString());
		
		//Lets clear the map after logging the current transaction's data		
		clearValues();
	}
	
	/** === Start Getter, Setter methods */
	public int getMetricsCountThreshold() {
		return this.metricsCountThreshold;
	}
	public void setMetricsCountThreshold(int metricsCountThreshold) {
		this.metricsCountThreshold = metricsCountThreshold;
	}
	/**== End Getter,Setter methods */
	
	/**
	 * Clears the map values.
	 */
	private void clearValues() {
		for (String key : metricNameToValueMap.get().keySet()) {
			metricNameToValueMap.get().put(key, null);
		}
	}
	
	/**
	 * Adds double quotes to the string, and escapes any double quote present in the input.
	 * @param s the undecorated input string
	 * @return decorated string for writing to CSV
	 */
	private String decorateForCsv(String s) {
		return StringUtils.isNotBlank(s) ? "\"" + s.trim().replaceAll("\"", "\\\"") + "\"" : "\"" + NOT_AVAILABLE + "\"";
	}
	
	/**
	 * Adds the specified metric/attribute to the currently active transaction data if the threshold is not crossed.
	 * @param key metric/attribute identifier key
	 * @param value the metric/attrinute value
	 */
	private void addTXData(String key, String value) {
		// check to see if the threshold for number of metrics has been crossed.
		if (metricNameToValueMap.get().size() > this.getMetricsCountThreshold()) {
			// Add the data truncation attribute to the TX data - will replace if already exists
			metricNameToValueMap.get().put(LogBasedTransactionDataLogger.DATA_TRUNCATED_ATTR, LogBasedTransactionDataLogger.DATA_TRUNCATED_VALUE);
			return;
		}
		metricNameToValueMap.get().put(key, value);
	}
	
}
