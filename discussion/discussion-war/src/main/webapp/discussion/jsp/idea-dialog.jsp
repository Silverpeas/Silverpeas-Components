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
<c:url var="ideaBase" value="${requestScope.componentUriBase}"/>
<dialog id="discussion-idea-dialog" class="discussion-event-dialog" aria-labelledby="discussion-idea-heading"
    data-options="${ideaBase}idea-options" data-loading="<fmt:message key='discussion.ideaLoading'/>"
    data-error="<fmt:message key='discussion.ideaError'/>" data-unavailable="<fmt:message key='discussion.ideaUnavailable'/>">
  <form id="discussion-idea-form" method="post" action="${ideaBase}ideas">
    <header><h2 id="discussion-idea-heading"><fmt:message key="discussion.proposeIdea"/></h2><button class="discussion-icon-button discussion-idea-cancel" type="button" aria-label="<fmt:message key='GML.close'/>"><svg class="discussion-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg></button></header>
    <p id="discussion-idea-loading" class="discussion-hint" role="status"></p>
    <fieldset id="discussion-idea-fields" disabled>
      <label for="discussion-idea-box"><fmt:message key="discussion.ideaBox"/></label>
      <select id="discussion-idea-box" name="box" required></select>
      <label for="discussion-idea-title"><fmt:message key="discussion.eventTitle"/></label>
      <input id="discussion-idea-title" name="title" type="text" required maxlength="160"/>
      <label for="discussion-idea-description"><fmt:message key="discussion.ideaDescription"/></label>
      <textarea id="discussion-idea-description" name="description" rows="5" maxlength="2000" required></textarea>
      <p class="discussion-hint"><fmt:message key="discussion.ideaModeration"/></p>
    </fieldset>
    <p id="discussion-idea-error" class="discussion-presence-error" role="alert" hidden></p>
    <footer><button id="discussion-idea-retry" type="button" hidden><fmt:message key="discussion.eventRetry"/></button><button class="discussion-idea-cancel" type="button"><fmt:message key="GML.cancel"/></button><button id="discussion-idea-submit" class="button" type="submit" disabled><fmt:message key="discussion.proposeIdea"/></button></footer>
  </form>
</dialog>
<p id="discussion-idea-status" class="discussion-hint" role="status" data-success="<fmt:message key='discussion.ideaCreated'/>" data-pending="<fmt:message key='discussion.ideaPending'/>" hidden></p>
