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

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.processManager.multilang.processManagerBundle"/>

<c:set var="incumbentLabel"><fmt:message key='processManager.replacements.incumbent'/></c:set>
<c:set var="substituteLabel"><fmt:message key='processManager.replacements.substitute'/></c:set>
<c:set var="startDateLabel"><fmt:message key='processManager.replacements.startDate'/></c:set>
<c:set var="endDateLabel"><fmt:message key='processManager.replacements.endDate'/></c:set>

<div class="table replacement-form">
  <label class="label-ui-dialog" for="sp_wf_replacement_form_i">${incumbentLabel}</label>
  <div class="champ-ui-dialog">
    <silverpeas-user-group-select
        id="sp_wf_replacement_form_i"
        v-on:api="selectIncumbentApi = $event"
        v-on:selection-change="incumbentChanged($event.selectedUserIds)"
        v-bind:initial-user-ids="replacement.incumbent && replacement.incumbent.id"
        v-bind:mandatory="true"
        v-bind:read-only="!context.currentUser.isSupervisor"
        v-bind:role-filter="roleFilter"
        v-bind:component-id-filter="context.componentInstanceId"></silverpeas-user-group-select>
  </div>
  <label class="label-ui-dialog" for="sp_wf_replacement_form_s">${substituteLabel}</label>
  <div class="champ-ui-dialog">
    <silverpeas-user-group-select
        id="sp_wf_replacement_form_s"
        v-on:api="selectSubstituteApi = $event"
        v-on:selection-change="substituteChanged($event.selectedUserIds)"
        v-bind:initial-user-ids="replacement.substitute && replacement.substitute.id"
        v-bind:mandatory="true"
        v-bind:role-filter="roleFilter"
        v-bind:component-id-filter="context.componentInstanceId"></silverpeas-user-group-select>
  </div>
  <label class="label-ui-dialog" for="sp_wf_replacement_form_sd">${startDateLabel}</label>
  <div class="champ-ui-dialog">
    <silverpeas-date-picker id="sp_wf_replacement_form_sd"
                            name="startDate"
                            v-bind:zone-id="context.currentUser.zoneId"
                            v-bind:mandatory="true"
                            v-model="replacement.startDate"
                            v-on:status-change="startDateStatus = $event"></silverpeas-date-picker>
  </div>
  <label class="label-ui-dialog" for="sp_wf_replacement_form_ed">${endDateLabel}</label>
  <div class="champ-ui-dialog">
    <silverpeas-date-picker id="sp_wf_replacement_form_ed"
                            name="endDate"
                            v-bind:zone-id="context.currentUser.zoneId"
                            v-bind:mandatory="true"
                            v-model="replacement.endDate"
                            v-on:status-change="endDateStatus = $event"></silverpeas-date-picker>
  </div>
</div>
