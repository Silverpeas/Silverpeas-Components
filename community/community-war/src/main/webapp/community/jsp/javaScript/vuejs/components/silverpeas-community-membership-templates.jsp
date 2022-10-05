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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/util" prefix="viewTags" %>
<c:set var="language" value="${sessionScope['SilverSessionController'].favoriteLanguage}"/>
<fmt:setLocale value="${language}"/>
<view:setBundle basename="org.silverpeas.components.community.multilang.communityBundle"/>

<fmt:message key="community.nbMembers" var="lessOrEqualOneMember">
  <fmt:param value="${1}"/>
</fmt:message>
<c:set var="lessOrEqualOneMember" value="${fn:replace(lessOrEqualOneMember, '1', '{{nbMembers}}')}"/>
<fmt:message key="community.nbMembers" var="moreThanOneMember">
  <fmt:param value="${2}"/>
</fmt:message>
<c:set var="moreThanOneMember" value="${fn:replace(moreThanOneMember, '2', '{{nbMembers}}')}"/>
<fmt:message key="community.join" var="joinLabel"/>
<fmt:message key="community.leave" var="leaveLabel"/>
<fmt:message key="community.join.pendingValidation" var="pendingValidationMsg"/>
<fmt:message key="GML.accept" var="acceptTitle"/>
<fmt:message key="community.join.request.validate.accept" var="acceptMsg">
  <fmt:param value="{{validateJoinRequestCtx.user.fullName}}"/>
</fmt:message>
<fmt:message key="GML.refuse" var="refuseTitle"/>
<fmt:message key="community.join.request.validate.refuse" var="refuseMsg">
  <fmt:param value="{{validateJoinRequestCtx.user.fullName}}"/>
</fmt:message>
<fmt:message key="community.charter.link.access" var="charterLink"/>
<fmt:message key="community.charter.accept.title" var="acceptCharterTitle"/>

<!-- ########################################################################################### -->
<silverpeas-component-template name="community-membership">
  <div v-if="community">
    <div class="silverpeas-community-membership">
      <div v-if="displayNbMembersForNonMembers && members" class="nb-members">
        <template v-if="nbMembers > 1">${moreThanOneMember}</template>
        <template v-else>${lessOrEqualOneMember}</template>
      </div>
      <div class="membership">
        <a v-if="isMember" class="leave-button" href="javascript:void(0)" v-on:click="leave">${leaveLabel}</a>
        <span v-else-if="isMembershipPending" class="pending-join-message">${pendingValidationMsg}</span>
        <a v-else class="join-button" href="javascript:void(0)" v-on:click="join">${joinLabel}</a>
      </div>
      <%--  CHARTER ACCEPT  --%>
      <silverpeas-community-charter-accept
          v-if="community.charterURL"
          v-on:api="acceptCharterPopinApi = $event"
          v-bind:community="community"></silverpeas-community-charter-accept>
      <%--  MEMBERSHIP VALIDATION  --%>
      <silverpeas-popin v-on:api="validateJoinRequestCtx.popinApi = $event"
                        v-bind:dialog-class="validateJoinRequestCtx.accept ? 'membership-request-accepted' : 'membership-request-refused'"
                        v-bind:title="validateJoinRequestCtx.accept ? '${silfn:escapeJs(acceptTitle)}' : '${silfn:escapeJs(refuseTitle)}'"
                        type="validation"
                        v-bind:minWidth="650">
        <div class="validate-join-request">
          <div v-if="validateJoinRequestCtx.accept">${acceptMsg}</div>
          <div v-else>${refuseMsg}</div>
          <form action="javascript:void(0)" method="post">
            <div class="fields extra-message">
              <div class="field">
                <label class="txtlibform" for="validate-join-extra-message"><fmt:message key='GML.additional.message'/></label>
                <div class="champs">
                  <textarea rows="5" cols="100" name="message" id="validate-join-extra-message" v-model="validateJoinRequestCtx.message"></textarea>
                </div>
              </div>
            </div>
          </form>
        </div>
      </silverpeas-popin>
    </div>
    <%--  CHARTER LINK  --%>
    <silverpeas-fade-transition>
      <silverpeas-community-charter-preview
          class="silverpeas-community-charter"
          v-bind:community="community"
          v-if="displayCharterOnSpaceHomepage && community.charterURL"></silverpeas-community-charter-preview>
    </silverpeas-fade-transition>
    <%--  LEAVE SPACE  --%>
    <silverpeas-popin v-on:api="leaveCtx.popinApi = $event"
                      v-bind:title="'${silfn:escapeJs(leaveLabel)}'"
                      v-bind:dialog-class="'membership-leave'"
                      type="validation"
                      v-bind:minWidth="650">
      <silverpeas-form-pane v-on:api="leaveCtx.formPaneApi = $event"
                            v-bind:manual-actions="true"
                            v-bind:mandatory-legend="false">
        <silverpeas-community-leave-form v-on:api="leaveCtx.formApi = $event"></silverpeas-community-leave-form>
      </silverpeas-form-pane>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="charter-accept">
  <div class="silverpeas-community-charter-accept">
    <div v-sp-init>
      {{addMessages({
      acceptCharterTitle : '${silfn:escapeJs(acceptCharterTitle)}'
    })}}
    </div>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="charter-preview">
  <div class="silverpeas-community-charter-preview" v-if="contentApi">
    <div v-sp-init>
      {{addMessages({
      charterLink : '${silfn:escapeJs(charterLink)}'
    })}}
    </div>
    <a href="javascript:void(0)" v-on:click="open">${charterLink}</a>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="charter-content">
  <div class="silverpeas-community-charter-content">
    <viewTags:displayExternalIframe url="${empty param.charterURL ? 'javascript:void(0)' : param.charterURL}"/>
  </div>
</silverpeas-component-template>

<!-- ########################################################################################### -->
<silverpeas-component-template name="leave-form">
  <div class="silverpeas-community-leave-form">
    <div class="fields">
      <div class="field">
        <label class="txtlibform" for="leave-reason"><fmt:message key='community.membership.leaving.reason'/></label>
        <div class="champs">
          <select id="leave-reason" name="reason" v-model="reason">
            <option v-for="(reasonLabel, index) in reasonLabels" v-bind:value="index">{{reasonLabel}}</option>
          </select>
        </div>
      </div>
      <div class="field">
        <label class="txtlibform" for="leave-extra-message"><fmt:message key='GML.additional.message'/></label>
        <div class="champs">
          <silverpeas-multiline-text-input name="message" id="leave-extra-message"
              v-bind:rows="5" v-bind:cols="100" v-model="message"></silverpeas-multiline-text-input>
        </div>
      </div>
      <div class="field leave-contact-accept">
        <div class="champs">
          <input id="leave-contact-accept" type="checkbox" name="contactInFuture"
                 v-model="contactInFuture"/>
        </div>
        <label class="txtlibform" for="leave-contact-accept"><fmt:message key='community.membership.leaving.contact.accept'/></label>
      </div>
    </div>
  </div>
</silverpeas-component-template>