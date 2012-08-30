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
package org.trpr.platform.batch.spi.spring.writer;

import java.util.Iterator;
import java.util.List;

/**
 * The <code>ItemAggregator</code> interface provides methods for in-memory(usually) aggregation/grouping of data during the write phase of a batch operation.
 * Extends the {@link Iterator} interface to indicate availability of data for writing post aggregation/grouping.
 * 
 * @author Regunath B
 * @version 1.0, 30 Aug 2012
 */
public interface ItemAggregator<T> extends Iterator<List<T>> {

	/**
	 * Adds data to this data aggregator
	 * @param data List containing data to be aggregated or grouped
	 */
	public void addData(List<T> data);
	
}
