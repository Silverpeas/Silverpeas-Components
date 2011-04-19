<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<c:set var="sessionController">Silverpeas_ScheduleEvent</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<fmt:message key="scheduleevent.js.notitle" var="scheduleEventNoTitle" />
<head>
<view:looknfeel />
<script type="text/javascript">
    function addInfoGene(){
    	var title = document.addInfoGene.title.value;
        if(title != null && title.length > 0){
        	document.addInfoGene.submit();
    	} else {
    	  alert('${scheduleEventNoTitle}');
    	}
    }
  </script>
</head>
<view:setBundle bundle="${requestScope.resources.iconsBundle}"
	var="icons" />
	
<c:set var="browseContext" value="${requestScope.browseContext}" />
<c:set var="currentScheduleEvent" value="${requestScope.currentScheduleEvent}" />

<c:set var="currentTitle" value="" />
<c:set var="currentDescription" value="" />

<c:if test="${currentScheduleEvent != null}">
<c:set var="currentTitle" value="${currentScheduleEvent.title}" />
<c:set var="currentDescription" value="${currentScheduleEvent.description}" />
</c:if>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5"
	marginheight="5" onload="document.getElementById('title').focus();">

<fmt:message key="scheduleevent.form.title.screen1" var="scheduleEventTitle" />
<view:browseBar>
	<view:browseBarElt link="" label="${scheduleEventTitle}" />
</view:browseBar>
<view:window>
	<fmt:message key="scheduleevent.form.title" var="titleLabel" />
	<fmt:message key="scheduleevent.form.description" var="descLabel" />
	<form id="addInfoGene" name="addInfoGene" method="POST"
		action="<c:url value="/Rscheduleevent/jsp/AddInfoGene"/>">
		<fmt:message key="scheduleevent.form.mandatory" var="mandatoryIconLabel" />
		<fmt:message key="scheduleevent.icons.mandatory" var="mandatoryIcon" bundle="${icons}" />
		${titleLabel}:<br/><input id="title" type="text" name="title" maxlength="254" size="100" value="${currentTitle}"/>
		<img alt="${mandatoryIconLabel}" src="${mandatoryIcon}" height="5" width="5"/>
		<br/>
		${descLabel}:<br/><textarea id="description" rows="5" cols="100" name="description">${currentDescription}</textarea>
		</form>
	<center>
		<view:buttonPane>
		<fmt:message key="scheduleevent.button.next" var="addInfoGeneLabel" />
		<fmt:message key="scheduleevent.button.cancel" var="cancelLabel" />
		<c:url var="cancelUrl" value="/Rscheduleevent/jsp/Cancel"/>
		<view:button label="${cancelLabel}" action="${cancelUrl}" />
		<view:button label="${addInfoGeneLabel}" action="${'javascript: addInfoGene();'}" /> 
		</view:buttonPane>
	</center>
</view:window>
</body>
</html>