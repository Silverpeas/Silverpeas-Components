<%@ include file="check.jsp" %>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());

	String userId = 	(String)request.getAttribute("userId");
%>

<script language="javascript">
function goToWysiwyg() {
    document.toWysiwyg.submit();
}
</script>

</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<%
	//Affichage du contenu: appel à l'éditeur wysiwyg
	out.println(window.printBefore());
%>

	<form name="toWysiwyg" Action="../../wysiwyg/jsp/htmlEditor.jsp" method="Post">
	<input type="hidden" name="SpaceId" value="<%=spaceId%>">
	<input type="hidden" name="SpaceName" value="<%=spaceLabel%>">
	<input type="hidden" name="ComponentId" value="<%=componentId%>">
	<input type="hidden" name="ComponentName" value="<%=componentLabel%>">
	<input type="hidden" name="UserId" value="<%=userId%>">
	<input type="hidden" name="BrowseInfo" value="<%=componentLabel%>">
	<input type="hidden" name="ObjectId" value="<%=componentId%>">
	<input type="hidden" name="Language" value="<%=resource.getLanguage()%>">
	<input type="hidden" name="ReturnUrl" value="<%=m_context%><%=webPagesUrl%>Preview">
	</form>

	<SCRIPT>goToWysiwyg()</SCRIPT>

<%
	out.println(window.printAfter());
%>

</body>
</html>
