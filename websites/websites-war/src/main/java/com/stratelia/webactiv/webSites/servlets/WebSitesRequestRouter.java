package com.stratelia.webactiv.webSites.servlets;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.webSites.control.WebSiteSessionController;
import com.stratelia.webactiv.webSites.siteManage.model.FolderDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;

/*
 * CVS Informations
 * 
 * $Id: WebSitesRequestRouter.java,v 1.15 2008/12/04 14:41:23 neysseri Exp $
 * 
 * $Log: WebSitesRequestRouter.java,v $
 * Revision 1.15  2008/12/04 14:41:23  neysseri
 * Erreur lors de la suggestion d'un site par un lecteur.
 * De plus, seuls les publieurs étaient notifiés (pas les gestionnaires).
 *
 * Revision 1.14  2008/07/03 06:30:39  neysseri
 * Ajout de traces
 *
 * Revision 1.13  2008/04/16 07:56:50  cbonin
 * correction bug téléchargement site web
 *
 * Revision 1.12  2008/04/16 07:10:21  neysseri
 * no message
 *
 * Revision 1.11.2.11  2008/04/14 10:27:50  cbonin
 * ajout en session du site web courant
 *
 * Revision 1.11.2.10  2008/04/11 12:44:32  cbonin
 * Remontée code -> Request Router
 *
 * Revision 1.11.2.8  2008/04/10 15:10:37  cbonin
 * Remontée code -> Request Router
 *
 * Revision 1.11.2.7  2008/04/10 09:23:41  cbonin
 * Remontée code -> Request Router
 *
 * Revision 1.11.2.6  2008/04/08 07:01:09  cbonin
 * Remontée code -> Request Router
 *
 * Revision 1.11.2.5  2008/04/07 16:31:03  cbonin
 * Remontée code -> Request Router
 *
 * Revision 1.11.2.4  2008/04/07 11:29:56  cbonin
 * Modifs checkScc -> utilisation de browseContext, spaceName etc....
 *
 * Revision 1.11.2.3  2008/04/04 09:35:36  cbonin
 * Ordonnancement des publis
 *
 * Revision 1.11.2.2  2008/04/03 14:19:41  cbonin
 * 1 site -> 1 publi
 *
 * Revision 1.11.2.1  2008/04/01 12:49:55  cbonin
 * Ordonnancement des thèmes
 *
 * Revision 1.11  2007/10/24 15:04:06  dlesimple
 * gestion http ou https vers wysiwyg par URLManager.getHttpmode()
 *
 * Revision 1.10  2007/07/09 15:12:01  cbonin
 * Ajout en request de BookmarkMode
 *
 * Revision 1.9  2005/07/04 10:04:51  dlesimple
 * Intégration WYSIWYG
 *
 * Revision 1.8  2005/05/12 16:21:50  neysseri
 * no message
 *
 * Revision 1.7  2005/05/04 14:50:06  sdevolder
 * *** empty log message ***
 *
 * Revision 1.6  2004/10/05 13:18:46  dlesimple
 * Couper/Coller composant
 *
 * Revision 1.5  2004/07/27 14:25:50  neysseri
 * Le rôle " Gestionnaire " n'était pas pris en compte correctement. Il avait les mêmes privilèges que le rôle " Lecteur ".
 * + Nettoyage eclipse
 *
 * Revision 1.4  2004/06/22 16:33:00  neysseri
 * implements new SilverContentInterface + nettoyage eclipse
 *
 * Revision 1.3  2004/02/11 09:54:42  neysseri
 * integration of webSites in PDC
 *
 * Revision 1.2  2003/01/17 15:44:06  neysseri
 * Site suggestion improvement
 *
 * Revision 1.1.1.1  2002/08/06 14:48:01  nchaix
 * no message
 *
 * Revision 1.4  2002/05/17 15:09:55  nchaix
 * Merge de la branche bug001 sur la branche principale
 *
 * Revision 1.3.4.1  2002/04/25 06:57:50  santonio
 * portlétisation
 *
 */
 
/**
 * Class declaration
 *
 *
 * @author
 */
public class WebSitesRequestRouter extends ComponentRequestRouter
{

    /**
     * This method has to be implemented in the component request router class.
     * returns the session control bean name to be put in the request object
     * ex : for almanach, returns "almanach"
     */
    public String getSessionControlBeanName()
    {
        return "webSites";
    }

    /**
     * Method declaration
     *
     *
     * @param mainSessionCtrl
     * @param componentContext
     *
     * @return
     *
     * @see
     */
    public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
    {
        ComponentSessionController component = (ComponentSessionController) new WebSiteSessionController(mainSessionCtrl, componentContext);

        return component;
    }

    /**
     * This method has to be implemented by the component request router
     * it has to compute a destination page
     * @param function The entering request function (ex : "Main.jsp")
     * @param componentSC The component Session Control, build and initialised.
     * @param request The entering request. The request router need it to get parameters
     * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach.jsp?flag=user")
     */
    public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
    {

        SilverTrace.info("webSites", "WebSitesRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "fonction = " + function);
        String destination = "";

        // the flag is the best user's profile
        String flag = getFlag(componentSC.getUserRoles());
        request.setAttribute("BestRole", flag);
		WebSiteSessionController scc = (WebSiteSessionController) componentSC;
		
        try
        {

            if (function.startsWith("Main"))
            {
            	//CBO : ADD
            	FolderDetail webSitesCurrentFolder = scc.getFolder("0");
            	scc.setSessionTopic(webSitesCurrentFolder);
            	request.setAttribute("CurrentFolder", webSitesCurrentFolder);
            	//CBO : FIN ADD
            	
                if (flag.equals("Publisher") || flag.equals("Admin"))
                {
                    destination = "/webSites/jsp/listSite.jsp";
                }
                else// reader
                {
                    destination = "/webSites/jsp/listSite_reader.jsp";
                }
            }
            
            //CBO : ADD
            else if (function.startsWith("listSite.jsp"))
            {
            	String action = (String) request.getParameter("Action");
            	String id = (String) request.getParameter("Id");
            	
            	if (action == null) {
            	    id = "0";
            	    action = "Search";
            	}
            	
            	FolderDetail webSitesCurrentFolder = scc.getFolder(id);
            	scc.setSessionTopic(webSitesCurrentFolder);
            	request.setAttribute("CurrentFolder", webSitesCurrentFolder);
            	
                destination = "/webSites/jsp/listSite.jsp?Action="+action+"&Id="+id;
            }
            
            else if (function.startsWith("listSite_reader.jsp"))
            {
            	String action = (String) request.getParameter("Action");
            	String id = (String) request.getParameter("Id");
            	
            	if (action == null) {
            	    id = "0";
            	    action = "Search";
            	}
            	
            	FolderDetail webSitesCurrentFolder = scc.getFolder(id);
            	scc.setSessionTopic(webSitesCurrentFolder);
            	request.setAttribute("CurrentFolder", webSitesCurrentFolder);
            	
                destination = "/webSites/jsp/listSite_reader.jsp?Action="+action+"&Id="+id;
            }
            //CBO : FIN ADD
            
            //CBO : REMOVE
            /*else if (function.startsWith("manage")) 
			{
            	request.setAttribute("BookmarkMode", new Boolean(scc.isBookmarkMode()));
				destination = "/webSites/jsp/manage.jsp";
			}*/

			else if (function.startsWith("portlet")) 
			{
				//CBO : ADD
            	FolderDetail webSitesCurrentFolder = scc.getFolder("0");
            	scc.setSessionTopic(webSitesCurrentFolder);
            	request.setAttribute("CurrentFolder", webSitesCurrentFolder);
            	//CBO : FIN ADD
            	
				if (flag.equals("Publisher") || flag.equals("Admin"))
				{	
					destination = "/webSites/jsp/listSitePortlet.jsp";
				}
				else       // reader
				{
					destination = "/webSites/jsp/listSite_readerPortlet.jsp";
				}
			}
            else if (function.startsWith("searchResult"))
            {
                String id = request.getParameter("Id");  /* id de la publication */
                String typeRequest = request.getParameter("Type");

                if ("Publication".equals(typeRequest) || "Site".equals(typeRequest))
                {
                    // recherche de l'url complete d'acces a la page
                    try
                    {
						SiteDetail sitedetail = null;
						if (typeRequest.equals("Site"))
						{
							sitedetail = scc.getWebSite(id);
						}
						else
						{
							PublicationDetail pubDetail = scc.getPublicationDetail(id);
							sitedetail = scc.getWebSite(pubDetail.getVersion());
						}

                        destination = getWebSitesDestination(sitedetail, request, scc);
                    }
                    catch (Exception e)
                    {
                        SilverTrace.warn("webSites", "WebSitesRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", null, e);
                    }
                }

                else if ("Node".equals(typeRequest)) {
                	//CBO : UPDATE
                    //destination = scc.getComponentUrl() + "Main.jsp?Action=Search&Id=" + id;
                	destination = scc.getComponentUrl() + "listSite.jsp?Action=Search&Id=" + id;
            	}
            }

			else if (function.equals("SuggestLink"))
			{
				String nomSite = (String) request.getParameter("nomSite");
				String description = (String) request.getParameter("description");
				String nomPage = (String) request.getParameter("nomPage");
				String auteur = (String) request.getParameter("auteur");
				String date = (String) request.getParameter("date");
				String listeIcones = (String) request.getParameter("ListeIcones");

				int begin = 0;
				int end = 0;
				end = listeIcones.indexOf(',', begin);
				String listeMessage = "";

				//parcours des icones
				while(end != -1) {
				  String nom = listeIcones.substring(begin, end);
				  listeMessage += "- "+nom + "\n";
				  begin = end + 1;
				  end = listeIcones.indexOf(',', begin);
				}

				scc.notifyPublishers(auteur,nomSite,description,nomPage,listeMessage,date);

				request.setAttribute("SuggestionName", nomSite);
				request.setAttribute("SuggestionUrl", nomPage);
				destination = getDestination("Main", componentSC, request);
			}
			
			else if (function.equals("DisplaySite"))
			{
				String sitePage = request.getParameter("SitePage");
				destination = sitePage;
			}
			else if (function.startsWith("ToWysiwyg"))
			{
				String path		= request.getParameter("path");
				String name		= request.getParameter("name");
				String nameSite = request.getParameter("nameSite");
				String id		= request.getParameter("id");
				
				//CBO : UPDATE
				//destination = URLManager.getHttpMode() + scc.getServerNameAndPort()+URLManager.getApplicationURL()+"/wysiwyg/jsp/htmlEditor.jsp?";
				destination = "http://"+getMachine(request) + URLManager.getApplicationURL()+"/wysiwyg/jsp/htmlEditor.jsp?";
				
				destination += "SpaceId="+scc.getSpaceId();
				
				//CBO : UPDATE
				/*destination += "&SpaceName="+URLEncoder.encode(scc.getSpaceLabel());
				"ISO-8859-1"
				destination += "&ComponentId="+scc.getComponentId();
				destination += "&ComponentName="+URLEncoder.encode(scc.getComponentLabel());
				destination += "&BrowseInfo="+URLEncoder.encode(nameSite);
				destination += "&Language=fr";
				destination += "&ObjectId="+id;
				destination += "&FileName="+URLEncoder.encode(name);
				destination += "&Path="+URLEncoder.encode(path);
				destination += "&ReturnUrl="+URLEncoder.encode(URLManager.getApplicationURL()+URLManager.getURL(scc.getSpaceId(), scc.getComponentId())+"FromWysiwyg?path="+path+"&name="+name+"&nameSite="+nameSite+"&profile="+flag+"&id="+id);
		        */
				destination += "&SpaceName="+URLEncoder.encode(scc.getSpaceLabel(), "ISO-8859-1");
				destination += "&ComponentId="+scc.getComponentId();
				destination += "&ComponentName="+URLEncoder.encode(scc.getComponentLabel(), "ISO-8859-1");
				destination += "&BrowseInfo="+URLEncoder.encode(nameSite, "ISO-8859-1");
				destination += "&Language=fr";
				destination += "&ObjectId="+id;
				destination += "&FileName="+URLEncoder.encode(name, "ISO-8859-1");
				destination += "&Path="+URLEncoder.encode(path, "ISO-8859-1");
				destination += "&ReturnUrl="+URLEncoder.encode(URLManager.getApplicationURL()+URLManager.getURL(scc.getSpaceId(), scc.getComponentId())+"FromWysiwyg?path="+path+"&name="+name+"&nameSite="+nameSite+"&profile="+flag+"&id="+id, "ISO-8859-1");
		        //CBO : FIN UPDATE
				SilverTrace.info("webSites", "WebSitesRequestRouter.getDestination().ToWysiwyg", "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);
			}
			else if (function.startsWith("FromWysiwyg"))
			{
				String path		= request.getParameter("path");
				String id		= request.getParameter("id");
				
				//CBO : ADD
				SiteDetail site = scc.getWebSite(id);
		        request.setAttribute("Site", site);
		        //CBO : FIN ADD
		            
				destination = "/webSites/jsp/design.jsp?Action=design&path="+path+"&Id="+id;
		        SilverTrace.info("webSites", "WebSitesRequestRouter.getDestination().FromWysiwyg", "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);
			}
            
            //CBO : ADD
			else if (function.equals("TopicUp"))
			{
				String topicId = request.getParameter("Id");

				scc.changeTopicsOrder("up", topicId);
				
				String id = scc.getSessionTopic().getNodePK().getId();
				FolderDetail webSitesCurrentFolder = scc.getFolder(id);
            	scc.setSessionTopic(webSitesCurrentFolder);
            	request.setAttribute("CurrentFolder", webSitesCurrentFolder);
            	
				destination = "/webSites/jsp/organize.jsp?Action=Search&Id=" + id;
			}
			else if (function.equals("TopicDown"))
			{
				String topicId = request.getParameter("Id");

				scc.changeTopicsOrder("down", topicId);
				
				String id = scc.getSessionTopic().getNodePK().getId();
				FolderDetail webSitesCurrentFolder = scc.getFolder(id);
            	scc.setSessionTopic(webSitesCurrentFolder);
            	request.setAttribute("CurrentFolder", webSitesCurrentFolder);
				
				destination = "/webSites/jsp/organize.jsp?Action=Search&Id=" + id;
			}
			else if (function.equals("PubUp"))
			{
				String pubId = request.getParameter("Id");

				scc.changePubsOrder(pubId, -1);
				
				String id = scc.getSessionTopic().getNodePK().getId();
				FolderDetail webSitesCurrentFolder = scc.getFolder(id);
            	scc.setSessionTopic(webSitesCurrentFolder);
            	request.setAttribute("CurrentFolder", webSitesCurrentFolder);
				
				destination = "/webSites/jsp/organize.jsp?Action=Search&Id=" + id;
			}
			else if (function.equals("PubDown"))
			{
				String pubId = request.getParameter("Id");

				scc.changePubsOrder(pubId, 1);
				
				String id = scc.getSessionTopic().getNodePK().getId();
				FolderDetail webSitesCurrentFolder = scc.getFolder(id);
            	scc.setSessionTopic(webSitesCurrentFolder);
            	request.setAttribute("CurrentFolder", webSitesCurrentFolder);
				
				destination = "/webSites/jsp/organize.jsp?Action=Search&Id=" + id;
			}
			else if (function.startsWith("modifDesc.jsp"))
			{
				String id = (String) request.getParameter("Id"); 
				String currentPath = (String) request.getParameter("path"); /* = null ou rempli si type= design */
				String type = (String) request.getParameter("type"); // null  ou design
				
				request.setAttribute("Site", scc.getWebSite(id));
				request.setAttribute("AllIcons", scc.getAllIcons());
				request.setAttribute("ListIcons", scc.getIcons(id));
				
				String recupParam = (String) request.getParameter("RecupParam"); //=null ou oui
				String complete = null;
				if (recupParam != null) {//=oui
					String nom = (String) request.getParameter("Nom"); 
					String description = (String) request.getParameter("Description"); 
					String lapage = (String) request.getParameter("Page"); 
					String listeIcones = (String) request.getParameter("ListeIcones"); 
				
					type = "design";
					complete = "&RecupParam=oui&Nom="+nom+"&Description="+description+"&Page="+lapage+"&ListeIcones="+listeIcones;					
				} else {
					destination = "/webSites/jsp/modifDesc.jsp?Id="+id+"&path="+currentPath+"&type="+type;					
				}
				
				destination = "/webSites/jsp/modifDesc.jsp?Id="+id;
				if(complete != null) {
					destination += complete;
				}
				if(currentPath != null) {
					destination += "&path="+currentPath;
				}
				if(type != null) {
					destination += "&type="+type;
				}
			}
			else if (function.equals("Suggest"))
			{
				request.setAttribute("AllIcons", scc.getAllIcons());
				request.setAttribute("Action", "suggest");
				
				destination = "/webSites/jsp/descBookmark.jsp";
			}
			else if (function.startsWith("descBookmark.jsp"))
			{
				request.setAttribute("AllIcons", scc.getAllIcons());
				
				destination = "/webSites/jsp/descBookmark.jsp";
			}
			else if (function.startsWith("descDesign.jsp"))
			{
				request.setAttribute("AllIcons", scc.getAllIcons());
				
				destination = "/webSites/jsp/descDesign.jsp";
				
			}
			else if(function.startsWith("organize.jsp")) {
				
            	String action = (String) request.getParameter("Action");
            	String id = (String) request.getParameter("Id");
            	String path = (String) request.getParameter("Path");
            
            	if(action == null) {
            		action = "Search";
            	} 
            	else if (action.equals("Update")) {
            		String childId = (String) request.getParameter("ChildId");
            		String name = (String) request.getParameter("Name");
            		String description = (String) request.getParameter("Description");
            		NodeDetail folder = new NodeDetail(childId,name,description,null,null,null,"0","X");
            		scc.updateFolderHeader(folder, "");
            		action = "Search";
            	} 
            	else if (action.equals("Delete")) { /* declassification des sites et suppression des themes */
            		
            		/* delete folder */
            		int i = 0;
            		String[] listeId = request.getParameterValues("checkbox");
            		if (listeId == null) {
            			String Id = (String) request.getParameter("checkbox");
            			if (Id != null) {
            				//delete theme et publications
            				scc.deleteFolder(Id);
            			}
            	   } else {
            		   String idFolderToDelete;
            			while (i < listeId.length) {
            				idFolderToDelete = (String) listeId[i];
            				//delete theme et publications
            				scc.deleteFolder(idFolderToDelete);
            				i++;
            			}
            		}

            		/* quels sont les sites a depublier */
            		ArrayList arrayToDePublish = new ArrayList();
            		Collection liste = scc.getAllWebSite();
            		Iterator j = liste.iterator();
            		SiteDetail site;
            		while (j.hasNext()) {
            			site = (SiteDetail) j.next();

            			if(scc.getIdPublication(site.getSitePK().getId()) == null) {
            				arrayToDePublish.add(site.getSitePK().getId());
            			}
            		 }

            		// dePublish
            		if (arrayToDePublish.size() > 0) {
            			scc.dePublish(arrayToDePublish);
            		}

            		/* declassify sites */
            		String listeSite = (String) request.getParameter("SiteList");
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
            			listNodePk = scc.getAllFatherPK(idPubToDeClassify);
            			if (listNodePk.size() == 0) {
            				arrayToDePublish.add(pub.getVersion());
            			}

            			begin = end + 1;
            			end = listeSite.indexOf(',', begin);
            			
            		}

            		// dePublish
            		if (arrayToDePublish.size() > 0) {
            			scc.dePublish(arrayToDePublish);
            		}

            		action = "Search";
            	}
            	else if (action.equals("classify")) {
            		String listeSite = (String) request.getParameter("SiteList");
            		ArrayList arrayToClassify = new ArrayList();
            		int begin = 0;
            		int end = 0;
            		end = listeSite.indexOf(',', begin);

            		String idSiteToClassify;
            		String pubId = null;
            		while(end != -1) {
            			idSiteToClassify = listeSite.substring(begin, end);
            			arrayToClassify.add(idSiteToClassify);
            			
            			pubId = scc.getIdPublication(idSiteToClassify);

            			scc.addPublicationToFolder(pubId, id);

            			begin = end + 1;
            			end = listeSite.indexOf(',', begin);
            		}
            		if (arrayToClassify.size() > 0) {
            			scc.publish(arrayToClassify); //set etat du site a 1
            		}

            		action = "Search";
            	}
            	else if (action.equals("declassify")) {

            		String listeSite = (String) request.getParameter("SiteList");

            		ArrayList arrayToDeClassify = new ArrayList();
            		int begin = 0;
            		int end = 0;
            		end = listeSite.indexOf(',', begin);
            		String idSiteToDeClassify = null;
            		String pubId = null;
            		Collection listNodePk;
            		while(end != -1) {
            			pubId = listeSite.substring(begin, end); // pubId

            			scc.removePublicationToFolder(pubId, id);

            			/* isPublished dans un autre theme */
            			listNodePk = scc.getAllFatherPK(pubId);
            			if (listNodePk.size() == 0) {
            				PublicationDetail pubDetail = scc.getPublicationDetail(pubId);
            				idSiteToDeClassify = pubDetail.getVersion();
            				arrayToDeClassify.add(idSiteToDeClassify);
            			}

            			begin = end + 1;
            			end = listeSite.indexOf(',', begin);
            		}

            		if (arrayToDeClassify.size() > 0) {
            			scc.dePublish(arrayToDeClassify); //set etat du site a 0
            		}
            		action = "Search";
            	}
            	
            	if(id == null) {
            		id = "0";
            	}
            	
            	FolderDetail webSitesCurrentFolder = scc.getFolder(id);
            	scc.setSessionTopic(webSitesCurrentFolder);
            	request.setAttribute("CurrentFolder", webSitesCurrentFolder);
            	
                destination = "/webSites/jsp/organize.jsp?Action="+action+"&Id="+id+"&Path="+path;
            }
			else if(function.equals("AddTopic")) {
				
				String action = (String) request.getParameter("Action");//=Add
				String fatherId = (String) request.getParameter("Id");
				String newTopicName = (String) request.getParameter("Name");
				String newTopicDescription = (String) request.getParameter("Description");
				    
				NodeDetail folder = new NodeDetail("X",newTopicName,newTopicDescription,null,null,null,"0","X");
				scc.addFolder(folder, "");
            	
                destination = "/webSites/jsp/addTopic.jsp?Action="+action+"&Id="+fatherId;
            }
			else if(function.startsWith("updateTopic")) {
				
				String id = (String) request.getParameter("ChildId");
				String path = (String) request.getParameter("Path");
				
				NodeDetail folderDetail = scc.getFolderDetail(id);
				request.setAttribute("CurrentFolder", folderDetail);
            	
                destination = "/webSites/jsp/updateTopic.jsp?ChildId="+id+"&Path="+path;
            }
			else if(function.startsWith("classifyDeclassify.jsp")) {
				
            	String action = (String) request.getParameter("Action");
            	String id = (String) request.getParameter("TopicId");
            	String linkedPathString = (String) request.getParameter("Path");
            	
            	Collection listeSites = scc.getAllWebSite();
            	request.setAttribute("ListSites", listeSites);
            	request.setAttribute("CurrentFolder", scc.getSessionTopic());
            	
            	destination = "/webSites/jsp/classifyDeclassify.jsp?Action="+action+"&TopicId="+id+"&Path="+linkedPathString;
			}
			else if(function.startsWith("manage.jsp")) {
				String action		= (String) request.getParameter("Action"); 
				
            	if (action != null && action.equals("addBookmark")) {
    				String nomSite		= (String) request.getParameter("nomSite");
    				String description	= (String) request.getParameter("description");
    				String nomPage		= (String) request.getParameter("nomPage");
    				String tempPopup	= (String) request.getParameter("popup");
    				String listeIcones	= (String) request.getParameter("ListeIcones");
    				String listeTopics	= (String) request.getParameter("ListeTopics");
    				
    				int popup = 0;
    				if ((tempPopup != null) && (tempPopup.length() > 0))
    					popup = 1;
    				
    				ArrayList listIcons = new ArrayList();
    				int begin = 0;
    				int end = 0;
    				if (listeIcones != null) {
    				  end = listeIcones.indexOf(',', begin);
    				  while(end != -1) {
    				      listIcons.add(listeIcones.substring(begin, end));
    				      begin = end + 1;
    				      end = listeIcones.indexOf(',', begin);
    				  }
    				}
    				
    				/* recuperation de l'id */
    				String id = scc.getNextId();

    				/* creation en BD */
    				SiteDetail descriptionSite = new SiteDetail(id, nomSite, description, nomPage, 1, null, null, 0, popup); /* type 1 = bookmark */
    				
    				String pubId = scc.createWebSite(descriptionSite);

    				if (listIcons.size() > 0) {
    					scc.associateIcons(id, listIcons);
    				}

    				if (nomPage.indexOf("://")==-1)
    				{
    					nomPage = "http://" + nomPage;
    				}

    				ArrayList arrayToClassify = new ArrayList();
    				boolean publish = false;
    				begin = 0;
    				end = 0;
    				end = listeTopics.indexOf(',', begin);
    				String idTopic;
    				while(end != -1) {
    					idTopic = listeTopics.substring(begin, end);

    					begin = end + 1;
    					end = listeTopics.indexOf(',', begin);
    					
    					// ajout de la publication dans le theme
    					scc.addPublicationToFolder(pubId, idTopic);

    					publish = true;
    				}

    				if (publish) {
    				  arrayToClassify.add(id);
    				  scc.publish(arrayToClassify); //set etat du site a 1
    			   }
            	} else if (action != null && action.equals("deleteWebSites")) {

            		ArrayList listToDelete = new ArrayList();

            		String liste = (String) request.getParameter("SiteList");

					int begin = 0;
					int end = 0;
					end = liste.indexOf(',', begin);
					String idToDelete;
					SiteDetail info;
					int type;
					  	                
					while(end != -1) {
						idToDelete = liste.substring(begin, end);
						listToDelete.add(idToDelete);

						//recup info sur ce webSite
						info = scc.getWebSite(idToDelete);
						type = info.getType(); /* type = 0 : site cree, type = 1 : site bookmark, type = 2 : site upload */

						if (type != 1) { //type != bookmark
							//delete directory
							scc.deleteDirectory(scc.getSettings().getString("uploadsPath")+scc.getSettings().getString("Context")+File.separator+scc.getComponentId()+File.separator+idToDelete);
						}

						//delete publication
						String pubId = scc.getIdPublication(idToDelete);
						scc.deletePublication(pubId);

						begin = end + 1;
						end = liste.indexOf(',', begin);
					}

					/* delete en BD */
					scc.deleteWebSites(listToDelete);
            	} 
            	else if (action != null && action.equals("updateDescription")) {

            		String id			= (String) request.getParameter("Id"); // cas de l'update
            		String nomSite		= (String) request.getParameter("nomSite");
    				String description	= (String) request.getParameter("description");
    				String nomPage		= (String) request.getParameter("nomPage");
    				String tempPopup	= (String) request.getParameter("popup");
    				String letat 		= (String) request.getParameter("etat");
    				String listeIcones	= (String) request.getParameter("ListeIcones");
    				String listeTopics	= (String) request.getParameter("ListeTopics"); 
    				
    				int popup = 0;
    				if ((tempPopup != null) && (tempPopup.length() > 0))
    					popup = 1;
    				
    				int etat = -1;
    				if (letat != null) {
    				  if (!letat.equals(""))
    				    etat = new Integer(letat).intValue();
    				}
    				
    				ArrayList listIcons = new ArrayList();
    				int begin = 0;
    				int end = 0;
    				if (listeIcones != null) {
    				  end = listeIcones.indexOf(',', begin);
    				  while(end != -1) {
    				      listIcons.add(listeIcones.substring(begin, end));
    				      begin = end + 1;
    				      end = listeIcones.indexOf(',', begin);
    				  }
    				}
    				
    				SiteDetail ancien = scc.getWebSite(id);
            		int type = ancien.getType();

            		/* update description en BD */
            		SiteDetail descriptionSite2 = new SiteDetail(id, nomSite, description, nomPage, type, null, null, etat, popup);

            		scc.updateWebSite(descriptionSite2);

            		if (listIcons.size() > 0) {
            			scc.associateIcons(id, listIcons);
            		}

            	    /* publications : classer le site dans les themes cochés*/
            	    ArrayList arrayToClassify = new ArrayList();
            	    boolean publish = false;
            		ArrayList arrayTopic = new ArrayList();
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
            	}
            	
            	Collection listeSites = scc.getAllWebSite();
            	request.setAttribute("ListSites", listeSites);
            	request.setAttribute("BookmarkMode", new Boolean(scc.isBookmarkMode()));
            	
            	destination = "/webSites/jsp/manage.jsp";
			}
            
			else if(function.startsWith("design.jsp")) {
				String action = (String) request.getParameter("Action"); /* = "newSite" la premiere fois, jamais null */
				String id = (String) request.getParameter("Id"); //jamais null sauf en creation ou en update de description
		    	String currentPath = (String) request.getParameter("path"); /* = null la premiere fois, rempli grace au newSite */
		    	if (currentPath != null) {
		    		currentPath = doubleAntiSlash(currentPath);
		    	}
		    	
			    ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.webSites.settings.webSiteSettings","fr");
			    if (action.equals("newSite")) {
			    	//ADD NEW SITE  -------------------------------------------------------------
			    	String nomSite = (String) request.getParameter("nomSite"); /* = rempli au premier acces a designSite pui toujours null */
			    	String description = (String) request.getParameter("description"); /* = rempli la premiere fois a la creation, puis toujours null*/
			    	String nomPage = (String) request.getParameter("nomPage"); /* = rempli la premiere fois a la creation, puis toujours null*/
			    	String tempPopup = (String) request.getParameter("popup");
			    	String listeIcones = (String) request.getParameter("ListeIcones"); /* = rempli la premiere fois a la creation, puis toujours null*/
			    	String listeTopics = (String) request.getParameter("ListeTopics"); /* = en cas de new Site ou de classifySite */
			    	
			    	int popup = 0;
			    	if ((tempPopup != null) && (tempPopup.length() > 0)) {
			    		popup = 1;
			    	}
			    	
		    	    ArrayList listIcons = new ArrayList();
		    	    int begin = 0;
		    	    int end = 0;
		    	    if (listeIcones != null) {
		    	      end = listeIcones.indexOf(',', begin);
		    	      while(end != -1) {
		    	          listIcons.add(listeIcones.substring(begin, end));
		    	          begin = end + 1;
		    	          end = listeIcones.indexOf(',', begin);
		    	      }
		    	    }
			    	
					/* recuperation de l'id */
					id = scc.getNextId();
					
					/* Creer le repertoire id */
					scc.createFolder(settings.getString("uploadsPath")+settings.getString("Context")+File.separator+scc.getComponentId()+File.separator+id);

					/* creation en BD */
					SiteDetail descriptionSite = new SiteDetail(id, nomSite, description, nomPage, 0, null, null, 0, popup);  /* type 0 = site cree */

					String pubId = scc.createWebSite(descriptionSite);
					descriptionSite = scc.getWebSite(id);
					scc.setSessionSite(descriptionSite);

					if (listIcons.size() > 0) {
						scc.associateIcons(id, listIcons);
					}

					currentPath = settings.getString("uploadsPath")+settings.getString("Context")+File.separator+scc.getComponentId()+File.separator+id;

					currentPath = doubleAntiSlash(currentPath);

					/* ajout de la page principale */
					String code = " ";

					/* Creer la page principale */
					scc.createFile(currentPath, nomPage, code);

					/* publications : classer le site dans les themes cochés */
					ArrayList arrayToClassify = new ArrayList();
					boolean publish = false;
					begin = 0;
					end = 0;
					end = listeTopics.indexOf(',', begin);
					String idTopic;
					while(end != -1) {
						idTopic = listeTopics.substring(begin, end);

						begin = end + 1;
						end = listeTopics.indexOf(',', begin);

						// ajout de la publication dans le theme
						scc.addPublicationToFolder(pubId, idTopic);

						publish = true;
					}

					if (publish) {
						arrayToClassify.add(id);
						scc.publish(arrayToClassify); //set etat du site a 1
					}
					
					request.setAttribute("Site", descriptionSite);
			    } 
			    else if (action.equals("updateDescription")) { // type 0 design ou 2 upload
			    	String nomSite = (String) request.getParameter("nomSite"); /* = rempli au premier acces a designSite pui toujours null */
			    	String description = (String) request.getParameter("description"); /* = rempli la premiere fois a la creation, puis toujours null*/
			    	String nomPage = (String) request.getParameter("nomPage"); /* = rempli la premiere fois a la creation, puis toujours null*/
			    	String tempPopup = (String) request.getParameter("popup");
			    	String etat = (String) request.getParameter("etat");
			    	String listeIcones = (String) request.getParameter("ListeIcones"); /* = rempli la premiere fois a la creation, puis toujours null*/
			    	
			    	int popup = 0;
			    	if ((tempPopup != null) && (tempPopup.length() > 0)) {
			    		popup = 1;
			    	}
			    	
			    	ArrayList listIcons = new ArrayList();
		    	    int begin = 0;
		    	    int end = 0;
		    	    if (listeIcones != null) {
		    	      end = listeIcones.indexOf(',', begin);
		    	      while(end != -1) {
		    	          listIcons.add(listeIcones.substring(begin, end));
		    	          begin = end + 1;
		    	          end = listeIcones.indexOf(',', begin);
		    	      }
		    	    }
			    	 
					SiteDetail ancien = scc.getSessionSite();
					id = ancien.getSitePK().getId();
					int type = ancien.getType();

					/* verif que le nom de la page principale est correcte */
					Collection collPages = scc.getAllWebPages2(currentPath);
					Iterator j = collPages.iterator();
					boolean ok = false;
					File f;
					while (j.hasNext()) {
						f = (File) j.next();
						if (f.getName().equals(nomPage)) {
							ok = true;
							break;
						}
					}

					boolean searchOk = ok;

					SiteDetail descriptionSite2 = new SiteDetail(id, nomSite, description, nomPage, type, null, null, new Integer(etat).intValue(), popup);

					if (searchOk) {

						/* update description en BD */
						
						scc.updateWebSite(descriptionSite2);

						if (listIcons.size() > 0) {
							scc.associateIcons(id, listIcons);
						}
					} else {
						request.setAttribute("SearchOK", Boolean.FALSE);
						request.setAttribute("ListeIcones", listeIcones);
					}
					
					descriptionSite2 = scc.getWebSite(id);
					scc.setSessionSite(descriptionSite2);
					request.setAttribute("Site", descriptionSite2);
				}
			    else if (action.equals("addFolder")) {
			    	String name = (String) request.getParameter("name"); /* = null la premiere fois, puis = nom du repertoire courant */
			    	
			        //ADD FOLDER -------------------------------------------------------------
			        /* Creer le nouveau repertoire */
			        scc.createFolder(currentPath+File.separator+name);

			        request.setAttribute("Site", scc.getSessionSite());
			        
			    } 
			    else if (action.equals("renameFolder")) {
			    	String name = (String) request.getParameter("name"); /* = null la premiere fois, puis = nom du repertoire courant */
			    	String newName = (String) request.getParameter("newName"); /* = changement de noms des fichiers et repertoires */
			    	
			    	//RENAME FOLDER -------------------------------------------------------------
			        
			        /* Modifier le nom du repertoire */
			        scc.renameFolder(currentPath+File.separator+name, currentPath+File.separator+newName);

			        request.setAttribute("Site", scc.getSessionSite());

			    }
			    else if (action.equals("deleteFolder")) {
			    	String name = (String) request.getParameter("name"); /* = null la premiere fois, puis = nom du repertoire courant */
			    	
			        //DELETE FOLDER -------------------------------------------------------------
			        
			        /* Supprimer le repertoire */
			        scc.delFolder(currentPath+File.separator+name);

			        request.setAttribute("Site", scc.getSessionSite());
			    }
			    else if (action.equals("addPage")) {
			    	String nomPage = (String) request.getParameter("nomPage"); /* = rempli la premiere fois a la creation, puis toujours null*/
			    	
			    	//ADD PAGE -------------------------------------------------------------
					String code = (String) request.getParameter("Code"); // = code de la page a parser
					
					code = EncodeHelper.htmlStringToJavaString(code);
					
					String newCode = parseCodeSupprImage(scc, code, request, settings, currentPath); /* enleve les http://localhost:8000/WAwebSiteUploads/WA0webSite17/18/  et on garde seulement rep/icon.gif */
					
					newCode = parseCodeSupprHref(scc, newCode, settings, currentPath); /* enleve les http://localhost:8000/webactiv/RwebSite/jsp/  et on garde seulement rep/page.html */
					
					// Creer une nouvelle page
					scc.createFile(currentPath, nomPage, newCode);

					request.setAttribute("Site", scc.getSessionSite());
			    }
			    else if (action.equals("renamePage")) {
			        //RENAME PAGE -------------------------------------------------------------
			        
			    	String name = (String) request.getParameter("name"); /* = null la premiere fois, puis = nom du repertoire courant */
			    	String newName = (String) request.getParameter("newName"); /* = changement de noms des fichiers et repertoires */
			    	
			        /* Modifier le nom du fichier */
			        scc.renameFile(currentPath, name, newName);

			        request.setAttribute("Site", scc.getSessionSite());
			    }

			    else if (action.equals("deletePage")) {
			        //DELETE PAGE -------------------------------------------------------------
			    	String name = (String) request.getParameter("name"); /* = null la premiere fois, puis = nom du repertoire courant */
			    	
			        /* Supprimer la page */
			        scc.deleteFile(currentPath+File.separator+name);

			        request.setAttribute("Site", scc.getSessionSite());
			    }

			    else if (action.equals("classifySite")) { // cas de l'upload et du design
			        //CLASSIFY SITE -------------------------------------------------------------
			        
			    	String listeTopics = (String) request.getParameter("ListeTopics"); /* = en cas de new Site ou de classifySite */
			    	
			    	request.setAttribute("Site", scc.getSessionSite());

			        /* publications : classer le site dans les themes cochés*/
			        ArrayList arrayToClassify = new ArrayList();
			        boolean publish = false;

					ArrayList arrayTopic = new ArrayList();
					int begin = 0;
					int end = 0;
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
			    }

			    else if (action.equals("design")) {
			        //DESIGN -------------------------------------------------------------
			        SiteDetail site = scc.getWebSite(id);
			        scc.setSessionSite(site);
			        request.setAttribute("Site", site);
			    }
			    
			    else {
			        //AUTRE  -------------------------------------------------------------
		            // view en cas de rechargement de la page pour naviguer dans le chemin
		            //ou createSite annule
		            // ou upload d'image
			    	 
			    	SiteDetail site = scc.getSessionSite();
			    	id = site.getPK().getId();
			    	request.setAttribute("Site", site);
		            
			    }
			    
			    destination = "/webSites/jsp/design.jsp?Action=design&path="+currentPath+"&Id="+id;
			}
            
			else if(function.equals("EffectiveUploadFile")) {
				List<FileItem> items = FileUploadUtil.parseRequest(request);
				
				String thePath = FileUploadUtil.getParameter(items, "path");
				FileItem item = FileUploadUtil.getFile(items);
				if (item != null)
				{
					String fileName = FileUploadUtil.getFileName(item);
					File file = new File(thePath,fileName);
					item.write(file);
				}
				
                request.setAttribute("UploadOk", Boolean.TRUE);
				
                destination = "/webSites/jsp/uploadFile.jsp?path="+thePath;
			}
            
			else if(function.startsWith("descUpload.jsp")) {
				request.setAttribute("AllIcons", scc.getAllIcons());
				destination = "/webSites/jsp/descUpload.jsp";
			}
			else if(function.equals("EffectiveUploadSiteZip")) {
				List<FileItem> items = FileUploadUtil.parseRequest(request);
				
				String nomSite = FileUploadUtil.getParameter(items, "nomSite");
				String description = FileUploadUtil.getParameter(items, "description");
				String popupString = FileUploadUtil.getParameter(items, "popup");
				int popup = 0;
				if("on".equals(popupString))
					popup = 1;
				String nomPage = FileUploadUtil.getParameter(items, "nomPage");
				String listeIcones = FileUploadUtil.getParameter(items, "ListeIcones");
				String listeTopics = FileUploadUtil.getParameter(items, "ListeTopics");
				
				FileItem fileItem = FileUploadUtil.getFile(items);
				if (fileItem != null)
				{
					/* recuperation de l'id = nom du directory */
				    String id = scc.getNextId();
				    
				    SiteDetail descriptionSite = new SiteDetail(id, nomSite, description, nomPage, 2, null, null, 0, popup); /* type 2 = site uploade */

					/* Création du directory */
				    String cheminZip = scc.getSettings().getString("uploadsPath")+scc.getSettings().getString("Context")+File.separator+scc.getComponentId()+File.separator+id;				
					File directory = new File(cheminZip);
					if (directory.mkdir()) {
						/* creation du zip sur le serveur */
						String fichierZipName = FileUploadUtil.getFileName(fileItem);
						File fichier = new File(cheminZip+File.separator+fichierZipName);
						
						fileItem.write(fichier);
						
						/* dezip du fichier.zip sur le serveur */
						String cheminFichierZip = cheminZip+File.separator+fichierZipName;
						scc.unzip(cheminZip, cheminFichierZip);

						/* verif que le nom de la page principale est correcte */
						Collection collPages = scc.getAllWebPages2(cheminZip);
						SilverTrace.debug("webSites", "RequestRouter.EffectiveUploadSiteZip", "root.MSG_GEN_PARAM_VALUE", collPages.size()+" files in zip");
						SilverTrace.debug("webSites", "RequestRouter.EffectiveUploadSiteZip", "root.MSG_GEN_PARAM_VALUE", "nomPage = "+nomPage);
						
						Iterator j = collPages.iterator();
						
						boolean searchOk = false;
						File f;
						while (j.hasNext()) {
							f = (File) j.next();
							SilverTrace.debug("webSites", "RequestRouter.EffectiveUploadSiteZip", "root.MSG_GEN_PARAM_VALUE", "f.getName() = "+f.getName());
							if (f.getName().equals(nomPage)) {
								searchOk = true;
								break;
							}
						}

						if (searchOk) 
						{
							/* creation en BD */
							ArrayList listIcons = new ArrayList();
							int begin = 0;
							int end = 0;
							if (listeIcones != null) {
								end = listeIcones.indexOf(',', begin);
								while(end != -1) {
									listIcons.add(listeIcones.substring(begin, end));
									begin = end + 1;
									end = listeIcones.indexOf(',', begin);
								}
							}

							String pubId = scc.createWebSite(descriptionSite);
							
							if (listIcons.size() > 0) {
								scc.associateIcons(id, listIcons);
							}

							/* publications : classer le site dans les themes cochés */
							String idTopic;
							ArrayList arrayToClassify = new ArrayList();
							boolean publish = false;
							begin = 0;
							end = 0;
							end = listeTopics.indexOf(',', begin);
							while(end != -1) {
								idTopic = listeTopics.substring(begin, end);

								begin = end + 1;
								end = listeTopics.indexOf(',', begin);

								scc.addPublicationToFolder(pubId, idTopic);

								publish = true;
							}

							if (publish) {
								arrayToClassify.add(id);
								scc.publish(arrayToClassify); //set etat du site a 1
							}
							
							Collection listeSites = scc.getAllWebSite();
			            	request.setAttribute("ListSites", listeSites);
			            	request.setAttribute("BookmarkMode", new Boolean(scc.isBookmarkMode()));
			            	
							destination = "/webSites/jsp/manage.jsp";
							
						}
						else { //le nom de la page principale n'est pas bonne, on supprime ce qu'on a dezipe
							scc.deleteDirectory(cheminZip);
							
							request.setAttribute("Site", descriptionSite);
							request.setAttribute("AllIcons", scc.getAllIcons());
							request.setAttribute("ListeIcones", listeIcones);
							request.setAttribute("UploadOk", Boolean.TRUE);
							request.setAttribute("SearchOk", Boolean.FALSE);
							destination = "/webSites/jsp/descUpload.jsp";
						}
						
					}//if directory.mkdir
					else {
						request.setAttribute("Site", descriptionSite);
						request.setAttribute("AllIcons", scc.getAllIcons());
						request.setAttribute("ListeIcones", listeIcones);
						request.setAttribute("UploadOk", Boolean.FALSE);
						destination = "/webSites/jsp/descUpload.jsp";
					}
				}
			}           
			else 
            {
                destination = "/webSites/jsp/" + function;
            }

        }
        catch (Exception e)
        {
            request.setAttribute("javax.servlet.jsp.jspException", e);
            destination = "/admin/jsp/errorpageMain.jsp";
        }

        // Open the destination page
        SilverTrace.info("webSites", "WebSitesRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "openPage = " + function);

        return destination;
    }

	private String getWebSitesDestination(SiteDetail sitedetail, HttpServletRequest request, WebSiteSessionController scc)
	{
		String siteId	= sitedetail.getSitePK().getId();
		
		//CBO : UPDATE
		//String nomPage	= sitedetail.getPage();
		String nomPage	= sitedetail.getContent();
		
		int    type		= sitedetail.getType();

		if (type == 1)
		{  
			// type bookmark
			if (nomPage.indexOf("://")==-1)
			{
				//no protocol is mentionned
				//by default = "http"
				nomPage = "http://" + nomPage;
			}
		}
		else
		{  	// upload, design
			ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.webSites.settings.webSiteSettings", "fr");
			
			//CBO : UPDATE
			//nomPage = URLManager.getHttpMode() + getMachine(request) + File.separator + settings.getString("Context") + File.separator + scc.getComponentId() + File.separator + siteId + File.separator + nomPage;
			nomPage = "http://" + getMachine(request) + File.separator + settings.getString("Context") + File.separator + scc.getComponentId() + File.separator + siteId + File.separator + nomPage;
		}
		return "/webSites/jsp/ouvertureSite.jsp?URL=" + EncodeHelper.javaStringToJsString(nomPage);
	}

    /* construitTab */

    /**
     * Method declaration
     *
     *
     * @param deb
     *
     * @return
     *
     * @see
     */
    private ArrayList construitTab(String deb)
    {
        /* deb = id/rep/  ou id\rep/ */
        /* res = [id | rep] */
        int       i = 0;
        String    noeud = "";
        ArrayList array = new ArrayList();


        while (i < deb.length())
        {
            char car = deb.charAt(i);

            if (car == '/' || car == '\\')
            {
                array.add(noeud);
                noeud = "";
            }
            else
            {
                noeud += car;
            }
            i++;
        }
        return array;
    }

    /* getMachine */

    /**
     * Method declaration
     *
     *
     * @param request
     *
     * @return
     *
     * @see
     */
    private String getMachine(HttpServletRequest request)
    {
        ResourceLocator settings = new ResourceLocator("com.stratelia.webactiv.webSites.settings.webSiteSettings", "fr");
        ResourceLocator generalSettings = new ResourceLocator("com.stratelia.webactiv.general", "fr");

        String          machine = settings.getString("Machine"); //ex : http://info.aero.jussieu.fr:8080
        String          context = (generalSettings.getString("ApplicationURL")).substring(1);

        if (machine.equals(""))
        {
        	//CBO : UPDATE
            /*HttpUtils    u = new HttpUtils();
            StringBuffer url = u.getRequestURL(request);*/
        	StringBuffer url = request.getRequestURL();
            //CBO : FIN UPDATE

            ArrayList    a = construitTab(url.toString());

            int          j = 1;

            while (true)
            {
                if (j > a.size())
                {
                    break;
                }

                if (!a.get(j).equals(context))
                {
                    if (machine.equals(""))
                    {
                        machine += a.get(j);
                    }
                    else
                    {
                        machine = machine + File.separator + a.get(j);
                    }
                }
                else
                {
                    break;
                }
                j++;
            }
        }
        return machine;
    }

    /* getFlag */

    /**
     * Method declaration
     *
     *
     * @param profiles
     *
     * @return
     *
     * @see
     */
    private String getFlag(String[] profiles)
    {
        String flag = "Reader";

        for (int i = 0; i < profiles.length; i++)
        {
            // if admin, return it, we won't find a better profile
            if (profiles[i].equals("Admin"))
            	return profiles[i];
			if (profiles[i].equals("Publisher"))
				flag = profiles[i];
        }
        return flag;
    }
    
    //CBO : ADD
    /* doubleAntiSlash */
    private String doubleAntiSlash(String chemin) {
          int i = 0;
          String res = chemin;
          boolean ok = true;

          while (ok) {
            int j = i + 1;
            if ((i < res.length()) && (j < res.length())) {
                char car1 = res.charAt(i);
                char car2 = res.charAt(j);

                if ( (car1 == '\\' && car2 == '\\') ||
                     (car1 != '\\' && car2 != '\\') ) {
                }
                else {
                        String avant = res.substring(0, j);
                        String apres = res.substring(j);
                        if ( (apres.startsWith("\\\\")) ||
                             (avant.endsWith("\\\\")) ) {
                        }
                        else {
                            res = avant + '\\' + apres;
                            i++;
                        }
                }
            }
            else {
                if (i < res.length()) {
                    char car = res.charAt(i);
                    if (car == '\\')
                        res = res + '\\';
                }
                ok = false;
            }
            i = i + 2;
          }
          return res;
    }
    
    /* ignoreAntiSlash */
    public String ignoreAntiSlash(String chemin) {
      /* ex : \\\rep1\\rep2\\rep3 */
      /* res = rep1\\rep2\\re3 */

        String res = chemin;
        boolean ok = false;
        while (!ok) {
            char car = res.charAt(0);
            if (car == '\\') {
                res = res.substring(1);
            }
            else ok = true;
        }
        return res;

    }

    /* supprDoubleAntiSlash */
    public String supprDoubleAntiSlash(String chemin) {
      /* ex : id\\rep1\\rep11\\rep111 */
      /* res = id\rep1\rep11\re111 */

        String res = "";
        int i = 0;

        while (i < chemin.length()) {
            char car = chemin.charAt(i);
            if (car == '\\') {
                res = res + car;
                i++;
            }
            else res = res + car;
            i++;
        }
        return res;
    }

    /* finNode */
    public String finNode(WebSiteSessionController scc, String path) {
        /* ex : ....webSite17\\id\\rep1\\rep2\\rep3 */
        /* res : id\rep1\rep2\rep3 */

        int longueur = scc.getComponentId().length();
        int index = path.lastIndexOf(scc.getComponentId());
        String chemin = path.substring(index + longueur);

        chemin = ignoreAntiSlash(chemin);
        chemin = supprDoubleAntiSlash(chemin);

        return chemin;
    }

    /* sortCommun */
    private ArrayList sortCommun(ArrayList tabContexte, ArrayList tab) {
      /* tabContexte = [id | rep1 | rep2] */
      /* tab = [id | rep1 | rep3] */
      /* res = [id | rep1] */
        int i = 0;
        boolean ok = true;
        ArrayList array = new ArrayList();



        while (ok && i < tabContexte.size()) {
            String contenuContexte = (String) tabContexte.get(i);
            if (i < tab.size()) {
              String contenu = (String) tab.get(i);
              if (contenuContexte.equals(contenu)) {
                array.add(contenu);

              }
              else ok = false;
              i++;
            }
            else ok = false;
         }
         return array;
    }
    
    /* sortRester */
    private String sortReste(ArrayList tab, ArrayList tabCommun) {
      /* tab = [id | rep1 | rep2 | rep3] */
      /* tabCommun = [id | rep1] */
      /* res = rep2/rep3 */
        String res = "";



        int indice = tabCommun.size();

        while (indice < tab.size()) {
            String contenu = (String) tab.get(indice);
            res += contenu + "/";
            indice++;
         }

         if (! res.equals(""))
            res = res.substring(0, res.length() - 1);

         return res;
    }

    
    /* parseCodeSupprImage */
    private String parseCodeSupprImage(WebSiteSessionController scc, String code, HttpServletRequest request, ResourceLocator settings, String currentPath) {
       String theCode = code;
       String avant;
       String apres;
       int index;
       String finChemin;
   	String image = "<IMG border=0 src=\"http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+scc.getComponentId()+"/";
   	int longueurImage = 19 + ("http://"+getMachine(request)+"/"+settings.getString("Context")+"/"+scc.getComponentId()+"/").length();
   	index = code.indexOf(image);
   	if (index == -1) return theCode;
   	else {
         avant = theCode.substring(0, index + 19);
         finChemin = theCode.substring(index + longueurImage);

         int indexGuillemet = finChemin.indexOf("\"");
         String absolute = finChemin.substring(0, indexGuillemet);

         apres = finChemin.substring(indexGuillemet);
         int indexSlash = absolute.lastIndexOf("/");
         String fichier = absolute.substring(indexSlash + 1);

         String deb = absolute.substring(0, indexSlash);
         ArrayList tab = construitTab(deb+"/");

         /* id/rep1 */
         String cheminContexte = finNode(scc, currentPath);
         ArrayList tabContexte = construitTab(cheminContexte+"/");
         ArrayList tabCommun = sortCommun(tabContexte, tab);
         String reste = sortReste(tab, tabCommun);
         int nbPas = tabContexte.size() - tabCommun.size();
         String relatif = "";
         int i = 0;
         while (i < nbPas) {
           relatif += "../";
           i++;
         }

         if (reste.equals(""))
           relatif += fichier;
         else relatif += reste + "/" + fichier;
         apres = relatif + apres;
         return (avant + parseCodeSupprImage(scc, apres, request, settings, currentPath));
     }
    }

    /* parseCodeSupprHref */
    /* ex : code = ...<a href="rr:icones/fleche.html"> <a href="http://www.etc"> <a href="aa:REP1/page.html">: liens deja en relatif (rr:) ou url externe (http://) ou liens en abslu
            res = ...<a href="icones/fleche.html"> <a href="http://www.etc"> <a href="page.html"> : tous les liens en relatifs ou urlk externe */
    private String parseCodeSupprHref(WebSiteSessionController scc, String code, ResourceLocator settings, String currentPath) {
       String theCode = code;
       String avant;
       String apres;
       int index;
       String href = "<A href=\""; /* longueur de chaine = 9 */
       String finChemin;
       String fichier;
       String deb;
       String theReturn = "";


     index = theCode.indexOf(href);
     if (index == -1) theReturn = theCode;

     else {

           avant = theCode.substring(0, index + 9);


           apres = theCode.substring(index + 9);


           if (apres.substring(0, 7).equals("http://")) { /* lien externe */
                 theReturn = avant + parseCodeSupprHref(scc, apres, settings, currentPath);
           }
           else if (apres.substring(0, 6).equals("ftp://")) { /* lien externe */
                 theReturn = avant + parseCodeSupprHref(scc, apres, settings, currentPath);
           }
           else if (apres.substring(0, 3).equals("rr:")) { /* deja en relatif */

                 apres = apres.substring(3);

                 theReturn = avant + parseCodeSupprHref(scc, apres, settings, currentPath);
           }
           else if (apres.substring(0, 3).equals("aa:")) { /* lien absolu a transformer en relatif */

               /* finChemin = rep/coucou.html">... */
               finChemin = theCode.substring(index + 9 + 3);
               SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "finChemin = "+finChemin);

               /* traitement */
               int indexGuillemet = finChemin.indexOf("\"");
   	    SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "indexGuillemet = "+new Integer(indexGuillemet).toString());

               /* absolute = rep/coucou.html */
              String absolute = finChemin.substring(0, indexGuillemet);
              SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "absolute = "+absolute);

               /* apres = ">... */
               apres = finChemin.substring(indexGuillemet);
               SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "apres = "+apres);

               int indexSlash = absolute.lastIndexOf("\\");
   	    SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "indexSlash = "+new Integer(indexSlash).toString());

               if (indexSlash == -1) { /* pas d'arborescence, le fichier du lien est sur la racine */
                   fichier = absolute;
                   deb = "";
               }
               else {
                 /* fichier = coucou.html */
                 fichier = absolute.substring(indexSlash + 1);
                 deb = absolute.substring(0, indexSlash);
               }
               ArrayList tab = construitTab(deb+"/"); /* dans ce tableau il manque l'id */

             /* cheminContexte = id/rep */
   	      int longueur = scc.getComponentId().length();
   	      int index2 = currentPath.lastIndexOf(scc.getComponentId());
   	      String chemin = currentPath.substring(index2 + longueur);

   	      chemin = chemin.substring(1);
   	      chemin = supprDoubleAntiSlash(chemin);
   	      String cheminContexte = chemin;
               ArrayList tabContexte = construitTab(cheminContexte+"/");
               /* ajoute l'id dans le premier tableau */
               tab.add(0, tabContexte.get(0));

               /* tabCommun = [id | rep] */
               ArrayList tabCommun = sortCommun(tabContexte, tab);

               /* reste = vide */
               String reste = sortReste(tab, tabCommun);

               /* nbPas = 0 */
               int nbPas = tabContexte.size() - tabCommun.size();
               String relatif = "";
               int i = 0;
               while (i < nbPas) {
                 relatif += "../";
                 i++;
               }

               if (reste.equals(""))
                 relatif += fichier;
               else relatif += reste + "/" + fichier;

   	        /* relatif = vide */
               apres = relatif + apres;
               theReturn = avant + parseCodeSupprHref(scc, apres, settings, currentPath);
            }
         }
         return theReturn;
   }

}
