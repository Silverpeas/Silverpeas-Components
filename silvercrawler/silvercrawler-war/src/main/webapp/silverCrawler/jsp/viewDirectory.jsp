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
<%@page import="java.net.URLEncoder"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.silverpeas.util.StringUtil"%>
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
String 		language		= (String) request.getAttribute("Language");
Boolean     isReadWriteActivated = (Boolean) request.getAttribute("isReadWriteActivated");
Boolean     isUserAllowedToSetRWAccess = (Boolean) request.getAttribute("userAllowedToSetRWAccess");
String 		errorMessage 	= (String) request.getAttribute("errorMessage");
String 		successMessage 	= (String) request.getAttribute("successMessage");

boolean download 	= isDownload.booleanValue();
boolean isRootPath 	= isRootPathB.booleanValue();
boolean allowedNav 	= isAllowedNav.booleanValue();
boolean folderIsWritable = folder.isWritable();
boolean readWriteActivated = isReadWriteActivated.booleanValue();
boolean userAllowedToSetRWAccess = isUserAllowedToSetRWAccess.booleanValue();

ResourceLocator generalSettings = GeneralPropertiesManager.getGeneralResourceLocator();
String sRequestURL = request.getRequestURL().toString();
String m_sAbsolute = sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
String httpServerBase = generalSettings.getString("httpServerBase", m_sAbsolute);

int nbDirectories = 10;
if (maxDirectories != null && Integer.parseInt(maxDirectories) != 0)
	nbDirectories = Integer.parseInt(maxDirectories);
int nbFiles = 10;
if (maxFiles != null && Integer.parseInt(maxFiles) != 0)
	nbFiles = Integer.parseInt(maxFiles);

boolean nav = true;
if ("user".equals(profile) && !allowedNav)
	nav = false;

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
			if (nav)
				chemin = chemin + "<a href=\"GoToDirectory?DirectoryPath="+ directory + "\">" + EncodeHelper.javaStringToHtmlString(directory)+"</a>";
			else
				chemin = chemin + EncodeHelper.javaStringToHtmlString(directory);

			namePath = namePath + directory;
			suivant = itPath.hasNext();
		}
	}
}

%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<view:looknfeel />
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/checkForm.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/upload_applet.js"></script>
<script type="text/javascript" src="<%=m_context%>/silverCrawler/javaScript/dragAndDrop.js"></script>
<style>
.alert-message .close {
    color: #000000;
    float: right;
    font-size: 20px;
    font-weight: bold;
    margin-top: -2px;
    opacity: 0.2;
    text-shadow: 0 1px 0 #FFFFFF;
}

.alert-message.error {
    background-color: #C43C35;
    background-image: -moz-linear-gradient(center top , #EE5F5B, #C43C35);
    background-repeat: repeat-x;
    border-color: rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.25);
    text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.25);
    color: #FFFFFF;
    font-weight: bold;
}

.alert-message.success {
	background-color: #57A957;
    background-image: -moz-linear-gradient(center top , #62C462, #57A957);
    background-repeat: repeat-x;
    border-color: rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.25);
    text-shadow: 0 -1px 0 rgba(0, 0, 0, 0.25);
    color: #FFFFFF;
    font-weight: bold;
}

.alert-message {
    background-color: #EEDC94;
    background-image: -moz-linear-gradient(center top , #FCEEC1, #EEDC94);
    background-repeat: repeat-x;
    border-color: rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.1) rgba(0, 0, 0, 0.25);
    border-radius: 4px 4px 4px 4px;
    border-style: solid;
    border-width: 1px;
    box-shadow: 0 1px 0 rgba(255, 255, 255, 0.25) inset;
    color: #404040;
    margin-bottom: 18px;
    padding: 7px 14px;
    text-shadow: 0 1px 0 rgba(255, 255, 255, 0.5);
    margin-left: 15px;
    margin-right: 15px;
}
</style>
<script type="text/javascript">

var downloadWindow = window;

function indexFolder(folderName)
{
    if(window.confirm("<%=resource.getString("silverCrawler.folderIndexConfirmation")%> '" + folderName + "' ?")){
          document.folderDetailForm.action = "IndexPath";
          document.folderDetailForm.FolderName.value = folderName;
          document.folderDetailForm.submit();
    }
}

function removeFolder(folderName)
{
    if(window.confirm("<%=resource.getString("silverCrawler.folderRemoveConfirmation")%> '" + folderName + "' ?")){
          document.folderDetailForm.action = "RemoveFolder";
          document.folderDetailForm.FolderName.value = folderName;
          document.folderDetailForm.submit();
    }
}

function removeFile(fileName)
{
    if(window.confirm("<%=resource.getString("silverCrawler.fileRemoveConfirmation")%> '" + fileName + "' ?")){
          document.folderDetailForm.action = "RemoveFile";
          document.folderDetailForm.FolderName.value = "";
          document.folderDetailForm.FileName.value = fileName;
          document.folderDetailForm.submit();
    }
}

function removeFilesByLot() {
	var selectedFiles = "<%=resource.getString("silverCrawler.filesRemoveConfirmation")%> : \n\n";
	$('input:checkbox[name=checkedFile]:checked').each(function() {selectedFiles = selectedFiles + $(this).val() + "\n";});
	if(window.confirm(selectedFiles)){
        document.liste_file.action = "RemoveSelectedFiles";
		document.liste_file.submit();
  }
}

function removeFoldersByLot() {
	var selectedFolders = "<%=resource.getString("silverCrawler.foldersRemoveConfirmation")%> : \n\n";
	$('input:checkbox[name=checkedDir]:checked').each(function() {selectedFolders = selectedFolders + $(this).val() + "\n";});
	if(window.confirm(selectedFolders)){
        document.liste_dir.action = "RemoveSelectedFolders";
		document.liste_dir.submit();
  }
}

function renameFolder(folderName) {
    	$("#modalDialog").dialog({
        		buttons: {
            		"Ok": function() {
            			submitForm(folderName);
                		},
            		"Cancel": function() {
                		$(this).dialog("close");
                		}
			 	},

			 	width: 400,

			 	title: "<%=resource.getString("silverCrawler.renameFolder")%>"

	 	});

		var url = "<%=m_context%>/RsilverCrawler/<%=componentId%>/RenameFolderForm?folderName="+escape(folderName);
    	$("#modalDialog").load(url).dialog("open");
}

function createFolder() {
	$("#modalDialog").dialog({
    		buttons: {
        		"Ok": function() {
        			submitCreateFolder();
            		},
        		"Cancel": function() {
            		$(this).dialog("close");
            		}
		 	},

		 	width: 400,

		 	title: "<%=resource.getString("silverCrawler.createFolder")%>"

 	});

	var url = "<%=m_context%>/RsilverCrawler/<%=componentId%>/CreateFolderForm";
	$("#modalDialog").load(url).dialog("open");
}

function uploadFile() {
	$("#modalDialog").dialog({
    		buttons: {
        		"Ok": function() {
        			submitUploadFileForm();
            		},
        		"Cancel": function() {
            		$(this).dialog("close");
            		}
		 	},

		 	width: 400,

		 	title: "<%=resource.getString("silverCrawler.uploadFile")%>"

 	});

	var url = "<%=m_context%>/RsilverCrawler/<%=componentId%>/UploadFileForm";
	$("#modalDialog").load(url).dialog("open");

}

function renameFile(fileName) {
	$("#modalDialog").dialog({
    		buttons: {
        		"Ok": function() {
        			submitRenameFileForm(fileName);
            		},
        		"Cancel": function() {
            		$(this).dialog("close");
            		}
		 	},

		 	width: 400,

		 	title: "<%=resource.getString("silverCrawler.renameFile")%>"

 	});

	var url = "<%=m_context%>/RsilverCrawler/<%=componentId%>/RenameFileForm?fileName="+escape(fileName);
	$("#modalDialog").load(url).dialog("open");
}

function submitForm(folderName) {
	$.ajax({
		  url: "RenameFolder?folderName="+escape(folderName)+"&newName="+escape($("#newName").val()),
		  context: document.body,
		  success: function( data ) {
			    if (data=='statusOK') {
			    	document.folderDetailForm.action = "ViewDirectory";
			        document.folderDetailForm.FolderName.value = "";
			        document.folderDetailForm.submit();
				}
			    else {
				    alert(data);
				}
			  },
		  failure: function(){
			    alert('erreur inconnue');
			  }
		});

}

function submitRenameFileForm(fileName) {
	$.ajax({
		  url: "RenameFile",
		  data: {fileName: fileName, newName: $("#newName").val()},
		  context: document.body,
		  success: function( data ) {
			    if (data=='statusOK') {
			    	document.folderDetailForm.action = "ViewDirectory";
			        document.folderDetailForm.FolderName.value = "";
			        document.folderDetailForm.submit();
				}
			    else {
				    alert(data);
				}
			  },
		  failure: function(){
			    alert('erreur inconnue');
			  }
		});

}

function submitUploadFileForm(folderName) {
	document.fileUploadForm.submit();
}

function submitCreateFolder() {
	$.ajax({
		  url: "CreateFolder",
		  data: {newName: $("#newName").val()},
		  context: document.body,
		  success: function( data ) {
			    if (data=='statusOK') {
			    	document.folderDetailForm.action = "ViewDirectory";
			        document.folderDetailForm.FolderName.value = "";
			        document.folderDetailForm.submit();
				}
			    else {
				    alert(data);
				}
			  },
		  failure: function(){
			    alert('erreur inconnue');
			  }
		});
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
	url = "DownloadFolder?FolderName="+encodeURIComponent(folderName);
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
	url = "ViewDownloadHistory?Name="+encodeURIComponent(name);
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

function hideErrorMsg() {
	$("#errorMsg").hide(500);
}

function hideSuccessMsg() {
	$("#successMsg").hide(500);
}

function showDnD() {
	<%
	ResourceLocator uploadSettings = new ResourceLocator("com.stratelia.webactiv.util.uploads.uploadSettings", "");
	String maximumFileSize = uploadSettings.getString("MaximumFileSize", "10000000");
	if (profile.equals("publisher")) { %>
		showHideDragDrop('<%=httpServerBase+m_context%>/SilverCrawlerDragAndDrop/?UserId=<%=userId%>&ComponentId=<%=componentId%>&IgnoreFolders=1&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeNormal_<%=language%>.html','<%=resource.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resource.getString("GML.DragNDropExpand")%>','<%=resource.getString("GML.DragNDropCollapse")%>');
	<% } else { %>
		showHideDragDrop('<%=httpServerBase+m_context%>/SilverCrawlerDragAndDrop/?UserId=<%=userId%>&ComponentId=<%=componentId%>&SessionId=<%=session.getId()%>','<%=httpServerBase + m_context%>/upload/ModeNormal_<%=language%>.html','<%=resource.getString("GML.applet.dnd.alt")%>','<%=maximumFileSize%>','<%=m_context%>','<%=resource.getString("GML.DragNDropExpand")%>','<%=resource.getString("GML.DragNDropCollapse")%>');
	<% } %>
}

function processDnD() {
	document.folderDetailForm.action = "ProcessDragAndDrop";
    document.folderDetailForm.submit();
}

$(document).ready(function(){
    var dialogOpts = {
            modal: true,
            autoOpen: false,
            height: "auto"
    };

    $("#modalDialog").dialog(dialogOpts);    //end dialog
});
</script>
</head>
<body>
<%
browseBar.setDomainName(spaceLabel);
browseBar.setComponentName(componentLabel, "Main");
browseBar.setPath(chemin);

// mettre les opération si on est à la racine
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

	// opération de réindexation par lot
	operationPane.addOperation(resource.getIcon("silverCrawler.indexDirByLot"), resource.getString("silverCrawler.indexDirByLot"), "javaScript:indexDirByLot('')");
	operationPane.addOperation(resource.getIcon("silverCrawler.indexFileByLot"), resource.getString("silverCrawler.indexFileByLot"), "javaScript:indexFileByLot('')");
}

if ( ("admin".equals(profile)) || ("publisher".equals(profile)) )
{
	// RW operations
	if (readWriteActivated && folderIsWritable) {
	  	operationPane.addLine();
	  	if ("admin".equals(profile))
 		{
			operationPane.addOperationOfCreation(resource.getIcon("silverCrawler.createFolder"), resource.getString("silverCrawler.createFolder"), "javascript:createFolder()");
 		}

	  	if ( ("admin".equals(profile)) || ("publisher".equals(profile)) )
 		{
			operationPane.addOperationOfCreation(resource.getIcon("silverCrawler.uploadFile"), resource.getString("silverCrawler.uploadFile"), "javascript:uploadFile()");
 		}

	  	operationPane.addLine();
	  	if ("admin".equals(profile))
 		{
			operationPane.addOperation(resource.getIcon("silverCrawler.removeFoldersByLot"), resource.getString("silverCrawler.removeFoldersByLot"), "javascript:removeFoldersByLot()");
 		}

	  	if ( ("admin".equals(profile)) || ("publisher".equals(profile)) )
 		{
			operationPane.addOperation(resource.getIcon("silverCrawler.removeFilesByLot"), resource.getString("silverCrawler.removeFilesByLot"), "javascript:removeFilesByLot()");
 		}

	}

	// opération d'activation/désactivation de l'accès lecture/écriture
	if (userAllowedToSetRWAccess) {
		operationPane.addLine();
		if (readWriteActivated) {
			operationPane.addOperation(resource.getIcon("silverCrawler.unactivateRWAccess"), resource.getString("silverCrawler.unactivateRWAccess"), "UnactivateRWaccess");
		}
		else {
		  operationPane.addOperation(resource.getIcon("silverCrawler.activateRWAccess"), resource.getString("silverCrawler.activateRWAccess"), "ActivateRWaccess");
		}
	}
}

out.println(window.printBefore());

if (StringUtil.isDefined(errorMessage)) {
  %>
	<div class="alert-message error" id="errorMsg">
        <a href="javascript:hideErrorMsg()" class="close">×</a>
        <p><%=errorMessage%></p>
      </div>
  <%
}

if (StringUtil.isDefined(successMessage)) {
  %>
	<div class="alert-message success" id="successMsg">
        <a href="javascript:hideSuccessMsg()" class="close">×</a>
        <p><%=successMessage%></p>
      </div>
  <%
}
%>
<view:frame>
<view:board>
<%
// affichage de la zone de recherche
// ---------------------------------
Button validateButton 	= gef.getFormButton("OK", "javascript:onClick=sendData();", false);
%>
<center>
	<form name="searchForm" action="Search" method="post" onsubmit="javascript:sendData();">
	<table border="0" cellpadding="0" cellspacing="0">
			<tr>
				<td valign="middle" align="left" class="txtlibform" width="30%"><%=resource.getString("GML.search")%></td>
				<td align="left" valign="middle">
					<table border="0" cellspacing="0" cellpadding="0">
						<tr valign="middle">
							<td valign="middle"><input type="text" name="WordSearch" size="36" onkeydown="checkSubmitToSearch(event)"/></td>
							<td valign="middle">&nbsp;</td>
							<td valign="middle" align="left" width="100%"><% out.println(validateButton.print());%></td>
						</tr>
					</table>
				</td>
			</tr>
    </table>
    </form>
</center>
</view:board>
<br/>
<view:areaOfOperationOfCreation/>
<% if ( ( ("admin".equals(profile)) || ("publisher".equals(profile)) ) && folderIsWritable && readWriteActivated ) { %>
<div id="DnD">
<table width="98%" cellpadding="0" cellspacing="0"><tr><td align="right">
<a href="javascript:showDnD()" id="dNdActionLabel"><%=resource.getString("GML.DragNDropExpand")%></a>
</td></tr></table>
<table width="100%" border="0" id="DropZone">
<tr>
	<td>
		<div id="DragAndDrop" style="background-color: #CDCDCD; border: 1px solid #CDCDCD; paddding:0px; width:100%" valign="top"><img src="<%=m_context%>/util/icons/colorPix/1px.gif" height="2"/></div>
	</td>
</tr></table>
</div>
<% }  %>

<form name="liste_dir">
<%

// remplissage de l'ArrayPane avec la liste des sous répertoires
// -------------------------------------------------------------
if (nav || (!nav && !isRootPath))
{
	Collection files = folder.getFolders();
	if (files != null && files.size() > 0)
	{
	    Iterator i = files.iterator();
	    String link = "";

	    ArrayPane arrayPane = gef.getArrayPane("folderList", "ViewDirectory", request, session);
	    // nombre de répertoires à afficher
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

	        String fileName = file.getName();
	        String encodedFileName = URLEncoder.encode(fileName, "UTF-8");

	        boolean indexed = file.isIsIndexed();

	        String nameCell = "";

	        if (nav)
	        {
	        	nameCell = "<a href=\"SubDirectory?DirectoryPath="+encodedFileName + "\">" + EncodeHelper.javaStringToHtmlString(fileName)+"</a>";
	        }
	        else
	        {
	        	nameCell = EncodeHelper.javaStringToHtmlString(fileName);
	        }
	        //  permalien
	        String filePath = file.getPath();
	        filePath = filePath.substring(rootPath.length()+1);
	        link = URLManager.getApplicationURL() + "/SubDir/" + componentId +"?Path="+URLEncoder.encode(filePath, "UTF-8");
	        nameCell = nameCell + "&nbsp;<a href=\"" + link + "\">"+ "<img border=\"0\" src=\""+resource.getIcon("silverCrawler.permalien")+"\">" + "</a>";

	        // affichage de la cellule
	        arrayLine.addArrayCellText(nameCell);

	        if (download || "admin".equals(profile))
	        {
				// création de la colonne des icônes
		        IconPane iconPane = gef.getIconPane();
		        if ("admin".equals(profile))
				{
		        	//icône de l'historique
		    	   	Icon historyIcon = iconPane.addIcon();
		    	   	historyIcon.setProperties(resource.getIcon("silverCrawler.viewHistory"), resource.getString("silverCrawler.downloadHistory"), "javaScript:viewDownloadHistory('"+EncodeHelper.javaStringToJsString(fileName)+"')");
		    	   	iconPane.setSpacing("20px");

					// icône "réindexer"
					Icon indexIcon = iconPane.addIcon();
					indexIcon.setProperties(resource.getIcon("silverCrawler.reIndexer"), resource.getString("silverCrawler.reIndexer"), "javaScript:indexFolder('"+EncodeHelper.javaStringToJsString(fileName)+"')");
			   		iconPane.setSpacing("20px");
				}
			   	if (download)
			   	{
			   		// icône "télécharger le répertoire"
			   		Icon downloadIcon = iconPane.addIcon();
			   		downloadIcon.setProperties(resource.getIcon("silverCrawler.download"), resource.getString("silverCrawler.download"), "javaScript:downloadFolder('"+EncodeHelper.javaStringToJsString(fileName)+"')");
			   		iconPane.setSpacing("20px");
			   	}

			   	if ("admin".equals(profile) && indexed)
			   	{
			   		// icône "répertoire indéxé"
			   		Icon indexedIcon = iconPane.addIcon();
			   		indexedIcon.setProperties(resource.getIcon("silverCrawler.isIndexed"), resource.getString("silverCrawler.isIndexed"), "");
			   		iconPane.setSpacing("20px");
			   	}

			   	if ("admin".equals(profile) && folderIsWritable && readWriteActivated)
			   	{
			   		// icône "Suppression"
			   		Icon deleteIcon = iconPane.addIcon();
			   		deleteIcon.setProperties(resource.getIcon("silverCrawler.removeFolder"), resource.getString("silverCrawler.removeFolder"), "javaScript:removeFolder('"+EncodeHelper.javaStringToJsString(fileName)+"')");
			   		iconPane.setSpacing("20px");

			   		// icône "Renommage"
			   		Icon renameIcon = iconPane.addIcon();
			   		renameIcon.setProperties(resource.getIcon("silverCrawler.renameFolder"), resource.getString("silverCrawler.renameFolder"), "javaScript:renameFolder('"+EncodeHelper.javaStringToJsString(fileName)+"')");
			   		iconPane.setSpacing("20px");
			   	}

			   	arrayLine.addArrayCellIconPane(iconPane);
			   	if ("admin".equals(profile))
				{
			   		// case à cocher pour traitement par lot
			   		arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkedDir\" value=\""+EncodeHelper.javaStringToHtmlString(fileName)+"\">");
				}
	        }
	     }
	    out.println(arrayPane.print());
	}
	%>
	</form>
	<form name="liste_file" >
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
	    if ("admin".equals(profile) || "publisher".equals(profile))
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
			ArrayCellText cell = arrayLine.addArrayCellText("<img src=\""+fileDetail.getFileIcon()+"\" width=\"20\" height=\"20\"/>");
			cell.setCompareOn(FileRepositoryManager.getFileExtension(fileDetail.getName()));

		    fileName = fileDetail.getName();

		    boolean indexed = fileDetail.isIsIndexed();

		    ArrayCellLink cellLink = arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(fileDetail.getName()), fileDetail.getFileURL(userId, componentId));
		    cellLink.setTarget("_blank");

		    ArrayCellText cellSize = arrayLine.addArrayCellText(fileDetail.getFileSize());
		    cellSize.setCompareOn(new Long(fileDetail.getSize()));

		    if ("admin".equals(profile) || "publisher".equals(profile))
			{
		    	IconPane iconPane = gef.getIconPane();

		    	if ("admin".equals(profile)) {
		        	//icône de l'historique
		    	   	Icon historyIcon = iconPane.addIcon();
		    	   	historyIcon.setProperties(resource.getIcon("silverCrawler.viewHistory"), resource.getString("silverCrawler.downloadHistory"), "javaScript:viewDownloadHistory('"+EncodeHelper.javaStringToJsString(fileName)+"')");
		    	   	iconPane.setSpacing("20px");

		    	   	//icône "réindexer"
					Icon indexIcon = iconPane.addIcon();
					indexIcon.setProperties(resource.getIcon("silverCrawler.reIndexer"), resource.getString("silverCrawler.reIndexer"), "javaScript:indexFile('"+EncodeHelper.javaStringToJsString(fileName)+"')");
			   		iconPane.setSpacing("20px");

			   		if (indexed)
				   	{
				   		// icône "répertoire indéxé"
				   		Icon indexedIcon = iconPane.addIcon();
				   		indexedIcon.setProperties(resource.getIcon("silverCrawler.isIndexed"), resource.getString("silverCrawler.isIndexed"), "");
				   		iconPane.setSpacing("20px");
				   	}
		    	}

		   		if (folderIsWritable && readWriteActivated)
			   	{
			   		// icône "Suppression"
			   		Icon deleteIcon = iconPane.addIcon();
			   		deleteIcon.setProperties(resource.getIcon("silverCrawler.removeFile"), resource.getString("silverCrawler.removeFile"), "javaScript:removeFile('"+EncodeHelper.javaStringToJsString(fileName)+"')");
			   		iconPane.setSpacing("20px");

			   		// icône "Renommage"
			   		Icon renameIcon = iconPane.addIcon();
			   		renameIcon.setProperties(resource.getIcon("silverCrawler.renameFile"), resource.getString("silverCrawler.renameFile"), "javaScript:renameFile('"+EncodeHelper.javaStringToJsString(fileName)+"')");
			   		iconPane.setSpacing("20px");
			   	}

		   		arrayLine.addArrayCellIconPane(iconPane);

		   		if ("admin".equals(profile))
				{
			   		// case à cocher pour traitement par lot
			   		arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkedFile\" value=\""+EncodeHelper.javaStringToHtmlString(fileName)+"\">");
				}
			}
		 }
		out.println(arrayPane.print());
	}
}
%>
</form>
</view:frame>
<%
out.println(window.printAfter());
%>

<div id="modalDialog"></div>

<form name="folderDetailForm" action="viewDirectory" method="post">
<input type="hidden" name="FolderName"/>
<input type="hidden" name="FileName"/>
</form>
<view:progressMessage/>
</body>
</html>
