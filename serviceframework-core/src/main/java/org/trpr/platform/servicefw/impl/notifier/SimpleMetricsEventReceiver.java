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
package org.trpr.platform.servicefw.impl.notifier;

import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.service.model.common.statistics.ServiceStatistics;
import org.trpr.platform.servicefw.spi.notifier.MetricsEventReceiver;

/**
 * Simple implementation of {@link MetricsEventReceiver} that prints the event to Logger
 * 
 * @author devashishshankar
 * @version 1.0, 13th March, 2013
 */
public class SimpleMetricsEventReceiver implements MetricsEventReceiver {

	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(SimpleMetricsEventReceiver.class);
	
	/**
	 * Interface method implementation. Method called when a rule fails
	 */
	@Override
	public void handleMetricsEvent(String rule, ServiceStatistics serviceStatistics) {
		LOGGER.debug("RULE event received for Rule: "+rule+":"+serviceStatistics);
	}
}
