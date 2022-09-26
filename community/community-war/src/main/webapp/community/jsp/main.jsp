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
    "http://www.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="org.silverpeas.components.community.control.MemberUIEntity" %>
<%@ page import="java.util.Collections" %>
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="spaceLabel" value="${requestScope.browseContext[0]}"/>
<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<view:setConstant constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN" var="adminRole"/>

<fmt:message key="community.menu.item.editSpaceHomePage" var="editSpaceHomePageLabel"/>
<fmt:message key="community.menu.item.memberList" var="memberListLabel"/>
<fmt:message key="community.menu.item.history" var="historyLabel"/>
<fmt:message key="community.menu.item.selectSpaceHomepage" var="selectSpaceHomepageLabel"/>
<fmt:message key="community.join" var="joinLabel"/>
<fmt:message key="community.leave" var="leaveLabel"/>
<fmt:message key="community.members.pendingValidation.list" var="pendingValidationListLabel"/>
<fmt:message key="community.members.item.requestedMembershipOn" var="requestedMembershipOnLabel"/>
<fmt:message key="GML.lastName" var="lastNameLabel"/>
<fmt:message key="GML.firstName" var="firstNameLabel"/>
<fmt:message key="GML.Validation" var="validationLabel"/>
<fmt:message key="GML.accept" var="acceptLabel"/>
<fmt:message key="GML.refuse" var="refuseLabel"/>

<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>
<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="currentUserZoneId" value="${requestScope.currentUserZoneId}"/>
<c:set var="highestUserRole" value="${requestScope.highestUserRole}"/>
<jsp:useBean id="highestUserRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>
<c:set var="isSpaceHomepage" value="${silfn:booleanValue(requestScope.isSpaceHomepage)}"/>
<c:set var="adminMustValidateNewMember" value="${silfn:booleanValue(requestScope.adminMustValidateNewMember)}"/>
<c:set var="membersToValidateData" value="${requestScope.membersToValidate}"/>
<c:set var="isAdmin" value="${highestUserRole.isGreaterThanOrEquals(adminRole)}"/>
<c:set var="isMember" value="${silfn:booleanValue(requestScope.isMember)}"/>
<c:set var="isMembershipPending" value="${silfn:booleanValue(requestScope.isMembershipPending)}"/>
<c:set var="spacePresentationContent" value="${requestScope.spacePresentationContent}"/>
<c:set var="formatInstant" value="${i -> i == null ? null : silfn:formatTemporal(i.atZone(currentUserZoneId), currentUserZoneId, currentUserLanguage)}"/>

<view:sp-page>
  <view:sp-head-part>
    <view:script src="/community/jsp/javaScript/services/silverpeas-community-subscription-service.js"/>
    <view:link href="/community/jsp/javaScript/vuejs/components/silverpeas-community-subscription.css"/>
    <view:script src="/community/jsp/javaScript/vuejs/components/silverpeas-community-subscription.js"/>
    <script type="application/javascript">
      let arrayPaneAjaxControl;
      whenSilverpeasReady().then(function() {
        notyInfo('${highestUserRole}');
        notyInfo('${isMember ? 'is a member' : 'is not a member'}');
      });
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}"/>
    <c:if test="${not isSpaceHomepage}">
      <view:operationPane>
        <c:if test="${isAdmin}">
          <view:operation action="spaceHomePage/edit" altText="${editSpaceHomePageLabel}"/>
        </c:if>
        <view:operation action="members" altText="${memberListLabel}"/>
        <c:if test="${isAdmin}">
          <view:operation action="members/history" altText="${historyLabel}"/>
          <view:operation action="space/homepage/select" altText="${selectSpaceHomepageLabel}"/>
        </c:if>
      </view:operationPane>
    </c:if>
    <view:window>
      <view:frame>
        <div id="subscription">
          <silverpeas-community-subscription
              v-on:api="api = $event"
              v-on:membership-join-request="refreshMembersToValidate"
              v-on:join-request-validated="refreshMembersToValidate"></silverpeas-community-subscription>
        </div>
        <c:if test="${membersToValidateData != null}">
          <jsp:useBean id="membersToValidateData" type="org.silverpeas.core.util.SilverpeasList<org.silverpeas.components.community.model.CommunityMembership>"/>
          <c:set var="membersToValidate" value="<%=MemberUIEntity.convertList(membersToValidateData, Collections.emptySet())%>"/>
          <view:board>
            <div id="membersToValidate-list">
              <view:arrayPane title="${pendingValidationListLabel}" var="membersToValidate" routingAddress="${componentUriBase}/Main" export="true" numberLinesPerPage="10">
                <view:arrayColumn title="${lastNameLabel}" compareOn="${r -> fn:toLowerCase(r.user.lastname)}"/>
                <view:arrayColumn title="${firstNameLabel}" compareOn="${r -> fn:toLowerCase(r.user.lastname)}"/>
                <view:arrayColumn title="${requestedMembershipOnLabel}" compareOn="${r -> r.requestedMembershipOn}"/>
                <view:arrayColumn title="${validationLabel}" width="100px" sortable="false"/>
                <view:arrayLines var="memberToValidate" items="${membersToValidate}">
                  <view:arrayLine>
                    <view:arrayCellText text="${memberToValidate.user.lastName}"/>
                    <view:arrayCellText text="${memberToValidate.user.firstName}"/>
                    <view:arrayCellText text="${formatInstant(memberToValidate.requestedMembershipOn())}"/>
                    <view:arrayCellText>
                      <a href="javascript:void(0)"
                         onclick="subscriptionVm.api.validateJoinRequest('${memberToValidate.user.id}', true)"
                         title="${acceptLabel}">${acceptLabel}</a>
                      <a href="javascript:void(0)"
                         onclick="subscriptionVm.api.validateJoinRequest('${memberToValidate.user.id}', false)"
                         title="${refuseLabel}">${refuseLabel}</a>
                    </view:arrayCellText>
                  </view:arrayLine>
                </view:arrayLines>
              </view:arrayPane>
              <script type="text/javascript">
                whenSilverpeasReady(function() {
                  arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#membersToValidate-list');
                });
              </script>
            </div>
          </view:board>
        </c:if>
        <div class="space-facade">${spacePresentationContent}</div>
      </view:frame>
    </view:window>
    <script type="text/javascript">
      window.subscriptionVm = new Vue({
        el : '#subscription',
        provide : function() {
          return {
            context: this.context,
            subscriptionService: new CommunitySubscriptionService(this.context)
          }
        },
        data : {
          context : {
            currentUser : extendsObject({
              isMember : ${isMember},
              isMembershipPending : ${isMembershipPending},
              isAdmin :${isAdmin}
            }, currentUser),
            componentInstanceId : '${componentId}',
            spaceLabel : '${spaceLabel}'
          },
          api : undefined
        },
        methods : {
          refreshMembersToValidate : function() {
            if (arrayPaneAjaxControl) {
              sp.ajaxRequest('${componentUriBase}/Main')
                  .send()
                  .then(arrayPaneAjaxControl.refreshFromRequestResponse);
            }
          }
        }
      });
    </script>
  </view:sp-body-part>
</view:sp-page>