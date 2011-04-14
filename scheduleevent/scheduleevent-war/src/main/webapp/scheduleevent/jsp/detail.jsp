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
<%@ include file="check.jsp"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<html>
  <fmt:setLocale value="${sessionScope[sessionController].language}" />
  <view:setBundle bundle="${requestScope.resources.multilangBundle}" />
  <view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
  <head>
    <view:looknfeel />
    <script type="text/javascript">
	    function deleteScheduleEvent(){
	    	if(confirm('<fmt:message key="scheduleevent.form.delete.confirm"/>')){
	    		document.utilForm.action="<c:url value="/Rscheduleevent/jsp/Delete"/>";
	    		document.utilForm.submit();
	    	}
	    }

	    function modifyState(){
	    	document.utilForm.action="<c:url value="/Rscheduleevent/jsp/ModifyState"/>";
	    	document.utilForm.submit();
	    }

	    function valid(){
	    	document.reponseForm.submit();
	    }
  </script>
  </head>
  <%
  ScheduleEventSessionController seScc = (ScheduleEventSessionController) request.getAttribute("ScheduleEvent");
  
  HashMap counter = new HashMap();
  %>
  <c:set var="userId" value="<%=seScc.getUserId()%>"/>
  <body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
  
  <fmt:message key="scheduleevent" var="scheduleEventTitle" />
  <view:browseBar>
  	<c:url value="/Rscheduleevent/jsp/Main" var="returnMain"/>
  	<view:browseBarElt link="${returnMain}" label="${scheduleEventTitle}" />
  </view:browseBar>
  <fmt:message key="scheduleevent.form.hour.pm" var="hourChoicePM" />
  <fmt:message key="scheduleevent.form.hour.am" var="hourChoiceAM" />
  
  <fmt:message key="scheduleevent.icons.open" var="openIcon" bundle="${icons}" />
  <fmt:message key="scheduleevent.icons.open.alt" var="openIconAlt" />
  <fmt:message key="scheduleevent.icons.close" var="closeIcon" bundle="${icons}" />
  <fmt:message key="scheduleevent.icons.close.alt" var="closeIconAlt" />
  <fmt:message key="scheduleevent.icons.delete" var="deleteIcon" bundle="${icons}" />
  <fmt:message key="scheduleevent.icons.delete.alt" var="deleteIconAlt" />
  
   <c:set var="scheduleEvent" value="${requestScope.scheduleEventDetail}"/>
  
  <c:if test="${scheduleEvent.author == userId}">
	  <view:operationPane>
	    <view:operation altText="${deleteIconAlt}" icon="${deleteIcon}" action="${'javascript:deleteScheduleEvent();'}" />
	    <c:if test="${scheduleEvent.status == 0}">
	    	<view:operation altText="${openIconAlt}" icon="${openIcon}" action="${'javascript:modifyState();'}" />
		</c:if>
		<c:if test="${scheduleEvent.status != 0}">
			<view:operation altText="${closeIconAlt}" icon="${closeIcon}" action="${'javascript:modifyState();'}" />
		</c:if>
	  </view:operationPane>
  </c:if>
    
  <view:window>
  	<form id="utilForm" name="utilForm" method="POST" action="">
  		<input type="hidden" name="scheduleEventId" value="${scheduleEvent.id}"/>
  	</form>
  	<form id="reponseForm" name="reponseForm" method="POST" action="<c:url value="/Rscheduleevent/jsp/ValidResponse"/>">
  		<input type="hidden" name="scheduleEventId" value="${scheduleEvent.id}"/>
  	<table id="scheduleEventDetail" class="tableArrayPane" width="98%" cellspacing="2" cellpadding="2" border="0">
  		<tr align="center">
  			<td valign="top" align="center" class="ArrayColumn"><fmt:message key="scheduleevent.column.contributor"/></td>
	  		<c:forEach items="${scheduleEvent.dates}" var="dateOption" varStatus="dateIndex">
	  			<fmt:formatDate var="currentDate" pattern="dd MMM yy" value="${dateOption.day}"/>
				<c:if test="${dateOption.hour == 8}">
					<td valign="top" align="center" class="ArrayColumn">${currentDate}&nbsp;${hourChoiceAM}</td>
				</c:if>
				<c:if test="${dateOption.hour != 8}">
					<td valign="top" align="center" class="ArrayColumn">${currentDate}&nbsp;${hourChoicePM}</td>
				</c:if>
	  		</c:forEach>
	  	</tr>
	  	<c:set var="isButtonPaneNecessary"/>
  		<c:forEach items="${scheduleEvent.contributors}" var="contributor" varStatus="contribIndex">
  			<c:set var="isName"/>
  			<c:set var="contributorId" value="${contributor.userId}"/>
  			<c:if test="${contributorId != userId}">
  				<c:forEach items="${scheduleEvent.dates}" var="date" varStatus="dateIndex">
  					<c:set var="isOk" value="false"/>
  					<c:set var="dateId" value="${date.id}"/>
  					<c:forEach items="${scheduleEvent.responses}" var="response" varStatus="responseIndex">
  						<c:if test="${response.userId == contributorId}">	
	  						<c:if test="${empty isName}">
	  							<tr>
	  							<%
	  							Contributor currentContributor = (Contributor) pageContext.getAttribute("contributor");
				      			UserDetail contributorDetail = seScc.getUserDetail(String.valueOf(currentContributor.getUserId()));
				      			%>
				      			<td valign="top" align="center" class="ArrayCell"><%=contributorDetail.getDisplayedName()%></td>
	  							<c:set var="isName" value="done"/>	
	  						</c:if>
	  						<c:if test="${response.optionId == date.id}">
	  							<c:set var="isOk" value="true"/>
	  						</c:if>
  						</c:if>
  					</c:forEach>
  					<c:if test="${not empty isName}">
	  					<c:if test="${isOk == 'true'}">
	  					<%
	  					String currentDateId = (String) pageContext.getAttribute("dateId");
	  					if(counter.get(currentDateId) == null){
	  					  counter.put(currentDateId, new Integer(1));
	  					}else{
	  					  int count = ((Integer)counter.get(currentDateId)).intValue();
	  					  count++;
	  					  counter.put(currentDateId, new Integer(count));
	  					}
	  					%>
	  					<td valign="top" align="center" class="ArrayCell" style="background-color: green">OK</td>
						</c:if>
						<c:if test="${isOk == 'false'}">
							<td valign="top" align="center" class="ArrayCell" style="background-color: red">&nbsp;</td>
						</c:if>
					</c:if>
  				</c:forEach>
  				<c:if test="${not empty isName}">
	  					</tr>
	  				</c:if>
  			</c:if>
  		</c:forEach>
  		<c:forEach items="${scheduleEvent.contributors}" var="contributor" varStatus="contribIndex">
  			<c:set var="contributorId" value="${contributor.userId}"/>
  			<c:if test="${contributorId == userId}">
  				<tr>
  					<%
	  				String currentUserId = (String) pageContext.getAttribute("userId");
	      			UserDetail user = seScc.getUserDetail(currentUserId);
	      			%>
	      			<td valign="top" align="center" class="ArrayCell"><%=user.getDisplayedName()%></td>
  					<c:set var="isButtonPaneNecessary" value="true"/>
  					<c:forEach items="${scheduleEvent.dates}" var="date" varStatus="dateIndex">
  						<c:set var="dateId" value="${date.id}"/>
	  					<fmt:formatDate pattern="ddMMyy" value="${date.day}" var="dateTmpId"/>
	  					<c:set var="isSet"/>
	  					<c:forEach items="${scheduleEvent.responses}" var="response" varStatus="responseIndex">
  							<c:if test="${response.userId == contributorId}">
  								<c:if test="${response.optionId == dateId}">
  									<c:set var="isSet" value="true"/>
  								</c:if>
  							</c:if>
	  					</c:forEach>
	  					<c:if test="${scheduleEvent.status == 0}">
	  						<c:set var="isButtonPaneNecessary" value="false"/>
	  						<c:if test="${isSet == 'true'}">
	  							<td valign="top" align="center" class="ArrayCell" style="background-color: green"><fmt:message key="scheduleevent.reponse.ok"/></td>
	  						</c:if>
	  						<c:if test="${isSet != 'true'}">
	  							<td valign="top" align="center" class="ArrayCell" style="background-color: red"><fmt:message key="scheduleevent.reponse.ko"/></td>
	  						</c:if>
	  					</c:if>
  						<c:if test="${scheduleEvent.status != 0}">
	  						<td valign="top" align="center" class="ArrayCell"><input type="checkbox" name="${dateTmpId}_${date.hour}" <c:if test="${isSet == 'true'}">checked="checked"</c:if>/></td>
	  					</c:if>
	  					<c:if test="${isSet == 'true'}">
	  						<%
		  						String currentDateId = (String) pageContext.getAttribute("dateId");
		  						if(counter.get(currentDateId) == null){
			  					  counter.put(currentDateId, new Integer(1));
			  					}else{
			  					  int count = ((Integer)counter.get(currentDateId)).intValue();
			  					  count++;
			  					  counter.put(currentDateId, new Integer(count));
			  					}
		  						%>
	  					</c:if>
	  				</c:forEach>
  				</tr>
  			</c:if>
  		</c:forEach>
  		
  		<tr>
  		<td valign="top" align="center" class="ArrayCell"><fmt:message key="scheduleevent.reponse.sum"/></td>
  		<c:forEach items="${scheduleEvent.dates}" var="date" varStatus="dateIndex">
  			<c:set var="dateId" value="${date.id}"/>
  			<td valign="top" align="center" class="ArrayCell">
			<%
			String currentDateId = (String) pageContext.getAttribute("dateId");
			if(counter.get(currentDateId) != null){%><%=((Integer)counter.get(currentDateId)).intValue()%><%}else{%>0<%}%>
			</td>	
  		</c:forEach>
  		</tr>
  		
  	</table>
  	</form>
  	
  	<c:if test="${isButtonPaneNecessary == 'true'}">
  	<center>
	  	<view:buttonPane>
			<fmt:message key="scheduleevent.button.valid" var="validLabel" />
			<view:button label="${validLabel}" action="${'javascript:valid();'}" />
		</view:buttonPane>
	</center>
  	</c:if>
  	
  </view:window>
  </body>
</html>