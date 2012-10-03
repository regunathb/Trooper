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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.trpr.platform.impl.task.URIResource;
import org.trpr.platform.spi.task.Resource;

/**
 * The <code>CEPEngineResource</code> class is a CEP Engine {@link Resource} used in processing event stream processing in Trooper.
 * Looks up a CEP engine defined as a Spring Bean and identified by this Resource {@link Resource#getName()} method.
 * 
 * @author Regunath B
 * @version 1.0, 28/09/2012
 */
@SuppressWarnings("rawtypes")
public class CEPEngineResource<T> implements Resource<T>, ApplicationContextAware {
	
	/** Default serial version UID*/
	private static final long serialVersionUID = 1L;

	/** The name of the CEP Engine bean in the application context that created this Resource or was set on it explicitly */
	public static final String CEP_ENGINE_BEAN_NAME = "cepEngine";
	
	/** Description for this CEP Engine Resource */
	private static final String CEP_ENGINE_DESCRIPTION = "Trooper CEP Engine";
	
	/** The Spring app context that created this Resource or was set on it explicitly */
	private ApplicationContext appContext;

	/**
	 * Interface method implementation. Checks of a Spring bean by name {@link CEPEngineResource#CEP_ENGINE_BEAN_NAME} exists in the application context
	 * set on this class
	 * @see org.trpr.platform.spi.task.Resource#exists()
	 */
	public boolean exists() {
		return this.appContext.containsBean(((URIResource)this.getSerializedForm()).getSerializedForm());
	}

	/**
	 * Interface method implementation. Creates and returns a {@link URIResource}
	 * @see org.trpr.platform.spi.task.Resource#getSerializedForm()
	 */
	@SuppressWarnings({ "unchecked"})
	public T getSerializedForm() {
		return (T)new URIResource<T>(this);
	}
	
	/**
	 * Interface method implementation. Returns {@link #CEP_ENGINE_BEAN_NAME}
	 * @see org.trpr.platform.spi.task.Resource#getName()
	 */
	public String getName() {
		return CEPEngineResource.CEP_ENGINE_BEAN_NAME;
	}

	/**
	 * Interface method implementation. Return static description text contained in {@link #CEP_ENGINE_DESCRIPTION}
	 * @see org.trpr.platform.spi.task.Resource#getDescription()
	 */
	public String getDescription() {
		return CEPEngineResource.CEP_ENGINE_DESCRIPTION;
	}

	/**
	 * Interface method implementation. Returns true if the serialized forms match
	 * @see org.trpr.platform.spi.task.Resource#equals(org.trpr.platform.spi.task.Resource)
	 */
	public boolean equals(Resource<T> resource) {
		return this.getSerializedForm().equals(resource.getSerializedForm());
	}

	/**
	 * Gets the CEP Engine bean from the Spring Application context using the serialized form.
	 * @return a CEP Engine implementation
	 */
	public Object getObject() throws Exception {
		return this.appContext.getBean(((URIResource)this.getSerializedForm()).getSerializedForm());
	}
	
	/**
	 * Interface method implementation. Sets the ApplicationContext which has the CEP Engine bean
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;		
	}
	
}
