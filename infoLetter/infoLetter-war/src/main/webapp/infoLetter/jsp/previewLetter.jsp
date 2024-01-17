<%--

    Copyright (C) 2000 - 2024 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "https://www.silverpeas.org/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ include file="check.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="resources" value="${requestScope.resources}"/>
<jsp:useBean id="resources" type="org.silverpeas.core.util.MultiSilverpeasBundle"/>
<c:set var="userLanguage" value="${resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${resources.multilangBundle}"/>
<view:setBundle bundle="${resources.iconsBundle}" var="icons"/>

<c:set var="origin" value='${silfn:fullApplicationURL(pageContext.request).replaceFirst("(https?://[^/]+)(.*)", "$1")}'/>
<view:setConstant constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN" var="adminRole"/>
<view:setConstant constant="org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC.TYPE" var="resourceType"/>

<fmt:message key="infoLetter.sendLetterToMe" var="sendLetterToMeLabel"/>
<fmt:message key="infoLetter.sendLetterToMe" var="sendLetterToMeIcon" bundle="${icons}"/>
<c:url var="sendLetterToMeIcon" value="${sendLetterToMeIcon}"/>
<fmt:message key="infoLetter.sendLetterToManager" var="sendLetterToManagerLabel"/>
<fmt:message key="infoLetter.sendLetterToManager" var="sendLetterToManagerIcon" bundle="${icons}"/>
<c:url var="sendLetterToManagerIcon" value="${sendLetterToManagerIcon}"/>
<fmt:message key="infoLetter.validLetter" var="validLetterLabel"/>
<fmt:message key="infoLetter.validLetter" var="validLetterIcon" bundle="${icons}"/>
<c:url var="validLetterIcon" value="${validLetterIcon}"/>
<fmt:message key="infoLetter.confirmResetWithTemplate" var="confirmResetWithTemplate"/>
<fmt:message key="infoLetter.resetWithTemplate" var="resetWithTemplateLabel"/>
<fmt:message key="infoLetter.resetWithTemplate" var="resetWithTemplateIcon" bundle="${icons}"/>
<c:url var="resetWithTemplateIcon" value="${resetWithTemplateIcon}"/>

<fmt:message key="infoLetter.sendLetter" var="sendLetterMsg"/>
<fmt:message key="infoLetter.headerLetter" var="headerLetterLabel"/>
<fmt:message key="infoLetter.editionLetter" var="editionLetterLabel"/>
<fmt:message key="infoLetter.previewLetter" var="previewLetterLabel"/>

<c:set var="componentId" value="<%=componentId%>"/>
<c:set var="entity" value="${requestScope.entity}"/>
<jsp:useBean id="entity" type="org.silverpeas.components.infoletter.model.InfoLetterPublicationPdC"/>
<c:set var="parution" value="${requestScope.parution}"/>
<c:set var="parutionTitle" value="${requestScope.parutionTitle}"/>

<view:sp-page>
  <view:sp-head-part withCheckFormScript="true">
    <view:script src="/infoLetter/jsp/javaScripts/infoLetter.js"/>
    <script type="text/javascript">
      function goHeaders() {
        setupCommonParams(sp.navRequest('ParutionHeaders')).go();
      }

      function goEditContent() {
        setupCommonParams(sp.navRequest('EditContent')).go();
      }

      function sendLetterToMe() {
        $.progressMessage();
        setupCommonParams(sp.navRequest('SendLetterToMe')).go();
      }

      function sendLetterToManager() {
        $.progressMessage();
        setupCommonParams(sp.navRequest('SendLetterToManager')).go();
      }

      function goValidate() {
        jQuery.popup.confirm('${silfn:escapeJs(sendLetterMsg)}', function() {
          $.progressMessage();
          setupCommonParams(sp.navRequest('ValidateParution')).go();
        });
      }

      function goResetWithTemplate() {
        jQuery.popup.confirm('${silfn:escapeJs(confirmResetWithTemplate)}', function() {
          $.progressMessage();
          setupCommonParams(sp.navRequest('EditContent')).withParam('resetWithTemplate', true).go();
        });
      }

      function setupCommonParams(request) {
        return request.withParam('parution', '${parution}');
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar path="${parutionTitle}"/>
    <view:operationPane>
      <view:operation action="javascript:sendLetterToMe()" altText="${sendLetterToMeLabel}" icon="${sendLetterToMeIcon}"/>
      <view:operation action="javascript:sendLetterToManager()" altText="${sendLetterToManagerLabel}" icon="${sendLetterToManagerIcon}"/>
      <view:operationSeparator/>
      <view:operation action="javascript:goValidate()" altText="${validLetterLabel}" icon="${validLetterIcon}"/>
      <view:operationSeparator/>
      <view:operation action="javascript:goResetWithTemplate()" altText="${resetWithTemplateLabel}" icon="${resetWithTemplateIcon}"/>
    </view:operationPane>
    <view:window>
      <view:tabs>
        <view:tab label="${previewLetterLabel}" action="javascript:void(0)" selected="true"/>
        <view:tab label="${headerLetterLabel}" action="javascript:goHeaders()" selected="false"/>
        <view:tab label="${editionLetterLabel}" action="javascript:goEditContent()" selected="false"/>
      </view:tabs>
      <view:frame>
        <div id="preview">
          <div class="rightContent">
            <c:set var="callbackUrl" value="Preview?parution=${parution}"/>
            <viewTags:displayAttachments componentInstanceId="${componentId}"
                                         resourceId="${parution}"
                                         resourceType="${resourceType}"
                                         reloadCallbackUrl="${callbackUrl}"
                                         highestUserRole="${adminRole}"
                                         hasToBeIndexed="${false}"
                                         showIcon="${true}"
                                         showTitle="${true}"
                                         showDescription="${true}"
                                         showFileSize="${true}"
                                         showMenuNotif="${true}"
                                         contributionManagementContext="${requestScope.contributionManagementContext}"/>
          </div>
          <div class="principalContent">
            <h2 class="publiName">${silfn:escapeHtml(entity.title)}</h2>
            <c:if test="${not empty entity.description}">
              <p class="publiDesc text2">${silfn:escapeHtml(entity.description)}</p>
            </c:if>
            <div id="inlined-css-html-container"></div>
            <script type="text/javascript">
              whenSilverpeasReady().then(function() {
                monitorHeightOfIsolatedDisplay('${parution}', '${origin}');
              });
            </script>
          </div>
        </div>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>