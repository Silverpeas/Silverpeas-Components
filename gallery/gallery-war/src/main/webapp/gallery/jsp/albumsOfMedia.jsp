<%--
  Copyright (C) 2000 - 2014 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib prefix="gallery" tagdir="/WEB-INF/tags/silverpeas/gallery" %>

<c:set var="userLanguage" value="${requestScope.resources.language}" scope="request"/>
<jsp:useBean id="userLanguage" type="java.lang.String" scope="request"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%-- Request attributes --%>
<c:set var="media" value="${requestScope.Media}" scope="request"/>
<jsp:useBean id="media" type="com.silverpeas.gallery.model.Media" scope="request"/>

<c:set var="albumListIds" value="${requestScope.PathList}"/>
<jsp:useBean id="albumListIds" type="java.util.List<java.lang.String>"/>
<c:set var="allAlbums" value="${requestScope.Albums}"/>
<jsp:useBean id="allAlbums" type="java.util.List<com.silverpeas.gallery.model.AlbumDetail>"/>

<%-- Labels --%>
<fmt:message key="GML.validate" var="validateLabel"/>

<div class="locations">
  <view:board>
    <view:form id="pathsId" name="paths" action="SelectPath" method="POST">
      <input type="hidden" name="MediaId" value="${media.id}">
      <c:set var="oldLevel" value="${0}"/>
      <c:forEach var="album" items="${allAlbums}" varStatus="status">
        <c:if test="${album.level gt 1}">
          <c:set var="albumTabulation" value=""/>
          <c:choose>
            <c:when test="${album.level gt oldLevel}">
              <ul>
            </c:when>
            <c:when test="${album.level lt oldLevel}">
              </li>${silfn:repeat('</ul>', (oldLevel - album.level))}
            </c:when>
          </c:choose>
          <c:set var="checked" value="${albumListIds.contains(album.nodePK.id) ? 'checked': ''}"/>
          <c:if test="${album.level eq oldLevel}"></li></c:if>
          <li><input type="checkbox" name="albumChoice" value="${album.id}" ${checked}>&#160;
          ${albumTabulation}${album.name}
          <c:if test="${status.last}">
            </li>
            ${silfn:repeat('</ul>', (oldLevel - album.level))}
          </c:if>
          <c:set var="oldLevel" value="${album.level}"/>
        </c:if>
      </c:forEach>
    </view:form>
  </view:board>
  <script type="text/javascript">
    function sendLocationData() {
      $.progressMessage();
      $('#pathsId').submit();
    }
  </script>
</div>
<view:buttonPane>
  <view:button label="${validateLabel}" action="javascript:onClick=sendLocationData();"/>
</view:buttonPane>