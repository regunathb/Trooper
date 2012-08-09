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
package org.trpr.platform.runtime.impl.event;

import org.trpr.platform.core.impl.event.PlatformApplicationEvent;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.event.PlatformEventConsumer;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.model.event.PlatformEvent;
import org.trpr.platform.runtime.common.RuntimeConstants;
import org.springframework.context.ApplicationEvent;

/**
 * The <code>BootstrapProgressMonitor</code> is an implementation of the {@link PlatformEventConsumer} interface that listens and processes 
 * {@link PlatformEvent} that is of type {@link RuntimeConstants#BOOTSTRAPMONITOREDEVENT}. This class provides blocking semantics on the invoking 
 * thread if the bootstrap process is not completed. 
 * 
 * @author Regunath B
 * @version 1.0, 06/06/2012
 */

public class BootstrapProgressMonitor implements PlatformEventConsumer {

	/**
	 * The Logger instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(BootstrapProgressMonitor.class);

	/** Constants to indicate possible states of this progress monitor*/
	private static final int BOOTSTRAP_IN_PROGRESS = 0;
	private static final int BOOTSTRAP_COMPLETE = 1;

	/** Place holder for storing the current state of bootstrap progress*/
	private volatile static int bootstrapState = BOOTSTRAP_IN_PROGRESS;
	
	/**
	 * Checks the runtime state and makes the calling thread wait for bootstrap to complete.
	 */
	public void waitForBootstrapComplete() {
		while (BootstrapProgressMonitor.bootstrapState == BootstrapProgressMonitor.BOOTSTRAP_IN_PROGRESS) {
			synchronized (BootstrapProgressMonitor.class) {
				try {
					LOGGER.info("Bootstrap in progress. BootstrapProgressMonitor waiting to be notified on completion for thread : " + Thread.currentThread());
					BootstrapProgressMonitor.class.wait();
					LOGGER.info("Bootstrap completed. BootstrapProgressMonitor allowing thread to continue : " + Thread.currentThread());
				} catch (InterruptedException e) {
					// ignore this exception
				}
			}
		}
	}
	
	/**
	 * Interface method implementation. Handles ApplicationEvent only of type {@link PlatformApplicationEvent} where the source is
	 * {@link PlatformEvent} and of type {@link ServerConstants#BOOTSTRAPMONITOREDEVENT}. Wakes up any waiting threads that are awaiting
	 * bootstrap completion. Also updates the internal bootstrap progress state maintained by this class
	 * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
	 */
	public void onApplicationEvent(ApplicationEvent event) {
		if (event.getSource() instanceof PlatformEvent) {
			PlatformEvent platformEvent = (PlatformEvent) event.getSource();
			if(platformEvent.getEventType()!=null&&platformEvent.getEventType().equalsIgnoreCase(RuntimeConstants.BOOTSTRAPMONITOREDEVENT)){
				synchronized (BootstrapProgressMonitor.class) {
					if(platformEvent.getEventStatus() != null && platformEvent.getEventStatus().equalsIgnoreCase(RuntimeConstants.BOOTSTRAP_START_STATE)){
						BootstrapProgressMonitor.bootstrapState = BootstrapProgressMonitor.BOOTSTRAP_COMPLETE;
						BootstrapProgressMonitor.class.notifyAll();
					}else if(platformEvent.getEventStatus() !=null && platformEvent.getEventStatus().equalsIgnoreCase(RuntimeConstants.BOOTSTRAP_STOP_STATE)){
						BootstrapProgressMonitor.bootstrapState = BootstrapProgressMonitor.BOOTSTRAP_IN_PROGRESS;
					}
				}
				  	
			}
		}		
	}

}

