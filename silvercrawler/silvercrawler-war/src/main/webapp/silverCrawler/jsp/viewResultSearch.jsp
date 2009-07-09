<%@ include file="check.jsp" %>

<% 
Collection docs 			= (Collection) request.getAttribute("Docs");
Collection path 			= (Collection) request.getAttribute("Path");
String userId 				= (String) request.getAttribute("UserId");
String profile 				= (String) request.getAttribute("Profile");
String word 				= (String) request.getAttribute("Word");

//création du chemin :
String 		chemin 		= "";
if (path != null)
{
	String 		namePath	= "";
	boolean 	suivant 	= false;
	Iterator 	itPath 		= (Iterator) path.iterator();
	
	while (itPath.hasNext()) 
	{
		String directory = (String) itPath.next();
		if (directory != null)
		{
			if (suivant) 
			{
				chemin = chemin + " > ";
				namePath = " > " + namePath;
			}
			chemin = chemin + "<a href=\"GoToDirectory?DirectoryPath="+ directory + "\">" + Encode.javaStringToHtmlString(directory)+"</a>";
			namePath = namePath + directory;
			suivant = itPath.hasNext();
		}
	}
}

%>


<html>
<head>
<%
	out.println(gef.getLookStyleSheet());
%>
<link rel="stylesheet" href="<%=m_context%>/util/styleSheets/modal-message.css" type="text/css">
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/ajax.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/ajax-dynamic-content.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/modalMessage/modal-message.js"></script> 
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">

var downloadWindow = window;

function viewDownloadHistory(name)
{
	url = "ViewDownloadHistoryFromResult?Name="+name;
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
		displayStaticMessage();
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
<body bgcolor="#ffffff" leftmargin="5" topmargin="5" marginwidth="5" marginheight="5">

<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setPath(chemin);

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
			<% out.println(docs.size());%><%=resource.getString("silverCrawler.nbResult")%><%=word%>
        </td>
    </tr>
    <tr class=intfdcolor4><td>&nbsp;</td></tr>
</table border=0>

<%
// affichage des fichiers, résultats de la recherche
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
	        	// allimenter l'ArrayPane avec le répertoire
	        	ArrayLine  arrayLine = arrayPane.addArrayLine();
	        	
	        	ArrayCellText cell = arrayLine.addArrayCellText("<img src=\""+resource.getIcon("silverCrawler.folder")+"\" />");
				cell.setCompareOn(FileRepositoryManager.getFileExtension(fileDetail.getName()));
				
	            /* IconPane icon = gef.getIconPane();
	    		Icon folderIcon = icon.addIcon();
	    		folderIcon.setProperties(resource.getIcon("silverCrawler.folder"), "");
	       		icon.setSpacing("30px");
	       		ArrayCellText cell = arrayLine.addArrayCellIconPane(icon); */
	        		
	            fileName = fileDetail.getName();
	            filePath = fileDetail.getPath();
	    	
	    	    arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(fileDetail.getName()), "SubDirectoryFromResult?DirectoryPath="+fileDetail.getPath());
	    	    
	    	    arrayLine.addArrayCellText("");
	    	    
	    	    if ("admin".equals(profile))
	    		{
	    	    	IconPane iconPane = gef.getIconPane();
	    	    	
	            	//icône de l'historique
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
			
				ArrayCellText cell = arrayLine.addArrayCellText("<img src=\""+fileDetail.getFileIcon()+"\" width=\"20\" height=\"20\"/>");
				cell.setCompareOn(FileRepositoryManager.getFileExtension(fileDetail.getName()));
			    
			    fileName = fileDetail.getName();
			    filePath = fileDetail.getPath();
			    
			    ArrayCellLink cellLink = arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(fileDetail.getName()), fileDetail.getFileURL(userId, componentId));
			    cellLink.setTarget("_blank");
			    
			    ArrayCellText cellSize = arrayLine.addArrayCellText(fileDetail.getFileSize());
			    cellSize.setCompareOn(new Long(fileDetail.getSize()));
			    
			    if ("admin".equals(profile))
				{
			    	IconPane iconPane = gef.getIconPane();
			    	
		        	//icône de l'historique
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
<%@ include file="modalMessage.jsp.inc" %>
</body>
</html>