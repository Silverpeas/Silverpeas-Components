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

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%-- Constants --%>
<view:setConstant var="PREVIEW_RESOLUTION" constant="com.silverpeas.gallery.constant.MediaResolution.PREVIEW"/>

<%-- Attributes --%>
<%@ attribute name="sound" required="true"
              type="com.silverpeas.gallery.model.Sound"
              description="The sound" %>
<c:set var="_mediaResolution" value="${PREVIEW_RESOLUTION}"/>
<%@ attribute name="mediaResolution" required="false"
              type="com.silverpeas.gallery.constant.MediaResolution"
              description="The resolution of the video." %>
<c:if test="${mediaResolution != null}">
  <c:set var="_mediaResolution" value="${mediaResolution}"/>
</c:if>

<div id="videoContainer"></div>
<script type="text/javascript">
  $(document).ready(function() {
    $('#videoContainer').player('audio', {
      container : {
        width : '${_mediaResolution.width}px',
        height : '${_mediaResolution.height}px'
      },
      clip : {
        url : '${silfn:escapeJs(sound.applicationOriginalUrl)}.mp3',
        coverImage : {
          url : "${silfn:escapeJs(sound.getApplicationThumbnailUrl(_mediaResolution))}",
          scaling : 'orig'
        },
        mimeType : '${silfn:escapeJs(sound.fileMimeType.mimeType)}'
      },
      canvas : {
        backgroundColor : '#E1E1E1'
      }
    });
  });
</script>