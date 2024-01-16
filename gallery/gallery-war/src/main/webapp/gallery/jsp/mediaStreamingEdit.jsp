<%--
  Copyright (C) 2000 - 2024 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "https://www.silverpeas.org/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%-- Labels --%>
<fmt:message key="GML.validate" var="validateLabel"/>

<%-- Constants --%>
<view:setConstant var="mediaType" constant="org.silverpeas.components.gallery.constant.MediaType.Streaming"/>
<view:setConstant var="SMALL_RESOLUTION" constant="org.silverpeas.components.gallery.constant.MediaResolution.SMALL"/>

<gallery:editMediaLayout mediaType="${mediaType}">
  <jsp:attribute name="headerBloc">
    <jsp:useBean id="media" scope="request" type="org.silverpeas.components.gallery.model.Streaming"/>
    <gallery:streamingLibrary/>
    <script type="text/javascript">
      $(document).ready(function() {
        var $validateButton = $($('.buttonPane .milieuBoutonV5 a')[0]);
        var $dummyValidate = $('<a>').append('${validateLabel}');
        var $validateButtonContainer = $validateButton.parent();
        var oldValue = '${media.homepageUrl}';
        $('#fileId').on('blur', function() {
          var value = $.trim($(this).val());
          var $title = $('#title');
          var $author = $('#author');
          if (value && value != oldValue) {
            $.progressMessage();
            getPromiseOfStreamingProviderData(value).then(function(providerData) {
              if (!providerData) {
                return this;
              }
              if (providerData.title) {
                $title.val(providerData.title);
              }
              if (providerData.author) {
                $author.val(providerData.author);
              }
            }).always(function() {
              $dummyValidate.detach();
              $validateButtonContainer.append($validateButton);
              $validateButtonContainer.removeClass('validateNotAvailable');
              $.closeProgressMessage();
            });
          } else {
            $dummyValidate.detach();
            $validateButtonContainer.append($validateButton);
            $validateButtonContainer.removeClass('validateNotAvailable');
            $.closeProgressMessage();
          }
        }).on('focus', function() {
          oldValue = $.trim($(this).val());
          $validateButton.detach();
          $validateButtonContainer.append($dummyValidate);
          $validateButtonContainer.addClass('validateNotAvailable');
          this.select();
        });
      });
    </script>
  </jsp:attribute>

  <jsp:attribute name="mediaPreviewBloc">
    <gallery:streamingPlayer streaming="${media}"/>
  </jsp:attribute>
</gallery:editMediaLayout>
