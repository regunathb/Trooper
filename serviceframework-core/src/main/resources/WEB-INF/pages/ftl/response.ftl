<#include "./../header.ftl"> 
<div id="response">
	<h2>Response</h2>
	
	<textarea id="XMLFileContents" name="XMLFileContents">		
	${response}
	</textarea>
</div>
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
<#include "./../footer.ftl"> 
