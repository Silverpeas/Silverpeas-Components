<%@ include file="checkCrm.jsp" %>

<html>
<head>
	<title></title>
	<script type="text/javascript">
		function functionSubmit() {
			window.document.enctypeForm.submit();
		}
	</script>
</head>

<body onLoad="functionSubmit()">
	<Form Name="enctypeForm" ACTION="ViewClient" Method="POST" ENCTYPE="multipart/form-data">		
		<input type="hidden" name="Action" VALUE="ViewClient">
	</Form>
</body>
</html>
