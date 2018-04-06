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
<c:set var="resultSet"         value="${requestScope.resultSet}"/>
<jsp:useBean id="resultSet" type="java.util.List<org.silverpeas.components.jdbcconnector.service.TableRow>"/>

<fmt:message var="windowTitle"   key="windowTitleMain"/>
<fmt:message var="crumbTitle"    key="titreExecution"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>${windowTitle}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <script type="application/javascript">
    function goToApp() {
      spLayout.getBody().load({ComponentId: '${componentId}'});
    }
  </script>
</head>
<body>
<view:browseBar componentId="${componentId}" extraInformations="${crumbTitle}" ignoreComponentLink="false"/>
<view:window>
  <view:frame>
    <div id="result-set">
      <c:set var="fieldNames" value="${resultSet[0].fieldNames}"/>
      <view:arrayPane var="ResultSet${componentId}" routingAddress="Main" numberLinesPerPage="10">
        <c:forEach var="fieldName" items="${fieldNames}">
          <view:arrayColumn title="${fieldName}" compareOn="${(r, i) -> r.getFieldValue(r.fieldNames[i])}" sortable="true"/>
        </c:forEach>
        <view:arrayLines var="row" items="${resultSet}">
          <view:arrayLine>
            <c:forEach var="fieldName" items="${fieldNames}">
              <view:arrayCellText text="${row.getFieldValue(fieldName)}"/>
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