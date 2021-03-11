<%--
  Copyright (C) 2000 - 2021 Silverpeas
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ tag import="org.silverpeas.components.mydb.web.MyDBMessageManager" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<fmt:message var="technicalErrorLabel" key="mydb.error.technical"/>

<c:set var="messageManager" value="<%=MyDBMessageManager.get()%>"/>
<c:set var="isError" value="${messageManager.isError()}"/>

<c:if test="${isError}">
  <div id="error" class="MessageReadHighPriority">
    <span>${messageManager.getError().getFirst()}</span>
    <c:set var="technicalError" value="${messageManager.getError().getSecond()}"/>
    <c:if test="${not empty technicalError}">
      <script type="text/javascript">
        function showTechnicalError() {
          SilverpeasError.add('${silfn:escapeJs(technicalError)}').show();
        }
      </script>
      <div id="technicalError">
        <a href="javascript:void(0)" onclick="showTechnicalError()">${technicalErrorLabel}</a>
      </div>
    </c:if>
  </div>
</c:if>
