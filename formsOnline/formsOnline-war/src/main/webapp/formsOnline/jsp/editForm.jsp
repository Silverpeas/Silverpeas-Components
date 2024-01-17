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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>

<%@ page import="org.silverpeas.components.formsonline.control.FormsOnlineSessionController" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>

<fmt:setLocale value="${lang}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<c:set var="form" value="${requestScope['currentForm']}"/>
<jsp:useBean id="form" type="org.silverpeas.components.formsonline.model.FormDetail"/>
<c:set var="isCreationCase" value="${form.id eq -1}"/>
<c:set var="templates" value="${requestScope['availableTemplates']}"/>
<c:set var="m_listGroupSenders" value="${form.sendersAsGroups}"/>
<c:set var="m_listUserSenders" value="${form.sendersAsUsers}"/>
<c:set var="m_listGroupReceivers" value="${form.receiversAsGroups}"/>
<c:set var="m_listUserReceivers" value="${form.receiversAsUsers}"/>
<c:set var="m_listGroupIntermediateReceivers" value="${form.intermediateReceiversAsGroups}"/>
<c:set var="m_listUserIntermediateReceivers" value="${form.intermediateReceiversAsUsers}"/>

<c:set var="hierarchicalValidation" value=""/>
<c:if test="${form.hierarchicalValidation}">
  <c:set var="hierarchicalValidation" value="checked=\"checked\""/>
</c:if>

<c:set var="deleteAfterRequestExchange" value=""/>
<c:if test="${form.deleteAfterRequestExchange}">
  <c:set var="deleteAfterRequestExchange" value="checked=\"checked\""/>
</c:if>

<c:set var="requestExchangeReceiver" value=""/>
<c:if test="${form.requestExchangeReceiver.isPresent()}">
  <c:set var="requestExchangeReceiver" value="${form.requestExchangeReceiver.get()}"/>
</c:if>

<c:url var="iconMandatory" value="/util/icons/mandatoryField.gif"/>

<fmt:message key="formsOnline.senders" var="labelSenders"/>
<fmt:message key="formsOnline.receivers" var="labelReceivers"/>
<fmt:message key="formsOnline.validation.inter" var="labelValidationInter"/>
<fmt:message key="GML.mandatory" var="labelMandatory"/>
<fmt:message key="formsOnline.form.exchange.mail.help" var="sendEmailHelpMessage"/>
<fmt:message key="formsOnline.form.exchange.immediateDeletion.help" var="immediateDeletionHelpMessage"/>

<fmt:message bundle="${icons}" var="helpIcon" key="formsOnline.icons.help"/>
<c:url var="helpIcon" value="${helpIcon}"/>

<c:set var="id_ListSenders" value="<%=FormsOnlineSessionController.USER_PANEL_SENDERS_PREFIX%>"/>
<c:set var="id_ListReceivers" value="<%=FormsOnlineSessionController.USER_PANEL_RECEIVERS_PREFIX%>"/>
<c:set var="id_ListIntermediateReveivers" value="<%=FormsOnlineSessionController.USER_PANEL_INTERMEDIATE_RECEIVERS_PREFIX%>"/>

<view:sp-page>
<view:sp-head-part withFieldsetStyle="true" withCheckFormScript="true">
<view:includePlugin name="popup"/>
<script type="text/javascript">
function valider() {
  const description = stripInitialWhitespace(document.creationForm.description.value);
  const templateSelectedIndex = document.creationForm.template.selectedIndex;
  const title = stripInitialWhitespace(document.creationForm.title.value);

  if (isWhitespace(title)) {
    SilverpeasError.add("'<fmt:message key="GML.title"/>' <fmt:message key="GML.MustBeFilled"/>");
  }

  if (templateSelectedIndex < 1) {
    SilverpeasError.add("'<fmt:message key="formsOnline.Template"/>' <fmt:message key="GML.MustBeFilled"/>");
  }

  if (isWhitespace(description)) {
    SilverpeasError.add("'<fmt:message key="GML.description"/>' <fmt:message key="GML.MustBeFilled"/>");
  }

  const email = document.getElementById('sendEmail').value;
  if (StringUtil.isDefined(email) && !checkemail(email)) {
    SilverpeasError.add("'<fmt:message key="formsOnline.sendEmail"/>' <fmt:message key="GML.MustContainsEmail"/>");
  }

  const directDeletionChecked = document.getElementById('directDeletion').checked;
  if (directDeletionChecked) {
    if (StringUtil.isNotDefined(email)) {
      SilverpeasError.add("'<fmt:message key="formsOnline.sendEmail"/>' <fmt:message key="GML.MustBeFilled"/>");
    }
  } else if (${form.published}) {
    if (sp.element.querySelectorAll('input[id^="${id_ListReceivers}"').filter(function(input) {
      return !!input.value
    }).length === 0) {
      SilverpeasError.add("<fmt:message key="formsOnline.form.error.finalValidatorMissing"/>");
    }
  }

  if (!SilverpeasError.show()) {
    document.creationForm.submit();
  }
}

function viewForm() {
  const xml = $("#templates").val();
  SP_openWindow("Preview?Form="+xml);
}

function showHideOnImmediateRequestDeletionAfterExchangeCheckboxChange(checkBoxTarget) {
  if (checkBoxTarget.checked) {
    $("#bossValidationForm").hide();
    $("#validator-containers").hide();
  } else {
    $("#bossValidationForm").show();
    $("#validator-containers").show();
  }
}

function showHidePreviewButton() {
  const xml = $("#templates").val();
  if (xml.length > 0) {
    $("#view-form").show();
  } else {
    $("#view-form").hide();
  }
}

function notifySenders() {
  const users = $("#listSenders-userIds").val();
  const groups = $("#listSenders-groupIds").val();
  notify(users, groups);
}

function notifyReceivers() {
  let users = $("#listIntermediateReceivers-userIds").val();
  let groups = $("#listIntermediateReceivers-groupIds").val();
  users += ","+$("#listReceivers-userIds").val();
  groups += ","+$("#listReceivers-groupIds").val();
  notify(users, groups);
}

function notifyAll() {
  let users = $("#listIntermediateReceivers-userIds").val();
  let groups = $("#listIntermediateReceivers-groupIds").val();
  users += ","+$("#listReceivers-userIds").val();
  groups += ","+$("#listReceivers-groupIds").val();
  users += ","+$("#listSenders-userIds").val();
  groups += ","+$("#listSenders-groupIds").val();
  notify(users, groups);
}

function notify(users, groups) {
  sp.messager.open("${form.instanceId}", {
    contributionId: ${form.id},
    recipientUsers: users,
    recipientGroups: groups,
    recipientEdition: false
  });
}

$(document).ready(function() {
  showHidePreviewButton();
  $("#templates").change(function() {
    showHidePreviewButton();
  });
  const directDeletionCheckBox = document.querySelector('#directDeletion');
  showHideOnImmediateRequestDeletionAfterExchangeCheckboxChange(directDeletionCheckBox)
  directDeletionCheckBox.addEventListener('change', () =>
      showHideOnImmediateRequestDeletionAfterExchangeCheckboxChange(directDeletionCheckBox));
  TipManager.simpleHelp(".sendEmail-help-button", "${silfn:escapeJs(sendEmailHelpMessage)}");
  TipManager.simpleHelp(".directDeletion-help-button", "${silfn:escapeJs(immediateDeletionHelpMessage)}");
});
</script>

</view:sp-head-part>
<view:sp-body-part>
<view:operationPane>
  <fmt:message var="labelNotifySenders" key="formsOnline.form.action.notify.senders"/>
  <fmt:message var="labelNotifyReceivers" key="formsOnline.form.action.notify.receivers"/>
  <fmt:message var="labelNotifyAll" key="formsOnline.form.action.notify.all"/>
  <c:if test="${not isCreationCase and form.published}">
    <view:operation action="javascript:notifySenders()" altText="${labelNotifySenders}"/>
    <view:operation action="javascript:notifyReceivers()" altText="${labelNotifyReceivers}"/>
    <view:operation action="javascript:notifyAll()" altText="${labelNotifyAll}"/>
  </c:if>
</view:operationPane>

<view:window>

	<form name="creationForm" action="SaveForm" method="post">

    <fieldset id="informationForm" class="skinFieldset">
      <legend><fmt:message key="GML.bloc.information.principals"/></legend>
      <div class="fields">
        <div class="field" id="titleForm">
          <label for="title" class="txtlibform"><fmt:message key="GML.title"/></label>
          <div class="champs">
            <input type="text" id="title" name="title" size="60" maxlength="200" value="${form.title}"/>
            &nbsp;<img width="5" height="5" alt="${labelMandatory}" src="${iconMandatory}" /> </div>
        </div>
        <div class="field" id="templateForm">
          <label for="templates" class="txtlibform"><fmt:message key="formsOnline.Template"/></label>
          <div class="champs">
            <c:choose>
              <c:when test="${not isCreationCase}">
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
            <textarea rows="4" cols="65" name="description" id="description" maxlength="2000">${form.description}</textarea>
            &nbsp;<img width="5" height="5" alt="${labelMandatory}" src="${iconMandatory}" />
          </div>
        </div>
        <div class="field" id="bossValidationForm" style="display: none">
          <label for="bossValidation" class="txtlibform"><fmt:message key="formsOnline.validation.boss"/></label>
          <div class="champs">
            <input type="checkbox" name="bossValidation" id="bossValidation" value="true" ${hierarchicalValidation}/>
          </div>
        </div>
        <div class="field" id="sendEmailForm">
          <label for="sendEmail" class="txtlibform"><fmt:message key="formsOnline.sendEmail"/></label>
          <div class="champs">
            <input type="text" id="sendEmail" name="sendEmail" size="60" maxlength="200" value="${requestExchangeReceiver}"/>
            <span><img class="sendEmail-help-button" src="${helpIcon}" alt=""/></span>
          </div>
        </div>
        <div class="field" id="directDeletionForm">
          <label for="directDeletion" class="txtlibform"><fmt:message key="formsOnline.directDeletion"/></label>
          <div class="champs">
            <input type="checkbox" name="directDeletion" id="directDeletion" value="true" ${deleteAfterRequestExchange}/>
            <span><img class="directDeletion-help-button" src="${helpIcon}" alt=""/></span>
          </div>
        </div>

      </div>
    </fieldset>

    <viewTags:displayListOfUsersAndGroups users="${m_listUserSenders}" groups="${m_listGroupSenders}" label="${labelSenders}" id="${id_ListSenders}" updateCallback="ModifySenders"/>
    <div class="table" id="validator-containers" style="display: none">
      <div class="cell">
        <viewTags:displayListOfUsersAndGroups users="${m_listUserIntermediateReceivers}" groups="${m_listGroupIntermediateReceivers}" label="${labelValidationInter}" id="${id_ListIntermediateReveivers}" updateCallback="ModifyIntermediateReceivers"/>
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

</view:sp-body-part>
</view:sp-page>