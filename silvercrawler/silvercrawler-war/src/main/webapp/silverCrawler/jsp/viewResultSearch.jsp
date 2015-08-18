<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="org.silverpeas.util.EncodeHelper"%>
<%@page import="java.net.URLEncoder"%>
<%@ page import="org.silverpeas.util.FileRepositoryManager" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="check.jsp" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ taglib tagdir="/WEB-INF/tags/silverpeas/silverCrawler" prefix="silverCrawler" %>
<% 
Collection docs 			= (Collection) request.getAttribute("Docs");
String profile 				= (String) request.getAttribute("Profile");
String word 				= (String) request.getAttribute("Word");
%>


<html>
<head>
<view:looknfeel/>
  <script type="text/javascript" src="<c:url value="/util/javaScript/checkForm.js"/>"></script>
<script type="text/javascript">

var downloadWindow = window;

function viewDownloadHistory(name)
{
	url = "ViewDownloadHistoryFromResult?Name="+encodeURIComponent(name);
    windowName = "downloadWindow";
	larg = "650";
	haut = "350";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!downloadWindow.closed && downloadWindow.name== "exportWindow")
    	downloadWindow.close();
    downloadWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function sendData() 
{
	var query = stripInitialWhitespace(document.searchForm.WordSearch.value);
	if (!isWhitespace(query) && query != "*") {
		$.progressMessage();
		setTimeout("document.searchForm.submit();", 500);
    }
}

function checkSubmitToSearch(ev)
{
	var touche = ev.keyCode;
	if (touche == 13)
		sendData();
}
</script>

</head>
<body>
<silverCrawler:browseBar navigationAuthorized="true" />

<%

out.println(window.printBefore());
out.println(frame.printBefore());

Board	board		 = gef.getBoard();

out.println(board.printBefore());

// affichage de la zone de recherche
// ---------------------------------
Button validateButton 	= (Button) gef.getFormButton("OK", "javascript:onClick=sendData();", false);
%>
<center>
<table border="0" cellpadding="0" cellspacing="0">
	<form Name="searchForm" action="Search" Method="POST" onSubmit="sendData()">
		<tr>
			<td valign="middle" align="left" class="txtlibform" width="30%"><%=resource.getString("GML.search")%></td>
			<td align="left" valign="middle">
				<table border="0" cellspacing="0" cellpadding="0">
					<tr valign="middle">
						<td valign="middle"><input type="text" name="WordSearch" size="36" onkeydown="checkSubmitToSearch(event)"></td>
						<td valign="middle">&nbsp;</td>
						<td valign="middle" align="left" width="100%"><% out.println(validateButton.print());%></td>
					</tr>
				</table>
			</td>
		</tr>
	</form>
</table>
</center>
<%
out.println(board.printAfter());
out.println("<br>");

%>

<table align="center" border="0" cellspacing="0" cellpadding="0" width="98%">
	<tr valign="middle" class="intfdcolor">
		<td align="center" class="ArrayNavigation">
			<% out.println(docs.size());%> <%=resource.getString("silverCrawler.nbResult")%> <%=word%>
        </td>
    </tr>
    <tr class=intfdcolor4><td>&nbsp;</td></tr>
</table border=0>

<%
// affichage des fichiers, r�sultats de la recherche
// -------------------------------------------------

	if (docs != null && docs.size() > 0)
	{
		Iterator iterator = docs.iterator();
			
	    // liste des fichiers
	    ArrayPane arrayPane = gef.getArrayPane("docs", "ViewResult?WordSearch="+word, request, session);
	    ArrayColumn columnType = arrayPane.addArrayColumn(resource.getString("GML.type"));
	    columnType.setWidth("40px");
	    ArrayColumn columnName = arrayPane.addArrayColumn(resource.getString("GML.name"));
	    columnName.setWidth("550px");
	    ArrayColumn columnSize = arrayPane.addArrayColumn(resource.getString("GML.size"));
	    columnSize.setWidth("60px");
	    
	    if ("admin".equals(profile))
	    {
	    	ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("silverCrawler.operation"));
	    	columnOp.setSortable(false);
	    }
		
		FileDetail fileDetail = null;
		String fileName = "";
		String filePath = "";
		
		while (iterator.hasNext()) 
	    {
	        fileDetail = (FileDetail) iterator.next();
	
	        if (fileDetail.isIsDirectory())
	        {
	        	// allimenter l'ArrayPane avec le r�pertoire
	        	ArrayLine  arrayLine = arrayPane.addArrayLine();
	        	
	        	ArrayCellText cell = arrayLine.addArrayCellText("<img src=\""+resource.getIcon("silverCrawler.folder")+"\" />");
				cell.setCompareOn(FileRepositoryManager.getFileExtension(fileDetail.getName()));
					        		
	            fileName = fileDetail.getName();
	            filePath = fileDetail.getPath();
	    	
	    	    arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(fileDetail.getName()), "SubDirectoryFromResult?DirectoryPath="+URLEncoder.encode(fileDetail.getPath(), "UTF-8"));
	    	    
	    	    arrayLine.addArrayCellText("");
	    	    
	    	    if ("admin".equals(profile))
	    		{
	    	    	IconPane iconPane = gef.getIconPane();
	    	    	
	            	//ic�ne de l'historique
	        	   	Icon historyIcon = iconPane.addIcon();
	        	   	historyIcon.setProperties(resource.getIcon("silverCrawler.viewHistory"), resource.getString("silverCrawler.downloadHistory"), "javaScript:viewDownloadHistory('"+Encode.javaStringToJsString(filePath)+"')");
	        	   	iconPane.setSpacing("20px");
	        	   	
	        	   	arrayLine.addArrayCellIconPane(iconPane);
	    		}
	        }
	        else
	        {
	        	// allimenter l'arrayPane avec le fichier
				ArrayLine  arrayLine = arrayPane.addArrayLine();
			
				ArrayCellText cell = arrayLine.addArrayCellText("<img src=\""+fileDetail.getFileIcon()+"\"/>");
				cell.setCompareOn(FileRepositoryManager.getFileExtension(fileDetail.getName()));
			    
			    fileName = fileDetail.getName();
			    filePath = fileDetail.getPath();
			    
			    ArrayCellLink cellLink = arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(fileDetail.getName()), fileDetail.getFileURL(
              componentId));
			    cellLink.setTarget("_blank");
			    
			    ArrayCellText cellSize = arrayLine.addArrayCellText(fileDetail.getFileSize());
			    cellSize.setCompareOn(new Long(fileDetail.getSize()));
			    
			    if ("admin".equals(profile))
				{
			    	IconPane iconPane = gef.getIconPane();
			    	
		        	//ic�ne de l'historique
		    	   	Icon historyIcon = iconPane.addIcon();
		    	   	historyIcon.setProperties(resource.getIcon("silverCrawler.viewHistory"), resource.getString("silverCrawler.downloadHistory"), "javaScript:viewDownloadHistory('"+Encode.javaStringToJsString(filePath)+"')");
		    	   	iconPane.setSpacing("20px");
		    	   	
		    	   	arrayLine.addArrayCellIconPane(iconPane);
				}
	        }
		 }
		out.println(arrayPane.print());
	}


out.println(frame.printAfter());
out.println(window.printAfter());

%>
<FORM name="folderDetailForm" action="viewDirectory" method=post >
<input type="hidden" name="FolderName">
</FORM>
<view:progressMessage/>
</body>
</html>