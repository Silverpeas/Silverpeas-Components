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
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

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
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.browseBars.BrowseBar"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayLine"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayColumn"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.arrayPanes.ArrayCellText"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.iconPanes.IconPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.icons.Icon"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.operationPanes.OperationPane"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.navigationList.NavigationList"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.frame.Frame"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.window.Window"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.board.Board"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.*"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.*"%>
<%@ page import="com.stratelia.webactiv.util.publication.info.model.*"%>

<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>


<%!

  /**
    * Called on :
    *
    */
    private boolean dejaPublie(String siteId, Collection liste) {
          SilverTrace.info("websites", "JSPclassifyDeclassify", "root.MSG_GEN_PARAM_VALUE",
                           "deja publie : id = "+siteId);

          Iterator l = liste.iterator();
          while(l.hasNext()) {
            SiteDetail site = (SiteDetail) l.next();
            String id = site.getSitePK().getId();
            SilverTrace.info("websites", "JSPclassifyDeclassify", "root.MSG_GEN_PARAM_VALUE",
                             "deja publie compare a : id = "+id);
            if (id.equals(siteId))
                return true;
          }
          return false;
    }

  /**
    * Called on :
    *
    */
    private Collection remove(Collection c, SiteDetail site) {
          String theId = site.getSitePK().getId();
          SilverTrace.info("websites", "JSPclassifyDeclassify", "root.MSG_GEN_PARAM_VALUE",
                           "remove : id = "+theId);

          ArrayList resultat = new ArrayList();
          Iterator k = c.iterator();
          while(k.hasNext()) {
               SiteDetail sitedetail = (SiteDetail) k.next();
               String id = sitedetail.getSitePK().getId();

               if (! id.equals(theId))
                  resultat.add(sitedetail);
          }
          return resultat;
    }
%>


<%

ResourceLocator settings = new ResourceLocator("org.silverpeas.webSites.settings.webSiteSettings","fr");
String checkSite=m_context+"/util/icons/ok.gif";
String addSite=m_context+"/util/icons/webSites_to_add.gif";
String declass=m_context+"/util/icons/webSites_trash.gif";

//Recuperation des parametres
String action = request.getParameter("Action"); //jamais null
String id = request.getParameter("TopicId");//jamais null
String linkedPathString = request.getParameter("Path");//jamais null
Collection listeSites = (Collection) request.getAttribute("ListSites");
FolderDetail webSitesCurrentFolder = (FolderDetail) request.getAttribute("CurrentFolder");
%>

<!-- classifyDeclassify -->

<HTML>
<HEAD>

<%
out.println(gef.getLookStyleSheet());
%>

<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script Language="JavaScript">

function topicGoTo(id) {
    document.topicDetailForm.Action.value = "Search";
    document.topicDetailForm.Id.value = id;
    document.topicDetailForm.submit();
}

/******************************************************************************************/

function classify(nbSite) {
    listeSiteToBePublished = "";

    if (nbSite == 1) {
        if (document.liste.classifySite.checked)
            listeSiteToBePublished += document.liste.classifySite.value + ",";
    }
    else {
        for (i=0; i<nbSite; i++) {
          if (document.liste.classifySite[i].checked)
                      listeSiteToBePublished += document.liste.classifySite[i].value + ",";
         }
    }

    document.topicDetailForm.Action.value = "classify";
    document.topicDetailForm.SiteList.value = listeSiteToBePublished;
    document.topicDetailForm.submit();
}

/******************************************************************************************/

function declassify(nbSite) {
    listeSiteToBeDePublished = "";

    if (nbSite == 1) {
        if (document.liste.declassSite.checked)
            listeSiteToBeDePublished += document.liste.declassSite.value + ",";
    }
    else {
        for (i=0; i<nbSite; i++) {
          if (document.liste.declassSite[i].checked)
                      listeSiteToBeDePublished += document.liste.declassSite[i].value + ",";
         }
    }

    document.topicDetailForm.Action.value = "declassify";
    document.topicDetailForm.SiteList.value = listeSiteToBeDePublished;
    document.topicDetailForm.submit();
}

/******************************************************************************************/

function publicationGoTo(type, theURL, nom){
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

</script>
</HEAD>


<BODY leftmargin=5 topmargin=5>

<FORM NAME="topicDetailForm" ACTION="organize.jsp" METHOD=POST >
  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="<%=id%>">
  <input type="hidden" name="SiteList">
</FORM>

<FORM NAME="liste">

<%

	//CBO : REMOVE Collection listeSites = scc.getAllWebSite();
	SilverTrace.info("websites", "JSPclassifyDeclassify", "root.MSG_GEN_PARAM_VALUE",
					 "taille de l'array site  = "+listeSites.size());
	//CBO : REMOVE webSitesCurrentFolder = scc.getSessionTopic();
	Collection listeSitesPublies = webSitesCurrentFolder.getPublicationDetails();
	SilverTrace.info("websites", "JSPclassifyDeclassify", "root.MSG_GEN_PARAM_VALUE",
					 "taille de la liste des sites publiés  = "+listeSitesPublies.size());

	if (listeSitesPublies.size() > 0) {
		 Iterator k = listeSitesPublies.iterator();
		  while(k.hasNext()) {
			   PublicationDetail sitepublieTheme = (PublicationDetail) k.next();
			   String siteId = sitepublieTheme.getVersion();
			   if (dejaPublie(siteId, listeSites)) {
					  SiteDetail siteToDelete = scc.getWebSite(siteId);
					  ArrayList a = new ArrayList(listeSites);
					  listeSites = remove(a, siteToDelete);
			   }
		  }
	}

	ArrayList arraySites = new ArrayList(listeSites);

    Window window = gef.getWindow();
    String bodyPart="";

    // La barre de naviagtion
    BrowseBar browseBar = window.getBrowseBar();
	  browseBar.setDomainName(spaceLabel);
    browseBar.setComponentName(componentLabel, "organize.jsp");
    if (linkedPathString.equals(""))
        browseBar.setPath(resources.getString("ClasserDeclasser"));
    else browseBar.setPath(linkedPathString+" - "+resources.getString("ClasserDeclasser"));


    //Les operations
    OperationPane operationPane = window.getOperationPane();
    operationPane.addOperation(addSite, resources.getString("AjouterSites"), "javascript:onClick=classify('"+arraySites.size()+"')");
    operationPane.addLine();
    operationPane.addOperation(declass, resources.getString("DeclasserSites"), "javascript:onClick=declassify('"+listeSitesPublies.size()+"')");

    //Le cadre
    Frame frame = gef.getFrame();

    //Le tableau de tri
    ArrayPane arrayPane = gef.getArrayPane("siteList", "classifyDeclassify.jsp?Action=View&TopicId="+id+"&Path="+Encode.javaStringToHtmlString(linkedPathString), request, session);
    arrayPane.setVisibleLineNumber(1000);
    arrayPane.setTitle(resources.getString("ListeSitesNonPubliesTheme"));
    //D�finition des colonnes du tableau
    arrayPane.addArrayColumn(resources.getString("GML.name"));
    arrayPane.addArrayColumn(resources.getString("GML.description"));
    ArrayColumn arrayColumnStatus = arrayPane.addArrayColumn(resources.getString("GML.status"));
    arrayColumnStatus.setSortable(false);
    ArrayColumn arrayColumnDel = arrayPane.addArrayColumn("&nbsp;");
    arrayColumnDel.setSortable(false);

    SiteDetail site;
    int i=0;
    while (i < arraySites.size()) {
	    site = (SiteDetail) arraySites.get(i);
	    /* ecriture des lignes du tableau */
	    String theId = site.getSitePK().getId();
	    String nom = site.getName();
	    String theDescription = site.getDescription();
		if (theDescription == null)
			theDescription = "";
    
		String thePage = site.getContent();

	    String type = new Integer(site.getType()).toString();
	    int theEtat = site.getState();

		ArrayLine arrayLine = arrayPane.addArrayLine();

	    if (nom.length() > 40)
	          nom = nom.substring(0, 40) + "...";

      arrayLine.addArrayCellLink(nom, "javascript:onClick=publicationGoTo('"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+componentId+"/"+theId+"/' , '"+Encode.javaStringToJsString(thePage)+"')");

	    if (theDescription.length() > 80)
	          theDescription = theDescription.substring(0, 80) + "...";
	    arrayLine.addArrayCellText(theDescription);

		if (theEtat == 1) {
			//Ajout de l'icones publie
			IconPane iconPanePub1 = gef.getIconPane();
			Icon checkIcon1 = iconPanePub1.addIcon();
			checkIcon1.setProperties(checkSite,resources.getString("SitePublie"));
			arrayLine.addArrayCellIconPane(iconPanePub1);
		}
		else {
			arrayLine.addArrayCellText("");
		}
		arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"classifySite\" value=\""+theId+"\">");
		i++;
	}

    //Recuperation du tableau dans le haut du cadre
    frame.addTop(arrayPane.print());

	//Le board
	Board board = gef.getBoard();

	String liste = "";

	if (listeSitesPublies.size() > 0) {
		liste += "<table border=\"0\">\n";
		//R�cup des sites
		Iterator j = listeSitesPublies.iterator();
		while (j.hasNext()) {
			PublicationDetail sitepublie = (PublicationDetail) j.next();
			String pubId = sitepublie.getPK().getId();
			String siteName = sitepublie.getName();
			String siteDescription = sitepublie.getDescription();
			if (siteDescription == null)
				siteDescription = "";

			String sitePage = sitepublie.getContent();
			String type = new Integer(sitepublie.getImportance()).toString();
			String siteId = sitepublie.getVersion();

			liste += "<tr>\n";
			liste += "<td valign=\"top\" width=\"5%\"><input type=\"checkbox\" name=\"declassSite\" value=\""+pubId+"\"></td>\n";

			//CBO : UPDATE
			/*liste += "<td valign=\"top\"><a class=\"textePetitBold\" href=\"javascript:onClick=publicationGoTo('"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+scc.getComponentId()+"/"+siteId+"/' , '"+Encode.javaStringToJsString(sitePage)+"')\">"+siteName+"</a><br>\n";*/
			liste += "<td valign=\"top\"><a class=\"textePetitBold\" href=\"javascript:onClick=publicationGoTo('"+type+"', 'http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+componentId+"/"+siteId+"/' , '"+Encode.javaStringToJsString(sitePage)+"')\">"+siteName+"</a><br>\n";

			liste += "<span class=\"txtnote\">"+siteDescription+"</span><br><br></td>\n";
		}

		liste += "</table>\n";
		board.addBody(liste);
	}

    //Recuperation de la liste des sites dans le cadre
	if(listeSitesPublies.size() > 0) {
		frame.addBottom(board.print());
	}

    //On crache le HTML ;o)
    bodyPart+=frame.print();
    window.addBody(bodyPart);
	out.println(window.print());
%>
</FORM>
</BODY>
</HTML>