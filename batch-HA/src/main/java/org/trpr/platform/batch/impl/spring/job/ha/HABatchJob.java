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
package org.trpr.platform.batch.impl.spring.job.ha;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.job.flow.FlowJob;
import org.trpr.platform.batch.impl.spring.jmx.JMXJobUtils;
import org.trpr.platform.batch.impl.spring.job.BatchJob;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.leader.LeaderLatch;


/**
 * The <code>HABatchJob</code> class is a HA implementation of Batch jobs that does Leader election among all running Trooper batch nodes
 * that are hosting this job. The instance that successfully acquires the Leader Latch executes the job and relinquishes the same after successful execution.
 * Leader election uses a Zookeeper ensemble as the distributed coordination service. The Curator framework (https://github.com/Netflix/curator) is used here.
 * 
 * @author Regunath B
 * @version 1.0, 04 Oct 2012
 */
public class HABatchJob extends BatchJob {

	/**
	 * The Job params used to determine the Latch data value, timeout and Curator client
	 */
	private static final String JOB_SHARD = "jobShard"; // the subset of job data that this job will process. Optional
	private static final String JOB_NAME = "jobName"; // use the job name if job shard is not defined
	private static final String LEADER_LATCH_TIMEOUT = "leaderLatchTimeout"; // optional
	private static final String CURATOR_CLIENT = "curatorClient";
	
	/** The default time to wait for leader latch acquisition in ms*/
	private static final int DEFAULT_TIMEOUT_MS = 60000;
	
	/** The Zookeeper path prefix for job latch creation*/
	private static final String ZK_PATH_PREFIX = "/Batch/Latch/";
	
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(HABatchJob.class);
	
	/**
	 * Overridden super-class method. Tries to acquire a Leader Latch and executes the job only when successful. Releases the Latch after execution.
	 * @see org.trpr.platform.batch.impl.spring.job.BatchJob#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String latchValue = (String)context.getJobDetail().getJobDataMap().get(JOB_SHARD);
		if (latchValue == null) {
			// use the job name instead
			latchValue = ((FlowJob)context.getJobDetail().getJobDataMap().get(JOB_NAME)).getName();
		}
		LeaderLatch leaderLatch = new LeaderLatch((CuratorFramework)context.getJobDetail().getJobDataMap().get(CURATOR_CLIENT),ZK_PATH_PREFIX + latchValue);
		try {
			leaderLatch.start();
			long leaderLatchTimeout = context.getJobDetail().getJobDataMap().get(LEADER_LATCH_TIMEOUT) == null ? DEFAULT_TIMEOUT_MS :
				Long.valueOf((String)context.getJobDetail().getJobDataMap().get(LEADER_LATCH_TIMEOUT));
			
			leaderLatch.await(leaderLatchTimeout, TimeUnit.MILLISECONDS);
			if (leaderLatch.hasLeadership()) { // this node is the negotiated leader across all participating Trooper batch nodes hosting this job
				super.execute(context);
				// now check for execution completion - could also be error, but still complete, before continuing
				JMXJobUtils jmxJobUtils = new JMXJobUtils();
				jmxJobUtils.waitForJobExecution(((FlowJob)context.getJobDetail().getJobDataMap().get(JOB_NAME)).getName(), 1000);
			} else {
				LOGGER.info("Not the negotiated leader and therefore not executing job : " + latchValue);
			}
		} catch (Exception e) {
			LOGGER.error("Error acquiring Leader Latch for : " + latchValue + ". Cannot execute job!. Error is : " + e.getMessage(), e);
		} finally {
			if (leaderLatch != null) {
				try {
					leaderLatch.close();
				} catch (IOException e) {
					LOGGER.error("Error relinquishing/closing Leader Latch for : " + latchValue + ". Error is : " + e.getMessage(), e);
				}
			}
		}
	}

}
