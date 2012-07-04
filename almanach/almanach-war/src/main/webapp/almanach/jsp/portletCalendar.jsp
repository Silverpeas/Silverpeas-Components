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

<c:set var="calendarView" value="${requestScope.calendarView}"/>
<c:set var="currentDay" value="${calendarView.currentDay}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <link rel='stylesheet' type='text/css' href="<c:url value='/almanach/jsp/styleSheets/almanach.css'/>" />
    <style type="text/css">
    </style>
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <view:includePlugin name="calendar"/>
    <script type="text/javascript">
      function viewEvent( id, date, componentId )
      {
        document.viewEventForm.action = "<c:url value='/Ralmanach/'/>"+componentId+"/viewEventContent.jsp";
        document.viewEventForm.Id.value = id;
        document.viewEventForm.Date.value = date;
        document.viewEventForm.submit();
      }
      
      function renderNextEvents( target ) {
        var MONTH_NAMES = ['<fmt:message key="GML.mois0"/>', '<fmt:message key="GML.mois1"/>', '<fmt:message key="GML.mois2"/>', '<fmt:message key="GML.mois3"/>',
          '<fmt:message key="GML.mois4"/>', '<fmt:message key="GML.mois5"/>', '<fmt:message key="GML.mois6"/>', '<fmt:message key="GML.mois7"/>',
          '<fmt:message key="GML.mois8"/>', '<fmt:message key="GML.mois9"/>', '<fmt:message key="GML.mois10"/>', '<fmt:message key="GML.mois11"/>'];
        var DAY_NAMES = ['<fmt:message key="GML.jour1"/>', '<fmt:message key="GML.jour2"/>', '<fmt:message key="GML.jour3"/>', '<fmt:message key="GML.jour4"/>',
          '<fmt:message key="GML.jour5"/>', '<fmt:message key="GML.jour6"/>', '<fmt:message key="GML.jour7"/>'];
        var TODAY = "<fmt:message key='GML.Today'/>";
        var TOMORROW = "<fmt:message key='GML.Tomorrow'/>";
        var SLOTS_MAX = 5;
       
        var events = <c:out value='${calendarView.eventsInJSON}' escapeXml='yes'/>;
        var slotCount = 0;
        var now = new Date();
        var nextday = new Date();
        nextday.setDate(nextday.getDate() + 1);
        
        function inTwoDigits( t )
        {
          if (t < 10) t = "0" + t;
          return t;
        }
        
        function computeIdFrom( date )
        {
          var year = date.getFullYear(), month = inTwoDigits(date.getMonth()+1), day = inTwoDigits(date.getDate());
          return year + "-" + month + "-" + day;
        }
        
        function formatDate( date )
        {
          var year = date.getFullYear(), month = inTwoDigits(date.getMonth()+1), day = inTwoDigits(date.getDate());
          return year + "/" + month + "/" + day;
        }
        
        function compareDate( date1, date2 )
        {
          if (date1.getFullYear() < date2.getFullYear() || date1.getMonth() < date2.getMonth() ||
            date1.getDate() < date2.getDate())
            return -1;
          if (date1.getFullYear() == date2.getFullYear() && date1.getMonth() == date2.getMonth() &&
            date1.getDate() == date2.getDate())
            return 0
          else return 1;
        }
        
        function startTimeOf( event ) {
          if (event.startTimeDefined)
          {
            var hours = inTwoDigits(startDate.getHours()), minutes = inTwoDigits(startDate.getMinutes());
            var startTime =  " <fmt:message key='GML.at'/> " + hours + ":" + minutes;
            return $("<span>").addClass("eventBeginHours").html(startTime);
          }
          return "";
        }
        
        function locationOf( event )
        {
          if (event.location.length > 0)
          {
            return $("<div>").addClass("eventInfo").
              append($("<div>").addClass("eventPlace").
              append($("<div>").addClass("bloc").append($("<span>").html(event.location)))).
              append($("<br>", {clear: "left"}));
          }
          return "";
        }
      
        function shortFormatOf( date )
        {
          var month = inTwoDigits(date.getMonth()+1), day = inTwoDigits(date.getDate());
          return $("<div>").addClass("eventShortDate").
            append($("<span>").addClass("number").append(day)).
            append("/").
            append($("<span>").addClass("month").append(month));
        }
      
        function longFormatOf( date )
        {
          if (compareDate(date, now) == 0)
            return $("<div>").addClass("eventLongDate").append(TODAY);
          else if (compareDate(date, nextday) == 0)
            return $("<div>").addClass("eventLongDate").append(TOMORROW);
          else
            return $("<div>").addClass("eventLongDate").
              append($("<span>").addClass("day").html(DAY_NAMES[date.getDay()] + " ")).
              append($("<span>").addClass("number").html(inTwoDigits(date.getDate()) + " ")).
              append($("<span>").addClass("month").html(MONTH_NAMES[date.getMonth()] + " ")).
              append($("<span>").addClass("year").html(date.getFullYear()));
        }
        
        function daySlotOf( event )
        {
          var id = computeIdFrom(startDate);
          var eventsInSameDay = $("#"+id);
          if (eventsInSameDay.length == 0)
          {
            eventsInSameDay = $("<li>").attr("id", id).addClass("events").
              append(shortFormatOf(startDate)).append(longFormatOf(startDate)).appendTo(listOfEvents);
            slotCount++;
          }
          if (event.priority && !eventsInSameDay.hasClass("priority"))
          {
            //id += "_priority";
            eventsInSameDay.addClass("priority");
          }
          return eventsInSameDay;
        }
        
        var listOfEvents = $("<ul>").attr("id", "eventList").addClass("eventList").appendTo(target);
        for (var i = 0; i < events.length && slotCount < SLOTS_MAX; i++) {
          var event = events[i];
          var eventCss = event.className.join(' ');
          var startDate = $.fullCalendar.parseDate(event.start);
          if (compareDate(startDate, now) < 0)
            startDate = now;
          
          daySlotOf(event).append($("<div>").attr("id", "event" + i).addClass("event " + eventCss).
            append($("<div>").addClass("eventName").
            append($("<a>", {
              "href": "javascript:viewEvent(" + event.id + ", '" + formatDate(startDate) + "' , '" + event.instanceId + "');",
              "title": "<fmt:message key='almanach.openEvent'/>"}).addClass(eventCss).html(event.title)).
            append(startTimeOf(event))).
            append(locationOf(event)));
        }
      }

      $(document).ready(function() {
        // page is now ready, initialize the calendar...
        renderNextEvents($("#calendar"));
      });
    </script>
  </head>
  <body class="portlet">

    <div id="calendar"></div>

    <form name="viewEventForm" action=""  method="POST" target="MyMain">
      <input type="hidden" name="Id"/>
      <input type="hidden" name="Date"/>
    </form>

  </body>
</html>