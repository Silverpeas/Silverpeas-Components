<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.CategoryDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ResourceDetail"%>
<%@ page import="com.silverpeas.resourcesmanager.model.ReservationDetail"%>
<%@ page import="com.stratelia.webactiv.util.DateUtil"%>
<%@ page import="java.util.List" %>
<%@ page import= "java.util.Date" %>
<%@ include file="check.jsp" %>
<% 
//Récupération des détails de l'ulisateur

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
		  if (confirm("Etes vous sûr de vouloir supprimer la réservation liée à l'événement "+event+"?")) {
			location.href="DeleteReservation?id="+reservationId;
		  }
	}
	
	</script>	
	</head>
	<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

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


while(listOfReservation.isEmpty() == false){
	IconPane iconPane1 = gef.getIconPane();
	Icon editIcon = iconPane1.addIcon();
	Icon deleteIcon = iconPane1.addIcon();
	ReservationDetail maReservation = (ReservationDetail)listOfReservation.get(0);
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