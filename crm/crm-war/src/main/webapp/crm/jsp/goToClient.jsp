<html>
<head>
	<title></title>
	<script type="text/javascript">
		function submitForm() {
			document.forms["enctypeForm"].submit();
		}
	</script>
</head>

<body onload="submitForm()">
	<form name="enctypeForm" action="ViewClient" method="post" enctype="multipart/form-data">
	</form>
</body>
</html>
