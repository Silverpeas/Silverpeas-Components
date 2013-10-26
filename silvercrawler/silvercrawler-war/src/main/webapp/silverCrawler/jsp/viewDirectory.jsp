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
<%@page import="java.net.URLEncoder"%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
<%@page import="com.silverpeas.util.StringUtil"%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>

<%-- Set resource bundle --%>
<fmt:setLocale value="${requestScope.resources.language}"/>
<view:setBundle bundle="${requestScope.resources.multilangBundle}"/>

<c:set var="componentId" value="${requestScope.browseContext[3]}"/>

<%@ include file="check.jsp" %>
<%
FileFolder folder = (FileFolder) request.getAttribute("Folder");
boolean folderIsWritable = folder.isWritable();
String profile = (String) request.getAttribute("Profile");
String userId = (String) request.getAttribute("UserId");
boolean download = (Boolean) request.getAttribute("IsDownload");
Collection path = (Collection) request.getAttribute("Path");
boolean isRootPath = (Boolean) request.getAttribute("IsRootPath");
boolean allowedNav = (Boolean) request.getAttribute("IsAllowedNav");
String rootPath = (String) request.getAttribute("RootPath");
String maxDirectories = (String) request.getAttribute("MaxDirectories");
String maxFiles = (String) request.getAttribute("MaxFiles");
String language = (String) request.getAttribute("Language");
boolean readWriteActivated = (Boolean) request.getAttribute("isReadWriteActivated");
boolean userAllowedToSetRWAccess = (Boolean) request.getAttribute("userAllowedToSetRWAccess");
boolean userAllowedToLANAccess = (Boolean) request.getAttribute("userAllowedToLANAccess");
String errorMessage = (String) request.getAttribute("errorMessage");
String successMessage = (String) request.getAttribute("successMessage");

String sRequestURL = request.getRequestURL().toString();
String m_sAbsolute =
    sRequestURL.substring(0, sRequestURL.length() - request.getRequestURI().length());
String httpServerBase = GeneralPropertiesManager.getString("httpServerBase", m_sAbsolute);

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
<script type="text/javascript">

var downloadWindow = window;

function indexFolder(folderName)
{
    if(window.confirm("<%=resource.getString("silverCrawler.folderIndexConfirmation")%> '" + folderName + "' ?")){
          $.progressMessage();
          document.folderDetailForm.action = "IndexPath";
          document.folderDetailForm.FolderName.value = folderName;
          document.folderDetailForm.submit();
    }
}

function removeFolder(folderName)
{
    if(window.confirm("<%=resource.getString("silverCrawler.folderRemoveConfirmation")%> '" + folderName + "' ?")){
          $.progressMessage();
          document.folderDetailForm.action = "RemoveFolder";
          document.folderDetailForm.FolderName.value = folderName;
          document.folderDetailForm.submit();
    }
}

function removeFile(fileName)
{
    if(window.confirm("<%=resource.getString("silverCrawler.fileRemoveConfirmation")%> '" + fileName + "' ?")){
          $.progressMessage();
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
        $.progressMessage();
        document.liste_file.action = "RemoveSelectedFiles";
        document.liste_file.submit();
  }
}

function removeFoldersByLot() {
	var selectedFolders = "<%=resource.getString("silverCrawler.foldersRemoveConfirmation")%> : \n\n";
	$('input:checkbox[name=checkedDir]:checked').each(function() {selectedFolders = selectedFolders + $(this).val() + "\n";});
	if(window.confirm(selectedFolders)){
        $.progressMessage();
        document.liste_dir.action = "RemoveSelectedFolders";
        document.liste_dir.submit();
  }
}

function renameFolder(folderName) {
  showDialog({
    dialogTitle : '<fmt:message key="silverCrawler.renameFolder" />',
    submitAction : 'RenameFolder',
    dialogFormId : 'renameForm',
    oldName : folderName
  });
}

function createFolder() {
  showDialog({
    dialogTitle : '<fmt:message key="silverCrawler.createFolder" />',
    submitAction : 'CreateFolder',
    dialogFormId : 'createForm'
  });
}

function uploadFile() {
  showDialog({
    isFileUpload : true,
    dialogTitle : '<fmt:message key="silverCrawler.uploadFile" />',
    submitAction : 'UploadFile',
    dialogFormId : 'fileUploadForm'
  });
}

function renameFile(fileName) {
  showDialog({
    dialogTitle : '<fmt:message key="silverCrawler.renameFile" />',
    submitAction : 'RenameFile',
    dialogFormId : 'renameForm',
    oldName : fileName
  });
}

function showDialog(params) {
  var options = $.extend({
    isFileUpload : false,
    dialogTitle : '',
    submitAction : '',
    dialogFormId : '',
    oldName : ''
  }, params);
  $("#modalDialog").dialog({
    buttons : {
      "Ok" : function() {
        $('form', this).submit();
      },
      "Cancel" : function() {
        $(this).dialog("close");
      }
    },
    width : 400,
    title : options.dialogTitle
  });

  var url = "<c:url value="/RsilverCrawler/${componentId}/" />" + options.submitAction +
      "Form?oldName=" + encodeURIComponent(options.oldName);
  $("#modalDialog").load(url, function() {
    $('form', this).submit(function(){
      $.progressMessage();
      if (options.isFileUpload) {
        return true;
      }
      performPost(options.submitAction, options.dialogFormId);
      return false;
    });
    $(this).dialog("open");
  });
}

function performPost(url, formId) {
  $.ajax({
    type : 'POST',
    url : url,
    data : $('#'+formId).serialize(),
    cache : false,
    success : function(data) {
      if (data == 'statusOK') {
        document.folderDetailForm.action = "ViewDirectory";
        document.folderDetailForm.FolderName.value = "";
        document.folderDetailForm.FileName.value = "";
        document.folderDetailForm.submit();
      } else {
        $.closeProgressMessage();
        alert(data);
        $("#newName").focus();
      }
    },
    error : function() {
      $.closeProgressMessage();
      alert('erreur inconnue');
      $("#newName").focus();
    }
  });
}

function indexFile(fileName)
{
    if(window.confirm("<%=resource.getString("silverCrawler.fileIndexConfirmation")%> '" + fileName + "' ?")){
          $.progressMessage();
          document.folderDetailForm.action = "IndexFile";
          document.folderDetailForm.FileName.value = fileName;
          document.folderDetailForm.submit();
    }
}

function indexDisk()
{
    if(window.confirm("<%=resource.getString("silverCrawler.diskIndexConfirmation")%>")){
          $.progressMessage();
          document.folderDetailForm.action = "IndexPath";
          document.folderDetailForm.FolderName.value = "";
          document.folderDetailForm.submit();
    }
}

function indexDirByLot()
{
	if(window.confirm("<%=resource.getString("silverCrawler.fileIndexByLotConfirmation")%>")){
    	$.progressMessage();
		document.liste_dir.action = "IndexDirSelected";
		document.liste_dir.submit();
	}
}

function indexFileByLot()
{
	if(window.confirm("<%=resource.getString("silverCrawler.fileIndexByLotConfirmation")%>")){
    	$.progressMessage();
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
	long maximumFileSize = FileRepositoryManager.getUploadMaximumFileSize();
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

function goToDirectory(path) {
	$.progressMessage();
	location.href = "SubDirectory?DirectoryPath="+encodeURIComponent(path);
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

if ("admin".equals(profile) || "publisher".equals(profile))
{
	// RW operations
	if (readWriteActivated && folderIsWritable) {
	  	operationPane.addLine();
	  	if ("admin".equals(profile))
 		{
			operationPane.addOperationOfCreation(resource.getIcon("silverCrawler.createFolder"), resource.getString("silverCrawler.createFolder"), "javascript:createFolder()");
 		}

	  	if ("admin".equals(profile) || "publisher".equals(profile))
 		{
			operationPane.addOperationOfCreation(resource.getIcon("silverCrawler.uploadFile"), resource.getString("silverCrawler.uploadFile"), "javascript:uploadFile()");
 		}

	  	operationPane.addLine();
	  	if ("admin".equals(profile))
 		{
			operationPane.addOperation(resource.getIcon("silverCrawler.removeFoldersByLot"), resource.getString("silverCrawler.removeFoldersByLot"), "javascript:removeFoldersByLot()");
 		}

	  	if ("admin".equals(profile) || "publisher".equals(profile))
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

<% if (userAllowedToLANAccess && readWriteActivated) {%>
<div id="physical-path"><%=resource.getString("silverCrawler.physicalPath")%> : ${Folder.path}</div>
<% } %>

<form name="liste_dir" action="#" method="POST">
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
	        if (nav) {
	        	nameCell = "<a href=\"javascript:onclick=goToDirectory('"+fileName + "')\">" + EncodeHelper.javaStringToHtmlString(fileName)+"</a>";
	        } else {
	        	nameCell = EncodeHelper.javaStringToHtmlString(fileName);
	        }
	        //  permalien
	        String filePath = file.getPath();
	        filePath = filePath.substring(rootPath.length()+1);
	        link = URLManager.getApplicationURL() + "/SubDir/" + componentId +"?Path="+URLEncoder.encode(filePath, "UTF-8");
	        nameCell = nameCell + "&nbsp;<a href=\"" + link + "\"><img border=\"0\" src=\""+resource.getIcon("silverCrawler.permalien")+"\"/></a>";

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
	<form name="liste_file" action="#" method="POST">
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
			ArrayCellText cell = arrayLine.addArrayCellText("<img src=\""+fileDetail.getFileIcon()+"\"/>");
			cell.setCompareOn(FileRepositoryManager.getFileExtension(fileDetail.getName()));

		    fileName = fileDetail.getName();

		    boolean indexed = fileDetail.isIsIndexed();

		    ArrayCellLink cellLink = arrayLine.addArrayCellLink(EncodeHelper.javaStringToHtmlString(fileDetail.getName()), fileDetail.getFileURL(
            componentId));
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

			   		if (userAllowedToLANAccess) {
				   		// icône "DirectAccess"
				   		Icon directAccessIcon = iconPane.addIcon();
				   		directAccessIcon.setProperties(resource.getIcon("silverCrawler.directAccess"), resource.getString("silverCrawler.directAccess"), fileDetail.getDirectURL());
				   		iconPane.setSpacing("20px");
			   		}
			   	}

		   		arrayLine.addArrayCellIconPane(iconPane);

		   		if ("admin".equals(profile) || "publisher".equals(profile))
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