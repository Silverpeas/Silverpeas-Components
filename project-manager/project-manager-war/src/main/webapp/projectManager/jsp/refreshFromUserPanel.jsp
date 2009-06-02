<%@ include file="check.jsp"%>

<%
String responsableFullName 	= (String) request.getAttribute("ResponsableFullName");
String responsableId 		= (String) request.getAttribute("ResponsableId");
%>

<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
   out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript">
function refresh() {
	<%
	if (responsableId != null) {
	%>
		window.opener.document.actionForm.Responsable.value = "<%=responsableFullName%>";
		window.opener.document.actionForm.ResponsableName.value = "<%=responsableFullName%>";
		window.opener.document.actionForm.ResponsableId.value = "<%=responsableId%>";
	<%
	}
	%>
	window.close();
}
</script>
</HEAD>
<BODY onLoad=refresh()>
</BODY>
</HTML>