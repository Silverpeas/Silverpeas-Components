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
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<fmt:message key="scheduleevent.js.notitle" var="scheduleEventNoTitle" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<script type="text/javascript">
	function addInfoGene() {
		var title = document.addInfoGene.title.value;
		if (title != null && title.length > 0) {
			document.addInfoGene.submit();
		} else {
			alert('${scheduleEventNoTitle}');
		}
	}
</script>
<link rel='stylesheet' type='text/css' href="<c:url value='/scheduleevent/jsp/styleSheets/scheduleevent.css'/>" />
</head>
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="currentScheduleEvent" value="${requestScope.currentScheduleEvent}" />

<c:set var="currentTitle" value="${currentScheduleEvent.title}" />
<c:set var="currentDescription" value="${currentScheduleEvent.description}" />

<body onload="document.getElementById('title').focus();">

<fmt:message key="scheduleevent.form.title.screen1" var="scheduleEventTitle" />
<fmt:message key="scheduleevent" var="componentName" />
<c:url value="/Rscheduleevent/jsp/Main" var="returnMain" />
<view:browseBar extraInformations="${scheduleEventTitle}">
	<view:browseBarElt link="${returnMain}" label="${componentName}" />
</view:browseBar>
<view:window>
	<fmt:message key="scheduleevent.form.title" var="titleLabel" />
	<fmt:message key="scheduleevent.form.description" var="descLabel" />

	<table width="98%" cellspacing="0" cellpadding="5" border="0" class="tableBoard">
		<tbody>
			<tr>
				<td>
					<center>
					<form id="addInfoGene" name="addInfoGene" method="post" action="<c:url value="/Rscheduleevent/jsp/AddInfoGene" />">
						<fmt:message key="scheduleevent.form.mandatoryfield" var="mandatoryLabel" />
						<fmt:message key="scheduleevent.form.mandatory" var="mandatoryIconLabel" />
						<fmt:message key="scheduleevent.icons.mandatory" var="mandatoryIcon" bundle="${icons}" />
						<c:set var="mandatoryImage" value="<img border='0' src='${mandatoryIcon}' width='5' height='5' />" />

						<table width="100%" cellpadding="5">
							<tbody>
								<tr>
									<td class="txtlibform">${titleLabel} :</td>
									<td>
										<input id="title" type="text" name="title" maxlength="254" size="100" value="<c:out value='${currentTitle}'/>" />&nbsp;${mandatoryImage}
									</td>
								</tr>
								<tr>
									<td class="txtlibform">${descLabel} :</td>
									<td>
										<textarea id="description" rows="5" cols="100" name="description"><c:out value="${currentDescription}"/></textarea>
									</td>
								</tr>
								<tr>
									<td class="txtlibform" colspan="2">(${mandatoryImage}&nbsp;:${mandatoryLabel})</td>
								</tr>
							</tbody>
						</table>
					</form>
					</center>
				</td>
			</tr>
		</tbody>
	</table>
	<br />
	<div class="buttonBar">
		<view:buttonPane>
			<%@ include file="navigationRessource.jspf"%>
			<c:url var="cancelUrl" value="/Rscheduleevent/jsp/Cancel" />
			<view:button label="${nextLabel}" action="${'javascript: addInfoGene();'}" />
			<view:button label="${cancelLabel}" action="${cancelUrl}" />
		</view:buttonPane>
	</div>
</view:window>
</body>
</html>