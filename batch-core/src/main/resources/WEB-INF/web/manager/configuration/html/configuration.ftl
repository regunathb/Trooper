<#import "/spring.ftl" as spring />
<script type="text/javascript">

</script>
<div id="configuration">

	<h1>Job Configuration</h1>
<!-- 	<#assign url><@spring.url relativeUrl="${servletPath}/job-configuration"/></#assign> -->
		
		<br>
		<span style="color:red "><#if RequestParameters.Error??>${RequestParameters.Error}</#if> </span>
	
	<table id = "job-conf-table" class="bordered-table">
	<tr>
		<th> Job Name </th>
		<th> Edit Job Details </th>
	</tr>
	
	<tr>
	
	<#if newjobs?? && newjobs?size!=0>
	<#list newjobs as job>
		<tr>
					<#assign job_url><@spring.url relativeUrl="${servletPath}/configuration/jobs/${job.name}"/></#assign>
					<#assign mod_url><@spring.url relativeUrl="${servletPath}/configuration/modify/jobs/${job.name}"/></#assign>
					<#assign del_url><@spring.url relativeUrl="${servletPath}/configuration/delete/jobs/${job.name}"/></#assign>
					<td><a href="${job_url}">${job.name}</a></td>
					<td><a href="${mod_url}">Edit</a></td>
				<!--	<td><a href="${del_url}">Delete</a></td> -->
		</tr>
	</#list>
	<#else>
	<tr>
		<td> <span style="font-style:italic">Did not find any Job. Try refresh or add new jobs.</span> </td>
		<td> </td>
	</tr>
	</#if>
	<tr>
	<#assign new_url><@spring.url relativeUrl="${servletPath}/configuration/modify_job"/></#assign>				
	<form id="addNewJob" action="${new_url}" method="POST" enctype="multipart/form-data" encoding="multipart/form-data">
	<td>Add new job: (Upload the configuration file)</td>
	<td>
			<input id="jobFile" type="file" name="jobFile" onchange="this.form.submit()" />
	</td>
	</form>
	</tr>
	</table>


</div><!-- configuration -->
