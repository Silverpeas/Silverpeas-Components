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
String		maxDirectories	= (String) request.getAttribute("MaxDirectories");
String 		maxFiles		= (String) request.getAttribute("MaxFiles");

boolean download 	= isDownload.booleanValue();
boolean isRootPath 	= isRootPathB.booleanValue();
boolean allowedNav 	= isAllowedNav.booleanValue();

int nbDirectories = 10;
if (maxDirectories != null && Integer.parseInt(maxDirectories) != 0)
	nbDirectories = Integer.parseInt(maxDirectories);
int nbFiles = 10;
if (maxFiles != null && Integer.parseInt(maxFiles) != 0)
	nbFiles = Integer.parseInt(maxFiles);

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

function indexFolder(folderName)
{
    if(window.confirm("<%=resource.getString("silverCrawler.folderIndexConfirmation")%> '" + folderName + "' ?")){
          document.folderDetailForm.action = "IndexPath";
          document.folderDetailForm.FolderName.value = folderName;
          document.folderDetailForm.submit();
    }
}

function indexFile(fileName)
{
    if(window.confirm("<%=resource.getString("silverCrawler.fileIndexConfirmation")%> '" + fileName + "' ?")){
          document.folderDetailForm.action = "IndexFile";
          document.folderDetailForm.FileName.value = fileName;
          document.folderDetailForm.submit();
    }
}

function indexDisk()
{
    if(window.confirm("<%=resource.getString("silverCrawler.diskIndexConfirmation")%>")){
          document.folderDetailForm.action = "IndexPath";
          document.folderDetailForm.FolderName.value = "";
          document.folderDetailForm.submit();
    }
}

function indexDirByLot()
{
	if(window.confirm("<%=resource.getString("silverCrawler.fileIndexByLotConfirmation")%>")){
		document.liste_dir.action = "IndexDirSelected";
		document.liste_dir.submit();
	}
}

function indexFileByLot()
{
	if(window.confirm("<%=resource.getString("silverCrawler.fileIndexByLotConfirmation")%>")){
		document.liste_file.action = "IndexFileSelected";
		document.liste_file.submit();
	}
}

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

// mettre les op�ration si on est � la racine
String name = folder.getName();
if ("admin".equals(profile))
{
	if (isRootPath) 
	{
		operationPane.addOperation(resource.getIcon("silverCrawler.statsDisk"), resource.getString("silverCrawler.downloadHistory"), "javaScript:viewDownloadHistory('')");
		operationPane.addOperation(resource.getIcon("silverCrawler.indexDisk"), resource.getString("silverCrawler.reIndexer"), "javaScript:indexDisk('')");
		if (download)
			operationPane.addOperation(resource.getIcon("silverCrawler.uploadDisk"), resource.getString("silverCrawler.download"), "javaScript:downloadFolder('')");
	}
	// op�ration de r�indexation par lot
	operationPane.addOperation(resource.getIcon("silverCrawler.indexDirByLot"), resource.getString("silverCrawler.indexDirByLot"), "javaScript:indexDirByLot('')");
	operationPane.addOperation(resource.getIcon("silverCrawler.indexFileByLot"), resource.getString("silverCrawler.indexFileByLot"), "javaScript:indexFileByLot('')");
}

out.println(window.printBefore());
out.println(frame.printBefore());

Board board	= gef.getBoard();

out.println(board.printBefore());
	
// affichage de la zone de recherche
// ---------------------------------
Button validateButton 	= (Button) gef.getFormButton("OK", "javascript:onClick=sendData();", false);
%>
<center>
	<table border="0" cellpadding="0" cellspacing="0">
		<form name="searchForm" action="Search" method="POST" onSubmit="javascript:sendData();">
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
<FORM NAME="liste_dir" >
<%

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
	    // nombre de r�pertoires � afficher
	    arrayPane.setVisibleLineNumber(nbDirectories);
	
	    ArrayColumn columnType = arrayPane.addArrayColumn(resource.getString("GML.type"));
	    columnType.setWidth("40px");
	    ArrayColumn columnName = arrayPane.addArrayColumn(resource.getString("GML.name"));
	    columnName.setWidth("615px");
	    
	    if (download || "admin".equals(profile))
	    {
	    	ArrayColumn columnOp = arrayPane.addArrayColumn(resource.getString("silverCrawler.operation"));
	    	columnOp.setSortable(false);
	    	ArrayColumn columnLot = arrayPane.addArrayColumn("");
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
	        
	        boolean indexed = file.isIsIndexed();
	
	        String nameCell = "";
	        
	        if (nav)
	        {
	        	//arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(fileName), "SubDirectory?DirectoryPath="+fileName);
	        	nameCell = "<a href=\"SubDirectory?DirectoryPath="+fileName + "\">" + Encode.javaStringToHtmlString(fileName)+"</a>";
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
	        
	        if (download || "admin".equals(profile))
	        {
				// cr�ation de la colonne des ic�nes
		        IconPane iconPane = gef.getIconPane();
		        if ("admin".equals(profile))
				{
		        	//ic�ne de l'historique
		    	   	Icon historyIcon = iconPane.addIcon();
		    	   	historyIcon.setProperties(resource.getIcon("silverCrawler.viewHistory"), resource.getString("silverCrawler.downloadHistory"), "javaScript:viewDownloadHistory('"+Encode.javaStringToJsString(fileName)+"')");
		    	   	iconPane.setSpacing("20px");
		    	   	
					// ic�ne "r�indexer"
					Icon indexIcon = iconPane.addIcon();
					indexIcon.setProperties(resource.getIcon("silverCrawler.reIndexer"), resource.getString("silverCrawler.reIndexer"), "javaScript:indexFolder('"+Encode.javaStringToJsString(fileName)+"')");
			   		iconPane.setSpacing("20px");
				}
			   	if (download)
			   	{
			   		// ic�ne "t�l�charger le r�pertoire"
			   		Icon downloadIcon = iconPane.addIcon();
			   		downloadIcon.setProperties(resource.getIcon("silverCrawler.download"), resource.getString("silverCrawler.download"), "javaScript:downloadFolder('"+Encode.javaStringToJsString(fileName)+"')");
			   		iconPane.setSpacing("20px");
			   	}
			   	
			   	if ("admin".equals(profile) && indexed)
			   	{
			   		// ic�ne "r�pertoire ind�x�"
			   		Icon indexedIcon = iconPane.addIcon();
			   		indexedIcon.setProperties(resource.getIcon("silverCrawler.isIndexed"), resource.getString("silverCrawler.isIndexed"), "");
			   		iconPane.setSpacing("20px");
			   	}
			   		
			   	arrayLine.addArrayCellIconPane(iconPane);
			   	if ("admin".equals(profile))
				{
			   		// case � cocher pour traitement par lot
			   		arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkedDir\" value=\""+Encode.javaStringToHtmlString(fileName)+"\">");
				}
	        }
	     }
	    out.println(arrayPane.print());
	}


	out.println("</FORM>");

	%>
	<FORM NAME="liste_file" >
	<%
	
	//affichage des fichiers
	//----------------------
	
	Collection fileList = folder.getFiles();
	if (fileList != null && fileList.size() > 0)
	{
	    Iterator itFile = fileList.iterator();
			
	    ArrayPane arrayPane = gef.getArrayPane("fileList", "ViewDirectory", request, session);
	    arrayPane.setVisibleLineNumber(nbFiles);
	    
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
	    	ArrayColumn columnLot = arrayPane.addArrayColumn("");
		}
		
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
		    
		    boolean indexed = fileDetail.isIsIndexed();
		
		    ArrayCellLink cellLink = arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(fileDetail.getName()), fileDetail.getFileURL(userId, componentId));
		    cellLink.setTarget("_blank");
		    
		    ArrayCellText cellSize = arrayLine.addArrayCellText(fileDetail.getFileSize());
		    cellSize.setCompareOn(new Long(fileDetail.getSize()));
		    		    	    
		    if ("admin".equals(profile))
			{
		    	IconPane iconPane = gef.getIconPane();
		    	
	        	//ic�ne de l'historique
	    	   	Icon historyIcon = iconPane.addIcon();
	    	   	historyIcon.setProperties(resource.getIcon("silverCrawler.viewHistory"), resource.getString("silverCrawler.downloadHistory"), "javaScript:viewDownloadHistory('"+Encode.javaStringToJsString(fileName)+"')");
	    	   	iconPane.setSpacing("20px");
	    	   	
	    	   	//ic�ne "r�indexer"
				Icon indexIcon = iconPane.addIcon();
				indexIcon.setProperties(resource.getIcon("silverCrawler.reIndexer"), resource.getString("silverCrawler.reIndexer"), "javaScript:indexFile('"+Encode.javaStringToJsString(fileName)+"')");
		   		iconPane.setSpacing("20px");    
		   		
		   		if (indexed)
			   	{
			   		// ic�ne "r�pertoire ind�x�"
			   		Icon indexedIcon = iconPane.addIcon();
			   		indexedIcon.setProperties(resource.getIcon("silverCrawler.isIndexed"), resource.getString("silverCrawler.isIndexed"), "");
			   		iconPane.setSpacing("20px");
			   	}
		   		arrayLine.addArrayCellIconPane(iconPane);
		   		
		   		if ("admin".equals(profile))
				{
			   		// case � cocher pour traitement par lot
			   		arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkedFile\" value=\""+Encode.javaStringToHtmlString(fileName)+"\">");
				}
			}
		 }
		out.println(arrayPane.print());
	}
}

out.println("</FORM>");

out.println(frame.printAfter());
out.println(window.printAfter());

%>
<FORM name="folderDetailForm" action="viewDirectory" method="post">
<input type="hidden" name="FolderName">
<input type="hidden" name="FileName">
</FORM>
<view:progressMessage/>
</body>
</html>