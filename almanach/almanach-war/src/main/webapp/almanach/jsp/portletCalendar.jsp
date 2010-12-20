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
<c:set var="almanach" value="${requestScope.almanach}"/>
<c:set var="events" value="${requestScope.events}" />
<c:set var="currentDay" value="${requestScope.currentDay}"/>
<c:set var="navigationLabel" value="${requestScope.navigationLabel}"/>

<HTML>
  <HEAD>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <link rel='stylesheet' type='text/css' href="<c:url value='/util/styleSheets/jquery/fullcalendar.css'/>" />
    <link rel='stylesheet' type='text/css' href="<c:url value='/almanach/jsp/styleSheets/almanach.css'/>" />
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/util/javaScript/jquery/fullcalendar.min.js'/>"></script>
    <script type="text/javascript">
      function nextMonth()
      {
        document.almanachForm.Action.value = "NextMonth";
        document.almanachForm.submit();
      }

      function previousMonth()
      {
        document.almanachForm.Action.value = "PreviousMonth";
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
          monthNames: ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet',
            'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'],
          dayNames: ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'],
          dayNamesShort: ['Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'],
          buttonText: {
            prev:     '&nbsp;&#9668;&nbsp;',  // left triangle
            next:     '&nbsp;&#9658;&nbsp;',  // right triangle
            prevYear: '&nbsp;&lt;&lt;&nbsp;', // <<
            nextYear: '&nbsp;&gt;&gt;&nbsp;', // >>
            today:    "Aujourd'hui",
            month:    'Moi',
            week:     'Semaine',
            day:      'Journée'
          },
          eventClick: function(calEvent, jsEvent, view) {
            var date = calEvent.start.getFullYear() + '/';
            if (calEvent.start.getMonth() < 10) date = date + '0' + (calEvent.start.getMonth() + 1) + '/';
            else date = date + (calEvent.start.getMonth() + 1) + '/';
            if (calEvent.start.getDate() < 10) date = date + '0' + calEvent.start.getDate() + '/';
            else date = date + calEvent.start.getDate();
            clickEvent(calEvent.id, date, calEvent.instanceId);
          },
          events: <c:out value='${events}' escapeXml='yes'/>
      <c:if test='${almanach.weekendNotVisible}'>
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
                          <td class=intfdcolor><a href="javascript:onClick=previousMonth()" onFocus="this.blur()"><img src="<c:url value='${leftArrow}'/>" border="0"></a></td>
                          <td class=intfdcolor align=center nowrap width="100%" height="24"><span class="txtnav"><c:out value="${navigationLabel}" /></span></td>
                          <td class=intfdcolor><a href="javascript:onClick=nextMonth()" onFocus="this.blur()"><img src="<c:url value='${rightArrow}'/>" border="0"></a></td>
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
    <form name="almanachForm" action="../..<c:out value='${almanach.componentUrl}'/>almanach.jsp"  method="POST" target="MyMain">
      <input type="hidden" name="Action"/>
      <input type="hidden" name="Id"/>
    </form>

    <form name="viewEventForm" action=""  method="POST" target="MyMain">
      <input type="hidden" name="Id"/>
      <input type="hidden" name="Date"/>
    </form>

    <form name="createEventForm" action="../..<c:out value='${almanach.componentUrl}'/>createEvent.jsp" method="POST" target="MyMain">
      <input type="hidden" name="Day"/>
    </form>

  </body>
</html>