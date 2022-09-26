<%--
  ~ Copyright (C) 2000 - 2021 Silverpeas
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
<fmt:message key="community.join.pendingValidation" var="pendingValidationMsg">
  <fmt:param value="{{spaceLabel}}"/>
</fmt:message>
<fmt:message key="GML.accept" var="acceptTitle"/>
<fmt:message key="community.join.request.validate.accept" var="acceptMsg">
  <fmt:param value="{{validateJoinRequestPopinCtx.user.fullName}}"/>
</fmt:message>
<fmt:message key="GML.refuse" var="refuseTitle"/>
<fmt:message key="community.join.request.validate.refuse" var="refuseMsg">
  <fmt:param value="{{validateJoinRequestPopinCtx.user.fullName}}"/>
</fmt:message>

<!-- ########################################################################################### -->
<silverpeas-component-template name="community-subscription">
  <div class="silverpeas-community-subscription">
    <div v-if="members" class="nb-members">
      <span v-if="nbMembers > 1">${moreThanOneMember}</span>
      <span v-else>${lessOrEqualOneMember}</span>
    </div>
    <div class="subscription">
      <a v-if="isMember" href="javascript:void(0)" v-on:click="leave">${leaveLabel}</a>
      <span v-else-if="isMembershipPending">${pendingValidationMsg}</span>
      <a v-else href="javascript:void(0)" v-on:click="join">${joinLabel}</a>
    </div>
    <silverpeas-popin v-on:api="validateJoinRequestPopinApi = $event"
                      v-bind:dialog-class="validateJoinRequestPopinCtx.accept ? 'membership-request-accepted' : 'membership-request-accepted'"
                      v-bind:title="validateJoinRequestPopinCtx.accept ? '${acceptTitle}' : '${refuseTitle}'"
                      type="validation"
                      minWidth="650">
      <div class="validate-join-request">
        <div v-if="validateJoinRequestPopinCtx.accept">${acceptMsg}</div>
        <div v-else>${refuseMsg}</div>
        <form action="javascript:void(0)" method="post">
          <div class="fields extra-message">
            <div class="field">
              <label class="txtlibform" for="validate-join-extra-message"><fmt:message key='GML.additional.message'/></label>
              <div class="champs">
                <textarea rows="5" cols="100" name="message" id="validate-join-extra-message" v-model="validateJoinRequestPopinCtx.message"></textarea>
              </div>
            </div>
          </div>
        </form>
      </div>
    </silverpeas-popin>
  </div>
</silverpeas-component-template>