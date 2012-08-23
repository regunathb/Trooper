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

package org.trpr.platform.seda.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.trpr.platform.core.impl.persistence.PersistenceManagerProvider;
import org.trpr.platform.core.spi.persistence.PersistenceManager;
import org.trpr.platform.core.spi.persistence.PersistentEntity;
import org.trpr.platform.seda.api.checkpointing.StageExecutionEvaluator;
import org.trpr.platform.service.model.common.platformservicerequest.PlatformServiceRequest;
import org.trpr.platform.service.model.common.platformserviceresponse.PlatformServiceResponse;
import org.trpr.platform.servicefw.ServiceContext;
import org.trpr.platform.servicefw.spi.ServiceContainer;
import org.trpr.platform.servicefw.spi.ServiceRequest;
import org.trpr.platform.servicefw.spi.ServiceResponse;

/**
 * The <code>SedaStageCheckpointingInterceptor</code> class is an implementation of the Spring {@link org.aopalliance.intercept.MethodInterceptor} interface that
 * when configured with a Spring AOP alliance proxy creator (such as {@link org.springframework.aop.framework.autoproxy.BeanNameAutoProxyCreator}), 
 * uses the configured {@link StageExecutionEvaluator} to evaluate the service execution results.
 * The service request-response are evaluated only for successful executions and the returned check-point data is persisted using the configured
 * {@link PersistenceManagerProvider} only if the active {@link ServiceContainer}
 * requires checkpointing.
 * 
 * @author Regunath B
 * @version 1.0, 23/08/2012
 */
public class SedaStageCheckpointingInterceptor<T,S,P extends PlatformServiceRequest,R extends PlatformServiceResponse> implements MethodInterceptor {
	
	/** The StageExecutionEvaluator to use for getting stage execution check-point data*/
	private StageExecutionEvaluator<T,S> stageExecutionEvaluator;
	
	/** The ServceContext for access to the service container*/
	private ServiceContext<T,S,P,R> serviceContext;
	
	/** The PersistenceManagerProvider to use for persisting the stage execution check-point data*/
	private PersistenceManager persistenceManager;

	/**
	 * Interface method implementation. Persists check-point data if the currently active ServiceContainer requires check-pointing
	 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
	 */
	@SuppressWarnings("unchecked")
	public Object invoke(MethodInvocation invocation) throws Throwable {
		boolean canProceed = true;
		Object returnValue = null;
		
		if (this.serviceContext.doesContainerSupportCheckpointing()) {
			Object arguments[] = invocation.getArguments();
			canProceed = this.stageExecutionEvaluator.canProceed((ServiceRequest<T>) arguments[0]);
		}
		
		if (canProceed) {
			returnValue = invocation.proceed();
			// check-point only if the currently active ServiceContainer requires it
			if (this.serviceContext.doesContainerSupportCheckpointing()) {
				Object arguments[] = invocation.getArguments();
				PersistentEntity[] entities = this.stageExecutionEvaluator.evaluateStageExecutionResponse(
						(ServiceRequest<T>) arguments[0], (ServiceResponse<S>) returnValue);
				if (null != entities) {
					this.persistenceManager.makePersistent(entities);
				}
			}
		}
		return returnValue;
	}
	
	/** Java bean style setter-getter methods*/
	public StageExecutionEvaluator<T,S> getStageExecutionEvaluator() {
		return this.stageExecutionEvaluator;
	}
	public void setStageExecutionEvaluator(StageExecutionEvaluator<T,S> stageExecutionEvaluator) {
		this.stageExecutionEvaluator = stageExecutionEvaluator;
	}
	public PersistenceManager getPersistenceManager() {
		return this.persistenceManager;
	}
	public ServiceContext<T,S,P,R> getServiceContext() {
		return this.serviceContext;
	}
	public void setServiceContext(ServiceContext<T,S,P,R> serviceContext) {
		this.serviceContext = serviceContext;
	}
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}
	/** End Java bean style setter-getter methods*/
	
}
