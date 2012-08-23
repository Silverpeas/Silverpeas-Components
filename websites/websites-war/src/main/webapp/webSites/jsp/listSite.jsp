<%--

    Copyright (C) 2000 - 2011 Silverpeas

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

<%@ page import="java.util.*"%>
<%@ page import="java.lang.*"%>

<%@ page import="com.stratelia.webactiv.util.*"%>

<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.*"%>
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
    private boolean iconAppartient(IconDetail iconDetail, Collection c) {
		SilverTrace.info("websites", "JSPlisteSite", "root.MSG_GEN_PARAM_VALUE", "appartient");
		boolean ok = false;

		String theId = iconDetail.getIconPK().getId();
		SilverTrace.info("websites", "JSPlisteSite", "root.MSG_GEN_PARAM_VALUE", "theId= "+theId);

		Iterator i = c.iterator();
		while(i.hasNext() && !ok) {
			IconDetail icon = (IconDetail) i.next();
			String id = icon.getIconPK().getId();
			SilverTrace.info("websites", "JSPlisteSite", "root.MSG_GEN_PARAM_VALUE", "id= "+id);
			if (theId.equals(id))
				ok = true;
			SilverTrace.info("websites", "JSPlisteSite", "root.MSG_GEN_PARAM_VALUE", "ok= "+ok);
		}
		return ok;
    }


%>


<%

ResourceLocator settings;
String rootId = "0";
String action;
String id;
String name;
String linkedPathString = "";
String pathString = "";
FolderDetail webSitesCurrentFolder = null;

settings = new ResourceLocator("com.stratelia.webactiv.webSites.settings.webSiteSettings","fr");

//Icons
//CBO : UPDATE
/*
String pxmag = iconsPath + "/util/icons/colorPix/1px.gif";
String flea = iconsPath + "/util/icons/buletGrey.gif";
String suggerer=iconsPath+"/util/icons/webSites_to_propose.gif";
String redFlag = iconsPath+"/util/icons/urgent.gif";
*/
String pxmag = m_context + "/util/icons/colorPix/1px.gif";
String flea = m_context + "/util/icons/buletGrey.gif";
String suggerer=m_context+"/util/icons/bookmark_to_add.gif";
String redFlag = m_context+"/util/icons/urgent.gif";

String bodyPart="";

// Retrieve parameter
action = (String) request.getParameter("Action");
id = (String) request.getParameter("Id");

//CBO : ADD
webSitesCurrentFolder = (FolderDetail) request.getAttribute("CurrentFolder");

// Update space
if (action == null) {
    SilverTrace.info("websites", "JSPlisteSite", "root.MSG_GEN_PARAM_VALUE", "action NULL");
    id = rootId;
    action = "Search";
}


%>

<!-- listSite -->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel />
<script type="text/javascript" src="javaScript/spacesInURL.js"></script>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

function topicGoTo(id) {
	document.topicDetailForm.Action.value = "Search";
  document.topicDetailForm.Id.value = id;
  document.topicDetailForm.submit();
}

/*********************************************************************/

function publicationGoTo(popup, type, theURL, nom){
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

	if (popup == "0") {
		document.topicDetailForm.action = "DisplaySite";
		document.topicDetailForm.SitePage.value = theURL;
		document.topicDetailForm.submit();
	}
	else {
		site = window.open(theURL,winName,windowParams);
	}
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


function openSPWindow(fonction, windowName){
  pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

</script>

</head>
<body>
<%

	//Traitement = View, Search, Add, Update, Delete, Classify, Declassify
	if (id == null) {
		id=rootId;
		action = "Search";
	}

	SilverTrace.info("websites", "JSPlisteSite", "root.MSG_GEN_PARAM_VALUE", "action = "+action);

	/* SEARCH */
	if (action.equals("Search")) {

		SilverTrace.info("websites", "JSPlisteSite", "root.MSG_GEN_PARAM_VALUE", "action = Search");

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

    String bestRole = (String) request.getAttribute("BestRole");
    OperationPane operationPane = window.getOperationPane();
    if ("Admin".equals(bestRole) && scc.isPdcUsed()) {
      String pdcUtilizationSrc  = m_context + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";
      operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId=" + scc.getComponentId() + "','utilizationPdc1')");
      operationPane.addLine();
    }
 
    
		// La barre de naviagtion
		BrowseBar browseBar = window.getBrowseBar();
		//CBO : UPDATE
		//browseBar.setDomainName(scc.getSpaceLabel());
		browseBar.setDomainName(spaceLabel);
		//CBO : UPDATE
		//browseBar.setComponentName(scc.getComponentLabel(), "listSite.jsp");
		browseBar.setComponentName(componentLabel, "Main");
		browseBar.setPath(linkedPathString);

    //Les onglets
    TabbedPane tabbedPane = gef.getTabbedPane();
		//CBO : UPDATE
    //tabbedPane.addTab(resources.getString("Consulter"), "listSite.jsp", true);
		tabbedPane.addTab(resources.getString("Consulter"), "Main", true);
		tabbedPane.addTab(resources.getString("Organiser"), "organize.jsp", false);
    tabbedPane.addTab(resources.getString("GML.management"), "manage.jsp", false);

    bodyPart+=tabbedPane.print();


		//Le cadre
		Frame frame = gef.getFrame();

		// Creation de la liste de navigation
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
				Link l = new Link(subtheme.getName(), "listSite.jsp?Action=Search&Id="+subtheme.getNodePK().getId());
				listSubDirectory.add(l);
			}
            /* ecriture des lignes du tableau */
      if (iteratorNbTool.hasNext()) {
				nbPub = ((Integer) iteratorNbTool.next()).toString();
      }
			//Ajout d'une ligne
			navList.addItemSubItem(themeName, "listSite.jsp?Action=Search&Id="+themeId, new Integer(nbPub).intValue() ,listSubDirectory);
		}

		if (subThemes.size() > 0)
		{
			//Recuperation du tableau dans le haut du cadre
			frame.addTop(navList.print());
		}

		//Liste des sites du th�me courant
		String liste = "";

		if (listeSites.size() > 0) {
			liste += "<TABLE CELLPADDING=3 CELLSPACING=0 ALIGN=CENTER BORDER=0 WIDTH=\"98%\"><tr><td>\n";
			//Recup des sites
			Iterator j = listeSites.iterator();
			while (j.hasNext()) {
				PublicationDetail site = (PublicationDetail) j.next();
				String siteId = site.getVersion();
				String siteName = site.getName();
				String siteDescription = EncodeHelper.javaStringToHtmlParagraphe(site.getDescription());
				if (siteDescription == null) {
					siteDescription = "";
				}
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
							listeIcones += "<A href=\"#\" onclick=\"openDictionnary()\"><img src=\""+icon.getAddress()+"\" alt=\""+resources.getString(icon.getName())+"\" border=0 align=absmiddle title=\""+resources.getString(icon.getName())+"\"></A>&nbsp;\n";
					}
				}

				if (rouge)
					liste+="<td valign=\"top\"><img src=\""+redFlag+"\" border=\"0\" align=absmiddle></td>\n";
				else
					liste+="<td valign=\"top\">&nbsp;</td>\n";

				SiteDetail siteDetail = scc.getWebSite(siteId);

				//CBO : UPDATE
				/*liste += "<td valign=\"top\" align=left nowrap>&#149;&nbsp;<a class=\"textePetitBold\" href=\"javascript:onClick=publicationGoTo('" + siteDetail.getPopup() + "', '"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+scc.getComponentId()+"/"+siteId+"/' , '"+EncodeHelper.javaStringToJsString(sitePage)+"')\">"+siteName+"</a></td><td align=left>\n";*/
				liste += "<td valign=\"top\" align=left nowrap>&#149;&nbsp;<a class=\"textePetitBold\" href=\"javascript:onClick=publicationGoTo('" + siteDetail.getPopup() + "', '"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+componentId+"/"+siteId+"/' , '"+EncodeHelper.javaStringToJsString(sitePage)+"')\">"+siteName+"</a></td><td align=left>\n";

				liste += listeIcones;
				liste += "</td></tr><tr><td class=intfdcolor51>&nbsp;</td><td colspan=2 width=\"100%\" class=intfdcolor51><span class=\"txtnote\">"+siteDescription+"</span></td></tr><tr><td colspan=3><img src=\""+pxmag+"\" height=3 width=200></td>\n";
			}
			liste += "</td></tr></table>\n";
		} else {
			liste = "<TABLE CELLPADDING=0 CELLSPACING=0 ALIGN=CENTER BORDER=0 WIDTH=\"98%\" class=intfdcolor4><tr><td><table border=0 cellspacing=0 cellpadding=5  WIDTH=\"100%\" class=contourintfdcolor><tr><td><BR><center>"+resources.getString("NoLinkAvailable")+"</center><BR></td></tr></table></td></tr></table>";
		}

		//Recuperation de la liste des sites dans le cadre
		frame.addBottom(liste);

		//On crache le HTML ;o)
		bodyPart+=frame.print();
		window.addBody(bodyPart);
		out.println(window.print());
	}
%>

<form name="topicDetailForm" action="listSite.jsp" method="post">
  <input type="hidden" name="Action"/>
  <input type="hidden" name="Id" value="<%=id%>" />
  <!-- CBO : REMOVE -->
  <!-- 
  <input type="hidden" name="Path" value="<%=EncodeHelper.javaStringToHtmlString(pathString)%>">
  <input type="hidden" name="ChildId">
  <input type="hidden" name="nomSite">
  <input type="hidden" name="description">
  <input type="hidden" name="nomPage">
  <input type="hidden" name="auteur">
  <input type="hidden" name="date">
  <input type="hidden" name="ListeIcones">
  -->
  <input type="hidden" name="SitePage"/>
</form>

</body>
</html>