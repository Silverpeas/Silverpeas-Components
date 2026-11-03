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
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<c:url var="channelsBase" value="${requestScope.instanceUriBase}"/>
<div class="discussion-channel-groups">
  <ul class="discussion-channel-list">
    <c:forEach items="${channels}" var="item">
      <c:if test="${not item.conversation.archived}">
        <li data-channel-scope="${item.conversation.scopeKey}" data-unread="${item.unread}" data-mentions="${item.mentions}" data-archived="${item.conversation.archived}">
          <a href="${channelsBase}channels/${item.conversation.scopeKey}/Main" class="discussion-channel-link" title="<c:out value='${item.conversation.channelDescription}'/>"><span class="discussion-channel-name"># <c:choose><c:when test="${empty item.conversation.channelName}"><fmt:message key="discussion.general"/></c:when><c:otherwise><c:out value="${item.conversation.channelName}"/></c:otherwise></c:choose></span><c:if test="${item.mentions gt 0}"><span class="discussion-channel-mentions">@ <c:out value="${item.mentions}"/></span></c:if><c:if test="${item.unread gt 0}"><span class="discussion-channel-count" aria-label="${item.unread} <fmt:message key='discussion.unread'/>">●</span></c:if></a>
          <c:if test="${canManageChannels}"><button class="discussion-icon-button discussion-channel-settings" type="button" title="<fmt:message key='discussion.manageChannel'/>" aria-label="<fmt:message key='discussion.manageChannel'/>" hidden><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#settings"/></svg></button></c:if>
        </li>
      </c:if>
    </c:forEach>
  </ul>
  <c:set var="archivedChannelCount" value="0"/>
  <c:forEach items="${channels}" var="item"><c:if test="${item.conversation.archived}"><c:set var="archivedChannelCount" value="${archivedChannelCount + 1}"/></c:if></c:forEach>
  <c:if test="${archivedChannelCount gt 0}">
    <details id="discussion-channel-archives" class="discussion-channel-archives">
      <summary><fmt:message key="discussion.channelArchives"/> <span class="discussion-channel-count"><c:out value="${archivedChannelCount}"/></span></summary>
      <ul class="discussion-channel-list">
        <c:forEach items="${channels}" var="item">
          <c:if test="${item.conversation.archived}">
            <li data-channel-scope="${item.conversation.scopeKey}" data-unread="${item.unread}" data-mentions="${item.mentions}" data-archived="${item.conversation.archived}">
              <a href="${channelsBase}channels/${item.conversation.scopeKey}/Main" class="discussion-channel-link" title="<c:out value='${item.conversation.channelDescription}'/>"><span class="discussion-channel-name"># <c:choose><c:when test="${empty item.conversation.channelName}"><fmt:message key="discussion.general"/></c:when><c:otherwise><c:out value="${item.conversation.channelName}"/></c:otherwise></c:choose></span><c:if test="${item.mentions gt 0}"><span class="discussion-channel-mentions">@ <c:out value="${item.mentions}"/></span></c:if><c:if test="${item.unread gt 0}"><span class="discussion-channel-count" aria-label="${item.unread} <fmt:message key='discussion.unread'/>">●</span></c:if></a>
              <c:if test="${canManageChannels}"><button class="discussion-icon-button discussion-channel-settings" type="button" title="<fmt:message key='discussion.manageChannel'/>" aria-label="<fmt:message key='discussion.manageChannel'/>" hidden><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#settings"/></svg></button></c:if>
            </li>
          </c:if>
        </c:forEach>
      </ul>
    </details>
  </c:if>
</div>
