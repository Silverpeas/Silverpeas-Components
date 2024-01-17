<%--
  ~ Copyright (C) 2000 - 2024 Silverpeas
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License as
  ~ published by the Free Software Foundation, either version 3 of the
  ~ License, or (at your option) any later version.
  ~
  ~ As a special exception to the terms and conditions of version 3.0 of
  ~ the GPL, you may redistribute this Program in connection with Free/Libre
  ~ Open Source Software ("FLOSS") applications as described in Silverpeas's
  ~ FLOSS exception.  You should have received a copy of the text describing
  ~ the FLOSS exception, and it is also available here:
  ~ "https://www.silverpeas.org/legal/floss_exception.html"
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <https://www.gnu.org/licenses/>.
  --%>
<%@ page import="org.silverpeas.components.websites.servlets.WebSitesRequestRouter" %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>


<%!

  /**
    * Called on :
    *
    */
    private boolean dejaPublie(String siteId, Collection<SiteDetail> liste) {
      for (final SiteDetail site : liste) {
        String id = site.getSitePK().getId();
        if (id.equals(siteId)) {
          return true;
        }
      }
      return false;
    }

  /**
    * Called on :
    *
    */
    private Collection remove(Collection<SiteDetail> c, SiteDetail site) {
      String theId = site.getSitePK().getId();
      List<SiteDetail> resultat = new ArrayList<>();
      for (final SiteDetail sitedetail : c) {
        String id = sitedetail.getSitePK().getId();
        if (!id.equals(theId)) {
          resultat.add(sitedetail);
        }
      }
      return resultat;
    }
%>


<%

String checkSite=m_context+"/util/icons/ok.gif";
String addSite=m_context+"/util/icons/webSites_to_add.gif";
String declass=m_context+"/util/icons/webSites_trash.gif";

//Recuperation des parametres
String id = request.getParameter("TopicId");//jamais null
String linkedPathString = request.getParameter("Path");//jamais null
Collection<SiteDetail> listeSites = (Collection) request.getAttribute("ListSites");
FolderDetail webSitesCurrentFolder =
    (FolderDetail) request.getAttribute(WebSitesRequestRouter.CURRENT_FOLDER_PARAM);
%>

<!-- classifyDeclassify -->

<view:sp-page>
<view:sp-head-part>
<view:script src="javaScript/commons.js"/>
<script type="text/javascript">

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

window.wsm = new WebSiteManager({
  contextUrl : 'organize.jsp',
  forceSitePopupOpening : true
});
</script>
</view:sp-head-part>
<view:sp-body-part>
<form name="topicDetailForm" action="organize.jsp" method=POST >
  <input type="hidden" name="Action">
  <input type="hidden" name="Id" value="<%=id%>">
  <input type="hidden" name="SiteList">
</form>

<form name="liste">

<%
	Collection<PublicationDetail> listeSitesPublies = webSitesCurrentFolder.getPublicationDetails();
	if (listeSitesPublies.size() > 0) {
    for (final PublicationDetail sitepublieTheme : listeSitesPublies) {
      String siteId = sitepublieTheme.getVersion();
      if (dejaPublie(siteId, listeSites)) {
        SiteDetail siteToDelete = scc.getWebSite(siteId);
        List<SiteDetail> a = new ArrayList<>(listeSites);
        listeSites = remove(a, siteToDelete);
      }
    }
	}

	List<SiteDetail> arraySites = new ArrayList<>(listeSites);

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

    for (SiteDetail site : arraySites) {
	    /* ecriture des lignes du tableau */
	    String theId = site.getSitePK().getId();
	    String nom = site.getName();
	    String theDescription = site.getDescription();
		if (theDescription == null)
			theDescription = "";

	    int theEtat = site.getState();

		ArrayLine arrayLine = arrayPane.addArrayLine();

	    if (nom.length() > 40)
	          nom = nom.substring(0, 40) + "...";

      arrayLine.addArrayCellLink(nom, "javascript:onClick=wsm.goToSite('"+theId+"')");

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
	}

    //Recuperation du tableau dans le haut du cadre
    frame.addTop(arrayPane.print());

	//Le board
	Board board = gef.getBoard();

	String liste = "";

	if (listeSitesPublies.size() > 0) {
		liste += "<table border=\"0\">\n";
		//Récup des sites
    for (final PublicationDetail sitepublie : listeSitesPublies) {
      String pubId = sitepublie.getPK().getId();
      String siteName = sitepublie.getName();
      String siteDescription = sitepublie.getDescription();
      if (siteDescription == null) {
        siteDescription = "";
      }

      String siteId = sitepublie.getVersion();

      liste += "<tr>\n";
      liste += "<td valign=\"top\" width=\"5%\"><input type=\"checkbox\" name=\"declassSite\" value=\"" + pubId + "\"></td>\n";

      liste +=
          "<td valign=\"top\"><a class=\"textePetitBold\" href=\"javascript:onClick=wsm.goToSite('" + siteId + "')\">" + siteName + "</a><br>\n";

      liste += "<span class=\"txtnote\">" + siteDescription + "</span><br><br></td>\n";
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
</form>
</view:sp-body-part>
</view:sp-page>