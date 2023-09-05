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
<%@ page import="org.silverpeas.core.admin.component.model.WAComponent" %>
<%@ page import="org.silverpeas.components.community.CommunityComponentSettings" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.components.community.multilang.communityBundle"/>

<fmt:message key="community.menu.item.selectSpaceHomepage" var="selectSpaceHomepageLabel"/>
<fmt:message key="community.menu.item.defineCharter" var="defineCharterLabel"/>
<fmt:message key="community.charter.define.url" var="urlLabel"/>
<fmt:message key="community.charter.define.url.help" var="urlHelp"/>
<fmt:message key="community.charter.define.url.input.help" var="inputUrlHelp"/>
<fmt:message key="community.charter.define.save.success" var="saveCharterSuccessMsg"/>
<c:set var="waCommunity" value="<%= WAComponent.getByName(CommunityComponentSettings.COMPONENT_NAME).orElse(null)%>"/>
<c:set var="displayCharterOnSpaceHomepageParam" value="${waCommunity.getAllParameters().stream()
    .filter(p -> p.getName() eq 'displayCharterOnSpaceHomepage')
    .map(p -> silfn:toLocalizedParameter(waCommunity, p, language))
    .findFirst()
    .orElse(null)}"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="community-management">
  <div class="community-management" v-if="community">
    <div v-sp-init>
      {{addMessages({
      saveCharterSuccessMsg : '${silfn:escapeJs(saveCharterSuccessMsg)}'
    })}}
    </div>
    <%--  SPACE HOMEPAGE POPIN  --%>
    <silverpeas-admin-space-homepage-popin
        v-bind:community="community"
        v-on:api="adminSpaceHomepagePopinApi = $event"
        v-on:validated="saveSpaceHomepage($event)"
        v-bind:title="'${silfn:escapeJs(selectSpaceHomepageLabel)}'"
        v-bind:space-id="spaceId"
        v-bind:homepage="spaceHomepage"></silverpeas-admin-space-homepage-popin>
    <%--   CHARTER POPIN   --%>
    <silverpeas-popin v-on:api="defineCharterPopinApi = $event"
                      v-bind:title="'${silfn:escapeJs(defineCharterLabel)}'"
                      v-bind:dialog-class="'define-charter-popin'"
                      type="validation"
                      v-bind:minWidth="650">
      <silverpeas-form-pane v-on:api="defineCharterFormApi = $event"
                            v-bind:mandatoryLegend="false"
                            v-bind:manualActions="true"
                            v-on:data-update="saveCharter">
        <silverpeas-community-define-charter-form
            v-bind:community="community"
            v-bind:display-charter-on-space-homepage="displayCharterOnSpaceHomepage"></silverpeas-community-define-charter-form>
      </silverpeas-form-pane>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="community-define-charter-form">
  <div class="define-charter-form">
    <div class="inlineMessage">${urlHelp}</div>
    <div class="field">
      <label class="txtlibform" for="dcCharterURL">${urlLabel}</label>
      <div class="champs">
        <silverpeas-url-input
            id="dcCharterURL"
            name="charterURL"
            v-bind:maxlength="400"
            v-bind:title="'${silfn:escapeJs(inputUrlHelp)}'"
            v-bind:placeholder="'https://www.xx.com/charte.html'"
            v-model="charterURL"></silverpeas-url-input>
        <div class="specificationInput">${inputUrlHelp}</div>
      </div>
    </div>
    <div class="field">
      <label class="txtlibform" for="dcDisplayCharter"
             title="${displayCharterOnSpaceHomepageParam.help}">${displayCharterOnSpaceHomepageParam.label}</label>
      <div class="champs">
        <input id="dcDisplayCharter" type="checkbox" name="dcDisplayCharter"
               title="${displayCharterOnSpaceHomepageParam.help}"
               v-model="displayCharter"/>
      </div>
    </div>
  </div>
</silverpeas-component-template>