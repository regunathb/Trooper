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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.openmbean.ArrayType;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

import org.springframework.batch.admin.service.JobService;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.trpr.platform.batch.impl.spring.SpringBatchComponentContainer;
import org.trpr.platform.batch.model.statistics.jobstatistics.JobStatistics;
import org.trpr.platform.core.PlatformException;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.management.jmx.AppInstanceAwareMBean;

/**
 * The <code>JobAdministrator</code> class provides a JMX based administration interface to the batch jobs created
 * and managed by the {@link SpringBatchComponentContainer}. 
 * This class also exposes metrics collected form the various deployed and running batch jobs.
 * This class exposes the collected statistics via JMX interface and wraps the data as JMX supported types. 
 * 
 * @author Regunath B
 * @version 1.0, 28 Aug 2012
 */
@ManagedResource(objectName = "spring.application:type=Trooper,application=Batch-Management,name=JobAdministrator-", description = "Batch Job Administration Interface")
public class JobAdministrator extends AppInstanceAwareMBean {
	
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(JobAdministrator.class);
	
	/** String literal constant for unresolved host IP*/
	private static final String UNKNOWN_HOST = "Unresolved Host IP";	
	
	/** The JMX type for holding step names*/
	private static ArrayType STEP_JMX_TYPE;
	
	/** Static initialization for the step names JMX array type*/
	static {
		try {
			STEP_JMX_TYPE = new ArrayType(1, SimpleType.STRING);
		} catch (OpenDataException e) {
			// do nothing as this should never happen
		} 
	}
	
	/** Data values used in constructing the JMX CompositeData and TabularData type instances*/
	private static final String[] ATTRIBUTE_NAMES = {
		"hostIP",
		"hostStartTime",
		"jobName", 
		"jobStatus",
		"jobSteps",
		"jobStepInError",
		"jobMessage", 
		"jobStartTime", 
		"jobEndTime", 
		};
	private static final String[] ATTRIBUTE_DESCRIPTIONS = {
		"Host IP",
		"Host Start Time",
		"Job name", 
		"Job last execution status",
		"Job steps",
		"Error step in job execution",
		"Job last execution message", 
		"Job last execution start", 
		"Job last execution end", 
		};
	private static final OpenType[] ATTRIBUTE_TYPES = {
		SimpleType.STRING, 
		SimpleType.DATE, 
		SimpleType.STRING, 
		SimpleType.STRING, 
		STEP_JMX_TYPE,
		SimpleType.STRING, 
		SimpleType.STRING, 
		SimpleType.DATE, 
		SimpleType.DATE, 
		};
	private static final String[] INDEX_NAMES = {"hostIP","jobName",};
	
	private static CompositeType compositeType;
	private static TabularType tableType;
	
	/** The run ID key in job params*/
	private static String RUN_ID_KEY = "run.id";
	
	// static initializer block for composite and table type initialization
	static {
		try {
			compositeType = new CompositeType("jobStatistics", "Job statistics", ATTRIBUTE_NAMES, ATTRIBUTE_DESCRIPTIONS, ATTRIBUTE_TYPES);
			tableType = new TabularType("listOfJobStatistics", "List of Job statistics", compositeType, INDEX_NAMES);
		} catch (Exception e) {
			// ideally we should never get this error as it uses statically defined data elements
			LOGGER.error("Error initializing JMX types used in job statistics monitoring : " + e.getMessage(),e);
		}
	}	

	/** The host IP placeholder */
	private String hostIP;
	
	/** The server startup timestamp */
	private Calendar hostStartTimeStamp;
	
	/** The JobOperator instance for this job administrator*/
	private JobOperator jobOperator;
	
	/** The Job run id increment*/
	private AtomicLong jobIdIncrementer = new AtomicLong();

	/** The JobExplorer instance for this job administrator*/
	private JobExplorer jobExplorer;
	
	/** The JobService for accessing Job execution details*/
	private JobService jobService;

	/** Constructor for this class*/
	public JobAdministrator() {
		// get the host IP
		try {
			this.hostIP = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// this shouldnot happen. set the host IP to "Unresolved IP"
			this.hostIP = UNKNOWN_HOST;
		}
		this.hostStartTimeStamp = Calendar.getInstance();
	}
	
	/**
	 * Returns the JMX bean naming suffix for all batch related MBeans
	 * @return the bean naming suffix
	 */
	public String getJMXBeanNameSuffix() {
		return this.getMBeanNameSuffix(null,null); // pass nulls to get the static suffix
	}
	
	/**
	 * The JMX interface method for reading the managed attribute containing job invocation statistics.
	 * @return TabularDataSupport JMX type containing a row each of {@link JobStatistics} wrapped as JMX type CompositeData
	 */
	@ManagedAttribute
	public TabularDataSupport getIndividualJobExecutionMetrics() {
		return this.populateJobStatistics();
	}
	
	/**
	 * Runs the job identified by the specified name using the configured task executor. Errors if any and outcome of the job execution 
	 * is not available as part of the method response parameters as the task executor configured may be asynchronous. Job execution outcome can 
	 * however be determined via {@link JobAdministrator#getJobInvocationStatistics()}
	 * @see JobAdministrator#getJobInvocationStatistics() to get a list of all deployed jobs
	 * @see JobAdministrator#getJobInvocationStatistics() to get last execution status of deployed jobs
	 * @param jobName valid job name identifying the job to be executed
	 * @throws PlatformException if the specified job name does not match one of the deployed jobs in the registry or other job execution triggering errors
	 */
	@ManagedOperation
	public void runJob(String jobName) throws PlatformException {
		// check if the job name is valid from the list got from JobOperator
		String registryJobName = null;
		for (String job : this.getJobOperator().getJobNames()) {
			if (job.equalsIgnoreCase(jobName)) {
				registryJobName = job;
				break;
			}
		}
		if (registryJobName == null) {
			// job by the specified name does not exist
			final String jobDoesnotExist = "Error executing jobs. No jobs found in the registry that match : " + jobName;
			LOGGER.error(jobDoesnotExist);
			throw new PlatformException(jobDoesnotExist);
		}
		try {
			// create new job parameters with an auto-incremented run ID
			JobParameters params = new JobParameters();
			params = new JobParametersBuilder(params).addLong(RUN_ID_KEY, this.jobIdIncrementer.incrementAndGet()).toJobParameters();
			this.getJobOperator().start(registryJobName, params.toString());
		} catch (Exception e) {
			final String jobExecutionError = "Error triggering job execution: " + e.getMessage();
			LOGGER.error(jobExecutionError, e);
			throw new PlatformException(jobExecutionError,e);
		}
	}
	
	/**
	 * Wraps the job statistics into JMX types
	 */
	private TabularDataSupport populateJobStatistics() {
		// Re-creates the invocation statistics object each time
		TabularDataSupport batchInvocationStatistics = new TabularDataSupport(tableType);
		JobStatistics[] stats = getStats();
		for (JobStatistics stat : stats) {
			Object[] statValues = new Object[ATTRIBUTE_NAMES.length];
			statValues[0] = stat.getHostIP();
			statValues[1] = stat.getHostStartTimeStamp().getTime();
			statValues[2] = stat.getJobName();
			statValues[3] = stat.getJobStatus();
			statValues[4] = stat.getJobSteps().toArray(new String[0]);
			statValues[5] = stat.getJobStepInError();
			statValues[6] = stat.getJobMessage();
			statValues[7] = stat.getJobStartTimeStamp() == null ? null : stat.getJobStartTimeStamp().getTime();
			statValues[8] = stat.getJobEndTimestamp() == null ? null : stat.getJobEndTimestamp().getTime();
			CompositeData compositeData;
			try {
				compositeData = new CompositeDataSupport(compositeType, ATTRIBUTE_NAMES, statValues);
				batchInvocationStatistics.put(compositeData);
			} catch (OpenDataException e) {
				// ideally we should not get this exception
				LOGGER.error("Error constructing JMX data type from job statistics. Error is : " + e.getMessage(), e);
			}
		}
		return batchInvocationStatistics;
	}
	
	/**
	 * Returns the JobStatistics array for all jobs deployed locally
	 * @return JobStatistics array containing one instance per job deployed locally
	 */
	private JobStatistics[] getStats() {
		
		JobStatistics[] jobStatistics = new JobStatistics[this.getJobOperator().getJobNames().size()];
		int count = 0;
		for (String jobName : this.getJobOperator().getJobNames()) {
			jobStatistics[count] = new JobStatistics();
			jobStatistics[count].setHostIP(this.getHostIP());
			jobStatistics[count].setHostStartTimeStamp(this.getHostStartTimeStamp());
			jobStatistics[count].setJobName(jobName);
			// get the last run JobInstance if any
			List<JobInstance> jobInstancesList = this.getJobExplorer().getJobInstances(jobName, 0, 1); // end is set as 1 to get a single element List
			if (jobInstancesList.size() > 0) { // there is at least one job instance 
				// get the first i.e. the most recent job instance
				JobInstance jobInstance = jobInstancesList.get(0);
				// now get all successful JobExecution(s) for this JobInstance
				List<JobExecution> jobExecutionList = this.getJobExplorer().getJobExecutions(jobInstance);
				if (jobExecutionList.size() > 0) { // there is at least one job execution for the job instance
					// get the first i.e. the most recent job execution
					JobExecution jobExecution = jobExecutionList.get(0);	
					jobStatistics[count].setJobStatus(jobExecution.getStatus().name());
					if (jobExecution.getStatus() == BatchStatus.FAILED) { // try to get the exit description from the contained steps that errored out
						Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
						for (StepExecution step : stepExecutions) {	
							jobStatistics[count].getJobSteps().add(step.getStepName());
							if (step.getExitStatus().getExitCode().equals(ExitStatus.FAILED.getExitCode())) {
								jobStatistics[count].setJobStepInError(step.getStepName());
								jobStatistics[count].setJobMessage(step.getExitStatus().getExitDescription());
							}
						}
					} else {
						jobStatistics[count].setJobMessage(jobExecution.getExitStatus().getExitDescription());
					}
					if (jobExecution.getStartTime() != null) {
						Calendar jobStartTimeStamp = Calendar.getInstance();
						jobStartTimeStamp.setTime(jobExecution.getStartTime());
						jobStatistics[count].setJobStartTimeStamp(jobStartTimeStamp);
					}
					
					if (jobExecution.getEndTime() != null) {
						Calendar jobEndTimeStamp = Calendar.getInstance();
						jobEndTimeStamp.setTime(jobExecution.getEndTime());
						jobStatistics[count].setJobEndTimestamp(jobEndTimeStamp);
					}
				}
			}
			count += 1;
		}
		return jobStatistics;		
	}
	
	/** Getter setter methods*/
	public JobOperator getJobOperator() {
		return jobOperator;
	}
	public void setJobOperator(JobOperator jobOperator) {
		this.jobOperator = jobOperator;
	}	
	public JobExplorer getJobExplorer() {
		return jobExplorer;
	}
	public void setJobExplorer(JobExplorer jobExplorer) {
		this.jobExplorer = jobExplorer;
	}
	public String getHostIP() {
		return this.hostIP;
	}	
	public Calendar getHostStartTimeStamp() {
		return this.hostStartTimeStamp;
	}
	public JobService getJobService() {
		return this.jobService;
	}
	public void setJobService(JobService jobService) {
		this.jobService = jobService;
	}	
	/** End Getter setter methods*/
	
}
