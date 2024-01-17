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
<%@ page import="org.silverpeas.core.persistence.jdbc.DBUtil" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="resources" value="${requestScope.resources}"/>
<jsp:useBean id="resources" type="org.silverpeas.core.util.MultiSilverpeasBundle"/>
<c:set var="userLanguage" value="${resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${resources.multilangBundle}"/>
<view:setBundle bundle="${resources.iconsBundle}" var="icons"/>

<fmt:message key="GML.validate" var="validateLabel"/>
<fmt:message key="GML.cancel" var="cancelLabel"/>
<fmt:message key="infoLetter.sendLetterToMe" var="sendLetterToMeLabel"/>
<fmt:message key="infoLetter.sendLetterToMe" var="sendLetterToMeIcon" bundle="${icons}"/>
<c:url var="sendLetterToMeIcon" value="${sendLetterToMeIcon}"/>
<fmt:message key="infoLetter.sendLetterToManager" var="sendLetterToManagerLabel"/>
<fmt:message key="infoLetter.sendLetterToManager" var="sendLetterToManagerIcon" bundle="${icons}"/>
<c:url var="sendLetterToManagerIcon" value="${sendLetterToManagerIcon}"/>

<fmt:message key="infoLetter.headerLetter" var="headerLetterLabel"/>
<fmt:message key="infoLetter.editionLetter" var="editionLetterLabel"/>
<fmt:message key="infoLetter.previewLetter" var="previewLetterLabel"/>

<c:set var="componentId" value="<%=componentId%>"/>
<c:set var="parution" value="${requestScope.parution}"/>
<c:set var="browseBarPath" value="${requestScope.browseBarPath}"/>
<c:set var="title" value="${requestScope.title}"/>
<c:set var="description" value="${requestScope.description}"/>

<view:sp-page>
  <view:sp-head-part withFieldsetStyle="true" withCheckFormScript="true">
    <script type="text/javascript">

      function goEditContent() {
        setupCommonParams(sp.navRequest('EditContent')).go();
      }

      function goView() {
        setupCommonParams(sp.navRequest('Preview')).go();
      }

      function submitForm() {
        let errorMsg = "";
        let errorNb = 0;

        if (!isValidTextArea(document.changeParutionHeaders.description)) {
          errorMsg +=
              "  - <fmt:message key="GML.theField"/> '<fmt:message key="GML.description"/>' <fmt:message key="ContainsTooLargeText"/> <%=DBUtil.getTextAreaLength()%> <fmt:message key="Characters"/>\n";
          errorNb++;
        }

        if (isWhitespace(stripInitialWhitespace(document.changeParutionHeaders.title.value))) {
          errorMsg +=
              "  - <fmt:message key="GML.theField"/> '<fmt:message key="infoLetter.name"/>' <fmt:message key="GML.MustBeFilled"/>\n";
          errorNb++;
        }

        <view:pdcValidateClassification errorCounter="errorNb" errorMessager="errorMsg"/>

        switch (errorNb) {
          case 0 :
            <view:pdcPositions setIn="document.changeParutionHeaders.Positions.value"/>;
            document.changeParutionHeaders.action = "ChangeParutionHeaders";
            $.progressMessage();
            document.changeParutionHeaders.submit();
            break;
          case 1 :
            errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> :\n" + errorMsg;
            jQuery.popup.error(errorMsg);
            break;
          default :
            errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
            jQuery.popup.error(errorMsg);
        }
      }

      function cancelForm() {
        sp.navRequest('Accueil').go();
      }

      function sendLetterToMe() {
        $.progressMessage();
        setupCommonParams(sp.navRequest('SendLetterToMe')).go();
      }

      function sendLetterToManager() {
        $.progressMessage();
        setupCommonParams(sp.navRequest('SendLetterToManager')).go();
      }

      function setupCommonParams(request) {
        return request.withParam('parution', '${parution}');
      }
    </script>
  </view:sp-head-part>
  <view:sp-body-part cssClass="infoletter">
    <view:browseBar path="${silfn:escapeHtml(browseBarPath)}"/>
    <view:operationPane>
      <c:if test="${not empty parution}">
        <view:operation action="javascript:sendLetterToMe()" altText="${sendLetterToMeLabel}" icon="${sendLetterToMeIcon}"/>
        <view:operation action="javascript:sendLetterToManager()" altText="${sendLetterToManagerLabel}" icon="${sendLetterToManagerIcon}"/>
      </c:if>
    </view:operationPane>
    <view:window>
      <c:if test="${not empty parution}">
        <view:tabs>
          <view:tab label="${previewLetterLabel}" action="javascript:goView()" selected="false"/>
          <view:tab label="${headerLetterLabel}" action="javascript:void(0)" selected="true"/>
          <view:tab label="${editionLetterLabel}" action="javascript:goEditContent()" selected="false"/>
        </view:tabs>
      </c:if>
      <view:frame>

        <%-- Initialize image --%>
        <fmt:message key="infoLetter.mandatory" var="mandatoryIcon" bundle="${icons}"/>
        <c:url var="mandatoryIconUrl" value="${mandatoryIcon}"/>

        <form name="changeParutionHeaders" action="ChangeParutionHeaders" method="post">
          <input type="hidden" name="parution" value="${parution}"/>
          <input type="hidden" name="Positions" value=""/>


          <fieldset id="infoFieldset" class="skinFieldset">
            <legend><fmt:message key="infoletter.header.fieldset.info"/></legend>

            <!-- SAISIE DU FORUM -->
            <div class="fields">
              <!-- Info letter title -->
              <div class="field" id="titleArea">
                <label class="txtlibform" for="title"><fmt:message key="infoLetter.name"/>
                  :&nbsp;</label>
                <div class="champs">
                  <input type="text" id="title" name="title" size="50" maxlength="<%=DBUtil.getTextFieldLength()%>" value="${title}"/>&nbsp;<img src="${mandatoryIconUrl}" width="5" height="5" alt=""/>
                </div>
              </div>
              <!-- Info letter description  -->
              <div class="field" id="descriptionArea">
                <label class="txtlibform" for="description"><fmt:message key="GML.description"/> :&nbsp;</label>
                <div class="champs">
                  <textarea id="description" name="description" cols="60" rows="6">${description}</textarea>
                </div>
              </div>
            </div>
          </fieldset>

        </form>
        <c:if test="${empty parution}">
          <view:pdcNewContentClassification componentId="${componentId}"/>
        </c:if>
        <c:if test="${not empty parution}">
          <view:pdcClassification componentId="${componentId}" contentId="${parution}" editable="true"/>
        </c:if>

        <div class="legend">
          <fmt:message key="GML.requiredField"/> :
          <img src="${mandatoryIconUrl}" width="5" height="5" alt=""/>
        </div>
        <view:buttonPane>
          <view:button label="${validateLabel}" action="javascript:submitForm()"/>
          <view:button label="${cancelLabel}" action="javascript:cancelForm()"/>
        </view:buttonPane>
      </view:frame>
    </view:window>
    <view:progressMessage/>
  </view:sp-body-part>
</view:sp-page>