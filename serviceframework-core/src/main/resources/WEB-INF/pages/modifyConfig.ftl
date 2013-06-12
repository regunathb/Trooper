<#include "./../header.ftl"> 
<script type="text/javascript">

function configFileSave() {
	document.forms['NewserviceForm']['identifier'].value = "Save";
    document.forms['NewserviceForm'].submit();
}
	
</script>
<div id="service">
	
	<H1> Add/Edit service Details :  ${serviceName}</H1>
	
	<span id="error" style="color:red">
		<#if XMLFileError??>
				${XMLFileError}
				<br />
		</#if>
		
		<#if LoadingError??>
		The configuration file could not be loaded. The changes weren't saved to FileSystem. Please refer to the stack trace: <br />
				<textarea readonly style="width: 700px; height: 100px;">${LoadingError}
				</textarea>
				<br />
		</#if>			
	</span>
	
	<form id="NewserviceForm" name="NewserviceForm" action="/deploy/services/${serviceName}" method="POST">

 <input type="hidden" name="identifier" value="null" id="stateChanger"/> 
		<ol>
			<li>
			 <H2> Name:  ${serviceName} </H2>
				 <input type="hidden" name="serviceName" value="${serviceName}" />
			
			<li> <H2> Edit Configuration File:  </H2> 
			
	<!--		<input id="serviceFile" type="file" name="serviceFile" onchange="serviceFileUpload()" />
			<input type="submit" name="submit" id="submit "value="Upload file"  /> -->
				
	<#if XMLFileName??>
	${XMLFileName}
	</#if>	
			
			<li>
				<textarea id="XMLFileContents" name="XMLFileContents" /><#if XMLFileContents??>${XMLFileContents}</#if>
				</textarea>
			
			
	 <input type="button" value="Save Changes" onclick="configFileSave()" name="save" id="save" /> 
			


		</ol>
		<br />
		<!-- Spring JS does not support multipart forms so no Ajax here -->

	</form>
	
	 <script>
			var editor = CodeMirror.fromTextArea(document.getElementById("XMLFileContents"), {
		  		mode: "application/xml",
		  		lineNumbers: true,
		  		lineWrapping: true
				});
			var hlLine = editor.addLineClass(0, "background", "activeline");
			editor.on("cursorActivity", function() {
			var cur = editor.getLine    //alert(document.forms['NewserviceForm']['identifier'].value);
			Handle(editor.getCursor().line);
			if (cur != hlLine) {
		    editor.removeLineClass(hlLine, "background", "activeline");
		    hlLine = editor.addLineClass(cur, "background", "activeline");
		  }
		});
</script>
	
	
</div><!-- services -->

<#include "./../footer.ftl"> 