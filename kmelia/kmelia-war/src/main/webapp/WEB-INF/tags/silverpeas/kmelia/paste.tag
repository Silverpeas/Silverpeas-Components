<%--
  Copyright (C) 2000 - 2018 Silverpeas
  
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.
  
  As a special exception to the terms and conditions of version 3.0 of
  the GPL, you may redistribute this Program in connection with Free/Libre
  Open Source Software ("FLOSS") applications as described in Silverpeas's
  FLOSS exception. You should have recieved a copy of the text describing
  the FLOSS exception, and it is also available here:
  "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero General Public License for more details.
  
  You should have received a copy of the GNU Affero General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  --%>

<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<%@ attribute name="highestUserRole" required="true"
              type="org.silverpeas.core.admin.user.model.SilverpeasRole"
              description="The highest role the user has" %>
<%@ attribute name="componentInstanceId" required="true"
              type="java.lang.String"
              description="The component instance id associated to the drag and drop" %>

<c:set var="kmeliaCtrl" value="${requestScope.kmelia}"/>
<jsp:useBean id="kmeliaCtrl" type="org.silverpeas.components.kmelia.control.KmeliaSessionController"/>

<view:setConstant var="writerRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.writer"/>
<jsp:useBean id="writerRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>

<view:setConstant constant="org.silverpeas.components.kmelia.control.KmeliaSessionController.CLIPBOARD_STATE.IS_EMPTY" var="IS_EMPTY_CLIPBOARD_STATE"/>
<view:setConstant constant="org.silverpeas.components.kmelia.control.KmeliaSessionController.CLIPBOARD_STATE.HAS_CUTS" var="HAS_CUTS_CLIPBOARD_STATE"/>
<view:setConstant constant="org.silverpeas.components.kmelia.control.KmeliaSessionController.CLIPBOARD_STATE.HAS_COPIES_AND_CUTS" var="HAS_COPIES_AND_CUTS_CLIPBOARD_STATE"/>

<c:set var="targetValidationEnabled" value="${kmeliaCtrl.targetValidationEnable || kmeliaCtrl.targetMultiValidationEnable}"/>
<c:set var="draftEnabled" value="${kmeliaCtrl.draftEnabled}"/>
<fmt:message var="ValidatorLabel" key="kmelia.Valideur"/>
<fmt:message var="copyStateHelpLabel" key="kmelia.copy.state.help"/>

<div id="pasteDialog" class="form-container" style="display: none;">
  <div id="pasteCaption"><fmt:message key="kmelia.paste.popin.caption"/></div>
  <div id="paste-draft">
    <br/>
    <span class="label"><fmt:message key="PubState"/></span>
    <div id="paste-copy-state-help" class="Titre">
      (${silfn:escapeHtml(copyStateHelpLabel)})
    </div>
    <div>
      <input value="Draft" type="radio" name="PastePublicationState" id="PasteDraftState" checked="checked"/>
      <label for="PasteDraftState"><fmt:message key="PubStateDraft"/></label><br/>
      <input value="NotDraft" type="radio" name="PastePublicationState" id="PastePublishedState"/>
      <label for="PastePublishedState"><fmt:message key="PubStatePublished"/></label>
    </div>
  </div>
  <div id="paste-validators">
    <c:set var="oneValidator" value="${kmeliaCtrl.targetValidationEnable}"/>
    <c:url var="validatorIcon" value="/util/icons/user.gif"/>
    <br/>
    <span class="label">${ValidatorLabel}</span>
    <div>
      <c:choose>
        <c:when test="${oneValidator}">
          <input type="text" name="Valideur" id="Valideur" value="" size="50" readonly="readonly"/>
        </c:when>
        <c:otherwise>
          <c:url var="validatorIcon" value="/util/icons/groupe.gif"/>
          <textarea name="Valideur" id="Valideur" rows="3" cols="40" readonly="readonly"></textarea>
        </c:otherwise>
      </c:choose>
      <input type="hidden" name="ValideurId" id="ValideurId" value=""/>
      <fmt:message var="selectLabel" key="kmelia.SelectValidator"/>
      <a href="#">
        <img src="${validatorIcon}" width="15" height="15" border="0" alt="${selectLabel}" title="${selectLabel}" align="absmiddle"/>
      </a>
    </div>
  </div>
</div>

<script type="text/javascript">
  function checkOnPaste(topicId) {
    var params = {
      "dnd" : false,
      "targetId" : topicId
    };
    kmeliaWebService.getClipboardState().then(function(clipboardState) {
      if (clipboardState !== '${IS_EMPTY_CLIPBOARD_STATE}') {
        params.clipboardState = clipboardState;
        displayPasteDialog(params, function() {
          sendPasteAction(topicId);
        });
      }
    });
  }

  function displayPasteDialog(params, callbackWhenNoOptionToAskToUser){
    var stateMustBeSet = false;
    var stateCopyHelpMustBeSet = false;
    if (!params.dnd) {
      if (!params.clipboardState || params.clipboardState === '${IS_EMPTY_CLIPBOARD_STATE}') {
        sp.log.warning("calling displayPasteDialog method whereas it does not exist element to paste");
        return;
      }
      stateMustBeSet = !!${draftEnabled} && params.clipboardState !== '${HAS_CUTS_CLIPBOARD_STATE}';
      stateCopyHelpMustBeSet = stateMustBeSet && params.clipboardState === '${HAS_COPIES_AND_CUTS_CLIPBOARD_STATE}';
    }
    var topicId = params.targetId;
    var currentUserProfile = kmeliaWebService.getUserProfileSynchronously(topicId);
    var validatorsMustBeSet = !!${targetValidationEnabled} && currentUserProfile === "writer" && topicId !== '1';
    if (!stateMustBeSet && !validatorsMustBeSet) {
      if (typeof callbackWhenNoOptionToAskToUser === 'function') {
        callbackWhenNoOptionToAskToUser();
      }
      return;
    }
    if (validatorsMustBeSet) {
      $("#paste-validators").show();
      $("#paste-validators a").click(function() {
        SP_openWindow('SelectValidator?FolderId='+topicId,'selectUser',800,600,'');
      });
    } else {
      $("#paste-validators").hide();
    }
    if (stateMustBeSet) {
      $("#paste-draft").show();
    } else {
      $("#paste-draft").hide();
    }
    if (stateCopyHelpMustBeSet) {
      $("#paste-copy-state-help").show();
    } else {
      $("#paste-copy-state-help").hide();
    }

    jQuery('#pasteDialog').popup('validation', {
      title : '<fmt:message key="kmelia.paste.popin.title" />',
      buttonDisplayed : true,
      isMaxWidth : true,
      callback : function() {
        var extraParams = {};
        if (stateMustBeSet) {
          extraParams.State = jQuery('input[name=PastePublicationState]:checked', this).val();
        }
        if (validatorsMustBeSet) {
          var userId = jQuery('#ValideurId', this).val();
          if (StringUtil.isNotDefined(userId)) {
            SilverpeasError.add("<fmt:message key="GML.thefield"/> <b>${ValidatorLabel}</b> <fmt:message key="GML.MustBeFilled"/>");
          } else {
            extraParams.ValidatorIds = userId;
          }
        }

        if (!SilverpeasError.show()) {
          if (params.dnd) {
            sendMovePublication(params, extraParams);
          } else {
            sendPasteAction(topicId, extraParams);
          }
          return true;
        }
        return false;
      },
      callbackOnClose : function() {
        jQuery('#ValideurId', this).val("");
        jQuery('#Valideur', this).val("");
      }
    });
  }

  function sendPasteAction(topicId, extraParams) {
    $.progressMessage();
    kmeliaWebService.pastePublications(topicId, extraParams).then(function(result) {
      $.closeProgressMessage();
      if (result === "ok") {
        pasteDone(topicId);
      } else {
        SilverpeasError.add(result).show();
      }
    });
  }
</script>