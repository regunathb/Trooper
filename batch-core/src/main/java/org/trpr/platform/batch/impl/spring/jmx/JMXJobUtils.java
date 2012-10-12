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
package org.trpr.platform.batch.impl.spring.jmx;

import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularDataSupport;

import org.springframework.batch.core.BatchStatus;

/**
 * The <code> JMXJobUtils </code> is a utility class for job related actions exposed via JMX by {@link JobAdministrator}
 * 
 * @author Regunath B
 * @version 1.0, 04/10/2012
 * 
 */
public class JMXJobUtils {

	/** Array of Batch completion status strings - success and failure */
	private static final String[] BATCH_COMPLETION_STATUSES = {
		BatchStatus.ABANDONED.name(),
		BatchStatus.COMPLETED.name(),
		BatchStatus.FAILED.name(),
		BatchStatus.STOPPED.name(),
	};
	
	/** The JobAdministrator name part of MBean object name*/
	private static final String JOB_ADMINISTRATOR = "JobAdministrator";
	
	/** Job method names and param names*/
	private static final String RUN_JOB = "runJob";
	private static final String JOB_METRICS = "getIndividualJobExecutionMetrics";
	private static final String JOB_PARAM_NAME = "jobName";
	private static final String JOB_PARAM_STATUS = "jobStatus";
	
	/** The JMX objects*/
	private MBeanServer mbeanServer = null;
	private ObjectInstance jobAdministratorInstance = null;
	
	/**
	 * Constructor for this class
	 */
	public JMXJobUtils() {
		this.mbeanServer = ManagementFactory.getPlatformMBeanServer();
		Set<ObjectInstance> mbeans = mbeanServer.queryMBeans(null, null);
		for (ObjectInstance mbean : mbeans) {
			if (mbean.getObjectName().toString().indexOf(JOB_ADMINISTRATOR) > -1) {
				this.jobAdministratorInstance = mbean;
				break;
			}
		}
	}
	
	/**
	 * Runs the specified job.
	 * @param jobName the valid job name to execute
	 * @throws Exception all Runtime and Checked exceptions thrown, if any, when executing the specified job
	 */
	public void runJob(String jobName) throws Exception {
		this.mbeanServer.invoke(jobAdministratorInstance.getObjectName(), RUN_JOB, new Object[] {jobName}, new String[] {String.class.getName()});
	}
	
	/**
	 * Polls the JobAdministrator for last execution status of the specified job. Makes the current thread sleep for the specified duration between consecutive
	 * checks
	 * @param jobName the job to wait for execution completion
	 * @param checkFrequency the sleep time in ms between status checks
	 * @throws Exception all Runtime and Checked exceptions thrown, if any, when monitoring the specified job
	 * @return the exist status as String
	 */
	public String waitForJobExecution(String jobName, long checkFrequency) throws Exception {
		while(true) {
			Thread.sleep(checkFrequency);
			TabularDataSupport batchStats = (TabularDataSupport)this.mbeanServer.invoke(this.jobAdministratorInstance.getObjectName(), JOB_METRICS, null, null);
			Set<Map.Entry<Object, Object>> stats = batchStats.entrySet();
			for (Map.Entry entry : stats) {
				CompositeData data = (CompositeData)entry.getValue();
				if (data.get(JOB_PARAM_NAME).equals(jobName) && data.get(JOB_PARAM_STATUS) != null) {
					String status = data.get("jobStatus").toString(); 
					for (String completedStatus : BATCH_COMPLETION_STATUSES) {
						if (status.equalsIgnoreCase(completedStatus)) {
							// job has completed execution, so exit.
							return status;								
						}
					}
				}					
			}
		}
		
	}
	
}
