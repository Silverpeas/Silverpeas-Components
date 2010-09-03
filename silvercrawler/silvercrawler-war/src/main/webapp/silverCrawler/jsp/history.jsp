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
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
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