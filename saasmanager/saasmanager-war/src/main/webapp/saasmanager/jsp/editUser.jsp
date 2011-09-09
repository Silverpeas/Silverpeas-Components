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
	<script type="text/javascript" src="${pageContext.request.contextPath}/saasmanager/jsp/js/editUser.js"></script>
	<style type="text/css">
		.label {width: 30%; white-space: nowrap;}
	</style>
</head>

<body>
	<view:window>
		<view:frame>
			<view:board>
				<form name="userForm" action="${pageContext.request.contextPath}/Rsaasmanager" method="post">
					<fmt:message key="mandatory" var="mandatory"/>
					<table cellpadding="5" width="100%">
						<tr>
							<td class="txtlibform label"><fmt:message key="lastName"/></td>
							<td><input type="text" name="lastName" size="30" value="${user.lastName}"/>
								<img alt='${mandatory}' src='${pageContext.request.contextPath}/util/icons/mandatoryField.gif' width="5" height="5" border="0"/></td>
						</tr>
						<tr>
							<td class="txtlibform label"><fmt:message key="firstName"/></td>
							<td><input type="text" name="firstName" size="30" value="${user.firstName}"/></td>
						</tr>
						<tr>
							<td class="txtlibform label"><fmt:message key="login"/></td>
							<c:choose>
								<c:when test="${user.id == -1}">
									<td><input type="text" name="login" size="30" value="${user.login}"/>
										<img alt='${mandatory}' src='${pageContext.request.contextPath}/util/icons/mandatoryField.gif' width="5" height="5" border="0"/></td>
								</c:when>
								<c:otherwise>
									<td>${user.login}</td>
								</c:otherwise>
							</c:choose>
						</tr>
						<tr>
							<td class="txtlibform label"><fmt:message key="email"/></td>
							<td><input type="text" name="email" size="30" value="${user.email}"/>
								<img alt='${mandatory}' src='${pageContext.request.contextPath}/util/icons/mandatoryField.gif' width="5" height="5" border="0"/></td>
						</tr>
						<tr>
							<td class="txtlibform label"><fmt:message key="password"/></td>
							<td><input type="password" name="password" size="30" value=""/>
								<c:if test="${user.id == -1}">
									<img alt='${mandatory}' src='${pageContext.request.contextPath}/util/icons/mandatoryField.gif' width="5" height="5" border="0"/>
								</c:if></td>
						</tr>
						<tr>
							<td class="txtlibform label"><fmt:message key="company"/></td>
							<td><input type="text" name="company" size="30" value="${user.company}"/></td>
						</tr>
						<tr>
							<td class="txtlibform label"><fmt:message key="phone"/></td>
							<td><input type="text" name="phone" size="30" value="${user.phone}"/></td>
						</tr>
						<tr>
							<td class="txtlibform label"><fmt:message key="role"/></td>
							<td><select name="role">
									<option value="admin"<c:if test="${user.role eq 'admin'}"> selected</c:if>><fmt:message key="role.admin"/></option>
									<option value="publisher"<c:if test="${user.role eq 'publisher'}"> selected</c:if>><fmt:message key="role.publisher"/></option>
									<option value="writer"<c:if test="${user.role eq 'writer'}"> selected</c:if>><fmt:message key="role.writer"/></option>
									<option value="reader"<c:if test="${user.role eq 'reader'}"> selected</c:if>><fmt:message key="role.reader"/></option>
								</select>
								<img alt='${mandatory}' src='${pageContext.request.contextPath}/util/icons/mandatoryField.gif' width="5" height="5" border="0"/></td>
						</tr>
						<tr>
							<td colspan="2">(<img alt='${mandatory}' src='${pageContext.request.contextPath}/util/icons/mandatoryField.gif'
								width="5" height="5" border="0"/> : <fmt:message key="mandatory"/>)</td>
						</tr>
						<c:if test="${not empty message}">
							<tr>
								<td>&nbsp;</td>
								<td class="txtlibform"><p><fmt:message key="message.${message}"/></p></td>
							</tr>
						</c:if>
					</table>
					<input type="hidden" name="action" value="management"/>
					<input type="hidden" name="function" value=""/>
					<input type="hidden" name="id" value="${user.id}"/>
					<input type="hidden" name="uid" value="${access.uid}"/>
					<input type="hidden" name="userId" value="${userId}"/>
					<center style="padding: 10px;">
						<view:buttonPane>
							<fmt:message key="cancel" var="cancel"/>
							<view:button label="${cancel}" action="javascript:cancelEdit();"/>
							<fmt:message key="validate" var="validate"/>
							<view:button label="${validate}" action="javascript:validate();"/>
						</view:buttonPane>
					</center>
				</form>
			</view:board>
		</view:frame>
	</view:window>
</body>
</html>