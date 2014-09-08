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
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/gallery" prefix="gallery" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<gallery:viewMediaLayout>
  <jsp:attribute name="headerBloc">
    <jsp:useBean id="media" scope="request" type="com.silverpeas.gallery.model.Streaming"/>
    <gallery:streamingLibrary/>
    <script type="text/javascript">
      $(document).ready(function() {

        var $characteristics = $('.fileCharacteristic');
        var $characteristicsContainer = $('p', $characteristics);
        $characteristics.hide();

        getPromiseOfStreamingProviderData('${media.homepageUrl}').then(function(providerData) {
          if (!providerData) {
            return this;
          }
          var metaDataFound = false;
          if (providerData.definition.width > 0) {
            metaDataFound = true;
            $('<span>',
                {'class' : 'fileCharacteristicSize'}).append('<fmt:message key="gallery.dimension"/> <b>' +
                    providerData.definition.width + ' x ' + providerData.definition.height +
                    '<fmt:message key="gallery.pixels"/></b>').prependTo($characteristicsContainer);
          }
          if (providerData.formattedDurationHMS) {
            metaDataFound = true;
            $('<span>',
                {'class' : 'fileCharacteristicDuration'}).append('<fmt:message key="gallery.duration"/><b>' +
                    providerData.formattedDurationHMS + '</b>').prependTo($characteristicsContainer);
          }

          if (metaDataFound) {
            $characteristics.show();
          }

          if (typeof displayPlayer === 'function') {
            displayPlayer(providerData);
          }
        });
      });
    </script>
  </jsp:attribute>

  <jsp:attribute name="mediaPreviewBloc">
    <gallery:streamingPlayer streaming="${media}"/>
  </jsp:attribute>
</gallery:viewMediaLayout>