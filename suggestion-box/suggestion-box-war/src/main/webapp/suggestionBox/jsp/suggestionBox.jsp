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
<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="currentUserId" value="${currentUser.id}"/>
<c:set var="isUserSubscribed" value="${requestScope.isUserSubscribed}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<c:set var="componentUriBase"><c:url value="${requestScope.componentUriBase}"/></c:set>
<c:set var="suggestionBoxId" value="${requestScope.suggestionBox.id}"/>
<c:set var="isEdito" value="${requestScope.isEdito}"/>

<view:setConstant var="adminRole" constant="com.stratelia.webactiv.SilverpeasRole.admin"/>
<view:setConstant var="publisherRole" constant="com.stratelia.webactiv.SilverpeasRole.publisher"/>
<view:setConstant var="writerRole" constant="com.stratelia.webactiv.SilverpeasRole.writer"/>

<fmt:message var="modifyEditoLabel" key="suggestionBox.menu.item.edito.modify"/>
<fmt:message var="publishSuggestionLabel" key="GML.publish"/>
<fmt:message var="approveSuggestionLabel" key="GML.validate"/>
<fmt:message var="refuseSuggestionLabel" key="GML.refuse"/>
<fmt:message var="browseBarPathSuggestionLabel" key="suggestionBox.menu.item.suggestion.add"/>
<fmt:message var="deleteSuggestionConfirmMessage" key="suggestionBox.message.suggestion.remove.confirm">
  <fmt:param value="<b>@name@</b>"/>
</fmt:message>
<fmt:message key="suggestionBox.menu.item.subscribe" var="subscribeToSuggestionBoxLabel"/>
<fmt:message key="suggestionBox.menu.item.unsubscribe" var="unsubscribeFromSuggestionBoxLabel"/>

<c:url var="suggestionBoxJS" value="/util/javaScript/angularjs/suggestionbox.js"/>
<c:url var="suggestionBoxServicesJS" value="/util/javaScript/angularjs/services/suggestionbox.js"/>
<c:url var="suggestionBoxValidationDirectiveJS" value="/util/javaScript/angularjs/directives/suggestionbox-validation.js"/>
<c:url var="suggestionBoxDeletionDirectiveJS" value="/util/javaScript/angularjs/directives/suggestionbox-deletion.js"/>
<c:url var="deleteIcon" value="/util/icons/delete.gif"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.suggestionBox">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <view:includePlugin name="wysiwyg"/>
  <view:includePlugin name="popup"/>
  <script type="text/javascript" src="${suggestionBoxServicesJS}"></script>
  <script type="text/javascript" src="${suggestionBoxValidationDirectiveJS}"></script>
  <script type="text/javascript" src="${suggestionBoxDeletionDirectiveJS}"></script>
  <script type="application/javascript">
    function successUnsubscribe() {
      $("#yui-gen1").empty().append($('<a>').addClass('yuimenuitemlabel').attr('href',
          "javascript:subscribe();").attr('title',
          '<view:encodeJs string="${subscribeToSuggestionBoxLabel}" />').append('<view:encodeJs string="${subscribeToSuggestionBoxLabel}" />'));
    }

    function successSubscribe() {
      $("#yui-gen1").empty().append($('<a>').addClass('yuimenuitemlabel').attr('href',
          "javascript:unsubscribe();").attr('title',
          '<view:encodeJs string="${unsubscribeFromSuggestionBoxLabel}" />').append('<view:encodeJs string="${unsubscribeFromSuggestionBoxLabel}" />'));
    }

    function unsubscribe() {
      $.post('<c:url value="/services/unsubscribe/${componentId}" />', successUnsubscribe(),
          'json');
    }

    function subscribe() {
      $.post('<c:url value="/services/subscribe/${componentId}" />', successSubscribe(), 'json');
    }
  </script>
</head>
<body ng-controller="mainController">
<view:operationPane>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(adminRole)}">
    <view:operation action="${componentUriBase}edito/modify" altText="${modifyEditoLabel}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${isUserSubscribed != null}">
    <c:choose>
      <c:when test="${isUserSubscribed}">
        <view:operation altText="${unsubscribeFromSuggestionBoxLabel}" icon="" action="javascript:unsubscribe();"/>
      </c:when>
      <c:otherwise>
        <view:operation altText="${subscribeToSuggestionBoxLabel}" icon="" action="javascript:subscribe();"/>
      </c:otherwise>
    </c:choose>
    <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
      <view:operation action="${componentUriBase}suggestion/new" altText="${browseBarPathSuggestionLabel}"/>
    </c:if>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <c:if test="${isEdito}">
      <view:displayWysiwyg objectId="${suggestionBoxId}" componentId="${componentId}" language="${null}"/>
    </c:if>
    <div class="table">
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
        <div ng-controller="notPublishedController" class="cell">
          <div suggestionbox-deletion></div>
          <ul id="my_notPublished_suggestions_list" class="container">
            <li ng-repeat="suggestion in notPublishedSuggestions">
              <a ng-href="${componentUriBase}suggestion/{{ suggestion.id }}"><span class="suggestion_title">{{ suggestion.title }}</span></a><br/>

              <div>{{ suggestion.status }}</div>
              <div ng-bind-html="suggestion.content"></div>
              <img ng-click="delete(suggestion)" src="${deleteIcon}" alt="remove" class="action remove"/>
              <a href="#" ng-click="publish(suggestion)"><span>${publishSuggestionLabel}</span></a><br/>
            </li>
          </ul>
        </div>
      </c:if>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
        <div suggestionbox-validation></div>
        <div ng-controller="pendingValidationController" class="cell">
          <ul id="my_pending_validation_suggestions_list" class="container">
            <li ng-repeat="suggestion in pendingValidationSuggestions">
              <a ng-href="${componentUriBase}suggestion/{{ suggestion.id }}"><span class="suggestion_title">{{ suggestion.title }}</span></a><br/>

              <div>{{ suggestion.status }}</div>
              <div ng-bind-html="suggestion.content"></div>
              <a href="#" ng-click="refuse(suggestion)"><span>${refuseSuggestionLabel}</span></a><br/>
              <a href="#" ng-click="approve(suggestion)"><span>${approveSuggestionLabel}</span></a><br/>
            </li>
          </ul>
        </div>
      </c:if>
      <div ng-controller="publishedController" class="cell">
        <ul id="my_published_suggestions_list" class="container">
          <li ng-repeat="suggestion in publishedSuggestions">
            <a ng-href="${componentUriBase}suggestion/{{ suggestion.id }}"><span class="suggestion_title">{{ suggestion.title }}</span></a><br/>

            <div>{{ suggestion.status }}</div>
            <div ng-bind-html="suggestion.content"></div>
          </li>
        </ul>
      </div>
    </div>
  </view:frame>
</view:window>
<script type="text/javascript">
  angular.module('silverpeas').value('context', {
    currentUserId : '${currentUserId}',
    suggestionBoxId : '${suggestionBoxId}',
    component : '${componentId}',
    componentUriBase : '${componentUriBase}'
  });
</script>
<script type="text/javascript" src="${suggestionBoxJS}"></script>
</body>
</html>