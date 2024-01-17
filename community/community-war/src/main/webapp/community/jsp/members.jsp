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

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<view:setConstant constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN" var="adminRole"/>

<fmt:message var="back" key="GML.back"/>
<fmt:message key="community.members.list" var="memberListLabel"/>
<fmt:message key="community.members.item.memberOn" var="memberOnLabel"/>
<fmt:message key="GML.lastName" var="lastNameLabel"/>
<fmt:message key="GML.firstName" var="firstNameLabel"/>
<fmt:message key="community.members.item.requestedMembershipOn" var="requestedMembershipOnLabel"/>

<c:url var="backUri" value="${requestScope.navigationContext.previousNavigationStep.uri}"/>
<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>
<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="currentUserZoneId" value="${requestScope.currentUserZoneId}"/>
<c:set var="highestUserRole" value="${requestScope.highestUserRole}"/>
<jsp:useBean id="highestUserRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>
<c:set var="adminMustValidateNewMember" value="${silfn:booleanValue(requestScope.adminMustValidateNewMember)}"/>
<c:set var="isAdmin" value="${highestUserRole.isGreaterThanOrEquals(adminRole)}"/>
<c:set var="isMember" value="${requestScope.isMember}"/>
<c:set var="memberData" value="${requestScope.members}"/>
<jsp:useBean id="memberData" type="org.silverpeas.core.util.SilverpeasList<org.silverpeas.components.community.model.CommunityMembership>"/>
<c:set var="members" value="<%=MemberUIEntity.convertList(memberData, Collections.emptySet())%>"/>
<c:set var="formatInstant" value="${i -> i == null ? null : silfn:formatTemporal(i.atZone(currentUserZoneId), currentUserZoneId, currentUserLanguage)}"/>

<view:sp-page>
  <view:sp-head-part>
    <script type="application/javascript">
      let arrayPaneAjaxControl;
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:browseBar componentId="${componentId}" path="${requestScope.navigationContext}"/>
    <view:operationPane>
    </view:operationPane>
    <view:window>
      <view:frame>
        <div id="members-list">
          <view:arrayPane title="${memberListLabel}"
                          var="communityMemberListIdentifier"
                          routingAddress="${componentUriBase}/members"
                          numberLinesPerPage="25"
                          export="true">
            <view:arrayColumn title="${lastNameLabel}" compareOn="${r -> fn:toLowerCase(r.user.lastName)}"/>
            <view:arrayColumn title="${firstNameLabel}" compareOn="${r -> fn:toLowerCase(r.user.firstName)}"/>
            <c:if test="${adminMustValidateNewMember}">
              <view:arrayColumn title="${requestedMembershipOnLabel}" compareOn="${r -> r.requestedMembershipOn() != null ? r.requestedMembershipOn() : null}"/>
            </c:if>
            <view:arrayColumn title="${memberOnLabel}" compareOn="${r -> r.memberOn()}"/>
            <view:arrayLines var="member" items="${members}">
              <view:arrayLine>
                <view:arrayCellText text="${member.user.lastName}"/>
                <view:arrayCellText text="${member.user.firstName}"/>
                <c:if test="${adminMustValidateNewMember}">
                  <view:arrayCellText text="${formatInstant(member.requestedMembershipOn())}"/>
                </c:if>
                <view:arrayCellText text="${formatInstant(member.memberOn())}"/>
              </view:arrayLine>
            </view:arrayLines>
          </view:arrayPane>
          <script type="text/javascript">
            whenSilverpeasReady(function() {
              arrayPaneAjaxControl = sp.arrayPane.ajaxControls('#members-list');
            });
          </script>
        </div>
      </view:frame>
      <view:buttonPane>
        <view:button label="${back}" action="javascript:sp.navRequest('${backUri}').go()"/>
      </view:buttonPane>
    </view:window>
  </view:sp-body-part>
</view:sp-page>