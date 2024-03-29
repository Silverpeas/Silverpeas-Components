<%--
  Copyright (C) 2000 - 2024 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

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
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/formsOnline" prefix="formsOnline" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%-- Attributes --%>
<%@ attribute name="requests" required="true"
              type="org.silverpeas.core.util.SilverpeasList"
              description="The user requests" %>

<%@ attribute name="title" required="true" type="java.lang.String" %>
<%@ attribute name="state" required="true" type="java.lang.String" %>

<c:if test="${not empty requests}">
  <div class="secteur-container my-formsOnline" id="my-formsOnline-${state}">
    <div class="header">
      <h3 class="my-formsOnline-title">${title}</h3>
    </div>
    <ul>
      <view:listPane var="${state}UserRequests" routingAddress="Main" numberLinesPerPage="10">
        <view:listItems items="${requests}" var="request">
          <li><a href="ViewRequest?Id=${request.id}">
            <span class="ask-form-date"><view:formatDateTime value="${request.creationDate}"/></span>
            <span class="form-title">${request.form.title}</span></a>
          </li>
        </view:listItems>
      </view:listPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          sp.listPane.ajaxControls('#my-formsOnline-${state}');
        });
      </script>
    </ul>
  </div>
</c:if>