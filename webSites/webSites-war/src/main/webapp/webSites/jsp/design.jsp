<%--

    Copyright (C) 2000 - 2022 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ page import="java.io.File" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.Encode" %>
<%@ page import="org.silverpeas.core.util.WebEncodeHelper" %>
<%@ include file="util.jsp" %>
<%@ include file="checkScc.jsp" %>

<%
	String addFolder=m_context+"/util/icons/create-action/add-folder.png";
  String addPage=m_context+"/util/icons/webSites_page_to_add.gif";
  String addPic=m_context+"/util/icons/create-action/download-website.png";
  String addLib=m_context+"/util/icons/create-action/add-website-to-topic.png";
  String updateDescription=m_context+"/util/icons/webSites_to_modify.gif";
  String belpou=m_context+"/util/icons/basket.gif";
  String update=m_context+"/util/icons/update.gif";
  String delete = m_context + "/util/icons/delete.gif";

  String action = request.getParameter("Action");
  String id = request.getParameter("Id"); //jamais null sauf en creation ou en update de description
  String currentPath = request.getParameter("Path");
  String date = "";
  String auteur = "";
  int popup = 0;
	String nomSite = null;
	String description = null;
	String nomPage = null;
	SiteDetail siteDetail = (SiteDetail) request.getAttribute("Site");
	if(siteDetail != null) {
		id = siteDetail.getSitePK().getId();
		nomSite = siteDetail.getName();
		description = siteDetail.getDescription();
		auteur = siteDetail.getCreatorId();
		date = resources.getOutputDate(siteDetail.getCreationDate());
    popup = siteDetail.getPopup();
		nomPage = siteDetail.getContentPagePath();
	}
	boolean searchOk = true;
	Boolean theSearch = (Boolean) request.getAttribute("SearchOK");
	String theListeIcones = (String) request.getAttribute("ListeIcones");
	if(theSearch != null && theSearch == Boolean.FALSE) {
		searchOk = false;
	}

	UserDetail user = scc.getUserDetail(auteur);
	if (user != null) {
	auteur = user.getDisplayedName();
	}
	Collection collectionRep = affichageChemin(scc, currentPath);
	String infoPath = displayPath(collectionRep, true, 3, "design.jsp?Action=view&Path=", nomSite);
%>

<!-- design -->

<html>
<head>
<view:looknfeel/>
<title><%=resources.getString("GML.popupTitle")%></title>
<script type="text/javascript">

<%

if (! searchOk) {
    out.println("notyError(\""+resources.getString("PrincipalPageNotCorrectDesign")+"\")");
    if (description == null) {
		description = "";
    }out.println("location.replace(\"modifDesc.jsp?Id="+id+"&Path="+currentPath+"&type=design&RecupParam=oui&Nom="+nomSite+"&Description="+description+"&Page="+nomPage+"&ListeIcones="+theListeIcones+"\");");

}

%>

function URLENCODE(URL){
  return encodeURIComponent(URL);
}

function openWindow(url, name) {
  return SP_openWindow(url, name, 700, 200, "directories=0,menubar=0,toolbar=0,alwaysRaised");
}

/**********************************************/

function B_RETOUR_ONCLICK() {
    if (window.repAddWindow != null)
        window.repAddWindow.close();
    if (window.repUpdateWindow != null)
        window.repUpdateWindow.close();
    if (window.pageUpdateWindow != null)
        window.pageUpdateWindow.close();
    if (window.uploadFileWindow != null)
        window.uploadFileWindow.close();
    if (window.pageAddWindow != null)
        window.pageAddWindow.close();

    location.href="manage.jsp?Action=view";
}

/**********************************************/
function folderAdd(id, path) {
    if (window.repAddWindow != null) {
        window.repAddWindow.close();
    }
    if (window.repUpdateWindow != null) {
        window.repUpdateWindow.close();
    }
    if (window.pageUpdateWindow != null) {
        window.pageUpdateWindow.close();
    }
    if (window.uploadFileWindow != null) {
        window.uploadFileWindow.close();
    }
    if (window.pageAddWindow != null) {
        window.pageAddWindow.close();
    }

    repAddWindow = openWindow({
      url : 'addRep.jsp',
      params : {
        'Id' : id,
        'Path' : path,
        'Action' : 'View'
      }
    }, "repAddWindow");
}

/**********************************************/
function pageAdd(path, nomsite) {
    if (window.repAddWindow != null) {
        window.repAddWindow.close();
    }
    if (window.repUpdateWindow != null) {
        window.repUpdateWindow.close();
    }
    if (window.pageUpdateWindow != null) {
        window.pageUpdateWindow.close();
    }
    if (window.uploadFileWindow != null) {
        window.uploadFileWindow.close();
    }
    if (window.pageAddWindow != null) {
        window.pageAddWindow.close();
    }
    pageAddWindow = openWindow({
      url : 'addPage.jsp',
      params : {
        'id' : '<%=id%>',
        'Path' : path,
        'Action' : 'View',
        'nameSite' : nomsite
      }
    }, "pageAddWindow");
}

/**********************************************/
function uploadFile(path) {
    if (window.repAddWindow != null)
        window.repAddWindow.close();
    if (window.repUpdateWindow != null)
        window.repUpdateWindow.close();
    if (window.pageUpdateWindow != null)
        window.pageUpdateWindow.close();
    if (window.uploadFileWindow != null)
        window.uploadFileWindow.close();
    if (window.pageAddWindow != null)
        window.pageAddWindow.close();

    uploadFileWindow = openWindow({
      url : 'uploadFile.jsp',
      params : {
        'Path' : path
      }
    }, "uploadFileWindow");
}



/**********************************************/
function renameFolder(id, path, name) {
    if (window.repAddWindow != null)
        window.repAddWindow.close();
    if (window.repUpdateWindow != null)
        window.repUpdateWindow.close();
    if (window.pageUpdateWindow != null)
        window.pageUpdateWindow.close();
    if (window.uploadFileWindow != null)
        window.uploadFileWindow.close();
    if (window.pageAddWindow != null)
        window.pageAddWindow.close();

    repUpdateWindow = openWindow({
      url : 'updateRep.jsp',
      params : {
        'Id' : id,
        'Path' : path,
        'Action' : 'View',
        'Name' : name
      }
    }, "repUpdateWindow");
}

/**********************************************/
function deleteFolder(id, path, name) {
    if (window.repAddWindow != null)
        window.repAddWindow.close();
    if (window.repUpdateWindow != null)
        window.repUpdateWindow.close();
    if (window.pageUpdateWindow != null)
        window.pageUpdateWindow.close();
    if (window.uploadFileWindow != null)
        window.uploadFileWindow.close();
    if (window.pageAddWindow != null)
        window.pageAddWindow.close();

  var label = "<%=resources.getString("MessageSuppressionFolder")%>";
  jQuery.popup.confirm(label, function() {
    document.design.Action.value = "deleteFolder";
    document.design.Id.value = id;
    document.design.Path.value = path;
    document.design.name.value = name;
    document.design.submit();
  });
}

/**********************************************/
function pageRedesign(path, name, namesite) {
      if (window.repAddWindow != null)
          window.repAddWindow.close();
      if (window.repUpdateWindow != null)
          window.repUpdateWindow.close();
      if (window.pageUpdateWindow != null)
          window.pageUpdateWindow.close();
      if (window.uploadFileWindow != null)
          window.uploadFileWindow.close();
      if (window.pageAddWindow != null)
          window.pageAddWindow.close();
      if (path.indexOf('..') >= 0)
        notyError("<%= resources.getString("GML.error.AccessForbidden") %>");
      else
        location.href="ToWysiwyg?Path="+URLENCODE(path)+"&name="+URLENCODE(name)+"&nameSite="+URLENCODE(namesite)+"&id=<%=id%>";
}

/**********************************************/
function renamePage(id, path, name) {
    if (window.repAddWindow != null)
        window.repAddWindow.close();
    if (window.repUpdateWindow != null)
        window.repUpdateWindow.close();
    if (window.pageUpdateWindow != null)
        window.pageUpdateWindow.close();
    if (window.uploadFileWindow != null)
        window.uploadFileWindow.close();
    if (window.pageAddWindow != null)
        window.pageAddWindow.close();

    pageUpdateWindow = openWindow({
      url : 'updatePage.jsp',
      params : {
        'Id' : id,
        'Path' : path,
        'Action' : 'View',
        'Name' : name
      }
    }, "pageUpdateWindow");

}

/**********************************************/
function deletePage(id, path, name) {
    if (window.repAddWindow != null)
        window.repAddWindow.close();
    if (window.repUpdateWindow != null)
        window.repUpdateWindow.close();
    if (window.pageUpdateWindow != null)
        window.pageUpdateWindow.close();
    if (window.uploadFileWindow != null)
        window.uploadFileWindow.close();
    if (window.pageAddWindow != null)
        window.pageAddWindow.close();

    var label = "<%=resources.getString("MessageSuppressionFile")%>";
    jQuery.popup.confirm(label, function() {
      document.design.Action.value = "deletePage";
      document.design.Id.value = id;
      document.design.Path.value = path;
      document.design.name.value = name;
      document.design.submit();
});
}
</script>

</head>
<body>

<form name="design" action="verif.jsp" method="post">
  <input type="hidden" name="Action">
  <input type="hidden" name="Id">
  <input type="hidden" name="Path">
  <input type="hidden" name="name">
  <input type="hidden" name="newName">
</form>

<form name="liste" action="verif.jsp" method="post">
<%
	Window window = gef.getWindow();
	String bodyPart="";

	BrowseBar browseBar = window.getBrowseBar();
	browseBar.setDomainName(spaceLabel);
  browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
	browseBar.setPath("<a href= \"manage.jsp?Action=view\"></a>"+infoPath);

	//Le cadre
	Frame frame = gef.getFrame();

	//Le tableau des repertoires
	ArrayPane arrayPaneRep = gef.getArrayPane("foldersList", "design.jsp?Action=design&Path="+currentPath+"&Id="+id, request, session);
	arrayPaneRep.setVisibleLineNumber(10);
	arrayPaneRep.setTitle(resources.getString("ListeRepertoires"));

	//Definition des colonnes du tableau
	ArrayColumn columnName = arrayPaneRep.addArrayColumn(resources.getString("GML.name"));
	ArrayColumn columnOperation = arrayPaneRep.addArrayColumn(resources.getString("FolderOperations"));
	columnOperation.setSortable(false);

	String bodyRep = "";

	//Recuperation du tableau dans le haut du cadre
	// desc site + path
	bodyRep += "<br><span class=\"txtnav\">"+ Encode.javaStringToHtmlString(nomSite)+"</span>";
	bodyRep += "<span class=\"txtnote\">&nbsp;("+Encode.javaStringToHtmlString(auteur)+" - "+date+")<br>\n";
	bodyRep += "<b>&nbsp;"+resources.getString("PagePrincipale")+" : </b>"+Encode.javaStringToHtmlString(nomPage)+"</span>\n";
	bodyRep += "<br><br>\n";

	/* ecriture des lignes du tableau : liste des repertoires */
	Collection subFolders = scc.getAllSubFolder(currentPath); /* Collection(File) */

	Iterator j = subFolders.iterator();
	String folderName = "";
	while (j.hasNext()) {
		File folder = (File) j.next();
		folderName = folder.getName();

		ArrayLine arrayLine = arrayPaneRep.addArrayLine();
		arrayLine.addArrayCellLink(folderName, "design.jsp?Action=view&Path="+currentPath+"/"+folderName);

		IconPane iconPane = gef.getIconPane();
		Icon updateIcon = iconPane.addIcon();
		updateIcon.setProperties(update, resources.getString("Rename")+" '"+folderName+"'" , "javascript:onClick=renameFolder('"+id+"', '"+Encode.javaStringToJsString(currentPath)+"', '"+Encode.javaStringToJsString(folderName)+"')");

		Icon deleteIcon = iconPane.addIcon();
		deleteIcon.setProperties(delete, resources.getString("GML.delete")+" '"+folderName+"'" , "javascript:onClick=deleteFolder('"+id+"', '"+Encode.javaStringToJsString(currentPath)+"', '"+Encode.javaStringToJsString(folderName)+"')");

		iconPane.setSpacing("30px");
		arrayLine.addArrayCellIconPane(iconPane);
	}

  bodyRep += arrayPaneRep.print();
  frame.addTop(bodyRep);
	bodyRep += "<BR><BR><BR><BR>";
/**-------------------------------------------------------------------------------------*/


	//Le tableau des fichiers
	ArrayPane arrayPaneFile = gef.getArrayPane("fileList", "design.jsp?Action=design&Path="+currentPath+"&Id="+id, request, session);
	arrayPaneFile.setVisibleLineNumber(10);
	arrayPaneFile.setTitle(resources.getString("ListeFichiers"));

	//Definition des colonnes du tableau
	ArrayColumn columnNam = arrayPaneFile.addArrayColumn(resources.getString("GML.name"));
	ArrayColumn columnOp = arrayPaneFile.addArrayColumn(resources.getString("FolderOperations"));
	columnOp.setSortable(false);

	/* ecriture des lignes du tableau : liste des fichiers */
	Collection file = scc.getAllFile(currentPath); /* Collection(File) */

	j = file.iterator();
	folderName = "";
	while (j.hasNext()) {
		File folder = (File) j.next();
		folderName = folder.getName();
		int indexPoint = folderName.lastIndexOf(".");
		String type = folderName.substring(indexPoint + 1);

		if (! type.startsWith("zip")) {
			ArrayLine arrayLine = arrayPaneFile.addArrayLine();

			//nom
			if (type.startsWith("htm") || type.startsWith("HTM")) {
				arrayLine.addArrayCellLink(folderName, "javascript:onClick=pageRedesign('"+ WebEncodeHelper.javaStringToJsString(currentPath)+"', '"+WebEncodeHelper.javaStringToJsString(folderName)+"', '"+WebEncodeHelper.javaStringToJsString(nomSite)+"')");
			} else if (folderName.equals(nomPage)) {
				arrayLine.addArrayCellText(folderName);
			} else {
			  arrayLine.addArrayCellText(folderName);
			}

			//operation
			if (! folderName.equals(nomPage)) {
				IconPane iconPane = gef.getIconPane();
				Icon updateIcon = iconPane.addIcon();
				updateIcon.setProperties(update, resources.getString("Rename")+" '"+folderName+"'" , "javascript:onClick=renamePage('"+id+"', '"+WebEncodeHelper.javaStringToJsString(currentPath)+"', '"+WebEncodeHelper.javaStringToJsString(folderName)+"')");

				Icon deleteIcon = iconPane.addIcon();
				deleteIcon.setProperties(delete, resources.getString("GML.delete")+" '"+folderName+"'" , "javascript:onClick=deletePage('"+id+"', '"+WebEncodeHelper.javaStringToJsString(currentPath)+"', '"+
            WebEncodeHelper.javaStringToJsString(folderName)+"')");

				iconPane.setSpacing("30px");
				arrayLine.addArrayCellIconPane(iconPane);
			}
			else {
				arrayLine.addArrayCellText("");
			}
		}
	}

  //Recuperation du tableau dans le bas du cadre
  String bodyFile = "<br><br>";
  bodyFile+=arrayPaneFile.print();
  bodyFile += "<br><br>";

  frame.addBottom(bodyFile);


  //Les operations
  OperationPane operationPane = window.getOperationPane();
  operationPane.addOperation(addFolder,resources.getString("FolderAdd"), "javascript:onClick=folderAdd('"+id+"', '"+WebEncodeHelper.javaStringToJsString(currentPath)+"')");
  operationPane.addLine();
  operationPane.addOperation(addPage,resources.getString("PageAdd"), "javascript:onClick=pageAdd('"+WebEncodeHelper.javaStringToJsString(currentPath)+"', '"+WebEncodeHelper.javaStringToJsString(nomSite)+"')");
  operationPane.addLine();
  operationPane.addOperation(addPic,resources.getString("FileUploadAdd"), "javascript:onClick=uploadFile('"+WebEncodeHelper.javaStringToJsString(currentPath)+"')");
  operationPane.addLine();
  operationPane.addOperation(addLib,resources.getString("ClasserSite"), "classifySite.jsp?Id="+id+"&Path="+currentPath);

  int indexSup = infoPath.indexOf(" > ");

  if (indexSup == -1) {//on est a la racine
	operationPane.addLine();
	operationPane.addOperation(updateDescription,resources.getString("ModificationDescription"), "modifDesc.jsp?Id="+id+"&Path="+currentPath+"&type=design");
  }


	bodyPart += frame.print();
  window.addBody(bodyPart);
  out.println(window.print());
%>
</form>
</body>
</html>
