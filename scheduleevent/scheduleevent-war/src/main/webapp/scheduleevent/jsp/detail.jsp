<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<%@ include file="form/dateFormat.jspf"%>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<view:includePlugin name="popup"/>
<c:url var="animationUrl" value="/util/javaScript/animation.js" />
<c:url var="openUserPopupUrl" value="/Rscheduleevent/jsp/OpenUserPopup" />
<c:url var="backPopupUrl" value="/Rscheduleevent/jsp/Detail" />
<script type="text/javascript" src="${animationUrl}"></script>
<script type="text/javascript">
	function deleteScheduleEvent() {
		if (confirm('<fmt:message key="scheduleevent.form.delete.confirm"/>')) {
			document.utilForm.action = "<c:url value="/Rscheduleevent/jsp/Delete"/>";
			document.utilForm.submit();
		}
	}

	function setUsers(){
		SP_openUserPanel('${openUserPopupUrl}', 'OpenUserPanel', 'menubar=no,scrollbars=no,statusbar=no');
		document.utilForm.action = "${backPopupUrl}";
		document.utilForm.submit();
	}

	function modifyState() {
		document.utilForm.action = "<c:url value="/Rscheduleevent/jsp/ModifyState"/>";
		document.utilForm.submit();
	}

	function valid() {
		document.reponseForm.submit();
	}

  function exportICal() {
    displaySingleFreePopupFrom('<c:url value="/Rscheduleevent/jsp/ExportToICal"/>', {
      title : '${scheduleEventDetail.title}',
      width : '500'
    });
  }
</script>
<link rel='stylesheet' type='text/css' href="<c:url value='/scheduleevent/jsp/styleSheets/scheduleevent.css'/>" />
</head>

<body class="scheduleEvent" id="scheduleEvent_detail">

<fmt:message key="scheduleevent" var="scheduleEventTitle" />
<c:url value="/Rscheduleevent/jsp/Main" var="returnMain" />
<view:browseBar extraInformations="${scheduleEventDetail.title}">
	<view:browseBarElt link="${returnMain}" label="${scheduleEventTitle}" />
</view:browseBar>

<c:set var="scheduleEventDetail" value="${requestScope.scheduleEventDetail}" />

<c:set var="selectionTime" value="${scheduleEventDetail.bestTimes}" />

<view:operationPane>
<c:if test="${scheduleEventDetail.allowedToChange}">
	<fmt:message key="scheduleevent.icons.delete" var="deleteIcon" bundle="${icons}" />
	<fmt:message key="scheduleevent.icons.delete.alt" var="deleteIconAlt" />
	<c:if test="${scheduleEventDetail.closed}">
		<fmt:message key="scheduleevent.icons.open" var="modifyStateIcon" bundle="${icons}" />
		<fmt:message key="scheduleevent.icons.open.alt" var="modifyStateIconAlt" />
	</c:if>
	<c:if test="${!scheduleEventDetail.closed}">
		<fmt:message key="scheduleevent.icons.close" var="modifyStateIcon" bundle="${icons}" />
		<fmt:message key="scheduleevent.icons.close.alt" var="modifyStateIconAlt" />
	</c:if>
	<fmt:message key="scheduleevent.icons.users" var="usersIcon" bundle="${icons}" />
	<fmt:message key="scheduleevent.icons.users.alt" var="usersIconAlt" />
		<view:operation altText="${deleteIconAlt}" icon="${deleteIcon}" action="${'javascript:deleteScheduleEvent();'}" />
		<view:operationSeparator/>
		<view:operation altText="${modifyStateIconAlt}" icon="${modifyStateIcon}" action="${'javascript:modifyState();'}" />
		<view:operationSeparator/>
		<view:operation altText="${usersIconAlt}" icon="${usersIcon}" action="${'javascript:setUsers();'}" />
  <view:operationSeparator/>
</c:if>
<c:if test="${scheduleEventDetail.currentUserDefinedAsSubscriber and scheduleEventDetail.closed and selectionTime.bestDateExists}">
  <fmt:message key="scheduleevent.icons.export.alt" var="exportScheduleEventAlt" />
  <fmt:message key="scheduleevent.icons.exportToICal" var="exportScheduleEventIconPath" bundle="${icons}" />
  <view:operation altText="${exportScheduleEventAlt}" icon="${exportScheduleEventIconPath}" action="${'javascript: exportICal();'}" />
</c:if>
</view:operationPane>

<view:window>
	<c:if test="${selectionTime.bestDateExists}"><div class="inlineMessage">
		<fmt:message key="${selectionTime.multilangLabel}">
			<fmt:param value="${selectionTime.datesNumber}"/>
			<fmt:param value="<strong>${scheduleEventDetail.presentParticipationPercentageRate}</strong>"/>
		</fmt:message>
		<c:set var="selectionTimeSeparator" value="" />
		<strong><c:forEach var="time" items="${selectionTime.times}">
			<fmt:message key="${time.multilangLabel}" var="selectedTime"/>
			<fmt:formatDate pattern="${gmlDateFormat}" value="${time.date.date}" var="selectedDate"/>
			<c:out value="${selectionTimeSeparator} ${selectedDate} ${fn:toLowerCase(selectedTime)}" />
			<c:set var="selectionTimeSeparator" value="," />
		</c:forEach></strong>
	</div></c:if>

	<table width="98%" cellspacing="0" cellpadding="5" border="0" class="tableBoard"><tbody><tr>
		<td>
			<fmt:message key="scheduleevent.form.title" var="titleLabel" />
			<fmt:message key="scheduleevent.form.description" var="descLabel" />
			<table width="100%" cellpadding="5">
				<tbody>
					<tr>
						<td class="txtlibform">${titleLabel}&nbsp;:</td>
            <td colspan="3"><c:out value="${scheduleEventDetail.title}"/></td>
					</tr>
					<c:if test="${not empty scheduleEventDetail.description}">
						<tr>
							<td class="txtlibform">${descLabel}&nbsp;:</td>
							<td colspan="3"><view:encodeHtmlParagraph string="${scheduleEventDetail.description}"/></td>
						</tr>
					</c:if>
					<tr>
						<td class="txtlibform"><fmt:message key="scheduleevent.form.listcontributors" />&nbsp;:</td>
						<td>${scheduleEventDetail.subscribersCount}</td>
						<td class="txtlibform"><fmt:message key="scheduleevent.form.participationrate" />&nbsp;:</td>
						<td>${scheduleEventDetail.participationPercentageRate}</td>
					</tr>
				</tbody>
			</table>
		</td></tr></tbody>
	</table>

	<p class="txtnav"><fmt:message key="scheduleevent.form.selectdateandvalidate" />&nbsp;:</p>

	<form id="utilForm" name="utilForm" method="post" action="">
		<input type="hidden" name="scheduleEventId" value="${scheduleEventDetail.id}" />
	</form>
	<form id="reponseForm" name="reponseForm" method="post" action="<c:url value="/Rscheduleevent/jsp/ValidResponse"/>"><div id="dateTable">
	    <input type="hidden" name="scheduleEventId" value="${scheduleEventDetail.id}" />
		<table class="questionResults" width="100%" cellspacing="0" cellpadding="0" border="0"><thead><tr class="questionResults-top">
			<td class="titreLigne">
				<table class="questionResults" width="100%" cellspacing="1" cellpadding="0" border="0"><thead>
					<tr class="questionResults-top"><td class="hideDay">&nbsp;</td></tr>
					<tr class="questionResults-top"><td class="hideTime">&nbsp;</td></tr>
					<c:if test="${scheduleEventDetail.currentUserDefinedAsSubscriber}">
						<c:set var="currentUser" value="${scheduleEventDetail.currentUser}" />
						<tr class="${currentUser.htmlClassAttribute}"><td class="displayUserName"><c:out value="${currentUser.name}"/></td></tr>
					</c:if>
					<c:forEach var="subscriber" items="${scheduleEventDetail.otherSubscribers}">
						<tr><td class="displayUserName"><c:out value="${subscriber.name}"/></td></tr>
					</c:forEach>
					<tr class="resultVote"><td>&nbsp;</td></tr>
					</thead>
				</table>
			</td>

			<c:set var="enableStyleTime" value="titreCouleur" />
			<c:set var="disableStyleTime" value="titreCouleur inactif" />
			<c:forEach var="date" items="${scheduleEventDetail.dates}">
				<td>
					<table class="questionResults" width="100%" cellspacing="1" cellpadding="0" border="0"><thead>
						<tr class="questionResults-top"><td colspan="${date.timesNumber}" class="day">
							<fmt:formatDate pattern="${gmlDateFormat}" value="${date.date}" /></td>
						</tr>
						<tr class="questionResults-top">
						    <c:set var="widthRate" value="${100 / date.timesNumber}%" />
							<c:forEach var="time" items="${date.times}">
								<td class="${time.htmlClassAttribute}" width="${widthRate}"><fmt:message key="${time.multilangLabel}" /></td>
							</c:forEach>
						</tr>
						<c:if test="${scheduleEventDetail.currentUserDefinedAsSubscriber}">
							<tr class="${currentUser.htmlClassAttribute}">
								<c:forEach var="time" items="${date.times}">
									<c:set var="availability" value="${time.availabilities[currentUser]}" />
									<td class="${availability.htmlClassAttribute}">
										<c:choose>
											<c:when test="${!scheduleEventDetail.closed && availability.editable}">
												<input type="checkbox" name="userChoices" value="${time.id}" <c:out value="${availability.markLabel}"/> />&nbsp;
											</c:when>
											<c:otherwise>
												${availability.markLabel}
											</c:otherwise>
										</c:choose>
									</td>
								</c:forEach>
							</tr>
						</c:if>
						<c:forEach var="subscriber" items="${scheduleEventDetail.otherSubscribers}"><tr>
							<c:forEach var="time" items="${date.times}">
								<c:set var="availability" value="${time.availabilities[subscriber]}" />
								<td class="${availability.htmlClassAttribute}">${availability.markLabel}</td>
							</c:forEach>
						</tr></c:forEach>
						<tr class="resultVote"><c:forEach var="time" items="${date.times}">
							<c:set var="participation" value="${time.presents}" />
							<td class="${participation.htmlClassAttribute}">${participation.positiveAnswerPercentage}</td>
						</c:forEach></tr>
						</thead>
					</table>
				</td>
			</c:forEach></tr></thead>
		</table>
	</div></form>

	<c:if test="${!scheduleEventDetail.closed && scheduleEventDetail.currentUserDefinedAsSubscriber}">
		<div class="buttonBar"><view:buttonPane>
			<fmt:message key="scheduleevent.button.valid" var="validLabel" />
			<view:button label="${validLabel}" action="${'javascript:valid();'}" />
		</view:buttonPane></div>
	</c:if>

	<c:if test="${requestScope.enableComment}">
		<view:comments 	userId="${requestScope.userId}" componentId="${requestScope.toolId}"
						resourceType="${scheduleEventDetail.resourceType}" resourceId="${scheduleEventDetail.id}" />
	</c:if>
</view:window>
</body>
</html>