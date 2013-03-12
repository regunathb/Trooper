<#import "/spring.ftl" as spring />
<pre style="word-wrap: break-word; white-space: pre-wrap;">
{"services" : {
    "registrations" : {
   <#if serviceInfo?? && serviceInfo?size!=0>
    <#list serviceInfo as statistics >
    		"${statistics.serviceName}": {
								"Version": "${statistics.serviceVersion}",
								"Total Request Count": "${statistics.totalRequestsCount}",
								"Success request count": "${statistics.successRequestsCount}",
								"Faliure request count": "${statistics.errorRequestsCount}",
								"Active Request Count": "${statistics.activeRequestsCount }",
								"P50ResponseTime": "${statistics.p50ResponseTime}",
								"P75ResponseTime": "${statistics.p75ResponseTime}",
								"P99ResponseTime": "${statistics.p99ResponseTime}",
								"P999ResponseTime": "${statistics.p999ResponseTime}",
								"oneMinRate": "${statistics.oneMinRate}",
								"fiveMinRate": "${statistics.fiveMinRate}",
								"fifteenMinRate": "${statistics.fifteenMinRate}",
								"Startup Time Stamp": "${statistics.startupTimeStamp.getTime()?datetime}",
								<#if statistics.lastCalledTimestamp??>
								"Last Called Timestamp": "${statistics.lastCalledTimestamp.getTime()?datetime}"
								<#else>
								"Last Called Timestamp": NA
								</#if>
			}
	</#list>
	</#if>
    }
}
</pre>