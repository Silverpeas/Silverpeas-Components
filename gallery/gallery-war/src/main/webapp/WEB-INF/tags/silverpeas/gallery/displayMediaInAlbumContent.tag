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

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<%-- Constants --%>
<view:setConstant var="PREVIEW_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.PREVIEW"/>
<view:setConstant var="userRole" constant="com.stratelia.webactiv.SilverpeasRole.user"/>

<%-- Attributes --%>
<%@ attribute name="media" required="true" type="com.silverpeas.gallery.model.Media"
              description="A media bean (Media.java). The label of the current value is handled." %>
<%@ attribute name="mediaResolution" required="true"
              type="com.silverpeas.gallery.constant.MediaResolution"
              description="The album path." %>
<%@ attribute name="isPortletDisplay" required="false"
              type="java.lang.Boolean"
              description="Indicates if displaying into a portlet." %>

<c:set var="thumbnailChip" value=""/>
<c:set var="classPreview" value="mediaPreview"/>
<c:set var="contentTipUrl" value="${media.getApplicationThumbnailUrl(PREVIEW_RESOLUTION)}"/>
<c:if test="${not isPortletDisplay and media.type.video}">
  <c:if test="${fn:contains(contentTipUrl, '/thumbnail/')}">
    <c:url var="thumbnailChipUrl" value="/gallery/jsp/icons/video_66x50.png"/>
    <c:set var="thumbnailChip">
      <img class="type-media" src="${thumbnailChipUrl}" alt=""/>
    </c:set>
  </c:if>
  <c:set var="classPreview" value="mediaPreview videoPreview"/>
</c:if>
${thumbnailChip}
<img id="imgId_${media.id}" class="${classPreview}" tipTitle="<c:out value="${silfn:truncate(media.title, 50)}"/>"
     tipUrl="${contentTipUrl}" src="${media.getApplicationThumbnailUrl(mediaResolution)}"
     border="0" alt="<c:out value='${media.title}'/>" style="max-width:${mediaResolution.width}px"/>
