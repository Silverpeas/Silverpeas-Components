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

<fmt:message key="processManager.replacements.none" var="noReplacementLabel"/>
<fmt:message key="processManager.replacements.mine.asIncumbent" var="mineAsIncumbentLabel"/>
<fmt:message key="processManager.replacements.mine.asSubstitute" var="mineAsSubstituteLabel"/>
<fmt:message key="processManager.replacements.all" var="allAsSubstituteLabel"/>

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
