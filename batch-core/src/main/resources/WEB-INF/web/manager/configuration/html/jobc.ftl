
<div id="job">

	<#assign url><@spring.url relativeUrl="${servletPath}/modify/jobs/${jobName}"/></#assign>
	<#assign mod_url><@spring.url relativeUrl="${servletPath}/configuration/modify/jobs/${jobName}"/></#assign>
					
	<H1> Name: ${jobName} (<a href="${mod_url}"> Edit </a>) </H1>
		
	<h2> Job Configuration File:</h2>
	<br />

	<textarea id="XMLFileContents">
	${XMLFileContents}
	</textarea>
	 <script>
			var editor = CodeMirror.fromTextArea(document.getElementById("XMLFileContents"), {
		  		mode: "application/xml",
		  		lineNumbers: true,
		  		lineWrapping: true,
		  		readOnly: true
				});
			var hlLine = editor.addLineClass(0, "background", "activeline");
			editor.on("cursorActivity", function() {
			var cur = editor.getLineHandle(editor.getCursor().line);
			if (cur != hlLine) {
		    editor.removeLineClass(hlLine, "background", "activeline");
		    hlLine = editor.addLineClass(cur, "background", "activeline");
		  }
		});
</script>
	
	

	
	<br />
	<br />
	
	<h2>Dependencies: </h2> 
			<#if dependencies?? && dependencies?size!=0>
			These are the dependencies found in the /lib folder: 
			<ul>
				<#list dependencies as dependency>
					<li />${dependency}
					<br />
				</#list>	
				</ul>
			<#else>
			<span style="font-style:italic">No dependencies found. You can add dependencies in the Edit Menu </span>
			
			</#if>		
	
</div><!-- jobs -->
