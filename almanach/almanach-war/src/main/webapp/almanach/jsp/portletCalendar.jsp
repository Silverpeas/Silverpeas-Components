<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="flag"><c:out value="${param['flag']}" default="user"/></c:set>
<c:set var="calendarView" value="${requestScope.calendarView}"/>
<c:set var="currentDay" value="${calendarView.currentDay}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <link rel='stylesheet' type='text/css' href="<c:url value='/almanach/jsp/styleSheets/almanach.css'/>" />
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <view:includePlugin name="calendar"/>
    <script type="text/javascript">
      function nextView()
      {
        document.almanachForm.Action.value = "NextView";
        document.almanachForm.submit();
      }

      function previousView()
      {
        document.almanachForm.Action.value = "PreviousView";
        document.almanachForm.submit();
      }
      function goToDay()
      {
        document.almanachForm.Action.value = "GoToday";
        document.almanachForm.submit();
      }

      function clickEvent(idEvent, date, componentId){
        viewEvent(idEvent, date, componentId);
      }

      function clickDay(day){
        flag = "<c:out value='${flag}'/>";
        if(flag == "publisher" || flag == "admin")
          addEvent(day);
      }

      function viewEvent(id, date, componentId)
      {
        document.viewEventForm.action = "<c:url value='/Ralmanach/'/>"+componentId+"/viewEventContent.jsp";
        document.viewEventForm.Id.value = id;
        document.viewEventForm.Date.value = date;
        document.viewEventForm.submit();
      }

      function addEvent(day)
      {
        document.createEventForm.Day.value = day;
        document.createEventForm.submit();
      }
      
      function buildCalendarView() {
      }

      $(document).ready(function() {
        
        var currentDay = new Date();
        currentDay.setFullYear(${currentDay.year});
        currentDay.setMonth(${currentDay.month});
        currentDay.setDate(${currentDay.dayOfMonth});

        // page is now ready, initialize the calendar...
        <c:if test='${not calendarView.viewType.nextEventsView}'>
            $("#calendar").calendar({
              view: "${fn:toLowerCase(calendarView.viewType.name)}",
              weekends: ${calendarView.weekendVisible},
              firstDayOfWeek: ${calendarView.firstDayOfWeek},
              currentDate: currentDay,
              events: <c:out value='${calendarView.eventsInJSON}' escapeXml='yes'/>,
              onday: clickDay,
              onevent: function(event) {
                var eventDate = $.fullCalendar.formatDate(event.start, "yyyy/MM/dd");
                clickEvent(event.id, eventDate, event.instanceId);
              }
            });
        </c:if>
        });
    </script>
  </head>
  <body class="portlet">
    <view:window>
      <view:frame>

        <!-- AFFICHAGE HEADER -->
        <fmt:message key="almanach.icons.leftArrow" var="leftArrow" bundle="${icons}"/>
        <fmt:message key="almanach.icons.rightArrow" var="rightArrow" bundle="${icons}"/>
        <fmt:message key="auJour" var="today" />

        <div id="navigation">
          <fmt:message key="almanach.icons.leftArrow" var="leftArrow" bundle="${icons}"/>
          <fmt:message key="almanach.icons.rightArrow" var="rightArrow" bundle="${icons}"/>
          <div id="currentScope">
            <a href="javascript:onClick=previousView()"><img src="<c:url value='${leftArrow}'/>" border="0" alt="" align="top"/></a>
            <span class="txtnav"><c:out value="${calendarView.label}" /></span>
            <a href="javascript:onClick=nextView()"><img src="<c:url value='${rightArrow}'/>" border="0" alt="" align="top"/></a>
          </div>
          <div id="today">
            <a href="javascript:onClick=goToDay()"><c:out value="${today}" /></a>
          </div>
        </div>

        <div id="calendar"></div>

      </view:frame>
    </view:window>
    <form name="almanachForm" action="../..<c:out value='${calendarView.almanach.url}'/>almanach.jsp"  method="POST" target="MyMain">
      <input type="hidden" name="Action"/>
      <input type="hidden" name="Id"/>
    </form>

    <form name="viewEventForm" action=""  method="POST" target="MyMain">
      <input type="hidden" name="Id"/>
      <input type="hidden" name="Date"/>
    </form>

    <form name="createEventForm" action="../..<c:out value='${calendarView.almanach.url}'/>createEvent.jsp" method="POST" target="MyMain">
      <input type="hidden" name="Day"/>
    </form>

  </body>
</html>