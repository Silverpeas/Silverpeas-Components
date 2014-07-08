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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%@ attribute name="albumPath" required="false"
              type="java.util.List"
              description="The album path." %>
<c:if test="${albumPath != null}">
  <jsp:useBean id="albumPath"
               type="java.util.List<com.stratelia.webactiv.util.node.model.NodeDetail>"
               scope="page"/>
</c:if>

<c:set var="_isViewNotVisible" value="${false}"/>
<%@ attribute name="isViewNotVisible" required="false"
              type="java.lang.Boolean"
              description="Parameter used if albumPath is not filled." %>
<c:if test="${isViewNotVisible != null}">
  <c:set var="_isViewNotVisible" value="${isViewNotVisible}"/>
</c:if>

<c:set var="_searchKeyword" value=""/>
<%@ attribute name="searchKeyword" required="false"
              type="java.lang.String"
              description="Parameter used if albumPath is not filled." %>
<c:if test="${searchKeyword != null}">
  <c:set var="_searchKeyword" value="'${searchKeyword}'"/>
</c:if>

<view:browseBar>
  <c:choose>
    <c:when test="${albumPath != null}">
      <c:if test="${not empty albumPath}">
        <c:forEach var="albumPathPart" items="${albumPath}">
          <c:if test="${albumPathPart.id != 0}">
            <view:browseBarElt label="${albumPathPart.name}" link="ViewAlbum?Id=${albumPathPart.id}" id="album${albumPathPart.id}"/>
          </c:if>
        </c:forEach>
      </c:if>
    </c:when>
    <c:otherwise>
      <c:choose>
        <c:when test="${_isViewNotVisible}">
          <fmt:message key="gallery.viewNotVisible" var="viewNotVisibleLabel"/>
          <view:browseBarElt label="${viewNotVisibleLabel}" link="#"/>
        </c:when>
        <c:otherwise>
          <fmt:message key="gallery.searchAdvanced" var="searchAdvancedLabel"/>
          <fmt:message key="gallery.resultSearch" var="resultSearchLabel"/>
          <view:browseBarElt label="${searchAdvancedLabel} > ${resultSearchLabel} ${not empty _searchKeyword ? _searchKeyword : ''}" link="SearchAdvanced"/>
        </c:otherwise>
      </c:choose>
    </c:otherwise>
  </c:choose>
</view:browseBar>