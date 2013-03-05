<%@ include file="./header.jsp"%>

<%@ page import="org.trpr.platform.service.model.common.statistics.ServiceStatistics"%>

<%

ServiceStatistics statistics = (ServiceStatistics)request.getAttribute("serviceInfo");

 %>
<script type="text/javascript">
function jobFileUpload() {
	
	document.forms['ServiceTestForm']['identifier'].value = "Upload file";
    document.forms['ServiceTestForm'].submit();
}
</script>
<div id="services">
	<h2>
		<%out.println(statistics.getServiceName()); %>
	</h2>
	Please enter the details to test the service:

	<form id="ServiceTestForm" name="ServiceTestForm"
		action="/execute/services/<% out.println(statistics.getServiceName());%>" method="POST">
		Service Request Class Name: &nbsp;&nbsp;&nbsp;<input type="text" name="serviceRequestClass" /> <br />
		Service Response Class Name: <input type="text" name="serviceResponseClass" /> <br />
		Service Request File: 
		<textarea id="XMLFileContents" name="XMLFileContents">		
		</textarea>
		
		<input type="submit" />
	
	</form>

	 <script>
			var editor = CodeMirror.fromTextArea(document.getElementById("XMLFileContents"), {
		  		mode: "application/xml",
		  		lineNumbers: true,
		  		lineWrapping: true
				});
			var hlLine = editor.addLineClass(0, "background", "activeline");
			editor.on("cursorActivity", function() {
			var cur = editor.getLine    //alert(document.forms['NewJobForm']['identifier'].value);
			Handle(editor.getCursor().line);
			if (cur != hlLine) {
		    editor.removeLineClass(hlLine, "background", "activeline");
		    hlLine = editor.addLineClass(cur, "background", "activeline");
		  }
		});
	</script>
</div>
<%@ include file="./footer.jsp"%>
