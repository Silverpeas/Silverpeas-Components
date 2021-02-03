<%--
  Copyright (C) 2000 - 2021 Silverpeas

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
<%@ tag import="org.silverpeas.components.gallery.GalleryComponentSettings" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ attribute name="jquerySelector" required="true"
              type="java.lang.String"
              description="The jQuery selector." %>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="NB_MAX_THUMBNAIL" value="<%=GalleryComponentSettings.getMaxNumberOfPreviewThumbnail()%>"/>

<style type="text/css">
  .photoPreviewTip {
    max-width: none;
    max-height: none;
  }
</style>

<script type="text/javascript">
  (function() {

    whenSilverpeasReady(function() {

      $('${jquerySelector}').each(function() {
        $(this).qtip({
          prerender : true,
          style : {
            classes : 'qtip-bootstrap photoPreviewTip'
          },
          content : {
            title : {
              text : $(this).attr("tipTitle")
            },
            text : "<img src='" + $(this).attr("tipUrl") + "' id='tipUrl_" + $(this).attr("id") + "' />"
          },
          position : {
            my : 'right middle',
            at : 'left middle',
            adjust : {
              method : "flipinvert"
            },
            viewport : $(window)
          },
          show : {
            solo : true
          }
        });
      });

      var mouseEnterCallback = function() {
        if (this.intervalID) {
          window.clearInterval(this.intervalID);
        } else {
          if (this.providerData) {
            var thumbnailUrl = this.providerData.thumbnailUrl;
            var thumbnailDefinition = this.providerData.thumbnailDefinition;
            if (thumbnailUrl) {
              this.getPreviewElement().setAttribute('src', thumbnailUrl);
            }
            if (thumbnailDefinition) {
              this.getPreviewElement().setAttribute('width', thumbnailDefinition.width);
              this.getPreviewElement().setAttribute('height', thumbnailDefinition.height);
            }
          }
        }
        this.intervalID = setInterval(changePicture.bind(this), 1000);
      };

      var mouseLeaveCallback = function() {
        window.clearInterval(this.intervalID);
      };

      var changePicture = function() {
        var src;
        this.changePictureCount++;
        if (this.providerData) {
          <%-- Case of a streaming --%>
          var thumbnailUrls = this.providerData.thumbnailPreviewUrls;
          if (thumbnailUrls.length > 1) {
            src = thumbnailUrls[this.changePictureCount % thumbnailUrls.length];
          }
        } else {
          <%-- Case of a video --%>
          src = this.getPreviewElement().getAttribute('src').replace(/thumbnail[/][0-9]+/g,
              'thumbnail/' + (this.changePictureCount % ${NB_MAX_THUMBNAIL}));
        }
        if (src) {
          this.getPreviewElement().setAttribute("src", src);
        }
      };

      <%-- Handles the thumbnail preview of videos or streamings --%>
      <%-- providerData exists only for streamings --%>
      var ThumbnailPreview = function(mediaElement, providerData) {
        this.intervalID = false;
        this.mediaElement = mediaElement;
        this.providerData = providerData;
        this.changePictureCount = 0;
        var previewElement;

        if (this.providerData) {
          if (!this.providerData.thumbnailPreviewUrls) {
            this.providerData.thumbnailPreviewUrls = [];
          }
          var thumbnailUrl = this.providerData.thumbnailUrl;
          if (thumbnailUrl) {
            this.mediaElement.setAttribute('src', thumbnailUrl);
            this.providerData.thumbnailPreviewUrls.unshift(thumbnailUrl);
          }
        }

        this.getPreviewElement = function() {
          if (!previewElement) {
            previewElement =
                document.querySelector("#tipUrl_" + this.mediaElement.getAttribute("id"));
          }
          return previewElement;
        };

        <%-- Multi browsers compliant --%>
        mediaElement.addEventListener('mouseenter', mouseEnterCallback.bind(this));
        mediaElement.addEventListener('mouseover', mouseEnterCallback.bind(this));
        mediaElement.addEventListener('mouseleave', mouseLeaveCallback.bind(this));
        mediaElement.addEventListener('mouseout', mouseLeaveCallback.bind(this));
      };

      var providerData = '<c:url value="/services/gallery/${componentId}/streamings/streamingId/providerData"/>';

      [].slice.call(document.querySelectorAll(".videoPreview, .streamingPreview"), 0)
          .forEach(function(mediaElement) {
            if (mediaElement.classList.contains("videoPreview")) {

              new ThumbnailPreview(mediaElement, null);

            } else if (mediaElement.classList.contains("streamingPreview")) {

              var streamingId = mediaElement.getAttribute("id").replace(/mediaId_/g, '');
              silverpeasAjax(providerData.replace(/streamingId/g, streamingId)).then(
                  function(request) {
                    var providerData = request.responseAsJson();
                    new ThumbnailPreview(mediaElement, providerData);
                  });

            }
          });
    });
  })();
</script>