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

<%@ include file="checkKmelia.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="com.silverpeas.kmelia.model.*"%>

<fmt:setLocale value="${sessionScope['SilverSessionController'].favoriteLanguage}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle basename="org.silverpeas.util.date.multilang.date" var="dateBundle" />


<c:set var="filterGroups" value="${requestScope.filterGroups}" />
<c:set var="querySearchs" value="${requestScope.mostInterestedSearch}" />
<c:set var="detailActivity" value="${requestScope.detailActivity}" />
<c:set var="distinctPublications" value="${requestScope.distinctPublications}"/>
<c:set var="startDate" value="${requestScope.startDate}" />
<c:set var="endDate" value="${requestScope.endDate}" />
<c:set var="filterIdGroup" value="${requestScope.filterIdGroup}" />
<c:set var="filterLibGroup" value="${requestScope.filterLibGroup}" />

<!-- Be careful the following code is not multilangue -->
<fmt:message var="dateFormat" key="dateOutputFormat" bundle="${dateBundle}" />
<fmt:formatDate var="startDateStr" value="${startDate}" pattern="${dateFormat}" />
<fmt:formatDate var="endDateStr" value="${endDate}" pattern="${dateFormat}"/> 

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><fmt:message key="kmelia.stat.title" /></title>
<view:looknfeel />
<view:includePlugin name="datepicker"/>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript">
function filterStat() {
  if (isCorrectForm()) {
    document.statForm.submit();
  }
}

function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;

  var beginDate = document.statForm.beginDate.value;
  var endDate = document.statForm.endDate.value;

  var beginDateOK = true;

  if (!isWhitespace(beginDate)) {
    if (!isDateOK(beginDate, '<%=kmeliaScc.getLanguage()%>')) {
         errorMsg+=" - '<fmt:message key="PubDateDebut" />' <fmt:message key="GML.MustContainsCorrectDate"/>\n";
         errorNb++;
         beginDateOK = false;
      } 
  }
  if (!isWhitespace(endDate)) {
    if (!isDateOK(endDate, '<%=kmeliaScc.getLanguage()%>')) {
      errorMsg+=" - '<fmt:message key="PubDateFin"/>' <fmt:message key="GML.MustContainsCorrectDate"/>\n";
      errorNb++;
    } else {
      if (!isWhitespace(beginDate) && !isWhitespace(endDate)) {
        if (beginDateOK && !isDate1AfterDate2(endDate, beginDate, '<%=kmeliaScc.getLanguage()%>')) {
          errorMsg+=" - '<fmt:message key="PubDateFin"/>' <fmt:message key="GML.MustContainsPostOrEqualDateTo"/> "+beginDate+"\n";
          errorNb++;
        }
      } else {
        if (isWhitespace(beginDate) && !isWhitespace(endDate)) {
          if (!isFuture(endDate, '<%=kmeliaScc.getLanguage()%>')) {
            errorMsg+=" - '<fmt:message key="PubDateFin"/>' <fmt:message key="GML.MustContainsPostDate"/>\n";
            errorNb++;
          }
        }
      }
    }
  }
       
  switch(errorNb) {
   case 0 :
     result = true;
    break;
   case 1 :
     errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
     window.alert(errorMsg);
     result = false;
    break    ;
   default :
     errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
     window.alert(errorMsg);
     result = false;
    break;
  }
  return result;
}

function openGroupPanel() {
  windowName = "userPanelWindow";
  windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised,scrollbars,resizable";
  SP_openWindow('statSelectionGroup', windowName, '750', '550','scrollbars=yes, resizable, alwaysRaised');
}


function clearFilterGroup() {
  $("#filterIdGroup").val("");
  $("#filterLibGroup").val("");
  
}

var dateFormat = '<fmt:message key="date.format.javascript" bundle="${dateBundle}"/>';

function updateTimeInterval() {
  var curDateControlVal = $("#dateControlId").val();
  var curDate = new Date();
  var strDate = $.datepicker.formatDate(dateFormat, curDate);
  $("#endDateId").val(strDate);
  if (curDateControlVal == 1) {
    $("#beginDateId").val(strDate);
  } else if (curDateControlVal==2) {
    curDate.setDate(curDate.getDate()-1);
    strDate = $.datepicker.formatDate(dateFormat, curDate);
    $("#beginDateId").val(strDate);
  } else if (curDateControlVal==3) {
    curDate.setDate(curDate.getDate()-7);
    strDate = $.datepicker.formatDate(dateFormat, curDate);
    $("#beginDateId").val(strDate);
  } else if (curDateControlVal==4) {
    curDate.setMonth(curDate.getMonth()-1);
    strDate = $.datepicker.formatDate(dateFormat, curDate);
    $("#beginDateId").val(strDate);
  } else if (curDateControlVal==5) {
    curDate.setYear(curDate.getFullYear()-1);
    strDate = $.datepicker.formatDate(dateFormat, curDate);
    $("#beginDateId").val(strDate);
  }
}

$(document).ready(function() {
  // do stuff when DOM is ready
  $("#filterIdGroup").val("${filterIdGroup}");
  $("#filterLibGroup").val("${filterLibGroup}");
});

</script>
</head>
<body class="topic-statistics">
<fmt:message key="kmelia.stat.browsebar" var="browseBarLabel" />
<view:browseBar extraInformations='${browseBarLabel}'/>
<view:window>
<view:frame>
<view:board>
<h1><fmt:message key="kmelia.stat.title" /> <c:out value="${requestScope.kmelia.componentLabel}" /></h1>

<fieldset>
  <legend><fmt:message key="kmelia.stat.result.fieldset.period"/></legend>
<div class="container-filter">
  <form class="content-container-filter" name="statForm" action="statistics" method="get">
    <div class="datecontrole">
      <label for="dateControl"><fmt:message key="kmelia.stat.filter.period"/></label>
      <select id="dateControlId" name="dateControl" onchange="javascript:updateTimeInterval();">
        <option value=""><fmt:message key="kmelia.stat.filter.period.datecontrol.custom"/></option>
        <option value="1"><fmt:message key="kmelia.stat.filter.period.datecontrol.today"/></option>
        <option value="2"><fmt:message key="kmelia.stat.filter.period.datecontrol.yesterday"/></option>
        <option value="3"><fmt:message key="kmelia.stat.filter.period.datecontrol.week"/></option>
        <option value="4"><fmt:message key="kmelia.stat.filter.period.datecontrol.month"/></option>
        <option value="5"><fmt:message key="kmelia.stat.filter.period.datecontrol.year"/></option>
      </select>
      <div id="datePersonnalisedControl">
        <fmt:message key="kmelia.stat.filter.period.start" />
        <input class="dateToPick" type="text" maxlength="10" size="12" value="${startDateStr}" name="beginDate" id="beginDateId" /> 
        <fmt:message key="kmelia.stat.filter.period.end" /> 
        <input class="dateToPick" type="text" maxlength="10" size="12" value="${endDateStr}" name="endDate" id="endDateId" />
      </div>
    </div>
    
    <div class="filterLibGroup">
      <label for="filterLibGroup"><fmt:message key="kmelia.stat.filter.group"/></label>
      <!-- Adding userPanelGroup -->
      <input type="text" disabled="" size="25" value="" name="filterLibGroup" id="filterLibGroup" />
      <input type="hidden" value="" name="filterIdGroup" id="filterIdGroup"/>
      <fmt:message key="kmelia.stat.filter.group.select" var="selectGroupLabel" />
      <a href="javascript:openGroupPanel();"><img border="0" align="absmiddle" title="${selectGroupLabel}" alt="${selectGroupLabel}" src="/silverpeas/util/icons/groupe.gif"></a>
      <a href="javascript:clearFilterGroup()"><img border="0" align="absmiddle" title="Effacer filtre" alt="Effacer filtre" src="/silverpeas/util/icons/delete.gif"></a> 
      <!-- 
      <select name="statGroupId" id="selectGroupId">
        <option value=""><fmt:message key="kmelia.stat.filter.group.noselection"/></option>
    <c:forEach var="group" items="${filterGroups}">
        <option value="${group.id}">${group.name}</option>
    </c:forEach>
      </select>
       -->
    </div>
    <div class="button-filter">
      <fmt:message var="filterStatButtonLabel" key="kmelia.stat.filter.button" />
      <view:buttonPane>
        <view:button action="javascript:onClick=filterStat();" label="${filterStatButtonLabel}" disabled="false" />
      </view:buttonPane>    
    </div>
  </form>
</div>
<span class="titreCouleur">
<h3>
  <fmt:message key="kmelia.stat.result.period">
    <fmt:param value="${startDateStr}" />
    <fmt:param value="${endDateStr}" />
  </fmt:message>
</h3>
</span>
<div class="inlineMessage">${detailActivity.createdPublicationNumber} <fmt:message key="kmelia.stat.result.created.publication" /> - ${detailActivity.modifiedPublicationNumber} <fmt:message key="kmelia.stat.result.modified.publication" /></div>
<div class="inlineMessage">${distinctPublications} <fmt:message key="kmelia.stat.result.consulted.publication" /></div>
</fieldset>
<br />

<c:if test="${not empty querySearchs}">
<fieldset>
  <legend><fmt:message key="kmelia.stat.result.fieldset.search"/></legend>
  
<table id="table-keyword-frequency" class="table-result-statistics"cellpadding="0" cellspacing="0">
  <thead>
    <tr class="title-table"><th colspan="3" class="titreCouleur"><fmt:message key="kmelia.stat.most.interested.search" /></th></tr>
    <tr>
      <td class="index-table"> </td>
      <td class="principal-element-table"><fmt:message key="kmelia.stat.most.interested.search.key" /></td>
      <td><fmt:message key="kmelia.stat.most.interested.search.nb" /></td>
    </tr>
  </thead>
  <tbody>
<c:forEach var="query" items="${querySearchs}" varStatus="status">
  <c:set var="rowStyle" value="${(status.index)%2 eq 0 ?'odd':'even'}"/>
    <tr class="${rowStyle}">
      <td class="index-table">${(status.index + 1)}.</td>
      <td class="principal-element-table titreCouleur">${query.query}</td>
      <td>${query.occurrences}</td>
    </tr>
</c:forEach>
  </tbody>
</table>
</fieldset>
</c:if>

</view:board>
</view:frame>
</view:window>
</body>
</html>