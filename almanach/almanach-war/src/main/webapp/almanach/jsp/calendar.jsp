<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ page import="com.stratelia.webactiv.almanach.control.AlmanachPdfGenerator"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%
  response.setHeader("Cache-Control", "no-store"); //HTTP 1.1
  response.setHeader("Pragma", "no-cache"); //HTTP 1.0
  response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="componentLabel" value="${browseContext[1]}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>

<c:set var="rssUrl" value="${requestScope.RSSUrl}"/>
<c:set var="almanachUrl" value="${requestScope.almanachURL}"/>
<c:set var="calendarView" value="${requestScope.calendarView}"/>
<c:set var="othersAlmanachs" value="${requestScope.othersAlmanachs}"/>
<c:set var="accessibleInstances" value="${requestScope.accessibleInstances}"/>
<c:set var="currentDay" value="${calendarView.currentDay}"/>
<c:set var="flag"><c:out value="${param['flag']}" default="user"/></c:set>
<fmt:message key="auJour" var="today" />
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel />
    <c:if test="${rssUrl ne null and not empty rssUrl}">
      <link rel="alternate" type="application/rss+xml" title="<c:out value='${componentLabel}'/> : <fmt:message key='almanach.rssNext'/>" href="<c:url value='${rssUrl}'/>"/>
    </c:if>
    <link rel='stylesheet' type='text/css' href="<c:url value='/util/styleSheets/jquery/fullcalendar.css'/>" />
    <link rel='stylesheet' type='text/css' href="<c:url value='/almanach/jsp/styleSheets/almanach.css'/>" />
    <style type="text/css">
      <c:forEach var="almanach" items="${othersAlmanachs}">
        <c:out value=".${almanach.instanceId} { border-color: ${almanach.color}; color: ${almanach.color}; }"/>
        <c:out value=".fc-agenda .${almanach.instanceId} .fc-event-time, .${almanach.instanceId} a { background-color: ${almanach.color}; border-color: ${almanach.color}; color: white; }"/>
      </c:forEach>
    </style>
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/util/javaScript/jquery/fullcalendar.min.js'/>"></script>
    <script type="text/javascript">

      function viewByMonth()
      {
        document.almanachForm.Action.value = "ViewByMonth";
        document.almanachForm.submit();
      }

      function viewByWeek()
      {
        document.almanachForm.Action.value = "ViewByWeek";
        document.almanachForm.submit();
      }

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

      function openSPWindow(fonction, windowName){
        pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '450','scrollbars=yes, resizable, alwaysRaised');
      }

      function viewEvent(id, date, componentId)
      {
        url = "<c:url value='/Ralmanach/'/>"+componentId+"/viewEventContent.jsp?Id="+id+"&Date="+date;
        window.open(url,'_self');
      }

      function addEvent(day)
      {
        document.eventForm.Day.value = day;
        document.eventForm.submit();
      }

      function printPdf(view)
      {
        window.open(view, "PdfGeneration", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
      }

      function exportICal() {
        SP_openWindow('exportToICal','iCalExport','500','230','scrollbars=no, noresize, alwaysRaised');
      }

      function viewEvents() {
        pdcUtilizationWindow = SP_openWindow("ViewYearEventsPOPUP", "allEvents", '600', '400','scrollbars=yes,resizable,alwaysRaised');
      }

      function exportIcal() {
        SP_openWindow("ToExportICal", "ToExportICal",'500','230','scrollbars=no, noresize, alwaysRaised');
      }

      <c:if test="${almanach.agregationUsed}">
        var actionAll = false;
        function updateAgregation(i)
        {
          if (document.agregateAlmanachs.chk_allalmanach)
          {
            newState = document.agregateAlmanachs.chk_allalmanach.checked;
            //Avoid too much submit for each checkbox (trigger onClick) if click on all checkbox
            if (!actionAll)
            {
              document.agregateAlmanachs.action = "UpdateAgregation";
              document.agregateAlmanachs.submit();
            }
          }
          else
          {
            document.agregateAlmanachs.action = "UpdateAgregation";
            document.agregateAlmanachs.submit();
          }
        }

        function agregateAll()
        {
          myForm = document.agregateAlmanachs;
          var newState = true;
          if (myForm.chk_allalmanach)
            newState = myForm.chk_allalmanach.checked;

          if (myForm.chk_almanach.length == null)
          {
            myForm.chk_almanach.checked = true;
          }
          else
          {
            for (i=0; i<myForm.chk_almanach.length; i++)
            {
              if (newState && !myForm.chk_almanach[i].checked)
                myForm.chk_almanach[i].checked = true;
              else if (!newState && myForm.chk_almanach[i].checked)
                myForm.chk_almanach[i].checked = false;
            }
          }
          document.agregateAlmanachs.action = "UpdateAgregation";
          document.agregateAlmanachs.submit();
        }
      </c:if>

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
              today:    "${today}",
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
            firstDay: <c:out value='${calendarView.firstDayOfWeek - 1}' />,
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
  </head>
  <body>
    <view:operationPane>
      <c:if test='${flag eq "admin" and almanach.pdcUsed}'>
        <fmt:message key="GML.PDCParam" var="opLabel" />
        <fmt:message key="almanach.icons.paramPdc" var="opIcon" bundle="${icons}"/>
        <c:url var="opIcon" value="${opIcon}"/>
        <c:set var="openPdc">
          javascript:onClick=openSPWindow('<c:url value="/RpdcUtilization/jsp/Main?ComponentId=${instanceId}"/>','utilizationPdc1')
        </c:set>
        <view:operation altText="${opLabel}" icon="${opIcon}" action="${openPdc}"/>
      </c:if>
      <c:if test="${flag eq 'admin' or flag eq 'publisher'}">
        <fmt:message key="creerEvenement" var="opLabel" />
        <fmt:message key="almanach.icons.addEvent" var="opIcon" bundle="${icons}"/>
        <c:url var="opIcon" value="${opIcon}"/>
        <view:operation altText="${opLabel}" icon="${opIcon}" action="javascript:onClick=addEvent('')"/>
        <view:operationSeparator/>
      </c:if>

      <fmt:message key="almanach.action.monthEvents" var="opLabel"/>
      <fmt:message key="almanach.icons.printEvents" var="opIcon" bundle="${icons}"/>
      <c:url var="opIcon" value="${opIcon}"/>
      <view:operation altText="${opLabel}" icon="${opIcon}" action="ViewMonthEvents"/>

      <fmt:message key="almanach.action.yearEvents" var="opLabel"/>
      <view:operation altText="${opLabel}" icon="" action="ViewYearEvents"/>

      <c:set var="opLabel"><fmt:message key="almanach.action.yearEvents"/> <fmt:message key="almanach.popup"/></c:set>
      <view:operation altText="${opLabel}" icon="" action="javascript:viewEvents()"/>
      <view:operationSeparator/>

      <fmt:message key="genererPdfMoisComplet" var="opLabel"/>
      <fmt:message key="almanach.icons.exportCalendarToPDF" var="opIcon" bundle="${icons}"/>
      <c:set var="opAction"><%= AlmanachPdfGenerator.PDF_MONTH_ALLDAYS%></c:set>
      <c:url var="opIcon" value="${opIcon}"/>
      <view:operation altText="${opLabel}" icon="${opIcon}" action="javascript:onClick=printPdf('${opAction}')"/>

      <fmt:message key="genererPdfJourEvenement" var="opLabel"/>
      <fmt:message key="almanach.icons.exportEventsToPDF" var="opIcon" bundle="${icons}"/>
      <c:set var="opAction"><%= AlmanachPdfGenerator.PDF_MONTH_EVENTSONLY%></c:set>
      <c:url var="opIcon" value="${opIcon}"/>
      <view:operation altText="${opLabel}" icon="${opIcon}" action="javascript:onClick=printPdf('${opAction}')"/>

      <fmt:message key="almanach.genererPdfAnnee" var="opLabel"/>
      <fmt:message key="almanach.icons.exportCalendarToPDF" var="opIcon"  bundle="${icons}"/>
      <c:set var="opAction"><%= AlmanachPdfGenerator.PDF_YEAR_EVENTSONLY%></c:set>
      <c:url var="opIcon" value="${opIcon}"/>
      <view:operation altText="${opLabel}" icon="${opIcon}" action="javascript:onClick=printPdf('${opAction}')"/>
      <view:operationSeparator/>

      <fmt:message key="almanach.exportToIcal" var="opLabel"/>
      <fmt:message key="almanach.icons.exportToICal" var="opIcon"  bundle="${icons}"/>
      <c:url var="opIcon" value="${opIcon}"/>
      <view:operation altText="${opLabel}" icon="${opIcon}" action="javascript:onClick=exportICal();"/>
    </view:operationPane>

    <view:window>

      <view:tabs>
        <fmt:message key="GML.week" var="opLabel" />
        <view:tab label="${opLabel}" action="javascript:onClick=viewByWeek()" selected="${calendarView.viewType.weeklyView}"/>
        <fmt:message key="GML.month" var="opLabel" />
        <view:tab label="${opLabel}" action="javascript:onClick=viewByMonth()" selected="${calendarView.viewType.monthlyView}"/>
      </view:tabs>

      <view:frame>
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
          <c:if test="${accessibleInstances ne null}">
            <div id="others">
              <select name="select" onchange="window.open(this.options[this.selectedIndex].value,'_self')" class="selectNS">
                <c:forEach var="instance" items="${accessibleInstances}">
                  <c:set var="componentId" value="${instance.instanceId}"/>
                  <c:set var="selected" value=""/>
                  <c:if test="${componentId eq instanceId}">
                    <c:set var="selected" value="selected='selected'"/>
                  </c:if>
                  <option value="<c:out value='${instance.url}'/>Main" <c:out value="${selected}" escapeXml="false"/>><c:out value="${instance.spaceId} - ${instance.label}"/></option>
                </c:forEach>
              </select>
            </div>
          </c:if>
        </div>

        <c:if test="${almanach.agregationUsed and not empty othersAlmanachs}">
          <div id="agregatedAlmanachs">
            <form name="agregateAlmanachs">
              <table><tr><td><fmt:message key="otherAlmanachEvents"/></td>
                  <c:forEach var="i" begin="0" end="${fn:length(othersAlmanachs) - 1}" step="1">
                    <c:set var="otherAlmanach" value="${othersAlmanachs[i]}"/>
                    <c:set var="checked" value=""/>
                    <c:if test="${otherAlmanach.agregated}">
                      <c:set var="checked" value="checked"/>
                    </c:if>
                    <c:if test="${i % 5 eq 0 and i >= 5}">
                    </tr><tr><td>&nbsp;</td>
                    </c:if>
                    <td>
                      <input onclick="updateAgregation(<c:out value='${i}'/>)" type="checkbox" name="chk_almanach" <c:out value='${checked}'/> value="<c:out value='${otherAlmanach.instanceId}'/>"/>
                    </td>
                    <td>
                      <a class="almanach" href="<c:url value='/Ralmanach/${otherAlmanach.instanceId}/Main'/>">
                        <span class="<c:out value='${otherAlmanach.instanceId}'/>"><b><c:out value='${otherAlmanach.label}'/></b></span>
                      </a>
                    </td>
                    <td>&nbsp;</td>
                  </c:forEach>
                  <c:if test="${fn:length(othersAlmanachs) gt 1}">
                    <c:set var="checked" value=""/>
                    <c:if test="${fn:length(othersAlmanachs) eq almanach.agregatedAlmanachsCount}">
                      <c:set var="checked" value="checked"/>
                    </c:if>
                    <td><input onClick="javascript: agregateAll();" <c:out value="${checked}"/> name="chk_allalmanach" type="checkbox"/></td>
                    <td><b><fmt:message key="allAlmanachs"/></b></td>
                  </c:if>
                </tr>
              </table>
            </form>
          </div>
        </c:if>

        <div id="calendar"></div>

        <div class="rss">
          <table>
            <tr>
              <c:if test="${rssUrl ne null and not empty rssUrl}">
                <td>
                  <a href="<c:url value='${rssUrl}'/>" class="rss_link"><img src="<c:url value="/util/icons/rss.gif" />" border="0" alt="rss"/></a>
                  <fmt:message key="almanach.rssNext" var="rssNext"/>
                  <link rel="alternate" type="application/rss+xml" title="<c:out value='${componentLabel} : ${rssNext}'/>" href="<c:url value='${rssUrl}'/>"/>
                </td>
              </c:if>
              <td>
                <fmt:message key='almanach.ical.subscribe' var="icsTitle"/>
                <a href="<c:url value='${almanachUrl}'/>" title="<c:out value='${icsTitle}'/>"><img align="top" src="icons/ical.gif" border="0" alt=""/></a>
              </td>
            </tr>
          </table>
        </div>

      </view:frame>
    </view:window>

    <form name="almanachForm" action="almanach.jsp" method="post">
      <input type="hidden" name="Action"/>
      <input type="hidden" name="Id"/>
    </form>

    <form name="eventForm" action="createEvent.jsp" method="post">
      <input type="hidden" name="Day"/>
    </form>
  </body>
</html>
