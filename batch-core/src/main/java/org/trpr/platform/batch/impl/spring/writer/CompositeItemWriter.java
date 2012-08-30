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

package org.trpr.platform.batch.impl.spring.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.trpr.platform.batch.spi.spring.writer.ItemAggregator;

/**
 * The <code>CompositeItemWriter</code> class is an implementation of the {@link ItemWriter} that implements the Composite design
 * pattern of executing all invoked operations on delegate ItemWritersS. This ItemWriter supports grouping of data items via the optional {@link ItemAggregator} implementation
 * injected to this CompositeItemWriter.
 * 
 * @author Regunath B
 * @version 1.0, 30 Aug 2012
 */
public class CompositeItemWriter<T> implements ItemWriter<T>, InitializingBean {

	/** The list of delegates */
	private List<ItemWriter<T>> delegates;
	
	/** Optional data aggregator */
	private ItemAggregator<T> aggregator;
	
	/**
	 * Interface method implementation. Checks for grouping, ordering
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	public void write(List<? extends T> data) throws Exception {
		// check to see if data needs to be aggregated
		if (this.getAggregator() != null) {
			this.getAggregator().addData((List<T>)data);
			// write out the data if the aggregator is done with aggregation/grouping
			while (this.getAggregator().hasNext()) {
				this.writeToOutput(this.getAggregator().next());
				this.getAggregator().remove();
			}
			return;
		}
		this.writeToOutput(data);
	}

	/**
	 * Interface method implementation. Ensures that the ItemProcessor delegates have been set and is not empty
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(delegates, "The 'delegates' may not be null");
		Assert.notEmpty(delegates, "The 'delegates' may not be empty");
	}

	/** Getter/setter methods */
	public List<ItemWriter<T>> getDelegates() {
		return this.delegates;
	}
	public void setDelegates(List<ItemWriter<T>> delegates) {
		this.delegates = delegates;
	}
	public ItemAggregator<T> getAggregator() {
		return this.aggregator;
	}
	public void setAggregator(ItemAggregator<T> aggregator) {
		this.aggregator = aggregator;
	}	
	/** End getter/setter methods */	
	
	/**
	 * Helper method to sort(if required) and write the results to the output
	 */
	protected void writeToOutput(List<? extends T> data) throws Exception {
		// write the data using the delegates. Sorting etc can be done by the delegates if required as entire collection of data is passed
		for (ItemWriter<? super T> writer : this.delegates) {
			writer.write(data);
		}		
	}
	
}
