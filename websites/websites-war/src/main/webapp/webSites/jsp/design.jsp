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
<%@ page import="java.util.Date"%>
<%@ page import="com.stratelia.webactiv.webSites.siteManage.model.SiteDetail"%>
<%@ page import="com.stratelia.webactiv.webSites.control.*"%>
<%@ page import="com.stratelia.webactiv.util.publication.model.*"%>
<%@ page import="com.stratelia.webactiv.util.publication.info.model.*"%>
<%@ page import="com.stratelia.webactiv.util.node.model.NodeDetail"%>
<%@ page import="java.io.*"%>

<%@ page import="com.stratelia.webactiv.util.exception.*"%>
<%@ page import="com.stratelia.silverpeas.silvertrace.*"%>


<%@ include file="util.jsp" %>

<%@ include file="checkScc.jsp" %>

<%!

 private ResourceLocator settings;


  /* extractFinChemin */
  private String extractFinChemin(String deb, String chemin) {
    /* deb = c:\\j2sdk\\public_html\\WAwebSiteUploads\\wa3webSite17 */
   /* chemin = c:\\j2sdk\\public_html\\WAwebSiteUploads\\wa3webSite17\\3\\rep1\\rep11 */
   /* res = 3\\rep1\\rep11 */
      int longueur = deb.length();
      String res = chemin.substring(longueur);
      return ignoreAntiSlash(res);
  }
%>

<%
//CBO : REMOVE 
/*<jsp:useBean id="thePath" scope="session" class="java.lang.String"/>
<jsp:useBean id="prems" scope="session" class="java.lang.String"/>*/


	//CBO : REMOVE settings = new ResourceLocator("com.stratelia.webactiv.webSites.settings.webSiteSettings","fr");

    //CBO : REMOVE String iconsPath = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");

    //Icons
	//CBO : UPDATE
    /*
	String addFolder=iconsPath+"/util/icons/folderAddBig.gif";
    String addPage=iconsPath+"/util/icons/webSites_page_to_add.gif";
    String addPic=iconsPath+"/util/icons/webSites_upload_file.gif";
    String addLib=iconsPath+"/util/icons/webSites_classify.gif";
    String updateDescription=iconsPath+"/util/icons/webSites_to_modify.gif";
    String belpou=iconsPath+"/util/icons/basket.gif";
    String update=iconsPath+"/util/icons/update.gif";
    String delete = iconsPath + "/util/icons/delete.gif";
	*/
	String addFolder=m_context+"/util/icons/folderAddBig.gif";
    String addPage=m_context+"/util/icons/webSites_page_to_add.gif";
    String addPic=m_context+"/util/icons/webSites_upload_file.gif";
    String addLib=m_context+"/util/icons/webSites_classify.gif";
    String updateDescription=m_context+"/util/icons/webSites_to_modify.gif";
    String belpou=m_context+"/util/icons/basket.gif";
    String update=m_context+"/util/icons/update.gif";
    String delete = m_context + "/util/icons/delete.gif";

    String action = (String) request.getParameter("Action"); /* = "newSite" la premiere fois, jamais null */
    String id = (String) request.getParameter("Id"); //jamais null sauf en creation ou en update de description
    String currentPath = (String) request.getParameter("path"); /* = null la premiere fois, rempli grace au newSite */
    //CBO : REMOVE String name = (String) request.getParameter("name"); /* = null la premiere fois, puis = nom du repertoire courant */
    //CBO : REMOVE String newName = (String) request.getParameter("newName"); /* = changement de noms des fichiers et repertoires */
    //CBO : REMOVE String nomSite = (String) request.getParameter("nomSite"); /* = rempli au premier acces a designSite pui toujours null */
    //CBO : REMOVE String description = (String) request.getParameter("description"); /* = rempli la premiere fois a la creation, puis toujours null*/
    //CBO : REMOVE String nomPage = (String) request.getParameter("nomPage"); /* = rempli la premiere fois a la creation, puis toujours null*/
    String date = "";
    String auteur = "";
    //CBO : REMOVE String tempPopup = (String) request.getParameter("popup");
    int popup = 0;
    //CBO : REMOVE if ((tempPopup != null) && (tempPopup.length() > 0))
    //CBO : REMOVE 	popup = 1;
    //CBO : REMOVE String listeIcones = (String) request.getParameter("ListeIcones"); /* = rempli la premiere fois a la creation, puis toujours null*/
    //CBO : REMOVE String listeTopics = (String) request.getParameter("ListeTopics"); /* = en cas de new Site ou de classifySite */

	//CBO : ADD
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
		nomPage = siteDetail.getContent();
	}
	boolean searchOk = true;
	Boolean theSearch = (Boolean) request.getAttribute("SearchOK");
	String theListeIcones = (String) request.getAttribute("ListeIcones");
	if(theSearch != null && theSearch == Boolean.FALSE) {
		searchOk = false;
	}
	//CBO : FIN ADD

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

    if (currentPath != null) {
		currentPath = doubleAntiSlash(currentPath);
    }

    //CBO : REMOVE boolean searchOk = true;

    SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ACTION = "+action);

	//CBO : REMOVE
    //NEW SITE -------------------------------------------------------------
/*    if (action.equals("newSite")) {
		//ADD NEW SITE  -------------------------------------------------------------
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ADD NEW  SITE = "+nomSite);

		/* recuperation de l'id */
/*		id = scc.getNextId();
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "webSite JSP designSite id = "+id);

		/* Creer le repertoire id */
		//CBO : UPDATE
		//scc.createFolder(settings.getString("uploadsPath")+settings.getString("Context")+"/"+scc.getComponentId()+"/"+id);
/*		scc.createFolder(settings.getString("uploadsPath")+settings.getString("Context")+"/"+componentId+"/"+id);

		/* creation en BD */
/*		SiteDetail descriptionSite = new SiteDetail(id, nomSite, description, nomPage, 0, null, null, 0, popup);  /* type 0 = site cree */

		//CBO : UPDATE
		//scc.createWebSite(descriptionSite);
/*		String pubId = scc.createWebSite(descriptionSite);

		if (listIcons.size() > 0)
			scc.associateIcons(id, listIcons);

		//CBO : UPDATE
		//currentPath = settings.getString("uploadsPath")+settings.getString("Context")+"/"+scc.getComponentId()+"/"+id;
		currentPath = settings.getString("uploadsPath")+settings.getString("Context")+"/"+componentId+"/"+id;

		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "avant doubleAntiSlash :  currentPath= "+currentPath);
		currentPath = doubleAntiSlash(currentPath);
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "apres doubleAntiSlash currentPath= "+currentPath);

		/* ajout de la page principale */
/*		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ADD PAGE PRINCIPALE : "+currentPath+"/"+nomPage);
		String code = " ";

		/* Creer la page principale */
/*		scc.createFile(currentPath, nomPage, code);

		/* publications : classer le site dans les themes coch�s */
/*		ArrayList arrayToClassify = new ArrayList();
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
			//CBO : REMOVE PublicationDetail pubDetail = new PublicationDetail("X", nomSite, description, null, null, null, "", "0", id, "", nomPage);

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

    //UPDATE DESCRIPTION -------------------------------------------------------------
	else if (action.equals("updateDescription")) { // type 0 design ou 2 upload
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "UPDATE DESCRIPTION id = "+id);

		String etat = (String) request.getParameter("etat");

		SiteDetail ancien = scc.getWebSite(id);
		int type = ancien.getType();

		/* verif que le nom de la page principale est correcte */
/*		Collection collPages = scc.getAllWebPages2(currentPath);
		Iterator j = collPages.iterator();
		boolean ok = false;
		while (j.hasNext()) {
			File f = (File) j.next();
			if (f.getName().equals(nomPage)) {
				ok = true;
				break;
			}
		}

		searchOk = ok;

		if (searchOk) {

			/* update description en BD */
			//CBO : REMOVE
			/*ArrayList theSite = new ArrayList();
			theSite.add(id);
			scc.deleteWebSites(theSite);*/

/*			SiteDetail descriptionSite2 = new SiteDetail(id, nomSite, description, nomPage, type, null, null, new Integer(etat).intValue(), popup);

			//CBO : UPDATE
			//scc.createWebSite(descriptionSite2);
			scc.updateWebSite(descriptionSite2);

			if (listIcons.size() > 0)
				scc.associateIcons(id, listIcons);

			/* update description des publications */
			//CBO : REMOVE
			/*Collection c = scc.getAllPublication(id);
			Iterator k = c.iterator();
			while (k.hasNext()) {
				String pubId = (String) k.next();
				PublicationDetail pub = new PublicationDetail(pubId, nomSite, description, null, null, null, null, new Integer(type).toString(), id, "", nomPage);
				scc.updatePublication(pub);
			}*/
/*		}
	}

    else if (action.equals("addFolder")) {
        //ADD FOLDER -------------------------------------------------------------
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ADD FOLDER : "+currentPath+"/"+name);

        /* Creer le nouveau repertoire */
/*        scc.createFolder(currentPath+"/"+name);

        SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "id= "+id);

        SiteDetail site = scc.getWebSite(id);
        nomSite = site.getName();

		//CBO : UPDATE
        //auteur = site.getAuthor();
		auteur = site.getCreatorId();

		//CBO : UPDATE
        //date = resources.getOutputDate(site.getDate());
		date = resources.getOutputDate(site.getCreationDate());

        popup = site.getPopup();

		//CBO : UPDATE
        //nomPage = site.getPage();
		nomPage = site.getContent();
    }

    else if (action.equals("renameFolder")) {
        //RENAME FOLDER -------------------------------------------------------------
        SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "RENAME FOLDER : "+currentPath+"/"+name);

        /* Modifier le nom du repertoire */
/*        scc.renameFolder(currentPath+"/"+name, currentPath+"/"+newName);

        SiteDetail site = scc.getWebSite(id);
        nomSite = site.getName();

		//CBO : UPDATE
        //auteur = site.getAuthor();
		auteur = site.getCreatorId();

		//CBO : UPDATE
        //date = resources.getOutputDate(site.getDate());
		date = resources.getOutputDate(site.getCreationDate());

        popup = site.getPopup();

		//CBO : UPDATE
        //nomPage = site.getPage();
		nomPage = site.getContent();
    }

    else if (action.equals("deleteFolder")) {
        //DELETE FOLDER -------------------------------------------------------------
        SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "DELETE FOLDER : "+currentPath+"/"+name);

        /* Supprimer le repertoire */
 /*       scc.delFolder(currentPath+"/"+name);

        SiteDetail site = scc.getWebSite(id);
        nomSite = site.getName();

		//CBO : UPDATE
        //auteur = site.getAuthor();
		auteur = site.getCreatorId();

		//CBO : UPDATE
        //date = resources.getOutputDate(site.getDate());
		date = resources.getOutputDate(site.getCreationDate());

        popup = site.getPopup();

		//CBO : UPDATE
        //nomPage = site.getPage();
		nomPage = site.getContent();
    }

    else if (action.equals("addPage")) {
		//ADD PAGE -------------------------------------------------------------
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ADD PAGE : "+currentPath+"/"+nomPage);
		String code = (String) request.getParameter("Code"); // = code de la page a parser
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ADD PAGE, CODE A PARSER = "+code);

		code = Encode.htmlStringToJavaString(code);
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ADD PAGE, apres encode = "+code);

		String newCode = parseCodeSupprImage(scc, code, request, settings, currentPath); /* enleve les http://localhost:8000/WAwebSiteUploads/WA0webSite17/18/  et on garde seulement rep/icon.gif */
/*		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ADD PAGE, APRES PARSE IMAGE, CODE = "+newCode);

		newCode = parseCodeSupprHref(scc, newCode, settings, currentPath); /* enleve les http://localhost:8000/webactiv/RwebSite/jsp/  et on garde seulement rep/page.html */
/*		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "ADD PAGE, APRES PARSE HREF, CODE = "+newCode);
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "currentPath = "+currentPath);
		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "nomPage = "+nomPage);
		// Creer une nouvelle page
		scc.createFile(currentPath, nomPage, newCode);

		//deduction de l'id a partir du path
		//CBO : UPDATE
		//int longueur = scc.getComponentId().length();
		int longueur = componentId.length();
		//CBO : UPDATE
		//int index2 = currentPath.lastIndexOf(scc.getComponentId());
		int index2 = currentPath.lastIndexOf(componentId);
		String chemin = currentPath.substring(index2 + longueur);

		chemin = chemin.substring(1);
		chemin = supprDoubleAntiSlash(chemin);

		String finNode = chemin;
		int index = finNode.indexOf("/");
		if (index == -1)
			id = finNode;
		else {
			id = finNode.substring(0, index);
		}

		SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "action= addPage, id du Site = "+id);

		SiteDetail site = scc.getWebSite(id);
		nomSite = site.getName();

		//CBO : UPDATE
		//auteur = site.getAuthor();
		auteur = site.getCreatorId();

		//CBO : UPDATE
		//date = resources.getOutputDate(site.getDate());
		date = resources.getOutputDate(site.getCreationDate());

		popup = site.getPopup();

		//CBO : UPDATE
		//nomPage = site.getPage();
		nomPage = site.getContent();
    }

    else if (action.equals("renamePage")) {
        //RENAME PAGE -------------------------------------------------------------
        SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "RENAME PAGE : "+currentPath+"/"+name);

        /* Modifier le nom du fichier */
/*        scc.renameFile(currentPath, name, newName);

        SiteDetail site = scc.getWebSite(id);
        nomSite = site.getName();

		//CBO : UPDATE
        //auteur = site.getAuthor();
		auteur = site.getCreatorId();

		//CBO : UPDATE
        //date = resources.getOutputDate(site.getDate());
		date = resources.getOutputDate(site.getCreationDate());

        popup = site.getPopup();

		//CBO : UPDATE
        //nomPage = site.getPage();
		nomPage = site.getContent();
    }

    else if (action.equals("deletePage")) {
        //DELETE PAGE -------------------------------------------------------------
        SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "DELETE PAGE : "+currentPath+"/"+name);

        /* Supprimer la page */
/*        scc.deleteFile(currentPath+"/"+name);

        SiteDetail site = scc.getWebSite(id);
        nomSite = site.getName();

		//CBO : UPDATE
        //auteur = site.getAuthor();
		auteur = site.getCreatorId();

		//CBO : UPDATE
        //date = resources.getOutputDate(site.getDate());
		date = resources.getOutputDate(site.getCreationDate());

        popup = site.getPopup();

		//CBO : UPDATE
        //nomPage = site.getPage();
		nomPage = site.getContent();
    }

    else if (action.equals("classifySite")) { // cas de l'upload et du design
        //CLASSIFY SITE -------------------------------------------------------------
        SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "action= classifySite, id du Site = "+id);

        SiteDetail site = scc.getWebSite(id);
        nomSite = site.getName();
        description = site.getDescription();

		//CBO : UPDATE
        //auteur = site.getAuthor();
		auteur = site.getCreatorId();

		//CBO : UPDATE
        //date = resources.getOutputDate(site.getDate());
		date = resources.getOutputDate(site.getCreationDate());

        popup = site.getPopup();

		//CBO : UPDATE
        //nomPage = site.getPage();
		nomPage = site.getContent();

        int type = site.getType(); // 0 design ou 2 upload

        /* publications : declasser completement le site */
		//CBO : REMOVE
        /*Collection c = scc.getAllPublication(id);
        Iterator k = c.iterator();
        while (k.hasNext()) {
            String pubId = (String) k.next();
            scc.deletePublication(pubId);
        }
        ArrayList arrayToDeClassify = new ArrayList();
        arrayToDeClassify.add(id);
        scc.dePublish(arrayToDeClassify);*/

		
        /* publications : classer le site dans les themes coch�s*/
/*        ArrayList arrayToClassify = new ArrayList();
        boolean publish = false;

		//CBO : UPDATE
        /*i = 0;
        begin = 0;
        end = 0;
        end = listeTopics.indexOf(',', begin);
        while(end != -1) {
            String idTopic = listeTopics.substring(begin, end);

            begin = end + 1;
            end = listeTopics.indexOf(',', begin);
            // ajout de la publication dans le theme
            PublicationDetail pubDetail = new PublicationDetail("X", nomSite, description, null, null, null, "", new Integer(type).toString(), id, "", nomPage);
            scc.getFolder(idTopic);
            String newPubId = scc.createPublication(pubDetail);
            publish = true;
        }

        if (publish) {
          arrayToClassify.add(id);
          scc.publish(arrayToClassify); //set etat du site a 1
       }*/
    
/*		ArrayList arrayTopic = new ArrayList();
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
			scc.dePublish(arrayToClassify); //set etat du site a 0
	   }
	    //CBO : FIN UPDATE
    }

    else if (action.equals("design")) {
        //DESIGN -------------------------------------------------------------
        SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "action= design, id du Site = "+id);
        SiteDetail site = scc.getWebSite(id);
        nomSite = site.getName();

		//CBO : UPDATE
        //auteur = site.getAuthor();
		auteur = site.getCreatorId();

		//CBO : UPDATE
        //date = resources.getOutputDate(site.getDate());
		date = resources.getOutputDate(site.getCreationDate());

        popup = site.getPopup();

		//CBO : UPDATE
        //nomPage = site.getPage();
		nomPage = site.getContent();
    }

/*    else {
    //AUTRE  -------------------------------------------------------------
        // view en cas de rechargement de la page pour naviguer dans le chemin
        //ou createSite annule
        // ou upload d'image
       //deduction de l'id a partir du path

		//CBO : UPDATE
		//int longueur2 = scc.getComponentId().length();
		int longueur2 = componentId.length();
		//CBO : UPDATE
		//int index2 = currentPath.lastIndexOf(scc.getComponentId());
		int index2 = currentPath.lastIndexOf(componentId);
		String chemin = currentPath.substring(index2 + longueur2);
		chemin = chemin.substring(1);
		chemin = supprDoubleAntiSlash(chemin);
		String finNode = chemin;
        int index = finNode.indexOf("/");
        if (index == -1)
            id = finNode;
        else {
            id = finNode.substring(0, index);
        }

        SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "action= view, id du Site = "+id);

        SiteDetail site = scc.getWebSite(id);
        nomSite = site.getName();

		//CBO : UPDATE
        //auteur = site.getAuthor();
		auteur = site.getCreatorId();

		//CBO : UPDATE
        //date = resources.getOutputDate(site.getDate());
		date = resources.getOutputDate(site.getCreationDate());

        popup = site.getPopup();

		//CBO : UPDATE
        //nomPage = site.getPage();
		nomPage = site.getContent();
    }*/
//CBO : FIN REMOVE
    
   	UserDetail user = scc.getUserDetail(auteur);
   	if (user != null)
    	auteur = user.getDisplayedName();
    	
	Collection collectionRep = affichageChemin(scc, currentPath);
	String infoPath = displayPath(collectionRep, true, 3, "design.jsp?Action=view&path=", nomSite);
	SilverTrace.info("webSites", "JSPdesign", "root.MSG_GEN_PARAM_VALUE", "infoPath = "+infoPath);
%>

<!-- design -->

<HTML>
<HEAD>

<%
out.println(gef.getLookStyleSheet());
%>

<TITLE><%=resources.getString("GML.popupTitle")%></TITLE>

<Script language="JavaScript">

<%

if (! searchOk) {
    out.println("alert(\""+resources.getString("PrincipalPageNotCorrectDesign")+"\")");
    if (description == null)
		description = "";

   
	//CBO : UPDATE	/*out.println("location.replace(\"modifDesc.jsp?Id="+id+"&path="+currentPath+"&type=design&RecupParam=oui&Nom="+nomSite+"&Description="+description+"&Page="+nomPage+"&ListeIcones="+listeIcones+"\");");*/
	out.println("location.replace(\"modifDesc.jsp?Id="+id+"&path="+currentPath+"&type=design&RecupParam=oui&Nom="+nomSite+"&Description="+description+"&Page="+nomPage+"&ListeIcones="+theListeIcones+"\");");

}

%>

    function URLENCODE(URL){
        URL = escape(URL);
        URL = URL.replace(/\+/g, "%2B");
        return URL;
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

        url = "addRep.jsp?Id="+id+"&Path="+path+"&Action=View";
        windowName = "repAddWindow";
        windowParams = "directories=0,menubar=0,toolbar=0,height=200,width=700,alwaysRaised";
        repAddWindow = open(url, windowName, windowParams, false);

    }

/**********************************************/
    function pageAdd(path, nomsite) {
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
        url = "addPage.jsp?Action=View&path="+URLENCODE(path)+"&nameSite="+URLENCODE(nomsite)+"&id=<%=id%>";
        windowName = "pageAddWindow";
        windowParams = "directories=0,menubar=0,toolbar=0,height=200,width=700,alwaysRaised";
        pageAddWindow = open(url, windowName, windowParams, false);
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

        <% //CBO : REMOVE session.putValue("thePath", currentPath);
           //CBO : REMOVE session.putValue("prems", "premiere fois");%>

        url = "uploadFile.jsp?path="+URLENCODE(path);
        windowName = "uploadFileWindow";
        windowParams = "directories=0,menubar=0,toolbar=0,height=200,width=700,alwaysRaised";
        uploadFileWindow = open(url, windowName, windowParams, false);
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

        url = "updateRep.jsp?Id="+id+"&Path="+path+"&Action=View&Name="+name;
        windowName = "repUpdateWindow";
        windowParams = "directories=0,menubar=0,toolbar=0,height=200,width=700,alwaysRaised";
        repUpdateWindow = open(url, windowName, windowParams, false);
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

        if (window.confirm("<%=resources.getString("MessageSuppressionFolder")%>")) {
            document.design.Action.value = "deleteFolder";
            document.design.Id.value = id;
            document.design.path.value = path;
            document.design.name.value = name;
            document.design.submit();
        }
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
  //DLE
        location.href="ToWysiwyg?path="+URLENCODE(path)+"&name="+URLENCODE(name)+"&nameSite="+URLENCODE(namesite)+"&id=<%=id%>";
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

        url = "updatePage.jsp?Id="+id+"&Path="+path+"&Action=View&Name="+name;
        windowName = "pageUpdateWindow";
        windowParams = "directories=0,menubar=0,toolbar=0,height=200,width=700,alwaysRaised";
        pageUpdateWindow = open(url, windowName, windowParams, false);

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

        if (window.confirm("<%=resources.getString("MessageSuppressionFile")%>")) {
              document.design.Action.value = "deletePage";
              document.design.Id.value = id;
              document.design.path.value = path;
              document.design.name.value = name;
              document.design.submit();
        }
    }
</Script>

</HEAD>
<BODY>

<FORM NAME="design" ACTION="verif.jsp" METHOD="POST">
  <input type="hidden" name="Action">
  <input type="hidden" name="Id">
  <input type="hidden" name="path">
  <input type="hidden" name="name">
  <input type="hidden" name="newName">
</FORM>

<FORM NAME="liste" ACTION="verif.jsp" METHOD="POST">
<%
	Window window = gef.getWindow();
	String bodyPart="";

	// La barre de naviagtion
	BrowseBar browseBar = window.getBrowseBar();
    //CBO : UPDATE
	//browseBar.setDomainName(scc.getSpaceLabel());
	browseBar.setDomainName(spaceLabel);
	//CBO : UPDATE
	//browseBar.setComponentName(scc.getComponentLabel(), "manage.jsp?Action=view");
	browseBar.setComponentName(componentLabel, "manage.jsp?Action=view");
	browseBar.setPath("<a href= \"manage.jsp?Action=view\"></a>"+infoPath);

	//Le cadre
	Frame frame = gef.getFrame();

	//Le tableau des r�pertoires
	ArrayPane arrayPaneRep = gef.getArrayPane("foldersList", "design.jsp?Action=design&path="+currentPath+"&Id="+id, request, session);
	arrayPaneRep.setVisibleLineNumber(10);
	arrayPaneRep.setTitle(resources.getString("ListeRepertoires"));

	//D�finition des colonnes du tableau
	ArrayColumn columnName = arrayPaneRep.addArrayColumn(resources.getString("GML.name"));
	ArrayColumn columnOperation = arrayPaneRep.addArrayColumn(resources.getString("FolderOperations"));
	columnOperation.setSortable(false);

	String bodyRep = "";

	//R�cup�ration du tableau dans le haut du cadre
	// desc site + path
	bodyRep += "<br><span class=\"txtnav\">"+Encode.javaStringToHtmlString(nomSite)+"</span>";
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
		arrayLine.addArrayCellLink(folderName, "design.jsp?Action=view&path="+currentPath+"/"+folderName);

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
	ArrayPane arrayPaneFile = gef.getArrayPane("fileList", "design.jsp?Action=design&path="+currentPath+"&Id="+id, request, session);
	arrayPaneFile.setVisibleLineNumber(10);
	arrayPaneFile.setTitle(resources.getString("ListeFichiers"));

	//D�finition des colonnes du tableau
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
			if (type.startsWith("htm") || type.startsWith("HTM"))
				arrayLine.addArrayCellLink(folderName, "javascript:onClick=pageRedesign('"+Encode.javaStringToJsString(currentPath)+"', '"+Encode.javaStringToJsString(folderName)+"', '"+Encode.javaStringToJsString(nomSite)+"')");
			else if (folderName.equals(nomPage))
				arrayLine.addArrayCellText(folderName);
			else arrayLine.addArrayCellText(folderName);

			//operation
			if (! folderName.equals(nomPage)) {
				IconPane iconPane = gef.getIconPane();
				Icon updateIcon = iconPane.addIcon();
				updateIcon.setProperties(update, resources.getString("Rename")+" '"+folderName+"'" , "javascript:onClick=renamePage('"+id+"', '"+Encode.javaStringToJsString(currentPath)+"', '"+Encode.javaStringToJsString(folderName)+"')");

				Icon deleteIcon = iconPane.addIcon();
				deleteIcon.setProperties(delete, resources.getString("GML.delete")+" '"+folderName+"'" , "javascript:onClick=deletePage('"+id+"', '"+Encode.javaStringToJsString(currentPath)+"', '"+Encode.javaStringToJsString(folderName)+"')");

				iconPane.setSpacing("30px");
				arrayLine.addArrayCellIconPane(iconPane);
			}
			else {
				arrayLine.addArrayCellText("");
			}

		}
	}

    //R�cup�ration du tableau dans le bas du cadre
    String bodyFile = "<br><br>";
    bodyFile+=arrayPaneFile.print();
    bodyFile += "<br><br>";

    frame.addBottom(bodyFile);


    //Les op�rations
    OperationPane operationPane = window.getOperationPane();
    operationPane.addOperation(addFolder,resources.getString("FolderAdd"), "javascript:onClick=folderAdd('"+id+"', '"+Encode.javaStringToJsString(currentPath)+"')");
    operationPane.addLine();
    operationPane.addOperation(addPage,resources.getString("PageAdd"), "javascript:onClick=pageAdd('"+Encode.javaStringToJsString(currentPath)+"', '"+Encode.javaStringToJsString(nomSite)+"')");
    operationPane.addLine();
    operationPane.addOperation(addPic,resources.getString("FileUploadAdd"), "javascript:onClick=uploadFile('"+Encode.javaStringToJsString(currentPath)+"')");
    operationPane.addLine();
    operationPane.addOperation(addLib,resources.getString("ClasserSite"), "classifySite.jsp?Id="+id+"&path="+currentPath);

    int indexSup = infoPath.indexOf(" > ");

    if (indexSup == -1) {//on est a la racine
    	operationPane.addLine();
    	operationPane.addOperation(updateDescription,resources.getString("ModificationDescription"), "modifDesc.jsp?Id="+id+"&path="+currentPath+"&type=design");
    }

    //On crache le HTML ;o)
	if (scc.isPdcUsed()) {
		TabbedPane tabbedPane = gef.getTabbedPane();
		tabbedPane.addTab(resources.getString("GML.description"), "#", true, false);
		tabbedPane.addTab(resources.getString("GML.PDC"), "pdcPositionsSite.jsp?Action=ViewPdcPositions&Id="+id+"&Path="+currentPath, false, true);
		bodyPart += tabbedPane.print();
	}

	bodyPart += frame.print();
    window.addBody(bodyPart);
    out.println(window.print());
%>
</FORM>
</BODY>
</HTML>