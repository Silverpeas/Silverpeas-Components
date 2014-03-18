<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<c:set var="suggestionBoxId" value="${requestScope.suggestionBox.id}"/>
<c:set var="isEdito" value="${requestScope.isEdito}"/>

<view:setConstant var="adminRole" constant="com.stratelia.webactiv.SilverpeasRole.admin" />
<view:setConstant var="publishRole" constant="com.stratelia.webactiv.SilverpeasRole.publisher" />

<fmt:message var="modifyEditoLabel" key="suggestionBox.menu.item.edito.modify"/>
<fmt:message var="addSuggestionLabel" key="suggestionBox.menu.item.suggestion.add"/>


<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html>
<head>
  <view:looknfeel/>
  <view:includePlugin name="wysiwyg"/>
</head>
<body>
<c:if test="${greaterUserRole.isGreaterThanOrEquals(publishRole)}">
<view:operationPane>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(adminRole)}">
    <view:operation action="edito/modify" altText="${modifyEditoLabel}"/>
    <view:operationSeparator/>
  </c:if>
  <view:operation action="suggestion/new" altText="${addSuggestionLabel}"/>
</view:operationPane>
</c:if>
<view:window>
  <view:frame>
    <c:if test="${isEdito}">
      <view:displayWysiwyg objectId="${suggestionBoxId}" componentId="${componentId}" language="${null}"/>
    </c:if>
  </view:frame>
</view:window>
</body>
</html>