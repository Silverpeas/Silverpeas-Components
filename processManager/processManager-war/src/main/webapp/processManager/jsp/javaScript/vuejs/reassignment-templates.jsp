<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
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
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.processManager.multilang.processManagerBundle"/>

<fmt:message key="processManager.reassignment.manage" var="reassignmentLabel"/>
<fmt:message key="processManager.reassignment.incumbent" var="incumbentLabel"/>
<fmt:message key="processManager.reassignment.substitute" var="substituteLabel"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="management">
  <workflow-reassignment-popin
      v-on:api="reassignmentPopinApi = $event"></workflow-reassignment-popin>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="popin">
  <silverpeas-popin
      v-on:api="setPopinApi"
      title="${reassignmentLabel}">
    <silverpeas-form-pane v-on:api="setFormPaneApi"
                          v-bind:manualActions="true">
      <workflow-reassignment-form
          v-on:api="setFormApi"></workflow-reassignment-form>
    </silverpeas-form-pane>
  </silverpeas-popin>
</silverpeas-component-template>


<c:set var="andLabel"><fmt:message key='GML.and'/></c:set>
<c:set var="incumbentLabel"><fmt:message key='processManager.reassignment.incumbent'/></c:set>
<c:set var="substituteLabel"><fmt:message key='processManager.reassignment.substitute'/></c:set>
<c:set var="withRolesLabel"><fmt:message key='processManager.reassignment.roles.with.label'/></c:set>
<c:set var="withRoleLabel"><fmt:message key='processManager.reassignment.role.with.label'/></c:set>
<c:set var="mustHaveSameRoles"><fmt:message key='processManager.reassignment.substitute.roles.same.as.incumbent'/></c:set>

<!-- ########################################################################################### -->
<silverpeas-component-template name="form">
  <div class="table reassignment-form" v-if="roleManager">
    <div v-sp-init>
      {{addMessages({
      incumbentLabel : '${silfn:escapeJs(incumbentLabel)}',
      substituteLabel : '${silfn:escapeJs(substituteLabel)}',
      andLabel : '${silfn:escapeJs(andLabel)}',
      mustHaveSameRoles : '${silfn:escapeJs(mustHaveSameRoles)}'
      })}}
    </div>
    <div>
      <silverpeas-label class="label-ui-dialog" for="sp_wf_reassignment_form_i"
                        v-bind:mandatory="true">${incumbentLabel}</silverpeas-label>
      <div class="champ-ui-dialog">
        <silverpeas-user-group-select
            id="sp_wf_reassignment_form_i"
            v-on:api="selectIncumbentApi = $event"
            v-on:selection-change="incumbentChanged($event.selectedUserIds)"
            v-bind:include-removed-users="true"
            v-bind:role-filter="workflowRoleFilter"
            v-bind:component-id-filter="context.componentInstanceId"></silverpeas-user-group-select>
      </div>
      <div class="champ-ui-dialog user-roles incumbent" v-if="formattedIncumbentRoles">
        <span class="label" v-if="incumbentRoles.length > 1">${withRolesLabel} </span>
        <span class="label" v-else>${withRoleLabel} </span>
        <span v-html="formattedIncumbentRoles"></span>
      </div>
    </div>
    <div>
      <silverpeas-label class="label-ui-dialog" for="sp_wf_reassignment_form_s"
             v-bind:mandatory="true">${substituteLabel}</silverpeas-label>
      <div class="champ-ui-dialog" v-sp-disable-if="!formattedIncumbentRoles">
        <silverpeas-user-group-select
            id="sp_wf_reassignment_form_s"
            v-on:api="selectSubstituteApi = $event"
            v-on:selection-change="substituteChanged($event.selectedUserIds)"
            v-bind:role-filter="substituteRoleFilter"
            v-bind:matching-all-roles="true"
            v-bind:component-id-filter="context.componentInstanceId"></silverpeas-user-group-select>
      </div>
      <div class="champ-ui-dialog user-roles substitue" v-if="formattedSubstituteRoles">
        <span class="label" v-if="substituteRoles.length > 1">${withRolesLabel} </span>
        <span class="label" v-else>${withRoleLabel} </span>
        <span v-html="formattedSubstituteRoles"></span>
      </div>
    </div>
  </div>
</silverpeas-component-template>