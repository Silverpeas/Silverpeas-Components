<%--
  ~ Copyright (C) 2000 - 2018 Silverpeas
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

<fmt:message var="windowTitle"   key="windowTitleParametrageRequete"/>
<fmt:message var="crumbTitle"    key="titreParametrageRequete"/>
<fmt:message var="popupTitle"    key="titrePopup"/>
<fmt:message var="resultTab"     key="tabbedPaneConsultation"/>
<fmt:message var="queryTab"      key="tabbedPaneRequete"/>
<fmt:message var="dataSourceTab" key="tabbedPaneParametresJDBC"/>
<fmt:message var="queryField"    key="champRequete"/>
<fmt:message var="buttonOk"      key="boutonValider"/>
<fmt:message var="buttonCancel"  key="boutonAnnuler"/>
<fmt:message var="requestError"  key="erreurChampsTropLong"/>
<fmt:message var="requestEditor" key="operationPaneRequete"/>

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
      if (isValidTextMaxi(document.requestEdition.SQLReq)) {
        document.requestEdition.action = "SetSQLRequest";
        document.requestEdition.submit();
      } else {
        var err = '${requestError}';
        jQuery.popup.error(err);
      }
    }

    function cancel() {
      document.requestEdition.action = "DoRequest";
      document.requestEdition.submit();
    }

    function launchSQLRequestEditor() {
      displaySingleFreePopupFrom('${requestScope.editorUrl}', {
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
        <label for="SQLReq">${queryField}&nbsp;:</label>
        <textarea id="SQLReq" name="SQLReq" rows="15" cols="100%" style="padding: 1px; margin-bottom: 10px;">
        <c:out value="${requestScope.sqlRequest}" escapeXml="true"/>
        </textarea>
        <view:buttonPane>
          <view:button label="${buttonOk}" action="javascript:onClick=saveRequest()"/>
          <view:button label="${buttonCancel}" action="javascript:onClick=cancel()"/>
        </view:buttonPane>
      </form>
  </view:frame>
</view:window>
</body>