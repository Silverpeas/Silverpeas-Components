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

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%@ attribute name="currentMediaResolution" required="true"
              type="com.silverpeas.gallery.constant.MediaResolution"
              description="The current displayed mediaResolution." %>
<%@ attribute name="nbMediaPerPage" required="true"
              type="java.lang.Integer"
              description="The number of media displayed in a page." %>
<%@ attribute name="currentPageIndex" required="true"
              type="java.lang.Integer"
              description="The first media of the list displayed." %>
<%@ attribute name="mediaList" required="true"
              type="java.util.List"
              description="The current album displayed." %>
<jsp:useBean id="mediaList"
             type="java.util.List<com.silverpeas.gallery.model.Media>"
             scope="page"/>

<table border="0" width="100%">
  <tr>
    <td align="center" width="100%" class=ArrayNavigation>
      <c:out value="${silfn:formatPaginationFromCurrentPageIndex(nbMediaPerPage, fn:length(mediaList), currentPageIndex)}"/>
      <fmt:message key="gallery.media" var="oneMediaLabel"/>
      <fmt:message key="gallery.media.several" var="severalMediaLabel"/>
      ${fn:length(mediaList) <= 1 ? oneMediaLabel : severalMediaLabel}
    </td>
    <td align="right" nowrap>
      <gallery:albumListDisplaySelector currentMediaResolution="${currentMediaResolution}"/>
    </td>
  </tr>
</table>