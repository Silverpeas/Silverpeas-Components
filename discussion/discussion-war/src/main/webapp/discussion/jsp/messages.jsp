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
<fmt:message key="discussion.like" var="likeLabel"/>
<fmt:message key="discussion.unlike" var="unlikeLabel"/>
<div class="discussion-page" data-before="${messagePage.nextBefore}" data-more="${messagePage.hasMore}">
  <c:set var="unreadBoundaryShown" value="${false}"/>
  <c:forEach items="${messagePage.chronologicalMessages}" var="message">
    <c:if test="${message.unread and not unreadBoundaryShown}">
      <div class="discussion-unread-divider" role="separator" aria-label="<fmt:message key='discussion.unread'/>"><span><fmt:message key="discussion.unread"/></span></div>
      <c:set var="unreadBoundaryShown" value="${true}"/>
    </c:if>
    <article id="message-${message.id}" class="discussion-message ${message.unread ? 'discussion-message--unread' : ''} ${currentUser.id eq message.creatorId ? 'discussion-message--own' : ''}" data-sequence="${message.sequenceNumber}">
      <div class="discussion-avatar" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(message.creator.displayedName, 0, 1))}"/></div>
      <div class="discussion-message-content">
        <header class="discussion-message-header">
          <span class="discussion-author"><c:out value="${message.creator.displayedName}"/></span>
          <time><fmt:formatDate value="${message.creationDate}" type="both" dateStyle="short" timeStyle="short"/></time>
          <c:if test="${message.unread}"><span class="discussion-unread-badge"><fmt:message key="discussion.unreadMessage"/></span></c:if>
        </header>
        <c:choose>
          <c:when test="${message.deleted}"><p class="discussion-deleted"><fmt:message key="discussion.deleted"/></p></c:when>
          <c:otherwise>
            <p class="discussion-body"><c:out value="${message.body}"/></p>
            <div class="discussion-message-footer">
            <c:if test="${canWrite and (currentUser.id eq message.creatorId or highestUserRole.name() eq 'ADMIN')}">
              <div class="discussion-actions">
                <details class="discussion-edit">
                  <summary class="discussion-icon-button" title="${fn:escapeXml(editLabel)}" aria-label="${fn:escapeXml(editLabel)}"><svg class="discussion-icon" aria-hidden="true"><use href="${discussionIcons}#edit"/></svg></summary>
                  <form method="post" action="${base}messages/${message.id}/edit">
                    <input type="hidden" name="version" value="${message.version}"/>
                    <textarea name="body" maxlength="2000" required aria-label="<fmt:message key='discussion.message'/>"><c:out value="${message.body}"/></textarea>
                    <button class="sp_button discussion-button--primary" type="submit"><fmt:message key="discussion.save"/></button>
                  </form>
                </details>
                <form class="discussion-delete" method="post" action="${base}messages/${message.id}/delete">
                  <input type="hidden" name="version" value="${message.version}"/>
                  <button class="discussion-icon-button discussion-button--danger" type="submit" title="${fn:escapeXml(deleteLabel)}" aria-label="${fn:escapeXml(deleteLabel)}"><svg class="discussion-icon" aria-hidden="true"><use href="${discussionIcons}#delete"/></svg></button>
                </form>
              </div>
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
          </c:otherwise>
        </c:choose>
      </div>
    </article>
  </c:forEach>
</div>
