<%--

    Copyright (C) 2000 - 2013 Silverpeas

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
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="com.silverpeas.util.EncodeHelper"%>
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
<%@ page import="com.stratelia.webactiv.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.publication.model.*"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.*"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view" %>
<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>

<%!
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
String rootId = "0";
String name;
String description;
String linkedPathString = "";
String pathString = "";

ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.webSites.settings.webSiteSettings","fr");

String addFolder=m_context+"/util/icons/create-action/add-folder.png";
String addSite=m_context+"/util/icons/create-action/add-website-to-topic.png";
String belpou=m_context+"/util/icons/webSites_topic_to_trash.gif";

if (bookmarkMode)
{
	addSite	= m_context+"/util/icons/create-action/add-bookmark-to-topic.png";
	belpou	= m_context+"/util/icons/bookmark_topic_to_trash.gif";
}

String folderUpdate=m_context+"/util/icons/update.gif";

String upIconSrc=m_context+"/util/icons/arrow/arrowUp.gif";
String downIconSrc=m_context+"/util/icons/arrow/arrowDown.gif";
String pxSrc=m_context+"/util/viewGenerator/icons/15px.gif";

String action = request.getParameter("Action");
String id = request.getParameter("Id");

FolderDetail webSitesCurrentFolder = (FolderDetail) request.getAttribute("CurrentFolder");

//Mise a jour de l'espace
if (action == null) {
    id = rootId;
    action = "Search";
}
else SilverTrace.info("websites", "JSPorganize", "root.MSG_GEN_PARAM_VALUE", "action = "+action);


%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel />
<script type="text/javascript" src="javaScript/spacesInURL.js"></script>
<script type="text/javascript">
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
</script>
</head>
<body>
<form name="liste" action="organize.jsp" method="post">
<%
	//Traitement = View, Search, Add, Update, Delete, Classify, Declassify
	if (id == null) {
		id=rootId;
		action = "Search";
	}

	/* SEARCH */
	if (action.equals("Search")) {
		name = webSitesCurrentFolder.getNodeDetail().getName();
		Collection pathC = webSitesCurrentFolder.getPath();
		pathString = navigPath(pathC, false, 3);
		linkedPathString = navigPath(pathC, true, 3);

		Collection subThemes = webSitesCurrentFolder.getNodeDetail().getChildrenDetails();
		Collection nbToolByFolder = webSitesCurrentFolder.getNbPubByTopic();

		Collection listeSites = webSitesCurrentFolder.getPublicationDetails();

		Window window = gef.getWindow();

		// La barre de naviagtion
		BrowseBar browseBar = window.getBrowseBar();
		browseBar.setDomainName(spaceLabel);
		browseBar.setComponentName(componentLabel, "organize.jsp");
		browseBar.setPath(linkedPathString);

		//Les op�rations
		OperationPane operationPane = window.getOperationPane();
		operationPane.addOperationOfCreation(addFolder, resources.getString("GML.createTheme") , "javascript:onClick=topicAdd('"+id+"')");
		operationPane.addOperationOfCreation(addSite, resources.getString("AjouterSitesTheme"), "javascript:onClick=publicationAdd('"+id+"')");
		operationPane.addOperation(belpou,resources.getString("DeclasserSitesSupprimerThemes"), "javascript:onClick=declassify('"+subThemes.size()+"','"+listeSites.size()+"');");

		out.println(window.printBefore());

		//Les onglets
		TabbedPane tabbedPane = gef.getTabbedPane();
		tabbedPane.addTab(resources.getString("Consulter"), "Main", false);
		tabbedPane.addTab(resources.getString("Organiser"), "organize.jsp", true);
		tabbedPane.addTab(resources.getString("GML.management"), "manage.jsp", false);
		
		out.println(tabbedPane.print());
%>
<view:frame>
<view:areaOfOperationOfCreation/>
<%
		//Le tableau de tri
		ArrayPane arrayPane = gef.getArrayPane("foldersList", "organize.jsp?Action=Search&Id="+id, request, session);
		arrayPane.setVisibleLineNumber(10);
		arrayPane.setTitle(resources.getString("ListeThemes"));
		
		arrayPane.addArrayColumn(resources.getString("GML.theme"));
		arrayPane.addArrayColumn(resources.getString("GML.description"));
		ArrayColumn arrayColumnOp = arrayPane.addArrayColumn(resources.getString("GML.operation"));
		arrayColumnOp.setSortable(false);
		
		if (scc.isSortedTopicsEnabled()) {
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

				if (scc.isSortedTopicsEnabled()) {
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

				arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"checkbox\" value=\""+themeId+"\"/>");

				nbChild++;
			}
		}
		out.println(arrayPane.print());

		//Liste des sites du th�me courant
		String liste = "";

		if (listeSites.size() > 0) {
			liste += "<table border=\"0\">\n";

			Iterator j = listeSites.iterator();
			
			int nbChild = 0;
			
			while (j.hasNext()) {
				PublicationDetail site = (PublicationDetail) j.next();
				String pubId = site.getPK().getId();
				String siteName = site.getName();
				String siteDescription = EncodeHelper.javaStringToHtmlParagraphe(site.getDescription());
				if (siteDescription == null)
					siteDescription = "";

				String sitePage = site.getContent();
				String type = new Integer(site.getImportance()).toString();
				String siteId = site.getVersion();
				liste += "<tr>\n";
				liste += "<td valign=\"top\" width=\"5%\"><input type=\"checkbox\" name=\"supSite\" value=\""+pubId+"\"/></td>\n";
				if (scc.isSortedTopicsEnabled()) {
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

					liste += "<td width=\"10px\">&nbsp;</td>\n";
					
					liste += "<td width=\"20px\" valign=\"top\">\n";
					liste += sortPane.print();
					liste += "</td>\n";
					
					liste += "<td width=\"10px\">&nbsp;</td>\n";
				}
				liste += "<td valign=\"top\">&#149;&nbsp;<a class=\"textePetitBold\" href=\"javascript:onClick=publicationGoTo('"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+componentId+"/"+siteId+"/' , '"+Encode.javaStringToJsString(sitePage)+"')\">"+siteName+"</a><br/>\n";

				liste += "<span class=\"txtnote\">&nbsp;&nbsp;"+siteDescription+"</span><br/><br/></td>\n";
				liste += "</tr>\n";
				
				nbChild++;
			}

			liste += "</table>\n";	
%>
			<br/>
			<view:board>
			<%=liste %>
			</view:board>
<%			
	}
%>
</view:frame>
<%
		out.println(window.printAfter());
	}
%>

  <input type="hidden" name="Action"/>
  <input type="hidden" name="Id" value="<%=id%>"/>
  <input type="hidden" name="Path" value="<%=Encode.javaStringToHtmlString(pathString)%>"/>
  <input type="hidden" name="ChildId"/>
  <input type="hidden" name="Name"/>
  <input type="hidden" name="Description"/>
  <input type="hidden" name="SiteList"/>
</form>

<form name="pubForm" action="classifyDeclassify.jsp" method="post">
<input type="hidden" name="Action"/>
<input type="hidden" name="TopicId"/>
<input type="hidden" name="Path" value="<%=Encode.javaStringToHtmlString(linkedPathString)%>"/>
</form>

</body>
</html>