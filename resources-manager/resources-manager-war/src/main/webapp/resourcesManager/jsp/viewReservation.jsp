<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.CategoryDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ResourceDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ReservationDetail"%>
<%@ page import="java.util.List" %>
<%@ include file="check.jsp" %>
<% 
//Récupération des détails de l'ulisateur
List listResourcesofReservation = (List)request.getAttribute("listResourcesofReservation");
String reservationId = (String)request.getAttribute("reservationId");
ReservationDetail maReservation = (ReservationDetail)request.getAttribute("reservation");
String objectView = (String) request.getAttribute("objectView");
String event = maReservation.getEvent();
String place = maReservation.getPlace();
String reason = EncodeHelper.javaStringToHtmlParagraphe(maReservation.getReason());
String dateEnd = resource.getOutputDateAndHour(maReservation.getEndDate());
String dateBegin = resource.getOutputDateAndHour(maReservation.getBeginDate());
String minuteHourDateBegin = DateUtil.getFormattedTime(maReservation.getBeginDate());
String minuteHourDateEnd = DateUtil.getFormattedTime(maReservation.getEndDate());
String flag = (String)request.getAttribute("Profile");

Board	board		 = gef.getBoard();
Button cancelButton = gef.getFormButton(resource.getString("resourcesManager.retourListeReservation"), "Calendar?objectView="+objectView,false);
ButtonPane buttonPane = gef.getButtonPane();
buttonPane.addButton(cancelButton);

%>
<html>
	<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	<script type='text/javascript'>
	function deleteReservation(){
		if(confirm("<%=resource.getString("resourcesManager.suppressionConfirmation")%>")){
			location.href="DeleteReservation?id="+<%=reservationId%>;	
		}
	}
	
	function getResource(resourceId, objectView){
		location.href="ViewResource?resourceId="+resourceId+"&provenance=reservation&reservationId="+<%=maReservation.getId()%> + "&objectView=" + objectView;
	}

	function valideResource(resourceId, objectView) {
		if(confirm("<%=resource.getString("resourcesManager.confirmValideResource")%>")){
        location.href="ValidateResource?ResourceId=" + resourceId + "&reservationId=" + <%=reservationId%> + "&objectView=" + objectView;
        }
    }

    function refuseResource(resourceId, resourceName, objectView) {
        window.location.href = "ForRefuseResource?ResourceId=" + resourceId + "&ResourceName=" + resourceName+ "&reservationId=" + <%=reservationId%> + "&objectView=" + objectView;
    }
	</script>
	</head>
	<body id="resourcesManager">

	<%
	if ("admin".equals(flag) || ("publisher".equals(flag) && maReservation.getUserId().equals(resourcesManagerSC.getUserId())))
	{
		operationPane.addOperation(resource.getIcon("resourcesManager.updateBig"), resource.getString("resourcesManager.modifierReservation"),"EditReservation?id="+reservationId);
		operationPane.addLine();
		operationPane.addOperation(resource.getIcon("resourcesManager.basketDelete"), resource.getString("resourcesManager.supprimerReservation"),"javascript:deleteReservation();");
	}	
		browseBar.setDomainName(spaceLabel);
		browseBar.setComponentName(componentLabel,"Main");
		browseBar.setPath(resource.getString("resourcesManager.recapitulatifReservation"));
				
		out.println(window.printBefore());
		out.println(frame.printBefore());
  %>
<table width="100%">
	<tr>
		<td>
		<% out.println(board.printBefore());%>
			<TABLE CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="100%">
				<tr>
					<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.evenement"));%> :</td>
					<td width="100%"><%=event%></td>
				</tr>
				
				<tr>
				<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.dateDebutReservation"));%> :</td> 
				<td> <%=dateBegin%></td>
				</tr>
				
				<tr>
				<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.dateFinReservation"));%> :</TD>
				<td><%=dateEnd%></td>
				</tr>
				<tr>
					<td class="txtlibform" nowrap="nowrap"><%=resource.getString("resourcesManager.bookedBy") %> :</td>
					<td><%=maReservation.getUserName()%></td>
				</tr>
				<tr>
					<td class="txtlibform" nowrap="nowrap"><%=resource.getString("GML.creationDate") %> :</td>
					<td><%=resource.getOutputDateAndHour(maReservation.getCreationDate())%></td>
				</tr>
				<tr>
					<td class="txtlibform" nowrap="nowrap"><%=resource.getString("GML.updateDate") %> :</td>
					<td><%=resource.getOutputDateAndHour(maReservation.getUpdateDate())%></td>
				</tr>
				<tr>
				<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.raisonReservation"));%> :</td> 
				<td><%=reason%></TD>
				</tr>
				
				<tr>
				<TD class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.lieuReservation"));%> :</td>
				<td><%=place%></TD>
				</tr>
			</TABLE>
		<%out.println(board.printAfter());%>
		</td>
		<td valign="top">
		<% out.println(board.printBefore());%>
			<TABLE CELLPADDING="3" CELLSPACING="0" BORDER="0" WIDTH="100%">
				<tr>
					<td class="txtlibform" nowrap="nowrap"><% out.println(resource.getString("resourcesManager.resourcesReserved"));%> :</td>
					<td width="100%"><%
					for(int i=0;i<listResourcesofReservation.size();i++){
						ResourceDetail maResource = (ResourceDetail)listResourcesofReservation.get(i);
						String resourceId = maResource.getId();
						String resourceName = maResource.getName();%>
						<%
						// afficher les icônes de validation et refus si la ressource est en état à valider 
						// et si l'utilisateur est le responsable de cette ressource
						String currentUser = resourcesManagerSC.getUserId() ;
						List managers = maResource.getManagers();
						if (STATUS_FOR_VALIDATION.equals(maResource.getStatus())) { %>
             <a style="color:red" href="javascript:getResource(<%=resourceId%>, '<%=objectView%>')"><%=resourceName%></a> 
	          <% } else if (STATUS_REFUSED.equals(maResource.getStatus())) { %>
	            <a style="color:grey" href="javascript:getResource(<%=resourceId%>, '<%=objectView%>')"><%=resourceName%></a> 
	          <% } else {%>
	            <a style="color:black" href="javascript:getResource(<%=resourceId%>, '<%=objectView%>')"><%=resourceName%></a> 
	           <% } 
						if (STATUS_FOR_VALIDATION.equals(maResource.getStatus()) &&  managers != null && managers.contains(currentUser)) { %>
							<a href="javascript:valideResource(<%=resourceId%>, '<%=objectView%>')">
							<img src="<%=m_context%>/util/icons/ok.gif" align="middle" border="0" alt="<%=resource.getString("resourcesManager.valideResource")%>" title="<%=resource.getString("resourcesManager.valideResource")%>">
							</a>&nbsp;
              <a href="javascript:refuseResource(<%=resourceId%>, '<%=resourceName%>', '<%=objectView%>')">
              <img src="<%=m_context%>/util/icons/wrong.gif" align="middle" border="0" alt="<%=resource.getString("resourcesManager.refuseResource")%>" title="<%=resource.getString("resourcesManager.refuseResource")%>">
              </a>&nbsp;
						<% }  %>
						<br/>
				<% } %>
					</td>
				</tr>
			</TABLE>
			<%out.println(board.printAfter());%>
			<br/>
      <%=resource.getString("resourcesManager.explain") %>
		</td>
	</tr>
</table>

<%		out.println("<BR/><center>"+buttonPane.print()+"</center><BR/>");		
		out.println(frame.printAfter());
		out.println(window.printAfter());
%>
</body>
</html>