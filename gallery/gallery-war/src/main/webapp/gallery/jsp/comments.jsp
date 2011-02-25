<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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

<fmt:message var="photoTab" key="gallery.photo"/>
<fmt:message var="infoTab" key="gallery.info"/>
<fmt:message var="commentTab" key="gallery.comments"/>
<fmt:message var="accessTab" key="gallery.accessPath"/>
<fmt:message var="pdcTab" key="GML.PDC"/>

<c:set var="browseContext" value="${requestScope.browseContext}"/>
<c:set var="instanceId" value="${browseContext[3]}"/>
<c:set var="photo" value="${requestScope.Photo}"/>
<c:set var="photoId" value="${photo.photoPK.id}"/>
<c:set var="userId" value="${requestScope.UserId}"/>
<c:set var="nodePath"  value="${requestScope.Path}"/>
<c:set var="commentCount" value="${requestScope.NbComments}"/>
<c:set var="updateAllowed" value="${requestScope.UpdateImageAllowed}"/>
<c:set var="pdc" value="${requestScope.IsUsePdc}"/>
<c:set var="callback">function( event ) { if (event.type === 'listing') { commentCount = event.comments.length; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + event.comments.length + ')'); } else if (event.type === 'deletion') { commentCount--; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + commentCount + ')'); } else if (event.type === 'addition') { commentCount++; $('#comment-tab').html('<c:out value="${commentTab}"/> ( ' + commentCount + ')'); } }</c:set>

<html>
  <head>
    <view:looknfeel/>
  </head>
  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
    <view:browseBar>
      <c:if test="${nodePath != null}">
        <c:forEach var="node" items="${nodePath}">
          <c:if test="${node.id != 0}">
            <view:browseBarElt label="${node.name}" link="ViewAlbum?Id=${node.nodePK.id}" id="${node.nodePK.id}"/>
          </c:if>
        </c:forEach>
      </c:if>
    </view:browseBar>

    <view:window>

      <view:tabs>
        <view:tab action="PreviewPhoto?PhotoId=${photoId}" label="${photoTab}" selected="false"/>
        <c:if test="${updateAllowed}">
          <view:tab action="EditInformation?PhotoId=${photoId}" label="${infoTab}" selected="false"/>
        </c:if>
        <view:tab action="#" label="<span id='comment-tab'>${commentTab} (${commentCount})</span>" selected="true"/>
        <c:if test="${updateAllowed}">
          <view:tab action="AccessPath?PhotoId=${photoId}" label="${accessTab}" selected="false"/>
          <c:if test="${pdc}">
            <view:tab action="PdcPositions?PhotoId=${photoId}" label="${pdcTab}" selected="false"/>
          </c:if>
        </c:if>
      </view:tabs>

      <view:frame>

        <view:comments userId="${userId}" componentId="${instanceId}" resourceId="${photoId}" callback="${callback}"/>

      </view:frame>
    </view:window>
  </body>
</html>