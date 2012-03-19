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
<%@ page import="com.silverpeas.kmelia.stats.StatisticServiceImpl" %>
<%@ page import="com.silverpeas.kmelia.model.StatsFilterVO" %>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title><fmt:message key="kmelia.stat.title" /></title>
<view:looknfeel />
</head>
<body>
<fmt:message key="kmelia.stat.browsebar" var="browseBarLabel" />
<view:browseBar extraInformations='${browseBarLabel}'/>
<view:window>
<view:frame>
<view:board>
<h1> Statistiques à afficher iciiiiiiiiiii :)</h1>
<%
StatsFilterVO stat = new StatsFilterVO("kmelia111", 55, new Date(), new Date());
StatisticServiceImpl statService = new StatisticServiceImpl();
statService.getNbConsultedPublication(stat);
%>
</view:board>
</view:frame>
</view:window>
</body>
</html>