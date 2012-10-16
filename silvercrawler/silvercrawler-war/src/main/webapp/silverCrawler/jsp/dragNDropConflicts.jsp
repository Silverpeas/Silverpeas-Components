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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%@ page isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController">Silverpeas_SilverCrawler_<c:out value="${componentId}" />
</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<fmt:message key="silverCrawler.someConflictsDetected" var="pageTitle" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>${pageTitle}</title>
		<view:looknfeel />
	</head>
	<body>

	<%-- BrowseBar --%>
	<view:browseBar>
  		<view:browseBarElt link="" label="${pageTitle}" />
	</view:browseBar>

	<%-- Operation Pane --%>
	<view:operationPane>
	</view:operationPane>

	<%-- Main content --%>
	<fmt:message key="silverCrawler.alreadyExists" var="alreadyExistsLabel"/>
	<fmt:message key="silverCrawler.ignore" var="ignoreLabel"/>
	<fmt:message key="silverCrawler.replace" var="replaceLabel"/>
	<fmt:message key="GML.validate" var="validateLabel"/>
	<fmt:message key="GML.cancel" var="cancelLabel"/>
	<view:window>
		<view:board>
			<form name="conflictsForm" action="ResolveConflicts" method="POST">
				<b><fmt:message key="silverCrawler.someConflictsDetected"/> :</b>
				<br/><br/>
				<table>
				<c:forEach items="${DnDReport.items}" var="item">
					<c:if test="${item.itemAlreadyExists}">
						<tr><td><b>${item.fileName}</b> ${alreadyExistsLabel}</td><td><input type="radio" name="choice${item.id}" value="ignore" checked/> ${ignoreLabel} - <input type="radio" name="choice${item.id}" value="replace"/> ${replaceLabel} </td></tr>
					</c:if>
				</c:forEach>
				</table>
				<br/>
				<view:buttonPane>
					<view:button action="javascript:document.conflictsForm.submit()" label="${validateLabel}"/>
					<view:button action="ViewDirectory" label="${cancelLabel}"/>
				</view:buttonPane>
			</form>
		</view:board>
	</view:window>
	</body>
</html>