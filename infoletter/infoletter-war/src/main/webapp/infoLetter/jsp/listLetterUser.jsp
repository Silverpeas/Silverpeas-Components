<%@ include file="check.jsp" %>
<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="JavaScript">
function openViewParution(par) {
    document.viewParution.parution.value = par;
    document.viewParution.submit();
}
</script>
</head>
<BODY marginheight=5 marginwidth=5 leftmargin=5 topmargin=5 bgcolor="#FFFFFF">
<%
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Accueil");
	
boolean showHeader = ( (Boolean) request.getAttribute("showHeader") ).booleanValue();
boolean isSuscriber = ((String)request.getAttribute("userIsSuscriber")).equals("true");
if (isSuscriber) 
	operationPane.addOperation(resource.getIcon("infoLetter.desabonner"), resource.getString("infoLetter.desabonner"), "UnsuscribeMe");
else 
	operationPane.addOperation(resource.getIcon("infoLetter.abonner"), resource.getString("infoLetter.abonner"), "SuscribeMe");	

	out.println(window.printBefore());
	out.println(frame.printBefore());	
%>

<%
if (showHeader)
{
	out.println(board.printBefore());
%>
<center>
		<table border="0" cellspacing="0" cellpadding="5" width="100%">
			<tr>
				<td class="txtlibform" valign="baseline" align=left nowrap><%=resource.getString("infoLetter.name")%> :</td>
				<td align=left width="100%"><%= (String) request.getAttribute("letterName") %></td>
			</tr>
			<tr> 
				<td class="txtlibform" valign="top" align=left nowrap><%=resource.getString("GML.description")%> :</td>
				<td align=left><%= (String) request.getAttribute("letterDescription") %></td>
			</tr>
			<tr> 
				<td class="txtlibform" valign="baseline" align=left nowrap><%=resource.getString("infoLetter.frequence")%> :</td>
				<td align=left><%= (String) request.getAttribute("letterFrequence") %></td>
			</tr>
		</table>
</CENTER>
<%
	out.println(board.printAfter());
	out.println("<br>");
}

// Recuperation de la liste des parutions
Vector publications = (Vector) request.getAttribute("listParutions");
int i=0;
				ArrayPane arrayPane = gef.getArrayPane("InfoLetter", "Main", request, session);
		        //arrayPane.setVisibleLineNumber(10);
				
		        arrayPane.setTitle(resource.getString("infoLetter.listParutions"));	
				
		        ArrayColumn arrayColumn0 = arrayPane.addArrayColumn("&nbsp;");
				arrayColumn0.setSortable(false);

				arrayPane.addArrayColumn(resource.getString("infoLetter.name"));
				arrayPane.addArrayColumn(resource.getString("GML.date"));
				// ArrayColumn arrayColumn = arrayPane.addArrayColumn(resource.getString("GML.operation"));
				// arrayColumn.setSortable(false);
if (publications.size()>0) {
	for (i = 0; i < publications.size(); i++) {
						InfoLetterPublication pub = (InfoLetterPublication) publications.elementAt(i);
						if (pub._isValid()) {
							ArrayLine arrayLine = arrayPane.addArrayLine();
						
							IconPane iconPane1 = gef.getIconPane();
							Icon debIcon = iconPane1.addIcon();
							debIcon.setProperties(resource.getIcon("infoLetter.minicone"), "#");
							arrayLine.addArrayCellIconPane(iconPane1);	
						
							arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(pub.getTitle()), "javascript:openViewParution('" + pub.getPK().getId() + "');");
						
							java.util.Date date = DateUtil.parse(pub.getParutionDate());
							ArrayCellText cell = arrayLine.addArrayCellText(resource.getOutputDate(date));
							cell.setCompareOn(date);
						}
	}
}	
		out.println(arrayPane.print());
		
%>
<form name="viewParution" action="View" method="post">
	<input type="hidden" name="parution" value="">
</form>
<%
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</BODY>
</HTML>