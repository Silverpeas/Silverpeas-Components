<%--
  Copyright (C) 2000 - 2014 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

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

<view:setConstant var="readerRole" constant="com.stratelia.webactiv.SilverpeasRole.reader"/>
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="back" key="GML.back"/>

<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="componentUriBase"><c:url value="${requestScope.componentUriBase}"/></c:set>
<c:set var="suggestionBoxId" value="${requestScope.currentSuggestionBox.id}"/>
<c:set var="suggestion" value="${requestScope.suggestion}"/>
<c:set var="target" value="${suggestion.id}"/>
<c:set var="isEditable" value="${requestScope.isEditable}"/>
<c:set var="isPublishable" value="${requestScope.isPublishable}"/>
<c:set var="isModeratorView" value="${requestScope.isModeratorView}"/>

<c:if test="${not greaterUserRole.isGreaterThanOrEquals(readerRole)}">
  <c:redirect url="/Error403.jsp"/>
</c:if>

<c:url var="suggestionBoxJS" value="/util/javaScript/angularjs/suggestionbox.js"/>
<c:url var="suggestionBoxServicesJS" value="/util/javaScript/angularjs/services/suggestionbox.js"/>
<c:url var="silverpeasPaginationJS" value="/util/javaScript/angularjs/directives/silverpeas-pagination.js"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.suggestionBox">
<head>
  <view:looknfeel/>
  <script type="text/javascript" src="${suggestionBoxServicesJS}"></script>
  <script type="text/javascript" src="${silverpeasPaginationJS}"></script>
</head>
<body ng-controller="mainController" id="${componentId}">
<view:browseBar componentId="${componentId}"/>
<view:window>
  <view:frame>
    <div ng-controller="suggestionListController" id="suggestion_list">
      <table>
        <thead>
          <tr><th>Suggestion</th>
            <th>Author</th>
            <th ng-click="sortByValidationDate()">Approved at</th>
            <th ng-click="sortByRating()">Rating</th></tr>
        </thead>
        <tbody>
          <tr ng-repeat="suggestion in suggestions" ng-class-odd="" ng-class-even="">
            <td><a ng-href="${componentUriBase}suggestions/{{suggestion.id}}">{{suggestion.title}}</a></td>
            <td>{{suggestion.author}}</td>
            <td>{{suggestion.validation.date | date: 'shortDate'}}</td>
            <td></td>
          </tr>
          <silverpeas-pagination page-size="10" items-size="suggestions.maxlength" on-page="changePage(page)"></silverpeas-pagination>
        </tbody>
    </table>
    </div>
    <br clear="all"/>
    <silverpeas-button ng-click="goAt('${componentUriBase}Main')">${back}</silverpeas-button>
  </view:frame>
</view:window>
<script type="text/javascript">
  angular.module('silverpeas').value('context', {
    currentUserId : '${currentUser.id}',
    suggestionBoxId : '${suggestionBoxId}',
    suggestionId : '${suggestion.id}',
    component : '${componentId}',
    componentUriBase : '${componentUriBase}'
  });
</script>
<script type="text/javascript" src="${suggestionBoxJS}"></script>
</body>
</html>