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

<c:set var="currentUser"      value="${requestScope.currentUser}"/>
<c:set var="currentUserId"    value="${currentUser.id}"/>
<c:set var="isUserSubscribed" value="${requestScope.isUserSubscribed}"/>
<c:set var="componentId"      value="${requestScope.browseContext[3]}"/>
<c:set var="greaterUserRole"  value="${requestScope.greaterUserRole}"/>
<c:set var="suggestionBox"    value="${requestScope.currentSuggestionBox}"/>
<c:set var="suggestionBoxId"  value="${suggestionBox.id}"/>
<c:set var="isEdito"          value="${requestScope.isEdito}"/>

<view:setConstant var="adminRole"                 constant="com.stratelia.webactiv.SilverpeasRole.admin"/>
<view:setConstant var="writerRole"                constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
<view:setConstant var="publisherRole"             constant="com.stratelia.webactiv.SilverpeasRole.publisher"/>
<view:setConstant var="STATUS_REFUSED"            constant="org.silverpeas.contribution.ContributionStatus.REFUSED"/>
<view:setConstant var="STATUS_VALIDATED"          constant="org.silverpeas.contribution.ContributionStatus.VALIDATED"/>

<fmt:message key="suggestionBox.menu.item.edito.modify"           var="modifyEditoLabel"/>
<fmt:message key="suggestionBox.menu.item.suggestion.add"         var="browseBarPathSuggestionLabel"/>
<fmt:message key="suggestionBox.menu.item.subscribe"              var="subscribeToSuggestionBoxLabel"/>
<fmt:message key="suggestionBox.menu.item.unsubscribe"            var="unsubscribeFromSuggestionBoxLabel"/>
<fmt:message key="suggestionBox.menu.item.suggestion.viewPending" var="suggestionsInPendingLabel"/>
<fmt:message key="suggestionBox.menu.item.suggestion.mine"        var="mySuggestionsLabel"/>
<fmt:message key="suggestionBox.label.suggestion.status.Refused"  var="refusedValidationStatusLabel"/>
<fmt:message key="suggestionBox.label.noSuggestions"              var="noSuggestions"/>
<fmt:message key="suggestionBox.label.noComments"                 var="noComments"/>
<fmt:message key="suggestionBox.message.edito.empty"              var="editoEmptyMessage">
  <fmt:param><c:url value="${componentUriBase}edito/modify"/></fmt:param>
</fmt:message>

<c:url var="componentUriBase"                   value="${requestScope.componentUriBase}"/>
<c:url var="suggestionBoxJS"                    value="/util/javaScript/angularjs/suggestionbox.js"/>
<c:url var="suggestionBoxServicesJS"            value="/util/javaScript/angularjs/services/suggestionbox.js"/>

<fmt:message key="suggestionBox.refusedSuggestion" var="refusedIconPath"  bundle="${icons}"/>
<fmt:message key="suggestionBox.proposeSuggestion" var="creationIconPath" bundle="${icons}"/>
<c:url var="refusedIcon"   value="${refusedIconPath}"/>
<c:url var="creationIcon" value="${creationIconPath}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.suggestionBox">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <view:includePlugin name="popup"/>
  <view:includePlugin name="toggle"/>
  <view:includePlugin name="rating"/>
  <script type="text/javascript" src="${suggestionBoxServicesJS}"></script>
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
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
    <view:operation action="${componentUriBase}suggestions/pending" altText="${suggestionsInPendingLabel}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
    <view:operationOfCreation action="${componentUriBase}suggestions/new" altText="${browseBarPathSuggestionLabel}" icon="${creationIcon}"/>
    <view:operation action="${componentUriBase}suggestions/mine" altText="${mySuggestionsLabel}"/>
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
</view:operationPane>
<view:window>
  <view:frame>
    <h2 class="suggestionBox-title">${suggestionBox.getTitle(currentUserLanguage)}</h2>
    <c:choose>
      <c:when test="${isEdito}">
        <silverpeas-toggle originalClass="suggestionBox-description">
          <view:displayWysiwyg objectId="${suggestionBoxId}" componentId="${componentId}" language="${null}"/>
        </silverpeas-toggle>
      </c:when>
      <c:when test="${greaterUserRole.isGreaterThanOrEquals(adminRole)}">
        <div class="inlineMessage">${silfn:escapeHtmlWhitespaces(editoEmptyMessage)}</div>
      </c:when>
    </c:choose>
    <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
      <div id="my-suggestionBox">
        <view:areaOfOperationOfCreation/>
        <fmt:message key="suggestionBox.label.suggestions.mine" var="labelMySuggestions"/>
        <div class="secteur-container my-suggestionBox-draft">
          <div class="header">
            <h3 class="my-suggestionBox-inProgress-title"><fmt:message key="suggestionBox.label.suggestions.inDraft"/></h3>
          </div>
          <ul ng-controller="suggestionsInDraftController">
            <li ng-if="inDraftSuggestions.length === 0"><span class="txt-no-content">${noSuggestions}</span></li>
            <li ng-repeat="suggestion in inDraftSuggestions">
              <img ng-if="suggestion.validation.status === '${STATUS_REFUSED}'" src='${refusedIcon}' alt='${refusedValidationStatusLabel}' title='${refusedValidationStatusLabel}'/>
              <a ng-href="${componentUriBase}suggestions/{{ suggestion.id }}">{{suggestion.title}}</a>
            </li>
          </ul>
        </div>
        <div class="secteur-container my-suggestionBox-inProgress">
          <div class="header">
            <h3 class="my-suggestionBox-inProgress-title"><fmt:message key="suggestionBox.label.suggestions.progress"/></h3>
          </div>
          <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
            <ul ng-controller="myOutOfDraftSuggestionsController">
              <li ng-if="myOutOfDraftSuggestions.length === 0"><span class="txt-no-content">${noSuggestions}</span></li>
              <li ng-repeat="suggestion in myOutOfDraftSuggestions">
                <a ng-href="${componentUriBase}suggestions/{{suggestion.id}}">{{suggestion.title}}</a>
                <span ng-if="suggestion.validation.status === '${STATUS_VALIDATED}'" class="vote"><silverpeas-rating readonly="true" forcedisplaywhennorating="true" raterrating="suggestion.raterRating"></silverpeas-rating></span>
                <span ng-if="suggestion.validation.status === '${STATUS_VALIDATED}'" class="counter-comments"><span>{{suggestion.commentCount}} <fmt:message key="GML.comments"/></span></span>
              </li>
            </ul>
          </c:if>
        </div>
      </div>
    </c:if>
    <div id="all-suggestionBox" class="" ng-class="readerView">
      <div class="secteur-container lastSuggestion" ng-controller="publishedSuggestionsController">
        <div class="header">
          <h3 class="lastSuggestion-title"><fmt:message key="suggestionBox.label.suggestions.last"/></h3>
        </div>
        <ul>
          <li ng-if="publishedSuggestions.length === 0"><span class="txt-no-content">${noSuggestions}</span></li>
          <li ng-repeat="suggestion in publishedSuggestions">
            <a ng-href="${componentUriBase}suggestions/{{ suggestion.id }}"><span class="date">{{suggestion.validation.date | date: 'shortDate'}}</span>{{suggestion.title}}</a>
          </li>
        </ul>
        <a href="${componentUriBase}suggestions/published" class="more" ng-if="publishedSuggestions.maxlength > maxItemsToRender"><fmt:message key="suggestionBox.menu.item.suggestions.published"/></a>
      </div>
      <div class="secteur-container buzzSuggestion">
        <div class="header">
          <h3 class="buzzSuggestion-title">
            <fmt:message key="suggestionBox.label.suggestions.buzz"/></h3>
        </div>
        <ul ng-controller="buzzPublishedSuggestionsController">
          <li ng-if="buzzPublishedSuggestions.length === 0"><span class="txt-no-content">${noSuggestions}</span></li>
          <li ng-repeat="suggestion in buzzPublishedSuggestions">
            <a ng-href="${componentUriBase}suggestions/{{ suggestion.id }}">{{suggestion.title}}<span class="counter-comments"><span>{{ suggestion.commentCount }}</span></span></a>
          </li>
        </ul>
      </div>
      <div class="secteur-container lastCommentSuggestion">
        <div class="header">
          <h3 class="lastCommentSuggestion-title">
            <fmt:message key="suggestionBox.label.suggestions.comments.last"/></h3>
        </div>
        <ul ng-controller="lastCommentsController">
          <li ng-if="lastComments.length === 0"><span class="txt-no-content">${noComments}</span></li>
          <li ng-repeat="comment in lastComments">
            <a class="a-suggestion" ng-href="${componentUriBase}suggestions/{{ comment.resourceId }}">{{comment.suggestionTitle}}</a>
            <div class="commentaires">
              <div>
                <div class="avatar"><img alt="{{ comment.author.fullName }}" ng-src="{{ comment.author.avatar }}" /></div>
                <pre class="text">{{ comment.text }}</pre>
                <p class="author"><span>{{ comment.author.fullName }}</span>, <span class="date">{{ comment.creationDate }}</span></p>
              </div>
            </div>
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
    componentUriBase : '${componentUriBase}',
    userRole: '${greaterUserRole}'
  });
</script>
<script type="text/javascript" src="${suggestionBoxJS}"></script>
</body>
</html>