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

<c:set var="webServiceProvider" value="${requestScope.webServiceProvider}"/>
<view:setConstant var="writerRole" constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
<view:setConstant var="publisherRole" constant="com.stratelia.webactiv.SilverpeasRole.publisher"/>
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<c:if test="${!greaterUserRole.isGreaterThanOrEquals(writerRole)}">
  <c:redirect url="/Error403.jsp"/>
</c:if>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="save" key="GML.validate"/>
<fmt:message var="cancel" key="GML.cancel"/>
<fmt:message var="back" key="GML.back"/>
<fmt:message var="publishSuggestionMenuLabel" key="GML.publish"/>
<fmt:message var="modifySuggestionMenuLabel" key="GML.modify"/>
<fmt:message var="deleteSuggestionMenuLabel" key="GML.delete"/>
<fmt:message var="deleteSuggestionConfirmMessage" key="suggestionBox.message.suggestion.remove.confirm">
  <fmt:param value=""/>
</fmt:message>
<fmt:message var="validateSuggestionMenuLabel" key="GML.validate"/>
<fmt:message var="refuseSuggestionMenuLabel" key="GML.refuse"/>

<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="language" value="${requestScope.resources.language}"/>
<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>
<c:url var="formValidator" value="/util/javaScript/checkForm.js"/>
<c:set var="componentUriBase"><c:url value="${requestScope.componentUriBase}"/></c:set>
<c:set var="suggestionBoxId" value="${requestScope.suggestionBox.id}"/>
<c:set var="suggestion" value="${requestScope.suggestion}"/>
<c:set var="target" value="add"/>
<c:set var="isPublishable" value="false"/>
<c:set var="isEditable" value="false"/>
<c:set var="isModeratorView" value="false"/>
<c:set var="canModeratorModifying" value="false"/>
<c:set var="isModeratorModifying" value="false"/>
<c:set var="isSuggestionReadOnly" value="false"/>
<c:if test="${suggestion != null}">
  <c:set var="target" value="${suggestion.id}"/>
  <c:set var="isEditable" value="${not suggestion.validated}"/>
  <c:set var="isPublishable" value="${suggestion.isPublishableBy(currentUser)}"/>
  <c:set var="isSuggestionReadOnly" value="${not isEditable or empty requestScope.edit}"/>
  <c:set var="isModeratorView" value="${suggestion.pendingValidation and greaterUserRole.isGreaterThanOrEquals(publisherRole)}"/>
  <c:set var="canModeratorModifying" value="${isModeratorView and isSuggestionReadOnly}"/>
  <c:set var="isModeratorModifying" value="${isModeratorView and not isSuggestionReadOnly}"/>
</c:if>

<c:choose>
  <c:when test="${suggestion == null}">
    <fmt:message var="browseBarPathSuggestionLabel" key="suggestionBox.menu.item.suggestion.add"/>
  </c:when>
  <c:when test="${isSuggestionReadOnly and isModeratorView}">
    <c:set var="browseBarPathSuggestionLabel">${validateSuggestionMenuLabel}</c:set>
  </c:when>
  <c:when test="${not isSuggestionReadOnly}">
    <c:set var="browseBarPathSuggestionLabel">${modifySuggestionMenuLabel}</c:set>
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
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <view:includePlugin name="wysiwyg"/>
  <view:includePlugin name="popup"/>
  <script type="text/javascript" src="${suggestionBoxServicesJS}"></script>
  <script type="text/javascript" src="${suggestionBoxValidationDirectiveJS}"></script>
  <script type="text/javascript" src="${suggestionBoxDeletionDirectiveJS}"></script>
  <script type="text/javascript" src="${formValidator}"></script>
  <script type="text/javascript">
    <c:if test="${not isSuggestionReadOnly}">
    $(document).ready(function() {
      <view:wysiwyg replace="content" language="${null}" toolbar="suggestionBox"/>
      $('#title').focus();
    });

    function getSuggestionContent() {
      return CKEDITOR.instances.content;
    }

    function validate(suggestion) {
      var error = {
        msg : '',
        count : 0
      };
      if (isWhitespace(suggestion.title)) {
        error.msg +=
            "<b><fmt:message key='GML.title'/></b> <fmt:message key='GML.MustBeFilled'/><br/>";
        error.count++;
      }
      return error;
    }

    function save() {
      var suggestion = {
        title : $('#title').val(),
        content : getSuggestionContent()
      };
      var error = validate(suggestion);
      if (error.count > 0) {
        notyError(error.msg);
      } else {
        $('#suggestion').submit();
      }
    }
    </c:if>

    <c:if test="${isSuggestionReadOnly and isEditable}">
    function modify() {
      $('#actions').attr('method', 'GET').attr('action',
          '${componentUriBase}suggestion/${target}/edit').submit();
    }
    </c:if>

    <c:if test="${isSuggestionReadOnly and isPublishable}">
    function publish() {
      $('#actions').attr('action', '${componentUriBase}suggestion/${target}/publish').submit();
    }
    function remove() {
      $('#delete').trigger('click');
    }
    </c:if>
    <c:if test="${isSuggestionReadOnly and canModeratorModifying}">
    function approve() {
      $('#approve').trigger('click');
    }
    function refuse() {
      $('#refuse').trigger('click');
    }
    </c:if>

    function cancel() {
      <c:choose>
      <c:when test="${suggestion == null or isSuggestionReadOnly}">
      $('#actions').attr('method', 'GET').attr('action', '${componentUriBase}Main').submit();
      </c:when>
      <c:otherwise>
      $('#actions').attr('method', 'GET').attr('action',
          '${componentUriBase}suggestion/${target}').submit();
      </c:otherwise>
      </c:choose>
    }
  </script>
</head>
<body ng-controller="mainController">
<view:browseBar componentId="${componentId}" path="${browseBarPathSuggestionLabel}"/>
<view:operationPane>
  <c:if test="${isSuggestionReadOnly and isEditable}">
    <view:operation action="javascript:modify();" altText="${modifySuggestionMenuLabel}"/>
    <view:operationSeparator/>
  </c:if>
  <c:if test="${isSuggestionReadOnly and isPublishable}">
    <view:operation action="javascript:publish();" altText="${publishSuggestionMenuLabel}"/>
    <view:operation action="javascript:remove();" altText="${deleteSuggestionMenuLabel}"/>
    <div id="delete" suggestionbox-deletion ng-click="delete(suggestion,true)" style="display: none"></div>
  </c:if>
  <c:if test="${isSuggestionReadOnly and canModeratorModifying}">
    <view:operation action="javascript:refuse();" altText="${refuseSuggestionMenuLabel}"/>
    <view:operation action="javascript:approve();" altText="${validateSuggestionMenuLabel}"/>
    <div suggestionbox-validation style="display: none"></div>
    <div id="refuse" ng-click="refuse(suggestion)" style="display: none"></div>
    <div id="approve" ng-click="approve(suggestion)" style="display: none"></div>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <div id="error" style="display: none;"></div>
    <form id="suggestion" name="suggestion" action="${componentUriBase}suggestion/${target}" method="POST">
      <div class="fields">
        <div class="field" id="suggestionName">
          <label for="title" class="txtlibform"><fmt:message key='GML.title'/><c:if test="${not isSuggestionReadOnly}">&nbsp;<img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/></c:if></label>

          <div class="champs">
            <c:choose>
              <c:when test="${isSuggestionReadOnly}">
                <span id="title"><c:out value='${suggestion.title}'/></span>
              </c:when>
              <c:otherwise>
                <input id="title" type="text" name="title" size="100%" maxlength="2000" value="<c:out value='${suggestion.title}'/>"/>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
        <br clear="all"/>

        <div class="field" id="eventDescriptionArea">
          <label for="content" class="txtlibform"><fmt:message key='GML.description'/></label>

          <div class="champs">
            <c:choose>
              <c:when test="${isSuggestionReadOnly}">
                <span id="content">${suggestion.content}</span>
              </c:when>
              <c:otherwise>
                <textarea rows="5" cols="10" name="content" id="content">${suggestion.content}</textarea>
              </c:otherwise>
            </c:choose>
          </div>
        </div>
      </div>
      <input type="hidden" value="${componentId}"/>

      <c:if test="${not isSuggestionReadOnly}">
      <div class="legend">
        <img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/>&nbsp;
        <fmt:message key='GML.requiredField'/>
      </div>
      </c:if>
      <view:buttonPane>
        <c:choose>
          <c:when test="${isSuggestionReadOnly}">
            <view:button label="${back}" action="javascript:cancel();"/>
          </c:when>
          <c:otherwise>
            <view:button label="${save}" action="javascript:save();"/>
            <view:button label="${cancel}" action="javascript:cancel();"/>
          </c:otherwise>
        </c:choose>
      </view:buttonPane>
    </form>
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