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

<view:setConstant var="writerRole" constant="com.stratelia.webactiv.SilverpeasRole.writer"/>
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="save" key="GML.validate"/>
<fmt:message var="cancel" key="GML.cancel"/>
<fmt:message var="modifySuggestionMenuLabel" key="GML.modify"/>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>
<c:url var="formValidator" value="/util/javaScript/checkForm.js"/>
<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>
<c:url var="backUri" value="${requestScope.backUrl}"/>
<c:set var="suggestion" value="${requestScope.suggestion}"/>
<c:set var="target" value="add"/>
<c:if test="${suggestion != null}">
  <c:set var="target" value="${suggestion.id}"/>
</c:if>

<c:if test="${not greaterUserRole.isGreaterThanOrEquals(writerRole)}">
  <c:redirect url="/Error403.jsp"/>
</c:if>

<c:choose>
  <c:when test="${suggestion == null}">
    <fmt:message var="browseBarPathSuggestionLabel" key="suggestionBox.menu.item.suggestion.add"/>
  </c:when>
  <c:otherwise>
    <c:set var="browseBarPathSuggestionLabel">${modifySuggestionMenuLabel}</c:set>
  </c:otherwise>
</c:choose>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <view:looknfeel/>
  <view:includePlugin name="wysiwyg"/>
  <view:includePlugin name="popup"/>
  <link type="text/css" href="<c:url value='/util/styleSheets/fieldset.css'/>" rel="stylesheet"/>
  <script type="text/javascript" src="${formValidator}"></script>
  <script type="text/javascript">
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

    function cancel() {
      <c:choose>
      <c:when test="${suggestion == null}">
      $('#actions').attr('method', 'GET').attr('action', '${backUri}').submit();
      </c:when>
      <c:otherwise>
      $('#actions').attr('method', 'GET').attr('action',
          '${componentUriBase}suggestions/${target}').submit();
      </c:otherwise>
      </c:choose>
    }
  </script>
</head>
<body>
<view:browseBar componentId="${componentId}" path="${browseBarPathSuggestionLabel}"/>
<view:window>
  <view:frame>
    <div id="error" style="display: none;"></div>
    <form id="suggestion" name="suggestion" action="${componentUriBase}suggestions/${target}" method="POST">
      <input type="hidden" value="${componentId}"/>

      <div class="table">
        <div class="cell">
          <fieldset class="skinFieldset" id="suggestionInformation">
            <legend><fmt:message key="GML.bloc.information.principal"/></legend>
            <div class="fields">
              <div id="suggestionNameArea" class="field">
                <label for="title" class="txtlibform"><fmt:message key='GML.title'/>&nbsp;<img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/></label>

                <div class="champs">
                  <input id="title" type="text" name="title" size="70%" maxlength="2000" value="<c:out value='${suggestion.title}'/>" style="width: auto"/>
                </div>
              </div>
            </div>
          </fieldset>
        </div>
        <c:if test="${suggestion == null}">
          <div style="width: 50%" class="cell">
            <view:fileUpload fieldset="true" jqueryFormSelector="form[name='suggestion']"/>
          </div>
        </c:if>
      </div>
      <div class="fields">
        <div id="suggestionDescriptionArea" class="field">
          <label class="txtlibform" for="suggestionDescription"><fmt:message key='GML.description'/></label>

          <div class="champs">
            <textarea rows="5" cols="10" name="content" id="suggestionDescription">${suggestion.content}</textarea>
          </div>
        </div>
      </div>

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