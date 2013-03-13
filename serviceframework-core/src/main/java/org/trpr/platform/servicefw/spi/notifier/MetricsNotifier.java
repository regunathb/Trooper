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

/**
 * <code>MetricsNotifier</code> takes a set of rules and a notification receiver (@link{MetricsEventReceiver}). It has
 * methods to check rules and notify the receiver. 
 * 
 * @author devashishshankar
 * @version 1.0, 13th March, 2013
 */
public interface MetricsNotifier {

	/** Method for checking the rules and notifying the notification Receiver. */
	public void checkRules();
	
	/** Sets the rules for this MetricsNotifier */
	public void setRules(List<String> rules);
	
	/** Sets the notification receiver class */
	public void setNotificationReceivers(List<MetricsEventReceiver> runnable);
}
