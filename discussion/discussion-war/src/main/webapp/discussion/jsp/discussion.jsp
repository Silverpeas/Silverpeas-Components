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
<c:url var="instanceBase" value="${requestScope.instanceUriBase}"/>
<c:if test="${not empty initialThreadRootId}">
  <c:url var="initialThreadUrl" value="${requestScope.componentUriBase}threads/${initialThreadRootId}">
    <c:param name="message" value="${initialThreadMessageId}"/>
  </c:url>
</c:if>
<c:set var="historyUrl" value="${base}history"/>
<c:set var="updatesUrl" value="${base}updates"/>
<c:set var="composeUrl" value="${base}messages/add"/>
<fmt:message key="${empty threadRoot ? 'discussion.post' : 'discussion.reply'}" var="composeActionLabel"/>
<fmt:message key="${empty threadRoot ? 'discussion.placeholder' : 'discussion.replyPlaceholder'}" var="composePlaceholder"/>
<c:if test="${not empty threadRoot}">
  <c:set var="historyUrl" value="${base}threads/${threadRoot.id}/history"/>
  <c:set var="updatesUrl" value="${base}threads/${threadRoot.id}/updates"/>
  <c:set var="composeUrl" value="${base}threads/${threadRoot.id}/reply"/>
</c:if>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8"/>
  <title><c:out value="${instanceInfo.title}"/></title>
  <view:looknfeel/>
  <view:includePlugin name="preview"/>
  <view:includePlugin name="userZoom"/>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-user-zoom.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-threads.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-view-state.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-channels.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-header.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-search.js'/>" defer></script>
  <view:includePlugin name="subscription"/>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-subscription.js'/>" defer></script>
  <link rel="stylesheet" href="<c:url value='/discussion/jsp/css/discussion.css'/>"/>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-desktop.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-activity.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/vendor/markdown-it-15.0.3.min.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-markdown.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/vendor/tiptap-3.32.2.min.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-rich-editor.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-editor.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-editing.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-quoting.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-layout.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-live.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-read.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-sidebar.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-pins.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-images.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-composer.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-files.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-file-picker.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-events.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-ideas.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-votes.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-visio.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-deletion.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-document-viewer.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-notification-settings.js'/>" defer></script>
</head>
<body>
<view:operationPane>
  <view:operation altText="<span id='subscriptionMenuLabel'></span>" icon="" action="#discussion-subscription"/>
</view:operationPane>
<view:window>
  <view:frame>
    <main id="discussion" class="discussion-channels-open" data-channel-scope="${currentChannel.scopeKey}" data-channels="${instanceBase}channels" data-history="${historyUrl}" data-thread-root-id="${threadRoot.id}" data-initial-thread-url="${fn:escapeXml(initialThreadUrl)}" data-participants="${base}participants" data-publications="${base}publications" data-updates="${updatesUrl}"
        data-files="${base}files" data-pins="${base}pins" data-instance-id="${discussionInstanceId}" data-read="${base}messages/read" data-typing="${base}typing" data-user-id="${currentUser.id}"
        data-typing-one="<fmt:message key='discussion.typingOne'/>" data-typing-many="<fmt:message key='discussion.typingMany'/>"
        data-message-id="<c:out value='${focusedMessageId}'/>"
        data-remove-quote="<fmt:message key='discussion.removeQuote'/>"
        data-message-required="<fmt:message key='discussion.messageRequired'/>"
        data-link-text="<fmt:message key='discussion.linkText'/>" data-link-url="<fmt:message key='discussion.linkUrl'/>"
        data-link-apply="<fmt:message key='discussion.linkApply'/>" data-link-invalid="<fmt:message key='discussion.linkInvalid'/>"
        data-format-label="<fmt:message key='discussion.formatLabel'/>" data-format-bold="<fmt:message key='discussion.formatBold'/>"
        data-format-italic="<fmt:message key='discussion.formatItalic'/>" data-format-strike="<fmt:message key='discussion.formatStrike'/>"
        data-format-bullets="<fmt:message key='discussion.formatBullets'/>" data-format-numbers="<fmt:message key='discussion.formatNumbers'/>"
        data-format-quote="<fmt:message key='discussion.formatQuote'/>" data-format-code="<fmt:message key='discussion.formatCode'/>" data-format-link="<fmt:message key='discussion.formatLink'/>"
        data-length-error="<fmt:message key='discussion.hint'/>"
        data-view-vote-label="<fmt:message key='discussion.viewVote'/>" data-view-idea-label="<fmt:message key='discussion.viewIdea'/>" data-contribution-base="<c:url value='/Contribution/'/>" data-view-event-label="<fmt:message key='discussion.viewEvent'/>"
        data-profile-base="<c:url value='/Rprofil/jsp/Main'/>"
        data-icons="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>"
        data-close-image="<fmt:message key='discussion.closeImage'/>" data-emoji-label="<fmt:message key='discussion.emoji'/>"
        data-mention-label="<fmt:message key='discussion.mention'/>">
      <header class="discussion-header">
        <button id="discussion-channels-toggle" class="discussion-channel-toggle" type="button" aria-controls="discussion-channels" aria-expanded="true" title="<fmt:message key='discussion.showChannels'/>" aria-label="<fmt:message key='discussion.showChannels'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#menu"/></svg><span id="discussion-channels-unread" class="discussion-channel-count" hidden></span></button>
        <div class="discussion-heading">
          <h1 title="<c:out value='${instanceInfo.description}'/>"><c:out value="${instanceInfo.title}"/></h1>
          <c:if test="${not empty threadRoot}">
            <p class="discussion-thread-context"><a class="discussion-thread-back" href="${base}messages/${threadRoot.id}#message-${threadRoot.id}"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#arrow-left"/></svg><fmt:message key="discussion.backToDiscussion"/></a>
              <strong><fmt:message key="discussion.thread"/></strong>
              <c:if test="${not threadRoot.deleted}"><span><fmt:message key="discussion.replyTo"/> <c:out value="${threadRoot.creator.displayedName}"/></span></c:if>
            </p>
          </c:if>
          <c:if test="${empty threadRoot}"><p class="discussion-subtitle discussion-channel-heading"><strong># <span id="discussion-channel-title"><c:choose><c:when test="${empty currentChannel.channelName}"><fmt:message key="discussion.general"/></c:when><c:otherwise><c:out value="${currentChannel.channelName}"/></c:otherwise></c:choose></span></strong><span><c:choose><c:when test="${currentChannel.main and empty currentChannel.channelDescription}"><c:out value="${instanceInfo.description}"/></c:when><c:otherwise><c:out value="${currentChannel.channelDescription}"/></c:otherwise></c:choose></span><c:if test="${currentChannel.archived}"><span class="discussion-channel-archived"><fmt:message key="discussion.channelArchived"/></span></c:if></p></c:if>
        </div>
        <form id="discussion-search" class="discussion-search" action="${base}search" method="get" role="search">
          <label class="discussion-compose-label" for="discussion-search-query"><fmt:message key="discussion.search"/></label>
          <input type="search" id="discussion-search-query" name="q" maxlength="200" placeholder="<fmt:message key='discussion.search'/>" autocomplete="off"/>
          <span class="discussion-search-submit"><button type="submit" class="discussion-icon-button" title="<fmt:message key='discussion.searchSubmit'/>" aria-label="<fmt:message key='discussion.searchSubmit'/>"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="10" cy="10" r="6"/><path d="m15 15 6 6"/></svg></button></span>
          <details id="discussion-search-scope" class="discussion-search-scope" data-label="<fmt:message key='discussion.searchScopeLabel'/>">
            <summary class="discussion-icon-button" title="<fmt:message key='discussion.searchThisChannel'/>" aria-label="<fmt:message key='discussion.searchThisChannel'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#filter"/></svg></summary>
            <fieldset class="discussion-search-scope-menu">
              <legend><fmt:message key="discussion.searchIn"/></legend>
              <label><input type="radio" name="allChannels" value="false" checked data-label="<fmt:message key='discussion.thisChannel'/>"/><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#check"/></svg><span><fmt:message key="discussion.thisChannel"/></span></label>
              <label><input id="discussion-search-all-channels" type="radio" name="allChannels" value="true" data-label="<fmt:message key='discussion.allChannels'/>"/><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#check"/></svg><span><fmt:message key="discussion.allChannels"/></span></label>
            </fieldset>
          </details>
        </form>
        <div class="discussion-reading" role="status" aria-live="polite" aria-atomic="true" data-unread-label="<fmt:message key='discussion.unreadSummary'/>" hidden>
          <span class="discussion-unread"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#unread"/></svg><span class="discussion-unread-count"><c:out value="${messagePage.unreadCount}"/></span></span>
        </div>
      </header>
      <template id="discussion-mention-directory">
        <c:forEach items="${mentionCandidates}" var="candidate">
          <span data-user-id="<c:out value='${candidate.id}'/>" data-name="<c:out value='${candidate.name}'/>"></span>
        </c:forEach>
      </template>
      <nav id="discussion-channels" class="discussion-channels" aria-label="<fmt:message key='discussion.channels'/>">
        <div class="discussion-channels-heading"><h2><fmt:message key="discussion.channels"/></h2><c:if test="${canManageChannels}"><button id="discussion-channel-create" class="discussion-icon-button" type="button" title="<fmt:message key='discussion.createChannel'/>" aria-label="<fmt:message key='discussion.createChannel'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#plus"/></svg></button></c:if><button id="discussion-channels-close" class="discussion-icon-button" type="button" title="<fmt:message key='discussion.closeChannels'/>" aria-label="<fmt:message key='discussion.closeChannels'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#panel-collapse"/></svg></button></div>
        <div id="discussion-channel-list"><jsp:include page="channels.jsp"/></div>

        <p id="discussion-channels-error" class="discussion-presence-error" hidden role="status"><fmt:message key="discussion.error"/></p>
      </nav>
      <c:if test="${canManageChannels}">
        <dialog id="discussion-channel-dialog" class="discussion-channel-dialog">
          <form method="post" action="${instanceBase}channels/create" data-create="${instanceBase}channels/create" data-update="${instanceBase}channels/${currentChannel.scopeKey}/settings">
            <h2><fmt:message key="discussion.manageChannel"/></h2>
            <label><fmt:message key="discussion.channelName"/><input name="name" required maxlength="80" data-current="<c:out value='${currentChannel.channelName}'/>"/></label>
            <label><fmt:message key="discussion.channelDescription"/><textarea name="description" maxlength="255" data-current="<c:out value='${currentChannel.channelDescription}'/>"></textarea></label>
            <label class="discussion-channel-archive-choice"><input name="archive" type="checkbox" data-current="${currentChannel.archived}" data-main="${currentChannel.main}"/><fmt:message key="discussion.archiveChannel"/></label>
            <input name="archived" type="hidden" value="false"/>
            <div><button type="button" class="discussion-channel-cancel"><fmt:message key="GML.cancel"/></button><button type="submit"><fmt:message key="GML.validate"/></button></div>
          </form>
        </dialog>
      </c:if>
      <div class="discussion-workspace">
        <div class="discussion-stream">
          <div id="discussion-scroll" class="discussion-scroll" tabindex="0" role="region" aria-label="<fmt:message key='discussion.history'/>">
            <div class="discussion-pagination">
              <button id="discussion-older" class="button discussion-button--quiet" type="button"><fmt:message key="discussion.older"/></button>
            </div>
            <p id="discussion-error" role="status" hidden><fmt:message key="discussion.error"/></p>
            <section id="discussion-history" aria-label="<fmt:message key='discussion.history'/>">
              <c:if test="${empty messagePage.messages}">
                <div class="discussion-empty">
                  <h3><fmt:message key="discussion.empty"/></h3>
                  <p><fmt:message key="discussion.emptyHint"/></p>
                </div>
              </c:if>
              <jsp:include page="messages.jsp"/>
            </section>
          </div>
          <p id="discussion-viewer-error" class="discussion-presence-error" role="status" hidden><fmt:message key="discussion.viewerError"/></p>
          <p id="discussion-live-error" class="discussion-hint" role="status" hidden><fmt:message key="discussion.liveError"/></p>
          <c:if test="${canWrite}">
            <form class="discussion-composer" method="post" enctype="multipart/form-data" action="${composeUrl}">
              <p id="discussion-event-status" class="discussion-hint" role="status" aria-live="polite" hidden data-success="<fmt:message key='discussion.eventCreated'/>"></p>
              <p id="discussion-typing" class="discussion-hint" role="status" aria-live="polite" hidden></p>
              <label for="discussion-body" class="discussion-compose-label"><fmt:message key="discussion.compose"/></label>
              <div class="discussion-compose-box">
              <textarea rows="1" id="discussion-body" name="body" maxlength="2000" required aria-describedby="discussion-compose-hint"
                  placeholder="${fn:escapeXml(composePlaceholder)}"></textarea>
              <fmt:message key="discussion.imagesHint" var="imagesHint"><fmt:param value="${uploadLimits.maxImages}"/><fmt:param value="${uploadLimits.imageSizeMb}"/></fmt:message>
              <fmt:message key="discussion.imagesError" var="imagesError"><fmt:param value="${uploadLimits.maxImages}"/><fmt:param value="${uploadLimits.imageSizeMb}"/></fmt:message>
              <fmt:message key="discussion.filesHint" var="filesHint"><fmt:param value="${uploadLimits.maxFiles}"/><fmt:param value="${uploadLimits.fileSizeMb}"/></fmt:message>
              <fmt:message key="discussion.filesError" var="filesError"><fmt:param value="${uploadLimits.maxFiles}"/><fmt:param value="${uploadLimits.fileSizeMb}"/></fmt:message>
              <div class="discussion-image-picker">
                <button class="discussion-icon-button discussion-add-image" type="button" aria-label="<fmt:message key='discussion.addImages'/>"
                    title="<fmt:message key='discussion.addImages'/> — ${fn:escapeXml(imagesHint)}"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#image"/></svg></button>
                <input id="discussion-images" type="file" tabindex="-1" aria-label="<fmt:message key='discussion.addImages'/>" name="images" accept="image/jpeg,image/png,image/gif" multiple
                    data-max-count="${uploadLimits.maxImages}" data-max-bytes="${uploadLimits.imageBytes}" data-error="${fn:escapeXml(imagesError)}" data-remove="<fmt:message key='discussion.removeImage'/>"/>
                <div id="discussion-image-preview" class="discussion-image-preview"></div>
                <p id="discussion-image-error" class="discussion-presence-error" role="status" hidden></p>
              </div>
              <div class="discussion-file-picker">
                <button class="discussion-icon-button discussion-add-file" type="button" aria-label="<fmt:message key='discussion.addFiles'/>"
                    title="<fmt:message key='discussion.addFiles'/> — ${fn:escapeXml(filesHint)}"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#paperclip"/></svg></button>
                <input id="discussion-files" type="file" name="files" multiple tabindex="-1" aria-label="<fmt:message key='discussion.addFiles'/>"
                    data-max-count="${uploadLimits.maxFiles}" data-max-bytes="${uploadLimits.fileBytes}" data-error="${fn:escapeXml(filesError)}" data-remove="<fmt:message key='discussion.removeFile'/>"/>
                <div id="discussion-file-preview" class="discussion-file-preview"></div>
                <p id="discussion-file-error" class="discussion-presence-error" role="status" hidden></p>
              </div>
              <button id="discussion-add-event" class="discussion-icon-button discussion-add-event" type="button" title="<fmt:message key='discussion.createEvent'/>" aria-label="<fmt:message key='discussion.createEvent'/>" hidden><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#calendar-plus"/></svg></button>
              <button id="discussion-add-idea" class="discussion-icon-button" type="button" title="<fmt:message key='discussion.proposeIdea'/>" aria-label="<fmt:message key='discussion.proposeIdea'/>" hidden><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#bulb"/></svg></button>
              <button id="discussion-add-vote" class="discussion-icon-button" type="button" title="<fmt:message key='discussion.createVote'/>" aria-label="<fmt:message key='discussion.createVote'/>" hidden><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#vote"/></svg></button>
              <button id="discussion-add-visio" class="discussion-icon-button" type="button" title="<fmt:message key='discussion.createVisio'/>" aria-label="<fmt:message key='discussion.createVisio'/>" hidden><svg class="discussion-icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="3" y="5" width="12" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M15 9l6-3v12l-6-3z" fill="none" stroke="currentColor" stroke-width="1.5"/></svg></button>
              <div class="discussion-composer-footer">
                <span id="discussion-compose-hint" class="discussion-hint discussion-compose-label"><fmt:message key="discussion.hint"/></span>
                <button class="discussion-send" type="submit" title="${fn:escapeXml(composeActionLabel)}" aria-label="${fn:escapeXml(composeActionLabel)}"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#send"/></svg></button>
              </div>
              </div>
            </form>
          </c:if>
        </div>
        <aside class="discussion-sidebar" aria-label="<fmt:message key='discussion.sidebar'/>">
          <div id="discussion-sidebar-accordions">
          <details id="discussion-search-section" class="discussion-sidebar-section" name="discussion-sidebar" hidden>
            <summary><h2><fmt:message key="discussion.searchResults"/> <span id="discussion-search-count" class="discussion-people-count"></span></h2></summary>
            <button id="discussion-search-close" type="button" class="discussion-icon-button" title="<fmt:message key='discussion.searchClose'/>" aria-label="<fmt:message key='discussion.searchClose'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#close"/></svg></button>
            <div class="discussion-sidebar-content">
              <p id="discussion-search-status" class="discussion-hint" role="status" aria-live="polite" data-loading="<fmt:message key='discussion.searchLoading'/>" data-error="<fmt:message key='discussion.searchError'/>"></p>
              <div id="discussion-search-results"></div>
              <button id="discussion-search-more" type="button" class="button discussion-button--quiet" hidden><fmt:message key="discussion.searchMore"/></button>
            </div>
          </details>
          <details id="discussion-participants-section" class="discussion-sidebar-section" name="discussion-sidebar" open>
            <summary><h2 id="discussion-people-heading"><fmt:message key="discussion.participants"/> <span id="discussion-participant-count" class="discussion-people-count"><c:out value="${fn:length(participants)}"/></span></h2></summary>
            <div class="discussion-sidebar-content">
              <div id="discussion-participants"><jsp:include page="participants.jsp"/></div>
              <p id="discussion-presence-error" class="discussion-presence-error" role="status" hidden><fmt:message key="discussion.presenceUnavailable"/></p>
            </div>
          </details>
          <details class="discussion-sidebar-section" id="discussion-publications-section" name="discussion-sidebar">
            <summary><h2><fmt:message key="discussion.publications"/> <span id="discussion-publication-count" class="discussion-people-count"><c:out value="${fn:length(citedPublications)}"/></span></h2></summary>
            <div class="discussion-sidebar-content">
              <div id="discussion-publications"><jsp:include page="publications.jsp"/></div>
              <p id="discussion-publications-error" class="discussion-presence-error" role="status" hidden><fmt:message key="discussion.publicationsError"/></p>
            </div>
          </details>
          <details class="discussion-sidebar-section" id="discussion-pins-section" name="discussion-sidebar">
            <summary><h2><fmt:message key="discussion.pinnedMessages"/> <span id="discussion-pin-count" class="discussion-people-count"><c:out value="${fn:length(pinnedMessages)}"/></span></h2></summary>
            <div class="discussion-sidebar-content">
              <div id="discussion-pins"><jsp:include page="pins.jsp"/></div>
              <p id="discussion-pins-error" class="discussion-presence-error" role="status" hidden><fmt:message key="discussion.pinError"/></p>
            </div>
          </details>
          <details class="discussion-sidebar-section" id="discussion-files-section" name="discussion-sidebar">
            <summary><h2><fmt:message key="discussion.sharedFiles"/> <span id="discussion-file-count" class="discussion-people-count"><c:out value="${fn:length(sharedFiles)}"/></span></h2></summary>
            <div class="discussion-sidebar-content">
              <div id="discussion-shared-files"><jsp:include page="files.jsp"/></div>
              <p id="discussion-files-error" class="discussion-presence-error" role="status" hidden><fmt:message key="discussion.filesLoadError"/></p>
            </div>
          </details>
          <details class="discussion-sidebar-section" id="discussion-notifications-section" name="discussion-sidebar">
            <summary><h2><fmt:message key="discussion.notifications"/></h2></summary>
            <div class="discussion-sidebar-content">
              <form id="discussion-channel-notifications" method="post" action="${instanceBase}channels/${currentChannel.scopeKey}/notifications">
                <label for="discussion-channel-notification-mode"><fmt:message key="discussion.channelNotifications"/></label>
                <select id="discussion-channel-notification-mode" name="mode" data-current="${channelNotificationMode}">
                  <option value="ALL"><fmt:message key="discussion.notifyAll"/></option><option value="MENTIONS"><fmt:message key="discussion.notifyMentions"/></option><option value="NONE"><fmt:message key="discussion.notifyNone"/></option>
                </select><button type="submit" class="discussion-button--quiet"><fmt:message key="GML.validate"/></button>
              </form>
              <form id="discussion-notification-settings" action="${base}notification-preferences" method="post"
                  data-enabled="${desktopPreference.enabled}" data-revision="${desktopPreference.revision}"
                  data-denied="<fmt:message key='discussion.notificationsDenied'/>" data-unsupported="<fmt:message key='discussion.notificationsUnsupported'/>">
                <label class="discussion-notification-choice"><input id="discussion-notifications-enabled" type="checkbox" name="enabled" value="true"/>
                  <span><fmt:message key="discussion.notificationsOutside"/></span></label>
                <p class="discussion-hint"><fmt:message key="discussion.notificationsHelp"/></p>
                <p class="discussion-hint"><fmt:message key="discussion.mentionPreferencesHelp"/></p>
                <button id="discussion-desktop-enable" class="discussion-button--quiet" type="button" hidden><fmt:message key="discussion.enableDesktop"/></button>
                <p id="discussion-notification-status" class="discussion-hint" role="status" hidden></p>
                <p id="discussion-notification-error" class="discussion-presence-error" role="status" hidden><fmt:message key="discussion.notificationSettingsError"/></p>
              </form>
            </div>
          </details>
          </div>
          <section id="discussion-thread-panel" class="discussion-thread-panel" aria-label="<fmt:message key='discussion.thread'/>" hidden>
            <header class="discussion-thread-panel-header">
              <h2><fmt:message key="discussion.thread"/></h2>
              <button id="discussion-thread-close" class="discussion-icon-button" type="button" aria-label="<fmt:message key='discussion.closeThread'/>" title="<fmt:message key='discussion.closeThread'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#close"/></svg></button>
            </header>
            <p id="discussion-thread-status" class="discussion-hint" role="status" data-loading="<fmt:message key='discussion.threadLoading'/>" data-error="<fmt:message key='discussion.threadLoadError'/>" hidden></p>
            <a id="discussion-thread-fallback" class="discussion-thread-fallback" hidden><fmt:message key="discussion.openThread"/></a>
            <iframe id="discussion-thread-frame" class="discussion-thread-frame" title="<fmt:message key='discussion.thread'/>"></iframe>
          </section>
        </aside>
      </div>
      <c:if test="${canWrite}"><jsp:include page="event-dialog.jsp"/><jsp:include page="idea-dialog.jsp"/><jsp:include page="vote-dialog.jsp"/><jsp:include page="visio-dialog.jsp"/></c:if>
      <jsp:include page="event-delete-dialog.jsp"/>
    </main>
  </view:frame>
</view:window>
</body>
</html>
