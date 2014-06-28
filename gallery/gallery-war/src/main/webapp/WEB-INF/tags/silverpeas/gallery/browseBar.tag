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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ attribute name="albumPath" required="true"
              type="java.util.List"
              description="The album path." %>
<jsp:useBean id="albumPath"
             type="java.util.List<com.stratelia.webactiv.util.node.model.NodeDetail>"
             scope="page"/>

<view:browseBar>
  <c:if test="${not empty albumPath}">
    <c:forEach var="albumPathPart" items="${albumPath}">
      <c:if test="${albumPathPart.id != 0}">
        <view:browseBarElt label="${albumPathPart.name}" link="ViewAlbum?Id=${albumPathPart.id}" id="${albumPathPart.id}"/>
      </c:if>
    </c:forEach>
  </c:if>
</view:browseBar>
