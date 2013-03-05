<%@ include file="./header.jsp" %>
					<div id="services">
					
<%@ page import="org.trpr.platform.servicefw.impl.ServiceStatisticsGatherer" %>
<%@ page import="org.trpr.platform.service.model.common.statistics.ServiceStatistics" %>
						<h2>Services Registered</h2>
						${message}
						<table title="Services Names" class="bordered-table">
							<tr>
								<th>Name</th>
								<th>Version</th>
								<th>Startup Time Stamp</th>
								<th>Last Called Timestamp</th>
								<th>Active Request Count</th>
								<th>Total Request Count</th>
								<th>Average response time</th>
								<th>Minimum response time</th>
								<th>Maximum Response time</th>
								<th>Last Service Response time</th>
								<th>Error request count</th>
								<th>Success request count</th>
							</tr>
							<% 
							ServiceStatistics[] serviceInfo = (ServiceStatistics[])request.getAttribute("serviceInfo");
							for(ServiceStatistics statistics:serviceInfo) {
								%>
							<tr class="name-sublevel1-even">
								<%
								out.println("<td><a href=\"/test/services/"+statistics.getServiceName()+"\">"+statistics.getServiceName()+"</a></td>");
								out.println("<td>"+statistics.getServiceVersion()+"</td>");
								out.println("<td>"+statistics.getStartupTimeStamp().getTime()+"</td>");
								if(statistics.getLastCalledTimestamp()==null)
									out.println("-");
								else
									out.println("<td>"+statistics.getLastCalledTimestamp().getTime()+"</td>");
								out.println("<td>"+statistics.getActiveRequestsCount()+"</td>");
								out.println("<td>"+statistics.getTotalRequestsCount()+"</td>");
								out.println("<td>"+statistics.getAverageResponseTime().shortValue()+"</td>");
								out.println("<td>"+statistics.getMinimumResponseTime().shortValue()+"</td>");
								out.println("<td>"+statistics.getMaximumResponseTime().shortValue()+"</td>");
								out.println("<td>"+statistics.getLastServiceRequestResponseTime()+"</td>");
								out.println("<td>"+statistics.getErrorRequestsCount()+"</td>");
								out.println("<td>"+statistics.getSuccessRequestsCount()+"</td>");
								
								%>
							</tr>
							<%
							}
							%>
						</table>

					</div>
					<!-- services -->
<%@ include file="./footer.jsp" %>
