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
<c:url var="eventBase" value="${requestScope.componentUriBase}"/>
<dialog id="discussion-event-dialog" class="discussion-event-dialog" aria-labelledby="discussion-event-heading"
    data-options="${eventBase}event-options" data-loading="<fmt:message key='discussion.eventLoading'/>"
    data-error="<fmt:message key='discussion.eventError'/>" data-unavailable="<fmt:message key='discussion.eventUnavailable'/>"
    data-invites="<fmt:message key='discussion.eventInvites'/>" data-period-error="<fmt:message key='discussion.eventPeriodError'/>">
  <form id="discussion-event-form" method="post" action="${eventBase}events">
    <header><h2 id="discussion-event-heading"><fmt:message key="discussion.createEvent"/></h2><button class="discussion-icon-button discussion-event-cancel" type="button" aria-label="<fmt:message key='GML.close'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#close"/></svg></button></header>
    <p id="discussion-event-loading" class="discussion-hint" role="status"></p>
    <fieldset id="discussion-event-fields" disabled>
      <label for="discussion-event-calendar"><fmt:message key="discussion.eventAlmanach"/></label>
      <select id="discussion-event-calendar" name="calendar" required></select>
      <label for="discussion-event-title"><fmt:message key="discussion.eventTitle"/></label>
      <input id="discussion-event-title" name="title" type="text" required maxlength="160"/>
      <div class="discussion-event-dates">
        <div><label for="discussion-event-start"><fmt:message key="discussion.eventStart"/></label><input id="discussion-event-start" name="start" type="datetime-local" min="0001-01-01T00:00" max="9999-12-31T23:59" step="60" required/></div>
        <div><label for="discussion-event-end"><fmt:message key="discussion.eventEnd"/></label><input id="discussion-event-end" name="end" type="datetime-local" min="0001-01-01T00:00" max="9999-12-31T23:59" step="60" required/></div>
      </div>
      <p class="discussion-hint"><fmt:message key="discussion.eventTimezone"/> <span id="discussion-event-zone"></span></p>
      <label for="discussion-event-description"><fmt:message key="discussion.eventDescription"/></label>
      <textarea id="discussion-event-description" name="description" rows="3" maxlength="2000"></textarea>
      <p id="discussion-event-invites" class="discussion-hint"></p>
    </fieldset>
    <p id="discussion-event-error" class="discussion-presence-error" role="alert" hidden></p>
    <footer><button id="discussion-event-retry" class="discussion-button--quiet" type="button" hidden><fmt:message key="discussion.eventRetry"/></button><button class="discussion-event-cancel" type="button"><fmt:message key="GML.cancel"/></button><button id="discussion-event-submit" class="button" type="submit" disabled><fmt:message key="discussion.eventSubmit"/></button></footer>
  </form>
</dialog>
