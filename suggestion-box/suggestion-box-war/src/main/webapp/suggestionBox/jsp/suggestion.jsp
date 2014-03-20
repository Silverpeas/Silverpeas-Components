<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<view:setConstant var="publishRole" constant="com.stratelia.webactiv.SilverpeasRole.publisher" />
<c:set var="greaterUserRole" value="${requestScope.greaterUserRole}"/>
<c:if test="${! greaterUserRole.isGreaterThanOrEquals(publishRole)}">
  <c:redirect url="/Error403.jsp"/>
</c:if>

<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="language" value="${requestScope.resources.language}"/>
<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>
<c:url var="formValidator" value="/util/javaScript/checkForm.js"/>
<c:set var="componentUriBase"><c:url value="${requestScope.componentUriBase}"/></c:set>
<c:set var="suggestion" value="${requestScope.suggestion}"/>

<fmt:message var="save" key="GML.validate"/>
<fmt:message var="cancel" key="GML.cancel"/>
<fmt:message var="addSuggestionLabel" key="suggestionBox.menu.item.suggestion.add"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Frameset//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd">
<html>
<head>
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
        msg: '',
        count: 0
      };
      if (isWhitespace(suggestion.title)) {
          error.msg += " - '<fmt:message key='GML.title'/>' <fmt:message key='GML.MustBeFilled'/>\n";
          error.count++;
      }
      return error;
    }

    function save() {
      var suggestion = {
        title: $('#title').val(),
        content: getSuggestionContent()
      };
      var error = validate(suggestion);
      if (error.count > 0) {
        $('#error').html(error.msg);
        $('#error').popup('information', {
          title: '<fmt:message key="GML.errors"/>'
        });
      } else {
        $('#suggestion').submit();
      }
    }

    function cancel() {
      $('#suggestion').attr('action', '${componentUriBase}Main').submit();
    }

    $(document).ready(function() {
      <view:wysiwyg replace="content" language="${null}" toolbar="suggestionBox"/>
      $('#title').focus();
     });
  </script>
</head>
<body>
<view:browseBar componentId="${componentId}" extraInformations="${addSuggestionLabel}"/>
<view:window>
  <view:frame>
    <div id="error" style="display: none;"></div>
    <c:choose>
      <c:when test="${suggestion == null}">
    <form id="suggestion" name="suggestion" action="${componentUriBase}suggestion/add" method="POST">   
      </c:when>
      <c:otherwise>
    <form id="suggestion" name="suggestion" action="${componentUriBase}suggestion/${suggestion.id}" method="PUT">    
      </c:otherwise>
    </c:choose>
      <div class="fields">
        <div class="field" id="suggestionName">
          <label for="title" class="txtlibform"><fmt:message key='GML.title'/>&nbsp;<img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/></label>
          <div class="champs">
            <input id="title" type="text" name="title" size="100%" maxlength="2000" value="${title}"/>
          </div>
        </div>
        <br clear="all"/>
        <div class="field" id="eventDescriptionArea">
          <label for="content" class="txtlibform"><fmt:message key='GML.description'/></label>
          <div class="champs">
            <textarea rows="5" cols="10" name="content" id="content">${content}</textarea>
          </div>
        </div>
      </div>
      <input type="hidden" value="${componentId}"/>
      <div class="legend">
        <img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/>&nbsp; <fmt:message key='GML.requiredField'/>
      </div>
      <view:buttonPane>
        <view:button label="${save}" action="javascript:save();"/>
        <view:button label="${cancel}" action="javascript:cancel();"/>
      </view:buttonPane>
    </form>
  </view:frame>
</view:window>
</body>
</html>