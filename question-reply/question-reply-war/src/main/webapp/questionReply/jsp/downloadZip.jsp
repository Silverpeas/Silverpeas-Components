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

<%@ page isELIgnored="false"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="checkQuestionReply.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle basename="com.silverpeas.importExportPeas.multilang.importExportPeasBundle"/>
<view:setBundle basename="com.stratelia.webactiv.multilang.generalMultilang" var="general"/>
<fmt:message key="importExportPeas.Export" var="page_title" />
<fmt:message key="GML.close" var="close_button" bundle="${general}" />


<%
  ExportReport report = (ExportReport) request.getAttribute("ExportReport");
%>
<html>
  <head>
    <title><c:out value="${page_title}" /></title>
    <view:looknfeel />
  </head>
  <body>
    <view:browseBar>
      <view:browseBarElt link="" label="${page_title}" />
    </view:browseBar>
    <view:window popup="true">
      <view:frame>
        <view:board>
          <table>
            <tr>
              <td class="txtlibform"><fmt:message key="importExportPeas.ExportDuration" /> :</td>
              <td><%=DateUtil.formatDuration(report.getDuration())%></td>
            </tr>
            <tr>
              <td class="txtlibform"><fmt:message key="importExportPeas.FileSize"/> :</td>
              <td><%=FileRepositoryManager.formatFileSize(report.getZipFileSize())%></td>
            </tr>
            <tr><td class="txtlibform"><fmt:message key="importExportPeas.File"/> :</td>
              <td><a href="<%=report.getZipFilePath()%>"><%=report.getZipFileName()%></a> <a href="<%=report.getZipFilePath()%>"><img src="<%=FileRepositoryManager.getFileIcon("zip")%>" border="0" align="absmiddle" /></a></td>
            </tr>
          </table>
        </view:board>
        <view:buttonPane horizontalPosition="${true}" verticalPosition="${false}">
          <view:button action="javaScript:window.close();" label="${close_button}"/>
        </view:buttonPane>
      </view:frame>
    </view:window>  
  </body>
</html>