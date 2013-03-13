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
package org.trpr.platform.servicefw.spi.notifier;

import org.trpr.platform.service.model.common.statistics.ServiceStatistics;

/**
 * <code>MetricsEventReceiver</code> is an interface which is notified in case of 
 * a Metrics Event (i.e., a Metrics Rule failing)
 * 
 * @author devashishshankar
 * @version 1.0, 13th March, 2013
 */
public interface MetricsEventReceiver {
	
	/**
	 * The method that is called to notify
	 * @param rule String rule which has succeeded
	 * @param serviceStatistics the statistics pertaining to the rule
	 */
	public void handleEvent(String rule, ServiceStatistics serviceStatistics);
}
