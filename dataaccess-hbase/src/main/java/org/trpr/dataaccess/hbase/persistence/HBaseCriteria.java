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
	
	/** Constants used to identify parameters set on this Criteria */
	public static final String START_TIMESTAMP = "startTimestamp";
	public static final String END_TIMESTAMP = "endTimestamp";
	public static final String VERSIONS = "numVersionsToFetch";
	public static final String START_KEY = "startKey";
	public static final String END_KEY = "endKey";
	
	/** The Scan object that may be used for highly customized and very HBase specific queries*/
	private Scan scan;

	/** Getter/Setter methods */
	public Scan getScan() {
		return this.scan;
	}
	public void setScan(Scan scan) {
		this.scan = scan;
	}
	
}
