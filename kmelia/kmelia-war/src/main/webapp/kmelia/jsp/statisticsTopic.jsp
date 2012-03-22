<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<%@ include file="checkKmelia.jsp" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="filterGroups" value="${requestScope.filterGroups}" />
<c:set var="querySearchs" value="${requestScope.mostInterestedSearch}" />
<c:set var="startDate" value="${requestScope.startDate}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><fmt:message key="kmelia.stat.title" /></title>
<view:looknfeel />
<script type="text/javascript">
function filterStat() {
  document.statForm.submit();
}
</script>
</head>
<body>
<fmt:message key="kmelia.stat.browsebar" var="browseBarLabel" />
<view:browseBar extraInformations='${browseBarLabel}'/>
<view:window>
<view:frame>
<view:board>
<h1><fmt:message key="kmelia.stat.title" /></h1>
<%--
<%@ page import="com.silverpeas.kmelia.stats.StatisticServiceImpl" %>
<%@ page import="com.silverpeas.kmelia.model.StatsFilterVO" %>
Date endDate = new Date();
Calendar cal = Calendar.getInstance();
cal.add(Calendar.DATE, -9);

Date beginDate = cal.getTime();

cal.add(Calendar.DATE, 11);
endDate = cal.getTime();
StatsFilterVO stat = new StatsFilterVO("kmelia111", 0, beginDate, endDate);
StatisticServiceImpl statService = new StatisticServiceImpl();
int nbPubli = statService.getNbConsultedPublication(stat);

--%>
<div id="filterStats">
  <fmt:message key="kmelia.stat.filter.title"/>
  <br/>
  Liste des groupes lecteurs ou rédacteurs:<br/>
  <ul>
    <c:forEach var="group" items="${filterGroups}" >
      <li><c:out value="${group.id}" /> - <c:out value="${group.name}" /> - <c:out value="${group.description}" /></li>
    </c:forEach>
  </ul>
  <form action="statistics" method="get" name="statForm">
    <input name="groupId" type="hidden" value="7" />
    
  </form>  
</div>


<p>

<fmt:formatDate var="startDateStr" value="${startDate}" pattern="dd/MM/yyyy" />
<fmt:formatDate var="endDateStr" value="${requestScope.endDate}" pattern="dd/MM/yyyy"/> 
<fmt:message key="kmelia.stat.consulted.publications">
  <fmt:param value="${startDateStr}" />
  <fmt:param value="${endDateStr}" />
</fmt:message>
<c:out value="${requestScope.nbConsultedPublication}"></c:out>
<br/>
<fmt:message key="kmelia.stat.activity.publications">
  <fmt:param value="${startDateStr}" />
  <fmt:param value="${endDateStr}" />
</fmt:message>
<c:out value="${requestScope.nbActivity}"></c:out>
</p>

<c:if test="${not empty querySearchs}">
<div id="mostImportantQuerySearch">
  <ul>
    <li>Termes recherchés - Nombre d'occurrences</li>
    <c:forEach var="query" items="${querySearchs}" >
      <li><c:out value="${query.query}" /> - <c:out value="${query.occurrences}" /></li>
    </c:forEach>
  </ul>
</div>
</c:if>

<fmt:message var="filterStatButtonLabel" key="kmelia.stat.filter.button" />
<view:buttonPane>
  <view:button action="javascript:onClick=filterStat();" label="${filterStatButtonLabel}" disabled="false" />
</view:buttonPane>

</view:board>
</view:frame>
</view:window>
</body>
</html>