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

<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<c:set var="sessionController">Silverpeas_ScheduleEvent</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<%@ include file="dateFormat.jspf"%>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="currentScheduleEvent" value="${requestScope.currentScheduleEvent}" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<script type="text/javascript">
	function addOptionsHour(){
		var dateValue;
		var alertFlag = false;
		
		$('#dateTable >tbody >tr').each(function(index) {
			dateChecked = $(this).find('input:checked').length > 0;
		  	if (!dateChecked) {
			  dateValue =  $(this).find('.day').text();
			  alertFlag = true;
		  	}
		});
		
		if (alertFlag) {
	      alert('<fmt:message key="scheduleevent.form.hour.mandatoryCheck"/> ' + $.trim(dateValue));
		} else {
    	  document.addOptionsHour.submit();
		}
    }
	
	function checkEnable(){
	}
  </script>
<link rel="stylesheet" type="text/css" href="<c:url value='/scheduleevent/jsp/styleSheets/scheduleevent.css'/>" />
</head>
<body class="scheduleEvent" id="scheduleEvent_selected_hour">

<fmt:message key="scheduleevent.form.title.screen3" var="scheduleEventTitle" />
<fmt:message key="scheduleevent" var="componentName" />
<c:url value="/Rscheduleevent/jsp/Main" var="returnMain" />
<view:browseBar extraInformations="${scheduleEventTitle}">
	<view:browseBarElt link="${returnMain}" label="${componentName}" />
</view:browseBar>
<view:window>
	<%@ include file="descriptionBoard.jspf"%>

	<p class="txtnav"><fmt:message key="scheduleevent.form.hour"/></p>

	<form name="addOptionsHour" method="post" action="<c:url value='/Rscheduleevent/jsp/AddOptionsHour' />"><div id="selected_hour">
		<table id="dateTable">
			<thead>
				<tr>
					<td></td>
					<td class="titreCouleur"><fmt:message key="scheduleevent.form.hour.am" /></td>
					<td class="titreCouleur"><fmt:message key="scheduleevent.form.hour.pm" /></td>
				</tr>
			</thead>
			<tbody>
				<c:set var="checked" value=" checked='checked'" />
				<c:forEach var="dateOption" items="${currentScheduleEvent.optionalDateIndexes}">
					<tr>
						<td class="day">
							<fmt:formatDate pattern="${gmlDateFormat}" value="${dateOption.date}" />
						</td>
						<td>
							<input type="checkbox" name="${dateOption.morningIndexFormat}" <c:if test='${dateOption.morning}'>${checked}</c:if> 
							       value="${time.id}" onclick="checkEnable()"/>
						</td>
						<td>
							<input type="checkbox" name="${dateOption.afternoonIndexFormat}" <c:if test='${dateOption.afternoon}'>${checked}</c:if>
							       value="${time.id}" onclick="checkEnable()"/>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table></div>
	</form>

	<div class="buttonBar">
		<view:buttonPane>
			<%@ include file="navigationRessource.jspf"%>
			<c:url var="backUrl" value="/Rscheduleevent/jsp/BackDate"/>
			<view:button label="${backLabel}" action="${backUrl}" />
			<view:button label="${nextLabel}" action="${'javascript: addOptionsHour();'}"/>
			<c:url var="cancelUrl" value="/Rscheduleevent/jsp/Cancel"/>
			<view:button label="${cancelLabel}" action="${cancelUrl}" />
		</view:buttonPane>
	</div>
</view:window>
</body>
</html>