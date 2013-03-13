<#import "/spring.ftl" as spring />
<pre style="word-wrap: break-word; white-space: pre-wrap;">
{"services" : {
    "registrations" : {
   <#if serviceInfo?? && serviceInfo?size!=0>
    <#list serviceInfo as statistics >
    		"${statistics.serviceName}": {
								"Version": "${statistics.serviceVersion}",
								"TotalRequestCount": "${statistics.totalRequestsCount}",
								"SuccessRequestCount": "${statistics.successRequestsCount}",
								"FailureRequestCount": "${statistics.errorRequestsCount}",
								"ActiveRequestCount": "${statistics.activeRequestsCount }",
								<#if statistics.p50ResponseTime??>
								"P50ResponseTime": "${statistics.p50ResponseTime}",
								<#else>
								"P50ResponseTime": NA
								</#if>
								<#if statistics.p75ResponseTime??>
								"P75ResponseTime": "${statistics.p75ResponseTime}",
								<#else>
								"P75ResponseTime": NA
								</#if>
								<#if statistics.p99ResponseTime??>
								"P99ResponseTime": "${statistics.p99ResponseTime}",
								<#else>
								"P99ResponseTime": NA
								</#if>
								<#if statistics.p999ResponseTime??>
								"P999ResponseTime": "${statistics.p999ResponseTime}",
								<#else>
								"P999ResponseTime": NA
								</#if>
								<#if statistics.oneMinRate??>
								"OneMinuteRate": "${statistics.oneMinRate}",
								<#else>
								"OneMinuteRate": NA
								</#if>
								<#if statistics.fiveMinRate??>
								"FiveMinuteRate": "${statistics.fiveMinRate}",
								<#else>
								"FiveMinuteRate": NA
								</#if>
								<#if statistics.fifteenMinRate??>
								"FifteenMinuteRate": "${statistics.fifteenMinRate}",
								<#else>
								"FifteenMinuteRate": NA
								</#if>
								"StartupTimeStamp": "${statistics.startupTimeStamp.getTime()?datetime}",
								<#if statistics.lastCalledTimestamp??>
								"LastCalledTimestamp": "${statistics.lastCalledTimestamp.getTime()?datetime}"
								<#else>
								"LastCalledTimestamp": NA
								</#if>
			}
	</#list>
	</#if>
    }
}
</pre>