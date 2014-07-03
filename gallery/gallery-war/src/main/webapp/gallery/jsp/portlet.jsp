<%--

    Copyright (C) 2000 - 2013 Silverpeas

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

<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="check.jsp" %>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<view:setConstant var="TINY_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.TINY"/>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="root" value="${requestScope.root}"/>
<jsp:useBean id="root" type="com.silverpeas.gallery.model.AlbumDetail"/>
<c:set var="mediaList" value="${requestScope.MediaList}"/>
<jsp:useBean id="mediaList" type="java.util.List<com.silverpeas.gallery.model.Media>"/>

<html>
<head>
  <view:looknfeel/>
  <script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
  <script language="javascript">

    function goToAlbum(id) {
      document.albumForm.Id.value = id;
      document.albumForm.submit();
    }

    function goToImage(photoId) {
      document.imageForm.MediaId.value = photoId;
      document.imageForm.submit();
    }

  </script>
  <style type="text/css">

    div {
      position: relative;
      display: inline;
    }

    #vignette img {
      margin: 1px;
      padding: 2px;
      border: 2px solid #B3BFD1;
    }

  </style>
</head>
<body>
<view:navigationList title="${root.name}">
  <c:forEach var="album" items="${root.childrenDetails}">
    <view:navigationListItem label="${album.name}" action="javascript:onClick=goToAlbum('${album.id}')"
                             description="${album.description}"/>
  </c:forEach>
</view:navigationList>
<view:board>
  <table border="0" cellspacing="0" cellpadding="0" align=center width="100%">
    <tr>
      <td align="center" class=ArrayNavigation>
        <fmt:message key="gallery.last.media"/>
      </td>
    </tr>
    <c:choose>
      <c:when test="${not empty mediaList}">
        <tr>
          <td>&#160;</td>
        </tr>
        <tr>
          <td align="center">
            <c:forEach var="media" items="${mediaList}">
              <div id="vignette">
                <a href="javascript:onClick=goToImage('${media.id}')">
                  <img src="${media.getApplicationThumbnailUrl(TINY_RESOLUTION)}" border="0" alt="<c:out value='${media.title}'/>" title="<c:out value='${media.title}'/>"></a>
              </div>
            </c:forEach>
          </td>
        </tr>
      </c:when>
      <c:otherwise>
        <tr>
          <td colspan="5" valign="middle" align="center" width="100%">
            <br><fmt:message key="gallery.empty.data"/><br>
          </td>
        </tr>
      </c:otherwise>
    </c:choose>
  </table>
</view:board>
<form name="albumForm" action="ViewAlbum" Method="POST" target="MyMain">
  <input type="hidden" name="Id">
</form>
<form name="imageForm" action="MediaView" Method="POST" target="MyMain">
  <input type="hidden" name="MediaId">
</form>
</body>
</html>