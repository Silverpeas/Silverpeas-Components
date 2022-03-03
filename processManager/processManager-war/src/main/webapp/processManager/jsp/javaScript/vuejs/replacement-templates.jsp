<%--
  ~ Copyright (C) 2000 - 2022 Silverpeas
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

<fmt:message key="processManager.replacements.none" var="noReplacementLabel"/>
<fmt:message key="processManager.replacements.mine.asIncumbent" var="mineAsIncumbentLabel"/>
<fmt:message key="processManager.replacements.mine.asSubstitute" var="mineAsSubstituteLabel"/>
<fmt:message key="processManager.replacements.all" var="allAsSubstituteLabel"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="module">
  <div class="replacement-module">
    <script type="text/x-template" id="workflow-no-item-replacement-list">
      <ul>
        <li><span class="txt-no-content">${noReplacementLabel}</span></li>
      </ul>
    </script>
    <workflow-replacement-management
        v-on:api="replacementApi = $event"
        v-on:replacement-create="api.reload()"
        v-on:replacement-update="api.reload()"
        v-on:replacement-delete="api.reload()"></workflow-replacement-management>
    <slot name="header" v-if="!fullDisplay"></slot>
    <!-- ALL LIST -->
    <div id="all-remplacement">
      <slot name="header" v-if="fullDisplay"></slot>
      <silverpeas-list v-if="allList"
                       class="all"
                       v-bind:items="allList">
        <div slot="before" class="title header">
          <h3 class="title-all">${allAsSubstituteLabel}</h3>
        </div>
        <silverpeas-list-item v-for="replacement in allList" v-bind:key="replacement.uri">
          <workflow-replacement-list-item
              v-bind:replacement="replacement"></workflow-replacement-list-item>
          <template slot="actions">
            <workflow-replacement-list-item-actions
                v-bind:replacement="replacement"
                v-on:modify-click="replacementApi.modify(replacement)"
                v-on:remove-click="replacementApi.remove(replacement)"></workflow-replacement-list-item-actions>
          </template>
        </silverpeas-list-item>
        <component slot="noItem" v-bind:is="{template:'#workflow-no-item-replacement-list'}"></component>
      </silverpeas-list>
    </div>
    <div id="my-remplacement" v-bind:class="{'lecteur-view':!fullDisplay}">
      <!-- INCUMBENT LIST -->
      <silverpeas-list v-if="incumbentList"
                       class="as-incumbent"
                       v-bind:items="incumbentList">
        <div slot="before" class="title header">
          <h3 class="title-as-incumbent">${mineAsIncumbentLabel}</h3>
        </div>
        <silverpeas-list-item v-for="replacement in incumbentList" v-bind:key="replacement.uri">
          <workflow-replacement-list-item
              v-bind:replacement="replacement"></workflow-replacement-list-item>
          <template slot="actions">
            <workflow-replacement-list-item-actions
                v-bind:replacement="replacement"
                v-on:modify-click="replacementApi.modify(replacement)"
                v-on:remove-click="replacementApi.remove(replacement)"></workflow-replacement-list-item-actions>
          </template>
        </silverpeas-list-item>
        <component slot="noItem" v-bind:is="{template:'#workflow-no-item-replacement-list'}"></component>
      </silverpeas-list>
      <!-- SUBSTITUTE LIST -->
      <silverpeas-list v-if="substituteList"
                       class="as-substitute"
                       v-bind:items="substituteList">
        <div slot="before" class="title header">
          <h3 class="title-as-substitute">${mineAsSubstituteLabel}</h3>
        </div>
        <silverpeas-list-item v-for="replacement in substituteList" v-bind:key="replacement.uri">
          <workflow-replacement-list-item
              v-bind:replacement="replacement"></workflow-replacement-list-item>
          <template slot="actions">
            <workflow-replacement-list-item-actions
                v-bind:replacement="replacement"
                v-on:modify-click="replacementApi.modify(replacement)"
                v-on:remove-click="replacementApi.remove(replacement)"></workflow-replacement-list-item-actions>
          </template>
        </silverpeas-list-item>
        <component slot="noItem" v-bind:is="{template:'#workflow-no-item-replacement-list'}"></component>
      </silverpeas-list>
    </div>
  </div>
</silverpeas-component-template>

<fmt:message var="addReplacementLabel" key="processManager.replacements.add"/>
<fmt:message var="modifyReplacementLabel" key="processManager.replacements.modify"/>
<c:set var="deleteMessage"><fmt:message key='processManager.replacements.delete.confirmation'>
  <fmt:param value="{{replacement.incumbent ? replacement.incumbent.fullName : ''}}"/>
  <fmt:param value="{{replacement.substitute ? replacement.substitute.fullName : ''}}"/>
</fmt:message></c:set>

<!-- ########################################################################################### -->
<silverpeas-component-template name="management">
  <div>
    <!-- CREATE POPIN -->
    <silverpeas-popin
        v-on:api="addPopinApi = $event"
        title="${addReplacementLabel}">
      <silverpeas-form-pane v-on:api="addFormApi = $event"
                            v-bind:manualActions="true">
        <workflow-replacement-form
            v-bind:replacement="replacement"
            v-bind:contentReadyDeferred="popinOpenDeferred"></workflow-replacement-form>
      </silverpeas-form-pane>
    </silverpeas-popin>
    <!-- MODIFY POPIN -->
    <silverpeas-popin
        v-on:api="modifyPopinApi = $event"
        title="${modifyReplacementLabel}">
      <silverpeas-form-pane v-on:api="modifyFormApi = $event"
                            v-bind:manualActions="true">
        <workflow-replacement-form
            v-bind:replacement="replacement"
            v-bind:contentReadyDeferred="popinOpenDeferred"></workflow-replacement-form>
      </silverpeas-form-pane>
    </silverpeas-popin>
    <!-- DELETE POPIN -->
    <silverpeas-popin
        v-on:api="deletePopinApi = $event"
        type="confirmation">
      <span>${deleteMessage}</span>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<fmt:message key="GML.modify" var="modifyLabel"/>
<fmt:message key="GML.delete" var="deleteLabel"/>
<c:url var="updateIconUrl" value="/util/icons/update.gif"/>
<c:url var="deleteIconUrl" value="/util/icons/delete.gif"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="list-item-actions">
  <div>
    <silverpeas-button
        v-if="replacement.canBeModified"
        icon-url="${updateIconUrl}"
        title="${modifyLabel}"
        v-on:click.native="$emit('modify-click')"></silverpeas-button>
    <silverpeas-button
        v-if="replacement.canBeDeleted"
        icon-url="${deleteIconUrl}"
        title="${deleteLabel}"
        v-on:click.native="$emit('remove-click')"></silverpeas-button>
  </div>
</silverpeas-component-template>

<fmt:message var="theLabel" key='GML.date.the'/>
<fmt:message var="fromLabel" key='GML.date.from'/>
<fmt:message var="toLabel" key='GML.to'/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="list-item">
  <div class="replacement-item">
    <div class="substitute">{{replacement.substitute.fullName}}</div>
    <div class="incumbent">{{replacement.incumbent.fullName}}</div>
    <div class="period">
      <span v-if="isOneDay" key="on-day">${theLabel} </span>
      <span v-else key="on-day">${fromLabel} </span>
      <span class="date">{{replacement.startDate | displayAsDate}}</span>
      <span v-if="!isOneDay">${toLabel} </span>
      <span v-if="!isOneDay" class="date">{{replacement.endDate | displayAsDate}}</span>
    </div>
    <workflow-replacement-matching-roles
        v-bind:replacement="replacement"></workflow-replacement-matching-roles>
  </div>
</silverpeas-component-template>

<c:set var="incumbentLabel"><fmt:message key='processManager.replacements.incumbent'/></c:set>
<c:set var="substituteLabel"><fmt:message key='processManager.replacements.substitute'/></c:set>
<c:set var="startDateLabel"><fmt:message key='processManager.replacements.startDate'/></c:set>
<c:set var="endDateLabel"><fmt:message key='processManager.replacements.endDate'/></c:set>
<c:set var="filterRoleLabel"><fmt:message key='processManager.replacements.roles.filter.label'/></c:set>
<c:set var="noMatchingRoleError"><fmt:message key='processManager.replacements.errors.noMatchingRole'/></c:set>

<!-- ########################################################################################### -->
<silverpeas-component-template name="form">
  <div class="table replacement-form">
    <div v-sp-init>
      {{addMessages({
      incumbentLabel : '${silfn:escapeJs(incumbentLabel)}',
      substituteLabel : '${silfn:escapeJs(substituteLabel)}',
      startDateLabel : '${silfn:escapeJs(startDateLabel)}',
      endDateLabel : '${silfn:escapeJs(endDateLabel)}',
      noMatchingRoleError : '${silfn:escapeJs(noMatchingRoleError)}'
      })}}
    </div>
    <div v-sp-disable-if="!roleManager">
      <label class="label-ui-dialog" for="sp_wf_replacement_form_i">${incumbentLabel}</label>
      <div class="champ-ui-dialog">
        <silverpeas-user-group-select
            id="sp_wf_replacement_form_i"
            v-on:api="selectIncumbentApi = $event"
            v-on:selection-change="incumbentChanged($event.selectedUserIds)"
            v-bind:initial-user-ids="replacement.incumbent && replacement.incumbent.id"
            v-bind:mandatory="true"
            v-bind:read-only="!(context.currentUser.isSupervisor && isCreation)"
            v-bind:role-filter="incumbentRoleFilter | mapRoleName"
            v-bind:component-id-filter="context.componentInstanceId"></silverpeas-user-group-select>
      </div>
    </div>
    <div v-sp-disable-if="!roleManager || !(replacement.incumbent && replacement.incumbent.id)">
      <label class="label-ui-dialog" for="sp_wf_replacement_form_s">${substituteLabel}</label>
      <div class="champ-ui-dialog replacement-role-filter" v-if="roleManager && substituteRoleFilterItems">
        <span class="label">${filterRoleLabel} </span>
        <ul>
          <li v-for="role in substituteRoleFilterItems" v-bind:key="role.name">
            <a href="javascript:void(0)"
               v-bind:class="{'item-selected':role.selected}"
               v-on:click.stop.prevent="role.selected=!role.selected">{{role.label}}</a></li>
        </ul>
      </div>
      <div class="champ-ui-dialog" v-sp-disable-if="!selectedSubstituteFilterRoles.length">
        <silverpeas-user-group-select
            id="sp_wf_replacement_form_s"
            v-on:api="selectSubstituteApi = $event"
            v-on:selection-change="substituteChanged($event.selectedUserIds)"
            v-bind:initial-user-ids="replacement.substitute && replacement.substitute.id"
            v-bind:mandatory="true"
            v-bind:role-filter="selectedSubstituteFilterRoles | mapRoleName"
            v-bind:component-id-filter="context.componentInstanceId"></silverpeas-user-group-select>
      </div>
      <div class="champ-ui-dialog">
        <workflow-replacement-matching-roles
            v-bind:replacement="replacement"
            v-bind:computed-trigger="computedRoleManagerMixinTrigger"></workflow-replacement-matching-roles>
      </div>
    </div>
    <label class="label-ui-dialog" for="sp_wf_replacement_form_sd">${startDateLabel}</label>
    <div class="champ-ui-dialog">
      <silverpeas-date-picker id="sp_wf_replacement_form_sd"
                              name="startDate"
                              v-bind:zone-id="context.currentUser.zoneId"
                              v-bind:mandatory="true"
                              v-model="replacement.startDate"></silverpeas-date-picker>
    </div>
    <label class="label-ui-dialog" for="sp_wf_replacement_form_ed">${endDateLabel}</label>
    <div class="champ-ui-dialog">
      <silverpeas-date-picker id="sp_wf_replacement_form_ed"
                              name="endDate"
                              v-bind:zone-id="context.currentUser.zoneId"
                              v-bind:mandatory="true"
                              v-model="replacement.endDate"></silverpeas-date-picker>
    </div>
  </div>
</silverpeas-component-template>


<c:set var="andLabel"><fmt:message key='GML.and'/></c:set>
<c:set var="forRoleLabel"><fmt:message key='processManager.replacements.forRole'/></c:set>
<c:set var="forRolesLabel"><fmt:message key='processManager.replacements.forRoles'/></c:set>

<!-- ########################################################################################### -->
<silverpeas-component-template name="matching-roles">
  <div class="replacement-matching-roles" v-if="roleManager && matchingRoles">
    <span v-if="matchingRoles.length === 1">${forRoleLabel} {{matchingRoles | mapRoleLabel | joinWith({separator:', ',lastSeparator:' ${andLabel} '})}}</span>
    <span v-if="matchingRoles.length > 1">${forRolesLabel} {{matchingRoles | mapRoleLabel | joinWith({separator:', ',lastSeparator:' ${andLabel} '})}}</span>
    <span v-if="!matchingRoles.length" class="error">${noMatchingRoleError}</span>
  </div>
</silverpeas-component-template>