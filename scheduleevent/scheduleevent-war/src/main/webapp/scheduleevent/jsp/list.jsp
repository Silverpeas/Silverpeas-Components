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
<%@ include file="check.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<fmt:setLocale value="${sessionScope[sessionController].language}" />
<%@ include file="form/dateFormat.jspf"%>
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />

<%
ScheduleEventSessionController seScc = (ScheduleEventSessionController) request.getAttribute("ScheduleEvent");
%>
<c:set var="userId" value="<%=seScc.getUserId()%>"/>
  
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <view:looknfeel />
    <script type="text/javascript">
	    function addScheduleEvent(){
	    	document.add.submit();
	    }

	    function deleteScheduleEvent(id){
	    	if(confirm('<fmt:message key="scheduleevent.form.delete.confirm"/>')){
	        	document.utilForm.action="<c:url value="/Rscheduleevent/jsp/Delete"/>";
	    		document.utilForm.scheduleEventId.value=id;
	    		document.utilForm.submit();
	    	}
	    }

	    function modifyState(id){
	    	document.utilForm.action="<c:url value="/Rscheduleevent/jsp/ModifyState"/>";
	    	document.utilForm.scheduleEventId.value=id;
	    	document.utilForm.submit();
	    }

	    function getDetail(id){
	      	document.utilForm.action="<c:url value="/Rscheduleevent/jsp/Detail"/>";
	    	document.utilForm.scheduleEventId.value=id;
	    	document.utilForm.submit();
	    }
  </script>
  </head>
  <body>
  
  <fmt:message key="scheduleevent" var="scheduleEventTitle" />
  <view:browseBar>
  	<view:browseBarElt link="" label="${scheduleEventTitle}" />
  </view:browseBar>
  
  <fmt:message key="scheduleevent.icons.add.alt" var="addScheduleEventAlt" />
  <fmt:message key="scheduleevent.icons.add" var="addScheduleEventIconPath" bundle="${icons}" />
  <view:operationPane>
    <view:operationOfCreation altText="${addScheduleEventAlt}" icon="${addScheduleEventIconPath}" action="${'javascript: addScheduleEvent();'}" />
  </view:operationPane>
  
  <fmt:message key="scheduleevent.icons.open" var="openIcon" bundle="${icons}" />
  <fmt:message key="scheduleevent.icons.open.alt" var="openIconAlt" />
  <fmt:message key="scheduleevent.icons.close" var="closeIcon" bundle="${icons}" />
  <fmt:message key="scheduleevent.icons.close.alt" var="closeIconAlt" />
  <fmt:message key="scheduleevent.icons.delete" var="deleteIcon" bundle="${icons}" />
  <fmt:message key="scheduleevent.icons.delete.alt" var="deleteIconAlt" />
  <fmt:message key="scheduleevent.icons.closed" var="closedIcon" bundle="${icons}" />
  <fmt:message key="scheduleevent.icons.closed.alt" var="closedIconAlt" />
  <fmt:message key="scheduleevent.icons.link" var="linkIcon" bundle="${icons}" />
  <fmt:message key="scheduleevent.icons.link.alt" var="linkIconAlt" />
  			
  <view:window>
  	<form id="add" name="add" method="post" action="<c:url value="/Rscheduleevent/jsp/Add"/>">
  	</form>
  	<form id="utilForm" name="utilForm" method="post">
  		<input type="hidden" name="scheduleEventId"/>
  	</form>
  	<view:areaOfOperationOfCreation/>
  	<table id="scheduleEvents" class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
  		<tr align="center">
  			<td valign="top" align="center" class="ArrayColumn"><fmt:message key="scheduleevent.column.title"/></td>
  			<td valign="top" align="center" class="ArrayColumn"><fmt:message key="scheduleevent.column.creationDate"/></td>
  			<td valign="top" align="center" class="ArrayColumn"><fmt:message key="scheduleevent.column.author"/></td>
  			<td valign="top" align="center" class="ArrayColumn"><fmt:message key="scheduleevent.column.utils"/></td>
  		</tr>
  		<c:if test="${not empty requestScope.scheduleEventList}">
    		<c:forEach items="${requestScope.scheduleEventList}" var="event" varStatus="eventIndex">
      		<tr align="center">
      			<td valign="top" align="center" class="ArrayCell"><a href="javascript:getDetail('${event.id}');">${event.title}</a>&nbsp;<a href="<c:url value="/ScheduleEvent/${event.id}"/>"><img src="${linkIcon}" border="0" align="bottom" alt="${linkIconAlt}" title="${linkIconAlt}"/></a><c:if test="${event.status == 0}">&nbsp;<img alt="${closedIconAlt}" title="${closedIconAlt}" src="${closedIcon}" height="15" width="15"/></c:if></td>
      			<td valign="top" align="center" class="ArrayCell"><view:formatDate value="${event.creationDate}" /></td>
      			<%
      			ScheduleEvent currentSe = (ScheduleEvent) pageContext.getAttribute("event");
      			%>
      			<td valign="top" align="center" class="ArrayCell"><view:username userId="<%=String.valueOf(currentSe.getAuthor())%>"/></td>
      			<td valign="top" align="center" class="ArrayCell">
        			<c:if test="${event.author == userId}">
	        			<c:if test="${event.status == 0}">
	        				<a href="javascript:modifyState('${event.id}')" title="${openIconAlt}"><img alt="${openIconAlt}" src="${openIcon}" height="15" width="15"/></a>
	        			</c:if>
	        			<c:if test="${event.status != 0}">
	        				<a href="javascript:modifyState('${event.id}')" title="${closeIconAlt}"><img alt="${closeIconAlt}" src="${closeIcon}" height="15" width="15"/></a>
	        			</c:if>
	        			&nbsp;&nbsp;<a href="javascript:deleteScheduleEvent('${event.id}')" title="${deleteIconAlt}"><img alt="${deleteIconAlt}" src="${deleteIcon}" height="15" width="15"/></a>
        			</c:if>
            	</td>
      		</tr>
    		</c:forEach>
  		</c:if>
  	</table>  	
  </view:window>
  </body>
</html>