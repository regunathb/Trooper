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

import java.util.List;

import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.trpr.platform.servicefw.spi.notifier.MetricsEvaluator;
import org.trpr.platform.servicefw.spi.notifier.MetricsListener;

/**
 * Implementation of {@link MetricsListener}. Uses {@link ConcurrentTaskScheduler} to
 * call {@link MetricsEvaluator} at specified time intervals
 * 
 * @author devashishshankar
 * @version 1.0, 13th March, 2013
 */
public class MetricsListenerImpl implements MetricsListener {

	/** The list of metricsEvaluators for this class */
	private List<MetricsEvaluator> metricsEvaluators;
	
	/** The timer instance for this class */
	private ConcurrentTaskScheduler timer;
	
	/** The default delay (in ms) after which the notifier is executed */
	private static final int DEFAULT_DELAY = 10000;

	/** The delay (in ms) after which the notifier is executed */
	private int delay = DEFAULT_DELAY;

	/** Interface method implementation. @see MetricsListener#run() */
	@Override
	public void run() {
		for(MetricsEvaluator metricsEvaluator : this.metricsEvaluators) {
			metricsEvaluator.checkRules();
		}
	}
	
	/** Getter/Setter methods */	
	@Override
	public void setMetricsEvaluators(List<MetricsEvaluator> metricsEvaluators) {
		this.metricsEvaluators = metricsEvaluators;
		
	}
	public List<MetricsEvaluator> getMetricsEvaluators() {
		return this.metricsEvaluators;
	}
	@Override
	public void setTimer(ConcurrentTaskScheduler timer) {
		this.timer = timer;
		this.timer.scheduleAtFixedRate(this, 10000);
	}
	@Override
	public void setDelay(int time) {
		this.delay = time;		
	}
	public int getDelay() {
		return delay;
	}
	/** End Getter/Setter methods */	
}
