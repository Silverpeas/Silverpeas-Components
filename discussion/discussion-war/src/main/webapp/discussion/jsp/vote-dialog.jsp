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
<c:url var="voteBase" value="${requestScope.componentUriBase}"/>
<dialog id="discussion-vote-dialog" class="discussion-event-dialog" aria-labelledby="discussion-vote-heading"
    data-options="${voteBase}vote-options" data-answer-error="<fmt:message key='discussion.voteAnswersHint'/>" data-loading="<fmt:message key='discussion.voteLoading'/>"
    data-error="<fmt:message key='discussion.voteError'/>" data-unavailable="<fmt:message key='discussion.voteUnavailable'/>">
  <form id="discussion-vote-form" method="post" action="${voteBase}votes">
    <header><h2 id="discussion-vote-heading"><fmt:message key="discussion.createVote"/></h2><button class="discussion-icon-button discussion-vote-cancel" type="button" aria-label="<fmt:message key='GML.close'/>"><svg class="discussion-icon" viewBox="0 0 24 24" aria-hidden="true"><path d="M6 6l12 12M18 6L6 18" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/></svg></button></header>
    <p id="discussion-vote-loading" class="discussion-hint" role="status"></p>
    <fieldset id="discussion-vote-fields" disabled>
      <label for="discussion-vote-box"><fmt:message key="discussion.voteInstance"/></label>
      <select id="discussion-vote-box" name="vote" required></select>
      <label for="discussion-vote-question"><fmt:message key="discussion.voteQuestion"/></label>
      <input id="discussion-vote-question" name="question" type="text" required maxlength="160"/>
      <label for="discussion-vote-answers"><fmt:message key="discussion.voteAnswers"/></label>
      <textarea id="discussion-vote-answers" name="answers" rows="5" maxlength="2100" required aria-describedby="discussion-vote-answer-hint"></textarea>
      <p id="discussion-vote-answer-hint" class="discussion-hint"><fmt:message key="discussion.voteAnswersHint"/></p>
      <label class="discussion-vote-check"><input name="multiple" type="checkbox"/> <fmt:message key="discussion.voteMultiple"/></label>
      <label class="discussion-vote-check"><input name="anonymous" type="checkbox"/> <fmt:message key="discussion.voteAnonymous"/></label>
      <p class="discussion-hint"><fmt:message key="discussion.voteNative"/></p>
    </fieldset>
    <p id="discussion-vote-error" class="discussion-presence-error" role="alert" hidden></p>
    <footer><button id="discussion-vote-retry" type="button" hidden><fmt:message key="discussion.eventRetry"/></button><button class="discussion-vote-cancel" type="button"><fmt:message key="GML.cancel"/></button><button id="discussion-vote-submit" class="button" type="submit" disabled><fmt:message key="discussion.createVote"/></button></footer>
  </form>
</dialog>
<p id="discussion-vote-status" class="discussion-hint" role="status" data-success="<fmt:message key='discussion.voteCreated'/>" hidden></p>
