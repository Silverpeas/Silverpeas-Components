<%--
  Copyright (C) 2000 - 2017 Silverpeas
  
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

<%@ attribute name="greatestUserRole" required="true"
              type="org.silverpeas.core.admin.user.model.SilverpeasRole"
              description="The greatest role the user has" %>
<%@ attribute name="componentInstanceId" required="true"
              type="java.lang.String"
              description="The component instance id associated to the drag and drop" %>

<c:set var="kmeliaCtrl" value="${requestScope.kmelia}"/>
<jsp:useBean id="kmeliaCtrl" type="org.silverpeas.components.kmelia.control.KmeliaSessionController"/>

<view:setConstant var="writerRole" constant="org.silverpeas.core.admin.user.model.SilverpeasRole.writer"/>
<jsp:useBean id="writerRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>
<c:if test="${greatestUserRole.isGreaterThanOrEquals(writerRole)}">

  <c:set var="targetValidationEnabled" value="${kmeliaCtrl.targetValidationEnable || kmeliaCtrl.targetMultiValidationEnable}"/>
  <c:set var="validationMandatory" value="${targetValidationEnabled && greatestUserRole.equals(writerRole)}"/>
  <c:set var="draftEnabled" value="${kmeliaCtrl.draftEnabled}"/>
  <fmt:message var="ValidatorLabel" key="kmelia.Valideur"/>

    <div id="pasteDialog" class="form-container" style="display: none;">
      <div id="pasteCaption"><fmt:message key="kmelia.paste.popin.caption"/></div>
      <c:if test="${draftEnabled}">
        <br/>
        <span class="label"><fmt:message key="PubState"/></span>
        <div>
          <input value="Draft" type="radio" name="PastePublicationState" id="PasteDraftState" checked="checked"/>
          <label for="draftState"><fmt:message key="PubStateDraft"/></label><br/>
          <input value="NotDraft" type="radio" name="PastePublicationState" id="PastePublishedState"/>
          <label for="publishedState"><fmt:message key="PubStatePublished"/></label>
        </div>
      </c:if>
      <c:if test="${validationMandatory}">
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
          <a href="#" onclick="javascript:SP_openWindow('SelectValidator','selectUser',800,600,'');">
            <img src="${validatorIcon}" width="15" height="15" border="0" alt="${selectLabel}" title="${selectLabel}" align="absmiddle"/>
          </a>
        </div>
      </c:if>
    </div>

    <script type="text/JavaScript">
      function checkOnPaste(folderId) {
        if (${draftEnabled} || ${validationMandatory}) {
          var url = "<c:url value="/KmeliaAJAXServlet?Action=IsClipboardContainsCopiedItems&ComponentId=${componentInstanceId}"/>";
          silverpeasAjax(url).then(function(request) {
            var result = request.responseText;
            if (result === "true") {
              // some items are copied
              displayPasteDialog(folderId);
            } else {
              sendPasteAction(folderId, "");
            }
          });
        } else {
          sendPasteAction(folderId, "");
        }
      }

      function displayPasteDialog(folderId){
        jQuery('#pasteDialog').popup('validation', {
          title : '<fmt:message key="kmelia.paste.popin.title" />',
          buttonDisplayed : true,
          isMaxWidth : true,
          callback : function() {
            var extraParams = "";
            <c:if test="${draftEnabled}">
              var state = jQuery('input[name=PastePublicationState]:checked', this).val();
              extraParams += "&State=" + state;
            </c:if>
            <c:if test="${validationMandatory}">
              var userId = jQuery('#ValideurId', this).val();
              if (StringUtil.isNotDefined(userId)) {
                SilverpeasError.add("<fmt:message key="GML.thefield"/> <b>${ValidatorLabel}</b> <fmt:message key="GML.MustBeFilled"/>");
              } else {
                extraParams += "&ValidatorIds=" + userId;
              }
            </c:if>

            if (!SilverpeasError.show()) {
              sendPasteAction(folderId, extraParams);
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

      function sendPasteAction(folderId, extraParams) {
        var url = "<c:url value="/KmeliaAJAXServlet?Action=Paste&ComponentId=${componentInstanceId}"/>";
        url += "&Id="+folderId;
        if (StringUtil.isDefined(extraParams)) {
          url += extraParams;
        }
        $.progressMessage();
        silverpeasAjax(url).then(function(request) {
          $.closeProgressMessage();
          var result = request.responseText;
          if (result === "ok") {
            pasteDone(folderId);
          } else {
            SilverpeasError.add(result).show();
          }
        });
      }
    </script>
</c:if>