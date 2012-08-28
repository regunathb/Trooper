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
package org.trpr.platform.batch.spi.spring.reader;

import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * The <code>BatchItemStreamReader</code> extends the Spring Batch {@link ItemStreamReader} to support reading of items in batches.
 * 
 * @author Regunath B
 * @version 1.0, 28 Aug 2012
 */
public interface BatchItemStreamReader<T> extends ItemStreamReader<T> {
	
	/**
	 * Reads and returns an array of data items. The size of the returned array is implementation specific.
	 * @return array of data items.
	 * @throws ParseException if there is a problem parsing the current batch
	 * (but the next one may still be valid)
	 * @throws UnexpectedInputException if there is an uncategorised problem
	 * with the input data. Assume potentially transient, so subsequent calls to
	 * read might succeed.
	 * @throws Exception if an there is a non-specific error.
	 */
	public T[] batchRead() throws Exception, UnexpectedInputException, ParseException;
}
