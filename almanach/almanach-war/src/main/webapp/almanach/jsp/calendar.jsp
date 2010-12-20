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
<c:set var="almanach" value="${requestScope.almanach}"/>
<c:set var="othersAlmanachs" value="${almanach.othersAlmanachsAsDTO}"/>
<c:set var="accessibleInstances" value="${almanach.accessibleInstances}"/>
<c:set var="events" value="${requestScope.events}" />
<c:set var="currentDay" value="${requestScope.currentDay}"/>
<c:set var="flag"><c:out value="${param['flag']}" default="user"/></c:set>
<c:set var="navigationLabel" value="${requestScope.navigationLabel}"/>

<html>
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
        <c:out value=".${almanach.instanceId} { color: ${almanach.color}; }"/>
        <c:out value=".fc-agenda .${almanach.instanceId} .fc-event-time, .${almanach.instanceId} a { background-color: ${almanach.color}; border-color: ${almanach.color}; color: white; }"/>
      </c:forEach>
    </style>
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

      function viewEvents() {
        pdcUtilizationWindow = SP_openWindow("ViewYearEventsPOPUP", "allEvents", '600', '400','scrollbars=yes,resizable,alwaysRaised');
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
      <view:operation altText="${opLabel}" icon="" action="ViewYearEvents"/>
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
    </view:operationPane>

    <view:window>
      <view:frame>
        <div id="navigation">
          <fmt:message key="almanach.icons.leftArrow" var="leftArrow" bundle="${icons}"/>
          <fmt:message key="almanach.icons.rightArrow" var="rightArrow" bundle="${icons}"/>
          <fmt:message key="auJour" var="today" />
          <div id="currentScope">
            <a href="javascript:onClick=previousMonth()"><img src="<c:url value='${leftArrow}'/>" border="0" alt="" align="top"/></a>
            <span class="txtnav"><c:out value="${navigationLabel}" /></span>
            <a href="javascript:onClick=nextMonth()"><img src="<c:url value='${rightArrow}'/>" border="0" alt="" align="top"/></a>
          </div>
          <div id="today">
            <a href="javascript:onClick=goToDay()"><c:out value="${today}" /></a>
          </div>
          <c:if test="${accessibleInstances ne null}">
            <div id="others">
              <select name="select" onchange="window.open(this.options[this.selectedIndex].value,'_self')" class="selectNS">
                <c:forEach var="instance" items="${accessibleInstances}">
                  <c:set var="componentId" value="${instance[0]}"/>
                  <c:set var="selected" value=""/>
                  <c:if test="${componentId eq instanceId}">
                    <c:set var="selected" value="selected='selected'"/>
                  </c:if>
                  <option value="<view:componentUrl componentId='${componentId}'/>Main" <c:out value="${selected}" escapeXml="false"/>><c:out value="${instance[2]} - ${instance[1]}"/></option>
                </c:forEach>
              </select>
            </div>
          </c:if>
        </div>

        <div id="calendar"></div>

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

        <c:if test="${rssUrl ne null and not empty rssUrl}">
          <table>
            <tr>
              <td><a href="<c:url value='${rssUrl}'/>"><img src="icons/rss.gif" border="0" alt="RSS"/></a></td>
            </tr>
          </table>
          <fmt:message key="almanach.rssNext" var="rssNext"/>
          <link rel="alternate" type="application/rss+xml" title="<c:out value='${componentLabel} : ${rssNext}'/>" href="<c:url value='${rssUrl}'/>"/>
        </c:if>

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
