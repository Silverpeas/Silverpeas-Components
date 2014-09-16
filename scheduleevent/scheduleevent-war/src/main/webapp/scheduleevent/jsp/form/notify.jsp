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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ include file="../check.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<c:set var="sessionController">Silverpeas_ScheduleEvent</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<%@ include file="dateFormat.jspf"%>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<c:set var="browseContext" value="${requestScope.browseContext}" />

<c:set var="currentScheduleEvent" value="${requestScope.currentScheduleEvent}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<c:url var="openUserPopupUrl" value="/Rscheduleevent/jsp/OpenUserPopup" />
<c:url var="backPopupUrl" value="/Rscheduleevent/jsp/ConfirmScreen" />
<c:url var="confirmUrl" value="/Rscheduleevent/jsp/Confirm" />
<script type="text/javascript">
	function setUsers(){
		SP_openUserPanel('${openUserPopupUrl}', 'OpenUserPanel', 'menubar=no,scrollbars=no,statusbar=no');
		document.confirmForm.action = "${backPopupUrl}";
		document.confirmForm.submit();
	}

	function confirmEvent(){
		document.confirmForm.action = "${confirmUrl}";
    	document.confirmForm.submit();
    }
  </script>
<link rel="stylesheet" type="text/css" href="<c:url value='/scheduleevent/jsp/styleSheets/scheduleevent.css'/>" />
</head>
<body class="scheduleEvent" id="scheduleEvent_notify">

<fmt:message key="scheduleevent.form.title.screen4" var="scheduleEventTitle" />
<fmt:message key="scheduleevent" var="componentName" />
<c:url value="/Rscheduleevent/jsp/Main" var="returnMain" />
<view:browseBar extraInformations="${scheduleEventTitle}">
	<view:browseBarElt link="${returnMain}" label="${componentName}" />
</view:browseBar>

<fmt:message key="scheduleevent.form.addcontributors.alt" var="addContribAltText" />

<view:window>
	<%@ include file="descriptionBoard.jspf"%>
	<div id="dateTable">
		<table class="questionResults" width="100%" cellspacing="0" cellpadding="0" border="0"><thead><tr class="questionResults-top">
			<td class="titreLigne">
				<table cellspacing="0" cellpadding="0" border="0"><thead>
				<tr><td>
					<fmt:message key="scheduleevent.form.days" />&nbsp;:</td>
				</tr>
				<tr><td>
					<fmt:message key="scheduleevent.form.times" />&nbsp;:</td>
				</tr></thead>
				</table>
			</td>

			<c:set var="enableStyleTime" value="titreCouleur" />
			<c:set var="disableStyleTime" value="titreCouleur inactif" />
			<c:forEach var="dateOption" items="${currentScheduleEvent.optionalDateIndexes}">
				<td>
					<table class="questionResult" width="100%" cellspacing="1" cellpadding="0" border="0"><thead>
						<tr class="questionResults-top"><td colspan="2" class="day">
							<fmt:formatDate pattern="${gmlDateFormat}" value="${dateOption.date}" /></td>
						</tr>
						<tr class="questionResults-top">
							<c:set var="styleTimeClass" value="${disableStyleTime}" />
							<c:if test="${dateOption.morning}"><c:set var="styleTimeClass" value="${enableStyleTime}" /></c:if>
							<td class="${styleTimeClass}" width="50%" ><fmt:message key="scheduleevent.form.hour.columnam" /></td>

							<c:set var="styleTimeClass" value="${disableStyleTime}" />
							<c:if test="${dateOption.afternoon}"><c:set var="styleTimeClass" value="${enableStyleTime}" /></c:if>
							<td class="${styleTimeClass}" width="50%"><fmt:message key="scheduleevent.form.hour.columnpm" /></td>
						</tr></thead>
					</table>
				</td>
			</c:forEach></tr></thead>
		</table>
	</div>

	<p class="txtnav"><fmt:message key="scheduleevent.form.selectContributors" /></p>

	<div id="contributorsInfos">
		<h3><fmt:message key="scheduleevent.form.listcontributors" />&nbsp;:</h3>
		<ul>
			<c:if test="${not empty currentScheduleEvent.contributors}">
				<c:forEach var="currentContributor" items="${currentScheduleEvent.contributors}" varStatus="lineInfo">
					<li><c:out value="${currentContributor.userName}"/></li>
				</c:forEach>
			</c:if>
			<li><a class="btnAction" href="${'javascript: setUsers();'}">${addContribAltText}</a></li>
		</ul>
	</div>

	<form name="confirmForm" method="post" action=""></form>

	<div class="buttonBar">
	<view:buttonPane>
		<%@ include file="navigationRessource.jspf"%>
		<c:url var="backUrl" value="/Rscheduleevent/jsp/BackHour" />
		<view:button label="${backLabel}" action="${backUrl}" />
		<c:if test="${not empty currentScheduleEvent.contributors}">
			<fmt:message key="scheduleevent.button.finish" var="confirmLabel" />
			<view:button label="${confirmLabel}" action="${'javascript: confirmEvent();'}" />
		</c:if>
		<c:url var="cancelUrl" value="/Rscheduleevent/jsp/Cancel" />
		<view:button label="${cancelLabel}" action="${cancelUrl}" />
	</view:buttonPane></div>
</view:window>
</body>
</html>