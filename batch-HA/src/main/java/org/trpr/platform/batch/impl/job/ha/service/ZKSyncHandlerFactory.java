package org.trpr.platform.batch.impl.job.ha.service;

import org.springframework.beans.factory.FactoryBean;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;

import com.netflix.curator.framework.CuratorFramework;

/**
 *  * The <code>ZKSyncHandlerFactory</code> class is a Spring factory bean for creating the ZKSyncHandler 
 * Note that this implementation creates a single static instance of ZKSyncHandler
 *  and returns the same for subsequent calls, implying that all application
 * contexts loaded using the same class loader will share the static instance.
 * 
 * @author devashishshankar
 * @version 1.0, 7 Feb, 2013
 */
public class ZKSyncHandlerFactory implements FactoryBean<ZKSyncHandler> {

	/** Instance of Job Configuration service*/
	private JobConfigurationService jobConfigurationService;
	
	/** Instance of curator framework */
	private CuratorFramework curatorFramework;

	/** The static instance of the {@link ZKSyncHandler}*/
	private static ZKSyncHandler zkSyncHandler;
	
	/**
	 * Interface method implementation. 
	 * Constructs and returns as instance of {@link ZKSyncHandler} for Trooper batch runtime.
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */	
	@Override
	public ZKSyncHandler getObject() throws Exception {
		if(zkSyncHandler==null) {
			ZKSyncHandlerFactory.zkSyncHandler = new ZKSyncHandler(jobConfigurationService, curatorFramework);
		}
		return ZKSyncHandlerFactory.zkSyncHandler;
	}
	
	/**
	 * Interface method implementation. Returns type of {@link ZKSyncHandler}
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@Override
	public Class<?> getObjectType() {
		return ZKSyncHandler.class;
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
	/**End Getter Setter*/
}
