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
<view:includePlugin name="datepicker"/>
<script type="text/javascript">
	$(document).ready(function() {
		$("#datepicker").datepicker({
			minDate : 0,
			onSelect : function(dateText, inst) {
				document.addDateForm.dateToAdd.value = dateText;
				document.addDateForm.submit();
				
			}
			<c:if test="${not empty scheduleEventLastDate}">
				,
				defaultDate: $.datepicker.parseDate('yy-mm-dd', '${scheduleEventLastDate}')
			</c:if>
		});
	});

	function deleteDate(dateId) {
		document.deleteDateForm.dateToDelete.value = dateId;
		document.deleteDateForm.submit();
	}
</script>
<link rel='stylesheet' type='text/css' href="<c:url value='/scheduleevent/jsp/styleSheets/scheduleevent.css'/>" />
</head>
<body class="scheduleEvent" id="scheduleEvent_selected_date">

<fmt:message key="scheduleevent.form.title.screen2" var="scheduleEventTitle" />
<fmt:message key="scheduleevent" var="componentName" />
<c:url value="/Rscheduleevent/jsp/Main" var="returnMain" />
<view:browseBar extraInformations="${scheduleEventTitle}">
	<view:browseBarElt link="${returnMain}" label="${componentName}" />
</view:browseBar>
<view:window>
	<%@ include file="descriptionBoard.jspf"%>
	<p class="txtnav"><fmt:message key="scheduleevent.form.pickday" />&nbsp;:</p>

	<div id="datepicker">
		<form name="addDateForm" method="post" action="<c:url value='/Rscheduleevent/jsp/AddDate' />">
			<input type="hidden" name="dateToAdd" />
		</form>
		<form name="deleteDateForm" method="post" action="<c:url value='/Rscheduleevent/jsp/DeleteDate' />">
			<input type="hidden" name="dateToDelete" />
		</form>
	</div>

	<div class="selected_date">
		<h3 class="titreCouleur">
			<fmt:message key="scheduleevent.form.datesSelected" />
		</h3>

		<c:if test="${not empty currentScheduleEvent.dates}">
			<fmt:message key="scheduleevent.dates.delete" var="deleteScheduleEventDateAlt" />
			<fmt:message key="scheduleevent.icons.dates.delete" var="deleteScheduleEventDate" bundle="${icons}" />
			<ul>
				<c:forEach var="currentDate" items="${currentScheduleEvent.optionalDateIndexes}" varStatus="lineInfo">
					<li>
						<fmt:formatDate pattern="${gmlDateFormat}" value="${currentDate.date}" />
						<a href="javascript: deleteDate(${currentDate.indexFormat})">
							<img alt="${deleteScheduleEventDateAlt}" src="${deleteScheduleEventDate}" />
						</a>
					</li>
				</c:forEach>
			</ul>
		</c:if>
	</div>

	<div class="buttonBar">
		<view:buttonPane>
			<%@ include file="navigationRessource.jspf"%>
			<c:url var="backUrl" value="/Rscheduleevent/jsp/BackInfoGene" />
			<view:button label="${backLabel}" action="${backUrl}" />
			<c:if test="${not empty currentScheduleEvent.optionalDateIndexes}">
				<c:url var="nextUrl" value="/Rscheduleevent/jsp/AddOptionsNext" />
				<view:button label="${nextLabel}" action="${nextUrl}" />
			</c:if>
			<c:url var="cancelUrl" value="/Rscheduleevent/jsp/Cancel" />
			<view:button label="${cancelLabel}" action="${cancelUrl}" />
		</view:buttonPane>
	</div>
</view:window>
</body>
</html>