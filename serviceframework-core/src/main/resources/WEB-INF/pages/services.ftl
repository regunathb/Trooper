<#import "/spring.ftl" as spring />
<#include "./../header.ftl"> 
<div id="services">
					
						<h2>Services Registered</h2>
						<table title="Services Names" class="bordered-table">
							<tr>
								<th>Name</th>
								<th>Version</th>
								<th>Total Request Count</th>
								<th>Success request count</th>
								<th>Faliure request count</th>
								<th>Average response time</th>
								<th>Minimum response time</th>
								<th>Maximum Response time</th>
								<th>Last Service Response time</th>
								<th>Active Request Count</th>
								<th>Startup Time Stamp</th>
								<th>Last Called Timestamp</th>
							</tr>
							<#list serviceInfo as statistics >
							<tr class="name-sublevel1-even">
								<td>
									<a href="/test/services/${statistics.serviceName}">
									${statistics.serviceName}
									</a>
								</td>
								<#if statistics.serviceVersion??>
								<td>${statistics.serviceVersion}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.totalRequestsCount??>
								<td> ${statistics.totalRequestsCount}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.successRequestsCount??>
								<td> ${statistics.successRequestsCount}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.errorRequestsCount??>
								<td> ${statistics.errorRequestsCount}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.averageResponseTime??>
								<td> ${statistics.averageResponseTime}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.minimumResponseTime??>
								<td> ${statistics.minimumResponseTime}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.maximumResponseTime??>
								<td> ${statistics.maximumResponseTime}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.lastServiceRequestResponseTime??>
								<td> ${statistics.lastServiceRequestResponseTime}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.activeRequestsCount??>
								<td> ${statistics.activeRequestsCount }</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.startupTimeStamp??>
								<td>${statistics.startupTimeStamp.getTime()?datetime}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.lastCalledTimestamp??>
								<td> ${statistics.lastCalledTimestamp.getTime()?datetime}</td>
								<#else>
								<td> NA </td>
								</#if>
								
							</tr>
							</#list>
						</table>

					</div>
					<!-- services -->
<#include "./../footer.ftl"> 
