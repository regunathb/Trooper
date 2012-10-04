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
import org.trpr.platform.batch.impl.spring.jmx.JMXJobUtils;
import org.trpr.platform.batch.impl.spring.job.BatchJob;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.recipes.leader.LeaderLatch;


/**
 * The <code>HABatchJob</code> class is HA implementation of Batch jobs that does Leader election among all running Trooper batch nodes
 * that are hosting this job. The instance that successfully acquires the Leader Latch executes the job and relinquishes the same after successful execution.
 * Leader election uses a Zookeeper ensemble as the distributed coordination service. The Curator framework (https://github.com/Netflix/curator) is used here.
 * 
 * @author Regunath B
 * @version 1.0, 04 Oct 2012
 */
public class HABatchJob extends BatchJob {

	/**
	 * The Job params used to determine the Latch data value
	 */
	private static final String JOB_SHARD = "jobShard"; // the subset of job data that this job will process. Optional
	private static final String JOB_NAME = "jobName"; // use the job name if job shard is not defined
	
	/** The default time to wait for leader latch acquisition in ms*/
	private static final int DEFAULT_TIMEOUT_MS = 60000;
	
	/** The timeout configured for this job. Defaults to DEFAULT_TIMEOUT_SECONDS*/
	private long leaderLatchTimeout = HABatchJob.DEFAULT_TIMEOUT_MS;
	
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(HABatchJob.class);
	
	/** The Curator framework client*/
	private CuratorFramework client;

	/**
	 * Overridden super-class method. Tries to acquire a Leader Latch and executes the job only when successful. Releases the Latch after execution.
	 * @see org.trpr.platform.batch.impl.spring.job.BatchJob#execute(org.quartz.JobExecutionContext)
	 */
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String latchValue = (String)context.getJobDetail().getJobDataMap().get(JOB_SHARD);
		if (latchValue == null) {
			// use the job name instead
			latchValue = (String)context.getJobDetail().getJobDataMap().get(JOB_NAME);
		}
		LeaderLatch leaderLatch = new LeaderLatch(client,latchValue);
		try {
			leaderLatch.start();
			leaderLatch.await(this.getLeaderLatchTimeout(), TimeUnit.MILLISECONDS);
			if (leaderLatch.hasLeadership()) { // this node is the negotiated leader across all participating Trooper batch nodes hosting this job
				super.execute(context);
				// now check for execution completion - could also be error, but still complete, before continuing
				JMXJobUtils jmxJobUtils = new JMXJobUtils();
				jmxJobUtils.waitForJobExecution((String)context.getJobDetail().getJobDataMap().get(JOB_NAME), 1000);
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

	/** Start Getter/Setter methods*/
	public CuratorFramework getClient() {
		return this.client;
	}
	public void setClient(CuratorFramework client) {
		this.client = client;
	}
	public long getLeaderLatchTimeout() {
		return this.leaderLatchTimeout;
	}
	public void setLeaderLatchTimeout(long leaderLatchTimeout) {
		this.leaderLatchTimeout = leaderLatchTimeout;
	}
	/** End Getter/Setter methods*/


}
