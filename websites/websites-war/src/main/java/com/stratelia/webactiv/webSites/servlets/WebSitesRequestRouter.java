/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.webSites.servlets;

import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.webSites.control.WebSiteSessionController;
import com.stratelia.webactiv.webSites.siteManage.model.FolderDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;
import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;

/**
 * Class declaration
 *
 * @author
 */
public class WebSitesRequestRouter extends ComponentRequestRouter<WebSiteSessionController> {

  /**
   *
   */
  private static final long serialVersionUID = -536203260896933461L;

  /**
   * This method has to be implemented in the component request router class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "webSites";
  }

  /**
   * Method declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public WebSiteSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WebSiteSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request router it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param scc The component Session Control, build and initialised.
   * @param request The entering request. The request router need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, WebSiteSessionController scc,
      HttpServletRequest request) {

    SilverTrace.info("webSites", "WebSitesRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "fonction = " + function);
    String destination = "";

    // the flag is the best user's profile
    String flag = getFlag(scc.getUserRoles());
    request.setAttribute("BestRole", flag);
    try {

      if (function.startsWith("Main")) {
        FolderDetail webSitesCurrentFolder = scc.getFolder("0");
        scc.setSessionTopic(webSitesCurrentFolder);
        request.setAttribute("CurrentFolder", webSitesCurrentFolder);
        if (flag.equals("Publisher") || flag.equals("Admin")) {
          destination = "/webSites/jsp/listSite.jsp";
        } else// reader
        {
          destination = "/webSites/jsp/listSite_reader.jsp";
        }
      } else if (function.startsWith("listSite.jsp")) {
        String action = request.getParameter("Action");
        String id = request.getParameter("Id");

        if (action == null) {
          id = "0";
          action = "Search";
        }

        FolderDetail webSitesCurrentFolder = scc.getFolder(id);
        scc.setSessionTopic(webSitesCurrentFolder);
        request.setAttribute("CurrentFolder", webSitesCurrentFolder);

        destination = "/webSites/jsp/listSite.jsp?Action=" + action + "&Id=" + id;
      } else if (function.startsWith("listSite_reader.jsp")) {
        String action = request.getParameter("Action");
        String id = request.getParameter("Id");

        if (action == null) {
          id = "0";
          action = "Search";
        }

        FolderDetail webSitesCurrentFolder = scc.getFolder(id);
        scc.setSessionTopic(webSitesCurrentFolder);
        request.setAttribute("CurrentFolder", webSitesCurrentFolder);

        destination = "/webSites/jsp/listSite_reader.jsp?Action=" + action + "&Id=" + id;
      } else if (function.startsWith("portlet")) {
        FolderDetail webSitesCurrentFolder = scc.getFolder("0");
        scc.setSessionTopic(webSitesCurrentFolder);
        request.setAttribute("CurrentFolder", webSitesCurrentFolder);

        if (flag.equals("Publisher") || flag.equals("Admin")) {
          destination = "/webSites/jsp/listSitePortlet.jsp";
        } else // reader
        {
          destination = "/webSites/jsp/listSite_readerPortlet.jsp";
        }
      } else if (function.startsWith("searchResult")) {
        String id = request.getParameter("Id"); /* id de la publication */
        String typeRequest = request.getParameter("Type");

        if ("Publication".equals(typeRequest) || "Site".equals(typeRequest)) {
          // recherche de l'url complete d'acces a la page
          try {
            SiteDetail sitedetail;
            if (typeRequest.equals("Site")) {
              sitedetail = scc.getWebSite(id);
            } else {
              PublicationDetail pubDetail = scc.getPublicationDetail(id);
              sitedetail = scc.getWebSite(pubDetail.getVersion());
            }

            destination = getWebSitesDestination(sitedetail, request, scc);
          } catch (Exception e) {
            SilverTrace.warn("webSites", "WebSitesRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", null, e);
          }
        } else if ("Node".equals(typeRequest)) {
          destination = scc.getComponentUrl() + "listSite.jsp?Action=Search&Id=" + id;
        }
      } else if (function.equals("SuggestLink")) {
        String nomSite = request.getParameter("nomSite");
        String description = request.getParameter("description");
        String nomPage = request.getParameter("nomPage");
        String auteur = request.getParameter("auteur");
        String date = request.getParameter("date");
        String listeIcones = request.getParameter("ListeIcones");

        int begin = 0;
        int end = listeIcones.indexOf(',', begin);
        String listeMessage = "";

        // parcours des icones
        while (end != -1) {
          String nom = listeIcones.substring(begin, end);
          listeMessage += "- " + nom + "\n";
          begin = end + 1;
          end = listeIcones.indexOf(',', begin);
        }

        scc.notifyPublishers(auteur, nomSite, description, nomPage, listeMessage, date);

        request.setAttribute("SuggestionName", nomSite);
        request.setAttribute("SuggestionUrl", nomPage);
        destination = getDestination("Main", scc, request);
      } else if (function.equals("DisplaySite")) {
        String sitePage = request.getParameter("SitePage");
        destination = sitePage;
      } else if (function.startsWith("ToWysiwyg")) {
        String path = request.getParameter("path");
        scc.checkPath(path);
        String name = request.getParameter("name");
        String nameSite = request.getParameter("nameSite");
        String id = request.getParameter("id");

        destination =
            "http://" + getMachine(request) + URLManager.getApplicationURL()
            + "/wysiwyg/jsp/htmlEditor.jsp?";
        destination += "SpaceId=" + scc.getSpaceId();

        destination += "&SpaceName=" + URLEncoder.encode(scc.getSpaceLabel(), "UTF-8");
        destination += "&ComponentId=" + scc.getComponentId();
        destination += "&ComponentName=" + URLEncoder.encode(scc.getComponentLabel(), "UTF-8");
        destination += "&BrowseInfo=" + URLEncoder.encode(nameSite, "UTF-8");
        destination += "&Language=fr";
        destination += "&ObjectId=" + id;
        destination += "&FileName=" + URLEncoder.encode(name, "UTF-8");
        destination += "&Path=" + URLEncoder.encode(path, "UTF-8");
        destination +=
            "&ReturnUrl=" + URLEncoder.encode(URLManager.getApplicationURL() + URLManager.getURL(
            scc.getSpaceId(), scc.getComponentId()) + "FromWysiwyg?path=" + path + "&name=" + name
            + "&nameSite=" + nameSite + "&profile=" + flag + "&id=" + id, "UTF-8");
        SilverTrace.info("webSites", "WebSitesRequestRouter.getDestination().ToWysiwyg",
            "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);
      } else if (function.startsWith("FromWysiwyg")) {
        String path = request.getParameter("path");
        scc.checkPath(path);
        String id = request.getParameter("id");

        SiteDetail site = scc.getWebSite(id);
        request.setAttribute("Site", site);

        destination = "/webSites/jsp/design.jsp?Action=design&path=" + path + "&Id=" + id;
        SilverTrace.info("webSites", "WebSitesRequestRouter.getDestination().FromWysiwyg",
            "root.MSG_GEN_PARAM_VALUE", "destination = " + destination);
      } else if (function.equals("TopicUp")) {
        String topicId = request.getParameter("Id");

        scc.changeTopicsOrder("up", topicId);

        String id = scc.getSessionTopic().getNodePK().getId();
        FolderDetail webSitesCurrentFolder = scc.getFolder(id);
        scc.setSessionTopic(webSitesCurrentFolder);
        request.setAttribute("CurrentFolder", webSitesCurrentFolder);

        destination = "/webSites/jsp/organize.jsp?Action=Search&Id=" + id;
      } else if (function.equals("TopicDown")) {
        String topicId = request.getParameter("Id");

        scc.changeTopicsOrder("down", topicId);

        String id = scc.getSessionTopic().getNodePK().getId();
        FolderDetail webSitesCurrentFolder = scc.getFolder(id);
        scc.setSessionTopic(webSitesCurrentFolder);
        request.setAttribute("CurrentFolder", webSitesCurrentFolder);

        destination = "/webSites/jsp/organize.jsp?Action=Search&Id=" + id;
      } else if (function.equals("PubUp")) {
        String pubId = request.getParameter("Id");

        scc.changePubsOrder(pubId, -1);

        String id = scc.getSessionTopic().getNodePK().getId();
        FolderDetail webSitesCurrentFolder = scc.getFolder(id);
        scc.setSessionTopic(webSitesCurrentFolder);
        request.setAttribute("CurrentFolder", webSitesCurrentFolder);

        destination = "/webSites/jsp/organize.jsp?Action=Search&Id=" + id;
      } else if (function.equals("PubDown")) {
        String pubId = request.getParameter("Id");

        scc.changePubsOrder(pubId, 1);

        String id = scc.getSessionTopic().getNodePK().getId();
        FolderDetail webSitesCurrentFolder = scc.getFolder(id);
        scc.setSessionTopic(webSitesCurrentFolder);
        request.setAttribute("CurrentFolder", webSitesCurrentFolder);

        destination = "/webSites/jsp/organize.jsp?Action=Search&Id=" + id;
      } else if (function.startsWith("modifDesc.jsp")) {
        String id = request.getParameter("Id");
        // = null ou rempli si type= design
        String currentPath = request.getParameter("path");
        String type = request.getParameter("type"); // null ou design

        request.setAttribute("Site", scc.getWebSite(id));
        request.setAttribute("AllIcons", scc.getAllIcons());
        request.setAttribute("ListIcons", scc.getIcons(id));

        String recupParam = request.getParameter("RecupParam"); // =null ou oui
        String complete = null;
        if (recupParam != null) {// =oui
          String nom = request.getParameter("Nom");
          String description = request.getParameter("Description");
          String lapage = request.getParameter("Page");
          String listeIcones = request.getParameter("ListeIcones");

          type = "design";
          complete =
              "&RecupParam=oui&Nom=" + nom + "&Description=" + description + "&Page=" + lapage
              + "&ListeIcones=" + listeIcones;
        } else {
          destination =
              "/webSites/jsp/modifDesc.jsp?Id=" + id + "&path=" + currentPath + "&type=" + type;
        }

        destination = "/webSites/jsp/modifDesc.jsp?Id=" + id;
        if (complete != null) {
          destination += complete;
        }
        if (currentPath != null) {
          destination += "&path=" + currentPath;
        }
        if (type != null) {
          destination += "&type=" + type;
        }
      } else if (function.equals("Suggest")) {
        request.setAttribute("AllIcons", scc.getAllIcons());
        request.setAttribute("Action", "suggest");

        destination = "/webSites/jsp/descBookmark.jsp";
      } else if (function.startsWith("descBookmark.jsp")) {
        request.setAttribute("AllIcons", scc.getAllIcons());

        destination = "/webSites/jsp/descBookmark.jsp";
      } else if (function.startsWith("descDesign.jsp")) {
        request.setAttribute("AllIcons", scc.getAllIcons());

        destination = "/webSites/jsp/descDesign.jsp";

      } else if (function.startsWith("organize.jsp")) {

        String action = request.getParameter("Action");
        String id = request.getParameter("Id");
        String path = request.getParameter("Path");

        if (action == null) {
          action = "Search";
        } else if (action.equals("Update")) {
          String childId = request.getParameter("ChildId");
          String name = request.getParameter("Name");
          String description = request.getParameter("Description");
          NodeDetail folder =
              new NodeDetail(childId, name, description, null, null, null, "0", "X");
          scc.updateFolderHeader(folder, "");
          action = "Search";
        } else if (action.equals("Delete")) {
          /*
           * declassification des sites et suppression des themes
           */
          /* delete folder */
          int i = 0;
          String[] listeId = request.getParameterValues("checkbox");
          if (listeId == null) {
            String Id = request.getParameter("checkbox");
            if (Id != null) {
              // delete theme et publications
              scc.deleteFolder(Id);
            }
          } else {
            String idFolderToDelete;
            while (i < listeId.length) {
              idFolderToDelete = (String) listeId[i];
              // delete theme et publications
              scc.deleteFolder(idFolderToDelete);
              i++;
            }
          }

          /* quels sont les sites a depublier */
          ArrayList<String> arrayToDePublish = new ArrayList<String>();
          Collection<SiteDetail> liste = scc.getAllWebSite();
          Iterator<SiteDetail> j = liste.iterator();
          SiteDetail site;
          while (j.hasNext()) {
            site = j.next();

            if (scc.getIdPublication(site.getSitePK().getId()) == null) {
              arrayToDePublish.add(site.getSitePK().getId());
            }
          }

          // dePublish
          if (arrayToDePublish.size() > 0) {
            scc.dePublish(arrayToDePublish);
          }

          /* declassify sites */
          String listeSite = request.getParameter("SiteList");
          arrayToDePublish = new ArrayList<String>();
          i = 0;
          int begin = 0;
          int end = 0;
          end = listeSite.indexOf(',', begin);
          String idPubToDeClassify;
          PublicationDetail pub;
          Collection<NodePK> listNodePk;
          while (end != -1) {
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
        } else if (action.equals("classify")) {
          String listeSite = request.getParameter("SiteList");
          ArrayList<String> arrayToClassify = new ArrayList<String>();
          int begin = 0;
          int end = 0;
          end = listeSite.indexOf(',', begin);

          String idSiteToClassify;
          String pubId = null;
          while (end != -1) {
            idSiteToClassify = listeSite.substring(begin, end);
            arrayToClassify.add(idSiteToClassify);

            pubId = scc.getIdPublication(idSiteToClassify);

            scc.addPublicationToFolder(pubId, id);

            begin = end + 1;
            end = listeSite.indexOf(',', begin);
          }
          if (arrayToClassify.size() > 0) {
            scc.publish(arrayToClassify); // set etat du site a 1
          }

          action = "Search";
        } else if (action.equals("declassify")) {

          String listeSite = request.getParameter("SiteList");

          ArrayList<String> arrayToDeClassify = new ArrayList<String>();
          int begin = 0;
          int end = 0;
          end = listeSite.indexOf(',', begin);
          String idSiteToDeClassify = null;
          String pubId = null;
          Collection<NodePK> listNodePk;
          while (end != -1) {
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
            scc.dePublish(arrayToDeClassify); // set etat du site a 0
          }
          action = "Search";
        }

        if (id == null) {
          id = "0";
        }

        FolderDetail webSitesCurrentFolder = scc.getFolder(id);
        scc.setSessionTopic(webSitesCurrentFolder);
        request.setAttribute("CurrentFolder", webSitesCurrentFolder);

        destination = "/webSites/jsp/organize.jsp?Action=" + action + "&Id=" + id + "&Path=" + path;
      } else if (function.equals("AddTopic")) {

        String action = request.getParameter("Action");// =Add
        String fatherId = request.getParameter("Id");
        String newTopicName = request.getParameter("Name");
        String newTopicDescription = request.getParameter("Description");

        NodeDetail folder =
            new NodeDetail("X", newTopicName, newTopicDescription, null, null, null, "0", "X");
        scc.addFolder(folder, "");

        destination = "/webSites/jsp/addTopic.jsp?Action=" + action + "&Id=" + fatherId;
      } else if (function.startsWith("updateTopic")) {

        String id = request.getParameter("ChildId");
        String path = request.getParameter("Path");

        NodeDetail folderDetail = scc.getFolderDetail(id);
        request.setAttribute("CurrentFolder", folderDetail);

        destination = "/webSites/jsp/updateTopic.jsp?ChildId=" + id + "&Path=" + path;
      } else if (function.startsWith("classifyDeclassify.jsp")) {

        String action = request.getParameter("Action");
        String id = request.getParameter("TopicId");
        String linkedPathString = request.getParameter("Path");

        Collection<SiteDetail> listeSites = scc.getAllWebSite();
        request.setAttribute("ListSites", listeSites);
        request.setAttribute("CurrentFolder", scc.getSessionTopic());

        destination =
            "/webSites/jsp/classifyDeclassify.jsp?Action=" + action + "&TopicId=" + id + "&Path="
            + linkedPathString;
      } else if (function.startsWith("manage.jsp")) {
        String action = request.getParameter("Action");

        if (action != null && action.equals("addBookmark")) {
          String nomSite = request.getParameter("nomSite");
          String description = request.getParameter("description");
          String nomPage = request.getParameter("nomPage");
          String tempPopup = request.getParameter("popup");
          String listeIcones = request.getParameter("ListeIcones");
          String listeTopics = request.getParameter("ListeTopics");
          // Retrieve positions
          String positions = request.getParameter("Positions");

          int popup = 0;
          if ((tempPopup != null) && (tempPopup.length() > 0)) {
            popup = 1;
          }

          ArrayList<String> listIcons = new ArrayList<String>();
          int begin = 0;
          int end = 0;
          if (listeIcones != null) {
            end = listeIcones.indexOf(',', begin);
            while (end != -1) {
              listIcons.add(listeIcones.substring(begin, end));
              begin = end + 1;
              end = listeIcones.indexOf(',', begin);
            }
          }

          /* recuperation de l'id */
          String id = scc.getNextId();

          /* Persist siteDetail inside database, type 1 = bookmark */
          SiteDetail descriptionSite =
              new SiteDetail(id, scc.getComponentId(), nomSite, description, nomPage, 1, null,
              null, 0, popup);
          descriptionSite.setPositions(positions);

          String pubId = scc.createWebSite(descriptionSite);

          if (listIcons.size() > 0) {
            scc.associateIcons(id, listIcons);
          }

          if (nomPage.indexOf("://") == -1) {
            nomPage = "http://" + nomPage;
          }

          ArrayList<String> arrayToClassify = new ArrayList<String>();
          boolean publish = false;
          begin = 0;
          end = 0;
          end = listeTopics.indexOf(',', begin);
          String idTopic;
          while (end != -1) {
            idTopic = listeTopics.substring(begin, end);

            begin = end + 1;
            end = listeTopics.indexOf(',', begin);

            // ajout de la publication dans le theme
            scc.addPublicationToFolder(pubId, idTopic);

            publish = true;
          }

          if (publish) {
            arrayToClassify.add(id);
            scc.publish(arrayToClassify); // set etat du site a 1
          }
        } else if (action != null && action.equals("deleteWebSites")) {

          ArrayList<String> listToDelete = new ArrayList<String>();

          String liste = request.getParameter("SiteList");

          int begin = 0;
          int end = 0;
          end = liste.indexOf(',', begin);
          String idToDelete;
          SiteDetail info;
          int type;

          while (end != -1) {
            idToDelete = liste.substring(begin, end);
            listToDelete.add(idToDelete);

            // recup info sur ce webSite
            info = scc.getWebSite(idToDelete);
            // type = 0 : site cree, type = 1 : site bookmark, type = 2 : site upload
            type = info.getType();

            if (type != 1) { // type != bookmark
              // delete directory
              scc.deleteDirectory(scc.getWebSitePathById(idToDelete));
            }

            // delete publication
            String pubId = scc.getIdPublication(idToDelete);
            scc.deletePublication(pubId);

            begin = end + 1;
            end = liste.indexOf(',', begin);
          }

          /* delete en BD */
          scc.deleteWebSites(listToDelete);
        } else if (action != null && action.equals("updateDescription")) {

          String id = request.getParameter("Id"); // cas de l'update
          String nomSite = request.getParameter("nomSite");
          String description = request.getParameter("description");
          String nomPage = request.getParameter("nomPage");
          String tempPopup = request.getParameter("popup");
          String letat = request.getParameter("etat");
          String listeIcones = request.getParameter("ListeIcones");
          String listeTopics = request.getParameter("ListeTopics");

          int popup = 0;
          if ((tempPopup != null) && (tempPopup.length() > 0)) {
            popup = 1;
          }

          int etat = -1;
          if (StringUtil.isDefined(letat)) {
            etat = Integer.parseInt(letat);
          }

          ArrayList<String> listIcons = new ArrayList<String>();
          int begin = 0;
          int end = 0;
          if (listeIcones != null) {
            end = listeIcones.indexOf(',', begin);
            while (end != -1) {
              listIcons.add(listeIcones.substring(begin, end));
              begin = end + 1;
              end = listeIcones.indexOf(',', begin);
            }
          }

          SiteDetail ancien = scc.getWebSite(id);
          int type = ancien.getType();

          /* update description en BD */
          SiteDetail descriptionSite2 =
              new SiteDetail(id, scc.getComponentId(), nomSite, description, nomPage, type, null,
              null, etat, popup);

          scc.updateWebSite(descriptionSite2);

          if (listIcons.size() > 0) {
            scc.associateIcons(id, listIcons);
          }

          /* publications : classer le site dans les themes cochés */
          ArrayList<String> arrayToClassify = new ArrayList<String>();
          boolean publish = false;
          ArrayList<String> arrayTopic = new ArrayList<String>();
          begin = 0;
          end = 0;
          end = listeTopics.indexOf(',', begin);
          String idTopic = null;
          while (end != -1) {
            idTopic = listeTopics.substring(begin, end);

            begin = end + 1;
            end = listeTopics.indexOf(',', begin);

            arrayTopic.add(idTopic);
            publish = true;
          }

          scc.updateClassification(id, arrayTopic);

          arrayToClassify.add(id);
          if (publish) {
            scc.publish(arrayToClassify); // set etat du site a 1
          } else {
            scc.dePublish(arrayToClassify);
          }
        }

        Collection<SiteDetail> listeSites = scc.getAllWebSite();
        request.setAttribute("ListSites", listeSites);
        request.setAttribute("BookmarkMode", Boolean.valueOf(scc.isBookmarkMode()));

        destination = "/webSites/jsp/manage.jsp";
      } else if (function.startsWith("design.jsp")) {
        // Action = newSite the firt time, never null
        String action = request.getParameter("Action");
        String id = request.getParameter("Id"); // jamais null sauf en creation ou en
        // Retrieve currentPath parameter : null when creating a webSite

        String currentPath = request.getParameter("path");
        if (currentPath != null) {
          currentPath = doubleAntiSlash(currentPath);
        }

        // ADD NEW SITE -------------------------------------------------------------
        if (action.equals("newSite")) {
          // Filled at first access then null
          String nomSite = request.getParameter("nomSite");
          // Filled at first access then null
          String description = request.getParameter("description");
          // Filled at first access then null
          String nomPage = request.getParameter("nomPage");
          String tempPopup = request.getParameter("popup");
          // Filled at first creation then null
          String listeIcones = request.getParameter("ListeIcones");
          // = en cas de new Site ou de classifySite
          String listeTopics = request.getParameter("ListeTopics");
          // Retrieve positions
          String positions = request.getParameter("Positions");

          int popup = 0;
          if ((tempPopup != null) && (tempPopup.length() > 0)) {
            popup = 1;
          }

          ArrayList<String> listIcons = new ArrayList<String>();
          int begin = 0;
          int end = 0;
          if (listeIcones != null) {
            end = listeIcones.indexOf(',', begin);
            while (end != -1) {
              listIcons.add(listeIcones.substring(begin, end));
              begin = end + 1;
              end = listeIcones.indexOf(',', begin);
            }
          }

          /* recuperation de l'id */
          id = scc.getNextId();

          /* Creer le repertoire id */
          scc.createFolder(scc.getWebSitePathById(id));

          // Persist siteDetail inside database type 0 = site cree
          SiteDetail descriptionSite =
              new SiteDetail(id, scc.getComponentId(), nomSite, description, nomPage, 0, null,
              null, 0, popup);
          descriptionSite.setPositions(positions);

          String pubId = scc.createWebSite(descriptionSite);
          descriptionSite = scc.getWebSite(id);
          scc.setSessionSite(descriptionSite);

          if (listIcons.size() > 0) {
            scc.associateIcons(id, listIcons);
          }

          currentPath = scc.getWebSitePathById(id);

          /* ajout de la page principale */
          String code = " ";

          /* Creer la page principale */
          scc.createFile(currentPath, nomPage, code);

          /* publications : classer le site dans les themes cochés */
          ArrayList<String> arrayToClassify = new ArrayList<String>();
          boolean publish = false;
          begin = 0;
          end = 0;
          end = listeTopics.indexOf(',', begin);
          String idTopic;
          while (end != -1) {
            idTopic = listeTopics.substring(begin, end);

            begin = end + 1;
            end = listeTopics.indexOf(',', begin);

            // ajout de la publication dans le theme
            scc.addPublicationToFolder(pubId, idTopic);

            publish = true;
          }

          if (publish) {
            arrayToClassify.add(id);
            scc.publish(arrayToClassify); // set etat du site a 1
          }

          request.setAttribute("Site", descriptionSite);
        } else if (action.equals("updateDescription")) { // type 0 design ou 2 upload

          // = rempli au premier acces a designSite pui toujours null
          String nomSite = request.getParameter("nomSite");

          // = rempli la premiere fois a la creation, puis toujours null
          String description = request.getParameter("description");

          // = rempli la premiere fois a la creation, puis toujours null
          String nomPage = request.getParameter("nomPage");
          String tempPopup = request.getParameter("popup");
          String etat = request.getParameter("etat");
          // = rempli la premiere fois a la creation, puis toujours null
          String listeIcones = request.getParameter("ListeIcones");

          int popup = 0;
          if ((tempPopup != null) && (tempPopup.length() > 0)) {
            popup = 1;
          }

          ArrayList<String> listIcons = new ArrayList<String>();
          int begin = 0;
          int end = 0;
          if (listeIcones != null) {
            end = listeIcones.indexOf(',', begin);
            while (end != -1) {
              listIcons.add(listeIcones.substring(begin, end));
              begin = end + 1;
              end = listeIcones.indexOf(',', begin);
            }
          }

          SiteDetail ancien = scc.getSessionSite();
          id = ancien.getSitePK().getId();
          int type = ancien.getType();

          /* verif que le nom de la page principale est correcte */
          Collection<File> collPages = scc.getAllWebPages2(currentPath);
          Iterator<File> j = collPages.iterator();
          boolean ok = false;
          File f;
          while (j.hasNext()) {
            f = j.next();
            if (f.getName().equals(nomPage)) {
              ok = true;
              break;
            }
          }

          boolean searchOk = ok;

          SiteDetail descriptionSite2 =
              new SiteDetail(id, scc.getComponentId(), nomSite, description, nomPage, type, null,
              null, Integer
              .parseInt(etat), popup);

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
        } else if (action.equals("addFolder")) {
          // = null la premiere fois, puis = nom du repertoire courant
          String name = request.getParameter("name");

          // ADD FOLDER -------------------------------------------------------------
          /* Creer le nouveau repertoire */
          scc.createFolder(currentPath + "/" + name);

          request.setAttribute("Site", scc.getSessionSite());

        } else if (action.equals("renameFolder")) {
          // = null la premiere fois, puis = nom du repertoire courant
          String name = request.getParameter("name");
          // = changement de noms des fichiers et repertoires
          String newName = request.getParameter("newName");

          // RENAME FOLDER -------------------------------------------------------------

          /* Modifier le nom du repertoire */
          scc.renameFolder(currentPath + "/" + name, currentPath + "/"
              + newName);

          request.setAttribute("Site", scc.getSessionSite());

        } else if (action.equals("deleteFolder")) {
          // null la premiere fois, puis = nom du repertoire courant
          String name = request.getParameter("name");

          // DELETE FOLDER -------------------------------------------------------------

          /* Supprimer le repertoire */
          scc.delFolder(currentPath + "/" + name);

          request.setAttribute("Site", scc.getSessionSite());
        } else if (action.equals("addPage")) {
          // = rempli la premiere fois a la creation, puis toujours null
          String nomPage = request.getParameter("nomPage");

          // ADD PAGE -------------------------------------------------------------
          String code = request.getParameter("Code"); // = code de la page a parser

          code = EncodeHelper.htmlStringToJavaString(code);

          /*
           * enleve les http :// localhost :8000/ WAwebSiteUploads / WA0webSite17 /18/ et on garde
           * seulement rep /icon .gif
           */
          String newCode = parseCodeSupprImage(scc, code, request, scc.getSettings(), currentPath);
          /*
           * enleve les http://localhost :8000 /webactiv/RwebSite /jsp/ et on garde seulement
           * rep/page.html
           */
          newCode = parseCodeSupprHref(scc, newCode, scc.getSettings(), currentPath);

          // Creer une nouvelle page
          scc.createFile(currentPath, nomPage, newCode);

          request.setAttribute("Site", scc.getSessionSite());
        } else if (action.equals("renamePage")) {
          // RENAME PAGE -------------------------------------------------------------
          // = null la premiere fois, puis = nom du repertoire courant
          String name = request.getParameter("name");
          // = changement de noms des fichiers et repertoires
          String newName = request.getParameter("newName");

          /* Modifier le nom du fichier */
          scc.renameFile(currentPath, name, newName);

          request.setAttribute("Site", scc.getSessionSite());
        } else if (action.equals("deletePage")) {
          // DELETE PAGE -------------------------------------------------------------
          // = null la premiere fois, puis = nom du repertoire courant
          String name = request.getParameter("name");

          /* Supprimer la page */
          scc.deleteFile(currentPath + "/" + name);

          request.setAttribute("Site", scc.getSessionSite());
        } else if (action.equals("classifySite")) { // cas de l'upload et du design
          // CLASSIFY SITE -------------------------------------------------------------
          // = en cas de new Site ou de classifySite
          String listeTopics = request.getParameter("ListeTopics");

          request.setAttribute("Site", scc.getSessionSite());

          /* publications : classer le site dans les themes cochés */
          ArrayList<String> arrayToClassify = new ArrayList<String>();
          boolean publish = false;

          ArrayList<String> arrayTopic = new ArrayList<String>();
          int begin = 0;
          int end = 0;
          end = listeTopics.indexOf(',', begin);
          String idTopic = null;
          while (end != -1) {
            idTopic = listeTopics.substring(begin, end);

            begin = end + 1;
            end = listeTopics.indexOf(',', begin);

            arrayTopic.add(idTopic);
            publish = true;
          }
          scc.updateClassification(id, arrayTopic);

          arrayToClassify.add(id);
          if (publish) {
            scc.publish(arrayToClassify); // set etat du site a 1
          } else {
            scc.dePublish(arrayToClassify); // set etat du site a 0
          }
        } else if (action.equals("design")) {
          // DESIGN -------------------------------------------------------------
          SiteDetail site = scc.getWebSite(id);
          scc.setSessionSite(site);
          request.setAttribute("Site", site);
        } else {
          // AUTRE -------------------------------------------------------------
          // view en cas de rechargement de la page pour naviguer dans le chemin
          // ou createSite annule
          // ou upload d'image

          SiteDetail site = scc.getSessionSite();
          id = site.getPK().getId();
          request.setAttribute("Site", site);

        }

        destination = "/webSites/jsp/design.jsp?Action=design&path=" + currentPath + "&Id=" + id;
      } else if (function.equals("EffectiveUploadFile")) {
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        String thePath = FileUploadUtil.getParameter(items, "path");
        FileItem item = FileUploadUtil.getFile(items);
        if (item != null) {
          String fileName = FileUploadUtil.getFileName(item);
          File file = new File(thePath, fileName);
          item.write(file);
        }

        request.setAttribute("UploadOk", Boolean.TRUE);

        destination = "/webSites/jsp/uploadFile.jsp?path=" + thePath;
      } else if (function.startsWith("descUpload.jsp")) {
        request.setAttribute("AllIcons", scc.getAllIcons());
        destination = "/webSites/jsp/descUpload.jsp";
      } else if (function.equals("EffectiveUploadSiteZip")) {
        List<FileItem> items = FileUploadUtil.parseRequest(request);

        String nomSite = FileUploadUtil.getParameter(items, "nomSite");
        String description = FileUploadUtil.getParameter(items, "description");
        String popupString = FileUploadUtil.getParameter(items, "popup");
        int popup = 0;
        if ("on".equals(popupString)) {
          popup = 1;
        }
        String nomPage = FileUploadUtil.getParameter(items, "nomPage");
        String listeIcones = FileUploadUtil.getParameter(items, "ListeIcones");
        String listeTopics = FileUploadUtil.getParameter(items, "ListeTopics");
        String positions = FileUploadUtil.getParameter(items, "Positions");

        FileItem fileItem = FileUploadUtil.getFile(items);
        if (fileItem != null) {
          /* recuperation de l'id = nom du directory */
          String id = scc.getNextId();

          // Persist uploaded website inside database, type=2
          SiteDetail descriptionSite =
              new SiteDetail(id, scc.getComponentId(), nomSite, description, nomPage, 2, null,
              null, 0, popup);

          descriptionSite.setPositions(positions);
          int result = scc.createWebSiteFromZipFile(descriptionSite, fileItem);
          switch (result) {
            case 0:
              /* creation en BD */
              ArrayList<String> listIcons = new ArrayList<String>();
              int begin = 0;
              int end = 0;
              if (listeIcones != null) {
                end = listeIcones.indexOf(',', begin);
                while (end != -1) {
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
              ArrayList<String> arrayToClassify = new ArrayList<String>();
              boolean publish = false;
              begin = 0;
              end = 0;
              end = listeTopics.indexOf(',', begin);
              while (end != -1) {
                idTopic = listeTopics.substring(begin, end);

                begin = end + 1;
                end = listeTopics.indexOf(',', begin);

                scc.addPublicationToFolder(pubId, idTopic);

                publish = true;
              }

              if (publish) {
                arrayToClassify.add(id);
                scc.publish(arrayToClassify); // set etat du site a 1
              }

              Collection<SiteDetail> listeSites = scc.getAllWebSite();
              request.setAttribute("ListSites", listeSites);
              request.setAttribute("BookmarkMode", Boolean.valueOf(scc.isBookmarkMode()));

              destination = "/webSites/jsp/manage.jsp";
              break;

            case -1:
              // le nom de la page principale n'est pas bonne, on supprime ce qu'on a dezipe
              scc.deleteDirectory(scc.getWebSitePathById(descriptionSite.getId()));

              request.setAttribute("Site", descriptionSite);
              request.setAttribute("AllIcons", scc.getAllIcons());
              request.setAttribute("ListeIcones", listeIcones);
              request.setAttribute("UploadOk", Boolean.TRUE);
              request.setAttribute("SearchOk", Boolean.FALSE);
              destination = "/webSites/jsp/descUpload.jsp";
              break;

            case -2:
              request.setAttribute("Site", descriptionSite);
              request.setAttribute("AllIcons", scc.getAllIcons());
              request.setAttribute("ListeIcones", listeIcones);
              request.setAttribute("UploadOk", Boolean.FALSE);
              destination = "/webSites/jsp/descUpload.jsp";
              break;
          }
        }
      } else {
        destination = "/webSites/jsp/" + function;
      }

    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    // Open the destination page
    SilverTrace.info("webSites", "WebSitesRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", "openPage = " + function);

    return destination;
  }

  private String getWebSitesDestination(SiteDetail sitedetail,
      HttpServletRequest request, WebSiteSessionController scc) {
    String siteId = sitedetail.getSitePK().getId();

    String nomPage = sitedetail.getContent();

    int type = sitedetail.getType();

    if (type == 1) {
      // type bookmark
      if (nomPage.indexOf("://") == -1) {
        // no protocol is mentionned
        // by default = "http"
        nomPage = "http://" + nomPage;
      }
    } else { // upload, design
      ResourceLocator settings = new ResourceLocator(
          "com.stratelia.webactiv.webSites.settings.webSiteSettings", "fr");

      nomPage = "http://" + getMachine(request) + "/"
          + settings.getString("Context") + "/"
          + scc.getWebSitePathById(siteId) + "/"
          + nomPage;
    }
    return "/webSites/jsp/ouvertureSite.jsp?URL="
        + EncodeHelper.javaStringToJsString(nomPage)
        + "&Popup=" + sitedetail.getPopup();

  }

  /* construitTab */
  /**
   * Method declaration
   *
   * @param deb
   * @return
   * @see
   */
  private List<String> construitTab(String deb) {
    /* deb = id/rep/ ou id\rep/ */
    /* res = [id | rep] */
    int i = 0;
    String noeud = "";
    List<String> array = new ArrayList<String>();

    while (i < deb.length()) {
      char car = deb.charAt(i);

      if (car == '/' || car == '\\') {
        array.add(noeud);
        noeud = "";
      } else {
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
   * @param request
   * @return
   * @see
   */
  private String getMachine(HttpServletRequest request) {
    ResourceLocator settings = new ResourceLocator(
        "com.stratelia.webactiv.webSites.settings.webSiteSettings", "fr");
    ResourceLocator generalSettings = new ResourceLocator(
        "com.stratelia.webactiv.general", "fr");

    String machine = settings.getString("Machine"); // ex :
    String context = (generalSettings.getString("ApplicationURL")).substring(1);

    if (machine.equals("")) {
      StringBuffer url = request.getRequestURL();

      List<String> a = construitTab(url.toString());

      int j = 1;

      while (true) {
        if (j > a.size()) {
          break;
        }

        if (!a.get(j).equals(context)) {
          if (machine.equals("")) {
            machine += a.get(j);
          } else {
            machine = machine + "/" + a.get(j);
          }
        } else {
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
   * @param profiles
   * @return
   * @see
   */
  private String getFlag(String[] profiles) {
    String flag = "Reader";

    for (int i = 0; i < profiles.length; i++) {
      // if admin, return it, we won't find a better profile
      if (profiles[i].equals("Admin")) {
        return profiles[i];
      }
      if (profiles[i].equals("Publisher")) {
        flag = profiles[i];
      }
    }
    return flag;
  }

  private String doubleAntiSlash(String chemin) {
    int i = 0;
    String res = chemin;
    boolean ok = true;

    while (ok) {
      int j = i + 1;
      if ((i < res.length()) && (j < res.length())) {
        char car1 = res.charAt(i);
        char car2 = res.charAt(j);

        if ((car1 == '\\' && car2 == '\\') || (car1 != '\\' && car2 != '\\')) {
        } else {
          String avant = res.substring(0, j);
          String apres = res.substring(j);
          if ((apres.startsWith("\\\\")) || (avant.endsWith("\\\\"))) {
          } else {
            res = avant + '\\' + apres;
            i++;
          }
        }
      } else {
        if (i < res.length()) {
          char car = res.charAt(i);
          if (car == '\\') {
            res = res + '\\';
          }
        }
        ok = false;
      }
      i = i + 2;
    }
    return res;
  }

  public String ignoreAntiSlash(String chemin) {
    /* ex : \\\rep1\\rep2\\rep3 */
    /* res = rep1\\rep2\\re3 */

    String res = chemin;
    boolean ok = false;
    while (!ok) {
      char car = res.charAt(0);
      if (car == '\\') {
        res = res.substring(1);
      } else {
        ok = true;
      }
    }
    return res;

  }

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
      } else {
        res = res + car;
      }
      i++;
    }
    return res;
  }

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

  private List<String> sortCommun(List<String> tabContexte, List<String> tab) {
    /* tabContexte = [id | rep1 | rep2] */
    /* tab = [id | rep1 | rep3] */
    /* res = [id | rep1] */
    int i = 0;
    boolean ok = true;
    List<String> array = new ArrayList<String>();

    while (ok && i < tabContexte.size()) {
      String contenuContexte = tabContexte.get(i);
      if (i < tab.size()) {
        String contenu = tab.get(i);
        if (contenuContexte.equals(contenu)) {
          array.add(contenu);

        } else {
          ok = false;
        }
        i++;
      } else {
        ok = false;
      }
    }
    return array;
  }

  private String sortReste(List<String> tab, List<String> tabCommun) {
    /* tab = [id | rep1 | rep2 | rep3] */
    /* tabCommun = [id | rep1] */
    /* res = rep2/rep3 */
    String res = "";

    int indice = tabCommun.size();

    while (indice < tab.size()) {
      String contenu = tab.get(indice);
      res += contenu + "/";
      indice++;
    }

    if (!res.equals("")) {
      res = res.substring(0, res.length() - 1);
    }

    return res;
  }

  private String parseCodeSupprImage(WebSiteSessionController scc, String code,
      HttpServletRequest request, ResourceLocator settings, String currentPath) {
    String theCode = code;
    String avant;
    String apres;
    int index;
    String finChemin;
    String image = "<IMG border=0 src=\"http://" + getMachine(request) + "/"
        + settings.getString("Context") + "/" + scc.getComponentId() + "/";
    int longueurImage = 19 + ("http://" + getMachine(request) + "/"
        + settings.getString("Context") + "/" + scc.getComponentId() + "/")
        .length();
    index = code.indexOf(image);
    if (index == -1) {
      return theCode;
    } else {
      avant = theCode.substring(0, index + 19);
      finChemin = theCode.substring(index + longueurImage);

      int indexGuillemet = finChemin.indexOf("\"");
      String absolute = finChemin.substring(0, indexGuillemet);

      apres = finChemin.substring(indexGuillemet);
      int indexSlash = absolute.lastIndexOf("/");
      String fichier = absolute.substring(indexSlash + 1);

      String deb = absolute.substring(0, indexSlash);
      List<String> tab = construitTab(deb + "/");

      /* id/rep1 */
      String cheminContexte = finNode(scc, currentPath);
      List<String> tabContexte = construitTab(cheminContexte + "/");
      List<String> tabCommun = sortCommun(tabContexte, tab);
      String reste = sortReste(tab, tabCommun);
      int nbPas = tabContexte.size() - tabCommun.size();
      String relatif = "";
      int i = 0;
      while (i < nbPas) {
        relatif += "../";
        i++;
      }

      if (reste.equals("")) {
        relatif += fichier;
      } else {
        relatif += reste + "/" + fichier;
      }
      apres = relatif + apres;
      return (avant + parseCodeSupprImage(scc, apres, request, settings,
          currentPath));
    }
  }

  private String parseCodeSupprHref(WebSiteSessionController scc, String code,
      ResourceLocator settings, String currentPath) {
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
    if (index == -1) {
      theReturn = theCode;
    } else {

      avant = theCode.substring(0, index + 9);

      apres = theCode.substring(index + 9);

      if (apres.substring(0, 7).equals("http://")) { /* lien externe */
        theReturn = avant
            + parseCodeSupprHref(scc, apres, settings, currentPath);
      } else if (apres.substring(0, 6).equals("ftp://")) { /* lien externe */
        theReturn = avant
            + parseCodeSupprHref(scc, apres, settings, currentPath);
      } else if (apres.substring(0, 3).equals("rr:")) { /* deja en relatif */

        apres = apres.substring(3);

        theReturn = avant
            + parseCodeSupprHref(scc, apres, settings, currentPath);
      } else if (apres.substring(0, 3).equals("aa:")) {
        // lien absolu a transformer en relatif

        /* finChemin = rep/coucou.html">... */
        finChemin = theCode.substring(index + 9 + 3);
        SilverTrace.info("webSites", "JSPcreateSite",
            "root.MSG_GEN_PARAM_VALUE", "finChemin = " + finChemin);

        /* traitement */
        int indexGuillemet = finChemin.indexOf("\"");
        SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE",
            "indexGuillemet = " + Integer.toString(indexGuillemet));

        /* absolute = rep/coucou.html */
        String absolute = finChemin.substring(0, indexGuillemet);
        SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "absolute = "
            + absolute);

        /* apres = ">... */
        apres = finChemin.substring(indexGuillemet);
        SilverTrace.
            info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "apres = " + apres);

        int indexSlash = absolute.lastIndexOf("\\");
        SilverTrace.info("webSites", "JSPcreateSite", "root.MSG_GEN_PARAM_VALUE", "indexSlash = "
            + Integer.toString(indexSlash));

        if (indexSlash == -1) {
          // pas d'arborescence, le fichier du lien est sur la racine
          fichier = absolute;
          deb = "";
        } else {
          /* fichier = coucou.html */
          fichier = absolute.substring(indexSlash + 1);
          deb = absolute.substring(0, indexSlash);
        }
        List<String> tab = construitTab(deb + "/");
        // dans ce tableau il manque l'id

        /* cheminContexte = id/rep */
        int longueur = scc.getComponentId().length();
        int index2 = currentPath.lastIndexOf(scc.getComponentId());
        String chemin = currentPath.substring(index2 + longueur);

        chemin = chemin.substring(1);
        chemin = supprDoubleAntiSlash(chemin);
        String cheminContexte = chemin;
        List<String> tabContexte = construitTab(cheminContexte + "/");
        /* ajoute l'id dans le premier tableau */
        tab.add(0, tabContexte.get(0));

        /* tabCommun = [id | rep] */
        List<String> tabCommun = sortCommun(tabContexte, tab);

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

        if (reste.equals("")) {
          relatif += fichier;
        } else {
          relatif += reste + "/" + fichier;
        }

        /* relatif = vide */
        apres = relatif + apres;
        theReturn = avant
            + parseCodeSupprHref(scc, apres, settings, currentPath);
      }
    }
    return theReturn;
  }
}
