<%--

    Copyright (C) 2000 - 2011 Silverpeas

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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="org.silverpeas.resourcemanager.model.Category"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ResourceDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.Reservation"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="java.util.List" %>
<%@ page import= "java.util.Date" %>
<%@ include file="check.jsp" %>
<% 
//Recuperation des details de l'ulisateur

List listOfReservation = (List)request.getAttribute("listOfReservation");

ArrayCellText arrayCellText1;
ArrayLine arrayLine;
ArrayCellText arrayCellText2;
ArrayCellText arrayCellText3;
ArrayCellText arrayCellText4;
%>
<html>
	<head>
	<%
		out.println(gef.getLookStyleSheet());
	%>
	<script language=JavaScript>
	function seeReservation(reservationId) {
		location.href="ViewReservation?reservationId="+reservationId;	
	}
	
	function deleteReservation(reservationId, event) {
		  if (confirm("Etes vous sûr de vouloir supprimer la réservation liée à l'évènement "+event+"?")) {
			location.href="DeleteReservation?id="+reservationId;
		  }
	}
	
	</script>	
	</head>
	<body>

	<%
		browseBar.setDomainName(spaceLabel);
		browseBar.setComponentName(componentLabel,"Main");
		tabbedPane.addTab(resource.getString("resourcesManager.accueil"), "Main", false);
		tabbedPane.addTab(resource.getString("GML.categories"), "ViewCategories", false);
		tabbedPane.addTab(resource.getString("resourcesManager.Reservation"), "#", true);
		
		operationPane.addOperation(resource.getIcon("resourcesManager.createReservation"), resource.getString("resourcesManager.creerReservation"),"NewReservation");
		out.println(window.printBefore());
		out.println(tabbedPane.print());
		out.println(frame.printBefore());
		%>
		<h2>Mes Reservations</h2>
<%

ArrayPane arrayPane = gef.getArrayPane("categoryList", "ViewReservations", request, session);
arrayPane.addArrayColumn(resource.getString("resourcesManager.evenement"));
arrayPane.addArrayColumn(resource.getString("GML.dateBegin"));
arrayPane.addArrayColumn(resource.getString("GML.dateEnd"));
arrayPane.addArrayColumn(resource.getString("resourcesManager.raison"));
arrayPane.addArrayColumn(resource.getString("resourcesManager.lieu"));
ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("resourcesManager.operations"));
columnOp.setSortable(false);


while(!listOfReservation.isEmpty()){
	IconPane iconPane1 = gef.getIconPane();
	Icon editIcon = iconPane1.addIcon();
	Icon deleteIcon = iconPane1.addIcon();
	Reservation maReservation = (Reservation)listOfReservation.get(0);
	String reservationId = maReservation.getId();
	String event = maReservation.getEvent();
	String place = maReservation.getPlace();
	String reason = maReservation.getReason();
	
	String dateEnd = resource.getOutputDate(maReservation.getEndDate());
	String dateBegin = resource.getOutputDate(maReservation.getBeginDate());
	
	
	arrayLine = arrayPane.addArrayLine();
	arrayLine.addArrayCellLink(event,"ViewReservation?reservationId="+reservationId);
	arrayCellText1 = arrayLine.addArrayCellText(dateBegin);
	arrayCellText2 = arrayLine.addArrayCellText(dateEnd);
	arrayCellText3 = arrayLine.addArrayCellText(reason);
	arrayCellText4 = arrayLine.addArrayCellText(place);
	editIcon.setProperties(resource.getIcon("resourcesManager.updateCategory"), resource.getString("resourcesManager.modifierReservation"),"EditReservation?id="+reservationId);
	deleteIcon.setProperties(resource.getIcon("resourcesManager.smallDelete"), resource.getString("resourcesManager.supprimerReservation"),"javascript:deleteReservation('"+reservationId+"','"+event+"')");
	arrayLine.addArrayCellIconPane(iconPane1);
	%>
	
	<%listOfReservation.remove(0);
}
		out.println(arrayPane.print());
		out.println(frame.printAfter());
		out.println(window.printAfter());
%>

</body>
</html>