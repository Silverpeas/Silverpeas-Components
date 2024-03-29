<%--
  Copyright (C) 2000 - 2024 Silverpeas

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have received a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.

  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%@ page import="org.silverpeas.core.notification.user.NotificationContext" %>

<view:setConstant var="writerRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.WRITER"/>
<view:setConstant var="adminRole"  constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN"/>
<c:set var="highestUserRole" value="${requestScope.highestUserRole}"/>

<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="back" key="GML.back"/>
<fmt:message var="publishSuggestionMenuLabel" key="GML.publish"/>
<fmt:message var="modifySuggestionMenuLabel" key="GML.modify"/>
<fmt:message var="deleteSuggestionMenuLabel" key="GML.delete"/>
<fmt:message var="validateSuggestionMenuLabel" key="GML.validate"/>
<fmt:message var="refuseSuggestionMenuLabel" key="GML.refuse"/>
<fmt:message var="notifyLabel" key="GML.notify"/>

<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="suggestionBox" value="${requestScope.currentSuggestionBox}"/>
<c:set var="suggestionBoxId" value="${suggestionBox.id}"/>
<c:set var="suggestion" value="${requestScope.suggestion}"/>
<c:set var="target" value="${suggestion.id}"/>
<c:set var="isEditable" value="${requestScope.isEditable}"/>
<c:set var="isPublishable" value="${requestScope.isPublishable}"/>
<c:set var="isModeratorView" value="${requestScope.isModeratorView}"/>
<c:set var="isAccessGuest" value="${requestScope.isAccessGuest}"/>
<jsp:useBean id="suggestionBox" type="org.silverpeas.components.suggestionbox.model.SuggestionBox"/>
<jsp:useBean id="suggestion" type="org.silverpeas.components.suggestionbox.web.SuggestionEntity"/>

<c:url var="backUri" value="${requestScope.navigationContext.previousNavigationStep.uri}"/>
<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>
<c:url var="suggestionBoxJS" value="/suggestionBox/jsp/javaScript/angularjs/suggestionbox.js"/>
<c:url var="suggestionBoxServicesJS" value="/suggestionBox/jsp/javaScript/angularjs/services/suggestionbox.js"/>
<c:url var="suggestionBoxValidationDirectiveJS" value="/suggestionBox/jsp/javaScript/angularjs/directives/suggestionbox-validation.js"/>
<c:url var="suggestionBoxDeletionDirectiveJS" value="/suggestionBox/jsp/javaScript/angularjs/directives/suggestionbox-deletion.js"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.suggestionBox">
<head>
  <view:looknfeel/>
  <view:includePlugin name="popup"/>
  <script type="text/javascript" src="${suggestionBoxServicesJS}"></script>
  <script type="text/javascript" src="${suggestionBoxValidationDirectiveJS}"></script>
  <script type="text/javascript" src="${suggestionBoxDeletionDirectiveJS}"></script>
  <script type="text/javascript">
    <c:if test="${isEditable}">
    function modify() {
      $('#actions').attr('method', 'GET').attr('action',
          '${componentUriBase}suggestions/${target}/edit').submit();
    }
    </c:if>

    <c:if test="${isPublishable}">
    function publish() {
      $('#actions').attr('action', '${componentUriBase}suggestions/${target}/publish').submit();
    }
    </c:if>

    function cancel() {
      $('#actions').attr('method', 'GET').attr('action', '${backUri}').submit();
    }
    function notify() {
      sp.messager.open('${componentId}', {<%=NotificationContext.CONTRIBUTION_ID%>: '${target}'});
    }
  </script>
</head>
<body ng-controller="mainController" id="${componentId}" class="suggestion-view">
<view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}">
  <c:if test="${isModeratorView}">
    <view:browseBarElt link="#" label="${validateSuggestionMenuLabel}"/>
  </c:if>
</view:browseBar>
<view:operationPane>
  <c:if test="${isEditable}">
    <view:operation action="javascript:modify();" altText="${modifySuggestionMenuLabel}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${isPublishable}">
    <view:operation action="javascript:publish();" altText="${publishSuggestionMenuLabel}"/>
  </c:if>
  <c:if test="${isPublishable or highestUserRole.isGreaterThanOrEquals(adminRole)}">
    <view:operation action="angularjs:remove(suggestion, true)" altText="${deleteSuggestionMenuLabel}"/>
    <div suggestionbox-deletion></div>
  </c:if>
  <c:if test="${isModeratorView}">
    <view:operation action="angularjs:refuse(suggestion)" altText="${refuseSuggestionMenuLabel}"/>
    <view:operation action="angularjs:approve(suggestion)" altText="${validateSuggestionMenuLabel}"/>
    <div suggestionbox-validation></div>
  </c:if>
  <c:if test="${suggestion.validation.validated && !isAccessGuest}">
    <view:operation action="javascript:notify()" altText="${notifyLabel}"/>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <div id="error" style="display: none;"></div>
    <div class="rightContent">
      <c:if test="${suggestion.validation.validated}">
        <viewTags:displayContributionRating readOnly="${false}" showNbRaterRatings="${true}" raterRating="${suggestion.raterRating}"/>
      </c:if>
      <viewTags:displayLastUserCRUD createDate="${suggestion.createDate}" createdBy="${suggestion.author}"/>
      <view:attachmentPane componentId="${componentId}" resourceId="${suggestion.id}" readOnly="${suggestion.validation.validated}"/>
    </div>
    <div class="principalContent">
      <h2 class="suggestion-title"><c:out value='${suggestion.title}'/></h2>
      <c:if test="${not empty suggestion.content}">
        <div id="richContent" class="rich-content">
            ${suggestion.content}
        </div>
      </c:if>
      <viewTags:viewAttachmentsAsContent componentInstanceId="${componentId}"
                                         resourceType="${suggestion.contributionType}"
                                         resourceId="${suggestion.id}"
                                         highestUserRole="${highestUserRole}"/>
      <c:if test="${not empty suggestion.validation.comment and (suggestion.validation.validated or suggestion.validation.refused)}">
        <div id="suggestionApprobationDetail">
          <p id="suggestionApprobationDetail-info"><span class="libelle"><fmt:message key="suggestionBox.label.suggestion.approbator.comment">
            <fmt:param><strong class="author"><c:out value="${suggestion.validation.validatorName}"/></strong></fmt:param>
            <fmt:param><span class="date"><c:out value="${silfn:formatDate(suggestion.validation.date, currentUserLanguage)}"/></span></fmt:param>
          </fmt:message></span></p>

          <div id="suggestionApprobationDetail-text" class="inlineMessage">
            <c:set var="validationComment" value="${suggestion.validation.comment}"/>
              ${silfn:escapeHtmlWhitespaces(validationComment)}
          </div>
        </div>
      </c:if>
    </div>

    <br clear="all"/>
    <view:buttonPane>
      <view:button label="${back}" action="javascript:cancel();"/>
    </view:buttonPane>

    <c:if test="${suggestion.validation.validated}">
      <viewTags:displayComments componentId="${componentId}"
                                resourceId="${suggestion.id}"
                                resourceType="${suggestion.contributionType}"/>

    </c:if>
  </view:frame>
</view:window>
<form id="actions" name="actions" action="#" method="POST" style="display: none"></form>
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
