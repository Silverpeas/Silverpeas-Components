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
<c:url var="visioBase" value="${requestScope.componentUriBase}"/>
<dialog id="discussion-visio-dialog" class="discussion-event-dialog" aria-labelledby="discussion-visio-heading"
    data-options="${visioBase}visio-options" data-error="<fmt:message key='discussion.visioError'/>"
    data-unavailable="<fmt:message key='discussion.visioUnavailable'/>">
  <form id="discussion-visio-form" method="post" action="${visioBase}meetings">
    <header><h2 id="discussion-visio-heading"><fmt:message key="discussion.createVisio"/></h2>
      <button class="discussion-icon-button discussion-visio-cancel" type="button" aria-label="<fmt:message key='GML.close'/>"><svg class="discussion-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18" fill="none" stroke="currentColor" stroke-width="1.5"/></svg></button>
    </header>
    <fieldset id="discussion-visio-fields">
      <label for="discussion-visio-title"><fmt:message key="discussion.visioTitle"/></label>
      <input id="discussion-visio-title" name="title" type="text" required maxlength="160"/>
      <p class="discussion-hint"><span id="discussion-visio-count">0</span> <fmt:message key="discussion.visioInvitees"/></p>
      <p class="discussion-hint"><fmt:message key="discussion.visioHint"/></p>
    </fieldset>
    <p id="discussion-visio-error" class="discussion-presence-error" role="alert" hidden></p>
    <footer><button class="discussion-visio-cancel" type="button"><fmt:message key="GML.cancel"/></button>
      <button id="discussion-visio-submit" class="button" type="submit"><fmt:message key="discussion.createVisio"/></button></footer>
  </form>
</dialog>
<p id="discussion-visio-status" class="discussion-hint" role="status" hidden><fmt:message key="discussion.visioCreated"/>
  <a id="discussion-visio-join" target="_blank" rel="noopener noreferrer"><fmt:message key="discussion.visioJoin"/></a>
</p>
