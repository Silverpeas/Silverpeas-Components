<%@ include file="check.jsp" %>

<%
	// récupération des paramètres :
	String 			userId 				= (String) request.getAttribute("UserId");
	String 			profile 			= (String) request.getAttribute("Profile");
	List 			orders				= (List) request.getAttribute("Orders");
	int 			nbOrdersProcess		= ((Integer) request.getAttribute("NbOrdersProcess")).intValue();
	
	// déclaration des variables :
	

	// création du chemin :
	String 		chemin 		= "";
	String 		namePath	= "";
	boolean 	suivant 	= false;
%>

<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="javascript">
</script>
<script src="<%=m_context%>/gallery/jsp/javaScript/createDragAndDrop.js" language="JScript"></script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
	// création de la barre de navigation
	browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "Main");
	chemin = "<a href=\"OrderViewList\">" + resource.getString("gallery.viewOrderList")+"</a>";
	browseBar.setPath(chemin);
	
	
	out.println(window.printBefore());
    out.println(frame.printBefore());
 	
	
	// afficher les demandes
	// ---------------------
	
	if ("admin".equals(profile))
	{
		Board board	= gef.getBoard();
		board.printBefore();
		int nbOrders = orders.size();
		int nbOrdersWait = nbOrders - nbOrdersProcess;
	    %>
		<table border="0" width="50%">
			<tr>
				<td class="txtlibform" nowrap><%=resource.getString("gallery.nbOrders")%> :</td>
				<td><%=nbOrders%></td>
			</tr>
			<tr>
				<td class="txtlibform" nowrap><%=resource.getString("gallery.nbOrdersProcess")%> :</td>
				<td><%=nbOrdersProcess%></td>
			</tr>
			<tr>
				<td class="txtlibform" nowrap><%=resource.getString("gallery.nbOrdersWait")%> :</td>
				<td><%=nbOrdersWait%></td>
			</tr>
		</table>
		<%
		board.printAfter();
	}

	if (orders != null)
	{
	ArrayPane arrayPane = gef.getArrayPane("orderList", "OrderViewList", request, session);
	Iterator it = (Iterator) orders.iterator();
	if (it.hasNext())
	{
		arrayPane.addArrayColumn(resource.getString("gallery.descriptionOrder"));
		
		if ("admin".equals(profile))
			arrayPane.addArrayColumn(resource.getString("gallery.orderOf"));
		
		arrayPane.addArrayColumn(resource.getString("gallery.orderDate"));
		ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("gallery.nbRows"));
		columnOp.setSortable(false);
		arrayPane.addArrayColumn(resource.getString("GML.status"));
		
		//arrayPane.setColumnToSort(1);
	}
	
	while (it.hasNext()) 
	{
		ArrayLine ligne = arrayPane.addArrayLine();
		
		Order oneOrder = (Order) it.next();
		int orderId = oneOrder.getOrderId();
		
		ligne.addArrayCellText("<a href=\"OrderView?OrderId="+orderId+"\">"+orderId+"</a>");
		
		String userName = oneOrder.getUserName();
		String processDate = resource.getOutputDateAndHour(oneOrder.getProcessDate());
		if ("admin".equals(profile))
			ligne.addArrayCellText("<a href=\"OrderView?OrderId="+orderId+"\">"+userName+"</a>");
		
		String date = resource.getOutputDateAndHour(oneOrder.getCreationDate());
		ArrayCellText arrayCellText0 = ligne.addArrayCellText("<a href=\"OrderView?OrderId="+orderId+"\">"+date+"</a>");
	            arrayCellText0.setCompareOn(date);
	            
	    ligne.addArrayCellText(oneOrder.getNbRows());
	    
	    if (!processDate.equals(""))
	    	ligne.addArrayCellText(resource.getString("gallery.processDate") + processDate);
	    else
	    	ligne.addArrayCellText(resource.getString("gallery.wait"));
	}
	out.println(arrayPane.print());
	}

	out.println(frame.printAfter());
	out.println(window.printAfter());
%>
	
  	
  	

</body>
</html>