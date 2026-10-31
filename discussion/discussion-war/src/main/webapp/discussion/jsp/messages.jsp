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
<c:url var="base" value="${requestScope.componentUriBase}"/>
<c:url var="discussionIcons" value="/discussion/jsp/icons/discussion-icons.svg"/>
<fmt:message key="discussion.edit" var="editLabel"/>
<fmt:message key="discussion.delete" var="deleteLabel"/>
<fmt:message key="discussion.pin" var="pinLabel"/>
<fmt:message key="discussion.unpin" var="unpinLabel"/>
<fmt:message key="discussion.like" var="likeLabel"/>
<fmt:message key="discussion.unlike" var="unlikeLabel"/>
<div class="discussion-page" data-before="${messagePage.nextBefore}" data-more="${messagePage.hasMore}" data-last-sequence="${messagePage.lastSequence}" data-unread-count="${messagePage.unreadCount}">
  <c:set var="unreadBoundaryShown" value="${false}"/>
  <c:forEach items="${messagePage.chronologicalMessages}" var="message">
    <c:choose>
      <c:when test="${message.deleted}">
        <article id="message-${message.id}" class="discussion-message" data-sequence="${message.sequenceNumber}" hidden></article>
      </c:when>
      <c:otherwise>
    <c:if test="${message.unread and not unreadBoundaryShown}">
      <div class="discussion-unread-divider" role="separator" aria-label="<fmt:message key='discussion.unread'/>"><span><fmt:message key="discussion.unread"/></span></div>
      <c:set var="unreadBoundaryShown" value="${true}"/>
    </c:if>
    <article id="message-${message.id}" class="discussion-message ${message.unread ? 'discussion-message--unread' : ''} ${currentUser.id eq message.creatorId ? 'discussion-message--own' : ''}" data-sequence="${message.sequenceNumber}">
      <div class="discussion-avatar" aria-hidden="true">
        <span><c:out value="${fn:toUpperCase(fn:substring(message.creator.displayedName, 0, 1))}"/></span>
        <c:set var="messageAvatar" value="${message.creator.smallAvatar}"/>
        <c:if test="${not empty messageAvatar and messageAvatar ne defaultAvatarPath}">
          <c:url var="messageAvatarUrl" value="${messageAvatar}"/>
          <img src="<c:out value='${messageAvatarUrl}'/>" alt="" width="30" height="30" loading="lazy"/>
        </c:if>
      </div>
      <div class="discussion-message-content">
        <header class="discussion-message-header">
          <span class="discussion-author"><c:out value="${message.creator.displayedName}"/></span>
          <c:if test="${message.pinned}"><span class="discussion-pinned-badge"><svg class="discussion-icon" aria-hidden="true"><use href="${discussionIcons}#pin"/></svg> <fmt:message key="discussion.pinned"/></span></c:if>
          <time><fmt:formatDate value="${message.creationDate}" type="both" dateStyle="short" timeStyle="short"/></time>
          <c:if test="${message.unread}"><span class="discussion-unread-badge"><fmt:message key="discussion.unreadMessage"/></span></c:if>
        </header>
            <c:if test="${not empty fn:trim(message.body)}"><p class="discussion-body"><c:out value="${message.body}"/></p></c:if>
            <c:if test="${not empty messageImages[message.id]}">
              <div class="discussion-message-images">
                <c:forEach items="${messageImages[message.id]}" var="image">
                  <a href="<c:out value='${image.url}'/>" target="_blank" rel="noopener noreferrer" aria-label="<fmt:message key='discussion.openImage'/>">
                    <img src="<c:out value='${image.url}'/>" alt="<fmt:message key='discussion.attachedImage'/>" loading="lazy"/>
                  </a>
                </c:forEach>
              </div>
            </c:if>
            <div class="discussion-message-footer">
            <c:if test="${canWrite and (currentUser.id eq message.creatorId or highestUserRole.name() eq 'ADMIN')}">
              <div class="discussion-actions">
                <details class="discussion-edit">
                  <summary class="discussion-icon-button" title="${fn:escapeXml(editLabel)}" aria-label="${fn:escapeXml(editLabel)}"><svg class="discussion-icon" aria-hidden="true"><use href="${discussionIcons}#edit"/></svg></summary>
                  <form method="post" action="${base}messages/${message.id}/edit">
                    <input type="hidden" name="version" value="${message.version}"/>
                    <textarea name="body" maxlength="2000" data-allow-empty="${not empty messageImages[message.id]}" required aria-label="<fmt:message key='discussion.message'/>"><c:out value="${message.body}"/></textarea>
                    <button class="sp_button discussion-button--primary" type="submit"><fmt:message key="discussion.save"/></button>
                  </form>
                </details>
                <form class="discussion-delete" method="post" action="${base}messages/${message.id}/delete">
                  <input type="hidden" name="version" value="${message.version}"/>
                  <button class="discussion-icon-button discussion-button--danger" type="submit" title="${fn:escapeXml(deleteLabel)}" aria-label="${fn:escapeXml(deleteLabel)}"><svg class="discussion-icon" aria-hidden="true"><use href="${discussionIcons}#delete"/></svg></button>
                </form>
              </div>
            </c:if>
            <c:if test="${canReact}">
              <form class="discussion-pin-form" method="post" action="${base}messages/${message.id}/pin"
                  data-pin-label="${fn:escapeXml(pinLabel)}" data-unpin-label="${fn:escapeXml(unpinLabel)}">
                <input type="hidden" name="pinned" value="${not message.pinned}"/>
                <button class="discussion-icon-button" type="submit" aria-pressed="${message.pinned}"
                    title="${fn:escapeXml(message.pinned ? unpinLabel : pinLabel)}" aria-label="${fn:escapeXml(message.pinned ? unpinLabel : pinLabel)}">
                  <svg class="discussion-icon" aria-hidden="true"><use href="${discussionIcons}#pin"/></svg>
                </button>
                <span class="discussion-pin-error" role="status" hidden><fmt:message key="discussion.pinError"/></span>
              </form>
            </c:if>
            <c:set var="reaction" value="${messageLikes[message.id]}"/>
            <c:choose>
              <c:when test="${canReact}">
                <form class="discussion-like-form" method="post" action="${base}messages/${message.id}/like"
                    data-like-label="${fn:escapeXml(likeLabel)}" data-unlike-label="${fn:escapeXml(unlikeLabel)}">
                  <input type="hidden" name="liked" value="${empty reaction or not reaction.liked}"/>
                  <button class="discussion-icon-button discussion-like-button" type="submit"
                      aria-pressed="${not empty reaction and reaction.liked}"
                      aria-label="${fn:escapeXml(not empty reaction and reaction.liked ? unlikeLabel : likeLabel)}"
                      title="${fn:escapeXml(not empty reaction and reaction.liked ? unlikeLabel : likeLabel)}">
                    <svg class="discussion-icon" aria-hidden="true"><use href="${discussionIcons}#heart"/></svg>
                    <span class="discussion-like-count"><c:out value="${empty reaction ? 0 : reaction.count}"/></span>
                  </button>
                  <span class="discussion-like-error" role="status" hidden><fmt:message key="discussion.likeError"/></span>
                </form>
              </c:when>
              <c:otherwise><span class="discussion-readonly-likes"><svg class="discussion-icon" aria-hidden="true"><use href="${discussionIcons}#heart"/></svg> <c:out value="${empty reaction ? 0 : reaction.count}"/></span></c:otherwise>
            </c:choose>
            </div>
      </div>
    </article>
      </c:otherwise>
    </c:choose>
  </c:forEach>
</div>
