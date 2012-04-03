<%--

    Copyright (C) 2000 - 2011 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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
browseBar.setClickable(false);

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
	<td align=left><a href=mailto:<%=EncodeHelper.javaStringToHtmlString(user.geteMail())%>><%=EncodeHelper.javaStringToHtmlString(EncodeHelper.javaStringToHtmlString(user.geteMail()))%></A></td>
</tr>
<%
  String[] properties = user.getPropertiesNames();
	String property = null;
	for (int p=0; p<properties.length; p++)
	{
		property = properties[p];
		if (!property.startsWith("password"))
		{
			%>
			<tr>
				<td valign="baseline" align=left class="txtlibform"><%=user.getSpecificLabel(resources.getLanguage(), property) %> :</td>
				<td align="left" valign="baseline"><%=EncodeHelper.javaStringToHtmlString(user.getValue(property))%></td>     
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