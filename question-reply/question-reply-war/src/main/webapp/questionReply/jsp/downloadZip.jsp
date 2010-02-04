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
<%@ page isELIgnored="false"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"%>
<%@ include file="checkQuestionReply.jsp" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<fmt:setLocale value="${userLanguage}"/>
<fmt:setBundle basename="com.silverpeas.importExportPeas.multilang.importExportPeasBundle"/>
<fmt:message key="importExportPeas.Export" var="page_title" />

<%
	ExportReport report = (ExportReport) request.getAttribute("ExportReport");
%>
<html>
<head>
<view:looknfeel />
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<view:browseBar link="" path="${page_title}" />
<view:window>
  <view:frame>
    <view:board>
<table>
<tr><td class="txtlibform"><fmt:message key="importExportPeas.ExportDuration" /> :</td><td><%=DateUtil.formatDuration(report.getDuration())%></td></tr>
<tr><td class="txtlibform"><fmt:message key="importExportPeas.FileSize"/> :</td><td><%=FileRepositoryManager.getFileSize(report.getZipFileSize())%></td></tr>
<tr><td class="txtlibform"><fmt:message key="importExportPeas.File"/> :</td><td><a href="<%=report.getZipFilePath()%>"><%=report.getZipFileName()%></a> <a href="<%=report.getZipFilePath()%>"><img src="<%=FileRepositoryManager.getFileIcon("zip")%>" border="0" align="absmiddle"></a></td></tr>
</table>
</view:board>
<%
ButtonPane buttonPane = gef.getButtonPane();
Button button = (Button) gef.getFormButton(resource.getString("GML.close"), "javaScript:window.close();", false);
buttonPane.addButton(button);
out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
%>
  </view:frame>
</view:window>  
</body>
</html>