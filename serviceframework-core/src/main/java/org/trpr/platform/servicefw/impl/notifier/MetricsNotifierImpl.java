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

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mvel2.CompileException;
import org.mvel2.MVEL;
import org.mvel2.PropertyAccessException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.impl.validation.ExpressionBasedValidator;
import org.trpr.platform.service.model.common.statistics.ServiceStatistics;
import org.trpr.platform.servicefw.impl.ServiceStatisticsGatherer;
import org.trpr.platform.servicefw.spi.notifier.MetricsEventReceiver;
import org.trpr.platform.servicefw.spi.notifier.MetricsNotifier;

/**
 * <code>MetricsNotifierImpl</code> is an implementation of  {@link MetricsNotifier}
 * MVEL based rules are used in this implementation.({@link http://mvel.codehaus.org/})
 * It gathers the Service metrics from {@link ServiceStatisticsGatherer} and checks for
 * mvel rules on the returned map.
 * 
 * The convention to be followed is : <service_name>_<service_version>.<property_name>, where,
 *  <property_name> is a property from {@link ServiceStatistics}
 * 
 * @author devashishshankar
 * @version 1.0, 13th March, 2013 
 */
public class MetricsNotifierImpl implements MetricsNotifier {

	/** The list of rules being checked by this instance */
	private List<String> rules;

	/** The notification receiver */
	private List<MetricsEventReceiver> receivers;

	/** {@link ServiceStatisticsGatherer} to gather the statistics */
	private ServiceStatisticsGatherer serviceStatisticsGatherer;

	/** {@link ExpressionBasedValidator} to validate MVEL expression */
	ExpressionBasedValidator expressionBasedValidator;

	/** Periods to be replaced (Periods have special meaning in MVEL) */
	public static final String PERIOD = "\\.";

	/** Replacement for a period in MVEL Expression */
	public static final String PERIOD_REPLACEMENT = "_";
	
	/** Logger instance for this class*/
	private static final Logger LOGGER = LogFactory.getLogger(MetricsNotifierImpl.class);
	
	/** Default rule to be sent to receiver if none of the rules is found */
	private static final String DEFAULT_RULE = "No rule found";

	/** Default Constructor */
	public MetricsNotifierImpl() {
		this.expressionBasedValidator = new ExpressionBasedValidator("rule failed");
		this.serviceStatisticsGatherer = new ServiceStatisticsGatherer();
	}

	/**
	 * Interface method Implementation. @see MetricsNotifier#checkRules()
	 */
	@Override
	public void checkRules() {
		Map<String, ServiceStatistics> statisticsMap = this.serviceStatisticsGatherer.getStatsAsMap();
		Map<String, ServiceStatistics> statisticsMapWithoutPeriod =new HashMap<String, ServiceStatistics>();
		//Map holds the old service name to new service name, e.g. abc_1.0 to abc_1_0
		Map<String, String> statisticsNameReplaceMap = new HashMap<String, String>();
		if(this.rules==null || this.rules.size()==0) { //If rule doesn't exist, send notification about all the services
			for(ServiceStatistics serviceStatistics: statisticsMap.values()) {
				if(this.receivers!=null) {
					for(MetricsEventReceiver receiver: this.receivers) {
						receiver.handleEvent(DEFAULT_RULE, serviceStatistics);
					}
				}
			}
			return;
		}
		//Replace period in the map keys
		for(String serviceName:statisticsMap.keySet()) {
			String newServiceName = serviceName.replaceAll(PERIOD, PERIOD_REPLACEMENT);
			statisticsMapWithoutPeriod.put(newServiceName, statisticsMap.get(serviceName));
			statisticsNameReplaceMap.put(serviceName, newServiceName);
		}
		for(String rule: this.rules) {
			//Check the rule to get the Service Name
			String ruleServiceName = null; //The serviceName accessed in the rule
			String replacedRule = null;    //Rule with correct periods replaced
			for(String originalName : statisticsNameReplaceMap.keySet()) {
				if(rule.contains(originalName)) {
					replacedRule = rule.replace(originalName, statisticsNameReplaceMap.get(originalName));
					ruleServiceName = originalName;
				}
			}
			if(ruleServiceName == null) { //No existing service found in rule
				LOGGER.warn("The rule: '"+rule+"' contains no currently deployed service");
				LOGGER.debug("The deployed services are: ");
				LOGGER.debug(statisticsMap.keySet().toString());
				continue;
			}
			ServiceStatistics currentStatistics = statisticsMap.get(ruleServiceName);  //Statistics to be sent back to reciever
			try {
				Serializable compiled = MVEL.compileExpression(replacedRule);
				boolean mvelResult = ((Boolean) MVEL.executeExpression(compiled,statisticsMapWithoutPeriod)).booleanValue();
				if(mvelResult==true) { //Rule is true
					if(this.receivers!=null) {
						for(MetricsEventReceiver receiver: this.receivers) {
							receiver.handleEvent(rule, currentStatistics);
						}
					}
				}
			}
			catch(PropertyAccessException e) {
				if(e.getCause() instanceof NullPointerException) {//Rule Failed probably due to property not found
					LOGGER.warn("Property accessed by MVEL rule not found: "+rule);
				} else {
					LOGGER.error("Property not found/not public. Please check the rule. Make sure the rule is a boolean expression: "+rule);
				}
			}
			catch(NullPointerException e) {
				LOGGER.warn("Property accessed by MVEL rule not found: "+rule);
			}
			catch (CompileException e) {
				LOGGER.error("CompileException while compiling MVEL rule: "+rule);
			}
			catch (Exception e) {
				LOGGER.error("Invalid rule/Property not found. Please check the rule.  Make sure the rule is a boolean expression: "+rule);
			}
		}
	}

	/** Getter/Setter Methods */
	public List<String> getRules() {
		return rules;
	}

	public void setRules(List<String> rules) {
		this.rules = rules;
	}

	public List<MetricsEventReceiver> getNotificationReceiver() {
		return receivers;
	}

	public void setNotificationReceivers(List<MetricsEventReceiver> receivers) {
		this.receivers = receivers;
	}
	
	public ServiceStatisticsGatherer getServiceStatisticsGatherer() {
		return serviceStatisticsGatherer;
	}

	public void setServiceStatisticsGatherer(
			ServiceStatisticsGatherer serviceStatisticsGatherer) {
		this.serviceStatisticsGatherer = serviceStatisticsGatherer;
	}
	/** End Getter/Setter Methods */
}
