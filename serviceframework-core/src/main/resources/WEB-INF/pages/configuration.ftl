<#import "/spring.ftl" as spring />
<#include "./../header.ftl"> 
<div id="services">
					
						<h2>Services Registered</h2>
<table id = "sp-conf-table" class="bordered-table">
    <tr>
        <th> Service Name </th>
        <th> Edit </th>
        <th> Test </th>
    </tr>

    <#list serviceInfo as service >
        <tr>
            <#assign job_url><@spring.url relativeUrl="/viewConfig/services/${service.serviceName}_${service.serviceVersion}"/></#assign>
            <#assign mod_url><@spring.url relativeUrl="/modify/services/${service.serviceName}_${service.serviceVersion}"/></#assign>
            <#assign test_url><@spring.url relativeUrl="/test/services/${service.serviceName}"/></#assign>
            <td><a href="${job_url}">${service.serviceName}_${service.serviceVersion}</a></td>
            <td><a href="${mod_url}">Edit</a></td>
            <td><a href="${test_url}">Test</a></td>
        </tr>
    </#list>
</table>


</div>
					<!-- services -->
<#include "./../footer.ftl"> 
