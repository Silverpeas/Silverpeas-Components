<%@ page import="org.silverpeas.util.FileRepositoryManager" %>
<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ include file="checkYellowpages.jsp" %>
<%
	String 	csvFileName = (String) request.getAttribute("CSVFilename");
	Long 	csvFileSize = (Long) request.getAttribute("CSVFileSize");
	String 	csvFileURL 	= (String) request.getAttribute("CSVFileURL");
%>

<html>
	<head>
	<view:looknfeel/>
	</head>
	<body>
	<view:browseBar extraInformations='<%=resources.getString("GML.export")%>'/>
<view:window popup="true">
<view:frame>
<view:board>
		<table>
			<tr>
				<td class="txtlibform"><%=resources.getString("GML.size")%> :</td>
				<td><%=FileRepositoryManager.formatFileSize(csvFileSize.longValue())%></td>
			</tr>
			<tr>
				<td class="txtlibform"><%=resources.getString("GML.csvFile")%> :</td>
				<td><a href="<%=csvFileURL%>"><%=csvFileName%></a></td>
			</tr>
		</table>
		</view:board>
		<%
			ButtonPane buttonPane = gef.getButtonPane();
			Button button = gef.getFormButton(resources.getString("GML.close"), "javaScript:window.close();", false);
			buttonPane.addButton(button);
			out.println(buttonPane.print());
		%>
	</view:frame>
	</view:window>
	</body>
</html>