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
package org.trpr.dataaccess.hbase.persistence;

import org.apache.hadoop.hbase.client.Scan;
import org.trpr.platform.core.spi.persistence.Criteria;

/**
 * This class is used to specify various criteria while reading records from
 * <code>HBase</code>
 * 
 * @shashikant soni
 * 
 * 
 */
public class HBaseCriteria extends Criteria {

	/**
	 * The number of version to fetch while searching for records
	 */
	private int numVersionsToFetch = 1;

	private long startTimestamp;
	
	private long endTimestamp;

	private int numRecordsToFetch;

	private Scan scan;

	public int getNumVersionsToFetch() {
		return numVersionsToFetch;
	}

	/**
	 * Set the value for number of records to fetch.
	 * 
	 * @param numVersion
	 */
	public void setNumVersionsToFetch(int numVersion) {
		this.numVersionsToFetch = numVersion;
	}

	public long getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * Set the start time range to fetch records. endTimestamp will use the
	 * current system time value while searching records
	 * 
	 * @param startTimestamp
	 *            Only columns that are updated after this timestamp will be
	 *            queried for
	 */
	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	public long getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * Set the end time range to fetch records.
	 * 
	 * @param endTimestamp
	 *            Only columns that are updated before this timestamp will be
	 *            queried for
	 */
	public void setEndTimestamp(long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Set the start and end time range to fetch records.
	 * 
	 * @param endTimestamp
	 *            can be null. If it is null, then the current system time will
	 *            be used as the value
	 * @param startTimestamp
	 *            is the mandatory field, which is the starting timestamp value
	 */
	public void setTimestampRange(long endTimestamp, long startTimestamp) {
		this.endTimestamp = endTimestamp;
		this.startTimestamp = startTimestamp;
	}

	/**
	 * Returns the embedded HBase Scan object
	 * 
	 * @return Scan instance
	 */
	public Scan getScan() {
		return scan;
	}

	/**
	 * Specify the HBase Scan class for finer control on your query
	 * 
	 * @param scan
	 *            Scan object that specifies query condition
	 */
	public void setScan(Scan scan) {
		this.scan = scan;
	}

	/**
	 * Number of records that will be returned in case of scan queries.
	 * 
	 * @return Value of attribute
	 */
	public int getNumRecordsToFetch() {
		return numRecordsToFetch;
	}

	/**
	 * Specify how many records should be retrieved. This is useful only in case
	 * of Scans.
	 * 
	 * Since HBasehandler will hold all the matching rows in memory, this gives
	 * an option to clients to restrict the number of entries returned by the
	 * underlying API. If you need more flexibility, consider using raw HBase
	 * API.
	 * 
	 * Value of 0 or negative will mean that all records will be returned.
	 */
	public void setNumRecordsToFetch(int numRecordsToFetch) {
		this.numRecordsToFetch = numRecordsToFetch;
	}
}
