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

<%@ page import="java.lang.*"%>
<%@ page import="com.stratelia.webactiv.util.*"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.*"%>
<%@ page import="com.stratelia.webactiv.util.publication.info.model.*"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>


<%@ include file="checkScc.jsp" %>
<%@ include file="util.jsp" %>

<%

ResourceLocator settings;
//CBO : REMOVE Collection listeSites;
ArrayList arraySites;

settings = new ResourceLocator("com.stratelia.webactiv.webSites.settings.webSiteSettings", "fr");

//CBO : REMOVE String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

String role 		= (String) request.getAttribute("BestRole");

//Icons
String bookmark			= m_context+"/util/icons/bookmark_to_add.gif";
String bookmarkDelete	= m_context+"/util/icons/bookmark_to_remove.gif";

String upload			= m_context+"/util/icons/webSites_upload.gif";
String create			= m_context+"/util/icons/webSites_to_design.gif";
String belpou 			= m_context+"/util/icons/webSites_to_del.gif";

String webLink			= m_context+"/util/icons/webLink.gif";
String localLink		= m_context+"/util/icons/computer.gif";
String check			= m_context+"/util/icons/ok.gif";
String pdcUtilizationSrc = m_context+"/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";


/* recup parametres */


//CBO : ADD
Collection listeSites = (Collection) request.getAttribute("ListSites");

%>

<html>
<head>

<%
out.println(gef.getLookStyleSheet());
%>

<title><%=resources.getString("GML.popupTitle")%></title>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<script type="text/javascript">

function URLENCODE(URL){
    URL = escape(URL);
    URL = URL.replace(/\+/g, "%2B");
    return URL;
}

    /*************************************************/

function B_SPECIFIER_BOOK_ONCLICK() {
    location.href="descBookmark.jsp";
}

    /*************************************************/

function B_SPECIFIER_UPLOAD_ONCLICK() {
    document.descriptionSite.submit();
}

    /*************************************************/

function B_SPECIFIER_DESIGN_ONCLICK() {
    location.href="descDesign.jsp";
}

    /*************************************************/

function deleteWebSites(nbSite) {

    listeSite = "";

    if (nbSite > 0) {
      if (nbSite == 1) {
            if (document.liste_liens.supLien.checked)
                    listeSite += document.liste_liens.supLien.value + ",";
      }

      else {
        for (i=0; i<nbSite; i++) {
            if (document.liste_liens.supLien[i] != null) {
                if (document.liste_liens.supLien[i].checked)
                    listeSite += document.liste_liens.supLien[i].value + ",";
            }
            else break;
        }
      }


      if (listeSite != "") {   //on a coch� au - un site
        if (window.confirm("<%=resources.getString("MessageSuppressionLien")%>")) {
        document.liste_liens.Action.value = "deleteWebSites";
        document.liste_liens.SiteList.value = listeSite;
        document.liste_liens.submit();
        }
      }
    }
}

    /*************************************************/

function updateDescription (id) {
        location.href = "modifDesc.jsp?Id="+id;
}

    /*************************************************/

    /* modification des contenus et de l'arborescence du site crees et uploades */
function designSite(path, id) {
    location.href = "design.jsp?Action=design&path="+URLENCODE(path)+"&Id="+id;
}

function openSPWindow(fonction, windowName){
	pdcUtilizationWindow = SP_openWindow(fonction, windowName, '600', '400','scrollbars=yes, resizable, alwaysRaised');
}

</script>
</head>
<body>

<form name="descriptionSite" action="descUpload.jsp" method="post" enctype="multipart/form-data">
  <input type="hidden" name="Action">
</form>


<form name="liste_liens" action="manage.jsp" >
  <input type="hidden" name="Action">
  <input type="hidden" name="SiteList">

<%

	//CBO : REMOVE listeSites = scc.getAllWebSite();
  arraySites = new ArrayList(listeSites);
  SilverTrace.info("websites", "JSPmanage", "root.MSG_GEN_PARAM_VALUE", "taille de l'arraySites= "+arraySites.size());


  Window window = gef.getWindow();
  String bodyPart="";
  BrowseBar browseBar = window.getBrowseBar();
  browseBar.setDomainName(spaceLabel);
	browseBar.setComponentName(componentLabel, "manage.jsp");

  //Les operations
  OperationPane operationPane = window.getOperationPane();

	if (scc.isPdcUsed() && "Admin".equals(role)) {
		operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+componentId+"','utilizationPdc1')");
		operationPane.addLine();
	}

  operationPane.addOperation(bookmark, resources.getString("BookmarkSite"), "javascript:onClick=B_SPECIFIER_BOOK_ONCLICK();");
  if (bookmarkMode) {
  	operationPane.addOperation(bookmarkDelete, resources.getString("SupprimerSite"), "javascript:onClick=deleteWebSites('"+listeSites.size()+"')");
  } else {
		operationPane.addOperation(upload, resources.getString("UploadSite"), "javascript:onClick=B_SPECIFIER_UPLOAD_ONCLICK();");
		operationPane.addOperation(create, resources.getString("DesignSite"), "javascript:onClick=B_SPECIFIER_DESIGN_ONCLICK();");
		operationPane.addOperation(belpou, resources.getString("SupprimerSite"), "javascript:onClick=deleteWebSites('"+listeSites.size()+"')");
	}
	

  //Les onglets
  TabbedPane tabbedPane = gef.getTabbedPane();
  tabbedPane.addTab(resources.getString("Consulter"), "listSite.jsp", false);
  tabbedPane.addTab(resources.getString("Organiser"), "organize.jsp", false);
  tabbedPane.addTab(resources.getString("GML.management"), "manage.jsp", true);

  //Le cadre
  Frame frame = gef.getFrame();

  //Le tableau de tri
  ArrayPane arrayPane = gef.getArrayPane("foldersList", "manage.jsp", request, session);
	arrayPane.setVisibleLineNumber(10);
	arrayPane.setTitle(resources.getString("ListeSites"));
	//Definition des colonnes du tableau
	arrayPane.addArrayColumn(resources.getString("GML.name"));
	arrayPane.addArrayColumn(resources.getString("GML.description"));
	if (! bookmarkMode) {
		ArrayColumn arrayColumnType = arrayPane.addArrayColumn(resources.getString("GML.type"));
		arrayColumnType.setSortable(false);
	}
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

		int theType = site.getType();
		int theEtat = site.getState();
		ArrayLine arrayLine = arrayPane.addArrayLine();

		//nom
		if (nom.length() > 40)
			  nom = nom.substring(0, 40) + "...";
		if (theType == 1) {// site externe
			arrayLine.addArrayCellLink(nom, "javascript:onClick=updateDescription('"+theId+"')");
		}
		else {//site interne
			//CBO : UPDATE
			arrayLine.addArrayCellLink(nom, "javascript:onClick=designSite('"+doubleAntiSlash(settings.getString("uploadsPath")+componentId+"/"+theId)+"', '"+theId+"')");
		}

		//desc
		if (theDescription.length() > 80)
			  theDescription = theDescription.substring(0, 80) + "...";
		arrayLine.addArrayCellText(theDescription);

		//type
		if (! bookmarkMode) {
			if (theType == 1) {// site externe
				IconPane iconPaneType2 = gef.getIconPane();
				Icon typeIcon2 = iconPaneType2.addIcon();
				typeIcon2.setProperties(webLink,resources.getString("SiteExterne"));
				arrayLine.addArrayCellIconPane(iconPaneType2);
			}
			else {//site interne
				IconPane iconPaneType1 = gef.getIconPane();
				Icon typeIcon1 = iconPaneType1.addIcon();
				typeIcon1.setProperties(localLink,resources.getString("SiteLocal"));
				arrayLine.addArrayCellIconPane(iconPaneType1);
			}
		}

		//etat
		if (theEtat == 1) {//Ajout de l'icones publi�
			IconPane iconPanePub1 = gef.getIconPane();
			Icon checkIcon1 = iconPanePub1.addIcon();
			checkIcon1.setProperties(check,resources.getString("SitePublie"));
			arrayLine.addArrayCellIconPane(iconPanePub1);
		}
		else {
			arrayLine.addArrayCellText("");
		}

		//check
		arrayLine.addArrayCellText("<input type=\"checkbox\" name=\"supLien\" value=\""+theId+"\">");

		i++;
  }

	//Recuperation du tableau dans le haut du cadre
	frame.addTop(arrayPane.print());
	frame.addBottom("");

	//On crache le HTML ;o)
	bodyPart+=tabbedPane.print();
	bodyPart+=frame.print();
	window.addBody(bodyPart);
	out.println(window.print());
%>
</form>
</body>
</html>