<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>

<fmt:setLocale value="${language}"/>
<view:setBundle bundle="${resources}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
	<view:looknfeel/>
	<script type="text/javascript" src="${pageContext.request.contextPath}/saasmanager/jsp/js/management.js"></script>
	<script type="text/javascript">
		<fmt:message key="confirmUserDeletion" var="confirmUserDeletionLabel"/>
		CONFIRM_USER_DELETION_LABEL = "${confirmUserDeletionLabel}";
		<fmt:message key="confirmSelfDeletion" var="confirmSelfDeletionLabel"/>
		CONFIRM_SELF_DELETION_LABEL = "${confirmSelfDeletionLabel}";
	</script>
</head>

<body>
	<c:if test="${admin}">
		<view:operationPane>
			<fmt:message key="addUser" var="addUser"/>
			<view:operation altText="${addUser}" icon="" action="javascript:newUser()"/>
		</view:operationPane>
	</c:if>
	<view:window>
		<view:frame>
			<view:board>
				<fmt:message key="usersList" var="usersList"/>
				<view:arrayPane var="users" title="${usersList}">
					<fmt:message key="lastName" var="lastName"/>
			        <view:arrayColumn title="${lastName}" sortable="false"/>
			        <fmt:message key="firstName" var="firstName"/>
			        <view:arrayColumn title="${firstName}" sortable="false"/>
			        <fmt:message key="email" var="email"/>
			        <view:arrayColumn title="${email}" sortable="false"/>
			        <fmt:message key="company" var="company"/>
			        <view:arrayColumn title="${company}" sortable="false"/>
			        <fmt:message key="phone" var="phone"/>
			        <view:arrayColumn title="${phone}" sortable="false"/>
			        <fmt:message key="role" var="role"/>
			        <view:arrayColumn title="${role}" sortable="false"/>
			        <c:if test="${admin}">
			        	<fmt:message key="actions" var="actions"/>
			        	<view:arrayColumn title="${actions}" sortable="false"/>
			        </c:if>
			        <fmt:message key="modify" var="modify"/>
			        <fmt:message key="remove" var="remove"/>
			        <c:forEach items="${users}" var="user">
			        	<view:arrayLine>
			        		<view:arrayCellText text="${user.lastName}"/>
			        		<view:arrayCellText text="${user.firstName}"/>
			        		<view:arrayCellText text="${user.email}"/>
			        		<view:arrayCellText text="${user.company}"/>
			        		<view:arrayCellText text="${user.phone}"/>
			        		<fmt:message key="role.${user.role}" var="role"/>
			        		<view:arrayCellText text="${role}"/>
			        		<c:if test="${admin}">
			        			<view:arrayCellText text="<a href='javascript:editUser(${user.id})'><img alt='${modify}'
		        					src='${pageContext.request.contextPath}/util/icons/update.gif'/></a>&nbsp;&nbsp;
			        				<a href='javascript:removeUser(${user.id})'><img alt='${remove}'
		        					src='${pageContext.request.contextPath}/util/icons/delete.gif'/></a>"/>
					        </c:if>
			        	</view:arrayLine>
			        </c:forEach>
				</view:arrayPane>
				<form name="managementForm" action="${pageContext.request.contextPath}/Rsaasmanager" method="post">
					<input type="hidden" name="action" value="management"/>
					<input type="hidden" name="function" value=""/>
					<input type="hidden" name="uid" value="${access.uid}"/>
					<input type="hidden" name="userId" value="${userId}"/>
					<input type="hidden" name="id" value=""/>
				</form>
			</view:board>
		</view:frame>
	</view:window>
</body>
</html>