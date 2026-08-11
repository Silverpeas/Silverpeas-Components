<%@ page import="org.silverpeas.components.kmelia.jstl.KmeliaDisplayHelper" %><%--

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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.fmt" prefix="fmt" %>
<%@ taglib uri="silverpeas.tags.viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="currentLang" value="${requestScope.Language}"/>
<fmt:setLocale value="${currentLang}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<fmt:message var="title" key="GML.ReadingControlTitle"/>
<c:set var="news" value="${requestScope['News']}"/>
<jsp:useBean id="news" type="org.silverpeas.components.quickinfo.model.News"/>

<view:sp-page>
  <view:sp-head-part title="${title}"/>
  <view:sp-body-part>
    <view:browseBar>
      <view:browseBarElt label="${news.title}" link="View?Id=${news.id}"/>
    </view:browseBar>
    <view:window>
      <view:frame>
        <viewTags:displayReadingControl
            componentInstanceId="${news.publication.instanceId}"
            contributionId="${news.id}"
            type="${news.contributionType}"/>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>