
<script type="text/javascript">
function jobFileUpload() {
	
	document.forms['NewJobForm']['identifier'].value = "Upload file";
    document.forms['NewJobForm'].submit();
}

function depFileUpload() {
	document.forms['NewJobForm']['identifier'].value = "Upload dependency";
    document.forms['NewJobForm'].submit();
    
}

function XMLFileSave() {
	document.forms['NewJobForm']['identifier'].value = "Save";
    document.forms['NewJobForm'].submit();
}
	
</script>
<div id="job">

	<#assign url>
	<@spring.url relativeUrl="${servletPath}/configuration/modify/jobs/${jobName[0]}" /> 
	</#assign>
	
	<H1> Add/Edit Job Details :  ${jobName[0]}</H1>
	
	<span id="error" style="color:red">
		<#if XMLFileError??>
				${XMLFileError}
				<br />
		</#if>
		<#if DepFileError??>
				${DepFileError}
				<br />
		</#if>	
		
		<#if LoadingError??>
		The spring-batch file could not be loaded. The changes weren't saved to FileSystem. Please refer to the stack trace: <br />
				<textarea readonly style="width: 700px; height: 100px;">${LoadingError}
				</textarea>
				<br />
		</#if>			
	</span>
	
	<form id="NewJobForm" name="NewJobForm" action="${url}" method="POST" enctype="multipart/form-data" encoding="multipart/form-data">

 <input type="hidden" name="identifier" value="null" id="stateChanger"/> 
		<ol>
			<li>
			<#list jobName as job>
			 <H2> Name:  ${job} </H2>
				 <input type="hidden" name="jobName" value="${job}" />
				</#list>
			
			<li> <H2> Upload Configuration File:  </H2> 
			
			<input id="jobFile" type="file" name="jobFile" onchange="jobFileUpload()" />
	<!--		<input type="submit" name="submit" id="submit "value="Upload file"  /> -->
					
			
			<li>
				<textarea id="XMLFileContents" name="XMLFileContents" /><#if XMLFileContents??>${XMLFileContents}</#if>
				</textarea>
			
			
	 <input type="button" value="Save Changes" onclick="XMLFileSave()" name="save" id="save" /> 
			<h2>Dependencies: </h2>
			<#if dependencies?? && dependencies?size!=0>
			<#assign x = 0 />
			<#assign y = 3 />
			<table>
				<#list dependencies as dependency>
					<#if x%3==0>
						<tr>
					</#if>
					<td>${dependency}</td>
					<#assign x = x+1 />
					<#assign y = y+1 />
					<#if (y)%3 == 0>
						</tr>
					</#if>
				</#list>	
			</table>
			<#else>
			<span style="font-style:italic">No dependencies found.  </span>
			
			</#if>				
			<li><h3> Add new Dependency:   </h3>     
			(Only .jar files allowed)    <br />
					<input id="depFile" type="file" name="depFile" onchange="depFileUpload()" />
			<!--		<input type="submit" value="Upload dependency" name="submit" id="submit" class="submit-button" /> -->
					<br />
					

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
			var cur = editor.getLine    //alert(document.forms['NewJobForm']['identifier'].value);
			Handle(editor.getCursor().line);
			if (cur != hlLine) {
		    editor.removeLineClass(hlLine, "background", "activeline");
		    hlLine = editor.addLineClass(cur, "background", "activeline");
		  }
		});
</script>
	
	
</div><!-- jobs -->
