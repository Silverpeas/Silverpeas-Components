<%@ page import="org.silverpeas.util.FileServerUtils" %>
<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
</head>
<body>
<%
	String statusMessage = "";
	String emailCsvFileName = "";
	String urlEmailCsv = "";
	
		boolean exportOk = new Boolean((String) request.getAttribute("ExportOk")).booleanValue();
		if (exportOk) {
			 statusMessage = resource.getString("GML.ExportSucceeded");
			 emailCsvFileName = (String) request.getAttribute("EmailCsvName");
			 urlEmailCsv = FileServerUtils.getUrlToTempDir(emailCsvFileName);
		} else {
			 statusMessage = resource.getString("GML.ExportFailed");
		}
%>
<view:window popup="true">
<view:frame>
<view:board>

<% if (exportOk) { %>
	<table width="100%" cellpadding="2" cellspacing="2" border="0">
		<tr>
			<td class="txtlibform"><%=statusMessage%></td>
	    </tr>
	    <tr>
	    	<td><a href="<%=urlEmailCsv%>"><%=emailCsvFileName%></a></td>
	    </tr>
	</table>
<% } %>
</view:board>
<center>
<%
	Button button = gef.getFormButton(resource.getString("GML.close"), "javascript:window.close()", false);
	out.print(button.print());
%>
</center>
</view:frame>
</view:window>
</body>
</html>