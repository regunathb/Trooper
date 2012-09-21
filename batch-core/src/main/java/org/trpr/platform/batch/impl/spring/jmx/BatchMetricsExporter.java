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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.batch.admin.jmx.SimpleJobExecutionMetrics;
import org.springframework.batch.admin.jmx.SimpleStepExecutionMetrics;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.context.SmartLifecycle;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedMetric;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;
import org.springframework.jmx.export.naming.MetadataNamingStrategy;
import org.springframework.jmx.support.MetricType;
import org.springframework.util.Assert;
import org.trpr.platform.batch.impl.spring.admin.SimpleJobService;
import org.trpr.platform.core.impl.logging.LogFactory;
import org.trpr.platform.core.spi.logging.Logger;
import org.trpr.platform.core.spi.management.jmx.InstanceAwareMBean;


/**
 * The <code>BatchMetricsExporter</code> class is a utility for exporting read-only executions metrics of the various batch jobs deployed on the Trooper
 * Batch profile. Uses the {@link SimpleJobService} to get details of jobs and their executions. Uses a JMX object naming pattern bounded by
 * {@link JobAdministrator#getJMXBeanNamePrefix()} and {@link JobAdministrator#getJMXBeanNameSuffix()}
 *   
 * @author Regunath B
 * @version 1.0, 21 Sep 2012
 */

@ManagedResource(objectName = "spring.application:type=Trooper,application=Batch-Management,name=BatchExecutionMetrics-", description = "Batch Job statistics")
public class BatchMetricsExporter extends MBeanExporter implements SmartLifecycle, InstanceAwareMBean {

	/** Constant for JMX bean naming for batch*/
	private static final String BEAN_NAMING_PREFIX = "spring.application:type=Trooper,application=Batch-Management,name=BatchExecutionMetrics-";
	
	/**
	 * The Log instance for this class
	 */
	private static final Logger LOGGER = LogFactory.getLogger(BatchMetricsExporter.class);
	
	/** The auto startup indicator. Set to true */
	private volatile boolean autoStartup = true;

	/** The phase. Set as 0 i.e. start with the other components within the context  */
	private volatile int phase = 0;

	/** Indicator for running status of this LifeCycle*/
	private volatile boolean running;

	/** Lock for life-cyle use*/
	private final ReentrantLock lifecycleLock = new ReentrantLock();
	
	/** The JobAdministrator implementation for collecting all the metrics */
	private JobAdministrator jobAdministrator;
	
	/** The JMX attributes source - based on annotations*/
	private final AnnotationJmxAttributeSource attributeSource = new AnnotationJmxAttributeSource();

	/** Hashset for jobs and steps registration*/
	private Set<String> jobKeys = new HashSet<String>();	
	private Set<String> stepKeys = new HashSet<String>();
	
	/**
	 * Constructor for this class
	 */
	public BatchMetricsExporter() {
		super();
		setAutodetect(false);
		setNamingStrategy(new MetadataNamingStrategy(attributeSource));
		setAssembler(new MetadataMBeanInfoAssembler(attributeSource));
	}
	
	/**
	 * The JMX managed attribute providing Job names
	 * @return array of Job Names
	 */
	@ManagedAttribute
	public String[] getJobNames() {
		this.registerJobs(); // register job executions if not registered already
		this.registerSteps(); // register step executions
		return this.jobKeys.toArray(new String[0]);
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Failure Count")
	public int getJobExecutionFailureCount() {
		int count = 0;
		int start = 0;
		int pageSize = 100;
		Collection<JobExecution> jobExecutions;
		do {
			jobExecutions = this.jobAdministrator.getJobService().listJobExecutions(start, pageSize);
			start += pageSize;
			for (JobExecution jobExecution : jobExecutions) {
				if (jobExecution.getStatus().isUnsuccessful()) {
					count++;
				}
			}
		} while (!jobExecutions.isEmpty());
		return count;
	}

	@ManagedMetric(metricType = MetricType.COUNTER, displayName = "Job Execution Count")
	public int getJobExecutionCount() {
		return this.jobAdministrator.getJobService().countJobExecutions();
	}
	
	/**
	 * Interface method implementation. Ensures that a {@link JobAdministrator} instance is set
	 * @see org.springframework.jmx.export.MBeanExporter#afterPropertiesSet()
	 */
	public void afterPropertiesSet() {
		Assert.state(jobAdministrator != null, "A JobAdministrator must be provided");
		super.afterPropertiesSet();
	}
	
	/**
	 * Interface method implementation. 
	 * @see org.springframework.context.Lifecycle#isRunning()
	 */
	public boolean isRunning() {
		this.lifecycleLock.lock();
		try {
			return this.running;
		} finally {
			this.lifecycleLock.unlock();
		}
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.context.Lifecycle#start()
	 */
	public void start() {
		this.lifecycleLock.lock();
		try {
			if (!this.running) {
				this.registerJobs(); // register jobs with JMX
				this.registerSteps(); // register steps with JMX
				this.running = true;
			}
		} finally {
			this.lifecycleLock.unlock();
		}
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.context.Lifecycle#stop()
	 */
	public void stop() {
		this.lifecycleLock.lock();
		try {
			if (this.running) {
				this.unregisterBeans(); // unregister all the beans
				this.jobKeys.clear(); // clear the job keys
				this.stepKeys.clear(); // clear the step keys				
				this.running = false;
			}
		} finally {
			this.lifecycleLock.unlock();
		}
	}

	/**
	 * Interface method implementation. 
	 * @see org.springframework.context.Phased#getPhase()
	 */
	public int getPhase() {
		return this.phase;
	}

	/**
	 * Interface method implementation. Returns true
	 * @see org.springframework.context.SmartLifecycle#isAutoStartup()
	 */
	public boolean isAutoStartup() {
		return this.autoStartup;
	}

	/**
	 * Interface method implementation
	 * @see org.springframework.context.SmartLifecycle#stop(java.lang.Runnable)
	 */
	public void stop(Runnable callback) {
		this.lifecycleLock.lock();
		try {
			this.stop();
			callback.run();
		} finally {
			this.lifecycleLock.unlock();
		}
	}
	
	/**
	 * Interface method implementation. Returns the value of {@link JobAdministrator#getJMXBeanNameSuffix()}
	 */
	public String getMBeanNameSuffix(Object managedBean, String beanKey) {
		return this.jobAdministrator.getJMXBeanNameSuffix();
	}
	
	/** Setter/Getter methods */
	public void setJobAdministrator(JobAdministrator jobAdministrator) {
		this.jobAdministrator = jobAdministrator;
	}	
	/** End setter/getter methods */
	
	/**
	 * Overriden superclass method. Disables super class registration to avoid duplicates
	 * @see org.springframework.jmx.export.MBeanExporter#registerBeans()
	 */
	protected void registerBeans() {
		// Completely disable super class registration to avoid duplicates
	}


	/**
	 * Encode the job name into an ObjectName in the form
	 * <code>JobAdministrator#BEAN_NAMING_PREFIX,name=[jobName]</code>.
	 * @param jobName the name of the job
	 * @return a String representation of an ObjectName
	 */
	protected String getBeanKeyForJobExecution(String jobName) {
		jobName = escapeForObjectName(jobName);
		return String.format(BEAN_NAMING_PREFIX + this.jobAdministrator.getJMXBeanNameSuffix() + ",job=%s", jobName);
	}

	/**
	 * Encode the job and step name into an ObjectName in the form
	 * <code>JobAdministrator#BEAN_NAMING_PREFIX,name=[jobName],step=[stepName]</code>.
	 * @param jobName the name of the job
	 * @param stepName the name of the step
	 * @return a String representation of an ObjectName
	 */
	protected String getBeanKeyForStepExecution(String jobName, String stepName) {
		jobName = escapeForObjectName(jobName);
		stepName = escapeForObjectName(stepName);
		return String.format(BEAN_NAMING_PREFIX + this.jobAdministrator.getJMXBeanNameSuffix() + ",job=%s,step=%s", jobName, stepName);
	}	
	
	/**
	 * Helper method to escape chracters for JMX object naming
	 */
	private String escapeForObjectName(String value) {
		value = value.replaceAll(" ", "_");
		value = value.replaceAll(",", ";");
		value = value.replaceAll("=", "~");
		value = value.replaceAll(":", "@");
		value = value.replaceAll(",", ";");
		value = value.replaceAll("=", "~");
		return value;
	}

	/**
	 * Helper method to register all jobs with JMX
	 */
	private void registerJobs() {
		for (String jobName : this.jobAdministrator.getJobService().listJobs(0, Integer.MAX_VALUE)) {
			if (!this.jobKeys.contains(jobName)) {
				this.jobKeys.add(jobName);
				registerBeanNameOrInstance(new SimpleJobExecutionMetrics(this.jobAdministrator.getJobService(), jobName),
						getBeanKeyForJobExecution(jobName));
			}
		}
	}
	
	/**
	 * Helper method to register all job steps with JMX
	 */	
	private void registerSteps() {
		for (String jobName : this.jobAdministrator.getJobService().listJobs(0, Integer.MAX_VALUE)) {
			Collection<JobExecution> jobExecutions = Collections.emptySet();
			try {
				jobExecutions = this.jobAdministrator.getJobService().listJobExecutionsForJob(jobName, 0, 1);
			}
			catch (NoSuchJobException e) {
				// do-nothing
				LOGGER.error("Job listed but does not exist", e);
			}
			for (JobExecution jobExecution : jobExecutions) {
				for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
					String stepName = stepExecution.getStepName();
					String stepKey = String.format("%s/%s", jobName, stepName);
					if (!this.stepKeys.contains(stepKey)) {
						this.stepKeys.add(stepKey);
						registerBeanNameOrInstance(new SimpleStepExecutionMetrics(this.jobAdministrator.getJobService(),jobName, stepName),
								getBeanKeyForStepExecution(jobName, stepName));
					}
				}
			}
		}
	}

}
