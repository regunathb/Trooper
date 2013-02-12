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
package org.trpr.platform.batch.impl.job.ha.service;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.trpr.platform.batch.impl.spring.SpringBatchComponentContainer;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.runtime.impl.event.BootstrapProgressMonitor;

import com.netflix.curator.framework.CuratorFramework;

/**
 * The <code>CuratorJobSyncHandlerFactory</code> class is a Spring factory bean for creating the CuratorJobSyncHandler 
 * Note that this implementation creates a single static instance of CuratorJobSyncHandler
 * and returns the same for subsequent calls, implying that all application
 * contexts loaded using the same class loader will share the static instance.
 * 
 * @author devashishshankar
 * @version 1.0, 7 Feb, 2013
 */
public class CuratorJobSyncHandlerFactory implements FactoryBean<CuratorJobSyncHandler> {

	/** Instance of Job Configuration service*/
	private JobConfigurationService jobConfigurationService;
	
	/** Instance of curator framework */
	private CuratorFramework curatorFramework;

	/** Bean name for {@link CuratorJobSyncHandler}*/
	public final String SYNC_HANDLER_BEAN_NAME = "curatorJobSyncHandlerDynamicBean";

	/** {@link BootstrapProgressMonitor} instance which is a constructor arg for {@link CuratorJobSyncHandler} */
	private BootstrapProgressMonitor bootstrapMonitorBean;
	
	/**
	 * Interface method implementation. 
	 * Constructs and returns as instance of {@link CuratorJobSyncHandler} for Trooper batch runtime.
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */	
	@Override
	public CuratorJobSyncHandler getObject() throws Exception {
		ApplicationContext context = SpringBatchComponentContainer.getCommonBatchBeansContext();
		if(!context.containsBean(this.SYNC_HANDLER_BEAN_NAME)) {
			DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getAutowireCapableBeanFactory();
			BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(CuratorJobSyncHandler.class.getName()).
					addConstructorArgValue(jobConfigurationService).addConstructorArgValue(curatorFramework).addConstructorArgValue(bootstrapMonitorBean);
			beanFactory.registerBeanDefinition(this.SYNC_HANDLER_BEAN_NAME, builder.getBeanDefinition());
		}
		return (CuratorJobSyncHandler) context.getBean(this.SYNC_HANDLER_BEAN_NAME);
	}
	
	/**
	 * Interface method implementation. Returns type of {@link CuratorJobSyncHandler}
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return CuratorJobSyncHandler.class;
	}
	
	/**
	 * Interface method implementation. Returns true as the instance is not just a singleton for the application context, but for all application contexts loaded
	 * by the same class loader
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	@Override
	public boolean isSingleton() {
		return true;
	}
	
	/**Getter/Setter methods */
	public JobConfigurationService getJobConfigService() {
		return jobConfigurationService;
	}
	public void setJobConfigService(
			JobConfigurationService jobConfigurationService) {
		this.jobConfigurationService = jobConfigurationService;
	}
	public CuratorFramework getCuratorClient() {
		return curatorFramework;
	}
	public void setCuratorClient(CuratorFramework curatorFramework) {
		this.curatorFramework = curatorFramework;
	}
	public BootstrapProgressMonitor getBootstrapMonitorBean() {
		return bootstrapMonitorBean;
	}
	public void setBootstrapMonitorBean(BootstrapProgressMonitor bootstrapMonitorBean) {
		this.bootstrapMonitorBean = bootstrapMonitorBean;
	}
	/**End Getter Setter*/
}
