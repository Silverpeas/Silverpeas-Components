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
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<view:setConstant var="writerRole"                          constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
<view:setConstant var="publisherRole"                       constant="com.stratelia.webactiv.SilverpeasRole.publisher"/>
<view:setConstant var="AllSuggestionsViewContext"           constant="org.silverpeas.components.suggestionbox.control.SuggestionBoxWebController.ViewContext.AllSuggestions"/>
<view:setConstant var="SuggestionsInValidationViewContext"  constant="org.silverpeas.components.suggestionbox.control.SuggestionBoxWebController.ViewContext.SuggestionsInValidation"/>
<view:setConstant var="MySuggestionsViewContext"            constant="org.silverpeas.components.suggestionbox.control.SuggestionBoxWebController.ViewContext.MySuggestions"/>
<view:setConstant var="SUGGESTION_LIST_IDENTIFIER"          constant="org.silverpeas.components.suggestionbox.control.SuggestionBoxWebController.SUGGESTION_LIST_ARRAYPANE_IDENTIFIER"/>

<c:set var="greaterUserRole"     value="${requestScope.greaterUserRole}"/>
<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>

<fmt:setLocale  value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="back"                   key="GML.back"/>
<fmt:message var="date"                   key="GML.date"/>
<fmt:message var="title"                  key="GML.title"/>
<fmt:message var="author"                 key="suggestionBox.label.suggestion.author"/>
<fmt:message var="rating"                 key="GML.rating"/>
<fmt:message var="ratingCount"            key="GML.rating.participation.number"/>
<fmt:message var="commentCount"           key="GML.comment.number"/>
<fmt:message var="status"                 key="GML.status"/>
<fmt:message var="proposeSuggestionLabel" key="suggestionBox.menu.item.suggestion.add"/>

<fmt:message var="refusedIconPath"           key="suggestionBox.refusedSuggestion"             bundle="${icons}"/>
<fmt:message var="validatedIconPath"         key="suggestionBox.validatedSuggestion"           bundle="${icons}"/>
<fmt:message var="pendingValidationIconPath" key="suggestionBox.SuggestionInPendingValidation" bundle="${icons}"/>
<fmt:message var="inDraftIconPath"           key="suggestionBox.SuggestionInDraft"             bundle="${icons}"/>
<fmt:message var="creationIconPath"          key="suggestionBox.proposeSuggestion"             bundle="${icons}"/>

<fmt:message key="suggestionBox.menu.item.suggestions.all"        var="allSuggestionsLabel"/>
<fmt:message key="suggestionBox.menu.item.suggestion.viewPending" var="suggestionsInPendingLabel"/>
<fmt:message key="suggestionBox.menu.item.suggestion.mine"        var="mySuggestionsLabel"/>
<fmt:message key="suggestionBox.menu.item.suggestions.published"  var="allPublishedSuggestionsLabel"/>

<c:set var="currentUser"     value="${requestScope.currentUser}"/>
<c:set var="componentId"     value="${requestScope.browseContext[3]}"/>
<c:set var="suggestionBox"   value="${requestScope.currentSuggestionBox}"/>
<c:set var="suggestionBoxId" value="${suggestionBox.id}"/>
<c:set var="isEdito"         value="${requestScope.isEdito}"/>
<c:set var="suggestions"     value="${requestScope.suggestions}"/>

<c:url var="viewContext"             value="${requestScope.navigationContext.currentNavigationStep.contextIdentifier}"/>
<c:url var="componentUriBase"        value="${requestScope.componentUriBase}"/>
<c:set var="allSuggestionsUri"       value="${componentUriBase}suggestions/all"/>
<c:set var="publishedSuggestionsUri" value="${componentUriBase}suggestions/published"/>
<c:set var="mineSuggestionsUri"      value="${componentUriBase}suggestions/mine"/>
<c:set var="pendingSuggestionsUri"   value="${componentUriBase}suggestions/pending"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.suggestionBox">
<head>
  <view:looknfeel/>
  <view:includePlugin name="toggle"/>
  <view:includePlugin name="rating"/>
  <script type="text/javascript">
    function goBack() {
      document.location = "${componentUriBase}Main";
    }
  </script>
</head>
<body id="${componentId}">
<view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}"/>
<view:operationPane>
  <c:url var="creationIcon" value="${creationIconPath}"/>
  <c:choose>
    <%-- ViewContext : all suggestions the user can see --%>
    <c:when test="${viewContext == AllSuggestionsViewContext}">
      <c:set var="routingAddress" value="${allSuggestionsUri}"/>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
        <view:operation action="${pendingSuggestionsUri}" altText="${suggestionsInPendingLabel}"/>
        <view:operationSeparator/>
      </c:if>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
        <view:operationOfCreation action="${componentUriBase}suggestions/new" altText="${proposeSuggestionLabel}" icon="${creationIcon}"/>
        <view:operationSeparator/>
      </c:if>
      <view:operation action="${publishedSuggestionsUri}" altText="${allPublishedSuggestionsLabel}"/>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
        <view:operation action="${mineSuggestionsUri}" altText="${mySuggestionsLabel}"/>
      </c:if>
    </c:when>
    <%-- ViewContext : suggestions in pending validation status --%>
    <c:when test="${viewContext == SuggestionsInValidationViewContext}">
      <c:set var="routingAddress" value="${pendingSuggestionsUri}"/>
      <view:operation action="${publishedSuggestionsUri}" altText="${allPublishedSuggestionsLabel}"/>
      <view:operation action="${allSuggestionsUri}" altText="${allSuggestionsLabel}"/>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
        <view:operation action="${mineSuggestionsUri}" altText="${mySuggestionsLabel}"/>
      </c:if>
    </c:when>
    <%-- ViewContext : my suggestions --%>
    <c:when test="${viewContext == MySuggestionsViewContext}">
      <c:set var="routingAddress" value="${mineSuggestionsUri}"/>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
        <view:operation action="${pendingSuggestionsUri}" altText="${suggestionsInPendingLabel}"/>
        <view:operationSeparator/>
      </c:if>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
        <view:operationOfCreation action="${componentUriBase}suggestions/new" altText="${proposeSuggestionLabel}" icon="${creationIcon}"/>
        <view:operationSeparator/>
      </c:if>
      <view:operation action="${publishedSuggestionsUri}" altText="${allPublishedSuggestionsLabel}"/>
      <view:operation action="${allSuggestionsUri}" altText="${allSuggestionsLabel}"/>
    </c:when>
    <%-- ViewContext : all published suggestions --%>
    <c:otherwise>
      <c:set var="routingAddress" value="${publishedSuggestionsUri}"/>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(publisherRole)}">
        <view:operation action="${pendingSuggestionsUri}" altText="${suggestionsInPendingLabel}"/>
        <view:operationSeparator/>
      </c:if>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
        <view:operationOfCreation action="${componentUriBase}suggestions/new" altText="${proposeSuggestionLabel}" icon="${creationIcon}"/>
        <view:operationSeparator/>
      </c:if>
      <view:operation action="${allSuggestionsUri}" altText="${allSuggestionsLabel}"/>
      <c:if test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
        <view:operation action="${mineSuggestionsUri}" altText="${mySuggestionsLabel}"/>
      </c:if>
    </c:otherwise>
  </c:choose>
</view:operationPane>
<view:window>
  <view:frame>
    <h2 class="suggestionBox-title">${suggestionBox.getTitle(currentUserLanguage)}</h2>
    <c:if test="${isEdito}">
      <div silverpeas-toggle originalClass="suggestionBox-description">
        <view:displayWysiwyg objectId="${suggestionBoxId}" componentId="${componentId}" language="${null}"/>
      </div>
    </c:if>
    <c:if test="${viewContext != SuggestionsInValidationViewContext and greaterUserRole.isGreaterThanOrEquals(writerRole)}">
      <view:areaOfOperationOfCreation/>
    </c:if>
    <view:arrayPane var="${SUGGESTION_LIST_IDENTIFIER}" routingAddress="${routingAddress}" sortableLines="true">
      <view:arrayColumn title="${status}" sortable="true"/>
      <view:arrayColumn title="${date}" sortable="true"/>
      <view:arrayColumn title="${title}" sortable="true"/>
      <view:arrayColumn title="${author}" sortable="true"/>
      <c:if test="${viewContext != SuggestionsInValidationViewContext}">
        <view:arrayColumn title="${rating}" sortable="true"/>
        <view:arrayColumn title="${ratingCount}" sortable="true"/>
        <view:arrayColumn title="${commentCount}" sortable="true"/>
      </c:if>
      <c:forEach var="suggestion" items="${suggestions}">
        <view:arrayLine>
          <c:choose>
            <c:when test="${suggestion.validation.refused}">
              <c:url var="statusIcon" value="${refusedIconPath}"/>
              <fmt:message key="suggestionBox.label.suggestion.status.Refused" var="suggestionStatus"/>
            </c:when>
            <c:when test="${suggestion.validation.validated}">
              <c:url var="statusIcon" value="${validatedIconPath}"/>
              <fmt:message key="suggestionBox.label.suggestion.status.Validated" var="suggestionStatus"/>
            </c:when>
            <c:when test="${suggestion.validation.pendingValidation}">
              <c:url var="statusIcon" value="${pendingValidationIconPath}"/>
              <fmt:message key="suggestionBox.label.suggestion.status.PendingValidation" var="suggestionStatus"/>
            </c:when>
            <c:otherwise>
              <c:url var="statusIcon" value="${inDraftIconPath}"/>
              <fmt:message key="suggestionBox.label.suggestion.status.InDraft" var="suggestionStatus"/>
            </c:otherwise>
          </c:choose>
          <view:arrayCellText text="<img src='${statusIcon}' alt='${suggestionStatus}' title='${suggestionStatus}'/>"/>
          <%-- the last update date is the validation date for refused and accepted suggestions --%>
          <view:arrayCellText text="<!-- ${suggestion.lastUpdateDate} -->${silfn:formatDate(suggestion.lastUpdateDate, currentUserLanguage)}"/>
          <view:arrayCellText text="<!-- ${suggestion.title} --><a href=\"${componentUriBase}suggestions/${suggestion.id}\">${suggestion.title}</a>"/>
          <view:arrayCellText text="${suggestion.authorName}"/>
          <c:if test="${viewContext != SuggestionsInValidationViewContext}">
            <c:choose>
              <c:when test="${suggestion.validation.validated}">
                <c:set var="_currentRaterRating"><viewTags:displayContributionRating readOnly="${true}" showNbRaterRatings="${false}" raterRating="${suggestion.raterRating}"/></c:set>
                <view:arrayCellText text="${_currentRaterRating}"/>
                <fmt:formatNumber pattern='#0000000000.##' value='${suggestion.raterRating.numberOfRaterRatings}' var="tmpValue"/>
                <view:arrayCellText text="<!-- ${tmpValue} -->${suggestion.raterRating.numberOfRaterRatings}"/>
                <fmt:formatNumber pattern='#0000000000.##' value='${suggestion.commentCount}' var="tmpValue"/>
                <view:arrayCellText text="<!-- ${tmpValue} -->${suggestion.commentCount}"/>
              </c:when>
              <c:otherwise>
                <view:arrayCellText text="-"/>
                <view:arrayCellText text="-"/>
                <view:arrayCellText text="-"/>
              </c:otherwise>
            </c:choose>
          </c:if>
        </view:arrayLine>
      </c:forEach>
    </view:arrayPane>
    <br clear="all"/>
    <view:buttonPane>
      <view:button label="${back}" action="javascript:goBack();"/>
    </view:buttonPane>
  </view:frame>
</view:window>
<script type="text/javascript">
  var myapp = angular.module('silverpeas.suggestionBox', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>
</html>