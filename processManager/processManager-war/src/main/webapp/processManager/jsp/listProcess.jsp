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
<%@ include file="checkProcessManager.jsp" %>
<%@ page import="org.silverpeas.core.contribution.content.form.Form" %>
<%@ page import="java.util.Collections" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/workflowFunctions" prefix="workflowfn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>


<c:set var="componentId" value="${requestScope.browseContext[3]}"/>

<%-- Set resource bundle --%>
<c:set var="userLanguage" value="${requestScope.resources.language}"/>
<jsp:useBean id="userLanguage" type="java.lang.String"/>
<fmt:setLocale value="${userLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<fmt:message var="manageReplacementLabel" key="processManager.replacements.manage"/>
<fmt:message var="manageReassignmentLabel" key="processManager.reassignment.manage"/>
<fmt:message var="createProcessLabel" key="processManager.createProcess"/>
<fmt:message var="userSettingsLabel" key="processManager.userSettings"/>
<fmt:message var="csvExportLabel" key="processManager.csvExport"/>
<fmt:message var="refreshLabel" key="processManager.refresh"/>
<fmt:message var="yourRoleLabel" key="processManager.yourRole"/>
<fmt:message var="labelFilter" key="processManager.filter"/>
<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>
<fmt:message var="idLabel" key="processManager.id"/>
<fmt:message var="statusLabel" key="processManager.status"/>

<fmt:message var="confirmDeleteMessage" key="processManager.confirmDelete"/>

<fmt:message key="processManager.inError" var="tmp" bundle="${icons}"/>
<c:url var="inErrorIconUrl" value="${tmp}"/>
<fmt:message var="inErrorLabel" key="processManager.inError"/>
<fmt:message key="processManager.locked" var="tmp" bundle="${icons}"/>
<c:url var="lockedByAdminIconUrl" value="${tmp}"/>
<fmt:message var="lockedByAdminLabel" key="processManager.lockedByAdmin"/>
<fmt:message key="processManager.timeout" var="tmp" bundle="${icons}"/>
<c:url var="timeoutIconUrl" value="${tmp}"/>
<fmt:message var="timeoutLabel" key="processManager.timeout"/>
<fmt:message key="processManager.small_remove" var="tmp" bundle="${icons}"/>
<c:url var="deleteIconUrl" value="${tmp}"/>
<fmt:message var="deleteLabel" key="processManager.delete"/>


<c:set var="processList" value="${requestScope.processList}"/>
<c:if test="${processList == null}">
  <c:set var="processList" value="<%=Collections.emptyList()%>"/>
</c:if>
<jsp:useBean id="processList" type="java.util.List<org.silverpeas.core.contribution.content.form.DataRecord>"/>

<c:set var="listHeaders" value="${requestScope.listHeaders}"/>
<jsp:useBean id="listHeaders" type="org.silverpeas.core.contribution.content.form.RecordTemplate"/>
<c:set var="headers" value="${listHeaders.fieldTemplates}"/>
<jsp:useBean id="headers" type="org.silverpeas.core.contribution.content.form.FieldTemplate[]"/>
<c:set var="items" value="${requestScope.FolderItems}"/>
<jsp:useBean id="items" type="org.silverpeas.core.workflow.api.model.Item[]"/>

<c:set var="roles" value="${requestScope.roles}"/>
<c:if test="${roles == null}">
  <c:set var="roles" value="<%=new NamedValue[0]%>"/>
</c:if>
<jsp:useBean id="roles" type="org.silverpeas.processmanager.NamedValue[]"/>

<c:set var="context" value="${requestScope.context}"/>
<jsp:useBean id="context" type="org.silverpeas.core.contribution.content.form.PagesContext"/>

<c:set var="isProcessIdVisible" value="${silfn:booleanValue(requestScope.isProcessIdVisible)}"/>
<c:set var="canCreate" value="${silfn:booleanValue(requestScope.canCreate)}"/>
<c:set var="hasUserSettings" value="${silfn:booleanValue(requestScope.hasUserSettings)}"/>
<c:set var="isCSVExportEnabled" value="${silfn:booleanValue(requestScope.isCSVExportEnabled)}"/>
<c:set var="currentRole" value="${requestScope.currentRole}"/>
<c:set var="currentReplacement" value="${requestScope.currentReplacement}"/>
<c:set var="isCurrentRoleSupervisor" value="${'supervisor' eq fn:toLowerCase(currentRole)}"/>
<c:set var="collapse" value="${silfn:booleanValue(empty requestScope.collapse ? 'true' : requestScope.collapse)}"/>
<c:set var="currentAndNextReplacementsAsIncumbent" value="${requestScope.CurrentAndNextReplacementsAsIncumbent}"/>

<fmt:message key="processManager.boxDown" var="iconBoxDown" bundle="${icons}"/>
<c:url var="iconBoxDown" value="${iconBoxDown}"/>
<fmt:message key="processManager.boxUp" var="iconBoxUp" bundle="${icons}"/>
<c:url var="iconBoxUp" value="${iconBoxUp}"/>

<%
  Form form = (Form) request.getAttribute("form");
  DataRecord data = (DataRecord) request.getAttribute("data");
%>

<view:sp-page angularJsAppName="silverpeas.processManager">
<view:sp-head-part>
  <view:includePlugin name="toggle"/>
  <% form.displayScripts(out, context); %>
  <script type="text/javascript">

    window.processListMonitor = new function() {
      applyEventDispatchingBehaviorOn(this);
    };

    let filterDisplayed = ${collapse};
    function toggleFilter(){
      if (filterDisplayed) {
        $("#filterForm").hide();
        $("#imgToggle").attr("src", "${iconBoxDown}");
      } else {
        displayFilter();
      }
      filterDisplayed = !filterDisplayed;
    }

    function displayFilter() {
      $("#filterForm").show();
      $("#imgToggle").attr("src", "${iconBoxUp}");
    }

    function setFilter() {
      ifCorrectFormExecute(function() {
        spProgressMessage.show();
        document['${context.formName}'].submit();
      });
    }

    function resetFilter() {
      document['${context.formName}'].reset();
    }

    function refreshList() {
      spProgressMessage.show();
      sp.navRequest('listProcess').go();
    }

    function removeProcessInstance(id) {
      $('#removeProcessInstanceConfirmation').popup('confirmation', {
        //title : "Title of the popup",
        callback : function() {
          spProgressMessage.show();
          window.location.href = "adminRemoveProcess?processId=" + id;
          return true;
        }
      });
    }

    $(function() {
      if (filterDisplayed) {
        displayFilter();
      }
    });
  </script>
</view:sp-head-part>
<view:sp-body-part cssClass="yui-skin-sam processManager-main currentProfile_${currentRole} page_processes">
<view:operationPane>
  <c:if test="${currentReplacement == null}">
    <fmt:message key="processManager.replacements.manage" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operation action="manageReplacements" altText="${manageReplacementLabel}" icon="${opIcon}"/>
  </c:if>
  <c:if test="${isCurrentRoleSupervisor}">
    <fmt:message key="processManager.reassignment.manage" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operation action="javascript:app.manager.openReassignment()" altText="${manageReassignmentLabel}" icon="${opIcon}"/>
  </c:if>
  <c:if test="${canCreate}">
    <fmt:message key="processManager.add" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operationOfCreation action="createProcess"
                              altText="${createProcessLabel}" icon="${opIcon}"/>
  </c:if>
  <c:if test="${hasUserSettings}">
    <fmt:message key="processManager.userSettings" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operation action="editUserSettings" altText="${userSettingsLabel}" icon="${opIcon}"/>
  </c:if>
  <c:if test="${isCSVExportEnabled}">
    <fmt:message key="processManager.csvExport" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operationSeparator/>
    <view:operation action="javascript:sp.preparedDownloadRequest('exportCSV').download()" altText="${csvExportLabel}" icon="${opIcon}"/>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
    <c:if test="${isCurrentRoleSupervisor}">
      <view:includePlugin name="listOfUsersAndGroups"/>
      <view:script src="/processManager/jsp/javaScript/services/workflow.service.js"/>
      <view:script src="/processManager/jsp/javaScript/vuejs/workflow.common.js"/>
      <view:script src="/processManager/jsp/javaScript/vuejs/reassignment.js"/>
      <c:set var="jsUserRoles" value="${requestScope.jsUserRoles}"/>
      <c:set var="jsComponentInstanceRoles" value="${requestScope.jsComponentInstanceRoles}"/>
      <div id="reassignment-module">
        <workflow-reassignment-management
            v-on:api="manager = $event"></workflow-reassignment-management>
      </div>
      <script type="text/javascript">
        const componentInstanceRoles = ${jsComponentInstanceRoles};
        const userRoles = ${jsUserRoles};
        window.handledRoles = {};
        for (let roleName in componentInstanceRoles) {
          if ((${isCurrentRoleSupervisor} || userRoles[roleName]) && roleName !== 'supervisor') {
            window.handledRoles[roleName] = componentInstanceRoles[roleName];
          }
        }
        window.app = SpVue.createApp({
          provide : function() {
            return {
              context : this.context,
              commonService : new WorkflowService('${componentId}', handledRoles)
            }
          },
          data : function() {
            return {
              context : {
                currentUser : currentUser,
                componentInstanceId : '${componentId}'
              },
              manager : undefined
            };
          }
        }).mount('#reassignment-module');
      </script>
    </c:if>
    <c:if test="${currentReplacement == null and fn:length(currentAndNextReplacementsAsIncumbent) > 0}">
      <div class="inlineMessage">
        <c:set var="incumbentMessage"><fmt:message key="processManager.replacements.incumbent.overview">
          <fmt:param value="${fn:length(currentAndNextReplacementsAsIncumbent)}"/>
        </fmt:message></c:set>
        ${incumbentMessage} <a href="manageReplacements">${manageReplacementLabel}</a>
      </div>
    </c:if>
    <c:if test="${fn:length(roles) > 1}">
      <c:set var="currentSelectedRole" value="${currentRole}"/>
      <c:if test="${currentReplacement != null}">
        <c:set var="currentSelectedRole" value="${currentReplacement.id}:${currentSelectedRole}"/>
      </c:if>
      <div id="roles">
        <form name="roleChoice" method="post" action="changeRole">
          <label class="textePetitBold" for="current-role">${yourRoleLabel} :&nbsp;</label>
          <select id="current-role" name="role" onchange="spProgressMessage.show();document.roleChoice.submit()">
            <c:forEach var="role" items="${roles}">
              <option ${role.name eq currentSelectedRole ? 'selected' : ''} value="${role.name}">${role.value}</option>
            </c:forEach>
          </select>
        </form>
      </div>
    </c:if>
    <view:componentInstanceIntro componentId="${componentId}" language="${userLanguage}"/>
    <view:areaOfOperationOfCreation/>
    <form id="filter" name="${context.formName}" method="post" action="filterProcess" enctype="multipart/form-data">
      <div class="bgDegradeGris">
        <div id="filterLabel">
          <p>
            <fmt:message key="${collapse ? 'processManager.boxUp' : 'processManager.boxDown'}" var="opIcon" bundle="${icons}"/>
            <c:url var="opIcon" value="${opIcon}"/>
            <a href="javascript:toggleFilter()"><img id="imgToggle" border="0" src="${opIcon}" alt=""/></a>
            <span class="txtNav">${labelFilter}</span>
          </p>

          <div id="refresh">
            <fmt:message key="processManager.refresh" var="opIcon" bundle="${icons}"/>
            <c:url var="opIcon" value="${opIcon}"/>
            <a href="javascript:refreshList()" title="${refreshLabel}"><img border="0" src="${opIcon}" alt="${refreshLabel}"/></a>
          </div>
        </div>
        <div id="filterForm" style="display: none">
          <% form.display(out, context, data); %>
          <view:buttonPane>
            <view:button label="${validateLabel}" action="javascript:setFilter()"/>
            <view:button label="${cancelLabel}" action="clearFilter"/>
          </view:buttonPane>
        </div>
      </div>
    </form>
    <div id="process-list">
      <c:set var="processCompareOnStatus" value="${p ->
                  (p.inError ? 0 :
                  (p.lockedByAdmin ? 1 :
                  (p.inTimeout ? 2 : 3)))}"/>
      <view:arrayPane var="wf-process-list-${componentId}" routingAddress="listSomeProcess" numberLinesPerPage="20">
        <view:arrayColumn title="${statusLabel}" compareOn="${p -> processCompareOnStatus(p)}"/>
        <c:forEach var="_header" items="${headers}" varStatus="headerStatus">
          <view:arrayColumn title="${_header.getLabel(userLanguage)}"
                            compareOn="${(p,i) -> (
                              index = isProcessIdVisible ? i - 2 : i - 1;
                              workflowfn:getFieldComparable(items, headers[index], p.getField(index), userLanguage))}"/>
        </c:forEach>
        <c:if test="${isCurrentRoleSupervisor}">
          <view:arrayColumn sortable="false"/>
        </c:if>
        <view:arrayLines var="process" items="${processList}">
          <c:set var="viewProcessUrl" value="viewProcess?processId=${process.id}"/>
          <c:set var="field" value="${process.getField(0)}"/>
          <jsp:useBean id="field" type="org.silverpeas.core.contribution.content.form.Field"/>
          <c:set var="processTitle" value="${field.getValue(userLanguage)}"/>
          <c:set var="processTitleLink"><a href="${viewProcessUrl}">${processTitle}</a></c:set>
          <jsp:useBean id="process" type="org.silverpeas.core.workflow.engine.datarecord.ProcessInstanceRowRecord"/>
          <view:arrayLine>
            <c:choose>

              <%-- ERROR CASE --%>
              <c:when test="${process.inError}">
                <view:arrayCellText><img class="icon" src="${inErrorIconUrl}" alt="${inErrorLabel}" title="${inErrorLabel}"/></view:arrayCellText>
                <c:choose>
                  <c:when test="${isCurrentRoleSupervisor}">
                    <view:arrayCellText text="${processTitleLink}"/>
                  </c:when>
                  <c:otherwise>
                    <view:arrayCellText text="${processTitle}"/>
                  </c:otherwise>
                </c:choose>
              </c:when>

              <%-- LOCKED BY ADMIN CASE --%>
              <c:when test="${process.lockedByAdmin}">
                <view:arrayCellText><img class="icon" src="${lockedByAdminIconUrl}" alt="${lockedByAdminLabel}" title="${lockedByAdminLabel}"/></view:arrayCellText>
                <c:choose>
                  <c:when test="${isCurrentRoleSupervisor}">
                    <view:arrayCellText text="${processTitleLink}"/>
                  </c:when>
                  <c:otherwise>
                    <view:arrayCellText text="${processTitle}"/>
                  </c:otherwise>
                </c:choose>
              </c:when>

              <%-- TIMEOUT CASE --%>
              <c:when test="${process.inTimeout}">
                <view:arrayCellText><img class="icon" src="${timeoutIconUrl}" alt="${timeoutLabel}" title="${timeoutLabel}"/></view:arrayCellText>
                <view:arrayCellText text="${processTitleLink}"/>
              </c:when>

              <%-- DEFAULT CASE --%>
              <c:otherwise>
                <view:arrayCellText/>
                <view:arrayCellText text="${processTitleLink}"/>
              </c:otherwise>
            </c:choose>

            <%-- DYNAMIC COLUMNS--%>
            <c:forEach var="_header" items="${headers}" varStatus="headerStatus" begin="1">
              <view:arrayCellText text="${workflowfn:formatFieldValue(items, _header, process.getField(headerStatus.index), userLanguage)}"/>
            </c:forEach>

            <%-- LAST COLUMN, if supervisor--%>
            <c:if test="${isCurrentRoleSupervisor}">
              <view:arrayCellText>
                <a href="javascript:removeProcessInstance(${process.id})" title="${deleteLabel}">
                  <img class="icon" src="${deleteIconUrl}" alt="${deleteLabel}" title="${deleteLabel}"/>
                </a>
              </view:arrayCellText>
            </c:if>
          </view:arrayLine>
        </view:arrayLines>
      </view:arrayPane>
      <script type="text/javascript">
        whenSilverpeasReady(function() {
          sp.arrayPane.ajaxControls('#process-list', {
            before : function(ajaxConfig) {
              spProgressMessage.show();
            }
          });
          window.processListMonitor.dispatchEvent('load');
        });
      </script>
    </div>
  </view:frame>
</view:window>
<div id="removeProcessInstanceConfirmation" style="display: none">
  ${confirmDeleteMessage}
</div>
<view:progressMessage/>
</view:sp-body-part>
</view:sp-page>