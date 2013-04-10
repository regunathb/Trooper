<#import "/spring.ftl" as spring />
<#include "./../header.ftl"> 
<div id="services">
					
						<h2>Services Registered</h2>
						<table title="Services Names" class="bordered-table">
							<tr>
								<th style="border-right: 1px solid #b8c885" rowspan="2">Name</th>
								<th style="border-right: 1px solid #b8c885" rowspan="2">Version</th>
								<th style="border-right: 1px solid #b8c885" colspan="4">Counts</th>
								<th style="border-right: 1px solid #b8c885" colspan="5">Response Time (ms)</th>
								<th style="border-right: 1px solid #b8c885" colspan="3">Request Rate (Rps)</th>
								<th style="border-right: 1px solid #b8c885" colspan="3">Error Rate (Rps)</th>
								<th style="border-right: 1px solid #b8c885" colspan="2">TimeStamps</th>
						
							</tr>
							<tr>
							<th>Total</th>
								<th>Success</th>
								<th>Faliure</th> 
								<th style="border-right: 1px solid #b8c885">Active</th>
								
								<th>p50</th>
								<th>p75</th>
								<th>p99</th>
								<th>p99.9</th>
								<th style="border-right: 1px solid #b8c885">mean</th> 

								<th>1 min</th>
								<th>5 min</th>
								<th style="border-right: 1px solid #b8c885">15 min</th>
								
								<th>1 min</th>
								<th>5 min</th>
								<th style="border-right: 1px solid #b8c885">15 min</th>
								
								<th>Startup</th>
								<th style="border-right: 1px solid #b8c885">Last Called</th>
							</tr>
							<#list serviceInfo as statistics >
							<tr class="name-sublevel1-even">
								<td style="border-right: 1px solid #b8c885">
									<a href="/test/services/${statistics.serviceName}">
									${statistics.serviceName}
									</a>
								</td>
								<#if statistics.serviceVersion??>
								<td style="border-right: 1px solid #b8c885">${statistics.serviceVersion}</td>
								<#else>
								<td style="border-right: 1px solid #b8c885"> NA </td>
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
								
								<#if statistics.activeRequestsCount??>
								<td style="border-right: 1px solid #b8c885"> ${statistics.activeRequestsCount }</td>
								<#else>
								<td style="border-right: 1px solid #b8c885"> NA </td>
								</#if>
								
								<#if statistics.p50ResponseTime??>
								<td> ${statistics.p50ResponseTime}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.p75ResponseTime??>
								<td> ${statistics.p75ResponseTime}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.p99ResponseTime??>
								<td> ${statistics.p99ResponseTime}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.p999ResponseTime??>
								<td> ${statistics.p999ResponseTime}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.meanResponseTime??>
								<td style="border-right: 1px solid #b8c885"> ${statistics.meanResponseTime}</td>
								<#else>
								<td style="border-right: 1px solid #b8c885"> NA </td>
								</#if>
								<#if statistics.oneMinRate??>
								<td> ${statistics.oneMinRate}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.fiveMinRate??>
								<td> ${statistics.fiveMinRate}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.fifteenMinRate??>
								<td style="border-right: 1px solid #b8c885"> ${statistics.fifteenMinRate}</td>
								<#else>
								<td style="border-right: 1px solid #b8c885"> NA </td>
								</#if>
								<#if statistics.oneMinErrorRate??>
								<td> ${statistics.oneMinErrorRate}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.fiveMinErrorRate??>
								<td> ${statistics.fiveMinErrorRate}</td>
								<#else>
								<td> NA </td>
								</#if>
								<#if statistics.fifteenMinErrorRate??>
								<td style="border-right: 1px solid #b8c885"> ${statistics.fifteenMinErrorRate}</td>
								<#else>
								<td style="border-right: 1px solid #b8c885"> NA </td>
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
