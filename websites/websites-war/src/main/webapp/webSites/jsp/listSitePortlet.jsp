<%--

    Copyright (C) 2000 - 2012 Silverpeas

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
<%@ page import="com.stratelia.webactiv.beans.admin.*"%>


<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.tabs.TabbedPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.Link"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodePK"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.*"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.*"%>


<%@ page import="com.stratelia.webactiv.beans.admin.OrganizationController"%>
<%@ page import="com.stratelia.webactiv.beans.admin.UserDetail"%>


<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>

<%!

  /**
    * Called on :
    *
    */
    private boolean iconAppartient(IconDetail iconDetail, Collection c) {
          boolean ok = false;

          String theId = iconDetail.getIconPK().getId();

          Iterator i = c.iterator();
          while(i.hasNext() && !ok) {
              IconDetail icon = (IconDetail) i.next();
              String id = icon.getIconPK().getId();
              if (theId.equals(id))
                ok = true;
          }
          return ok;
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
String pxmag = iconsPath + "/util/icons/colorPix/1px.gif";
String flea = iconsPath + "/util/icons/buletGrey.gif";
String suggerer=iconsPath+"/util/icons/create-action/add-bookmark.png";
String redFlag = iconsPath+"/util/icons/urgent.gif";
*/
String pxmag = m_context + "/util/icons/colorPix/1px.gif";
String flea = m_context + "/util/icons/buletGrey.gif";
String suggerer=m_context+"/util/icons/create-action/add-bookmark.png";
String redFlag = m_context+"/util/icons/urgent.gif";


String bodyPart="";

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
%>

<!-- listSite -->
<HTML>
<HEAD>
<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<%
out.println(gef.getLookStyleSheet());
%>
<script type="text/javascript" src="javaScript/spacesInURL.js"></script>
<!--CBO : UPDATE-->
<!--<script type="text/javascript" src="<%/*iconsPath*/%>/util/javaScript/animation.js"></script>-->
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script Language="JavaScript">

function topicGoTo(id) {
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

/*********************************************************************/

function publicationGoTo(type, theURL, nom){
    winName = "site" ;
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

/*********************************************************************/

	function openDictionnary() { //v2.0
		 theURL = "dictionnaireIcones.jsp";
		 winName = "dico";
		 larg ="480";
		 haut = "300";
		 windowParams = "scrollbars=yes, resizable, alwaysRaised";
         dico = SP_openWindow(theURL, winName, larg, haut, windowParams);
    }

/*********************************************************************/
function goto_jsp(jsp, param)
{
	window.open(jsp+param,"MyMain");
}

</script>
</HEAD>
<BODY leftmargin=5 topmargin=5>
<%

//Traitement = View, Search, Add, Update, Delete, Classify, Declassify
	if (id == null) {
		id=rootId;
		action = "Search";
	}

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

		// La barre de naviagtion
		BrowseBar browseBar = window.getBrowseBar();
		//CBO : UPDATE
		//browseBar.setDomainName(scc.getSpaceLabel());
		browseBar.setDomainName(spaceLabel);
		//CBO : UPDATE
		//browseBar.setComponentName(scc.getComponentLabel(), "javascript:goto_jsp('Main', '')");
		browseBar.setComponentName(componentLabel, "javascript:goto_jsp('Main', '')");
		browseBar.setPath(linkedPathString);

		//Les onglets
		TabbedPane tabbedPane = gef.getTabbedPane();
		//CBO : UPDATE
		//tabbedPane.addTab(resources.getString("Consulter"), "javascript:goto_jsp('listSite.jsp','')", true);
		tabbedPane.addTab(resources.getString("Consulter"), "javascript:goto_jsp('Main','')", true);
		tabbedPane.addTab(resources.getString("Organiser"), "javascript:goto_jsp('organize.jsp','')", false);
		tabbedPane.addTab(resources.getString("GML.management"), "javascript:goto_jsp('manage.jsp','')", false);

		bodyPart+=tabbedPane.print();

		//Le cadre
		Frame frame = gef.getFrame();

		// Cr�ation de la liste de navigation
		NavigationList navList = gef.getNavigationList();
		navList.setTitle("");
		Iterator i = subThemes.iterator();
		Iterator iteratorNbTool = nbToolByFolder.iterator();
		String themeName = "";
		String themeDescription = "";
		String themeId = "";
		String nbPub = "?";

        while (i.hasNext()) {
            ArrayList listSubDirectory = new ArrayList();
            NodeDetail theme = (NodeDetail) i.next();
            themeName = theme.getName();
            themeDescription = theme.getDescription();
            themeId = theme.getNodePK().getId();
            FolderDetail folder = scc.getFolder(themeId);
            Collection subItem = folder.getNodeDetail().getChildrenDetails();
            Iterator j = subItem.iterator();
            while (j.hasNext()) {
                    NodeDetail subtheme = (NodeDetail) j.next();
					//CBO : UPDATE
                    //Link l = new Link(subtheme.getName(), "javascript:goto_jsp('searchResult','Action=Search&Id="+theme.getNodePK().getId()+"')");
					Link l = new Link(subtheme.getName(), "javascript:goto_jsp('listSite.jsp?','Action=Search&Id="+theme.getNodePK().getId()+"')");
                    listSubDirectory.add(l);
             }
            /* ecriture des lignes du tableau */
            if (iteratorNbTool.hasNext())
                nbPub = ((Integer) iteratorNbTool.next()).toString();

			//Ajout d'une ligne
			navList.addItemSubItem(themeName, "javascript:goto_jsp('listSite.jsp?','Action=Search&Id="+themeId+"')", new Integer(nbPub).intValue() ,listSubDirectory);
		}

		if (subThemes.size() > 0)
		{
			//R�cup�ration du tableau dans le haut du cadre
			frame.addTop(navList.print());
		}

		//Liste des sites du th�me courant
		String liste = "";

        if (listeSites.size() > 0) {
			liste += "<TABLE CELLPADDING=3 CELLSPACING=0 ALIGN=CENTER BORDER=0 WIDTH=\"98%\"><tr><td>\n";
			//R�cup des sites
			Iterator j = listeSites.iterator();
			while (j.hasNext()) {
				PublicationDetail site = (PublicationDetail) j.next();
                String siteId = site.getVersion();
                String siteName = site.getName();
                String siteDescription = Encode.javaStringToHtmlParagraphe(site.getDescription());
				if (siteDescription == null)
					siteDescription = "";

                String sitePage = site.getContent();
                String type = new Integer(site.getImportance()).toString();
                liste += "<tr>\n";
                String listeIcones = "";
                boolean rouge = false;

                Collection icones = scc.getIcons(siteId);

                Collection c = scc.getAllIcons();
                Iterator k = c.iterator();
                while (k.hasNext()) {
					IconDetail icon = (IconDetail) k.next();
					if (iconAppartient(icon, icones)) {
						if (icon.getName().equals("Icon0"))
							rouge = true;
						else
							listeIcones += "<A href=\"#\" onclick=\"openDictionnary()\"><img src=\""+icon.getAddress()+"\" alt=\""+resources.getString(icon.getName())+"\" border=0 align=absmiddle></A>&nbsp;\n";
					}
                }

				if (rouge)
					liste+="<td valign=\"top\"><img src=\""+redFlag+"\" border=\"0\" align=absmiddle></td>\n";
				else
					liste+="<td valign=\"top\">&nbsp;</td>\n";
				//CBO : UPDATE
				/*liste += "<td valign=\"top\" align=left nowrap>&#149;&nbsp;<a class=\"textePetitBold\" href=\"javascript:onClick=publicationGoTo('"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+scc.getComponentId()+"/"+siteId+"/' , '"+Encode.javaStringToJsString(sitePage)+"')\">"+siteName+"</a></td><td align=left>\n";*/
				liste += "<td valign=\"top\" align=left nowrap>&#149;&nbsp;<a class=\"textePetitBold\" href=\"javascript:onClick=publicationGoTo('"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+componentId+"/"+siteId+"/' , '"+Encode.javaStringToJsString(sitePage)+"')\">"+siteName+"</a></td><td align=left>\n";

				liste += listeIcones;
				liste += "</td></tr><tr><td class=intfdcolor51>&nbsp;</td><td colspan=2 width=\"100%\" class=intfdcolor51><span class=\"txtnote\">&nbsp;&nbsp;"+siteDescription+"</span></td></tr><tr><td colspan=3><img src=\""+pxmag+"\" height=3 width=200></td>\n";
			}
			liste += "</td></tr></table>\n";
		} else {
			liste = "<TABLE CELLPADDING=0 CELLSPACING=0 ALIGN=CENTER BORDER=0 WIDTH=\"98%\" class=intfdcolor4><tr><td><table border=0 cellspacing=0 cellpadding=5  WIDTH=\"100%\" class=contourintfdcolor><tr><td><BR><center>"+resources.getString("NoLinkAvailable")+"</center><BR></td></tr></table></td></tr></table>";
		}

		//R�cup�ration de la liste des sites dans le cadre
		frame.addBottom(liste);

		//On crache le HTML ;o)
		bodyPart+=frame.print();
		window.addBody(bodyPart);
		out.println(window.print());
    }
%>

<FORM NAME="topicDetailForm" ACTION="listSite.jsp" METHOD=POST >
  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="<%=id%>">
   <!-- CBO : REMOVE -->
  <!-- 
  <input type="hidden" name="Path" value="<%=Encode.javaStringToHtmlString(pathString)%>">
  <input type="hidden" name="ChildId">
  <input type="hidden" name="nomSite">
  <input type="hidden" name="description">
  <input type="hidden" name="nomPage">
  <input type="hidden" name="auteur">
  <input type="hidden" name="date">
  <input type="hidden" name="ListeIcones">
	-->
</FORM>

</BODY>
</HTML>