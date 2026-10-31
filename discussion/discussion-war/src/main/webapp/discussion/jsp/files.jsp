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
<c:url var="fileMessageBase" value="${requestScope.componentUriBase}"/>
<c:url var="filesIcons" value="/discussion/jsp/icons/discussion-icons.svg"/>
<div class="discussion-files-list" data-count="${fn:length(sharedFiles)}">
  <c:if test="${empty sharedFiles}"><p class="discussion-hint"><fmt:message key="discussion.noFiles"/></p></c:if>
  <ul>
    <c:forEach items="${sharedFiles}" var="file">
      <li>
        <a class="discussion-file-download" href="<c:out value='${file.url}'/>" download="<c:out value='${file.name}'/>"><c:out value="${file.name}"/></a>
        <c:if test="${file.viewable}"><button class="discussion-icon-button discussion-view-file" type="button" data-attachment-id="${file.id}" data-language="${fn:escapeXml(file.language)}"
            title="<fmt:message key='discussion.viewFile'/>" aria-label="<fmt:message key='discussion.viewFile'/> : ${fn:escapeXml(file.name)}"><svg class="discussion-icon" aria-hidden="true"><use href="${filesIcons}#view"/></svg></button></c:if>
        <span class="discussion-hint"><c:out value="${file.displaySize}"/></span>
        <a class="discussion-file-origin" href="${fileMessageBase}messages/${file.messageId}"><fmt:message key="discussion.fileOrigin"/></a>
      </li>
    </c:forEach>
  </ul>
</div>
