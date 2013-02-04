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

package org.trpr.platform.batch.impl.spring.reader;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.trpr.platform.batch.impl.spring.partitioner.SimpleRangePartitioner;
import org.trpr.platform.batch.spi.spring.reader.BatchItemStreamReader;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

/**
 * The <code>CompositeItemStreamReader</code> class is an implementation of the {@link BatchItemStreamReader} that implements the Composite design
 * pattern of delegating all operations to an BatchItemStreamReader and caching the read data in a bounded collection. This implementation may be used when the 
 * delegate BatchItemStreamReader does not inherently support data reading as streams. The size of the bounded collection is determined by the delegate
 * {@link BatchItemStreamReader#batchRead()} return data size.
 * 
 * @author Regunath B
 * @version 1.0, 28 Aug 2012
 */
public class CompositeItemStreamReader<T> implements BatchItemStreamReader<T>, InitializingBean {
	
	/** The default timeout in seconds that applies to any BatchItemStreamReader#batchRead() call on the delegate*/
	private static final int DEFAULT_BATCH_READ_TIMEOUT = 60;
	
	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(CompositeItemStreamReader.class);
	
	/** The delegate that does the actual data reading*/
	private BatchItemStreamReader<T> delegate;
	
	/** The local list containing data items*/
	private Queue<T> localQueue = new LinkedList<T>(); 
	
	/** The Collection of ExecutionContext instances that determines data to be read */
	private Queue<ExecutionContext> contextList = new LinkedList<ExecutionContext>();	
	
	/** The CountDownLatch to keep track of ExecutionContext instances that are processed*/
	private CountDownLatch countDownLatch;
	
	/** The timeout for batch read calls on the delegate*/
	private int batchReadTimeout = DEFAULT_BATCH_READ_TIMEOUT;
	
	/**
	 * Constructor for this class
	 * @param delegate the ItemStreamReader delegate
	 */
	public CompositeItemStreamReader(BatchItemStreamReader<T> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Interface method implementation. Returns data from the collection. Reads data from the delegate if the bounded collection is empty and
	 * populates the local collection.
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	public T read() throws Exception, UnexpectedInputException, ParseException {		
		// return data from local queue if available already
		LOGGER.info("Queue size is : " + this.localQueue.size());
		synchronized(this) { // include the check for empty and remove in one synchronized block to avoid race conditions
			if (!this.localQueue.isEmpty()) {
				return this.localQueue.remove();
			}			
		}
		
		LOGGER.info("No items found. To batch read by partitions");
		
		ExecutionContext context = null;
		// else, check to see if any of the ExecutionContext(s) exist for processing
		synchronized(this) { // include the check for empty and remove in one synchronized block to avoid race conditions
			if (!this.contextList.isEmpty()) {
				context = this.contextList.remove();
			}
		}
		
		if (context != null) {
			LOGGER.info("Calling batch read for a partition");
			T[] items = this.delegate.batchRead(context); // DONOT have the delegate's batchRead() inside the below synchronized block. All readers will block then
			synchronized(this) { // include the add and remove operations in one synchronized block to avoid race conditions			
				for (T item : items) {
					this.localQueue.add(item);
				}
				if (this.countDownLatch != null) {
					this.countDownLatch.countDown(); // count down on the latch
				}
				return this.localQueue.remove(); // return an item for processing after populating the local collection
			}
		}
		synchronized(this) {
			if (this.countDownLatch != null) {
				this.countDownLatch.await(this.getBatchReadTimeout(), TimeUnit.SECONDS); // wait for any batch reads on the delegate to complete		
				// force clear the context list and set the count down latch to null. Enables clean start the next time the job the run
				if (this.countDownLatch.getCount() > 0) {
					LOGGER.info("Count down latch timeout occurred!");
				}
			}
			this.countDownLatch = null;
			this.contextList.clear();
		}
		
		// Check again to see if any new items have been added, exit otherwise
		synchronized(this) {
			if (!this.localQueue.isEmpty()) { // include the check for empty and remove in one synchronized block to avoid race conditions
				return this.localQueue.remove();
			}	
		}
		
		LOGGER.info("Size of queue before exit : " + this.localQueue.size());
		return null;
	}

	/**
	 * Interface method implementation. Throws {@link UnsupportedOperationException} as this composite reader's method should never be called.
	 * @see BatchItemStreamReader#batchRead()
	 */
	public T[] batchRead(ExecutionContext context) throws Exception, UnexpectedInputException, ParseException {
		throw new UnsupportedOperationException("Illegal invocation of batchRead(), call read() instead.");
	}

	
	/**
	 * Interface method implementation. Calls the namesake method on the delegate
	 * @see org.springframework.batch.item.ItemStream#close()
	 */
	public void close() throws ItemStreamException {
		this.delegate.close();
	}

	/**
	 * Interface method implementation. Stores the passed-in ExecutionContext in a ThreadLocal for use in {@link #batchRead()} method
	 * @see org.springframework.batch.item.ItemStream#open(org.springframework.batch.item.ExecutionContext)
	 */
	public void open(ExecutionContext context) throws ItemStreamException {
		LOGGER.info("Called open for a partition");
		this.contextList.add(context);
		if (this.countDownLatch == null || this.countDownLatch.getCount() == 0) { // create a CountDownLatch if variable instance is null or the previous one has counted down to zero
			this.countDownLatch = new CountDownLatch(context.getInt(SimpleRangePartitioner.TOTAL_PARTITIIONS, 1)); // initialize the CountDownLatch to the partition size
		}
		// dont call open() on the delegate. We will pass on the ExecutionContext as part of batchRead() instead
	}

	/**
	 * Interface method implementation. Calls the namesake method on the delegate
	 * @see org.springframework.batch.item.ItemStream#update(org.springframework.batch.item.ExecutionContext)
	 */
	public void update(ExecutionContext context) throws ItemStreamException {
		this.delegate.update(context);
	}
	
	/**
	 * Interface method implementation. Ensures that the BatchItemStreamReader delegate has been set and is not null
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(delegate, "The 'delegate' may not be null");
	}	
	
	/** Getter/setter methods */
	public BatchItemStreamReader<T> getDelegate() {
		return this.delegate;
	}
	public int getBatchReadTimeout() {
		return this.batchReadTimeout;
	}
	public void setBatchReadTimeout(int batchReadTimeout) {
		this.batchReadTimeout = batchReadTimeout;
	}	
	/** End getter/setter methods */


}