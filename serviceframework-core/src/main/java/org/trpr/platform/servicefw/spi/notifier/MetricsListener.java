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

import java.util.List;

import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * <code>MetricsListener</code> is a class that takes a list of {@link MetricsEvaluator} 
 * and calls their notifiers at specified intervals.
 * 
 * @author devashishshankar
 * @version 1.0, 13th March, 2013
 */
public interface MetricsListener extends Runnable {
	
	/** Interface method implementation. This method checks the rules in all MetricsEvaluators */
	@Override
	public void run();
	
	/** Sets the list of {@link MetricsEvaluator} objects */
	public void setMetricsEvaluators(List <MetricsEvaluator> metricsEvaluators);
	
	/** The task scheduler to be used by this class */
	public void setTimer(ConcurrentTaskScheduler timer);
	
	/** The time delay (in ms) after which the listener should activate */
	public void setDelay(int time);
}
