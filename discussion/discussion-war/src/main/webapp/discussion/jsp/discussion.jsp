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
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8"/>
  <title><c:out value="${instanceInfo.title}"/></title>
  <view:looknfeel/>
  <view:includePlugin name="preview"/>
  <link rel="stylesheet" href="<c:url value='/discussion/jsp/css/discussion.css'/>"/>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-desktop.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-activity.js'/>" defer></script>
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-editor.js'/>" defer></script>
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
  <script src="<c:url value='/discussion/jsp/javaScript/discussion-document-viewer.js'/>" defer></script>
</head>
<body>
<view:window>
  <view:frame>
    <main id="discussion" data-history="${base}history" data-participants="${base}participants" data-publications="${base}publications" data-updates="${base}updates"
        data-files="${base}files" data-pins="${base}pins" data-instance-id="${discussionInstanceId}" data-read="${base}messages/read" data-typing="${base}typing" data-user-id="${currentUser.id}"
        data-typing-one="<fmt:message key='discussion.typingOne'/>" data-typing-many="<fmt:message key='discussion.typingMany'/>"
        data-message-id="<c:out value='${focusedMessageId}'/>"
        data-length-error="<fmt:message key='discussion.hint'/>"
        data-profile-base="<c:url value='/Rprofil/jsp/Main'/>"
        data-icons="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>"
        data-close-image="<fmt:message key='discussion.closeImage'/>" data-emoji-label="<fmt:message key='discussion.emoji'/>"
        data-mention-label="<fmt:message key='discussion.mention'/>">
      <header class="discussion-header">
        <div class="discussion-heading">
          <h1><c:out value="${instanceInfo.title}"/></h1>
          <c:if test="${not empty instanceInfo.description}"><p class="discussion-subtitle"><c:out value="${instanceInfo.description}"/></p></c:if>
        </div>
        <div class="discussion-reading">
          <button id="discussion-desktop-enable" class="discussion-button--quiet" type="button" hidden><fmt:message key="discussion.enableDesktop"/></button>
          <span class="discussion-unread">
            <span class="discussion-unread-count"><c:out value="${messagePage.unreadCount}"/></span>
            <fmt:message key="discussion.unread"/>
          </span>

        </div>
      </header>
      <template id="discussion-mention-directory">
        <c:forEach items="${mentionCandidates}" var="candidate">
          <span data-user-id="<c:out value='${candidate.id}'/>" data-name="<c:out value='${candidate.name}'/>"></span>
        </c:forEach>
      </template>
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
            <form class="discussion-composer" method="post" enctype="multipart/form-data" action="${base}messages/add">
              <p id="discussion-typing" class="discussion-hint" role="status" aria-live="polite" hidden></p>
              <label for="discussion-body" class="discussion-compose-label"><fmt:message key="discussion.compose"/></label>
              <div class="discussion-compose-box">
              <textarea rows="1" id="discussion-body" name="body" maxlength="2000" required aria-describedby="discussion-compose-hint"
                  placeholder="<fmt:message key='discussion.placeholder'/>"></textarea>
              <div class="discussion-image-picker">
                <button class="discussion-icon-button discussion-add-image" type="button" aria-label="<fmt:message key='discussion.addImages'/>"
                    title="<fmt:message key='discussion.addImages'/> — <fmt:message key='discussion.imagesHint'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#image"/></svg></button>
                <input id="discussion-images" type="file" tabindex="-1" aria-label="<fmt:message key='discussion.addImages'/>" name="images" accept="image/jpeg,image/png,image/gif" multiple
                    data-error="<fmt:message key='discussion.imagesError'/>" data-remove="<fmt:message key='discussion.removeImage'/>"/>
                <div id="discussion-image-preview" class="discussion-image-preview"></div>
                <p id="discussion-image-error" class="discussion-presence-error" role="status" hidden></p>
              </div>
              <div class="discussion-file-picker">
                <button class="discussion-icon-button discussion-add-file" type="button" aria-label="<fmt:message key='discussion.addFiles'/>"
                    title="<fmt:message key='discussion.addFiles'/> — <fmt:message key='discussion.filesHint'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#paperclip"/></svg></button>
                <input id="discussion-files" type="file" name="files" multiple tabindex="-1" aria-label="<fmt:message key='discussion.addFiles'/>"
                    data-error="<fmt:message key='discussion.filesError'/>" data-remove="<fmt:message key='discussion.removeFile'/>"/>
                <div id="discussion-file-preview" class="discussion-file-preview"></div>
                <p id="discussion-file-error" class="discussion-presence-error" role="status" hidden></p>
              </div>
              <div class="discussion-composer-footer">
                <span id="discussion-compose-hint" class="discussion-hint discussion-compose-label"><fmt:message key="discussion.hint"/></span>
                <button class="discussion-send" type="submit" title="<fmt:message key='discussion.post'/>" aria-label="<fmt:message key='discussion.post'/>"><svg class="discussion-icon" aria-hidden="true"><use href="<c:url value='/discussion/jsp/icons/discussion-icons.svg'/>#send"/></svg></button>
              </div>
              </div>
            </form>
          </c:if>
        </div>
        <aside class="discussion-sidebar" aria-label="<fmt:message key='discussion.sidebar'/>">
          <details class="discussion-sidebar-section" name="discussion-sidebar" open>
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
        </aside>
      </div>
    </main>
  </view:frame>
</view:window>
</body>
</html>
