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
    <link rel='stylesheet' type='text/css' href="<c:url value='/almanach/jsp/styleSheets/almanach.css'/>" />
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script type="text/javascript">

      function viewByMonth()
      {
        document.almanachForm.Action.value = "ViewByMonth";
        $.progressMessage();
        document.almanachForm.submit();
      }

      function viewByWeek()
      {
        document.almanachForm.Action.value = "ViewByWeek";
        $.progressMessage();
        document.almanachForm.submit();
      }
      
      function viewNextEvents()
      {
        document.almanachForm.Action.value = "ViewNextEvents";
        $.progressMessage();
        document.almanachForm.submit();
      }

      function openSPWindow(fonction, windowName){
        pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '450','scrollbars=yes, resizable, alwaysRaised');
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
              $.progressMessage();
              document.agregateAlmanachs.submit();
            }
          }
          else
          {
            document.agregateAlmanachs.action = "UpdateAgregation";
            $.progressMessage();
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
          $.progressMessage();
          document.agregateAlmanachs.submit();
        }
      </c:if>
        
        var monthNames = ['<fmt:message key="GML.mois0"/>', '<fmt:message key="GML.mois1"/>', '<fmt:message key="GML.mois2"/>', '<fmt:message key="GML.mois3"/>',
              '<fmt:message key="GML.mois4"/>', '<fmt:message key="GML.mois5"/>', '<fmt:message key="GML.mois6"/>', '<fmt:message key="GML.mois7"/>',
              '<fmt:message key="GML.mois8"/>', '<fmt:message key="GML.mois9"/>', '<fmt:message key="GML.mois10"/>', '<fmt:message key="GML.mois11"/>'];
        var dayNames = ['<fmt:message key="GML.jour1"/>', '<fmt:message key="GML.jour2"/>', '<fmt:message key="GML.jour3"/>', '<fmt:message key="GML.jour4"/>',
              '<fmt:message key="GML.jour5"/>', '<fmt:message key="GML.jour6"/>', '<fmt:message key="GML.jour7"/>'];
                
        $(document).ready(function() {

          // page is now ready, initialize the calendar...

          var events = <c:out value='${calendarView.eventsInJSON}' escapeXml='yes'/>;
          var currentMonth = -1;
          var monthSection = null;
          $.each(events, function(index, event) {
            // the Date object doesn't support currently the ISO-8661 date format with timezone,
            // so remove the timezone part of the date before converting it to a Date object
            var startDate = new Date(event.start.replace(/\+\d+/g, ""));
            var endDate = new Date(event.end.replace(/\+\d+/g, ""));
            if (startDate.getMonth() != currentMonth) {
              var currentYear = startDate.getYear();
              currentMonth = startDate.getMonth();
              monthSection = $("<div id='eventsInMonth" + (currentMonth + 1) + "InYear" + currentYear + "' class='monthInYear'")
                .append($("<span class='title'>").html(monthNames[currentMonth] + ' ' + currentYear)).appendTo($('#calendar'));
            }
            var time = "<fmt:message key='GML.From'/> " + startDate.getHours() + ":" + startDate.getMinutes();
            if (endDate.getFullYear() > startDate.getFullYear() || endDate.getMonth() > startDate.getMonth() ||
              endDate.getDate() > startDate.getDate()) {
              time = time + " <fmt:message key='GML.to'/> " + endDate.toLocaleString(); 
            }
            time = time + " <fmt:message key='GML.at'/> " + endDate.getHours() + ":" + startDate.getMinutes();
            var url = "";
            if (event.url != null) {
              url = "<a href='" + event.url + "' alt='URL of the event'/>";
            }
            $("<div id='event" + event.id + "'").addClass("event").addClass(event.className)
              .append($("<div class='date'>")
                .append($("<span class='month'>").html(monthNames[currentMonth]))
                .append($("<span class='date'>").html(startDate.getDate()))
                .append($("<span class='day'>").html(dayNames[startDate.getDay()])))
              .append($("<span class='time'>").html(time))
              .append($("<span class='location'>").html(event.location))
              .append($("<span class='title'>").html(event.title))
              .append($("<span class='description'").html(event.description))
              .append($("<span class='url'>").html(event.url)).appendTo(monthSection);
          });
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
        <fmt:message key="almanach.rssNext" var="opLabel" />
        <view:tab label="${opLabel}" action="javascript:onClick=viewNextEvents()" selected="${calendarView.viewType.nextEventsView}"/>
      </view:tabs>

      <view:frame>
        <div id="navigation">

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
            <br clear="all"/>
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
        
    <view:progressMessage/>
  </body>
</html>
