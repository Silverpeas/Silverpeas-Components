<%--

    Copyright (C) 2000 - 2009 Silverpeas

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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkYellowpages.jsp" %>
<%@ include file="contactsList.jsp.inc" %>

<%
Collection contacts = (Collection) request.getAttribute("Contacts");
%>

<html>
<head>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
</head>
<BODY marginwidth=5 marginheight=5 leftmargin=5 topmargin=5>
<%
Window window = gef.getWindow();

BrowseBar browseBar=window.getBrowseBar();
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel);
browseBar.setPath(resources.getString("GML.print"));

OperationPane operationPane = window.getOperationPane();
operationPane.addOperation(resources.getIcon("yellowpages.printPage"), resources.getString("GML.print"), "javaScript:window.print();");

Frame frame=gef.getFrame();
Board board = gef.getBoard();

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());

displayContactsUser(yellowpagesScc, contacts, null, componentLabel, gef, request, session, resources, out);

out.println(board.printAfter());
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>