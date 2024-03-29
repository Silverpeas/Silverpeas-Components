<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%-- Set resource bundle --%>
<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="parution" value="${requestScope.parution}"/>
<c:set var="parutionTitle" value="${silfn:escapeHtml(requestScope.parutionTitle)}"/>
<c:set var="parutionContent" value="${requestScope.parutionContent}"/>

<view:sp-page>
  <view:sp-head-part withCheckFormScript="true">
    <view:includePlugin name="wysiwyg"/>
    <script type="text/javascript">
      function goHeaders() {
        setupCommonParams(sp.navRequest('ParutionHeaders')).go();
      }

      function goEditContent() {
        setupCommonParams(sp.navRequest('EditContent')).go();
      }

      function setupCommonParams(request) {
        return request.withParam('parution', '${parution}');
      }

      function goView() {
        setupCommonParams(sp.navRequest('Preview')).go();
      }

      function cancel() {
        sp.editor.wysiwyg.lastBackupManager.clear();
        goView();
      }

      function saveContentData() {
        $.progressMessage();
        sp.editor.wysiwyg.lastBackupManager.clear();
        document.contentForm.action = "SaveContent";
        CKEDITOR.instances.Content.updateElement();
        document.contentForm.submit();
      }

      $(document).ready(function() {
        <view:wysiwyg replace="Content" language="${userLanguage}" width="95%" height="500" toolbar="infoLetter"
                      spaceLabel="<%=spaceLabel%>" componentId="<%=componentId%>" componentLabel="<%=componentLabel%>"
                      browseInfo="${parutionTitle}" objectId="${parution}" activateWysiwygBackupManager="true" />
      });
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar extraInformations="${parutionTitle}"/>
    <view:operationPane>
      <view:operation action="EditContent?parution=${parution}&wbe=true" altText="Editer avec l'éditeur' Drag & Drop"/>
    </view:operationPane>
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
              <textarea rows="5" cols="10" name="editor" id="Content" style="display: none">${parutionContent}</textarea>
            </div>
          </div>
        </form>
        <br/>
        <view:buttonPane>
          <fmt:message key='GML.validate' var="tmpLabel"/>
          <view:button label="${tmpLabel}" action="javascript:onClick=saveContentData()"/>
          <fmt:message key='GML.cancel' var="tmpLabel"/>
          <view:button label="${tmpLabel}" action="javascript:onClick=cancel()"/>
        </view:buttonPane>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>