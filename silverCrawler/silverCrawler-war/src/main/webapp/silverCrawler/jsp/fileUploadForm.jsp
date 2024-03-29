<%--

    Copyright (C) 2000 - 2024 Silverpeas

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
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>

<%@ include file="check.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<view:includePlugin name="tkn"/>
<form id="fileUploadForm" name="fileUploadForm" method="POST" action="UploadFile" enctype="multipart/form-data">
  <table width="100%" cellspacing="2" cellpadding="2" border="0">

    <c:if test="${not empty errorMessage}">
      <tr>
        <td colspan="2">${errorMessage}</td>
      </tr>
    </c:if>

    <tr>
      <td><fmt:message key="silverCrawler.fileName"/></td>
      <td><input type="file" name="newFile" id="newFile"></td>
    </tr>

    <tr>
      <td colspan="2"><input type="checkbox" name="replaceExistingFile" value="Y">
        <fmt:message key="silverCrawler.replaceExistingFile"/></td>
    </tr>
  </table>
</form>