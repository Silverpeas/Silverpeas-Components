<%--

    Copyright (C) 2000 - 2026 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<div class="discussion-participants-list">
  <h2 id="discussion-people-heading"><fmt:message key="discussion.participants"/> <span class="discussion-people-count"><c:out value="${fn:length(participants)}"/></span></h2>
  <p class="discussion-presence-hint"><fmt:message key="discussion.presenceHint"/></p>
  <c:if test="${empty participants}"><p><fmt:message key="discussion.noParticipants"/></p></c:if>
  <ul>
    <c:forEach items="${participants}" var="participant">
      <c:url var="participantAvatar" value="${participant.avatar}"/>
      <li class="discussion-person" data-user-id="<c:out value='${participant.id}'/>">
        <img class="discussion-person-avatar" src="<c:out value='${participantAvatar}'/>" alt="" loading="lazy" width="30" height="30"/>
        <div class="discussion-person-info">
          <span class="discussion-person-name"><c:out value="${participant.name}"/></span>
          <span class="discussion-presence ${participant.connected ? 'is-connected' : ''}">
            <span class="discussion-presence-dot" aria-hidden="true"></span>
            <span class="discussion-presence-label"><c:choose><c:when test="${participant.connected}"><fmt:message key="discussion.connected"/></c:when><c:otherwise><fmt:message key="discussion.disconnected"/></c:otherwise></c:choose></span>
          </span>
        </div>
      </li>
    </c:forEach>
  </ul>
</div>
