<%@ page import="org.silverpeas.components.almanach.AlmanachSettings" %><%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ include file="almanachCheck.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle" var="calendarBundle"/>
<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>

<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="currentUserId" value="${currentUser.id}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="highestUserRole" value="${requestScope.highestUserRole}"/>
<c:set var="zoneId" value="<%=AlmanachSettings.getZoneId()%>"/>
<c:set var="nextEventViewLimit" value="<%=AlmanachSettings.getNbOccurrenceLimitOfNextEventView()%>"/>


<fmt:message var="noEventLabel" key="calendar.label.event.none" bundle="${calendarBundle}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.almanachcalendar" xml:lang="${currentUserLanguage}">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <title></title>
  <view:includePlugin name="calendar"/>
  <view:script src="/almanach/jsp/javaScript/angularjs/services/almanachcalendar.js"/>
  <view:script src="/almanach/jsp/javaScript/angularjs/almanachcalendar.js"/>
</head>
<body class="portlet" ng-controller="portletController">
<silverpeas-calendar-event-occurrence-list
    ng-if="occurrences"
    no-occurrence-label="${noEventLabel}"
    occurrences="occurrences"
    on-event-occurrence-click="gotToEventOccurrence(occurrence)">
</silverpeas-calendar-event-occurrence-list>
<script type="text/javascript">
  almanachCalendar.value('context', {
    currentUserId : '${currentUserId}',
    currentUserLanguage : '${currentUserLanguage}',
    component : '${componentId}',
    componentUriBase : '${componentUriBase}',
    userRole : '${highestUserRole}',
    zoneId : '${zoneId.toString()}',
    limit : ${nextEventViewLimit}
  });
</script>
</body>
</html>