<%--
  Copyright (C) 2000 - 2022 Silverpeas

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
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<fmt:message var="sliderFullscreenInfo" key="gallery.display.fullscreen.info"/>
<fmt:message var="sliderRunInfo" key="gallery.run.play.info"/>
<fmt:message var="sliderPauseInfo" key="gallery.run.standby.info"/>

<c:set var="componentInstanceId" value="${requestScope.browseContext[3]}"/>
<c:set var="albumId" value="${requestScope.albumId}"/>
<c:set var="wait" value="${requestScope.wait}"/>
<c:set var="currentUserSort" value="${requestScope.Sort}"/>

<view:link href="/gallery/jsp/styleSheets/slider/themes/classic/galleria.classic.min.css"/>
<view:includePlugin name="qtip"/>
<view:script src="/gallery/jsp/javaScript/slider/galleria-1.5.7.min.js"/>
<view:script src="/gallery/jsp/styleSheets/slider/themes/classic/galleria.classic.min.js"/>
<view:script src="/gallery/jsp/javaScript/silverpeas-gallery-slider.js"/>
<script type="text/JavaScript">
  function startSlideshow(fromMediaId) {
    $.popup.showWaiting();
    nbPauses = -1;
    var $slider = $('#gallerySlider');
    if ($slider.size() == 0) {
      $slider = $("<div>").attr("id", "gallerySlider");
      $(document.body).append($slider);
    }
    $slider.gallerySlider('album', {
      $elementsToHide: $('#videoContainer,#audioContainer,#streamingContainer').children(),
      mediaSort : '${currentUserSort}',
      componentInstanceId : '${componentInstanceId}',
      albumId : '${albumId}',
      fromMediaId : fromMediaId,
      waitInSeconds : '${wait}',
      width : 600,
      height : 400,
      dummyImage : '<view:componentUrl componentId=""/>/gallery/jsp/icons/notAvailable_<c:out value="${requestScope.resources.language}"/>_preview.jpg',
      callbackPlay : function() {
        nbPauses = 0;
        notyInfo('<c:out value="${sliderRunInfo}" escapeXml="false" />');
      },
      callbackPause : function() {
        nbPauses++;
        if (nbPauses == 1) {
          notyInfo('<c:out value="${sliderPauseInfo}" escapeXml="false" />');
        }
      },
      callbackEnterFullScreen : function() {
        notyInfo('<c:out value="${sliderFullscreenInfo}" escapeXml="false" />');
      },
      callbackLink : function(media) {
        return webContext +
            "/Rgallery/<c:out value="${componentInstanceId}" />/MediaView?MediaId=" + media.id;
      }
    });
  }
</script>