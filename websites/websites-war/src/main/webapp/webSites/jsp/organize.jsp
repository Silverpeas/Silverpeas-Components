<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="javax.servlet.*"%>
<%@ page import="javax.servlet.http.*"%>
<%@ page import="javax.servlet.jsp.*"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.beans.*"%>

<%@ page import="java.util.*"%>
<%@ page import="java.lang.*"%>
<%@ page import="javax.ejb.*,java.sql.SQLException,javax.naming.*,javax.rmi.PortableRemoteObject"%>

<%@ page import="com.stratelia.webactiv.util.*"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.*"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.*"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>


<%!

  /**
    * Called on :
    *
    */
    private boolean appartient(String siteId, Collection liste) {

          Iterator l = liste.iterator();
          while(l.hasNext()) {
            String id = (String) l.next();
            if (id.equals(siteId))
                return true;
          }
          return false;
    }


%>

<%


ResourceLocator settings;
//CBO : REMOVE String language;
String rootId = "0";
//CBO : REMOVE String space;
String action;
String id;
String name;
String description;
//CBO : REMOVE String creationDate;
//CBO : REMOVE String creatorName;
//CBO : REMOVE String path;
//CBO : REMOVE String fatherId;
//CBO : REMOVE String childId;
//CBO : REMOVE Collection subTopicList;
//CBO : REMOVE Collection publicationList;
String linkedPathString = "";
String pathString = "";
FolderDetail webSitesCurrentFolder = null;

settings = new ResourceLocator("com.stratelia.webactiv.webSites.settings.webSiteSettings","fr");

//CBO : REMOVE String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

//Icons
//CBO : UPDATE
/*
String addFolder=iconsPath+"/util/icons/folderAddBig.gif";
String addSite=iconsPath+"/util/icons/webSites_classify.gif";
String belpou=iconsPath+"/util/icons/webSites_topic_to_trash.gif";
String folderUpdate=iconsPath+"/util/icons/update.gif";
*/
String addFolder=m_context+"/util/icons/folderAddBig.gif";
String addSite=m_context+"/util/icons/webSites_classify.gif";
String belpou=m_context+"/util/icons/webSites_topic_to_trash.gif";

if (bookmarkMode)
{
	addSite	= m_context+"/util/icons/bookmark_to_addin_topic.gif";
	belpou	= m_context+"/util/icons/bookmark_topic_to_trash.gif";
}

String folderUpdate=m_context+"/util/icons/update.gif";

//CBO : ADD 
String upIconSrc=m_context+"/util/icons/arrow/arrowUp.gif";
String downIconSrc=m_context+"/util/icons/arrow/arrowDown.gif";
String pxSrc=m_context+"/util/viewGenerator/icons/15px.gif";
//CBO : FIN ADD

//R�cup�ration des param�tres
action = (String) request.getParameter("Action");
id = (String) request.getParameter("Id");
//CBO : REMOVE childId = (String) request.getParameter("ChildId");
//CBO : REMOVE language = (String) request.getParameter("Language");
//CBO : REMOVE space = (String) request.getParameter("Space");

//CBO : ADD
webSitesCurrentFolder = (FolderDetail) request.getAttribute("CurrentFolder");

//Mise a jour de l'espace
if (action == null) {
    id = rootId;
    action = "Search";
}
else SilverTrace.info("websites", "JSPorganize", "root.MSG_GEN_PARAM_VALUE", "action = "+action);


%>

<!-- organize -->

<HTML>
<HEAD>

<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="javaScript/spacesInURL.js"></script>
<!--CBO : UPDATE-->
<!--<script type="text/javascript" src="<%/*iconsPath*/%>/util/javaScript/animation.js"></script>-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>

<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>

<script Language="JavaScript">

var topicAddWindow = window;
var topicUpdateWindow = window;

function topicGoTo(id) {
	closeWindows();

    document.liste.Action.value = "Search";
    document.liste.Id.value = id;
    document.liste.submit();
}

/***************************************************************************/

function declassify(nbTopic, nbSite) {
    closeWindows();

    okTopic = "false";
    okSites = "false";

    if (nbTopic > 0) {
        if (nbTopic == 1) {
            if (document.liste.checkbox.checked)
                okTopic = "true";
        }
        else {
            for (i=0; i<nbTopic; i++) {
                if (document.liste.checkbox[i] != null) {
                    if (document.liste.checkbox[i].checked)
                        okTopic = "true";
                }
                else break;
            }
        }
    }

    if (nbSite > 0) {
        if (nbSite == 1) {
            if (document.liste.supSite.checked)
                    okSites = "true";
        }
        else {
            for (i=0; i<nbSite; i++) {
                if (document.liste.supSite[i] != null) {
                    if (document.liste.supSite[i].checked)
                        okSites = "true";
                }
                else break;
            }
        }
     }


    if (okTopic != "false"  || okSites != "false") { //au moins un theme ou un site est selectionne

    if (window.confirm("<%=resources.getString("FolderSiteDeleteConfirmation")%>")){
          listeSite = "";

      if (nbSite > 0) {
            if (nbSite == 1) {
                if (document.liste.supSite.checked)
                        listeSite += document.liste.supSite.value + ",";
            }
            else {
                for (i=0; i<nbSite; i++) {
                    if (document.liste.supSite[i] != null) {
                        if (document.liste.supSite[i].checked)
                                    listeSite += document.liste.supSite[i].value + ",";
                    }
                    else break;
                }
            }
       }

	  document.liste.Action.value = "Delete";
	  document.liste.SiteList.value = listeSite;
	  document.liste.submit();
	}
     }
}

/***************************************************************************/

function topicAdd(fatherId) {
	if (!topicAddWindow.closed && topicAddWindow.name == "topicAddWindow")
        topicAddWindow.close();

    path = document.liste.Path.value;
    url = "addTopic.jsp?Id="+fatherId+"&Path="+breakSpace(path)+"&Action=View";
    windowName = "topicAddWindow";
    larg = "670";
    haut = "270";
    windowParams = "directories=0,menubar=0,toolbar=0,alwaysRaised";
    topicAddWindow = SP_openWindow(url, windowName, larg , haut, windowParams);
}

/***************************************************************************/

function topicUpdate(id) {
	if (!topicUpdateWindow.closed && topicUpdateWindow.name== "topicUpdateWindow")
        topicUpdateWindow.close();

    document.liste.ChildId.value = id;
    path = document.liste.Path.value;
    url = "updateTopic.jsp?ChildId="+id+"&Path="+breakSpace(path);
    windowName = "topicUpdateWindow";
    larg = "670";
    haut = "270";
    windowParams = "directories=0,menubar=0,toolbar=0, alwaysRaised";
    topicUpdateWindow = SP_openWindow(url, windowName, larg , haut, windowParams);
}

/***************************************************************************/

function publicationAdd(topicId){
    closeWindows();

   /* classifier Declassifier dans ce theme */
    document.pubForm.Action.value = "View";
    document.pubForm.TopicId.value = topicId;
    document.pubForm.submit();
}

/***************************************************************************/

function publicationGoTo(type, theURL, nom){
	closeWindows();

    winName = "_blank" ;
    larg = "670";
    haut = "500";
    windowParams = "width="+larg+",height="+haut+", toolbar=yes, scrollbars=yes, resizable, alwaysRaised";

    if (type == "1") {
        if (nom.indexOf("://")!=-1)
            theURL = nom;
        else
        	theURL = "http://"+ nom;
     }
     else {
          theURL = theURL+nom;
     }
     site = window.open(theURL,winName,windowParams);
}

/***************************************************************************/

function closeWindows() {
	if (!topicAddWindow.closed && topicAddWindow.name=="topicAddWindow")
		topicAddWindow.close();

	if (!topicUpdateWindow.closed && topicUpdateWindow.name=="topicUpdateWindow")
		topicUpdateWindow.close();
}

/***************************************************************************/

//CBO : ADD
function topicDown(topicId) {
	closeWindows();

	document.liste.action = "TopicDown";
	document.liste.Id.value = topicId;
	document.liste.submit();
}

/***************************************************************************/

function topicUp(topicId) {
	closeWindows();

	document.liste.action = "TopicUp";
	document.liste.Id.value = topicId;
	document.liste.submit();
}

/***************************************************************************/

function pubDown(pubId) {
	closeWindows();

	document.liste.action = "PubDown";
	document.liste.Id.value = pubId;
	document.liste.submit();
}

/***************************************************************************/

function pubUp(pubId) {
	closeWindows();

	document.liste.action = "PubUp";
	document.liste.Id.value = pubId;
	document.liste.submit();
}
//CBO : FIN ADD

</script>
</HEAD>
<BODY>

<FORM NAME="liste" ACTION="organize.jsp" METHOD=POST >


<%
	//Traitement = View, Search, Add, Update, Delete, Classify, Declassify
	if (id == null) {
		id=rootId;
		action = "Search";
	}


	/* ADD */
	//CBO : REMOVE
	/*if (action.equals("Add")) {
		name = (String) request.getParameter("Name");
		description = (String) request.getParameter("Description");
		NodeDetail folder = new NodeDetail("X",name,description,null,null,null,"0","X");
		NodePK newNodePK = scc.addFolder(folder, "");
		action = "Search";
	}

	/* UPDATE */
	/*else if (action.equals("Update")) {
		childId = (String) request.getParameter("ChildId");
		name = (String) request.getParameter("Name");
		description = (String) request.getParameter("Description");
		NodeDetail folder = new NodeDetail(childId,name,description,null,null,null,"0","X");
		NodePK updatedNodePK = scc.updateFolderHeader(folder, "");
		action = "Search";
	}*/


	/* DELETE */
	/*else if (action.equals("Delete")) { /* declassification des sites et suppression des themes */
		
		/* delete folder */
/*		int i = 0;
		String[] listeId = request.getParameterValues("checkbox");
		if (listeId == null) {
			String Id = (String) request.getParameter("checkbox");
			if (Id != null) {
				//delete theme et publications
				scc.deleteFolder(Id);
			}
	   } else {
			while (i < listeId.length) {
				String idFolderToDelete = (String) listeId[i];
				//delete theme et publications
				scc.deleteFolder(idFolderToDelete);
				i++;
			}
		}

		/* quels sont les sites a depublier */
/*		ArrayList arrayToDePublish = new ArrayList();
		Collection liste = scc.getAllWebSite();
		Iterator j = liste.iterator();
		while (j.hasNext()) {
			SiteDetail site = (SiteDetail) j.next();

			//CBO : UPDATE
			/*Collection pub = scc.getAllPublication(site.getSitePK().getId());
			if (pub.size() == 0)
				arrayToDePublish.add(site.getSitePK().getId());
			*/
/*			if(scc.getIdPublication(site.getSitePK().getId()) == null) {
				arrayToDePublish.add(site.getSitePK().getId());
			}
			//CBO : FIN UPDATE
		 }

		// dePublish
		if (arrayToDePublish.size() > 0)
			scc.dePublish(arrayToDePublish);

		//CBO : UPDATE
	   /* delete sites */
	   /*String listeSiteToDelete = (String) request.getParameter("SiteList");
	    arrayToDePublish = new ArrayList();
		i = 0;
		int begin = 0;
		int end = 0;
		end = listeSiteToDelete.indexOf(',', begin);
		while(end != -1) {
			/* delete publication */
			/*String idPubToDelete = listeSiteToDelete.substring(begin, end);
			PublicationDetail pub = scc.getPublicationDetail(idPubToDelete);
			scc.deletePublication(idPubToDelete);

			/* isPublished dans un autre theme */
		/*	Collection coll = scc.getAllPublication(pub.getVersion());
			if (coll.size() == 0)
				arrayToDePublish.add(pub.getVersion());

			begin = end + 1;
			end = listeSiteToDelete.indexOf(',', begin);
		}
		*/

		/* declassify sites */
/*		String listeSite = (String) request.getParameter("SiteList");
		arrayToDePublish = new ArrayList();
		i = 0;
		int begin = 0;
		int end = 0;
		end = listeSite.indexOf(',', begin);
		String idPubToDeClassify;
		PublicationDetail pub;
		Collection listNodePk;
		while(end != -1) {
			idPubToDeClassify = listeSite.substring(begin, end); // pubId
			pub = scc.getPublicationDetail(idPubToDeClassify);

			scc.removePublicationToFolder(idPubToDeClassify, id);

			/* isPublished dans un autre theme */
/*			listNodePk = scc.getAllFatherPK(idPubToDeClassify);
			if (listNodePk.size() == 0)
				arrayToDePublish.add(pub.getVersion());

			begin = end + 1;
			end = listeSite.indexOf(',', begin);
			
		}
		//CBO : FIN UPDATE

		// dePublish
/*		if (arrayToDePublish.size() > 0)
			scc.dePublish(arrayToDePublish);

		action = "Search";
	}
	*/

	/* CLASSIFY */
/*	else if (action.equals("classify")) {
		String listeSite = (String) request.getParameter("SiteList");
		ArrayList arrayToClassify = new ArrayList();
		int i = 0;
		int begin = 0;
		int end = 0;
		end = listeSite.indexOf(',', begin);

		//CBO : UPDATE
		/*while(end != -1) {
			String idSiteToClassify = listeSite.substring(begin, end);
			arrayToClassify.add(idSiteToClassify);
			begin = end + 1;
			end = listeSite.indexOf(',', begin);
			// ajout de la publication
			SiteDetail site = scc.getWebSite(idSiteToClassify);
			
			//CBO : UPDATE
			/*PublicationDetail pubDetail = new PublicationDetail("X", site.getName(), site.getDescription(), null, null, null, "", new Integer(site.getType()).toString(), site.getSitePK().getId(), "", site.getPage());*/
			/*	PublicationDetail pubDetail = new PublicationDetail("X", site.getName(), site.getDescription(), null, null, null, "", new Integer(site.getType()).toString(), site.getSitePK().getId(), "", site.getContent());

			String newPubId = scc.createPublication(pubDetail);

		}
		if (arrayToClassify.size() > 0) {
			scc.publish(arrayToClassify); //set etat du site a 1
		}
		*/
/*		String idSiteToClassify;
		Collection listPub = null;
		String pubId = null;
		while(end != -1) {
			idSiteToClassify = listeSite.substring(begin, end);
			arrayToClassify.add(idSiteToClassify);
			
			//CBO : UPDATE
			/*listPub = scc.getAllPublication(idSiteToClassify);
			pubId = (String) listPub.iterator().next();*/
/*			pubId = scc.getIdPublication(idSiteToClassify);
			//CBO : FIN UPDATE

			scc.addPublicationToFolder(pubId, id);

			begin = end + 1;
			end = listeSite.indexOf(',', begin);
		}
		if (arrayToClassify.size() > 0) {
			scc.publish(arrayToClassify); //set etat du site a 1
		}
		//CBO : FIN UPDATE

		action = "Search";
	}*/

	/* DECLASSIFY */
/*	else if (action.equals("declassify")) {

		String listeSite = (String) request.getParameter("SiteList");

		ArrayList arrayToDeClassify = new ArrayList();
		int i = 0;
		int begin = 0;
		int end = 0;
		end = listeSite.indexOf(',', begin);
		//CBO : UPDATE
		/*while(end != -1) {
			String idSiteToDeClassify = listeSite.substring(begin, end); // pubId
			PublicationDetail pubDetail = scc.getPublicationDetail(idSiteToDeClassify);
			// suppression de la publication
			scc.deletePublication(idSiteToDeClassify);

			/* isPublished dans un autre theme */
			/*Collection coll = scc.getAllPublication(pubDetail.getVersion());
			if (coll.size() == 0)
				arrayToDeClassify.add(pubDetail.getVersion());

			begin = end + 1;
			end = listeSite.indexOf(',', begin);
		}

		*/
/*		String idSiteToDeClassify = null;
		String pubId = null;
		Collection listNodePk;
		while(end != -1) {
			pubId = listeSite.substring(begin, end); // pubId

			//CBO : REMOVE
			/*listPub = scc.getAllPublication(idSiteToDeClassify);
			pubId = (String) listPub.iterator().next();*/

/*			scc.removePublicationToFolder(pubId, id);

			/* isPublished dans un autre theme */
/*			listNodePk = scc.getAllFatherPK(pubId);
			if (listNodePk.size() == 0) {
				//CBO : UPDATE
				//arrayToDeClassify.add(idSiteToDeClassify);
				PublicationDetail pubDetail = scc.getPublicationDetail(pubId);
            	idSiteToDeClassify = pubDetail.getVersion();
            	arrayToDeClassify.add(idSiteToDeClassify);
			}

			begin = end + 1;
			end = listeSite.indexOf(',', begin);
		}
		//CBO : FIN UPDATE

		if (arrayToDeClassify.size() > 0) {
			scc.dePublish(arrayToDeClassify); //set etat du site a 0
		}
		action = "Search";
	} */
	//CBO : FIN REMOVE


	/* SEARCH */
	if (action.equals("Search")) {

		//CBO : REMOVE webSitesCurrentFolder = scc.getFolder(id);
		//CBO : REMOVE scc.setSessionTopic(webSitesCurrentFolder);
		name = webSitesCurrentFolder.getNodeDetail().getName();
		Collection pathC = webSitesCurrentFolder.getPath();
		pathString = navigPath(pathC, false, 3);
		linkedPathString = navigPath(pathC, true, 3);

		Collection subThemes = webSitesCurrentFolder.getNodeDetail().getChildrenDetails();
		Collection nbToolByFolder = webSitesCurrentFolder.getNbPubByTopic();

		Collection listeSites = webSitesCurrentFolder.getPublicationDetails();


		Window window = gef.getWindow();
		String bodyPart="";

		// La barre de naviagtion
		BrowseBar browseBar = window.getBrowseBar();
		//CBO : UPDATE
		//browseBar.setDomainName(scc.getSpaceLabel());
		browseBar.setDomainName(spaceLabel);
		//CBO : UPDATE
		//browseBar.setComponentName(scc.getComponentLabel(), "organize.jsp");
		browseBar.setComponentName(componentLabel, "organize.jsp");
		browseBar.setPath(linkedPathString);

		//Les op�rations
		OperationPane operationPane = window.getOperationPane();
		operationPane.addOperation(addFolder, resources.getString("GML.createTheme") , "javascript:onClick=topicAdd('"+id+"')");
		operationPane.addOperation(addSite, resources.getString("AjouterSitesTheme"), "javascript:onClick=publicationAdd('"+id+"')");
		operationPane.addOperation(belpou,resources.getString("DeclasserSitesSupprimerThemes"), "javascript:onClick=declassify('"+subThemes.size()+"','"+listeSites.size()+"');");

		//Les onglets
		TabbedPane tabbedPane = gef.getTabbedPane();
		tabbedPane.addTab(resources.getString("Consulter"), "listSite.jsp", false);
		tabbedPane.addTab(resources.getString("Organiser"), "organize.jsp", true);
		tabbedPane.addTab(resources.getString("GML.management"), "manage.jsp", false);

		//Le cadre
		Frame frame = gef.getFrame();

		//Le tableau de tri
		ArrayPane arrayPane = gef.getArrayPane("foldersList", "organize.jsp?Action=Search&Id="+id, request, session);
		arrayPane.setVisibleLineNumber(10);
		arrayPane.setTitle(resources.getString("ListeThemes"));
		//D�finition des colonnes du tableau
		arrayPane.addArrayColumn(resources.getString("GML.theme"));
		arrayPane.addArrayColumn(resources.getString("GML.description"));
		ArrayColumn arrayColumnOp = arrayPane.addArrayColumn(resources.getString("GML.operation"));
		arrayColumnOp.setSortable(false);
		
		//CBO : ADD
		if (scc.isSortedTopicsEnabled())
		{
			ArrayColumn arrayColumnOrder = arrayPane.addArrayColumn(resources.getString("GML.order"));
			arrayColumnOrder.setSortable(false);
		}
		
		ArrayColumn arrayColumnDel = arrayPane.addArrayColumn("&nbsp;");
		arrayColumnDel.setSortable(false);

		if (subThemes != null) {
			Iterator i = subThemes.iterator();
			Iterator iteratorNbTool = nbToolByFolder.iterator();
			String themeName = "";
			String themeDescription = "";
			String themeId = "";
			String nbPub = "?";
			int nbChild = 0;
			while (i.hasNext()) {
				NodeDetail theme = (NodeDetail) i.next();
				/* ecriture des lignes du tableau */
				themeName = theme.getName();
				themeDescription = theme.getDescription();
				themeId = theme.getNodePK().getId();
				if (iteratorNbTool.hasNext())
					nbPub = ((Integer) iteratorNbTool.next()).toString();

				ArrayLine arrayLine = arrayPane.addArrayLine();

				if (themeName.length() > 40)
					themeName = themeName.substring(0, 40) + "...";
				arrayLine.addArrayCellLink(Encode.javaStringToHtmlString(themeName), "organize.jsp?Action=Search&Id="+themeId);

				if (themeDescription.length() > 80)
					themeDescription = themeDescription.substring(0, 80) + "...";
				arrayLine.addArrayCellText(Encode.javaStringToHtmlString(themeDescription));

				IconPane iconPane = gef.getIconPane();
				Icon checkIcon1 = iconPane.addIcon();
				checkIcon1.setProperties(folderUpdate, resources.getString("GML.modify")+" '"+Encode.javaStringToHtmlString(themeName)+"'" , "javascript:onClick=topicUpdate('"+themeId+"')");
				arrayLine.addArrayCellIconPane(iconPane);

				//CBO : ADD
				if (scc.isSortedTopicsEnabled())
				{
					IconPane sortPane = gef.getIconPane();
					if (nbChild != 0) {
						Icon upIcon = sortPane.addIcon();
						upIcon.setProperties(upIconSrc, resources.getString("TopicUp")+" '"+themeName+"'", "javascript:onClick=topicUp('"+themeId+"')");
					} else {
						Icon upIcon = sortPane.addEmptyIcon();
					}

					if (nbChild < subThemes.size()-1) {
						Icon downIcon = sortPane.addIcon();
						downIcon.setProperties(downIconSrc, resources.getString("TopicDown")+" '"+themeName+"'", "javascript:onClick=topicDown('"+themeId+"')");
					} else {
						Icon downIcon = sortPane.addEmptyIcon();
					}
					arrayLine.addArrayCellIconPane(sortPane);
				}

				arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkbox\" value=\""+themeId+"\">");

				nbChild++;
			}
		}

		//R�cup�ration du tableau dans le haut du cadre
		frame.addTop(arrayPane.print());

		//Board
		Board board = gef.getBoard();

		//Liste des sites du th�me courant
		String liste = "";

		if (listeSites.size() > 0) {
			liste += "<table border=\"0\">\n";

			//R�cup des sites
			Iterator j = listeSites.iterator();
			
			//CBO : ADD
			int nbChild = 0;
			
			while (j.hasNext()) {
				PublicationDetail site = (PublicationDetail) j.next();
				String pubId = site.getPK().getId();
				String siteName = site.getName();
				String siteDescription = site.getDescription();
				if (siteDescription == null)
					siteDescription = "";

				String sitePage = site.getContent();
				String type = new Integer(site.getImportance()).toString();
				String siteId = site.getVersion();
				liste += "<tr>\n";
				liste += "<td valign=\"top\" width=\"5%\"><input type=\"checkbox\" name=\"supSite\" value=\""+pubId+"\"></td>\n";
				//CBO : ADD
				if (scc.isSortedTopicsEnabled())
				{
					IconPane sortPane = gef.getIconPane();
					if (nbChild != 0) {
						Icon upIcon = sortPane.addIcon();
						upIcon.setProperties(upIconSrc, resources.getString("PubUp")+" '"+siteName+"'", "javascript:onClick=pubUp('"+pubId+"')");
					} else {
						Icon upIcon = sortPane.addEmptyIcon();
					}

					if (nbChild < listeSites.size()-1) {
						Icon downIcon = sortPane.addIcon();
						downIcon.setProperties(downIconSrc, resources.getString("PubDown")+" '"+siteName+"'", "javascript:onClick=pubDown('"+pubId+"')");
					} else {
						Icon downIcon = sortPane.addEmptyIcon();
					}

					liste += "<TD width=\"10px\">&nbsp;</TD>\n";
					
					liste += "<TD width=\"20px\" valign=\"top\">\n";
					liste += sortPane.print();
					liste += "</TD>\n";
					
					liste += "<TD width=\"10px\">&nbsp;</TD>\n";
				}
				//CBO : FIN ADD
				
				//CBO : UPDATE
				/*liste += "<td valign=\"top\">&#149;&nbsp;<a class=\"textePetitBold\" href=\"javascript:onClick=publicationGoTo('"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+scc.getComponentId()+"/"+siteId+"/' , '"+Encode.javaStringToJsString(sitePage)+"')\">"+siteName+"</a><br>\n";*/
				liste += "<td valign=\"top\">&#149;&nbsp;<a class=\"textePetitBold\" href=\"javascript:onClick=publicationGoTo('"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+componentId+"/"+siteId+"/' , '"+Encode.javaStringToJsString(sitePage)+"')\">"+siteName+"</a><br>\n";

				liste += "<span class=\"txtnote\">&nbsp;&nbsp;"+siteDescription+"</span><br><br></td>\n";
				liste += "</tr>\n";
				
				//CBO : ADD
				nbChild++;
			}

			liste += "</table>\n";		

			board.addBody(liste);
		}

		//R�cup�ration de la liste des sites dans le cadre
		if(listeSites.size() > 0) {
			frame.addBottom(board.print());
		}

		//On crache le HTML ;o)
		bodyPart += tabbedPane.print();
		bodyPart += frame.print();
		window.addBody(bodyPart);
		out.println(window.print());
	}
%>

  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="<%=id%>">
  <input type="hidden" name="Path" value="<%=Encode.javaStringToHtmlString(pathString)%>">
  <input type="hidden" name="ChildId">
  <input type="hidden" name="Name">
  <input type="hidden" name="Description">
  <input type="hidden" name="SiteList">
</FORM>

<FORM NAME="pubForm" ACTION="classifyDeclassify.jsp" METHOD="POST">
<input type="hidden" name="Action">
<input type="hidden" name="TopicId">
<input type="hidden" name="Path" value="<%=Encode.javaStringToHtmlString(linkedPathString)%>">
</FORM>


</BODY>
</HTML>