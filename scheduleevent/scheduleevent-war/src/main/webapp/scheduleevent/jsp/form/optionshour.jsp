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
<html>
<head>
<view:looknfeel />
<script type="text/javascript">
	function addOptionsHour(){
    	document.addOptionsHour.submit();
    }
  </script>
</head>
<c:set var="sessionController">Silverpeas_ScheduleEvent</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}"
	var="icons" />
<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="currentScheduleEvent" value="${requestScope.currentScheduleEvent}" />

<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5"
	marginheight="5">

<fmt:message key="scheduleevent.form.title.screen3" var="scheduleEventTitle" />
<view:browseBar>
	<view:browseBarElt link="" label="${scheduleEventTitle}" />
</view:browseBar>
<view:window>
	
	<fmt:message key="scheduleevent.form.hour.pm" var="hourChoicePM" />
	<fmt:message key="scheduleevent.form.hour.am" var="hourChoiceAM" />
	<fmt:message key="scheduleevent.form.hour.ampm" var="hourChoiceAMPM" />
	<fmt:message key="scheduleevent.form.hour"/>
	<form name="addOptionsHour" method="POST" action="<c:url value="/Rscheduleevent/jsp/AddOptionsHour"/>">
	<c:if test="${currentScheduleEvent != null}">
		<c:forEach var="currentDate" items="${currentScheduleEvent.dates}">
			<c:set var="currentHour" value="${currentDate.hour}"/>
			<fmt:formatDate pattern="dd MMM yy" value="${currentDate.day}"></fmt:formatDate>
			<fmt:formatDate pattern="ddMMyy" value="${currentDate.day}" var="dateTmpId"></fmt:formatDate>
			<c:if test="${empty currentHour}">
				<input type="radio" name="hourFor${dateTmpId}" value="8" checked>${hourChoiceAM}&nbsp;
				<input type="radio" name="hourFor${dateTmpId}" value="14">${hourChoicePM}
				<input type="radio" name="hourFor${dateTmpId}" value="25">${hourChoiceAMPM}
			</c:if>
			<c:if test="${not empty currentHour}">
				<c:if test="${currentHour == 8}">
					<input type="radio" name="hourFor${dateTmpId}" value="8" checked>${hourChoiceAM}&nbsp;
					<input type="radio" name="hourFor${dateTmpId}" value="14">${hourChoicePM}&nbsp;
					<input type="radio" name="hourFor${dateTmpId}" value="25">${hourChoiceAMPM}
				</c:if>
				<c:if test="${currentHour == 14}">
					<input type="radio" name="hourFor${dateTmpId}" value="8">${hourChoiceAM}&nbsp;
					<input type="radio" name="hourFor${dateTmpId}" value="14" checked>${hourChoicePM}&nbsp;
					<input type="radio" name="hourFor${dateTmpId}" value="25">${hourChoiceAMPM}
				</c:if>
				<c:if test="${currentHour != 8 && currentHour != 14}">
					<input type="radio" name="hourFor${dateTmpId}" value="8">${hourChoiceAM}&nbsp;
					<input type="radio" name="hourFor${dateTmpId}" value="14">${hourChoicePM}&nbsp;
					<input type="radio" name="hourFor${dateTmpId}" value="25" checked>${hourChoiceAMPM}
				</c:if>
			</c:if>
			<br/>
		</c:forEach>		
	</c:if>	
	</form>
	
	<center>
	<view:buttonPane>
	<fmt:message key="scheduleevent.button.cancel" var="cancelLabel" />
	<c:url var="cancelUrl" value="/Rscheduleevent/jsp/Cancel"/>
	<view:button label="${cancelLabel}" action="${cancelUrl}" />
	<c:url var="backUrl" value="/Rscheduleevent/jsp/BackDate"/>
	<fmt:message key="scheduleevent.button.back" var="backToDateLabel" />
	<view:button label="${backToDateLabel}" action="${backUrl}" />
	<fmt:message key="scheduleevent.button.next" var="addOptionsHourLabel" />
	<view:button label="${addOptionsHourLabel}" action="${'javascript: addOptionsHour();'}" />
	</view:buttonPane>
	</center>
</view:window>
</body>
</html>