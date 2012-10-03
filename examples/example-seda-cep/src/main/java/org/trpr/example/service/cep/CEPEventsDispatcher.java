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
package org.trpr.example.service.cep;

import org.mule.api.MuleException;
import org.mule.module.client.MuleClient;
import org.trpr.example.model.entity.earthling.Earthling;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UnmatchedListener;

/**
 * The <code>CEPEventsDispatcher</code> implements Esper call-back interfaces for handling matched and un-matched events
 * @author Regunath B
 * @version 1.0, 03/10/2012
 */
public class CEPEventsDispatcher implements UnmatchedListener {
	
	/** Logger variable */
	private static final Logger LOGGER = LogFactory.getLogger(CEPEventsDispatcher.class);
	
	/** The MuleClient for dispatching events to appropriate end-points*/
	private MuleClient client = null;
	
	/**
	 * Constructor for this class
	 */
	public CEPEventsDispatcher () {
		try {
			this.client = new MuleClient(false);
		} catch (MuleException e) {
			LOGGER.error("Errror instantiating CEPEventsDispatcher : " + e.getMessage(), e);
		}
	}
	
	/**
	 * Called by the Esper engine for matched events
	 * @param earthling the Earthling instance that was a match in CEP processing
	 */
	public void update(Earthling earthling) {
		System.out.println("Handling MATCHED EVENT for : " + earthling.getLastName());
		try {
			this.client.send("amqp://myexchange:direct/?queue=echoMatchInput", earthling, null);
		} catch (MuleException e) {
			LOGGER.error("Errror dispatching matched event : " + e.getMessage(), e);
		}
	}

	/**
	 * Interface method implementation. Dispatches the 
	 * @see com.espertech.esper.client.UnmatchedListener#update(com.espertech.esper.client.EventBean)
	 */
	public void update(EventBean event) {
		/*
		System.out.println("Handling UN-MATCHED EVENT for : " + ((Earthling)event.getUnderlying()).getLastName());
		try {
			this.client.send("amqp://myexchange:direct/?queue=echoNonMatchInput", event.getUnderlying(), null);
		} catch (MuleException e) {
			LOGGER.error("Errror dispatching un-matched event : " + e.getMessage(), e);
		}
		*/
	}
	
}
