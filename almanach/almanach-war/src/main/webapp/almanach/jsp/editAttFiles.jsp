<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ page import="org.silverpeas.attachment.model.DocumentType"%>
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
<c:set var="instanceId" value="${browseContext[3]}"/>
<c:set var="spaceId" value="${browseContext[2]}"/>

<c:set var="event" value="${requestScope.Event}"/>
<c:set var="startDate" value="${requestScope.EventStartDate}"/>
<c:set var="pdcUsed" value="${requestScope.PdcUsed}"/>
<c:set var="id" value="${event.id}"/>
<c:set var="title" value="${event.title}"/>
<c:if test="${fn:length(title) > 30}">
  <c:set var="title" value="${fn:substring(title, 0, 30)}...."/>
</c:if>
<c:set var="url" value="${requestScope.ComponentURL}"/>

<!-- AFFICHAGE BROWSER -->
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <view:looknfeel/>
    <title><fmt:message key="GML.popupTitle"/></title>
  </head>
  <body marginheight="5" marginwidth="5" topmargin="5" leftmargin="5">
    <view:browseBar componentId="${instanceId}" extraInformations="${title}"/>
    <view:window>
      <view:tabs>
        <fmt:message key="evenement" var="tabLabel"/>
        <view:tab label="${tabLabel}" action="viewEventContent.jsp?Id=${id}&Date=${startDate}" selected="false"/>
        <fmt:message key="entete" var="tabLabel"/>
        <view:tab label="${tabLabel}" action="editEvent.jsp?Id=${id}&Date=${startDate}" selected="false"/>
        <fmt:message key="GML.attachments" var="tabLabel"/>
        <view:tab label="${tabLabel}" action="editAttFiles.jsp?Id=${id}&Date=${startDate}" selected="true"/>
      </view:tabs>
      <view:frame>
        <c:import url="/attachment/jsp/editAttachedFiles.jsp?Id=${id}&Date=${startDate}&SpaceId=${spaceId}&ComponentId=${instanceId}&Context=attachment&Url=${url}"/>
      </view:frame>
    </view:window>
  </body>
</html>