<%--

    Copyright (C) 2000 - 2012 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>

<%@ include file="check.jsp"%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator"
	prefix="view"%>

<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController">Silverpeas_SilverCrawler_<c:out
		value="${componentId}" />
</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}"
	var="icons" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><fmt:message key="silverCrawler.renameFile" /></title>
</head>
<body>

<form id="renameForm" name="renameForm" method="POST" action="RenameFile">
		<table id="renameTable" width="100%" cellspacing="2" cellpadding="2"
			border="0">

			<c:if test="${not empty errorMessage}">
				<tr>
					<td colspan="2">${errorMessage}</td>
				</tr>
			</c:if>

			<tr>
				<td><fmt:message key="silverCrawler.oldName" /></td>
				<td>${fileName}</td>
			</tr>

			<tr>
				<td><fmt:message key="silverCrawler.newName" /></td>
				<td><input type="text" name="newName" id="newName"></td>
			</tr>
		</table>
</form>

</body>
</html>