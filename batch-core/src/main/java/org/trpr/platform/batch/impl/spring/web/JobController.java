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
package org.trpr.platform.batch.impl.spring.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.batch.admin.web.TableUtils;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.trpr.platform.batch.common.JobHost;
import org.trpr.platform.batch.common.JobInfo;
import org.trpr.platform.batch.spi.spring.admin.JobConfigurationService;
import org.trpr.platform.batch.spi.spring.admin.JobService;

/**
 * The <code>JobController</code> class is an extension of {@link JobController} that adds abilities 
 * to add extra information about trigger, such as cronexpression, next fire time, etc.
 * 
 * @author devashishshankar
 * @version 1.0, 09 Jan 2013 
 */
@Controller
public class JobController extends org.springframework.batch.admin.web.JobController {

	/** Trooper Services used by this class */
	private JobService jobService;
	private JobConfigurationService jobConfigurationService;

	/**
	 * Autowired default constructor
	 */
	@Autowired
	private JobController(JobService jobService, JobConfigurationService jobConfigurationService) {
		super(jobService);
		this.jobService = jobService;
		this.jobConfigurationService = jobConfigurationService;
	}

	/**
	 * Overridden method from @link {org.springframework.batch.admin.web.JobController}. It now uses 
	 * @link {org.trpr.platform.batch.impl.spring.web.JobInfo} to hold additional details about job
	 * (Mainly scheduler related information). This is returned and added to the model so that it
	 * can be accessed by the ftl files
	 */
	@Override
	@RequestMapping(value = {"/jobs","/configuration"}, method = RequestMethod.GET)
	public void jobs(ModelMap model, @RequestParam(defaultValue = "0") int startJob,
			@RequestParam(defaultValue = "20") int pageSize ) {
		int total = jobService.countJobs();
		TableUtils.addPagination(model, total, startJob, pageSize, "Job");
		Collection<String> names = jobService.listJobs(startJob, pageSize);
		//List of JobInfo elements to hold information to be displayed on the web console
		List<JobInfo> jobs = new ArrayList<JobInfo>();
		for (String name : names) {
			int count = 0;
			try {
				count = jobService.countJobExecutionsForJob(name);
			}
			catch (NoSuchJobException e) {
				// shouldn't happen
			}
			//Getting attributes from jobService
			boolean launchable = jobService.isLaunchable(name);
			boolean incrementable = jobService.isIncrementable(name);
			String cronExp = jobService.getCronExpression(name);
			Date nextFireDate = jobService.getNextFireDate(name);
			JobInfo jobInfo = new JobInfo(name, count, null, launchable, incrementable,cronExp,nextFireDate);
			//Getting Host attributes from jobConfigService
			List<JobHost> listOfHosts = this.jobConfigurationService.getHostNames(name);
			if(listOfHosts!=null) {
				model.addAttribute("host", "true");
				for(JobHost host: listOfHosts) {
					jobInfo.addHost(host);
				}
			}
			jobs.add(jobInfo);
		}
		//Adding the list to the model "newjobs" to be accessed in the FTL files
		model.addAttribute("newjobs", jobs);
	}	
}