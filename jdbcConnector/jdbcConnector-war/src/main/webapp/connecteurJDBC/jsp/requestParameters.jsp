<%--
  ~ Copyright (C) 2000 - 2019 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ include file="head.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="currentUserLanguage" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<view:setConstant var="adminRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.admin"/>
<view:setConstant var="publisherRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.publisher"/>

<c:if test="${not requestScope.highestUserRole.isGreaterThanOrEquals(publisherRole)}">
  <c:redirect url="/Error403.jsp"/>
</c:if>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>

<fmt:message var="windowTitle"       key="windowTitleParametrageRequete"/>
<fmt:message var="crumbTitle"        key="titreParametrageRequete"/>
<fmt:message var="popupTitle"        key="titrePopup"/>
<fmt:message var="resultTab"         key="tabbedPaneConsultation"/>
<fmt:message var="queryTab"          key="tabbedPaneRequete"/>
<fmt:message var="dataSourceTab"     key="tabbedPaneParametresJDBC"/>
<fmt:message var="queryField"        key="champRequete"/>
<fmt:message var="queryFieldInError" key="champRequeteEnErreur"/>
<fmt:message var="buttonOk"          key="boutonValider"/>
<fmt:message var="buttonCancel"      key="boutonAnnuler"/>
<fmt:message var="msgMustBeFilled"   key="GML.MustBeFilled"/>
<fmt:message var="requestError"      key="erreurChampsTropLong"/>
<fmt:message var="requestEditor"     key="operationPaneRequete"/>

<c:url var="mandatoryIcons" value="/util/icons/mandatoryField.gif"/>
<c:url var="editorIcon" value="/util/icons/connecteurJDBC_request.gif"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <title>${windowTitle}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <view:looknfeel withCheckFormScript="true" withFieldsetStyle="true"/>
  <view:includePlugin name="popup"/>
  <script type="application/javascript">
    function saveRequest() {
      var $request = $('#SQLReq');
      if (isWhitespace($request.val())) {
        SilverpeasError.add('<b>${queryField}</b> ${msgMustBeFilled}');
      } else if (!isValidTextMaxi($request.val())) {
        SilverpeasError.add('<b>${queryField}</b> ${requestError}');
      }
      if (!SilverpeasError.show()) {
        document.requestEdition.action = "SetSQLRequest";
        document.requestEdition.submit();
      }
    }

    function cancel() {
      sp.formRequest('DoRequest').byPostMethod().submit();
    }

    function launchSQLRequestEditor() {
      window.requestEditorDialog = jQuery.popup.load('${requestScope.editorUrl}');
      window.requestEditorDialog.show('free', {
        title: '${popupTitle}',
        closeOnEscape: true,
        resizable: true,
        width: '800px'});
    }
  </script>
</head>
<body>
<view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}" extraInformations="${crumbTitle}"/>
<view:operationPane>
  <view:operation action="javascript:onClick=launchSQLRequestEditor()" altText="${requestEditor}" icon="${editorIcon}"/>
</view:operationPane>
<view:window>
  <view:tabs>
    <view:tab label="${resultTab}" action="Main" selected="false"/>
    <view:tab label="${queryTab}" action="ParameterRequest" selected="true"/>
    <c:if test="${requestScope.highestUserRole.isGreaterThanOrEquals(adminRole)}">
      <view:tab label="${dataSourceTab}" action="ParameterConnection" selected="false"/>
    </c:if>
  </view:tabs>
  <view:frame>
    <form name="requestEdition" action="SetSQLRequest" method="post">
      <fieldset class="skinFieldset">
        <div class="fields oneFieldPerLine">
          <div class="field">
            <label class="txtlibform" for="SQLReq">${queryField}</label>
            <div class="champs">
              <textarea id="SQLReq" name="SQLReq" rows="15" cols="100%" style="padding: 1px; margin-bottom: 10px;"><c:out value="${requestScope.sqlRequest}" escapeXml="true"/></textarea>
              <span>&nbsp;<img border="0" src="${mandatoryIcons}" width="5" height="5"></span>
            </div>
          </div>
          <c:if test="${not empty requestScope.sqlRequestInError}">
            <div class="field">
              <label class="txtlibform" for="SQLReqInError">${queryFieldInError}</label>
              <div class="champs">
                <textarea id="SQLReqInError" disabled="disabled" name="SQLReqInError" rows="15" cols="100%" style="padding: 1px; margin-bottom: 10px;"><c:out value="${requestScope.sqlRequestInError}" escapeXml="true"/></textarea>
              </div>
            </div>
          </c:if>
        </div>
      </fieldset>
      <div class="legend">
        <img alt="mandatory" src="${mandatoryIcons}" width="5" height="5"/>&nbsp;
        <fmt:message key='GML.requiredField'/>
      </div>
      <p>
        <view:buttonPane>
          <view:button label="${buttonOk}" action="javascript:onClick=saveRequest()"/>
          <view:button label="${buttonCancel}" action="javascript:onClick=cancel()"/>
        </view:buttonPane>
      </p>
    </form>
  </view:frame>
</view:window>
</body>