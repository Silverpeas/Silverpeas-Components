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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<view:setConstant var="mediaType" constant="com.silverpeas.gallery.constant.MediaType.Photo"/>
<view:setConstant var="supportedMediaMimeTypes" constant="com.silverpeas.gallery.constant.MediaMimeType.PHOTOS"/>
<view:setConstant var="PREVIEW_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.PREVIEW"/>
<view:setConstant var="SMALL_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.SMALL"/>

<c:set var="isViewMetadata" value="${requestScope.IsViewMetadata}"/>

<gallery:editMediaLayout mediaType="${mediaType}" supportedMediaMimeTypes="${supportedMediaMimeTypes}">
  <jsp:attribute name="headerBloc">
    <jsp:useBean id="media" scope="request" type="com.silverpeas.gallery.model.Photo"/>
    <jsp:useBean id="isNewMediaCase" scope="request" type="java.lang.Boolean"/>
    <c:if test="${not isNewMediaCase}">
      <gallery:handlePhotoPreview jquerySelector="${'#photoPreview'}"/>
    </c:if>
  </jsp:attribute>

  <jsp:attribute name="mediaPreviewBloc">
    <center>
      <div>
        <img id="photoPreview"
             tipTitle="<c:out value="${media.title}"/>"
             tipUrl="${media.getApplicationThumbnailUrl(PREVIEW_RESOLUTION)}"
             src="${media.getApplicationThumbnailUrl(SMALL_RESOLUTION)}" border="0" alt=""/>
      </div>
    </center>
    <gallery:displayMediaMetadata media="${media}" isViewMetadata="${isViewMetadata}"/>
  </jsp:attribute>
</gallery:editMediaLayout>