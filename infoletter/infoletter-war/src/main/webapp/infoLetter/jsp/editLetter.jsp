<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%-- Set resource bundle --%>
<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="parution" value="${requestScope.parution}"/>
<c:set var="parutionTitle" value="${silfn:escapeHtml(requestScope.parutionTitle)}"/>
<c:set var="parutionContent" value="${requestScope.parutionContent}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><fmt:message key="GML.popupTitle"/></title>
  <view:looknfeel/>
  <view:includePlugin name="wysiwyg"/>
<script type="text/javascript" src="<c:url value='/util/javaScript/checkForm.js'/>"></script>
<script type="text/javascript">
  function goHeaders() {
    document.headerParution.submit();
  }

  function goView() {
    document.viewParution.submit();
  }

  function goFiles() {
    document.attachedFiles.submit();
  }

  function saveContentData() {
    $.progressMessage();
    document.contentForm.action = "SaveContent";
    CKEDITOR.instances.Content.updateElement();
    document.contentForm.submit();
  }

  $(document).ready(function() {
    <view:wysiwyg replace="Content" language="${userLanguage}" width="95%" height="500" toolbar="infoLetter"
                  spaceId="<%=spaceId%>" spaceName="<%=spaceLabel%>" componentId="<%=componentId%>" componentName="<%=componentLabel%>"
                  browseInfo="${parutionTitle}" objectId="${parution}" />
  });
</script>
</head>
<body>
<view:browseBar extraInformations="${parutionTitle}"/>
<view:window>
<view:tabs>
  <fmt:message key='infoLetter.headerLetter' var="tmpLabel"/>
  <view:tab label="${tmpLabel}" action="javascript:goHeaders();" selected="false"/>
  <fmt:message key='infoLetter.editionLetter' var="tmpLabel"/>
  <view:tab label="${tmpLabel}" action="#" selected="true"/>
  <fmt:message key='infoLetter.previewLetter' var="tmpLabel"/>
  <view:tab label="${tmpLabel}" action="javascript:goView();" selected="false"/>
  <fmt:message key='infoLetter.attachedFiles' var="tmpLabel"/>
  <view:tab label="${tmpLabel}" action="javascript:goFiles();" selected="false"/>
</view:tabs>
<view:frame>
<form name="contentForm" action="javascript:saveContentData();" method="post">
  <input type="hidden" name="parution" value="${parution}"/>

  <div class="field" id="contentArea">
    <div class="champs">
      <div class="container-wysiwyg wysiwyg-fileStorage">

        <viewTags:displayToolBarWysiwyg
            editorName="Content"
            componentId="<%=componentId%>"
            objectId="${parution}" />
      </div>

      <textarea rows="5" cols="10" name="Content" id="Content">${parutionContent}</textarea>
    </div>
  </div>
</form>
<br/>
<view:buttonPane>
  <fmt:message key='GML.validate' var="tmpLabel"/>
  <view:button label="${tmpLabel}" action="javascript:onClick=saveContentData()"/>
  <fmt:message key='GML.cancel' var="tmpLabel"/>
  <view:button label="${tmpLabel}" action="javascript:onClick=goView()"/>
</view:buttonPane>

<form name="headerParution" action="ParutionHeaders" method="post">
  <input type="hidden" name="parution" value="${parution}"/>
  <input type="hidden" name="ReturnUrl" value="Preview"/>
</form>
<form name="viewParution" action="Preview" method="post">
  <input type="hidden" name="parution" value="${parution}"/>
</form>
<form name="attachedFiles" action="FilesEdit" method="post">
  <input type="hidden" name="parution" value="${parution}"/>
</form>
</view:frame>
</view:window>
<view:progressMessage/>
</body>
</html>