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
package org.trpr.platform.batch.impl.spring.processor;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.trpr.platform.batch.spi.spring.transformer.ItemTransformer;

/**
 * The <code>CompositeItemProcessor</code> class is an implementation of the {@link ItemProcessor} that implements the Composite design
 * pattern of executing all invoked operations on delegate ItemProcessorS. The delegates may be of type {@link ItemTransformer} that are used to
 * transform or filter out data being passed.  
 * 
 * @author Regunath B
 * @version 1.0, 29 Aug 2012
 */
public class CompositeItemProcessor <I, O> implements ItemProcessor<I, O>, InitializingBean {

	/** The list of delegates */
	private List<ItemProcessor<Object, Object>> delegates;

	/**
	 * Interface method implementation. Passes the item through the list of delegates by invoking {@link #process(Object)} on each.
	 * Filtered data i.e. return of null value by an ItemTransformer/ItemProcessor will result in stopping the chained execution.
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public O process(I item) throws Exception {
		Object result = item;
		for (ItemProcessor<Object, Object> delegate : delegates) {
			if (result == null) {
				return null;
			}
			result = delegate.process(result);
		}
		return (O) result;
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
	public List<ItemProcessor<Object, Object>> getDelegates() {
		return this.delegates;
	}
	public void setDelegates(List<ItemProcessor<Object, Object>> delegates) {
		this.delegates = delegates;
	}
	/** End getter/setter methods */	
	
}
