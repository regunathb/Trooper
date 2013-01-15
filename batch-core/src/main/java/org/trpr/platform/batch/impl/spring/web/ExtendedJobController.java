package org.trpr.platform.batch.impl.spring.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.quartz.Scheduler;
import org.springframework.batch.admin.web.JobController;
import org.springframework.batch.admin.web.TableUtils;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.trpr.platform.batch.impl.spring.SpringBatchComponentContainer;
import org.trpr.platform.batch.spi.spring.admin.JobService;


/**
 * The <code>ExtendedJobController</code> class is an extension of {@link JobController} that adds abilities to add extra information about trigger, such as cronexpression, next fire time, etc.
 * 
 * @author devashishshankar
 * @version 1.0, 09 Jan 2013
 */

@Controller
public class ExtendedJobController extends JobController {

	private JobService jobService;
	
	private Collection<String> extensions = new HashSet<String>();


	@Autowired
	private ExtendedJobController(JobService jobService) 
	{
		super(jobService);
		this.jobService = jobService;
		System.out.println("Typecast success");

	}

	
	@Override
	@ModelAttribute("jobName")
	public String getJobName(HttpServletRequest request) {
			
		
		String path = request.getPathInfo();
		int index = path.lastIndexOf("jobs/") + 5;
		if (index >= 0) {
			path = path.substring(index);
		}
		if (!path.contains(".")) {
			return path;
		}
		for (String extension : extensions) {
			if (path.endsWith(extension)) {
				path = StringUtils.stripFilenameExtension(path);
				// Only remove one extension so a job can be called job.html and
				// still be addressed
				break;
			}
		}
		return path;
	}
	
	
	@Override
	@RequestMapping(value = "/jobs", method = RequestMethod.GET)
	public void jobs(ModelMap model, @RequestParam(defaultValue = "0") int startJob,
			@RequestParam(defaultValue = "20") int pageSize) {
		int total = jobService.countJobs();
		TableUtils.addPagination(model, total, startJob, pageSize, "Job");
		Collection<String> names = jobService.listJobs(startJob, pageSize);
		List<ExtendedJobInfo> jobs = new ArrayList<ExtendedJobInfo>();
		
		
		for (String name : names) {
			int count = 0;
			try {
				count = jobService.countJobExecutionsForJob(name);
			}
			catch (NoSuchJobException e) {
				// shouldn't happen
			}
			boolean launchable = jobService.isLaunchable(name);
			boolean incrementable = jobService.isIncrementable(name);
			String cronExp = jobService.getCronExpression(name);
			Date nextFireDate = jobService.getNextFireDate(name);

			//System.out.println("ExtendedJobController.java#jobs: In here!!"+cronExp+nextFireDate);
			jobs.add(new ExtendedJobInfo(name, count, null, launchable, incrementable,cronExp,nextFireDate));
		}
		//System.out.println("ExtendedJobController.java#jobs: works!!");
		model.addAttribute("newjobs", jobs);
	}
	
	
}
