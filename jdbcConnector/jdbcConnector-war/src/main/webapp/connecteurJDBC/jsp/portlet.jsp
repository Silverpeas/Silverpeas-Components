<%--
  Copyright (C) 2000 - 2018 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "https://www.silverpeas.org/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ page import="org.silverpeas.components.jdbcconnector.service.comparators.Equality" %>

<%@ include file="head.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="componentId"       value="${requestScope.browseContext[3]}"/>
<c:set var="queryResult"       value="${requestScope.queryResult}"/>
<jsp:useBean id="queryResult" type="org.silverpeas.components.jdbcconnector.control.QueryResult"/>
<c:set var="nullValue"         value="<%=Equality.NULL%>"/>

<fmt:message var="windowTitle"   key="windowTitleMain"/>
<fmt:message var="crumbTitle"    key="titreExecution"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>${windowTitle}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <script type="application/javascript">
    function goToApp(componentId) {
      spLayout.getBody().load({ComponentId: componentId});
    }
  </script>
</head>
<body>
<view:browseBar componentId="${componentId}" componentJsCallback="goToApp" extraInformations="${crumbTitle}"/>
<view:window>
  <view:frame>
    <div id="result-set">
      <c:set var="fieldNames" value="${queryResult.fieldNames}"/>
      <view:arrayPane var="PortletResultSet${componentId}" routingAddress="portlet" numberLinesPerPage="10">
        <c:forEach var="fieldName" items="${fieldNames}">
          <view:arrayColumn title="${fieldName}" compareOn="${(r, i) -> r.getFieldValue(fieldNames[i])}"/>
        </c:forEach>
        <view:arrayLines var="row" items="${queryResult.rows}">
          <view:arrayLine>
            <c:forEach var="fieldName" items="${fieldNames}">
              <c:set var="currentValue" value="${row.getFieldValue(fieldName)}"/>
              <view:arrayCellText text="${currentValue == null ? nullValue : currentValue}" nullStringValue="${nullValue}"/>
            </c:forEach>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          sp.arrayPane.ajaxControls('#result-set');
        });
      </script>
    </div>
  </view:frame>
</view:window>
</body>
</html>