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

import java.util.Set;

import org.springframework.beans.factory.FactoryBean;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.UnmatchedListener;


/**
 * The Spring factory bean for creating the Esper CEP engine (http://esper.codehaus.org/)
 * 
 * @author Regunath B
 * @version 1.0, 03/10/2012
 * 
 */
public class EPServiceProviderFactory implements FactoryBean<EPServiceProvider>{
	
	/** The String variable in the EPL statement, to be registered with the CEP engine*/
	private String itemListVar;
	
	/** The EPL statement that needs to be registered with the CEP engine*/
	private String eplStatement;
	
	/** The Subscriber for matched events, to be registered with the CEP engine*/
	private Object matchedEventSubscriber;
	
	/** The UnmatchedListener for receiving un-matched events, to be registered with the CEP engine*/
	private UnmatchedListener unmatchedListener;

	/**
	 * Interface method implementation. Returns the Esper Engine type
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class<EPServiceProvider> getObjectType() {
		return EPServiceProvider.class;
	}

	/**
	 * Interface method implementation. Returns true
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * Interface method implementation. Creates and returns a EPServiceProvider instance
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public EPServiceProvider getObject() throws Exception {
		Configuration configuration = new Configuration();
		configuration.addVariable(this.itemListVar, Set.class, null);
		EPServiceProvider epProvider = EPServiceProviderManager.getDefaultProvider(configuration);
		EPStatement statement = epProvider.getEPAdministrator().createEPL(this.getEplStatement());
		statement.setSubscriber(this.getMatchedEventSubscriber());
		epProvider.getEPRuntime().setUnmatchedListener(this.getUnmatchedListener());
		return epProvider;
	}
	
	/** Getter/Setter methods */
	public String getEplStatement() {
		return this.eplStatement;
	}
	public void setEplStatement(String eplStatement) {
		this.eplStatement = eplStatement;
	}	
	public String getItemListVar() {
		return this.itemListVar;
	}
	public void setItemListVar(String itemListVar) {
		this.itemListVar = itemListVar;
	}	
	public Object getMatchedEventSubscriber() {
		return this.matchedEventSubscriber;
	}
	public void setMatchedEventSubscriber(Object matchedEventSubscriber) {
		this.matchedEventSubscriber = matchedEventSubscriber;
	}
	public UnmatchedListener getUnmatchedListener() {
		return this.unmatchedListener;
	}
	public void setUnmatchedListener(UnmatchedListener unmatchedListener) {
		this.unmatchedListener = unmatchedListener;
	}	
	/** End Getter/Setter methods */

}
