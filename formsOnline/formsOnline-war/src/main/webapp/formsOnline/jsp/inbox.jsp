<%--

    Copyright (C) 2000 - 2022 Silverpeas

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
<%@ page import="org.silverpeas.components.formsonline.control.RequestUIEntity" %>
<%@ page import="org.silverpeas.components.formsonline.model.RequestsByStatus" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/formsOnline" prefix="formsOnline" %>

<c:set var="lang" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<c:set var="currentUser" value="${sessionScope['SilverSessionController'].currentUserDetail}"/>
<c:set var="controller" value="${requestScope.FormsOnline}"/>
<jsp:useBean id="controller" type="org.silverpeas.components.formsonline.control.FormsOnlineSessionController"/>

<fmt:setLocale value="${lang}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="requests" value="${requestScope['Requests']}"/>
<jsp:useBean id="requests" type="org.silverpeas.components.formsonline.model.RequestsByStatus"/>

<c:set var="forms" value="${requestScope['Forms']}"/>
<c:set var="currentForm" value="${requestScope['CurrentForm']}"/>

<fmt:message var="statusReadLabel" key="formsOnline.stateRead"/>
<fmt:message var="statusValidatedLabel" key="formsOnline.stateValidated"/>
<fmt:message var="statusDeniedLabel" key="formsOnline.stateRefused"/>
<fmt:message var="statusArchivedLabel" key="formsOnline.stateArchived"/>
<fmt:message var="statusUnreadLabel" key="formsOnline.stateUnread"/>
<fmt:message var="statusCanceledLabel" key="formsOnline.stateCanceled"/>

<fmt:message var="colStatus" key="GML.status"/>
<fmt:message var="colDate" key="formsOnline.sendDate"/>
<fmt:message var="colSender" key="formsOnline.sender"/>
<fmt:message var="colForm" key="formsOnline.Form"/>
<fmt:message var="colValidator" key="formsOnline.request.process.user"/>
<fmt:message var="colProcessDate" key="GML.date"/>
<fmt:message var="colVH" key="formsOnline.requests.array.col.vh.label"/>
<fmt:message var="colVI" key="formsOnline.requests.array.col.vi.label"/>
<fmt:message var="colVF" key="formsOnline.requests.array.col.vf.label"/>

<fmt:message var="labelExport" key="GML.export.result"/>

<fmt:message var="deletionConfirmMessage" key="formsOnline.requests.action.delete.confirm"/>
<fmt:message var="archiveConfirmMessage" key="formsOnline.requests.action.archive.confirm"/>

<view:setConstant var="STATE_UNREAD" constant="org.silverpeas.components.formsonline.model.FormInstance.STATE_UNREAD"/>
<view:setConstant var="STATE_READ" constant="org.silverpeas.components.formsonline.model.FormInstance.STATE_READ"/>
<view:setConstant var="STATE_VALIDATED" constant="org.silverpeas.components.formsonline.model.FormInstance.STATE_VALIDATED"/>
<view:setConstant var="STATE_REFUSED" constant="org.silverpeas.components.formsonline.model.FormInstance.STATE_REFUSED"/>
<view:setConstant var="STATE_ARCHIVED" constant="org.silverpeas.components.formsonline.model.FormInstance.STATE_ARCHIVED"/>
<view:setConstant var="STATE_CANCELED" constant="org.silverpeas.components.formsonline.model.FormInstance.STATE_CANCELED"/>

<jsp:useBean id="possibleStatusFilters" class="java.util.LinkedHashMap"/>
<c:set target="${possibleStatusFilters}" property="${''}" value="${''}"/>
<c:set target="${possibleStatusFilters}" property="${statusUnreadLabel}" value="${STATE_UNREAD}"/>
<c:set target="${possibleStatusFilters}" property="${statusReadLabel}" value="${STATE_READ}"/>
<c:forEach var="possibleValidation" items="<%=RequestsByStatus.possibleRequestValidationsFrom(requests.getAll())%>">
  <fmt:message var="tmpLabel" key="formsOnline.statePending${silfn:capitalize(possibleValidation.name().toLowerCase())}Validation"/>
  <c:set target="${possibleStatusFilters}" property="${tmpLabel}" value="${possibleValidation.name()}"/>
</c:forEach>
<c:set target="${possibleStatusFilters}" property="${statusValidatedLabel}" value="${STATE_VALIDATED}"/>
<c:set target="${possibleStatusFilters}" property="${statusDeniedLabel}" value="${STATE_REFUSED}"/>
<c:set target="${possibleStatusFilters}" property="${statusArchivedLabel}" value="${STATE_ARCHIVED}"/>
<c:set target="${possibleStatusFilters}" property="${statusCanceledLabel}" value="${STATE_CANCELED}"/>
<c:set var="currentStateFilter" value="${controller.currentStateFilter}"/>
<c:set var="currentValidationTypeFilter" value="${controller.currentValidationTypeFilter}"/>

<view:sp-page>
<view:sp-head-part>
  <style type="text/css">
    #link-export {
      display: none;
    }
  </style>
  <script type="text/javascript">

    var arrayPaneAjaxControl;
    var checkboxMonitor = sp.selection.newCheckboxMonitor('#list input[name=selection]');

    function removeRequests() {
      jQuery.popup.confirm('${silfn:escapeJs(deletionConfirmMessage)}', function() {
        var ajaxRequest = sp.ajaxRequest("DeleteRequests").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function archiveRequests() {
      jQuery.popup.confirm('${silfn:escapeJs(archiveConfirmMessage)}', function() {
        var ajaxRequest = sp.ajaxRequest("ArchiveRequests").byPostMethod();
        checkboxMonitor.prepareAjaxRequest(ajaxRequest);
        ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
      });
    }

    function filterRequests() {
      var formId = $("#selectedForm").val();
      var state = $("#selectedState").val();
      var ajaxRequest = sp.ajaxRequest("FilterRequests").byPostMethod();
      ajaxRequest.withParam("FormId", formId);
      ajaxRequest.withParam("State", state);
      checkboxMonitor.prepareAjaxRequest(ajaxRequest);
      ajaxRequest.send().then(arrayPaneAjaxControl.refreshFromRequestResponse);
    }

    whenSilverpeasReady(function() {
      $("#selectedForm").change(function() {
        var formId = $(this).val();
        if (formId === "") {
          $("#link-export").hide();
        } else {
          $("#link-export").show();
        }
        filterRequests();
      });

      $("#selectedState").change(function() {
        filterRequests();
      });

      if (${currentForm != null}) {
        $("#link-export").show();
      }
    });
  </script>
</view:sp-head-part>
<view:sp-body-part id="all-requests">
<fmt:message var="browseBarAll" key="formsOnline.requests.all.breadcrumb"/>
<view:browseBar extraInformations="${browseBarAll}"/>
<view:operationPane>
  <fmt:message var="deleteReq" key="formsOnline.removeFormInstance"/>
  <view:operationOfCreation action="javascript:removeRequests()" icon="" altText="${deleteReq}"/>
  <fmt:message var="archiveReq" key="formsOnline.requests.action.archive"/>
  <view:operationOfCreation action="javascript:archiveRequests()" icon="" altText="${archiveReq}"/>
</view:operationPane>
<view:window>
  <view:frame>
    <div id="filter">
      <div id="stateFilter">
        <label for="selectedState">${colStatus}</label>
          <select id="selectedState">
            <c:forEach var="possibleStatusFilter" items="${possibleStatusFilters.entrySet()}">
              <c:set var="asString" value="${''.concat(possibleStatusFilter.value)}"/>
              <c:set var="selected" value="${asString eq ''.concat(currentStateFilter) or asString eq currentValidationTypeFilter.name() ? 'selected' : ''}"/>
              <option value="${possibleStatusFilter.getValue()}" ${selected}>${possibleStatusFilter.key}</option>
            </c:forEach>
        </select>
      </div>
      <div id="formFilter">
        <label for="selectedForm"><fmt:message key="formsOnline.Form"/></label>
        <select id="selectedForm">
          <option></option>
          <c:forEach items="${forms}" var="form">
            <c:choose>
              <c:when test="${form.id == currentForm.id}">
                <option value="${form.id}" selected="selected">${form.name}</option>
              </c:when>
              <c:otherwise>
                <option value="${form.id}">${form.name}</option>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </select>
        <a id="link-export" class="sp_button" href="javascript:void(0)" onclick="sp.preparedDownloadRequest('Export').download()"><fmt:message key="GML.export"/></a>
      </div>
    </div>
    <div id="list">
      <c:set var="requestStatusLabelLambda" value="${r ->
              (r.data.read ? statusReadLabel :
              (r.data.validated ? statusValidatedLabel :
              (r.data.denied ? statusDeniedLabel :
              (r.data.canceled ? statusCanceledLabel :
              (r.data.archived ? statusArchivedLabel : statusUnreadLabel)))))}"/>
      <c:set var="requestItems" value="<%=RequestUIEntity.convertList(requests.getAll(), controller.getSelectedValidatorRequestIds())%>"/>
      <c:set var="useHierarchicalValidation" value="false"/>
      <c:set var="useIntermediateValidation" value="false"/>
      <c:forEach var="requestItem" items="${requestItems}">
        <c:if test="${requestItem.data.form.hierarchicalValidation}">
          <c:set var="useHierarchicalValidation" value="true"/>
        </c:if>
        <c:if test="${requestItem.data.form.intermediateValidation}">
          <c:set var="useIntermediateValidation" value="true"/>
        </c:if>
      </c:forEach>
      <view:arrayPane var="myForms" routingAddress="InBox" numberLinesPerPage="25">
        <view:arrayColumn width="10" sortable="false"/>
        <view:arrayColumn title="${colStatus}" compareOn="${requestStatusLabelLambda}"/>
        <view:arrayColumn title="${colDate}" compareOn="${r -> r.data.creationDate}"/>
        <view:arrayColumn title="${colSender}" compareOn="${r -> r.creator.displayedName}"/>
        <view:arrayColumn title="${colForm}" compareOn="${r -> r.data.form.title}"/>
        <c:if test="${useHierarchicalValidation}">
          <view:arrayColumn title="${colVH}" compareOn="${r -> r.hierarchicalValidation.validator.displayedName}"/>
          <view:arrayColumn title="${colProcessDate}" compareOn="${r -> r.hierarchicalValidation.date}"/>
        </c:if>
        <c:if test="${useIntermediateValidation}">
          <view:arrayColumn title="${colVI}" compareOn="${r -> r.intermediateValidation.validator.displayedName}"/>
          <view:arrayColumn title="${colProcessDate}" compareOn="${r -> r.hierarchicalValidation.date}"/>
        </c:if>
        <view:arrayColumn title="${colVF}" compareOn="${r -> r.validator.displayedName}"/>
        <view:arrayColumn title="${colProcessDate}" compareOn="${r -> r.validationDate}"/>
        <view:arrayLines var="request" items="${requestItems}">
          <view:arrayLine>
            <c:choose>
              <c:when test="${request.data.canBeDeletedBy(currentUser) || request.data.canBeArchivedBy(currentUser)}">
                <view:arrayCellCheckbox name="selection"
                                        checked="${request.selected}"
                                        value="${request.id}"/>
              </c:when>
              <c:otherwise>
                <view:arrayCellText text=""/>
              </c:otherwise>
            </c:choose>
            <view:arrayCellText>
              <c:choose>
                <c:when test="${request.data.archived || request.data.canceled}">
                  ${requestStatusLabelLambda(request)}
                </c:when>
                <c:otherwise>
                  <formsOnline:validationsSchemaImage userRequest="${request.data}"/>
                </c:otherwise>
              </c:choose>
            </view:arrayCellText>
            <view:arrayCellText text="${silfn:formatDateAndHour(request.data.creationDate, lang)}"/>
            <view:arrayCellText text="${request.creator.displayedName}"/>
            <view:arrayCellText><a href="ViewRequest?Id=${request.id}&Origin=InBox">${request.data.form.title}</a></view:arrayCellText>
            <c:if test="${useHierarchicalValidation}">
              <c:set var="validation" value="${request.hierarchicalValidation}"/>
              <view:arrayCellText text="${validation.validator.displayedName}"/>
              <view:arrayCellText text="${silfn:formatDateAndHour(validation.date, lang)}"/>
            </c:if>
            <c:if test="${useIntermediateValidation}">
              <c:set var="validation" value="${request.intermediateValidation}"/>
              <view:arrayCellText text="${validation.validator.displayedName}"/>
              <view:arrayCellText text="${silfn:formatDateAndHour(validation.date, lang)}"/>
            </c:if>
            <view:arrayCellText text="${request.validator.displayedName}"/>
            <view:arrayCellText text="${silfn:formatDateAndHour(request.validationDate, lang)}"/>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          checkboxMonitor.pageChanged();
          arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#list', {
            before : checkboxMonitor.prepareAjaxRequest
          });
        });
      </script>
    </div>
  </view:frame>
</view:window>
</view:sp-body-part>
</view:sp-page>