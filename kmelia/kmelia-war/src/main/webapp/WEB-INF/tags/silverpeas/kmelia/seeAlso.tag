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

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%@ attribute name="readOnly" required="true"
              type="java.lang.Boolean"
              description="Links cannot be deleted" %>

<%@ attribute name="publicationPK" required="true"
              type="org.silverpeas.core.contribution.publication.model.PublicationPK"
              description="Publication identifier" %>

<%@ attribute name="links" required="true"
              type="java.util.List"
              description="Links of the publication" %>

<%@ attribute name="enabled" required="true"
              type="java.lang.Boolean"
              description="Display this block" %>

<fmt:message var="labelDelete" key="GML.delete"/>

<c:if test="${enabled}">
  <c:if test="${not empty links}">
    <div class="bgDegradeGris" id="block-seeAlso">
      <div class="bgDegradeGris header">
        <h4 class="clean"><fmt:message key="PubReferenceeParAuteur"/></h4>
      </div>
      <ul>
        <c:forEach var="seeAlso" items="${links}">
          <c:set var="seeAlsoClassName" value="seeAlso"/>
          <c:if test="${seeAlso.reverse}">
            <c:set var="seeAlsoClassName" value="seeAlsoReverse"/>
          </c:if>
          <li id="link-${seeAlso.id}" class="showActionsOnMouseOver ${seeAlsoClassName}">
            <a href="${seeAlso.pub.permalink}" class="sp-permalink" title="${seeAlso.pub.description}">${seeAlso.pub.name} <span>- <view:formatDate value="${seeAlso.pub.updateDate}"/></span></a>
            <c:if test="${not readOnly && not seeAlso.reverse}">
              <div class="actionShownOnMouseOver"><a class="delete" href="#" onclick="deleteLink('${seeAlso.id}');return false;" title="${labelDelete}">${labelDelete}</a></div>
            </c:if>
          </li>
        </c:forEach>
      </ul>
    </div>

    <script type="text/javascript">
      function deleteLink(id) {
        var ajaxRequest = sp.ajaxRequest(webContext+"/services/private/publications/${publicationPK.instanceId}/${publicationPK.id}/links/"+id).byDeleteMethod();
        ajaxRequest.send().then(function() {
          $("#link-"+id).remove();
          if ($("#block-seeAlso ul li").size() === 0) {
            $("#block-seeAlso").remove();
          }
        });
      }
    </script>
  </c:if>
</c:if>