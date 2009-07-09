<%@ include file="check.jsp" %>
<html>
<head>
<%
	Collection downloads = (Collection) request.getAttribute("Downloads");
	String name = (String) request.getAttribute("Name");
 
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">

var userWindow = window;

function editDetail(userId, folderName, userName)
{
	url = "ViewHistoryByUser?FolderName="+folderName+"&UserId="+userId+"&UserName="+userName;
    windowName = "userWindow";
	larg = "300";
	haut = "400";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!userWindow.closed && userWindow.name== "exportWindow")
    	userWindow.close();
    userWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "history.jsp");

Board	board		 = gef.getBoard();

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table>
<tr>
	<td class="txtlibform" nowrap><%=resource.getString("silverCrawler.nameHistory")%> :</td>
	<td nowrap><%=name%></td>
</tr>		
<%
	out.println("<tr><td>");
	if (downloads.size() > 0)
	{
	    Iterator i = downloads.iterator();
	    
	    ArrayPane arrayPane = gef.getArrayPane("downloadList", "ViewDownloadHistory?Name="+name, request, session);
	    arrayPane.addArrayColumn(resource.getString("GML.user"));
	    arrayPane.addArrayColumn(resource.getString("silverCrawler.lastDownload"));
	    arrayPane.addArrayColumn(resource.getString("silverCrawler.nbDownload"));
	    arrayPane.addArrayColumn(resource.getString("silverCrawler.detail"));
	
	    while (i.hasNext())
	    {
	    	HistoryByUser historyByUser = (HistoryByUser) i.next();
	        ArrayLine  ligne = arrayPane.addArrayLine();
	
	        // colonne User
	        String actorName = historyByUser.getUser().getLastName() + " " + historyByUser.getUser().getFirstName();
	    	ligne.addArrayCellText(actorName);
	        
	        // colonne lastDownload
	        ArrayCellText cell = null;
	    	Date dateDownload = historyByUser.getLastDownload();
	    	String date = "";
	        if (dateDownload == null) 
	        	date = "&nbsp;";
	        else 
	        	date = resource.getOutputDateAndHour(dateDownload);
	        cell = ligne.addArrayCellText(date);
	        if (dateDownload != null)
	        	cell.setCompareOn(dateDownload);
	        
	        // colonne Nbdownload
	        int nbDownload = historyByUser.getNbDownload();
	        ligne.addArrayCellText(nbDownload);
	        
	        // colonne des icones pour le détail
	        String historyUserId = historyByUser.getUser().getId();
	        IconPane iconPane = gef.getIconPane();
			Icon detailIcon = iconPane.addIcon();
	        detailIcon.setProperties(resource.getIcon("silverCrawler.info"), resource.getString("silverCrawler.detail"), "javascript:editDetail('"+historyUserId+"','"+Encode.javaStringToJsString(name)+"','"+actorName+"')");
	   		ligne.addArrayCellIconPane(iconPane);
	     }
	    out.println("</td></tr>");
		out.println("</table>");
	    out.println(arrayPane.print());
	    
	}
	else
	{
		String message = resource.getString("silverCrawler.noHistory");
		out.println(message);
		out.println("</td></tr>");
		out.println("</table>");
	}
	
	out.println(board.printAfter());
	out.println(frame.printMiddle());
	ButtonPane buttonPane = gef.getButtonPane();
	Button button = (Button) gef.getFormButton(resource.getString("GML.close"), "javaScript:window.close();", false);
	buttonPane.addButton(button);
	out.println("<BR><center>"+buttonPane.print()+"</center><BR>");
	out.println(frame.printAfter());
	out.println(window.printAfter());
	%>

</body>
</html>