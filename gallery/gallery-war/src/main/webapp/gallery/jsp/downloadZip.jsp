<%--

    Copyright (C) 2000 - 2020 Silverpeas

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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<%@ page import="org.silverpeas.core.util.file.FileRepositoryManager" %>

<c:set var="report" value="${requestScope.ExportReport}" />
<fmt:setLocale value="${sessionScope[sessionController].language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<fmt:message var="browseBarExport" key="GML.export.result"/>
<fmt:message var="closeButton" key="GML.close"/>
<c:choose>
  <c:when test="${report.error != null}">
    <c:forEach var="element" items="${report.error.stackTrace}" >
      <c:out value="${element}" /> <br/>
    </c:forEach>
  </c:when>
  <c:otherwise>
    <table>
    	<tr>
        <td class="txtlibform"><fmt:message key="GML.export.file"/> :</td>
        <td><a href="${report.zipFilePath}">${report.zipFileName}</a>
        <a href="${report.zipFilePath}"><img src="<%=FileRepositoryManager.getFileIcon("zip")%>" border="0" align="absmiddle" alt="${report.zipFileName}"/></a></td>
      </tr>
      <tr>
        <td class="txtlibform"><fmt:message key="GML.export.fileSize"/> :</td>
        <td>${view:humanReadableSize(report.zipFileSize)}</td>
      </tr>
      <tr>
        <td class="txtlibform"><fmt:message key="GML.export.duration"/> :</td>
        <td>${silfn:getDuration(report.duration).formattedDurationAsHMS}</td>
      </tr>
    </table>
  </c:otherwise>
</c:choose>