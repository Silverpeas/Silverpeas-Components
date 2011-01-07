<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%-- Include tag library --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="ctxPath" value="${pageContext.request.contextPath}" />
<%-- Set resource bundle --%>
<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="parentTaskId" value="" />
<c:if test="${not empty(requestScope['ActionMere'])}">
  <c:set var="parentTask" value="${requestScope['ActionMere']}"></c:set>
  <c:set var="parentTaskId" value="${parentTask.id}" />
</c:if>

<c:set var="viewMode" value="${requestScope['ViewMode']}"/>

<%
TaskDetail actionMere   = (TaskDetail) request.getAttribute("ActionMere");
TaskDetail oldest     = (TaskDetail) request.getAttribute("OldestAction");
String     role = (String) request.getAttribute("Role");
Date       startDate  = (Date) request.getAttribute("StartDate");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel />
<link href="<c:out value="${ctxPath}"/>/projectManager/jsp/css/gantt.css" type="text/css" rel="stylesheet" />
<script type="text/javascript" src="<c:out value="${ctxPath}"/>/util/javaScript/animation.js"></script>
<script language="javascript" type="text/javascript">

$(document).ready(function(){

  var triAll = true;
  $(".linkTri").click(function() {
    // Lien Toutes        
    if (this.id=='link_all')
    {
      $('.task_row').css('display','');
      $('.under_task').css('display','');
      $(".linkTri").removeClass('active');
      triAll = true;
      $(this).addClass('active');
    }else {
      var decoche = $(this).is('.active');
      
      // Lien decocher    
      if(decoche){

        var divACacher = this.id.substring(5);
        $('.'+divACacher).css('display','none');
        $(this).removeClass('active');
      // Lien cocher  
      }else {
        if(triAll){
          $('.task_row').css('display','none');
          $('.under_task').css('display','none');
          triAll = false;
          $('#link_all').removeClass('active');
        }
        var divAAfficher = this.id.substring(5);
        $('.'+divAAfficher).css('display','');
        $(this).addClass('active');
      }
    }
  });

  // By suppling no content attribute, the library uses each elements title attribute by default
  $('.task_wording a[href][title]').qtip({
    content: {
       text: false // Use each elements title attribute
    }
    ,style: {
            border: {
              width: 5,
              radius: 5
           },
           padding: 7, 
           textAlign: 'center',
           tip: true, 
           name: 'green' 
        }
    ,position: {
      adjust: { screen: true }
      /*corner: {
         target: 'topMiddle',
         tooltip: 'bottomMiddle'
      }*/
    }
  });

  $('#legendLabelId').click(function() {
	    if ($('#legende').is(':visible')) {
	        $('#legende').hide();
	    } else {
	        $('#legende').show();
	    }
  });

});

</script>
</head>
<body id="gantt">
<fmt:message key="projectManager.gantt.label" var="browseBarLabel"></fmt:message>
<view:browseBar>
  <view:browseBarElt label="${browseBarLabel}" link="ToGantt?viewMode=${viewMode}" />
  <c:if test="${not empty(parentTask)}">
    <view:browseBarElt label="${parentTask.nom}" link="ToGantt?viewMode=${viewMode}&id=${parentTask.id}"/>
  </c:if>
</view:browseBar>
<view:window>
<fmt:message key="projectManager.Projet" var="pmProject"  />
<fmt:message key="projectManager.Taches" var="pmTasks"  />
<fmt:message key="projectManager.Taches" var="pmAttachments" />
<fmt:message key="projectManager.Commentaires" var="pmComments"  />
<fmt:message key="projectManager.Gantt" var="pmGantt"  />
<fmt:message key="projectManager.Calendrier" var="pmCalendar"  />
<view:tabs>
  <view:tab label="${pmProject}" selected="false" action="ToProject"></view:tab>
  <view:tab label="${pmTasks}" selected="false" action="Main"></view:tab>
  <c:if test="${fn:contains(requestScope['Role'],'admin')}">
    <view:tab label="<%=resource.getString("GML.attachments")%>" selected="false" action="ToAttachments"></view:tab>
  </c:if>
  <view:tab label="${pmComments}" selected="false" action="ToComments"></view:tab>
  <view:tab label="${pmGantt}" selected="true" action="#"></view:tab>
  <c:if test="${fn:contains(requestScope['Role'],'admin')}">
    <view:tab label="${pmCalendar}" selected="false" action="ToCalendar"></view:tab>
  </c:if>
</view:tabs>
<view:frame>
<!-- sousNavBulle -->
<div class="sousNavBulle">

  <p id="navTemporelle">
    <fmt:message key="projectManager.gantt.view" />
    <c:set var="monthClass" value="" />
    <c:set var="quarterClass" value="" />
    <c:set var="yearClass" value="" />
    <c:choose>
      <c:when test="${fn:contains(requestScope['ViewMode'], 'year')}">
        <c:set var="yearClass" value="active" />
      </c:when>
      <c:when test="${fn:contains(requestScope['ViewMode'], 'quarter')}">
        <c:set var="quarterClass" value="active" />
      </c:when>
      <c:otherwise>
        <c:set var="monthClass" value="active" />
      </c:otherwise>
    </c:choose>
    <!-- TODO add url parameter in order to distinguish each one -->
    <a href="ToGantt?viewMode=month" class="<c:out value="${monthClass}" />"><fmt:message key="projectManager.gantt.view.month" /></a>  
    <a href="ToGantt?viewMode=quarter" class="<c:out value="${quarterClass}" />"><fmt:message key="projectManager.gantt.view.quarter" /></a>  
    <a href="ToGantt?viewMode=year" class="<c:out value="${yearClass}" />"><fmt:message key="projectManager.gantt.view.year" /></a>  
  </p>
   
  <p style="display:none"><fmt:message key="projectManager.gantt.view.tasks" />  
    <!-- sousNavCheck -->
    <span class="sousNavCheck">
      <!-- TODO add url parameter in order to distinguish each one -->
      <a href="#" id="link_current" class="linkTri"><fmt:message key="projectManager.gantt.view.tasks.inprogress" /></a>  
      <a href="#" id="link_close" class="linkTri"><fmt:message key="projectManager.gantt.view.tasks.done" /></a>  
      <a href="#" id="link_notStarted" class="linkTri"><fmt:message key="projectManager.gantt.view.tasks.notstarted" /></a>  
      <a href="#" id="link_frost" class="linkTri"><fmt:message key="projectManager.gantt.view.tasks.frozen" /></a>  
      <a href="#" id="link_lost" class="linkTri"><fmt:message key="projectManager.gantt.view.tasks.cancel" /></a> 
      <a href="#" id="link_warning" class="linkTri"><fmt:message key="projectManager.gantt.view.tasks.alert" /></a>
           -
    </span>
    <a href="#" id="link_all" class="linkTri active"><fmt:message key="projectManager.gantt.view.tasks.all" /></a>
    <!-- /sousNavCheck -->
  </p>
</div>
<!-- /sousNavBulle -->

<%-- Import Legend --%>
<c:import url="gantt_legend.jsp" />
  
<%
  Date actionStartDate  = new Date();
  Date actionEndDate    = null;
  
  if (actionMere != null)
  {
    actionStartDate   = actionMere.getDateDebut();
    actionEndDate   = actionMere.getDateFin();
  }
  if (oldest != null)
  {
    actionStartDate   = oldest.getDateDebut();
    actionEndDate   = oldest.getDateFin();
  }
  
  if (startDate==null) {
    startDate = actionStartDate;
  }

  Calendar actionStartCalendar = new GregorianCalendar(1980, 1 , 1);
  Calendar actionEndCalendar = new GregorianCalendar(1980, 1 , 1);
  Calendar startCalendar = new GregorianCalendar(1980, 1 , 1);
  Calendar endCalendar = new GregorianCalendar(1980, 1 , 1);
  Calendar nextStartCalendar = new GregorianCalendar(1980, 1 , 1);
  Calendar lastStartCalendar = new GregorianCalendar(1980, 1 , 1);

  // If start or end date is not defined ==> let the dates at 1/1/1980
  if (actionStartDate != null && actionEndDate != null) {
    actionStartCalendar.setTime(actionStartDate);
    actionEndCalendar.setTime(actionEndDate);
  }

  startCalendar.setTime(startDate);
  startCalendar.set(GregorianCalendar.DATE, 1);
  
  endCalendar.setTime(startCalendar.getTime());
  endCalendar.add(GregorianCalendar.MONTH, 1);
  endCalendar.add(GregorianCalendar.DATE, -1);
  
  nextStartCalendar.setTime(startCalendar.getTime());
  nextStartCalendar.add(GregorianCalendar.MONTH, 1);
  
  lastStartCalendar.setTime(startCalendar.getTime());
  lastStartCalendar.add(GregorianCalendar.MONTH, -1);
  
  String dispStartDate = resource.getOutputDate(startCalendar.getTime());
  String dispEndDate = resource.getOutputDate(endCalendar.getTime());
  String strNextStartDate = resource.getInputDate(nextStartCalendar.getTime());
  String strLastStartDate = resource.getInputDate(lastStartCalendar.getTime());
  
  int nbDaysDisplayed = endCalendar.get(GregorianCalendar.DATE)-startCalendar.get(GregorianCalendar.DATE);
  nbDaysDisplayed = startCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
%>

<%-- -------------------------------------------------------------------------- --%>
<%--                          YEAR VIEW                          --%>
<%-- -------------------------------------------------------------------------- --%>
<c:set var="months" value="${requestScope['MonthsVO']}" />

<%-- Prepare totalDays variable --%>
<c:set var="totalDays" value="0"/>
<c:forEach var="curMonth" items="${months}">
  <c:set var="totalDays" value="${totalDays + curMonth.nbDays}" />
</c:forEach>


<table width="100%" border="0" cellspacing="0" cellpadding="0" class="tableFrame">
  <tr>
    <td colspan="3" class="hautFrame">
      <table class="ganttMois" width="100%" border="0" cellspacing="0" cellpadding="0">
        <thead>
          <!-- Display month link -->
          <tr>
            <td colspan="4" class="noBorder"></td>
            <td colspan="<c:out value="${totalDays}" />" id="month_nav" >
               <a title="mois précédent" href="ToGantt?viewMode=<c:out value="${viewMode}" />&Id=<c:out value="${parentTaskId}"/>&StartDate=<%=strLastStartDate%>"><img alt="&lt;&lt;" src="<%=resource.getIcon("projectManager.gauche")%>" /></a>
               <span><fmt:message  key="projectManager.gantt.view.quarter.year" /></span>&nbsp;<fmt:formatDate value="${requestScope['StartDate']}" pattern="MMMMMMMMMM yyyy" /> 
               <a title="mois suivant" href="ToGantt?viewMode=<c:out value="${viewMode}" />&Id=<c:out value="${parentTaskId}"/>&StartDate=<%=strNextStartDate%>"><img alt="&gt;&gt;" src="<%=resource.getIcon("projectManager.droite")%>" /></a>
            </td>
          </tr>
          <!-- Display Month name -->
          <tr id="week_number">
            <td colspan="4" class="noBorder" width="20%">&nbsp;</td>
<c:forEach var="curMonth" items="${months}" varStatus="monthIndex">
<%-- ${fn:length(curMonth.weeks)} --%>
            <td colspan="<c:out value="${curMonth.nbDays}" />" class="month_begin"><fmt:formatDate value="${curMonth.weeks[0].days[0].day}" pattern="MMMMMMMMMM yyyy" /></td>
</c:forEach>
          </tr>

          <!-- Display week number line -->
          <tr id="day_number">
            <td>&nbsp;</td>
            <td width="100px" height="20" class="task_wording"><fmt:message  key="projectManager.Taches" /></td>
            <td width="15px" colspan="2" class="state" ><fmt:message  key="projectManager.gantt.tasks.state" /></td>
<c:forEach var="curMonth" items="${months}" varStatus="monthIndex">
  <c:forEach var="curWeek" items="${curMonth.weeks}" varStatus="weekIndex">
            <c:set var="columnClass" value="week_number" />
            <c:if test="${(curWeek.number%2) == 0}">
              <c:set var="columnClass" value="${columnClass} day_odd" />
            </c:if>
            <c:if test="${(weekIndex.first)}">
              <c:set var="columnClass" value="${columnClass} month_begin"/>
            </c:if>
            <c:set var="curWeekWidth" value="${fn:length(curWeek.days)}"/>
            <td colspan="${curWeekWidth}" class="<c:out value="${columnClass}"/>"> <%--width="<c:out value="${curWeekWidth}"/>%" --%>
            <c:choose>
              <c:when test="${fn:length(curWeek.days) > 3}">
                <c:out value="${curWeek.number}" />
              </c:when>
              <c:otherwise>&nbsp;
              </c:otherwise>
            </c:choose>
            </td>
  </c:forEach>
</c:forEach>
          </tr>
        </thead>
          <tbody>
<c:set var="curTasks" value="${requestScope['Tasks']}" />
<c:forEach items="${curTasks}" var="task" varStatus="taskIndex">
  <%-- Prepare table row class BEGIN --%>
  <c:set var="taskStatus" value="" />
  <c:set var="rowClass" value="" />
  <c:set var="taskStatus" value="" />
  <c:choose>
    <c:when test="${task.statut == 0}">
      <!-- IN_PROGRESS task -->
      <c:set var="taskStatus" value="in_progress" />
      <fmt:message var="taskStatusStr" key="projectManager.gantt.view.tasks.inprogress" ></fmt:message>
    </c:when>
    <c:when test="${task.statut == 1}">
      <!-- FROZEN task -->
      <c:set var="taskStatus" value="frost" />
      <fmt:message var="taskStatusStr" key="projectManager.gantt.view.tasks.frozen" ></fmt:message>
    </c:when>
    <c:when test="${task.statut == 2}">
      <!-- STOPPED task -->
      <c:set var="taskStatus" value="lost" />
      <fmt:message var="taskStatusStr" key="projectManager.gantt.view.tasks.cancel" ></fmt:message>
    </c:when>
    <c:when test="${task.statut == 3}">
      <!-- DONE task -->
      <c:set var="taskStatus" value="done" />
      <fmt:message var="taskStatusStr" key="projectManager.gantt.view.tasks.done" ></fmt:message>
    </c:when>
    <c:when test="${task.statut == 4}">
      <!-- ALERT task -->
      <c:set var="taskStatus" value="warning" />
      <fmt:message var="taskStatusStr" key="projectManager.gantt.view.tasks.alert" ></fmt:message>
    </c:when>
    <c:otherwise>
      <!-- NOT_STARTED task -->
      <c:set var="taskStatus" value="not_started" />
      <fmt:message var="taskStatusStr" key="projectManager.gantt.view.tasks.notstarted" ></fmt:message>
    </c:otherwise>
  </c:choose>
  <c:set var="rowClass" value="${taskStatus}" />
  <c:if test="${task.level > 0}">
    <c:set var="rowClass" value="${rowClass} under_task last_child" />
  </c:if>
  <c:choose>
    <c:when test="${task.estDecomposee == 1}">
      <c:set var="rowClass" value="${rowClass} level${task.level}" />
    </c:when>
    <c:otherwise>
      <c:set var="rowClass" value="task_row ${rowClass}" />
    </c:otherwise>
  </c:choose>
  <%-- Prepare table row class END --%>
  <%-- DISPLAY TR --%>
    <tr class="<c:out value="${rowClass}" />" id="taskRow<c:out value="${task.id}" />">
      <td class="numerotation"><c:out value="${taskIndex.count}"/>.</td>
      <td class="task_wording">
        <div>
    <c:if test="${task.estDecomposee == 1}">
      <a class="linkSee" href="ToGantt?viewMode=<c:out value="${viewMode}" />&Id=<c:out value="${task.id}" />"><img border="0" src="<%=resource.getIcon("projectManager.treePlus")%>" alt="plus"/></a>&nbsp;
    </c:if>
      <a href="ViewTask?Id=<c:out value="${task.id}" />" title="<fmt:message key="projectManager.gantt.tasks.responsible"/> : <c:out value="${task.responsableFullName}" />"><c:out value="${task.nom}" /></a>
        </div>
      </td>
      <td class="state" width="20px"><p><c:out value="${taskStatusStr}"/>&nbsp;</p></td>
      <td class="percentage" width="8px"><p>
  <c:choose>
    <c:when test="${task.consomme != 0}">
    <fmt:formatNumber value="${(task.consomme / (task.consomme + task.raf)) * 100}" type="number" pattern="0"></fmt:formatNumber> %
    </c:when>
    <c:otherwise>
    0 %
    </c:otherwise>
  </c:choose></p>
      </td>
  <%-- Initialize loop variable --%>
  <fmt:formatDate value="${task.dateDebut}" pattern="yyyyMMdd" var="startDayStr"/>
  <fmt:formatDate value="${task.dateFin}" pattern="yyyyMMdd" var="endDayStr"/>

<c:set var="cptCol" value="0"/>

<c:forEach var="curMonth" items="${months}" varStatus="monthIndex">
  <c:forEach var="curWeek" items="${curMonth.weeks}" varStatus="weekIndex">
    <c:forEach var="curDay" items="${curWeek.days}" varStatus="dayIndex">
      <c:set var="cptCol" value="${cptCol + 1}"/>
      <%-- Initialize column CSS class --%>
      <c:set var="columnClass" value="" />
      <c:if test="${(curWeek.number%2) == 0}">
        <c:set var="columnClass" value="day_odd" />
      </c:if>
      <c:if test="${weekIndex.first && dayIndex.first}">
        <c:set var="columnClass" value="${columnClass} month_begin"/>
      </c:if>
      <fmt:formatDate value="${curDay.day}" pattern="yyyyMMdd" var="curDayStr"/>
      <c:set var="isTaskDay" value="false" />
      <c:choose>
        <c:when test="${curDayStr == startDayStr && curDayStr == endDayStr}">
          <c:set var="columnClass" value="${columnClass} lenght_oneDay task_start"/>
          <c:set var="isTaskDay" value="true" />
        </c:when>
        <c:when test="${curDayStr == startDayStr}">
          <c:set var="columnClass" value="${columnClass} task_start"/>
          <c:set var="isTaskDay" value="true" />
        </c:when>
        <c:when test="${curDayStr == endDayStr}">
          <c:set var="columnClass" value="${columnClass} task_end"/>
          <c:set var="isTaskDay" value="true" />
        </c:when>
        <c:when test="${curDayStr > startDayStr && curDayStr < endDayStr}">
          <c:set var="columnClass" value="${columnClass} task"/>
          <c:set var="isTaskDay" value="true" />
        </c:when>
      </c:choose>
      <c:if test="${isTaskDay}">
        <%-- Check for holidays display --%>
        <c:forEach items="${requestScope['Holidays']}" var="holiday">
          <fmt:formatDate value="${holiday}" pattern="yyyyMMdd" var="curHolidayStr"/>
          <c:if test="${curHolidayStr == curDayStr}">
            <c:set var="columnClass" value="${columnClass} day_unworked"/>
          </c:if>
        </c:forEach>
      </c:if>

      <td class="${columnClass}" id="taskCel${task.id}_${cptCol}">
        <c:if test="${isTaskDay}">
        <div>&nbsp;<div><span>x</span></div></div>
        </c:if>
      </td>
    </c:forEach>
  </c:forEach>
</c:forEach>
    </tr>
</c:forEach>
        </tbody>
      </table>
    </td>
  </tr>
  <tr>
    <td colspan="3" class="milieuFrame">

    </td>
  </tr>
</table>
</view:frame>
</view:window>
</body>
</html>