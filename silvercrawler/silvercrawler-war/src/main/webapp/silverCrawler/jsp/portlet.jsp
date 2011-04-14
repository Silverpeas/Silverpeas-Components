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
<%@ include file="check.jsp" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<% 
FileFolder 	folder 			= (FileFolder) request.getAttribute("Folder");
String 		profile 		= (String) request.getAttribute("Profile");
String 		userId 			= (String) request.getAttribute("UserId");
Boolean 	isDownload 		= (Boolean) request.getAttribute("IsDownload");
Collection 	path 			= (Collection) request.getAttribute("Path");
Boolean 	isRootPathB 	= (Boolean) request.getAttribute("IsRootPath");
Boolean 	isAllowedNav 	= (Boolean) request.getAttribute("IsAllowedNav");
String		rootPath		= (String) request.getAttribute("RootPath");

boolean download 	= isDownload.booleanValue();
boolean isRootPath 	= isRootPathB.booleanValue();
boolean allowedNav 	= isAllowedNav.booleanValue();

boolean nav = true;
if ("user".equals(profile) && !allowedNav)
	nav = false;

//cr�ation du chemin :
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
			if (nav)
				chemin = chemin + "<a href=\"GoToDirectory?DirectoryPath="+ directory + "\">" + Encode.javaStringToHtmlString(directory)+"</a>";
			else
				chemin = chemin + Encode.javaStringToHtmlString(directory);
				
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

<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script language="javascript">

var downloadWindow = window;

function downloadFolder(folderName)
{
	url = "DownloadFolder?FolderName="+folderName;
    windowName = "downloadWindow";
	larg = "650";
	haut = "200";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    if (!downloadWindow.closed && downloadWindow.name== "exportWindow")
    	downloadWindow.close();
    downloadWindow = SP_openWindow(url, windowName, larg, haut, windowParams);
}

function viewDownloadHistory(name)
{
	url = "ViewDownloadHistory?Name="+name;
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
		setTimeout("javascrip:goToResult('"+query+"');", 500);
    }
}


function goToDirectory(path) {
    document.directoryForm.DirectoryPath.value = path;
    document.directoryForm.submit();
}

function goToResult(query) {
    document.resultForm.WordSearch.value = query;
    document.resultForm.submit();
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

// mettre les op�ration si on est � la racine
String name = folder.getName();

out.println(frame.printBefore());

Board board	= gef.getBoard();

out.println(board.printBefore());
	
// affichage de la zone de recherche
// ---------------------------------
Button validateButton 	= (Button) gef.getFormButton("OK", "javascript:onClick=sendData();", false);
%>
<center>
	<table border="0" cellpadding="0" cellspacing="0">
		<form name="searchForm" action="Search" method="POST">
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


// remplissage de l'ArrayPane avec la liste des sous r�pertoires
// -------------------------------------------------------------
if (nav || (!nav && !isRootPath))
{
	Collection files = folder.getFolders();
	if (files != null && files.size() > 0)
	{
	    Iterator i = files.iterator();
	    String   fileName = "";
	    String link = "";
	    
	    ArrayPane arrayPane = gef.getArrayPane("folderList", "ViewDirectory", request, session);
	
	    ArrayColumn columnType = arrayPane.addArrayColumn(resource.getString("GML.type"));
	    columnType.setWidth("40px");
	    ArrayColumn columnName = arrayPane.addArrayColumn(resource.getString("GML.name"));
	    columnName.setWidth("615px");
	    
	    if (download || "admin".equals(profile))
	    {
	    	ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("silverCrawler.operation"));
	    	columnOp.setSortable(false);
	    }
	
	    while (i.hasNext())
	    {
	        FileDetail file = (FileDetail) i.next();
	        ArrayLine  arrayLine = arrayPane.addArrayLine();
	
	        // icone du dossier
	        IconPane icon = gef.getIconPane();
			Icon folderIcon = icon.addIcon();
			folderIcon.setProperties(resource.getIcon("silverCrawler.folder"), "");
	   		icon.setSpacing("30px");
	   		arrayLine.addArrayCellIconPane(icon);
	        
	        fileName = file.getName();
	
	        String nameCell = "";
	        
	        if (nav)
	        {
	        	//arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(fileName), "SubDirectory?DirectoryPath="+fileName);
	        	//nameCell = "<a href=\"SubDirectory?DirectoryPath="+fileName + "\">" + Encode.javaStringToHtmlString(fileName)+"</a>";
	        	nameCell = "<a href=\"javascript:onClick=goToDirectory('"+fileName+"')\">" + Encode.javaStringToHtmlString(fileName)+"</a>";
	        }
	        else
	        {
	        	nameCell = Encode.javaStringToHtmlString(fileName);
	        }
	        //  permalien
	        //link = URLManager.getApplicationURL() + "/SubDir/" + Encode.javaStringToHtmlString(fileName)+"?ComponentId="+componentId;
	        String filePath = file.getPath();
	        filePath = filePath.substring(rootPath.length()+1);
	        link = URLManager.getApplicationURL() + "/SubDir/" + componentId +"?Path="+filePath;
	        nameCell = nameCell + "&nbsp;<a href=\"" + link + "\">"+ "<img border=\"0\" src=\""+resource.getIcon("silverCrawler.permalien")+"\">" + "</a>";
	        
	        // affichage de la cellule
	        arrayLine.addArrayCellText(nameCell);
	        
			// cr�ation de la colonne des ic�nes
	        IconPane iconPane = gef.getIconPane();
		   	if (download)
		   	{
		   		// ic�ne "t�l�charger le r�pertoire"
		   		Icon downloadIcon = iconPane.addIcon();
		   		downloadIcon.setProperties(resource.getIcon("silverCrawler.download"), resource.getString("silverCrawler.download"), "javaScript:downloadFolder('"+Encode.javaStringToJsString(fileName)+"')");
		   		iconPane.setSpacing("20px");
		   	}
			   		
		   	arrayLine.addArrayCellIconPane(iconPane);
	     }
	    out.println(arrayPane.print());
	}


	//affichage des fichiers
	//----------------------
	
	Collection fileList = folder.getFiles();
	if (fileList != null && fileList.size() > 0)
	{
	    Iterator itFile = fileList.iterator();
			
	    ArrayPane arrayPane = gef.getArrayPane("fileList", "ViewDirectory", request, session);
	
	    ArrayColumn columnType = arrayPane.addArrayColumn(resource.getString("GML.type"));
	    columnType.setWidth("40px");
	    ArrayColumn columnName = arrayPane.addArrayColumn(resource.getString("GML.name"));
	    columnName.setWidth("550px");
	    ArrayColumn columnSize = arrayPane.addArrayColumn(resource.getString("GML.size"));
	    columnSize.setWidth("60px");
		
		FileDetail fileDetail = null;
		String fileName = "";
		
		while (itFile.hasNext())
		{
			fileDetail = (FileDetail) itFile.next();
	
			ArrayLine  arrayLine = arrayPane.addArrayLine();
		
		    // icone du type du fichier
		    /*IconPane icon = gef.getIconPane();
			Icon fileIcon = icon.addIcon();
			fileIcon.setProperties(fileDetail.getFileIcon(), "");
			icon.setSpacing("30px");*/
			ArrayCellText cell = arrayLine.addArrayCellText("<img src=\""+fileDetail.getFileIcon()+"\" width=\"20\" height=\"20\"/>");
			cell.setCompareOn(FileRepositoryManager.getFileExtension(fileDetail.getName()));
		    
		    fileName = fileDetail.getName();
		
		    ArrayCellLink cellLink = arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(fileDetail.getName()), fileDetail.getFileURL(userId, componentId));
		    cellLink.setTarget("_blank");
		    
		    ArrayCellText cellSize = arrayLine.addArrayCellText(fileDetail.getFileSize());
		    cellSize.setCompareOn(new Long(fileDetail.getSize()));
		    		    	    
		 }
		out.println(arrayPane.print());
	}
}

out.println(frame.printAfter());

%>
<FORM name="folderDetailForm" action="viewDirectory" method="post">
<input type="hidden" name="FolderName">
<input type="hidden" name="FileName">
</FORM>

<form name="directoryForm" action="SubDirectory" Method="POST" target="MyMain">
	<input type="hidden" name="DirectoryPath">
</form>

<form name="resultForm" action="Search" method="POST" target="MyMain">
	<input type="hidden" name="WordSearch" size="36"></td>
</form>
<view:progressMessage/>
</body>
</html>