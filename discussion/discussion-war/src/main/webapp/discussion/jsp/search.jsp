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
<c:url var="base" value="${requestScope.instanceUriBase}"/>
<div class="discussion-search-page" data-next-offset="${searchPage.nextOffset}" data-has-more="${searchPage.hasMore}" data-total="${searchPage.total}">
  <c:if test="${empty searchPage.messages}"><p class="discussion-hint"><fmt:message key="discussion.searchEmpty"/></p></c:if>
  <ul class="discussion-search-list">
    <c:forEach items="${searchPage.messages}" var="message">
      <li>
        <a href="${base}messages/${message.id}#message-${message.id}">
          <small class="discussion-search-channel"># <c:out value="${searchChannels[message.conversationId]}"/></small>
          <strong><c:out value="${message.creator.displayedName}"/></strong>
          <c:if test="${not empty message.threadRootId}"><small class="discussion-thread-search-label"><fmt:message key="discussion.inThread"/></small></c:if>
          <time><fmt:formatDate value="${message.creationDate}" type="both" dateStyle="short" timeStyle="short"/></time>
          <span><c:out value="${searchBodies[message.id]}"/></span>
        </a>
      </li>
    </c:forEach>
  </ul>
</div>
