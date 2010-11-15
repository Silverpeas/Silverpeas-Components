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
<c:url var="animationUrl" value="/util/javaScript/animation.js"/>
<c:url var="openUserPopupUrl" value="/Rscheduleevent/jsp/OpenUserPopup"/>
<c:url var="backPopupUrl" value="/Rscheduleevent/jsp/ConfirmScreen"/>
<c:url var="confirmUrl" value="/Rscheduleevent/jsp/Confirm"/>
<script type="text/javascript" src="${animationUrl}"></script>
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

<fmt:message key="scheduleevent.form.title.screen4" var="scheduleEventTitle" />
<view:browseBar>
	<view:browseBarElt link="" label="${scheduleEventTitle}" />
</view:browseBar>

<fmt:message key="scheduleevent.form.addcontributors.alt" var="addContribAltText" />
<fmt:message key="scheduleevent.icons.add" var="addIconPath" bundle="${icons}" />
  
<view:operationPane>
    <view:operation altText="${addContribAltText}" icon="${addIconPath}" action="${'javascript: setUsers();'}" />
  </view:operationPane>
<view:window>
	
	<table id="generalInfos" class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
		<tr align="center">
  			<td valign="top" align="center" class="ArrayColumn"><fmt:message key="scheduleevent.column.title"/></td>
  			<td valign="top" align="center" class="ArrayColumn"><fmt:message key="scheduleevent.column.description"/></td>
  		</tr>
  		<tr align="left">
  			<td valign="top" align="center" class="ArrayCell">${currentScheduleEvent.title}</td>
  			<td valign="top" align="center" class="ArrayCell">${currentScheduleEvent.description}</td>
  		</tr>
  	</table>
	<br/>
	<table id="contributorsInfos" class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
	<tr align="center">
  		<td valign="top" align="center" class="ArrayColumn"><fmt:message key="scheduleevent.form.listcontributors"/></td>
	</tr>
  	<tr align="left">
  		<td valign="top" align="center" class="ArrayCell">
			<c:if test="${not empty currentScheduleEvent.contributors}">
				<c:forEach var="currentContributor" items="${currentScheduleEvent.contributors}" varStatus="lineInfo">
					<c:out value="${currentContributor.userName}"></c:out>
					<c:if test="${!lineInfo.last}"><br/></c:if> 
				</c:forEach>
			</c:if>
		</td>
	</tr>
	</table>
	<form name="confirmForm" method="POST" action="">
	</form>
	
	<center>
	<view:buttonPane>
	<fmt:message key="scheduleevent.button.cancel" var="cancelLabel" />
	<c:url var="cancelUrl" value="/Rscheduleevent/jsp/Cancel"/>
	<view:button label="${cancelLabel}" action="${cancelUrl}" />
	<c:url var="backUrl" value="/Rscheduleevent/jsp/BackHour"/>
	<fmt:message key="scheduleevent.button.back" var="backToDateLabel" />
	<view:button label="${backToDateLabel}" action="${backUrl}" />
	<c:if test="${not empty currentScheduleEvent.contributors}">
		<fmt:message key="scheduleevent.button.finish" var="confirmLabel" />
		<view:button label="${confirmLabel}" action="${'javascript: confirmEvent();'}" />
	</c:if>
	</view:buttonPane>
	</center>
</view:window>
</body>
</html>