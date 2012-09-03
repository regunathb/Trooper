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
package org.trpr.example.batch.greeting.reader;

import java.util.Calendar;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.batch.common.BatchException;
import org.trpr.platform.batch.impl.spring.partitioner.SimpleRangePartitioner;
import org.trpr.platform.batch.impl.spring.reader.CompositeItemStreamReader;
import org.trpr.platform.batch.spi.spring.reader.BatchItemStreamReader;


/**
 * The <code>GreetingJobReader</code> class is a simple implementation of the {@link BatchItemStreamReader} that returns the sample data item {@link Earthling} instances
 * from memory. This reader may be suitably modified to read data from a data source such as DB, file system etc.
 * 
 * @author Regunath B
 * @version 1.0, 1 Sep 2012
 */
public class GreetingJobReader<T extends Earthling> implements BatchItemStreamReader<Earthling> {
	
	/** The read batch items size */
	private int batchSize;
	
	/** The partition index, if any*/
	private int partitionIndex = -1;
	
	/** Indicator to signal read complete*/
	private boolean isReadComplete;

	/**
	 * Interface method implementation. Throws an exception suggesting to use the {@link #batchRead()} method instead via the {@link CompositeItemStreamReader} instead
	 * @see org.springframework.batch.item.ItemReader#read()
	 */
	public Earthling read() throws Exception, UnexpectedInputException, ParseException  {
		throw new BatchException("Operation is not supported! Use the CompositeItemStreamReader#read() method instead.");
	}

	/**
	 * Interface method implementation. Simply creates and returns an array of Earthling instances of total size defined by the READ_BATCH static variable value
	 * @see org.trpr.platform.batch.spi.spring.reader.BatchItemStreamReader#batchRead()
	 */
	public Earthling[] batchRead() throws Exception, UnexpectedInputException, ParseException {
		if (this.isReadComplete) { // no more data to read
			this.isReadComplete = false; // set to false for next read when scheduled by the job trigger
			return null;
		}
		Earthling[] earthlings = new Earthling[this.getBatchSize()];
		for (int i=0; i<this.getBatchSize();i++) {
			earthlings[i] = new Earthling();
			earthlings[i].setFirstName("Mr");
			earthlings[i].setLastName("Trooper " + this.partitionIndex);
			Calendar c  = Calendar.getInstance();
			c.set(Calendar.YEAR, 2010);
			earthlings[i].setDateOfBirth(c);	
		}
		this.isReadComplete = true; // signal that this reader does not have any more data
		return earthlings;
	}
	
	/**
	 * Interface method implementation. Does nothing
	 * @see org.springframework.batch.item.ItemStream#close()
	 */
	public void close() throws ItemStreamException {
		// no op		
	}

	/**
	 * Interface method implementation. Stores the partition index, if any, to be appended to the data item
	 * @see SimpleRangePartitioner#TOTAL_PARTITIIONS, {@link SimpleRangePartitioner#PARTITION_INDEX}
	 * @see org.springframework.batch.item.ItemStream#open(org.springframework.batch.item.ExecutionContext)
	 */
	public void open(ExecutionContext context) throws ItemStreamException {
		this.partitionIndex = context.getInt(SimpleRangePartitioner.PARTITION_INDEX, -1);
	}

	/**
	 * Interface method implementation. Does nothing
	 * @see org.springframework.batch.item.ItemStream#update(org.springframework.batch.item.ExecutionContext)
	 */
	public void update(ExecutionContext arg0) throws ItemStreamException {
		// no op		
	}


	/** Getter/setter methods*/
	public int getBatchSize() {
		return this.batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	/** End Getter/setter methods*/
	
}
