<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ page import="org.silverpeas.core.admin.user.model.SilverpeasRole" %>
<%@ page import="org.silverpeas.core.web.selection.BasketSelectionUI" %>

<%@ include file="almanachCheck.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<view:setBundle basename="org.silverpeas.calendar.multilang.calendarBundle" var="calendarBundle"/>
<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>
<view:setConstant var="CALENDAR_EVENT_TYPE" constant="org.silverpeas.core.calendar.CalendarEvent.TYPE"/>

<c:set var="highestUserRole"        value="${requestScope.highestUserRole}"/>
<view:setConstant var="publisherRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.PUBLISHER"/>

<c:set var="currentUser"            value="${requestScope.currentUser}"/>
<c:set var="currentUserId"          value="${currentUser.id}"/>
<c:set var="isAnonymous" value="${requestScope.isAnonymous}"/>
<c:set var="isAccessGuest" value="${requestScope.isAccessGuest}"/>
<c:set var="componentId"            value="${requestScope.browseContext[3]}"/>
<c:set var="timeWindowViewContext"  value="${requestScope.timeWindowViewContext}"/>
<jsp:useBean id="timeWindowViewContext" type="org.silverpeas.components.almanach.AlmanachTimeWindowViewContext"/>

<c:set var="occurrence" value="${requestScope.occurrence}"/>
<jsp:useBean id="occurrence" type="org.silverpeas.core.webapi.calendar.CalendarEventOccurrenceEntity"/>
<c:set var="occurrenceUri" value="${requestScope.occurrence.occurrenceUri}"/>

<fmt:message var="back" key="GML.back"/>
<fmt:message key="GML.notify" var="notifyLabel"/>
<fmt:message var="modifyLabel" key="GML.modify"/>
<fmt:message key="GML.delete" var="deleteLabel"/>
<fmt:message key="GML.putInBasket" var="putInBasket"/>

<c:url var="backUri" value="${requestScope.navigationContext.previousNavigationStep.uri}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.almanachcalendar" xml:lang="${currentUserLanguage}">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel withFieldsetStyle="true"/>
  <title></title>
  <view:includePlugin name="calendar"/>
  <view:includePlugin name="basketSelection"/>
  <view:script src="/almanach/jsp/javaScript/angularjs/services/almanachcalendar.js"/>
  <view:script src="/almanach/jsp/javaScript/angularjs/almanachcalendar.js"/>
</head>
<body ng-controller="viewController">
<view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}"/>
<c:if test="${not isAnonymous and not isAccessGuest}">
  <view:operationPane>
    <silverpeas-calendar-event-management api="eventMng"
                                          on-occurrence-deleted="goToPage('${backUri}')"
                                          on-event-attendee-participation-updated="reloadOccurrenceFromContext()">
    </silverpeas-calendar-event-management>
    <view:operation
        action="angularjs:notifyEventOccurrence(ceo)"
        altText="${notifyLabel}"/>
    <c:if test="${occurrence.canBeModified()}">
      <view:operationSeparator/>
      <view:operation
          action="angularjs:editEventOccurrence(ceo)"
          altText="${modifyLabel}"/>
      <view:operation
          action="angularjs:eventMng.removeOccurrence(ceo)"
          altText="${deleteLabel}"/>
    </c:if>
    <c:if test="<%=BasketSelectionUI.displayPutIntoBasketSelectionShortcut()%>">
      <view:operationSeparator/>
      <view:operation
          action="angularjs:putEventOccurrenceInBasket(ceo)"
          altText="${putInBasket}"/>
    </c:if>
  </view:operationPane>
</c:if>
<view:window>
  <view:frame>
    <silverpeas-calendar-event-view ng-if="ceo"
                                    calendar-event-occurrence="ceo">
      <pane-main>
        <silverpeas-calendar-event-view-main
            calendar-event-occurrence="ceo">
        </silverpeas-calendar-event-view-main>
        <silverpeas-calendar-event-view-attendees
            calendar-event-occurrence="ceo"
            ng-if="ceo.attendees && ceo.attendees.length"
            on-participation-answer="eventMng.eventAttendeeParticipationAnswer(ceo, attendee)">
        </silverpeas-calendar-event-view-attendees>
        <viewTags:viewAttachmentsAsContent componentInstanceId="${componentId}"
                                           resourceType="${CALENDAR_EVENT_TYPE}"
                                           resourceId="${occurrence.eventId}"
                                           highestUserRole="${requestScope.highestUserRole}"/>
        <view:buttonPane>
          <view:button label="${back}" action="${backUri}"/>
        </view:buttonPane>
      </pane-main>
      <pane-extra>
        <silverpeas-calendar-event-view-reminder
            calendar-event-occurrence="ceo">
        </silverpeas-calendar-event-view-reminder>
        <silverpeas-calendar-event-view-recurrence
            calendar-event-occurrence="ceo"
            ng-if="ceo.recurrence">
        </silverpeas-calendar-event-view-recurrence>
        <silverpeas-calendar-event-view-attachment
            calendar-event-occurrence="ceo">
        </silverpeas-calendar-event-view-attachment>
        <silverpeas-calendar-event-view-crud
            calendar-event-occurrence="ceo"></silverpeas-calendar-event-view-crud>
        <silverpeas-calendar-event-view-pdc-classification
            calendar-event-occurrence="ceo"></silverpeas-calendar-event-view-pdc-classification>
      </pane-extra>
    </silverpeas-calendar-event-view>
  </view:frame>
</view:window>
<view:progressMessage/>

<script type="text/javascript">
  almanachCalendar.value('context', {
    currentUserId : '${currentUserId}',
    currentUserLanguage : '${currentUserLanguage}',
    component : '${componentId}',
    componentUriBase : '${componentUriBase}',
    userRole : '${highestUserRole}',
    zoneId : '${timeWindowViewContext.zoneId.toString()}',
    occurrenceUri : '${occurrenceUri}'
  });
</script>
</body>
</html>