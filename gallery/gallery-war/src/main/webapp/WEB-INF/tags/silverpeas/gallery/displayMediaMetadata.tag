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

<c:set var="_userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${_userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%@ attribute name="media" required="true"
              type="com.silverpeas.gallery.model.Media"
              description="The photo" %>
<%@ attribute name="isViewMetadata" required="true"
              type="java.lang.Boolean"
              description="Is view of metadata activated" %>

<c:set var="_photo" value="${media.photo}"/>

<c:if test="${not empty _photo && isViewMetadata and not empty _photo.metaDataProperties}">
  <div class="metadata bgDegradeGris" id="metadata">
    <div class="header bgDegradeGris">
      <h4 class="clean"><fmt:message key="GML.metadata"/></h4>
    </div>
    <div id="metadata_list">
      <c:forEach var="metaDataKey" items="${_photo.metaDataProperties}">
        <c:set var="metaData" value="${_photo.getMetaData(metaDataKey)}"/>
        <jsp:useBean id="metaData" type="com.silverpeas.gallery.model.MetaData"/>
        <p id="metadata_${fn:replace(metaData.label, ' ', '_')}">${metaData.label}
          <b><c:out value="${metaData.date ? silfn:formatDateAndHour(metaData.dateValue, _userLanguage) : metaData.value}"/></b>
        </p>
      </c:forEach>
    </div>
  </div>
</c:if>