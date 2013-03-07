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
								"Average response time": "${statistics.averageResponseTime}",
								"Minimum response time": "${statistics.minimumResponseTime}",
								"Maximum Response time": "${statistics.maximumResponseTime}",
								"Last Service Response time": "${statistics.lastServiceRequestResponseTime}",
								"Active Request Count": "${statistics.activeRequestsCount }",
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