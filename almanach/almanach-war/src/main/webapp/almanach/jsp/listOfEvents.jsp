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

<c:set var="function" value="${requestScope.Function}"/>
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
    <style type="text/css">
      <c:out value=".${instanceId} { border-color: ${calendarView.almanach.color}; color: ${calendarView.almanach.color}; }"/>
      <c:out value=".${instanceId} .fc-event-skin { background-color: ${calendarView.almanach.color}; border-color: ${calendarView.almanach.color}; color: white; }"/>
      <c:forEach var="almanach" items="${othersAlmanachs}">
        <c:out value=".${almanach.instanceId} { border-color: ${almanach.color}; color: ${almanach.color}; }"/>
        <c:out value=".${almanach.instanceId} .fc-event-skin { background-color: ${almanach.color}; border-color: ${almanach.color}; color: white; }"/>
      </c:forEach>
    </style>
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script type="text/javascript" src="<c:url value='/util/javaScript/jquery/fullcalendar.min.js'/>"></script>
    <script type="text/javascript">
      <c:if test="${calendarView.viewType.nextEventsView}">
        <!--
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
      </c:if>
        function viewEvent(id, date, componentId) {
          url = "<c:url value='/Ralmanach/'/>"+componentId+"/viewEventContent.jsp?Id="+id+"&Date="+date;
          window.open(url,'_self');
        }
        var MONTH_NAMES = ['<fmt:message key="GML.mois0"/>', '<fmt:message key="GML.mois1"/>', '<fmt:message key="GML.mois2"/>', '<fmt:message key="GML.mois3"/>',
          '<fmt:message key="GML.mois4"/>', '<fmt:message key="GML.mois5"/>', '<fmt:message key="GML.mois6"/>', '<fmt:message key="GML.mois7"/>',
          '<fmt:message key="GML.mois8"/>', '<fmt:message key="GML.mois9"/>', '<fmt:message key="GML.mois10"/>', '<fmt:message key="GML.mois11"/>'];
        var DAY_NAMES = ['<fmt:message key="GML.jour1"/>', '<fmt:message key="GML.jour2"/>', '<fmt:message key="GML.jour3"/>', '<fmt:message key="GML.jour4"/>',
          '<fmt:message key="GML.jour5"/>', '<fmt:message key="GML.jour6"/>', '<fmt:message key="GML.jour7"/>'];
        
        var monthsHavingEvents = [];
        var events = <c:out value='${calendarView.eventsInJSON}' escapeXml='yes'/>;
        
        /**
         * Formats the specified date into a string according the pattern 'yyyy/MM/dd'.
         */
        function formatDate(date) {
          var year = date.getFullYear(), month = date.getMonth()+1, day = date.getDate();
          if (month < 10) month = "0" + month;
          if (day < 10) day = "0" + day;
          return year + "/" + month + "/" + day;
        }
        
        /**
         * Formats the specified date and time into a string according the patterm 'HH:mm'.
         */
        function formatTime(time) {
          var hours = time.getHours(), minutes = time.getMinutes();
          if (hours < 10) hours = "0" + hours;
          if (minutes < 10) minutes = "0" + minutes;
          return hours + ":" + minutes;
        }

         function updateDate(dateTimeToUpdate, withDate) {
           dateTimeToUpdate.setYear(withDate.getYear());
           dateTimeToUpdate.setMonth(withDate.getMonth());
           dateTimeToUpdate.setDate(withDate.getDate())
         }
        
        /**
         * Selects the specified month by showing the events that occur in this month and by hiding
         * thoses in others specified months.
         */
        function selectMonth( elt, monthWithEvents ) {
          $(".sousNavBulle a").removeClass();
          elt.addClass("active");
          printEventsInMonth(monthWithEvents);
        }
        
        /**
         * Prints the events that occur in the specified month. The month is indicated as a string
         * with the following pattern 'NAME_OF_MONTH YEAR'.
         * The events in the others months are hidden.
         */
        function printEventsInMonth(selectedMonth) {
          $.each(monthsHavingEvents, function(index, monthWithEvents) {
            if (selectedMonth != null && monthWithEvents != selectedMonth) {
              $('#' + monthWithEvents.replace(' ', '')).hide();
            } else if (selectedMonth == null) {
              $('#' + monthWithEvents.replace(' ', '')).show();
            }
          });
          if (selectedMonth != null) $('#' + selectedMonth.replace(' ', '')).show();
        }
        
        /**
         * Builds the calendar view with the specified next events categorized by month and
         * ordered by date.
         * The months and built by browsing the events and are remembered for month selection.
         */
        function buildCalendarView( events, monthsHavingEvents ) {
          var currentMonth = -1, currentYear = -1, monthSection = null, today = new Date();
          $("#calendar").children().remove();
          $("<ul>").attr("id", "eventList").appendTo("#calendar");
          $.each(events, function(index, event) {
            var eventStartDate = $.fullCalendar.parseDate(event.start);
            var endDate = $.fullCalendar.parseDate(event.end);
            var startDate = eventStartDate;
      <c:if test="${calendarView.viewType.nextEventsView}">
            if (!event.allDay) {
              if (startDate < today) startDate.setDate(today.getDate());
            }
      </c:if>
            if (startDate.getMonth() != currentMonth || startDate.getFullYear() != currentYear) {
              currentYear = startDate.getFullYear();
              currentMonth = startDate.getMonth();
              monthSection = $("<ul>").addClass("eventList")
              .appendTo($("<li>").attr("id", MONTH_NAMES[currentMonth] + currentYear)
              .append($("<h3>").addClass("eventMonth").html(MONTH_NAMES[currentMonth] + ' ' + currentYear))
              .appendTo($('#eventList')));
              monthsHavingEvents.push(MONTH_NAMES[currentMonth] + ' ' + currentYear);
            }
            var startTime = "", endTime = "";
            if (event.startTimeDefined) {
              startTime = "<fmt:message key='GML.From'/> " + formatTime(startDate);
            }
            if (endDate.getFullYear() > startDate.getFullYear() || endDate.getMonth() > startDate.getMonth() ||
              endDate.getDate() > startDate.getDate()) {
              endTime = "<fmt:message key='GML.to'/> " + endDate.toLocaleDateString(); 
            }
            if (event.endTimeDefined) {
              endTime = endTime + " <fmt:message key='GML.at'/> " + formatTime(endDate);
            }
            var eventSection = $("<li>").attr("id", "event" + index).addClass("event " + event.className.join(' ')).click(function() {
              viewEvent(event.id, formatDate(eventStartDate), event.instanceId);
            })
            .append($("<div>").addClass("eventBeginDate")
            .append($("<span>").addClass("day").html(DAY_NAMES[startDate.getDay()]))
            .append($("<span>").addClass("number").html(startDate.getDate()))
            .append($("<span>").addClass("month").html(MONTH_NAMES[currentMonth]))
            .append($("<span>").addClass("year").html(startDate.getFullYear())))
            .append($("<h2>").addClass("eventName")
            .append($("<a>").addClass(event.className.join(' ')).attr({
              "href": "javascript:viewEvent(" + event.id + "," + formatDate(startDate) + "," + event.instanceId + ");",
              "title": "<fmt:message key='almanach.openEvent'/>"}).html(event.title))).appendTo(monthSection);
            
            if (event.location.length > 0 || startTime.length > 0 || endTime.length > 0 || 
              (event.eventURL != null && event.eventURL.length > 0)) {
              var eventInfoSection = $("<div>").addClass("eventInfo");
              if (event.location.length > 0) {
                eventInfoSection.append($("<div>").addClass("eventPlace")
                .append($("<div>").addClass("bloc")
                .append($("<span>").html(event.location))));
              }
              if (startTime.length > 0 || endTime.length > 0) {
                eventInfoSection.append($("<div>").addClass("eventDate")
                .append($("<div>").addClass("bloc")
                .append($("<span>").addClass("eventBeginDate").html(startTime))              
                .append($("<span>").addClass("eventEndDate").html(endTime))));
              }
              if (event.eventURL != null && event.eventURL.length > 0) {
                eventInfoSection.append($("<div>").addClass("eventURL")
                .append($("<div>").addClass("bloc")
                .append($("<span>")
                .append($("<a>").attr({
                  "target": "_blank",
                  "href": event.eventURL }).html("<fmt:message key='GML.linkToVisit'/>")))));
              }
              eventInfoSection.append($("<br>").attr("clear", "left")).appendTo(eventSection);
            }
            
            var eventDescSection = $("<div>").addClass("eventDesc").appendTo(eventSection);
            if (event.hasAttachments) {
              eventDescSection.append($("<div>").addClass("eventAttachments")
              .load("<c:url value='/attachment/jsp/displayAttachments.jsp?Context=Images&ComponentId=${instanceId}' />&Id=" + event.id));
            }
            eventDescSection.append($("<div>").html(event.description)).append($("<br>").addClass("clearAll"));
          });
        }
        
        /**
         * Builds the navigation by month from the specified array of months. Each element in the
         * array is a string in the form of "NAME_OF_THE_MONTH YEAR".
         * For a month selected, only the events occuring in this month are shown.
         */
        function buildNavigationByMonth( monthsHavingEvents ) {
      <c:if test='${function != "ViewMonthEvents"}'>
          var monthNavSection = $("<p>").html("<fmt:message key='GML.Show'/> ")
          .append($("<a>").attr("href", "#").addClass("active").html("<fmt:message key='GML.allMP'/>").click(function() {
            selectMonth($(this)); })).appendTo($("#navigation"));
          $.each(monthsHavingEvents, function(index, monthWithEvents) {
            $("<a>").attr("href", "#").html(monthWithEvents).click(function() {
              selectMonth($(this), monthWithEvents);
            }).appendTo(monthNavSection.append(" "));
          })
      </c:if>
        }
                
        $(document).ready(function() {

          // page is now ready, initialize the calendar...
          buildCalendarView(events, monthsHavingEvents);
          buildNavigationByMonth(monthsHavingEvents);
          
        });
        -->
    </script>
  </head>
  <body class="eventsView">
    <c:choose>
      <c:when test='${function == "ViewMonthEvents"}'>
        <fmt:message key="almanach.browsebar.monthEvents" var="currentNode"/>
        <view:browseBar path="${currentNode}"/>
      </c:when>
      <c:when test='${function == "ViewYearEvents"}'>
        <fmt:message key="almanach.browsebar.yearEvents" var="currentNode"/>
        <view:browseBar path="${currentNode}"/>
      </c:when>
    </c:choose>
    <view:operationPane>
      <c:choose>
        <c:when test="${calendarView.viewType.nextEventsView}">
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
        </c:when>
        <c:otherwise>
          <fmt:message key="GML.print" var="opLabel"/>
          <fmt:message key="almanach.icons.printEvents" var="opIcon"  bundle="${icons}"/>
          <c:url var="opIcon" value="${opIcon}"/>
          <view:operation altText="${opLabel}" icon="${opIcon}" action="javascript:onClick=window.print();"/>
        </c:otherwise>
      </c:choose>
    </view:operationPane>

    <view:window>

      <c:if test="${calendarView.viewType.nextEventsView}">
        <view:tabs>
          <fmt:message key="almanach.rssNext" var="opLabel" />
          <view:tab label="${opLabel}" action="javascript:onClick=viewNextEvents()" selected="${calendarView.viewType.nextEventsView}"/>
          <fmt:message key="GML.week" var="opLabel" />
          <view:tab label="${opLabel}" action="javascript:onClick=viewByWeek()" selected="${calendarView.viewType.weeklyView}"/>
          <fmt:message key="GML.month" var="opLabel" />
          <view:tab label="${opLabel}" action="javascript:onClick=viewByMonth()" selected="${calendarView.viewType.monthlyView}"/>
        </view:tabs>
      </c:if>

      <view:frame>
        <c:if test='${function != "ViewMonthEvents"}'>
          <div class="sousNavBulle">
            <div id="navigation">
              <c:if test="${accessibleInstances ne null and not empty accessibleInstances}">
                <div id="others">
                  <select name="select" onchange="window.open(this.options[this.selectedIndex].value,'_self')" class="selectNS">
                    <c:forEach var="instance" items="${accessibleInstances}">
                      <c:set var="componentId" value="${instance.instanceId}"/>
                      <c:set var="selected" value=""/>
                      <c:if test="${componentId eq instanceId}">
                        <c:set var="selected" value="selected='selected'"/>
                      </c:if>
                      <option value="<c:out value='${instance.url}'/>Main?view=<c:out value='${calendarView.viewType.name}'/>" <c:out value="${selected}" escapeXml="false"/>><c:out value="${instance.spaceId} - ${instance.label}"/></option>
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
                <br clear="all"/>
              </div>
            </c:if>
          </div>
        </c:if>

        <div id="calendar"></div>

        <c:choose>
          <c:when test="${calendarView.viewType.nextEventsView}">
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

            <form name="almanachForm" action="almanach.jsp" method="post">
              <input type="hidden" name="Action"/>
              <input type="hidden" name="Id"/>
            </form>

            <form name="eventForm" action="createEvent.jsp" method="post">
              <input type="hidden" name="Day"/>
            </form>
          </c:when>
          <c:otherwise>
            <center>
              <fmt:message key="GML.back" var="comeBack"/>
              <view:button action="almanach.jsp" label="${comeBack}"/>
            </center>
          </c:otherwise>
        </c:choose>

      </view:frame>
    </view:window>

    <view:progressMessage/>
  </body>
</html>
