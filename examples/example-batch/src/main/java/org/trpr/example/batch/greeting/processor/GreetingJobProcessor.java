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
package org.trpr.example.batch.greeting.processor;

import org.springframework.batch.item.ItemProcessor;
import org.trpr.example.model.entity.earthling.Earthling;

/**
 * The <code>GreetingJobProcessor</code> class is a simple implementation of the Spring Batch {@link ItemProcessor}. This implementation modifies
 * the {@link Earthling} data item's first name to demonstrate data processing
 * 
 * @author Regunath B
 * @version 1.0, 1 Sep 2012
 */
public class GreetingJobProcessor<I extends Earthling, O extends Earthling> implements ItemProcessor<Earthling,Earthling> {

	/**
	 * Interface method implementation. Changes the gender in the first name of the specified Earthling object
	 * @see org.springframework.batch.item.ItemProcessor#process(java.lang.Object)
	 */
	public Earthling process(Earthling earthling) throws Exception {
		earthling.setFirstName(earthling.getFirstName().equalsIgnoreCase("Mr") ? "Mrs": "Mr");
		return earthling;
	}

}
