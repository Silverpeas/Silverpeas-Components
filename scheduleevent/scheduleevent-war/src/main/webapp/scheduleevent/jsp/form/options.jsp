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
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator"
	prefix="view"%>
<html>
<head>
<view:looknfeel />
<script type="text/javascript">
	$(document).ready(function() {
	    $("#datepicker").datepicker({
	    	onSelect: function(dateText, inst) {
	    		document.addDateForm.dateToAdd.value = dateText;
				document.addDateForm.submit();
			}
	    });
	  });

	  function deleteDate(dateId){
		  document.deleteDateForm.dateToDelete.value = dateId;
		  document.deleteDateForm.submit();
	  }
  </script>
</head>
<c:set var="sessionController">Silverpeas_ScheduleEvent</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}"
	var="icons" />
<c:set var="browseContext" value="${requestScope.browseContext}" />

<c:set var="currentScheduleEvent" value="${requestScope.currentScheduleEvent}"/>

<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5"
	marginheight="5">

<fmt:message key="scheduleevent.form.title.screen2" var="scheduleEventTitle" />
<view:browseBar>
	<view:browseBarElt link="" label="${scheduleEventTitle}" />
</view:browseBar>
<view:window>
	
	<form name="addDateForm" method="POST" action="<c:url value="/Rscheduleevent/jsp/AddDate"/>">
		<input type="hidden" name="dateToAdd"/>
	</form>
	<form name="deleteDateForm" method="POST" action="<c:url value="/Rscheduleevent/jsp/DeleteDate"/>">
		<input type="hidden" name="dateToDelete"/>
	</form>
	<center>
		<table>
		<tr valign="top">
			<td width="50%">
				<div id="datepicker"></div>
			</td>
			<td width="50%" align="center">
				<fmt:message key="scheduleevent.form.datesSelected"/>
				<br/>
				<c:if test="${not empty currentScheduleEvent.dates}">
				  	<fmt:message key="scheduleevent.dates.delete" var="deleteScheduleEventDateAlt"/>
					<fmt:message key="scheduleevent.icons.dates.delete" var="deleteScheduleEventDate" bundle="${icons}" />
					<c:forEach var="currentDate" items="${currentScheduleEvent.dates}" varStatus="lineInfo">
						<fmt:formatDate pattern="dd MMM yy" value="${currentDate.day}"></fmt:formatDate>
						<fmt:formatDate pattern="ddMMyy" value="${currentDate.day}" var="currentId"></fmt:formatDate>
						<a href="javascript:deleteDate(<c:out value="${currentId}"/>)"><img alt="${deleteScheduleEventDateAlt}" src="${deleteScheduleEventDate}"/></a>
						<c:if test="${!lineInfo.last}"><br/></c:if> 
					</c:forEach>		
				</c:if>
			</td>
		</tr>
		</table>
	</center>
	
	<center>
	<view:buttonPane>
	<fmt:message key="scheduleevent.button.cancel" var="cancelLabel" />
	<c:url var="cancelUrl" value="/Rscheduleevent/jsp/Cancel"/>
	<view:button label="${cancelLabel}" action="${cancelUrl}" />
	<c:url var="backUrl" value="/Rscheduleevent/jsp/BackInfoGene"/>
	<fmt:message key="scheduleevent.button.back" var="backToInfoGeneLabel" />
	<view:button label="${backToInfoGeneLabel}" action="${backUrl}" />
	<c:if test="${not empty currentScheduleEvent.dates}">
		<c:url var="nextUrl" value="/Rscheduleevent/jsp/AddOptionsNext"/>
		<fmt:message key="scheduleevent.button.next" var="addOptionsLabel" />
		<view:button label="${addOptionsLabel}" action="${nextUrl}" />
	</c:if>
	</view:buttonPane>
	</center>
</view:window>
</body>
</html>