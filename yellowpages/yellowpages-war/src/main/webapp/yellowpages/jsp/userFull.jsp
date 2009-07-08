<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkYellowpages.jsp" %>

<%										
UserFull user = (UserFull) request.getAttribute("UserFull");
%>
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</HEAD>
<BODY>
<%
Window window = gef.getWindow();
Frame frame = gef.getFrame();
Board board = gef.getBoard();
        
OperationPane operationPane = window.getOperationPane();
operationPane.addOperation(resources.getIcon("yellowpages.contactPrint"), resources.getString("GML.print"), "javaScript:window.print();");

BrowseBar browseBar = window.getBrowseBar();
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel);
browseBar.setPath(resources.getString("BBarconsultManager"));

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>

<center>
<table CELLPADDING=5 CELLSPACING=0 BORDER=0 WIDTH=100%>
<tr>
	<td class=txtlibform width="100"><%=resources.getString("Contact")%> :</td>
	<td align=left class=txtnav><%=user.getDisplayedName()%></td>
</tr>
<tr>
	<td valign=baseline align=left class=txtlibform><%=resources.getString("GML.eMail")%> :</td>
	<td align=left><%=user.geteMail()%></td>
</tr>
<%
	String[] properties = user.getPropertiesNames();
	String property = null;
	for (int p=0; p<properties.length; p++)
	{
		property = (String) properties[p];
		if (!property.startsWith("password"))
		{
			%>
			<tr>
				<td valign="baseline" align=left class="txtlibform"><%=user.getSpecificLabel(resources.getLanguage(), property) %> :</td>
				<td align="left" valign="baseline"><%=Encode.javaStringToHtmlString(user.getValue(property))%></td>     
			</tr>
			<%
		}
	}
%>
</table>

<%
	out.println(board.printAfter());
	out.println(frame.printAfter());
	out.println(window.printAfter());
%>

</BODY>
</HTML>