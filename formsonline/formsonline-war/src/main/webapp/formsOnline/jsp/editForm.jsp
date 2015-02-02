<%@ page import="com.silverpeas.formsonline.control.FormsOnlineSessionController" %>
<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>

<fmt:setLocale value="${lang}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />

<c:set var="form" value="${requestScope['currentForm']}"/>
<c:set var="templates" value="${requestScope['availableTemplates']}"/>
<c:set var="m_listGroupSenders" value="${form.sendersAsGroups}"/>
<c:set var="m_listUserSenders" value="${form.sendersAsUsers}"/>
<c:set var="m_listGroupReceivers" value="${form.receiversAsGroups}"/>
<c:set var="m_listUserReceivers" value="${form.receiversAsUsers}"/>

<c:url var="cssFieldset" value="/util/styleSheets/fieldset.css"/>
<c:url var="iconMandatory" value="/util/icons/mandatoryField.gif"/>
<c:url var="jsCheckForm" value="/util/javaScript/checkForm.js"/>

<fmt:message key="formsOnline.senders" var="labelSenders"/>
<fmt:message key="formsOnline.receivers" var="labelReceivers"/>
<fmt:message key="GML.mandatory" var="labelMandatory"/>

<c:set var="id_ListSenders" value="<%=FormsOnlineSessionController.userPanelSendersPrefix%>"/>
<c:set var="id_ListReceivers" value="<%=FormsOnlineSessionController.userPanelReceiversPrefix%>"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title></title>
<view:looknfeel/>
<link type="text/css" href="${cssFieldset}" rel="stylesheet" />
<script type="text/javascript" src="${jsCheckForm}"></script>
<script type="text/javascript">

function isCorrectForm() {
  var errorMsg = "";
  var errorNb = 0;

  var description = stripInitialWhitespace(document.creationForm.description.value);
	var templateSelectedIndex = document.creationForm.template.selectedIndex;
  var title = stripInitialWhitespace(document.creationForm.title.value);

  if (isWhitespace(title)) {
    errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="GML.title"/>' <fmt:message key="GML.MustBeFilled"/>\n";
    errorNb++;
  }

  if (templateSelectedIndex < 1) {
     errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="formsOnline.Template"/>' <fmt:message key="GML.MustBeFilled"/>\n";
     errorNb++;
  }

  if (isWhitespace(description)) {
    errorMsg+="  - <fmt:message key="GML.theField"/> '<fmt:message key="GML.description"/>' <fmt:message key="GML.MustBeFilled"/>\n";
    errorNb++;
  }

  switch(errorNb) {
    case 0 :
        result = true;
        break;
    case 1 :
        errorMsg = "<fmt:message key="GML.ThisFormContains"/> 1 <fmt:message key="GML.error"/> : \n" + errorMsg;
        window.alert(errorMsg);
        result = false;
        break;
    default :
        errorMsg = "<fmt:message key="GML.ThisFormContains"/> " + errorNb + " <fmt:message key="GML.errors"/> :\n" + errorMsg;
        window.alert(errorMsg);
        result = false;
        break;
  }
  return result;
}

function valider() {
	if (isCorrectForm()) {
		document.creationForm.submit();
	}
}

function viewForm() {
  var xml = $("#templates").val();
  SP_openWindow("Preview?Form="+xml);
}

function showHidePreviewButton() {
  var xml = $("#templates").val();
  if (xml.length > 0) {
    $("#view-form").show();
  } else {
    $("#view-form").hide();
  }
}

$(document).ready(function() {

  showHidePreviewButton();

  $("#templates").change(function() {
    showHidePreviewButton();
  });
});
</script>

</head>
<body>
<view:window>

	<form name="creationForm" action="SaveForm" method="post">

    <fieldset id="informationForm" class="skinFieldset">
      <legend><fmt:message key="GML.bloc.information.principals"/></legend>
      <div class="fields">
        <div class="field" id="titleForm">
          <label for="title" class="txtlibform"><fmt:message key="GML.title"/></label>
          <div class="champs">
            <input type="text" id="title" name="title" size="60" maxlength="200" value="${form.title}">
            &nbsp;<img width="5" height="5" alt="${labelMandatory}" src="${iconMandatory}" /> </div>
        </div>
        <div class="field" id="templateForm">
          <label for="template" class="txtlibform"><fmt:message key="formsOnline.Template"/></label>
          <div class="champs">
            <c:choose>
              <c:when test="${form.id != -1}">
                <select name="template" id="templates" size="1" disabled="disabled">
              </c:when>
              <c:otherwise>
                <select name="template" id="templates" size="1">
              </c:otherwise>
            </c:choose>
              <option value="">---------------------</option>
              <c:forEach items="${templates}" var="template">
                <c:choose>
                  <c:when test="${not empty form.xmlFormName && form.xmlFormName == template.fileName}">
                    <option selected="selected" value="${template.fileName}">${template.name}</option>
                  </c:when>
                  <c:otherwise>
                    <option value="${template.fileName}">${template.name}</option>
                  </c:otherwise>
                </c:choose>
              </c:forEach>
            </select>
            &nbsp;<img width="5" height="5" alt="${labelMandatory}" src="${iconMandatory}" /> <br />
            <a class="button" id="view-form" href="#" onclick="viewForm();return false;"><span><fmt:message key="formsOnline.Preview"/></span></a> </div>
        </div>
        <div class="field" id="descriptionForm">
          <label for="description" class="txtlibform"><fmt:message key="GML.description"/></label>
          <div class="champs">
            <textarea rows="4" cols="65" name="description" id="description">${form.description}</textarea>
            &nbsp;<img width="5" height="5" alt="${labelMandatory}" src="${iconMandatory}" />
          </div>
        </div>
      </div>
    </fieldset>

    <div class="table">
      <div class="cell">
        <viewTags:displayListOfUsersAndGroups users="${m_listUserSenders}" groups="${m_listGroupSenders}" label="${labelSenders}" id="${id_ListSenders}" updateCallback="ModifySenders"/>
      </div>
      <div class="cell">
        <viewTags:displayListOfUsersAndGroups users="${m_listUserReceivers}" groups="${m_listGroupReceivers}" label="${labelReceivers}" id="${id_ListReceivers}" updateCallback="ModifyReceivers"/>
      </div>
    </div>
    <div class="legend"> <img width="5" height="5" alt="${labelMandatory}" src="${iconMandatory}"> : ${labelMandatory}</div>

  </form>

  <view:buttonPane>
    <fmt:message var="buttonValidate" key="GML.validate"/>
    <fmt:message var="buttonCancel" key="GML.cancel"/>
    <view:button label="${buttonValidate}" action="javascript:valider();" />
    <view:button label="${buttonCancel}" action="Main" />
  </view:buttonPane>

</view:window>

</body>
</html>