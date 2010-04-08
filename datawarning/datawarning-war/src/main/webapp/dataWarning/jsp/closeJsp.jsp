<HTML>
<script language="javascript">
	function chargement()
	{
		window.opener.location.href='<%=(String)request.getAttribute("TheCommand")%>';
		window.close();
	}
</script>
<BODY onLoad="javascript:chargement()">
</BODY>
</HTML>