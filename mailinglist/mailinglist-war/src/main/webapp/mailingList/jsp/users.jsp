<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController">Silverpeas_MailingList_<c:out value="${componentId}" />
</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><fmt:message key="mailingList.tab.users.title" /></title>
<view:looknfeel />
<c:if test="${requestScope.currentUserIsAdmin}">
  <script type="text/javascript">
    function deleteUsers(){
      if(confirm('<fmt:message key="mailingList.users.delete.confirm"/>')){
        document.removeUsers.submit();
      }
    }
    function addUsers(){
      document.add.submit();
    }
  </script>
</c:if>
</head>
<body>
<fmt:message key="mailingList.icons.user.delete" var="deleteUserIcon" bundle="${icons}" />
<fmt:message key="mailingList.icons.user.delete.alt" var="deleteUserAltText" />
<fmt:message key="mailingList.icons.user.add" var="addUserIcon" bundle="${icons}" />
<fmt:message key="mailingList.icons.user.add.alt" var="addUserAltText" />
<c:url var="deleteUserIconUrl" value="${deleteUserIcon}" />
<c:url var="addUserIconUrl" value="${addUserIcon}" />
<fmt:message key="mailingList.tab.list.title" var="listTabTitle" />
<fmt:message key="mailingList.tab.activity.title" var="activityTabTitle" />
<fmt:message key="mailingList.tab.users.title" var="usersTabTitle" />
<view:browseBar>
  <view:browseBarElt label="${usersTabTitle}" link="" />
</view:browseBar>
<c:if test="${requestScope.currentUserIsAdmin}">
  <view:operationPane>
    <view:operation altText="${deleteUserAltText}" icon="${deleteUserIconUrl}" action="javascript:deleteUsers();" />
    <view:operation altText="${addUserAltText}" icon="${addUserIconUrl}" action="javascript:addUsers();" />
  </view:operationPane>
</c:if>
<view:window>
  <c:url var="activityAction" value="/Rmailinglist/${componentId}/Main" />
  <c:url var="listAction" value="/Rmailinglist/${componentId}/list/${componentId}" />
  <view:tabs>
    <view:tab label="${activityTabTitle}" action="${activityAction}" selected="false" />
    <view:tab label="${listTabTitle}" action="${listAction}" selected="false" />
    <c:if test="${requestScope.currentListIsModerated}">
      <fmt:message key="mailingList.tab.moderation.title" var="moderationTabTitle" />
      <c:url var="moderationAction" value="/Rmailinglist/${componentId}/moderationList/${componentId}" />
      <view:tab label="${moderationTabTitle}" action="${moderationAction}" selected="false" />
    </c:if>
    <view:tab label="${usersTabTitle}" action="${'#'}" selected="true" />
    <c:if test="${requestScope.currentUserIsAdmin}">
      <fmt:message key="mailingList.tab.subscribers.title" var="subscribersTabTitle" />
      <c:url var="subscriberssAction" value="/Rmailinglist/${componentId}/subscription/${componentId}/subscription/put" />
      <view:tab label="${subscribersTabTitle}" action="${subscriberssAction}" selected="false" />
    </c:if>
  </view:tabs>
  <view:frame>
    <center>
    <form id="removeUsers" name="removeUsers" method="POST" action="<c:url value="/Rmailinglist/${componentId}/users/delete"/>">
    <table id="list" class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
      <tr>
        <td class="ArrayColumn" align="center" width="20">&nbsp;</td>
        <td class="ArrayColumn" width="100%"><fmt:message key="mailingList.users.email.title" /></td>
      </tr>
      <c:forEach items="${requestScope.currentUsersList}" var="user" varStatus="userIndex">
        <tr>
          <td align="center" valign="top"><input type="checkbox" name="users" value="<c:out value="${user.email}" />" /></td>
          <td><c:out value="${user.email}" /></td>
        </tr>
      </c:forEach>
      <tr>
        <c:url var="paginationAction" value="/Rmailinglist/${componentId}/users/${componentId}" />
        <td colspan="2"><view:pagination currentPage="${requestScope.currentPage}" nbPages="${requestScope.nbPages}" action="${paginationAction}" pageParam="currentPage" /></td>
      </tr>
    </table>
    </form>

    <form id="add" name="add" method="POST" action="<c:url value="/Rmailinglist/${componentId}/users/add/"/>">
    <table class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
      <tr>
        <td><b><fmt:message key="mailingList.users.add.title" /> :</b><br />
        <textarea id="users" name="users" cols="160" rows="5"></textarea></td>
      </tr>
    </table>
    </form>
    </center>
  </view:frame>
</view:window>
</body>
</html>