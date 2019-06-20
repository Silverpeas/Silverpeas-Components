<%--

    Copyright (C) 2000 - 2019 Silverpeas

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
<fmt:message var="createProcessLabel" key="processManager.createProcess"/>
<fmt:message var="userSettingsLabel" key="processManager.userSettings"/>
<fmt:message var="csvExportLabel" key="processManager.csvExport"/>
<fmt:message var="refreshLabel" key="processManager.refresh"/>
<fmt:message var="yourRoleLabel" key="processManager.yourRole"/>
<fmt:message var="labelFilter" key="processManager.filter"/>
<fmt:message var="validateLabel" key="GML.validate"/>
<fmt:message var="cancelLabel" key="GML.cancel"/>

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

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" id="ng-app" ng-app="silverpeas.processManager">
<head>
  <title><%=resource.getString("GML.popupTitle")%>
  </title>
  <view:looknfeel/>
  <view:includePlugin name="toggle"/>
  <% form.displayScripts(out, context); %>
  <script type="text/javascript">
    var filterDisplayed = ${collapse};
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
        document.${context.formName}.submit();
      });
    }

    function resetFilter() {
      document.${context.formName}.reset();
    }

    function refreshList() {
      spProgressMessage.show();
      sp.formRequest('listProcess').submit();
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

    function exportCSV() {
      SP_openWindow("exportCSV", "exportWindow", "550", "350",
          "directories=0,menubar=0,toolbar=0,alwaysRaised");
    }

    $(function() {
      if (filterDisplayed) {
        displayFilter();
      }
    });
  </script>
</head>
<body class="yui-skin-sam processManager-main currentProfile_${currentRole} page_processes">
<view:operationPane>
  <c:if test="${currentReplacement == null}">
    <fmt:message key="processManager.replacements.manage" var="opIcon" bundle="${icons}"/>
    <c:url var="opIcon" value="${opIcon}"/>
    <view:operation action="manageReplacements" altText="${manageReplacementLabel}" icon="${opIcon}"/>
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
    <view:operation action="javaScript:exportCSV();" altText="${csvExportLabel}" icon="${opIcon}"/>
  </c:if>
</view:operationPane>
<view:window>
  <view:frame>
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
            <a href="javascript:refreshList()" title="${refreshLabel}"><img border="0" src="${opIcon}" alt="${refreshLabel}" align="absmiddle"/></a>
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
        <c:if test="${isProcessIdVisible}">
          <view:arrayColumn title="#" compareOn="${p -> silfn:longValue(p.id)}"/>
        </c:if>
        <view:arrayColumn title="<>" compareOn="${p -> processCompareOnStatus(p)}"/>
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
                <c:if test="${isProcessIdVisible}">
                  <view:arrayCellText text="${process.id}"/>
                </c:if>
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
                <c:if test="${isProcessIdVisible}">
                  <view:arrayCellText text="${process.id}"/>
                </c:if>
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
                <c:if test="${isProcessIdVisible}">
                  <view:arrayCellText text="${process.id}"/>
                </c:if>
                <view:arrayCellText><img class="icon" src="${timeoutIconUrl}" alt="${timeoutLabel}" title="${timeoutLabel}"/></view:arrayCellText>
                <view:arrayCellText text="${processTitleLink}"/>
              </c:when>

              <%-- DEFAULT CASE --%>
              <c:otherwise>
                <c:if test="${isProcessIdVisible}">
                  <view:arrayCellText><a href="${viewProcessUrl}">${process.id}</a></view:arrayCellText>
                </c:if>
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
        });
      </script>
    </div>
  </view:frame>
</view:window>
<div id="removeProcessInstanceConfirmation" style="display: none">
  ${confirmDeleteMessage}
</div>
<view:progressMessage/>
<script type="text/javascript">
  /* declare the module myapp and its dependencies (here in the silverpeas module) */
  var myapp = angular.module('silverpeas.processManager', ['silverpeas.services', 'silverpeas.directives']);
</script>
</body>
</html>