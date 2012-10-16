<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
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

<fmt:message var="projectTab" key="projectManager.Projet"/>
<fmt:message var="taskTab" key="projectManager.Taches"/>
<fmt:message var="attachmentTab" key="GML.attachments"/>
<fmt:message var="commentTab" key="projectManager.Commentaires"/>
<fmt:message var="ganttTab" key="projectManager.Gantt"/>
<fmt:message var="calendarTab" key="projectManager.Calendrier"/>

<c:set var="instanceId" value="${requestScope.InstanceId}"/>
<c:set var="userId" value="${requestScope.UserId}"/>
<c:set var="role" value="${requestScope.Role}"/>

<html>
  <head>
    <title></title>
    <view:looknfeel/>
    <script type="text/javascript" src="<c:url value='/util/javaScript/animation.js'/>"></script>
    <script language="javascript">
    </script>

  </head>
  <body>
    <view:browseBar></view:browseBar>
    <view:window>

      <view:tabs>
        <view:tab action="ToProject" label="${projectTab}" selected="false"/>
        <view:tab action="Main" label="${taskTab}" selected="false"/>
        <c:if test="${'admin' == role}">
          <view:tab action="ToAttachments" label="${attachmentTab}" selected="false"/>
        </c:if>
        <view:tab action="#" label="${commentTab}" selected="true"/>
        <view:tab action="ToGantt" label="${ganttTab}" selected="false"/>
        <c:if test="${'admin' == role}">
          <view:tab action="ToCalendar" label="${calendarTab}" selected="false"/>
        </c:if>
      </view:tabs>

      <view:frame>

        <view:comments 	userId="${userId}" componentId="${instanceId}"
        				resourceType="ProjectManager" resourceId="-1"/>

      </view:frame>
    </view:window>
  </body>
</html>