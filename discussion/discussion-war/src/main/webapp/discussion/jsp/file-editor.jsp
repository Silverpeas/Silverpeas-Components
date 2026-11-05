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
<c:url var="editionBase" value="${requestScope.componentUriBase}"/>
<c:if test="${requestScope.editableFile.editable}">
  <button class="discussion-icon-button discussion-edit-file" type="button"
      data-edit-url="${editionBase}files/${requestScope.editableFile.id}/edit"
      data-language="${fn:escapeXml(requestScope.editableFile.language)}"
      title="<fmt:message key='discussion.editFile'/>" aria-label="<fmt:message key='discussion.editFile'/> : ${fn:escapeXml(requestScope.editableFile.name)}">
    <svg class="discussion-icon" viewBox="0 0 24 24" aria-hidden="true"><rect x="2" y="3" width="20" height="14" rx="1" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M8 21h8M12 17v4M7 13l1-3 6-5 3 3-6 5z" fill="none" stroke="currentColor" stroke-width="1.5"/></svg>
  </button>
</c:if>
