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

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<c:set var="userLanguage" value="${requestScope.resources.language}" scope="request"/>
<jsp:useBean id="userLanguage" type="java.lang.String" scope="request"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<fmt:message var="downloadWatermarkIcon" key='gallery.image.dowloadWatermark' bundle='${icons}'/>

<c:url var="downloadWatermarkIconUrl" value="${downloadWatermarkIcon}"/>

<view:setConstant var="PREVIEW_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.PREVIEW"/>
<view:setConstant var="WATERMARK_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.WATERMARK"/>

<c:set var="isViewMetadata" value="${requestScope.IsViewMetadata}"/>
<c:set var="isWatermark" value="${requestScope.IsWatermark}"/>

<gallery:viewMediaLayout>
  <jsp:attribute name="headerBloc">
    <jsp:useBean id="media" scope="request" type="com.silverpeas.gallery.model.Photo"/>
    <jsp:useBean id="mediaUrl" scope="request" type="java.lang.String"/>
  </jsp:attribute>

  <jsp:attribute name="additionalDownloadBloc">
    <c:if test="${isWatermark}">
      <c:set var="watermarlUrl" value="${media.getApplicationThumbnailUrl(WATERMARK_RESOLUTION)}"/>
      <c:if test="${not empty watermarlUrl}">
        <a href="${watermarlUrl}" target="_blank">
          <img src="${downloadWatermarkIconUrl}" alt="<fmt:message key='gallery.originalWatermark'/>" title="<fmt:message key='gallery.originalWatermark'/>"/>
        </a>
      </c:if>
    </c:if>
  </jsp:attribute>

  <jsp:attribute name="specificSpecificationBloc">
    <c:if test="${media.definition.defined}">
      <span class="fileCharacteristicSize"><fmt:message key="gallery.dimension"/> <b>${media.definition.width}
        x ${media.definition.height} <fmt:message key="gallery.pixels"/></b></span>
    </c:if>
  </jsp:attribute>

  <jsp:attribute name="bottomContentTopBloc">
    <gallery:displayMediaMetadata media="${media}" isViewMetadata="${isViewMetadata}"/>
  </jsp:attribute>

  <jsp:attribute name="mediaPreviewBloc">
    <a href="#" onclick="javascript:startSlideshow('${media.id}')">
      <img alt="<c:out value="${media.name}"/>" src="${media.getApplicationThumbnailUrl(PREVIEW_RESOLUTION)}"/>
    </a>
  </jsp:attribute>
</gallery:viewMediaLayout>