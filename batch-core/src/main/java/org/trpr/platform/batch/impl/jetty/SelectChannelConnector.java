package org.trpr.platform.batch.impl.jetty;

import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.runtime.common.RuntimeVariables;

public class SelectChannelConnector extends
		org.mortbay.jetty.nio.SelectChannelConnector {
	
	private JobConfigurationService jobConfigurationService;
	
	public void setJobConfigService(JobConfigurationService jobConfigurationService){
		this.jobConfigurationService = jobConfigurationService;
	}
	@Override
	public void setPort(int port) {
		this.jobConfigurationService.setPort(port);
		super.setPort(port);
	}
}
