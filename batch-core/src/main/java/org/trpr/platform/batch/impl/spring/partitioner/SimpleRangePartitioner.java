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
package org.trpr.platform.batch.impl.spring.partitioner;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.trpr.platform.batch.spi.spring.reader.BatchItemStreamReader;

/**
 * The <code>SimpleRangePartitioner</code> is a range based implementation of the {@link Partitioner} interface. This implementation creates as many partitions
 * as specified by the grid size and populates the {@link ExecutionContext} instances with key-values to indicate partition index and total partition size i.e.
 * grid size. Step consituents like {@link BatchItemStreamReader} may use the partition index to selectively read ranges of data for processing by {@link ItemProcessor}
 * and {@link ItemWriter}.
 * 
 * @author Regunath B
 * @version 1.0, 30 Aug 2012
 */
public class SimpleRangePartitioner implements Partitioner {

	/** The partition identification prefix */
	private static final String PARTITION_KEY = "partition";
	
	/** ExecutionContext key names */
	public static final String TOTAL_PARTITIIONS = "totalPartitions";
	public static final String PARTITION_INDEX = "partitionIndex";

	/**
	 * Interface method implementation. Creates and returns a map of ExecutionContext instances keyed by the partition key
	 * @see org.springframework.batch.core.partition.support.Partitioner#partition(int)
	 */
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> map = new HashMap<String, ExecutionContext>(gridSize);
		for (int i = 0; i < gridSize; i++) {
			ExecutionContext context = new ExecutionContext();
			context.putInt(TOTAL_PARTITIIONS, gridSize);
			context.putInt(PARTITION_INDEX, i);
			map.put(PARTITION_KEY + i, context);
		}
		return map;
	}

}
