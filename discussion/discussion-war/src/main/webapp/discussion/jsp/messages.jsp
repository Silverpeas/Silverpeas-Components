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
<c:url var="base" value="${requestScope.componentUriBase}"/>
<div class="discussion-page" data-before="${messagePage.nextBefore}" data-more="${messagePage.hasMore}">
  <c:forEach items="${messagePage.messages}" var="message">
    <article class="discussion-message" data-sequence="${message.sequenceNumber}">
      <p class="discussion-author"><c:out value="${message.creator.displayedName}"/>
        <time><fmt:formatDate value="${message.creationDate}" type="both" dateStyle="short" timeStyle="short"/></time>
      </p>
      <c:choose>
        <c:when test="${message.deleted}"><p><fmt:message key="discussion.deleted"/></p></c:when>
        <c:otherwise>
          <p class="discussion-body"><c:out value="${message.body}"/></p>
          <c:if test="${canWrite and (currentUser.id eq message.creatorId or highestUserRole.name() eq 'ADMIN')}">
            <details>
              <summary><fmt:message key="discussion.edit"/></summary>
              <form method="post" action="${base}messages/${message.id}/edit">
                <input type="hidden" name="version" value="${message.version}"/>
                <textarea name="body" maxlength="2000" required aria-label="<fmt:message key='discussion.message'/>"><c:out value="${message.body}"/></textarea>
                <button type="submit"><fmt:message key="discussion.edit"/></button>
              </form>
            </details>
            <form method="post" action="${base}messages/${message.id}/delete">
              <input type="hidden" name="version" value="${message.version}"/>
              <button type="submit"><fmt:message key="discussion.delete"/></button>
            </form>
          </c:if>
        </c:otherwise>
      </c:choose>
    </article>
  </c:forEach>
</div>
