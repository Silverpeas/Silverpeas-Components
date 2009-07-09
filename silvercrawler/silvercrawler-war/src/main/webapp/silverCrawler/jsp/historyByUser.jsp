<%@ include file="check.jsp" %>
<html>
<head>
<%
	Collection downloads = (Collection) request.getAttribute("DownloadsByUser");
	String userName = (String) request.getAttribute("UserName");
	String userId = (String) request.getAttribute("UserId");
	String folderName = (String) request.getAttribute("FolderName");
	
	out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script language="JavaScript">
</script>
</head>
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "historyByUser.jsp");

Board	board		 = gef.getBoard();

out.println(window.printBefore());
out.println(frame.printBefore());
out.println(board.printBefore());
%>
<table>
<tr>
	<td>
	<td class="txtlibform" nowrap><%=resource.getString("GML.user")%> :</td>
	<td nowrap><%=userName%></td>
		
	</td>
	<%
	out.println("<tr><td>");
	if ((downloads != null) && (downloads.size() > 0))
	{
		
	    Iterator i = downloads.iterator();
	    
	    ArrayPane arrayPane = gef.getArrayPane("downloadList", "ViewHistoryByUser?UserId="+userId+"&UserName="+userName+"&FolderName="+folderName, request, session);
	
	    arrayPane.addArrayColumn(resource.getString("GML.date"));
	
	    while (i.hasNext())
	    {
	        HistoryDetail download = (HistoryDetail) i.next();
	        ArrayLine  arrayLine = arrayPane.addArrayLine();
	
	        ArrayCellText cell = null;
	    	Date dateDownload = download.getDate();
	    	String date = "";
	        if (dateDownload == null) 
	        	date = "&nbsp;";
	        else 
	        	date = resource.getOutputDateAndHour(dateDownload);
	        cell = arrayLine.addArrayCellText(date);
	        if (dateDownload != null)
	        	cell.setCompareOn(dateDownload);
	     }
	    out.println("</td></tr>");
		out.println("</table>");
	    out.println(arrayPane.print());
	}
    else
	{
		resource.getString("silverCrawler.noHistory");
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