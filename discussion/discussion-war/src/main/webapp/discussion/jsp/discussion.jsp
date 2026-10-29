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
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8"/>
  <title><c:out value="${browseContext[1]}"/></title>
  <view:looknfeel/>
  <link rel="stylesheet" href="<c:url value='/discussion/jsp/css/discussion.css'/>"/>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion.js'/>" defer></script>
</head>
<body>
<view:window>
  <view:frame>
    <main id="discussion" data-history="${base}history">
      <header class="discussion-header">
        <div>
          <h1><fmt:message key="discussion.title"/></h1>
          <p class="discussion-subtitle"><fmt:message key="discussion.subtitle"/></p>
        </div>
        <div class="discussion-reading">
          <span class="discussion-unread">
            <span class="discussion-unread-count"><c:out value="${messagePage.unreadCount}"/></span>
            <fmt:message key="discussion.unread"/>
          </span>
          <c:if test="${messagePage.unreadCount gt 0}">
            <form method="post" action="${base}read">
              <input type="hidden" name="sequence" value="${messagePage.lastSequence}"/>
              <button class="discussion-button discussion-button--quiet" type="submit"><fmt:message key="discussion.read"/></button>
            </form>
          </c:if>
        </div>
      </header>
      <c:if test="${canWrite}">
        <form class="discussion-composer" method="post" action="${base}messages/add">
          <label for="discussion-body"><fmt:message key="discussion.compose"/></label>
          <textarea id="discussion-body" name="body" maxlength="2000" required aria-describedby="discussion-compose-hint"
              placeholder="<fmt:message key='discussion.placeholder'/>"></textarea>
          <div class="discussion-composer-footer">
            <span id="discussion-compose-hint" class="discussion-hint"><fmt:message key="discussion.hint"/></span>
            <button class="discussion-button discussion-button--primary" type="submit"><fmt:message key="discussion.post"/></button>
          </div>
        </form>
      </c:if>
      <section id="discussion-history" aria-label="<fmt:message key='discussion.history'/>">
        <h2 class="discussion-history-title"><fmt:message key="discussion.history"/></h2>
        <c:if test="${empty messagePage.messages}">
          <div class="discussion-empty">
            <h3><fmt:message key="discussion.empty"/></h3>
            <p><fmt:message key="discussion.emptyHint"/></p>
          </div>
        </c:if>
        <jsp:include page="messages.jsp"/>
      </section>
      <p id="discussion-error" role="status" hidden><fmt:message key="discussion.error"/></p>
      <div class="discussion-pagination">
        <button id="discussion-older" class="discussion-button discussion-button--quiet" type="button"><fmt:message key="discussion.older"/></button>
      </div>
    </main>
  </view:frame>
</view:window>
</body>
</html>
