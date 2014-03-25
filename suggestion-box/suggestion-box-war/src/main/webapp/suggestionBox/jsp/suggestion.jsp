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
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<c:if test="${!greaterUserRole.isGreaterThanOrEquals(writerRole)}">
  <c:redirect url="/Error403.jsp"/>
</c:if>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="language" value="${requestScope.resources.language}"/>
<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>
<c:url var="formValidator" value="/util/javaScript/checkForm.js"/>
<c:set var="componentUriBase"><c:url value="${requestScope.componentUriBase}"/></c:set>
<c:set var="suggestion" value="${requestScope.suggestion}"/>
<c:set var="target" value="add"/>
<c:set var="isPublishable" value="false"/>
<c:if test="${suggestion != null}">
  <c:set var="target" value="${suggestion.id}"/>
  <c:set var="isPublishable" value="${suggestion.isPublishableBy(currentUser)}"/>
</c:if>
<c:set var="isReadOnly" value="${!isPublishable}"/>
<fmt:message var="save" key="GML.validate"/>
<fmt:message var="cancel" key="GML.cancel"/>
<fmt:message var="addSuggestionLabel" key="suggestionBox.menu.item.suggestion.add"/>
<fmt:message var="publishSuggestionMenuLabel" key="GML.publish"/>
<fmt:message var="deleteSuggestionMenuLabel" key="GML.delete"/>
<fmt:message var="deleteSuggestionConfirmMessage" key="suggestionBox.message.suggestion.confirm">
  <fmt:param value=""/>
</fmt:message>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel/>
  <view:includePlugin name="wysiwyg"/>
  <view:includePlugin name="popup"/>
  <script type="text/javascript" src="${formValidator}"></script>
  <script type="text/javascript">
    function getSuggestionContent() {
      return CKEDITOR.instances.content;
    }

    function validate(suggestion) {
      var error = {
        msg : '',
        count : 0
      };
      if (isWhitespace(suggestion.title)) {
        error.msg += " - '<fmt:message key='GML.title'/>' <fmt:message key='GML.MustBeFilled'/>\n";
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
        $('#error').html(error.msg);
        $('#error').popup('error', {
          title : '<fmt:message key="GML.errors"/>'
        });
      } else {
        $('#suggestion').submit();
      }
    }

    function cancel() {
      $('#actions').attr('method', 'GET').attr('action', '${componentUriBase}Main').submit();
    }

    <c:if test="${isPublishable}">
    function publish() {
      $('#actions').attr('action', '${componentUriBase}suggestion/publish/${target}').submit();
    }

    function remove() {
      $('#confirmation').html("${deleteSuggestionConfirmMessage} ?");
      $('#confirmation').popup('confirmation', {
        callback : function() {
          $('#actions').attr('action',
              '${componentUriBase}suggestion/delete/${target}').submit();
        }
      });
    }
    </c:if>

    $(document).ready(function() {
      <view:wysiwyg replace="content" language="${null}" toolbar="suggestionBox"/>
      $('#title').focus();
    });
  </script>
</head>
<body>
<view:browseBar componentId="${componentId}" extraInformations="${addSuggestionLabel}"/>
<c:if test="${isPublishable}">
  <view:operationPane>
    <view:operation action="javascript:publish();" altText="${publishSuggestionMenuLabel}"/>
    <view:operation action="javascript:remove();" altText="${deleteSuggestionMenuLabel}"/>
  </view:operationPane>
</c:if>
<view:window>
  <view:frame>
    <div id="confirmation" style="display: none;"></div>
    <div id="error" style="display: none;"></div>
    <form id="suggestion" name="suggestion" action="${componentUriBase}suggestion/${target}" method="POST">
      <div class="fields">
        <div class="field" id="suggestionName">
          <label for="title" class="txtlibform"><fmt:message key='GML.title'/>&nbsp;<img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/></label>

          <div class="champs">
            <input id="title" type="text" name="title" size="100%" maxlength="2000" value="<c:out value='${suggestion.title}'/>"/>
          </div>
        </div>
        <br clear="all"/>

        <div class="field" id="eventDescriptionArea">
          <label for="content" class="txtlibform"><fmt:message key='GML.description'/></label>

          <div class="champs">
            <textarea rows="5" cols="10" name="content" id="content">${suggestion.content}</textarea>
          </div>
        </div>
      </div>
      <input type="hidden" value="${componentId}"/>

      <div class="legend">
        <img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/>&nbsp;
        <fmt:message key='GML.requiredField'/>
      </div>
      <view:buttonPane>
        <view:button label="${save}" action="javascript:save();"/>
        <view:button label="${cancel}" action="javascript:cancel();"/>
      </view:buttonPane>
    </form>
  </view:frame>
</view:window>
<form id="actions" name="actions" action="#" method="POST" style="display: none"></form>
</body>
</html>