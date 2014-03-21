<%--

    Copyright (C) 2000 - 2014 Silverpeas

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
<c:set var="componentUriBase"><c:url value="${requestScope.componentUriBase}"/></c:set>
<c:set var="suggestionBoxId" value="${requestScope.suggestionBox.id}"/>
<c:set var="isEdito" value="${requestScope.isEdito}"/>
<c:set var="currentUserId" value="${sessionScope['SilverSessionController'].userId}"/>

<view:setConstant var="adminRole" constant="com.stratelia.webactiv.SilverpeasRole.admin" />
<view:setConstant var="publishRole" constant="com.stratelia.webactiv.SilverpeasRole.publisher" />

<fmt:message var="modifyEditoLabel" key="suggestionBox.menu.item.edito.modify"/>
<fmt:message var="addSuggestionLabel" key="suggestionBox.menu.item.suggestion.add"/>

<c:url var="suggestionBoxJS" value="/util/javaScript/angularjs/suggestionbox.js"/>
<c:url var="suggestionBoxServicesJS" value="/util/javaScript/angularjs/services/suggestionbox.js"/>
<c:url var="deleteIcon" value="/util/icons/delete.gif"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.suggestionBox">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <view:includePlugin name="wysiwyg"/>
  <script type="text/javascript" src="${suggestionBoxServicesJS}"></script>
</head>
<body>
<c:if test="${greaterUserRole.isGreaterThanOrEquals(publishRole)}">
<view:operationPane>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(adminRole)}">
    <view:operation action="${componentUriBase}edito/modify" altText="${modifyEditoLabel}"/>
    <view:operationSeparator/>
  </c:if>
  <view:operation action="${componentUriBase}suggestion/new" altText="${addSuggestionLabel}"/>
</view:operationPane>
</c:if>
<view:window>
  <view:frame>
    <c:if test="${isEdito}">
      <view:displayWysiwyg objectId="${suggestionBoxId}" componentId="${componentId}" language="${null}"/>
    </c:if>
    <div ng-controller="mainController">
      <ul id="my_suggestions_list" class="container">
        <li ng-repeat="suggestion in suggestions">
          <a ng-href="${componentUriBase}suggestion/{{ suggestion.id }}"><span class="suggestion_title">{{ suggestion.title }}</span></a>
          <!-- TODO once the deletion by web service is implemented, replace the argument by: suggestion.id -->
          <img ng-click="delete('${componentUriBase}suggestion/delete/'+ suggestion.id)" src="${deleteIcon}" alt="remove" class="remove"/>
        </li>
      </ul>
    </div>
    <!-- TODO delete this form once the web service to delete a given suggestion is done -->
    <form id="deletion" action="" method="POST"></form>
  </view:frame>
</view:window>
  <script type="text/javascript">
    angular.module('silverpeas').value('context', {
          currentUserId: '${currentUserId}',
          component: '${componentId}'});

  </script>
  <script type="text/javascript" src="${suggestionBoxJS}"></script>
</body>
</html>