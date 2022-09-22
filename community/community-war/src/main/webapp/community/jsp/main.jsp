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
<%@ include file="check.jsp" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ taglib uri="http://www.silverpeas.com/tld/silverFunctions" prefix="silfn" %>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>
<c:set var="currentUserLanguage" value="${requestScope.resources.language}"/>
<fmt:setLocale value="${currentUserLanguage}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons"/>

<view:setConstant constant="org.silverpeas.core.admin.user.model.SilverpeasRole.ADMIN" var="adminRole"/>

<fmt:message key="community.menu.item.editSpaceHomePage" var="editSpaceHomePageLabel"/>

<c:url var="componentUriBase" value="${requestScope.componentUriBase}"/>
<c:set var="currentUser" value="${requestScope.currentUser}"/>
<c:set var="highestUserRole" value="${requestScope.highestUserRole}"/>
<jsp:useBean id="highestUserRole" type="org.silverpeas.core.admin.user.model.SilverpeasRole"/>
<c:set var="isAdmin" value="${highestUserRole.isGreaterThanOrEquals(adminRole)}"/>
<c:set var="isMember" value="${requestScope.isMember}"/>
<c:set var="spaceFacadeContent" value="${requestScope.spaceFacadeContent}"/>

<view:sp-page>
  <view:sp-head-part>
    <script type="application/javascript">
      whenSilverpeasReady().then(function() {
        notyInfo('${highestUserRole}');
        notyInfo('${isMember ? 'is a member' : 'is not a member'}');
      });
    </script>
  </view:sp-head-part>
  <view:sp-body-part>
    <view:operationPane>
      <c:if test="${isAdmin}">
        <view:operation action="spaceHomePage/edit" altText="${editSpaceHomePageLabel}"/>
      </c:if>
    </view:operationPane>
    <view:window>
      <c:if test="${isAdmin}">
        <view:frame>
          Bienvenue sur l'application community.
        </view:frame>
      </c:if>
      <view:frame>
        <c:choose>
          <c:when test="${isMember}">
            <div class="leave-space-header"></div>
          </c:when>
          <c:otherwise>
            <div class="join-space-header"></div>
          </c:otherwise>
        </c:choose>
        <c:if test="${isAdmin}">
          <view:board>
            Cette instance s'appelle
            <span class="communityName"><c:out value="${requestScope.browseContext[1]}"/></span>.<br/>
            Elle se situe dans l'espace
            <span class="communityName"><c:out value="${requestScope.browseContext[0]}"/></span>.
          </view:board>
        </c:if>
        <div class="space-facade">${spaceFacadeContent}</div>
      </view:frame>
    </view:window>
  </view:sp-body-part>
</view:sp-page>