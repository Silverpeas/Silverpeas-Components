<%--
  Copyright (C) 2000 - 2015 Silverpeas
  
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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%@ attribute name="navigationAuthorized" required="true"
              type="java.lang.Boolean"
              description="Indicates if the navigation is authorized" %>

<c:set var="folderPath" value="${requestScope.Path}"/>
<script type="text/javascript">
  function silverCrawlerBrowseBarLink(path) {
    jQuery.progressMessage();
    jQuery("<form>", {
      'method' : 'GET', 'action' : 'GoToDirectory'
    }).append(jQuery('<input>', {
      "type" : "text", "name" : "DirectoryPath", "value" : path
    })).submit();
  }
</script>
<view:browseBar>
  <c:forEach var="folderPathPart" items="${folderPath}">
    <c:set var="_link" value=""/>
    <c:if test="${navigationAuthorized}">
      <c:set var="_link" value="javascript:silverCrawlerBrowseBarLink('${folderPathPart}')"/>
    </c:if>
    <view:browseBarElt label="${folderPathPart}" link="${_link}"/>
  </c:forEach>
</view:browseBar>