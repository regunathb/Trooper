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
package org.trpr.example.batch.greeting.writer;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.integration.spi.messaging.MessagePublisher;

/**
 * The <code>GreetingJobWriter</code> class is a simple implementation of the Spring Batch {@link }. This implementation logs the {@link Earthling} attributes
 * and sends i.e. writes the data item to a message queue.
 * 
 * @author Regunath B
 * @version 1.0, 1 Sep 2012
 */
public class GreetingJobWriter<T extends Earthling> implements ItemWriter<Earthling> {
	
	/** The Logger interface*/
	private static final Logger LOGGER = LogFactory.getLogger(GreetingJobWriter.class);
	
	/** The MessagePublisher to publish messages to*/
	private MessagePublisher publisher;

	/**
	 * Interface method implementation. Publishes i.e. writes the output to a message queue as configured for the MessagePublisher
	 * @see org.springframework.batch.item.ItemWriter#write(java.util.List)
	 */
	public void write(List<? extends Earthling> earthlings) throws Exception {
		for (Earthling earthling : earthlings) {
			LOGGER.info("Publishing Earthling : " + earthling.getFirstName() + " " + earthling.getLastName() + "; DOB is " + earthling.getDateOfBirth().getTime());
			this.publisher.publish(earthling);
		}
	}

	/** Getter/setter methods*/
	public MessagePublisher getPublisher() {
		return this.publisher;
	}
	public void setPublisher(MessagePublisher publisher) {
		this.publisher = publisher;
	}
	/** End Getter/setter methods*/

}
