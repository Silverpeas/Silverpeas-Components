<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

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
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="parentTaskId" value="" />
<c:if test="${not empty(requestScope['ActionMere'])}">
  <c:set var="parentTask" value="${requestScope['ActionMere']}"></c:set>
  <c:set var="parentTaskId" value="${parentTask.id}" />
</c:if>

<c:set var="viewMode" value="${requestScope['ViewMode']}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<view:looknfeel />
<view:includePlugin name="qtip"/>
<link href="<c:out value="${ctxPath}"/>/projectManager/jsp/css/gantt.css" type="text/css" rel="stylesheet" />
<script type="text/javascript" src="<c:out value="${ctxPath}"/>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<c:out value="${ctxPath}"/>/projectManager/jsp/js/ajax_project.js"></script>
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
  highlightResponsible();

  $('#legendLabelId').click(function() {
    if ($('#legende').is(':visible')) {
        $('#legende').hide();
    } else {
        $('#legende').show();
    }
  });

  $("#ajaxLoadError").dialog('close');
  
});



/**
 * Add tooltip over task name
 */
function highlightResponsible() {
  // Tooltip over task in order to know the responsible
  $('a[href][title]',$('.task_wording')).qtip({
	content: {
		text: false // Use each elements title attribute
	},
	style: {
		tip: true,
		classes: "qtip-shadow qtip-green"
	},
	position: {
		adjust: {
			method: "flip flip"
		},
		viewport: $(window)
	}
    });
}

function getContext() {
  return "<c:out value="${ctxPath}" />";
}
// global javascript variable
var listHolidays = [];

<c:forEach items="${requestScope['Holidays']}" var="holiday" varStatus="holidayIndex">
listHolidays[<c:out value="${holidayIndex.index}" />] = '<fmt:formatDate value="${holiday}" pattern="yyyyMMdd"/>';
</c:forEach>

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
<fmt:message key="GML.attachments" var="gmlAttachments" />
<view:tabs>
  <view:tab label="${pmProject}" selected="false" action="ToProject"></view:tab>
  <view:tab label="${pmTasks}" selected="false" action="Main"></view:tab>
  <c:if test="${fn:contains(requestScope['Role'],'admin')}">
    <view:tab label="${gmlAttachments}" selected="false" action="ToAttachments"></view:tab>
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
      <c:when test="${fn:contains(viewMode, 'year')}">
        <c:set var="yearClass" value="active" />
      </c:when>
      <c:when test="${fn:contains(viewMode, 'quarter')}">
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


<%-- -------------------------------------------------------------------------- --%>
<%--                          YEAR VIEW                          --%>
<%-- -------------------------------------------------------------------------- --%>
<input type="hidden" id="hiddenInProgressId" value="<fmt:message key="projectManager.gantt.view.tasks.inprogress" />" />
<input type="hidden" id="hiddenFrozenId" value="<fmt:message key="projectManager.gantt.view.tasks.frozen" />" />
<input type="hidden" id="hiddenCancelId" value="<fmt:message key="projectManager.gantt.view.tasks.cancel" />" />
<input type="hidden" id="hiddenDoneId" value="<fmt:message key="projectManager.gantt.view.tasks.done" />" />
<input type="hidden" id="hiddenWarningId" value="<fmt:message key="projectManager.gantt.view.tasks.alert" />" />
<input type="hidden" id="hiddenNotStartedId" value="<fmt:message key="projectManager.gantt.view.tasks.notstarted" />" />
<input type="hidden" id="hiddenResponsibleId" value="<fmt:message key="projectManager.gantt.tasks.responsible"/>" />
<input type="hidden" id="hiddenExpandTreeImgId" value="<c:out value="${ctxPath}"/><fmt:message key="projectManager.treePlus" bundle="${icons}"/>" />
<input type="hidden" id="hiddenCollapseTreeImgId" value="<c:out value="${ctxPath}"/><fmt:message key="projectManager.treeMinus" bundle="${icons}"/>" />
<input type="hidden" id="hiddenComponentId" value="<c:out value="${requestScope['browseContext'][3]}"/>" />

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
    <%-- Initialize view message--%> 
    <c:set var="monthsHeader" value=""/>
    <c:set var="monthsBefore" value=""/>
    <c:set var="monthsAfter" value=""/>
    <c:set var="dateBefore" value=""/>
    <c:set var="dateAfter" value=""/>
    <c:choose>
      <c:when test="${fn:contains(viewMode, 'year')}">
        <fmt:message var="monthsHeader" key="projectManager.gantt.view.year.header" />
        <fmt:message var="monthsBefore" key="projectManager.gantt.view.year.previous" />
        <fmt:message var="monthsAfter" key="projectManager.gantt.view.year.next" />
        <fmt:formatDate var="dateBefore" value="${requestScope['BeforeYear']}" pattern="dd/MM/yyyy" />
        <fmt:formatDate var="dateAfter" value="${requestScope['AfterYear']}" pattern="dd/MM/yyyy" />
      </c:when>
      <c:otherwise>
		    <fmt:message var="monthsHeader" key="projectManager.gantt.view.quarter.header" />
		    <fmt:message var="monthsBefore" key="projectManager.gantt.view.quarter.previous" />
		    <fmt:message var="monthsAfter" key="projectManager.gantt.view.quarter.next" />
        <fmt:formatDate var="dateBefore" value="${requestScope['BeforeQuarter']}" pattern="dd/MM/yyyy" />
        <fmt:formatDate var="dateAfter" value="${requestScope['AfterQuarter']}" pattern="dd/MM/yyyy" />
      </c:otherwise>
    </c:choose>

          <!-- Display month link -->
          <tr>
            <td colspan="4" class="noBorder"></td>
            <td colspan="<c:out value="${totalDays}" />" id="month_nav" >
              <a title="<c:out value="${monthsBefore}"/>" href="ToGantt?viewMode=<c:out value="${viewMode}" />&Id=<c:out value="${parentTaskId}"/>&StartDate=<c:out value="${dateBefore}"/>"><img alt="&lt;&lt;" src="<c:out value="${ctxPath}"/><fmt:message key="projectManager.left.double" bundle="${icons}" />" /></a>
              <a title="<fmt:message key="projectManager.gantt.view.month.previous"/>" href="ToGantt?viewMode=<c:out value="${viewMode}" />&Id=<c:out value="${parentTaskId}"/>&StartDate=<fmt:formatDate value="${requestScope['BeforeMonth']}" pattern="dd/MM/yyyy" />"><img alt="&lt;" src="<c:out value="${ctxPath}"/><fmt:message key="projectManager.gauche" bundle="${icons}" />" /></a> 
               <span><c:out value="${monthsHeader}" /> 
               </span>&nbsp; <fmt:formatDate value="${requestScope['StartDate']}" pattern="MMMMMMMMMM yyyy" />
               <a title="<fmt:message key="projectManager.gantt.view.month.next"/>" href="ToGantt?viewMode=<c:out value="${viewMode}" />&Id=<c:out value="${parentTaskId}"/>&StartDate=<fmt:formatDate value="${requestScope['AfterMonth']}" pattern="dd/MM/yyyy" />"><img alt="&gt;" src="<c:out value="${ctxPath}"/><fmt:message key="projectManager.droite" bundle="${icons}" />" /></a>
               <a title="<c:out value="${monthsAfter}"/>" href="ToGantt?viewMode=<c:out value="${viewMode}" />&Id=<c:out value="${parentTaskId}"/>&StartDate=<c:out value="${dateAfter}"/>"><img alt="&gt;&gt;" src="<c:out value="${ctxPath}"/><fmt:message key="projectManager.right.double" bundle="${icons}" />" /></a>
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

          <%-- Add new empty line in order prepare ajax loading --%>
          <tr id="emptyDays" style="display:none;">
<c:forEach var="curMonth" items="${months}" varStatus="monthIndex">
  <c:forEach var="curWeek" items="${curMonth.weeks}" varStatus="weekIndex">
    <c:forEach var="curDay" items="${curWeek.days}" varStatus="dayIndex">
      <c:set var="columnClass" value="" />
      <c:if test="${(curWeek.number%2) == 0}">
        <c:set var="columnClass" value="day_odd" />
      </c:if>
      <c:if test="${weekIndex.first && dayIndex.first}">
        <c:set var="columnClass" value="${columnClass} month_begin"/>
      </c:if>
      <td class="<c:out value="${columnClass}"/>"><fmt:formatDate value="${curDay.day}" pattern="yyyyMMdd" /></td>
    </c:forEach>
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
  <c:if test="${task.level > 1}">
    <c:set var="rowClass" value="${rowClass} under_task" />
    <c:if test="${taskIndex.last}">
      <c:set var="rowClass" value="${rowClass} last_child" />
    </c:if>
    <c:set var="rowClass" value="${rowClass} level${task.level}" />
  </c:if>
  <c:if test="${task.estDecomposee == 0}">
    <c:set var="rowClass" value="${rowClass} task_row" />
  </c:if>
  <%-- Prepare table row class END --%>
  <%-- DISPLAY TR --%>
    <tr class="<c:out value="${rowClass}" />" id="taskRow<c:out value="${task.id}" />">
      <td class="numerotation"><c:out value="${taskIndex.count}"/>.</td>
      <td class="task_wording">
        <div>
    <c:if test="${task.estDecomposee == 1}">
      <a class="linkSee" href="javascript:loadTask('<c:out value="${task.id}" />', '<c:out value="${requestScope['browseContext'][3]}"/>');" id="taskLink<c:out value="${task.id}" />"><img id="taskLinkImg<c:out value="${task.id}" />" border="0" src="<c:out value="${ctxPath}"/><fmt:message key="projectManager.treePlus" bundle="${icons}"/>" alt="+"/></a>
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

      <td class="${columnClass}"  id="td<c:out value="${task.id}_${curDayStr}" />">
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
<div id="ajaxLoadError" style="display:none;">
  <fmt:message key="projectManager.gantt.ajax.error.load" />
</div>
</body>
</html>