<#import "/spring.ftl" as spring />
<div id="jobs">
	
	<#if newjobs?? && newjobs?size!=0>
		
			<h2>Job Names Registered</h2>
	
			<table title="Jobs Names" class="bordered-table">
				<tr>
					<th>Name! changed</th>
					<th>Description</th>
					<th>Execution&nbsp;Count</th>
					<th>Launchable</th>
					<th>Incrementable</th>
					<th>Cron Expression </th>
					<th>Next Fire Time </th>
				</tr>
				<#list newjobs as job>
					<#if job_index % 2 == 0>
						<#assign rowClass="name-sublevel1-even"/>
					<#else>
						<#assign rowClass="name-sublevel1-odd"/>
					</#if>
					<tr class="${rowClass}">
						<#assign job_url><@spring.url relativeUrl="${servletPath}/jobs/${job.name}"/></#assign>
						<td><a href="${job_url}">${job.name}</a></td>
						<td><@spring.messageText code="${job.name}.description" text="No description"/></td>
						<td>${job.executionCount}</td>
						<td><#if job.launchable??>${job.launchable?string}<#else>?</#if></td>
						<td><#if job.incrementable??>${job.incrementable?string}<#else>?</#if></td>
						<td>${job.cronExpression}</td>
						<td>${job.nextFireTime}</td> 
					</tr>
				</#list>
			</table>
			<ul class="controlLinks">
				<li>Rows: ${startJob}-${endJob} of ${totalJobs}</li> 
				<#assign job_url><@spring.url relativeUrl="${servletPath}/jobs"/></#assign>
				<#if nextJob??><li><a href="${job_url}?startJob=${nextJob?c}&pageSize=${pageSize!20}">Next</a></li></#if>
				<#if previousJob??><li><a href="${job_url}?startJob=${previousJob?c}&pageSize=${pageSize!20}">Previous</a></li></#if>
				<!-- TODO: enable pageSize editing -->
				<li>Page Size: ${pageSize!20}</li>
			</ul>
	
	<#else>
		<p>There are no jobs registered.</p>
	</#if>

</div><!-- jobs -->
