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
<%@ include file="checkKmelia.jsp" %>
<%
	List report = (List) request.getAttribute("ZipReport");

	String zipName 	= (String) report.get(0);
	String zipSize 	= (String) report.get(1);
	String zipURL	= (String) report.get(2);
%>
<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
</head>
<body>
<%
Window window = gef.getWindow();
Frame frame = gef.getFrame();
Board board = gef.getBoard();

BrowseBar browseBar = window.getBrowseBar();

browseBar.setComponentName(resources.getString("GML.ExportSucceeded"));

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table cellpadding="3" border="0">
<tr><td class="txtlibform" style="vertical-align:middle;"><%=resources.getString("GML.file")%> :</td><td><a href="<%=zipURL%>"><%=zipName%></a> <a href="<%=zipURL%>"><img src="<%=FileRepositoryManager.getFileIcon("zip")%>" border="0" align="absmiddle"></a></td></tr>
<tr><td class="txtlibform"><%=resources.getString("GML.size")%> :</td><td><%=FileRepositoryManager.formatFileSize(Long.parseLong(zipSize))%></td></tr>
</table>
<%
out.println(board.printAfter());
ButtonPane buttonPane = gef.getButtonPane();
Button button = (Button) gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
buttonPane.addButton(button);
out.println("<br/><center>"+buttonPane.print()+"</center>");
out.println(frame.printAfter());
out.println(window.printAfter());
%>
</body>
</html>