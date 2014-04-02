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
<view:setConstant var="writerRole" constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="back" key="GML.back"/>
<fmt:message var="publishSuggestionMenuLabel" key="GML.publish"/>
<fmt:message var="modifySuggestionMenuLabel" key="GML.modify"/>
<fmt:message var="deleteSuggestionMenuLabel" key="GML.delete"/>
<fmt:message var="validateSuggestionMenuLabel" key="GML.validate"/>
<fmt:message var="refuseSuggestionMenuLabel" key="GML.refuse"/>

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

<c:choose>
  <c:when test="${isModeratorView}">
    <c:set var="browseBarPathSuggestionLabel">${validateSuggestionMenuLabel}</c:set>
  </c:when>
  <c:otherwise>
    <c:set var="browseBarPathSuggestionLabel" value=""/>
  </c:otherwise>
</c:choose>

<c:url var="suggestionBoxJS" value="/util/javaScript/angularjs/suggestionbox.js"/>
<c:url var="suggestionBoxServicesJS" value="/util/javaScript/angularjs/services/suggestionbox.js"/>
<c:url var="suggestionBoxValidationDirectiveJS" value="/util/javaScript/angularjs/directives/suggestionbox-validation.js"/>
<c:url var="suggestionBoxDeletionDirectiveJS" value="/util/javaScript/angularjs/directives/suggestionbox-deletion.js"/>

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
          '${componentUriBase}suggestion/${target}/edit').submit();
    }
    </c:if>

    <c:if test="${isPublishable}">
    function publish() {
      $('#actions').attr('action', '${componentUriBase}suggestion/${target}/publish').submit();
    }
    </c:if>

    function cancel() {
      $('#actions').attr('method', 'GET').attr('action', '${componentUriBase}Main').submit();
    }
  </script>
</head>
<body ng-controller="mainController" id="${componentId}">
<view:browseBar componentId="${componentId}" path="${browseBarPathSuggestionLabel}"/>
<view:operationPane>
  <c:if test="${isEditable}">
    <view:operation action="javascript:modify();" altText="${modifySuggestionMenuLabel}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${isPublishable}">
    <view:operation action="javascript:publish();" altText="${publishSuggestionMenuLabel}"/>
    <view:operation action="angularjs:delete(suggestion, true)" altText="${deleteSuggestionMenuLabel}"/>
    <div suggestionbox-deletion style="display: none"></div>
  </c:if>
  <c:if test="${isModeratorView}">
    <view:operation action="angularjs:refuse(suggestion)" altText="${refuseSuggestionMenuLabel}"/>
    <view:operation action="angularjs:approve(suggestion)" altText="${validateSuggestionMenuLabel}"/>
    <div suggestionbox-validation style="display: none"></div>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <div id="error" style="display: none;"></div>
    <div class="rightContent">
      <view:attachmentPane componentId="${componentId}" resourceId="${suggestion.id}" readOnly="${suggestion.validation.validated}"/>
    </div>
    <div class="fields">
      <label class="txtlibform"><fmt:message key='GML.title'/></label>

      <div class="champs"><c:out value='${suggestion.title}'/></div>
      <c:if test="${not empty suggestion.content}">
        <br clear="all"/>

        <div class="field">
          <label class="txtlibform"><fmt:message key='GML.description'/></label>
          <span>${suggestion.content}</span>
        </div>
      </c:if>
      <c:if test="${not empty suggestion.validation.comment && (suggestion.validation.validated || suggestion.validation.refused)}">
        <br clear="all"/>

        <div class="field" id="validationCommentArea">
          <label class="txtlibform"><fmt:message key='GML.contribution.validation.comment'/></label>

          <div class="champs">
            <span>${suggestion.validation.comment}</span>
          </div>
        </div>
      </c:if>
    </div>

    <br clear="all"/>
    <view:buttonPane>
      <view:button label="${back}" action="javascript:cancel();"/>
    </view:buttonPane>

    <c:choose>
      <c:when test="${greaterUserRole.isGreaterThanOrEquals(writerRole)}">
        <view:comments componentId="${componentId}" resourceId="${suggestion.id}" userId="${currentUser.id}" resourceType="${suggestion.contributionType}"/>
      </c:when>
      <c:otherwise>
        <view:commentListing componentId="${componentId}" resourceId="${suggestion.id}" userId="${currentUser.id}"/>
      </c:otherwise>
    </c:choose>
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