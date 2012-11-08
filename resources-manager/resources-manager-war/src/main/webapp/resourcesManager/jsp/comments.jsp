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

<fmt:message var="extraInfo" key="resourcesManager.informationResource"/>
<fmt:message var="resourceTab" key="resourcesManager.resource"/>
<fmt:message var="commentTab" key="resourcesManager.commentaires"/>

<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="componentLabel" value="${browseContext[1]}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>
<c:set var="resourceName" value="${requestScope.resourceName}"/>
<c:set var="resourceType" value="${requestScope.resourceType}"/>
<c:set var="resourceId" value="${requestScope.resourceId}"/>
<c:set var="userId" value="${requestScope.UserId}"/>
<c:set var="path" value="${requestScope.Path}"/>
<c:set var="indexation" value="0"/>

<html>
  <head>
    <view:looknfeel/>
  </head>
  <body>
    <view:browseBar componentId="${componentLabel}" path="${path}" ignoreComponentLink="true "extraInformations="${extraInfo} ${resourceName}"></view:browseBar>

    <view:window>
      <view:tabs>
        <view:tab action="ViewResource" label="${resourceTab}" selected="false"/>
        <view:tab action="#" label="${commentTab}" selected="true"/>
      </view:tabs>

      <view:frame>
        <view:comments  userId="${userId}" componentId="${instanceId}"
                        resourceType="${resourceType}" resourceId="${resourceId}" indexed="${indexation}" />  
      </view:frame>
    </view:window>
  </body>
</html>