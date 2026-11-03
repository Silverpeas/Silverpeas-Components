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
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view" %>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<dialog id="discussion-delete-event-dialog" aria-labelledby="discussion-delete-event-heading" data-error="<fmt:message key='discussion.deleteEventError'/>">
  <header>
    <h2 id="discussion-delete-event-heading"><fmt:message key="discussion.deleteEventHeading"/></h2>
    <button id="discussion-delete-event-close" class="discussion-icon-button" type="button" aria-label="<fmt:message key='GML.close'/>"><svg class="discussion-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg></button>
  </header>
  <p id="discussion-delete-question" data-vote="<fmt:message key='discussion.deleteVoteQuestion'/>" data-event="<fmt:message key='discussion.deleteEventQuestion'/>" data-idea="<fmt:message key='discussion.deleteIdeaQuestion'/>" data-message="<fmt:message key='discussion.deleteMessageQuestion'/>"></p>
  <p id="discussion-delete-event-title"></p>
  <p id="discussion-delete-event-denied" data-vote="<fmt:message key='discussion.deleteVoteDenied'/>" class="discussion-hint" data-event="<fmt:message key='discussion.deleteEventDenied'/>" data-idea="<fmt:message key='discussion.deleteIdeaDenied'/>" hidden><fmt:message key="discussion.deleteEventDenied"/></p>
  <p id="discussion-delete-event-recurrent" class="discussion-hint" hidden><fmt:message key="discussion.deleteEventRecurrent"/></p>
  <footer>
    <button id="discussion-delete-event-cancel" type="button"><fmt:message key="GML.cancel"/></button>
    <button id="discussion-delete-message-only" data-message="<fmt:message key='discussion.deleteMessageConfirm'/>" data-linked="<fmt:message key='discussion.deleteMessageOnly'/>" type="button"><fmt:message key="discussion.deleteMessageOnly"/></button>
    <button id="discussion-delete-message-and-event" class="button" type="button"><fmt:message key="discussion.deleteMessageAndEvent"/></button>
  </footer>
</dialog>
