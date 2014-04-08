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
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="currentUserId" value="${currentUser.id}"/>
<c:set var="isUserSubscribed" value="${requestScope.isUserSubscribed}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<c:set var="suggestionBox" value="${requestScope.currentSuggestionBox}"/>
<c:set var="suggestionBoxId" value="${suggestionBox.id}"/>
<c:set var="isEdito" value="${requestScope.isEdito}"/>

<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>

<view:setConstant var="adminRole" constant="com.stratelia.webactiv.SilverpeasRole.admin"/>
<view:setConstant var="writerRole" constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
<view:setConstant var="publisherRole" constant="com.stratelia.webactiv.SilverpeasRole.publisher"/>

<view:setConstant var="STATUS_REFUSED" constant="org.silverpeas.contribution.ContributionStatus.REFUSED"/>
<view:setConstant var="STATUS_PENDING_VALIDATION" constant="org.silverpeas.contribution.ContributionStatus.PENDING_VALIDATION"/>
<view:setConstant var="STATUS_VALIDATED" constant="org.silverpeas.contribution.ContributionStatus.VALIDATED"/>

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
  </c:if>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
    <fmt:message key="suggestionBox.proposeSuggestion" var="tmpIcon" bundle="${icons}"/>
    <c:url var="tmpIcon" value="${tmpIcon}"/>
    <view:operationOfCreation action="${componentUriBase}suggestions/new" altText="${browseBarPathSuggestionLabel}" icon="${tmpIcon}"/>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <h2 class="suggestionBox-title">${suggestionBox.getTitle(currentUserLanguage)}</h2>
    <c:if test="${isEdito}">
      <div class="suggestionBox-description">
        <view:displayWysiwyg objectId="${suggestionBoxId}" componentId="${componentId}" language="${null}"/>
      </div>
    </c:if>
    <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
      <div id="my-suggestionBox">
        <view:areaOfOperationOfCreation/>
        <fmt:message key="suggestionBox.label.suggestions.mine" var="labelMySuggestions"/>
        <div class="secteur-container my-suggestionBox-draft">
          <div class="header">
            <h3 class="my-suggestionBox-inProgress-title">
              <c:out value="${labelMySuggestions} "/>
              <strong><fmt:message key="suggestionBox.label.suggestions.inDraft"/></strong></h3>
          </div>
          <div suggestionbox-deletion></div>
          <ul ng-controller="inDraftController">
            <li ng-repeat="suggestion in inDraftSuggestions">
              <a ng-href="${componentUriBase}suggestions/{{ suggestion.id }}">{{suggestion.title}}</a>
                <%--TODO BEGIN REMOVE AFTER DEV--%>
              <img ng-click="delete(suggestion)" src="${deleteIcon}" alt="remove" style="cursor: pointer"/>
              <a href="#" ng-click="publish(suggestion)"><span>${publishSuggestionLabel}</span></a><br/>
                <%--TODO END REMOVE AFTER DEV--%>
            </li>
          </ul>
        </div>
        <div class="secteur-container my-suggestionBox-inProgress">
          <div class="header">
            <h3 class="my-suggestionBox-inProgress-title">
              <strong><fmt:message key="suggestionBox.label.suggestions.progress"/></strong>
              <c:out value="${fn:toLowerCase(labelMySuggestions)}"/></h3>
          </div>
          <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
            <div suggestionbox-deletion></div>
            <ul ng-controller="outOfDraftController">
              <li ng-repeat="suggestion in outOfDraftSuggestions">
                <a ng-href="${componentUriBase}suggestions/{{suggestion.id}}">{{suggestion.title}}</a>
                <span class="vote">  </span>
                <span class="counter-comments"><span>{{suggestion.commentCount}} <fmt:message key="GML.comments"/></span></span>
                  <%--TODO BEGIN REMOVE AFTER DEV--%>
                <span>{{suggestion.validation.status}}</span>
                <span></span>
                <img ng-if="'${STATUS_REFUSED}'=== suggestion.validation.status" ng-click="delete(suggestion)" src="${deleteIcon}" alt="remove" style="cursor: pointer"/>
                <span><a ng-if="'${STATUS_PENDING_VALIDATION}'!== suggestion.validation.status && '${STATUS_VALIDATED}'!== suggestion.validation.status" href="#" ng-click="publish(suggestion)"><span>${publishSuggestionLabel}</span></a></span><br/>
                  <%--TODO END REMOVE AFTER DEV--%>
              </li>
            </ul>
          </c:if>
        </div>

          <%--TODO BEGIN REMOVE AFTER DEV--%>
        <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
          <div suggestionbox-validation></div>
          <div ng-controller="pendingValidationController" class="cell">
            <ul id="my_pending_validation_suggestions_list" class="container">
              <li ng-repeat="suggestion in pendingValidationSuggestions">
                <a ng-href="${componentUriBase}suggestions/{{ suggestion.id }}"><span class="suggestion_title">{{ suggestion.title }}</span></a><br/>

                <div>{{ suggestion.validation.status }}</div>
                <div ng-bind-html="suggestion.content"></div>
                <a href="#" ng-click="refuse(suggestion)"><span>${refuseSuggestionLabel}</span></a><br/>
                <a href="#" ng-click="approve(suggestion)"><span>${approveSuggestionLabel}</span></a><br/>
              </li>
            </ul>
          </div>
        </c:if>
          <%--TODO END REMOVE AFTER DEV--%>
      </div>
    </c:if>
    <div id="all-suggestionBox">
      <div class="secteur-container lastSuggestion">
        <div class="header">
          <h3 class="lastSuggestion-title"><fmt:message key="suggestionBox.label.suggestions.last"/></h3>
        </div>
        <ul ng-controller="publishedController">
          <li ng-repeat="suggestion in publishedSuggestions" ng-if="$index < 5">
            <a ng-href="${componentUriBase}suggestions/{{ suggestion.id }}"><span class="date">{{suggestion.validation.date | date: 'shortDate'}}</span>{{suggestion.title}}</a>
          </li>
        </ul>
        <a href="${componentUriBase}suggestions/published" class="more"><fmt:message key="suggestionBox.label.suggestions.more"/></a>
      </div>
      <div class="secteur-container buzzSuggestion">
        <div class="header">
          <h3 class="buzzSuggestion-title">
            <fmt:message key="suggestionBox.label.suggestions.buzz"/></h3>
        </div>
        <ul ng-controller="buzzPublishedController">
          <li ng-repeat="suggestion in buzzPublishedSuggestions">
            <a ng-href="${componentUriBase}suggestions/{{ suggestion.id }}">{{suggestion.title}}</a>
          </li>
        </ul>
      </div>
      <div class="secteur-container lastCommentSuggestion">
        <div class="header">
          <h3 class="lastCommentSuggestion-title">
            <fmt:message key="suggestionBox.label.suggestions.comments.last"/></h3>
        </div>
        <ul>
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