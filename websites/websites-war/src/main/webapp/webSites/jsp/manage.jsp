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
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.SiteDetail"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.*"%>
<%@ page import="com.stratelia.webactiv.util.publication.info.model.*"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="com.stratelia.webactiv.util.viewGenerator.html.Encode"%>

<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>

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

//CBO : REMOVE String action		= (String) request.getParameter("Action");
//CBO : REMOVE String id			= (String) request.getParameter("Id"); /* cas de l'update */
//CBO : REMOVE String nomSite		= (String) request.getParameter("nomSite");
//CBO : REMOVE String description	= (String) request.getParameter("description");
//CBO : REMOVE String nomPage		= (String) request.getParameter("nomPage");
//CBO : REMOVE String tempPopup	= (String) request.getParameter("popup");

//CBO : REMOVE 
/*int popup = 0;
if ((tempPopup != null) && (tempPopup.length() > 0))
	popup = 1;*/
//CBO : REMOVE String listeIcones	= (String) request.getParameter("ListeIcones");
//CBO : REMOVE String listeTopics	= (String) request.getParameter("ListeTopics");



//CBO : REMOVE
/*ArrayList listIcons = new ArrayList();
int i = 0;
int begin = 0;
int end = 0;
if (listeIcones != null) {
  end = listeIcones.indexOf(',', begin);
  while(end != -1) {
      listIcons.add(listeIcones.substring(begin, end));
      begin = end + 1;
      end = listeIcones.indexOf(',', begin);
  }
}*/

//CBO : REMOVE String letat = (String) request.getParameter("etat"); /* cas du update : 0 si non publie, 1 si publie */
//CBO : REMOVE
/*SilverTrace.info("websites", "JSPmanage", "root.MSG_GEN_PARAM_VALUE", "letat= "+letat);
int etat = -1;
if (letat != null) {
  SilverTrace.info("websites", "JSPmanage", "root.MSG_GEN_PARAM_VALUE", "letat= "+letat);
  if (!letat.equals(""))
    etat = new Integer(letat).intValue();
}*/

//CBO : ADD
Collection listeSites = (Collection) request.getAttribute("ListSites");


/* code */
//CBO : REMOVE
/*
if (action != null) {

  //ADD DESCRIPTION -------------------------------------------------------------

  if (action.equals("addBookmark")) {
	SilverTrace.info("websites", "JSPmanage", "root.MSG_GEN_PARAM_VALUE", "ADD NEW BOOKMARK");

	/* recuperation de l'id */
/*	id = scc.getNextId();

	/* creation en BD */
/*	SiteDetail descriptionSite = new SiteDetail(id, nomSite, description, nomPage, 1, null, null, 0, popup); /* type 1 = bookmark */
	
	//CBO : UPDATE
	//scc.createWebSite(descriptionSite);
/*	String pubId = scc.createWebSite(descriptionSite);

	if (listIcons.size() > 0)
		scc.associateIcons(id, listIcons);

	if (nomPage.indexOf("://")==-1)
	{
		nomPage = "http://" + nomPage;
	}

	ArrayList arrayToClassify = new ArrayList();
	boolean publish = false;
	i = 0;
	begin = 0;
	end = 0;
	end = listeTopics.indexOf(',', begin);
	while(end != -1) {
		String idTopic = listeTopics.substring(begin, end);

		begin = end + 1;
		end = listeTopics.indexOf(',', begin);
		// ajout de la publication dans le theme
		//CBO : REMOVE PublicationDetail pubDetail = new PublicationDetail("X", nomSite, description, null, null, null, "", "1", id, "", nomPage);

		//CBO : REMOVE scc.getFolder(idTopic);

		//CBO : UPDATE
		//String newPubId = scc.createPublication(pubDetail);
		scc.addPublicationToFolder(pubId, idTopic);

		publish = true;
	}

	if (publish) {
	  arrayToClassify.add(id);
	  scc.publish(arrayToClassify); //set etat du site a 1
   }
  }


	//DELETE DESCRIPTION -------------------------------------------------------------
  else if (action.equals("deleteWebSites")) {

      ArrayList listToDelete = new ArrayList();

      String liste = (String) request.getParameter("SiteList");

      i = 0;
      begin = 0;
      end = 0;
      end = liste.indexOf(',', begin);
      while(end != -1) {
          String idToDelete = liste.substring(begin, end);
          listToDelete.add(idToDelete);

          // recup info sur ce webSite
          SiteDetail info = scc.getWebSite(idToDelete);
          int type = info.getType(); /* type = 0 : site cree, type = 1 : site bookmark, type = 2 : site upload */

/*          if (type != 1) { //type != bookmark
                //delete directory
				//CBO : UPDATE
				//scc.deleteDirectory(settings.getString("uploadsPath")+settings.getString("Context")+"/"+scc.getComponentId()+"/"+idToDelete);
				scc.deleteDirectory(settings.getString("uploadsPath")+settings.getString("Context")+"/"+componentId+"/"+idToDelete);
          }

			//delete publication
			//CBO : UPDATE
			/*Collection c = scc.getAllPublication(idToDelete);
			Iterator k = c.iterator();
			while (k.hasNext()) {
				String pubId = (String) k.next();
				scc.deletePublication(pubId);
			}*/
/*			String pubId = scc.getIdPublication(idToDelete);
			scc.deletePublication(pubId);
			//CBO : FIN UPDATE

          begin = end + 1;
          end = liste.indexOf(',', begin);
      }

      /* delete en BD */
/*      scc.deleteWebSites(listToDelete);
  }


    //UPDATE DESCRIPTION -------------------------------------------------------------
    else  if (action.equals("updateDescription")) {
      SilverTrace.info("websites", "JSPmanage", "root.MSG_GEN_PARAM_VALUE", "UPDATE DESCRIPTION id = "+id);

      SiteDetail ancien = scc.getWebSite(id);
      int type = ancien.getType();

      /* update description en BD */

	  //CBO : REMOVE
      /*ArrayList theSite = new ArrayList();
      theSite.add(id);
      scc.deleteWebSitesFromUpdate(theSite);*/
	 
/*      SiteDetail descriptionSite2 = new SiteDetail(id, nomSite, description, nomPage, type, null, null, etat, popup);

	  //CBO : UPDATE
      //scc.createWebSite(descriptionSite2);
	  scc.updateWebSite(descriptionSite2);

      if (listIcons.size() > 0)
          scc.associateIcons(id, listIcons);

	//CBO : REMOVE
    /* publications : declasser completement le site */
    /*Collection c = scc.getAllPublication(id);
    Iterator k = c.iterator();
    while (k.hasNext()) {
        String pubId = (String) k.next();
        scc.deletePublication(pubId);
    }
    ArrayList arrayToDeClassify = new ArrayList();
    arrayToDeClassify.add(id);
    scc.dePublish(arrayToDeClassify);*/
	//CBO : FIN REMOVE


    /* publications : classer le site dans les themes coch�s*/
/*    ArrayList arrayToClassify = new ArrayList();
	//CBO : UPDATE
    boolean publish = false;
    /*i = 0;
    begin = 0;
    end = 0;
    end = listeTopics.indexOf(',', begin);
    while(end != -1) {
        String idTopic = listeTopics.substring(begin, end);

        begin = end + 1;
        end = listeTopics.indexOf(',', begin);
    
		// ajout de la publication dans le theme
        PublicationDetail pubDetail = new PublicationDetail("X", nomSite, description, null, null, null, "", "1", id, "", nomPage);
        scc.getFolder(idTopic);
        String newPubId = scc.createPublication(pubDetail);
        publish = true;
    }

    if (publish) {
      arrayToClassify.add(id);
      scc.publish(arrayToClassify); //set etat du site a 1
   }*/

/*	ArrayList arrayTopic = new ArrayList();
	i = 0;
    begin = 0;
    end = 0;
    end = listeTopics.indexOf(',', begin);
	String idTopic = null;
    while(end != -1) {
        idTopic = listeTopics.substring(begin, end);

        begin = end + 1;
        end = listeTopics.indexOf(',', begin);
    
		arrayTopic.add(idTopic);
		publish = true;
    }

	scc.updateClassification(id, arrayTopic);

	arrayToClassify.add(id);
	if (publish) {
		scc.publish(arrayToClassify); //set etat du site a 1
    } else {
		scc.dePublish(arrayToClassify);
	}
   //CBO : FIN UPDATE

 } 
} */
//CBO : FIN REMOVE

%>

<HTML>
<HEAD>

<%
out.println(gef.getLookStyleSheet());
%>

<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>
<script type="text/javascript" src="<%=m_context%>/util/javaScript/animation.js"></script>
<Script language="JavaScript">

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

</Script>
</HEAD>
<BODY>

<FORM NAME="descriptionSite" ACTION="descUpload.jsp" METHOD="POST" ENCTYPE="multipart/form-data">
  <input type="hidden" name="Action">
</FORM>


<FORM NAME="liste_liens" ACTION="manage.jsp" >
  <input type="hidden" name="Action">
  <input type="hidden" name="SiteList">

<%

	//CBO : REMOVE listeSites = scc.getAllWebSite();
    arraySites = new ArrayList(listeSites);
    SilverTrace.info("websites", "JSPmanage", "root.MSG_GEN_PARAM_VALUE", "taille de l'arraySites= "+arraySites.size());


    Window window = gef.getWindow();
    String bodyPart="";

    // La barre de naviagtion
    BrowseBar browseBar = window.getBrowseBar();
	//CBO : UPDATE
	//browseBar.setDomainName(scc.getSpaceLabel());
    browseBar.setDomainName(spaceLabel);
	//CBO : UPDATE
    //browseBar.setComponentName(scc.getComponentLabel(), "manage.jsp");
	browseBar.setComponentName(componentLabel, "manage.jsp");

    //Les op�rations
    OperationPane operationPane = window.getOperationPane();

	if (scc.isPdcUsed() && role.equals("Admin")) {
		//CBO : UPDATE
		/*operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+scc.getComponentId()+"','utilizationPdc1')");*/
		operationPane.addOperation(pdcUtilizationSrc, resources.getString("GML.PDCParam"), "javascript:onClick=openSPWindow('"+m_context+"/RpdcUtilization/jsp/Main?ComponentId="+componentId+"','utilizationPdc1')");
		operationPane.addLine();
	}

    operationPane.addOperation(bookmark, resources.getString("BookmarkSite"), "javascript:onClick=B_SPECIFIER_BOOK_ONCLICK();");
    if (bookmarkMode)
    {
    	operationPane.addOperation(bookmarkDelete, resources.getString("SupprimerSite"), "javascript:onClick=deleteWebSites('"+listeSites.size()+"')");
    } 
    else 
    {
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
	//D�finition des colonnes du tableau
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
			/*arrayLine.addArrayCellLink(nom, "javascript:onClick=designSite('"+doubleAntiSlash(settings.getString("uploadsPath")+settings.getString("Context")+"/"+scc.getComponentId()+"/"+theId)+"', '"+theId+"')");*/
			arrayLine.addArrayCellLink(nom, "javascript:onClick=designSite('"+doubleAntiSlash(settings.getString("uploadsPath")+settings.getString("Context")+"/"+componentId+"/"+theId)+"', '"+theId+"')");
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

	//R�cup�ration du tableau dans le haut du cadre
	frame.addTop(arrayPane.print());
	frame.addBottom("");

	//On crache le HTML ;o)
	bodyPart+=tabbedPane.print();
	bodyPart+=frame.print();
	window.addBody(bodyPart);
	out.println(window.print());
%>
</FORM>
</BODY>
</HTML>