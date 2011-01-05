<%--

    Copyright (C) 2000 - 2009 Silverpeas

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

<HTML>
  <HEAD>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <link rel='stylesheet' type='text/css' href="<c:url value='/util/styleSheets/jquery/fullcalendar.css'/>" />
    <link rel='stylesheet' type='text/css' href="<c:url value='/almanach/jsp/styleSheets/almanach.css'/>" />
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/util/javaScript/jquery/fullcalendar.min.js'/>"></script>
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

      $(document).ready(function() {

        // page is now ready, initialize the calendar...

        $('#calendar').fullCalendar({
          // put your options and callbacks here
          monthNames: ['<fmt:message key="GML.mois0"/>', '<fmt:message key="GML.mois1"/>', '<fmt:message key="GML.mois2"/>', '<fmt:message key="GML.mois3"/>',
            '<fmt:message key="GML.mois4"/>', '<fmt:message key="GML.mois5"/>', '<fmt:message key="GML.mois6"/>', '<fmt:message key="GML.mois7"/>',
            '<fmt:message key="GML.mois8"/>', '<fmt:message key="GML.mois9"/>', '<fmt:message key="GML.mois10"/>', '<fmt:message key="GML.mois11"/>'],
          dayNames: ['<fmt:message key="GML.jour1"/>', '<fmt:message key="GML.jour2"/>', '<fmt:message key="GML.jour3"/>', '<fmt:message key="GML.jour4"/>',
            '<fmt:message key="GML.jour5"/>', '<fmt:message key="GML.jour6"/>', '<fmt:message key="GML.jour7"/>'],
          dayNamesShort: ['<fmt:message key="GML.shortJour1"/>', '<fmt:message key="GML.shortJour2"/>', '<fmt:message key="GML.shortJour3"/>',
            '<fmt:message key="GML.shortJour4"/>', '<fmt:message key="GML.shortJour5"/>', '<fmt:message key="GML.shortJour6"/>', '<fmt:message key="GML.shortJour7"/>'],
          buttonText: {
            prev:     '&nbsp;&#9668;&nbsp;',  // left triangle
            next:     '&nbsp;&#9658;&nbsp;',  // right triangle
            prevYear: '&nbsp;&lt;&lt;&nbsp;', // <<
            nextYear: '&nbsp;&gt;&gt;&nbsp;', // >>
            today:    "<fmt:message key='auJour'/>",
            month:    '<fmt:message key="GML.month"/>',
            week:     '<fmt:message key="GML.week"/>',
            day:      '<fmt:message key="GML.day"/>'
          },
          minHour: 8,
          allDayText: '',
          allDayDefault: false,
          timeFormat: 'HH:mm{ - HH:mm}',
          axisFormat: 'HH:mm',
          columnFormat: { agendaWeek: 'ddd d' },
          firstDay: <c:out value='${calendarView.firstDayOfWeek -1}' />,
          defaultView: "<c:out value='${calendarView.viewType}'/>",
          dayClick: function(date, allDay, jsEvent, view) {
            var dayDate = $.fullCalendar.formatDate(date, "yyyy-MM-dd'T'HH:mm");
            clickDay(dayDate);
          },
          eventClick: function(calEvent, jsEvent, view) {
            var eventDate = $.fullCalendar.formatDate(calEvent.start, "yyyy/MM/dd");
            clickEvent(calEvent.id, eventDate, calEvent.instanceId);
          },
          events: <c:out value='${calendarView.eventsInJSON}' escapeXml='yes'/>
      <c:if test='${not calendarView.weekendVisible}'>
            , weekends: false
      </c:if>
          });

          $('#calendar').fullCalendar('gotoDate', <c:out value="${currentDay.year}"/>, <c:out value="${currentDay.month}"/>, <c:out value="${currentDay.dayOfMonth}"/>)

        });
    </script>
  </HEAD>
  <BODY MARGINHEIGHT="5" MARGINWIDTH="5" TOPMARGIN="5" LEFTMARGIN="5">
    <view:window>
      <view:frame>

        <!-- AFFICHAGE HEADER -->
        <fmt:message key="almanach.icons.leftArrow" var="leftArrow" bundle="${icons}"/>
        <fmt:message key="almanach.icons.rightArrow" var="rightArrow" bundle="${icons}"/>
        <fmt:message key="auJour" var="today" />
      <CENTER>
        <table width="98%" border="0" cellspacing="0" cellpadding="1">
          <tr>
            <td>
              <table cellpadding=0 cellspacing=0 border=0 width=50% bgcolor=000000>
                <tr>
                  <td>
                    <table cellpadding=2 cellspacing=1 border=0 width="100%" >
                      <tr>
                        <td class=intfdcolor align=center nowrap width="100%" height="24"><a href="javascript:onClick=goToDay()" onFocus="this.blur()" class=hrefComponentName><c:out value='${today}'/></a></td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </td>
            <td width="100%">
              <table cellpadding=0 cellspacing=0 border=0 width=50% bgcolor=000000>
                <tr>
                  <td>
                    <table cellpadding=0 cellspacing=1 border=0 width="100%" >
                      <tr>
                        <td class=intfdcolor><a href="javascript:onClick=previousView()" onFocus="this.blur()"><img src="<c:url value='${leftArrow}'/>" border="0"></a></td>
                        <td class=intfdcolor align=center nowrap width="100%" height="24"><span class="txtnav"><c:out value="${calendarView.label}" /></span></td>
                        <td class=intfdcolor><a href="javascript:onClick=nextView()" onFocus="this.blur()"><img src="<c:url value='${rightArrow}'/>" border="0"></a></td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </table>
        <BR/>
        <div id="calendar"></div>
      </CENTER>

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