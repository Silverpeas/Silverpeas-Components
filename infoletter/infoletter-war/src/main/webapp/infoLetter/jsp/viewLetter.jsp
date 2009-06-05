<%!
void displayViewWysiwyg(String id, String spaceId, String componentId, HttpServletRequest request, HttpServletResponse response)
	throws com.stratelia.silverpeas.infoLetter.InfoLetterException {
    try {
        getServletConfig().getServletContext().getRequestDispatcher("/wysiwyg/jsp/htmlDisplayer.jsp?ObjectId=" + 
		id + "&SpaceId=" + spaceId + "&ComponentId=" + componentId).include(request, response);
    } catch (Exception e) {
		throw new com.stratelia.silverpeas.infoLetter.InfoLetterException("viewLetter_JSP.displayViewWysiwyg",
		com.stratelia.webactiv.util.exception.SilverpeasRuntimeException.ERROR, e.getMessage());			
    }
}
%>
<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
String parutionTitle = (String) request.getAttribute("parutionTitle");
String parution = (String) request.getAttribute("parution");
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
function goFiles (){
	document.attachedFiles.submit();
}
</script>
</head>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	browseBar.setPath("<a href=\"Accueil\"></a> " + Encode.javaStringToHtmlString(parutionTitle));
	out.println(window.printBefore());
 
	//Instanciation du cadre avec le view generator
	out.println(frame.printBefore());	

%>

	<table width="100%">
		<tr><td width="80%">
			<%
				out.flush();
				displayViewWysiwyg(parution, spaceId, componentId, request, response);		
			%>
		</td>
		<td valign="top">
			<%
				out.flush();
				getServletConfig().getServletContext().getRequestDispatcher("/attachment/jsp/displayAttachments.jsp?Id="+parution+"&ComponentId="+componentId+"&Context=Images").include(request, response);
			%>
		</td></tr>
	</table>
	<form name="attachedFiles" action="FilesView" method="post">			
		<input type="hidden" name="parution" value="<%= parution %>">
	</form>
<%
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
</form>
</BODY>
</HTML>

