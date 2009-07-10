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
String event = maReservation.getEvent();
String place = maReservation.getPlace();
String reason = Encode.javaStringToHtmlParagraphe(maReservation.getReason());
String dateEnd = resource.getOutputDateAndHour(maReservation.getEndDate());
String dateBegin = resource.getOutputDateAndHour(maReservation.getBeginDate());
String minuteHourDateBegin = DateUtil.getFormattedTime(maReservation.getBeginDate());
String minuteHourDateEnd = DateUtil.getFormattedTime(maReservation.getEndDate());
String flag = (String)request.getAttribute("Profile");

Board	board		 = gef.getBoard();
Button cancelButton = gef.getFormButton(resource.getString("resourcesManager.retourListeReservation"), "Calendar",false);
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
	
	function getResource(resourceId){
		location.href="ViewResource?resourceId="+resourceId+"&provenance=reservation&reservationId="+<%=maReservation.getId()%>;
	}
	</script>
	</head>
	<body id="resourcesManager">

	<%
	if (!flag.equals("user")){
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
						ResourceDetail maResource = (ResourceDetail)listResourcesofReservation.get(i);%>
						<a onClick="getResource(<%=maResource.getId()%>)" style="cursor: pointer;"><%=maResource.getName()%></a><br/>
					<%}
				%>
					</td>
				</tr>
			</TABLE>
			<%out.println(board.printAfter());%>
		</td>
	</tr>
</table>

<%		out.println("<BR/><center>"+buttonPane.print()+"</center><BR/>");		
		out.println(frame.printAfter());
		out.println(window.printAfter());
%>
</body>
</html>