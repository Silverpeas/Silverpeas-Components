/*
 * Copyright (C) 2000 - 2022 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites.servlets;

import org.apache.commons.fileupload.FileItem;
import org.silverpeas.components.websites.control.WebSiteSessionController;
import org.silverpeas.components.websites.service.WebSitesException;
import org.silverpeas.components.websites.servlets.design.SiteDesignActionHandler;
import org.silverpeas.components.websites.siteManage.model.FolderDetail;
import org.silverpeas.components.websites.siteManage.model.SiteDetail;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.util.JSONCodec;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.mvc.util.RoutingException;
import org.silverpeas.core.web.mvc.util.WysiwygRouting;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.core.contribution.model.CoreContributionType.UNKNOWN;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

public class WebSitesRequestRouter extends ComponentRequestRouter<WebSiteSessionController> {

  private static final long serialVersionUID = -536203260896933461L;
  private static final String FOLDER_PATH_FILTER = "&Path=";
  private static final String WEBSITE_BASE_URL = "/webSites/jsp/organize.jsp?Action=Search&Id=";
  private static final String PUBLISHER_ROLE = "Publisher";
  private static final String ADMIN_ROLE = "Admin";
  public static final String PATH_PARAM = "Path";
  public static final String CURRENT_FOLDER_PARAM = "CurrentFolder";
  public static final String SITE_NAME_PARAM = "nomSite";
  public static final String DESCRIPTION_PARAM = "description";
  public static final String POPUP_PARAM = "popup";
  public static final String PAGE_NAME_PARAM = "nomPage";
  public static final String TOPIC_LIST_PARAM = "ListeTopics";
  public static final String ICON_LIST_PARAM = "ListeIcones";

  /**
   * This method has to be implemented in the component request router class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   */
  @Override
  public String getSessionControlBeanName() {
    return "webSites";
  }

  @Override
  public WebSiteSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new WebSiteSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented by the component request router it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param scc The component Session Control, build and initialised.
   * @param request The entering request. The request router need it to get parameters
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, WebSiteSessionController scc, HttpRequest request) {
    String destination;

    // the flag is the best user's profile
    String flag = getFlag(scc.getUserRoles());
    request.setAttribute("BestRole", flag);
    try {
      if (function.startsWith("Main")) {
        destination = processForWebSiteHome(scc, request, flag);
      } else if (function.startsWith("listSite.jsp")) {
        destination = listAllSites(scc, request);
      } else if (function.equals("siteAsJson")) {
        final String siteId = request.getParameter("id");
        final SiteDetail siteDetail = scc.getWebSite(siteId);
        destination = sendJson(JSONCodec.encodeObject(o -> o
            .put("id", siteDetail.getId())
            .put("url", WebSitesUtil.getSiteURL(request, siteDetail.getInstanceId(), siteId))
            .put("type", siteDetail.getSiteType())
            .put("name", siteDetail.getName())
            .put("contentPath", siteDetail.getContentPagePath())
            .put(POPUP_PARAM, siteDetail.getPopup() == 1)));
      } else if (function.startsWith("listSite_reader.jsp")) {
        destination = listAllSitesForReader(scc, request);
      } else if (function.startsWith("portlet")) {
        destination = listAllSitesWithinPortlet(scc, request, flag);
      } else if (function.startsWith("searchResult")) {
        destination = listSitesMatchingSearch(scc, request);
      } else if ("SuggestLink".equals(function)) {
        destination = suggestLink(scc, request);
      } else if ("DisplaySite".equals(function)) {
        final String sitePage = request.getParameter("SitePage");
        request.setAttribute("sitePage", sitePage);
        destination = "/webSites/jsp/openSite.jsp";
      } else if (function.startsWith("ToWysiwyg")) {
        destination = toWysiwygEditor(scc, request, flag);
      } else if (function.startsWith("FromWysiwyg")) {
        destination = fromWysiwygEditor(scc, request);
      } else if ("TopicUp".equals(function)) {
        destination = moveUpTopic(scc, request);
      } else if ("TopicDown".equals(function)) {
        destination = moveDownTopic(scc, request);
      } else if ("PubUp".equals(function)) {
        destination = moveUpPublication(scc, request);
      } else if ("PubDown".equals(function)) {
        destination = moveDownPublication(scc, request);
      } else if (function.startsWith("modifDesc.jsp")) {
        destination = modifySiteDescription(scc, request);
      } else if ("Suggest".equals(function)) {
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
        destination = organizeSites(scc, request);
      } else if ("AddTopic".equals(function)) {
        destination = addTopic(scc, request);
      } else if (function.startsWith("updateTopic")) {
        destination = updateTopic(scc, request);
      } else if (function.startsWith("classifyDeclassify.jsp")) {
        destination = classification(scc, request);
      } else if (function.startsWith("manage.jsp")) {
        destination = manageSite(scc, request);
      } else if (function.startsWith("design.jsp")) {
        destination = new SiteDesignActionHandler(scc).handle(request);
      } else if (function.equals("EffectiveUploadFile")) {
        destination = uploadFile(scc, request);
      } else if (function.startsWith("descUpload.jsp")) {
        request.setAttribute("AllIcons", scc.getAllIcons());
        destination = "/webSites/jsp/descUpload.jsp";
      } else if (function.equals("EffectiveUploadSiteZip")) {
        destination = uploadSite(scc, request);
      } else {
        destination = "/webSites/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      destination = "/admin/jsp/errorpageMain.jsp";
    }

    return destination;
  }

  private String uploadSite(final WebSiteSessionController scc, final HttpRequest request)
      throws Exception {
    List<FileItem> items = request.getFileItems();

    String nomSite = FileUploadUtil.getParameter(items, SITE_NAME_PARAM);
    String description = FileUploadUtil.getParameter(items, DESCRIPTION_PARAM);
    String popupString = FileUploadUtil.getParameter(items, POPUP_PARAM);
    int popup = "on".equals(popupString) ? 1 : 0;
    String nomPage = FileUploadUtil.getParameter(items, PAGE_NAME_PARAM);
    String listeIcones = FileUploadUtil.getParameter(items, ICON_LIST_PARAM);
    String listeTopics = FileUploadUtil.getParameter(items, TOPIC_LIST_PARAM);
    String positions = FileUploadUtil.getParameter(items, "Positions");
    String destination = "";

    FileItem fileItem = FileUploadUtil.getFile(items);
    if (fileItem != null) {
          /* recuperation de l'id = nom du directory */
      String id = scc.getNextId();

      // Persist uploaded website inside database, type=2
      SiteDetail descriptionSite =
          new SiteDetail(id, scc.getComponentId(), nomSite, description, nomPage, 2, null, null,
              0, popup);

      descriptionSite.setPositions(positions);
      int result = scc.createWebSiteFromZipFile(descriptionSite, fileItem);
      if (result == 0) {
        createSite(scc, descriptionSite, listeIcones, listeTopics);
        Collection<SiteDetail> listeSites = scc.getAllWebSite();
        request.setAttribute("ListSites", listeSites);
        request.setAttribute("BookmarkMode", scc.isBookmarkMode());
        destination = "/webSites/jsp/manage.jsp";
      } else if (result == -1) {
        // le nom de la page principale n'est pas bonne, on supprime ce qu'on a dezipe
        scc.deleteDirectory(scc.getWebSitePathById(descriptionSite.getId()));
        request.setAttribute("Site", descriptionSite);
        request.setAttribute("AllIcons", scc.getAllIcons());
        request.setAttribute(ICON_LIST_PARAM, listeIcones);
        request.setAttribute("UploadOk", Boolean.TRUE);
        request.setAttribute("SearchOk", Boolean.FALSE);
        destination = "/webSites/jsp/descUpload.jsp";
      } else if (result == -2) {
        request.setAttribute("Site", descriptionSite);
        request.setAttribute("AllIcons", scc.getAllIcons());
        request.setAttribute(ICON_LIST_PARAM, listeIcones);
        request.setAttribute("UploadOk", Boolean.FALSE);
        destination = "/webSites/jsp/descUpload.jsp";
      } else if (result == -3) { // the zip content isn't correct
        scc.deleteDirectory(scc.getWebSitePathById(descriptionSite.getId()));
        request.setAttribute("Site", descriptionSite);
        request.setAttribute("AllIcons", scc.getAllIcons());
        request.setAttribute(ICON_LIST_PARAM, listeIcones);
        request.setAttribute("UploadOk", Boolean.FALSE);
        destination = "/webSites/jsp/descUpload.jsp";
      }
    }
    return destination;
  }

  private void createSite(final WebSiteSessionController scc, SiteDetail site, String listOfIcons,
      String listOfTopics) throws WebSitesException {
        /* creation en BD */
    ArrayList<String> listIcons = new ArrayList<>();
    if (listOfIcons != null) {
      for (String icon : listOfIcons.split(",")) {
        if (!icon.isEmpty()) {
          listIcons.add(icon);
        }
      }
    }

    String pubId = scc.createWebSite(site);
    if (!listIcons.isEmpty()) {
      scc.associateIcons(site.getId(), listIcons);
    }

              /* publications : classer le site dans les themes cochés */
    ArrayList<String> arrayToClassify = new ArrayList<>();
    boolean publish = false;
    for (String idTopic : listOfTopics.split(",")) {
      if (!idTopic.isEmpty()) {
        scc.addPublicationToFolder(pubId, idTopic);
        publish = true;
      }
    }

    if (publish) {
      arrayToClassify.add(site.getId());
      scc.publish(arrayToClassify);
    }
  }

  private String uploadFile(final WebSiteSessionController scc, final HttpRequest request)
      throws Exception {

    List<FileItem> items = request.getFileItems();
    boolean status = true;
    String thePath = FileUploadUtil.getParameter(items, PATH_PARAM);
    scc.checkPath(thePath);
    FileItem item = FileUploadUtil.getFile(items);
    if (item != null) {
      status = (scc.addFileIntoWebSite(thePath, item) == 0);
    }

    request.setAttribute("UploadOk", status);

    return  "/webSites/jsp/uploadFile.jsp?Path=" + thePath;
  }

  private String manageSite(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String action = request.getParameter("Action");
    if ("addBookmark".equals(action)) {
      addBookmarkToSite(scc, request);
    } else if ("deleteWebSites".equals(action)) {
      deleteSite(scc, request);
    } else if ("updateDescription".equals(action)) {
      updateSite(scc, request);
    }

    Collection<SiteDetail> listeSites = scc.getAllWebSite();
    request.setAttribute("ListSites", listeSites);
    request.setAttribute("BookmarkMode", scc.isBookmarkMode());

    return "/webSites/jsp/manage.jsp";
  }

  private void updateSite(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String id = request.getParameter("Id");
    String nomSite = request.getParameter(SITE_NAME_PARAM);
    String description = request.getParameter(DESCRIPTION_PARAM);
    String nomPage = request.getParameter(PAGE_NAME_PARAM);
    String tempPopup = request.getParameter(POPUP_PARAM);
    String letat = request.getParameter("etat");
    String listeIcones = request.getParameter(ICON_LIST_PARAM);
    String listeTopics = request.getParameter(TOPIC_LIST_PARAM);

    int popup = (tempPopup != null) && (tempPopup.length() > 0) ? 1 : 0;
    int etat = StringUtil.isDefined(letat) ? Integer.parseInt(letat) : -1;

    List<String> listIcons = new ArrayList<>();
    for (String icon : listeIcones.split(",")) {
      if (!icon.isEmpty()) {
        listIcons.add(icon);
      }
    }
    SiteDetail ancien = scc.getWebSite(id);
    int type = ancien.getSiteType();
          /* update description en BD */
    SiteDetail descriptionSite2 =
        new SiteDetail(id, scc.getComponentId(), nomSite, description, nomPage, type, null, null,
            etat, popup);

    scc.updateWebSite(descriptionSite2);
    if (listIcons.size() > 0) {
      scc.associateIcons(id, listIcons);
    }

          /* publications : classer le site dans les themes cochés */
    ArrayList<String> arrayToClassify = new ArrayList<>();
    boolean publish = false;
    ArrayList<String> arrayTopic = new ArrayList<>();
    for (String idTopic : listeTopics.split(",")) {
      if (!idTopic.isEmpty()) {
        arrayTopic.add(idTopic);
        publish = true;
      }
    }

    scc.updateClassification(id, arrayTopic);

    arrayToClassify.add(id);
    if (publish) {
      scc.publish(arrayToClassify); // set etat du site a 1
    } else {
      scc.dePublish(arrayToClassify);
    }
  }

  private void deleteSite(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    ArrayList<String> listToDelete = new ArrayList<>();
    String liste = request.getParameter("SiteList");

    for (String siteId : liste.split(",")) {
      if (!siteId.isEmpty()) {
        listToDelete.add(siteId);

        // recup info sur ce webSite
        SiteDetail info = scc.getWebSite(siteId);
        // type = 0 : site cree, type = 1 : site bookmark, type = 2 : site upload
        if (info.getSiteType() != 1) {
          // type != bookmark
          // delete directory
          scc.deleteDirectory(scc.getWebSitePathById(siteId));
        }

        // delete publication
        String pubId = scc.getIdPublication(siteId);
        scc.deletePublication(pubId);
      }
    }
          /* delete en BD */
    scc.deleteWebSites(listToDelete);
  }

  private void addBookmarkToSite(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String nomSite = request.getParameter(SITE_NAME_PARAM);
    String description = request.getParameter(DESCRIPTION_PARAM);
    String nomPage = request.getParameter(PAGE_NAME_PARAM);
    String tempPopup = request.getParameter(POPUP_PARAM);
    String listeIcones = request.getParameter(ICON_LIST_PARAM);
    String listeTopics = request.getParameter(TOPIC_LIST_PARAM);

    // Retrieve positions
    String positions = request.getParameter("Positions");
    int popup = (tempPopup != null) && (tempPopup.length() > 0) ? 1 : 0;
    List<String> listIcons =
        listeIcones != null ? CollectionUtil.asList(listeIcones.split(",")) : new ArrayList<>();
    if (listIcons.get(0).isEmpty()) {
      listIcons.remove(0);
    }
          /* recuperation de l'id */
    String id = scc.getNextId();

          /* Persist siteDetail inside database, type 1 = bookmark */
    SiteDetail descriptionSite =
        new SiteDetail(id, scc.getComponentId(), nomSite, description, nomPage, 1, null, null, 0,
            popup);
    descriptionSite.setPositions(positions);

    String pubId = scc.createWebSite(descriptionSite);

    if (listIcons.size() > 0) {
      scc.associateIcons(id, listIcons);
    }

    boolean publish = false;
    for (String idTopic : listeTopics.split(",")) {
      if (!idTopic.isEmpty()) {
        scc.addPublicationToFolder(pubId, idTopic);
        publish = true;
      }
    }

    ArrayList<String> arrayToClassify = new ArrayList<>();
    if (publish) {
      arrayToClassify.add(id);
      scc.publish(arrayToClassify);
    }
  }

  private String classification(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {

    String action = request.getParameter("Action");
    String id = request.getParameter("TopicId");
    String linkedPathString = request.getParameter(PATH_PARAM);

    Collection<SiteDetail> listeSites = scc.getAllWebSite();
    request.setAttribute("ListSites", listeSites);
    request.setAttribute(CURRENT_FOLDER_PARAM, scc.getSessionTopic());

    return
        "/webSites/jsp/classifyDeclassify.jsp?Action=" + action + "&TopicId=" + id +
            FOLDER_PATH_FILTER + linkedPathString;
  }

  private String updateTopic(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {

    String id = request.getParameter("ChildId");
    String path = request.getParameter(PATH_PARAM);

    NodeDetail folderDetail = scc.getFolderDetail(id);
    request.setAttribute(CURRENT_FOLDER_PARAM, folderDetail);

    return "/webSites/jsp/updateTopic.jsp?ChildId=" + id + FOLDER_PATH_FILTER + path;
  }

  private String addTopic(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {

    String action = request.getParameter("Action");// =Add
    String fatherId = request.getParameter("Id");
    String newTopicName = request.getParameter("Name");
    String newTopicDescription = request.getParameter(DESCRIPTION_PARAM);

    NodeDetail folder =
        new NodeDetail("X", newTopicName, newTopicDescription, 0, "X");
    scc.addFolder(folder, "");

    return "/webSites/jsp/addTopic.jsp?Action=" + action + "&Id=" + fatherId;
  }

  private String organizeSites(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String action = request.getParameter("Action");
    String id = request.getParameter("Id");
    String path = request.getParameter(PATH_PARAM);

    if (action == null) {
      action = "Search";
    } else if ("Update".equals(action)) {
      String childId = request.getParameter("ChildId");
      String name = request.getParameter("Name");
      String description = request.getParameter(DESCRIPTION_PARAM);
      NodeDetail folder =
          new NodeDetail(childId, name, description, 0, "X");
      scc.updateFolderHeader(folder, "");
      action = "Search";
    } else if ("Delete".equals(action)) {
          /*
           * declassification des sites et suppression des themes
           */
          /* delete folder */
      String[] paramValues = request.getParameterValues("checkbox");
      String Id = request.getParameter("checkbox");
      if (paramValues == null && Id != null) {
        // delete theme et publications
        scc.deleteFolder(Id);
      }

      String[] listeId = paramValues == null ? new String[0] : paramValues;
      for (String idFolderToDelete: listeId) {
        scc.deleteFolder(idFolderToDelete);
      }

          /* quels sont les sites a depublier */
      ArrayList<String> arrayToDePublish = new ArrayList<>();
      Collection<SiteDetail> liste = scc.getAllWebSite();
      for (SiteDetail site: liste) {
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
      arrayToDePublish = new ArrayList<>();
      int begin = 0;
      int end = listeSite.indexOf(',', begin);
      String idPubToDeClassify;
      PublicationDetail pub;
      Collection<NodePK> listNodePk;
      while (end != -1) {
        idPubToDeClassify = listeSite.substring(begin, end);
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
    } else if ("classify".equals(action)) {
      String listeSite = request.getParameter("SiteList");
      ArrayList<String> arrayToClassify = new ArrayList<>();
      int begin = 0;
      int end = listeSite.indexOf(',', begin);

      String idSiteToClassify;
      String pubId;
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

      ArrayList<String> arrayToDeClassify = new ArrayList<>();
      int begin = 0;
      int end = listeSite.indexOf(',', begin);
      String idSiteToDeClassify;
      String pubId;
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
    request.setAttribute(CURRENT_FOLDER_PARAM, webSitesCurrentFolder);

    return "/webSites/jsp/organize.jsp?Action=" + action + "&Id=" + id + FOLDER_PATH_FILTER + path;
  }

  private String modifySiteDescription(final WebSiteSessionController scc,
      final HttpRequest request) throws WebSitesException {
    String id = request.getParameter("Id");
    // = null ou rempli si type= design
    String currentPath = request.getParameter(PATH_PARAM);
    String type = request.getParameter("type"); // null ou design

    request.setAttribute("Site", scc.getWebSite(id));
    request.setAttribute("AllIcons", scc.getAllIcons());
    request.setAttribute("ListIcons", scc.getIcons(id));

    String recupParam = request.getParameter("RecupParam"); // =null ou oui
    String complete = null;
    if (recupParam != null) {// =oui
      String nom = request.getParameter("Nom");
      String description = request.getParameter(DESCRIPTION_PARAM);
      String lapage = request.getParameter("Page");
      String listeIcones = request.getParameter(ICON_LIST_PARAM);

      type = "design";
      complete =
          "&RecupParam=oui&Nom=" + nom + "&Description=" + description + "&Page=" + lapage +
              "&ListeIcones=" + listeIcones;
    }

    String destination = "/webSites/jsp/modifDesc.jsp?Id=" + id;
    if (complete != null) {
      destination += complete;
    }
    if (currentPath != null) {
      destination += FOLDER_PATH_FILTER + currentPath;
    }
    if (type != null) {
      destination += "&type=" + type;
    }
    return destination;
  }

  private String moveDownPublication(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String pubId = request.getParameter("Id");

    scc.changePubsOrder(pubId, 1);

    String id = scc.getSessionTopic().getNodePK().getId();
    FolderDetail webSitesCurrentFolder = scc.getFolder(id);
    scc.setSessionTopic(webSitesCurrentFolder);
    request.setAttribute(CURRENT_FOLDER_PARAM, webSitesCurrentFolder);

    return WEBSITE_BASE_URL + id;
  }

  private String moveUpPublication(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String pubId = request.getParameter("Id");

    scc.changePubsOrder(pubId, -1);

    String id = scc.getSessionTopic().getNodePK().getId();
    FolderDetail webSitesCurrentFolder = scc.getFolder(id);
    scc.setSessionTopic(webSitesCurrentFolder);
    request.setAttribute(CURRENT_FOLDER_PARAM, webSitesCurrentFolder);

    return WEBSITE_BASE_URL + id;
  }

  private String moveDownTopic(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String topicId = request.getParameter("Id");

    scc.changeTopicsOrder("down", topicId);

    String id = scc.getSessionTopic().getNodePK().getId();
    FolderDetail webSitesCurrentFolder = scc.getFolder(id);
    scc.setSessionTopic(webSitesCurrentFolder);
    request.setAttribute(CURRENT_FOLDER_PARAM, webSitesCurrentFolder);

    return WEBSITE_BASE_URL + id;
  }

  private String moveUpTopic(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String topicId = request.getParameter("Id");

    scc.changeTopicsOrder("up", topicId);

    String id = scc.getSessionTopic().getNodePK().getId();
    FolderDetail webSitesCurrentFolder = scc.getFolder(id);
    scc.setSessionTopic(webSitesCurrentFolder);
    request.setAttribute(CURRENT_FOLDER_PARAM, webSitesCurrentFolder);

    return WEBSITE_BASE_URL + id;
  }

  private String fromWysiwygEditor(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String path = request.getParameter(PATH_PARAM);
    scc.checkPath(path);
    String id = request.getParameter("id");

    SiteDetail site = scc.getWebSite(id);
    request.setAttribute("Site", site);

    return "/webSites/jsp/design.jsp?Action=design" + FOLDER_PATH_FILTER + path + "&Id=" + id;
  }

  private String toWysiwygEditor(final WebSiteSessionController scc, final HttpRequest request,
      final String userRole) throws WebSitesException, UnsupportedEncodingException,
      RoutingException {
    String path = request.getParameter(PATH_PARAM);
    scc.checkPath(path);
    String name = request.getParameter("name");
    String nameSite = request.getParameter("nameSite");
    String id = request.getParameter("id");

    WysiwygRouting routing = new WysiwygRouting();
    WysiwygRouting.WysiwygRoutingContext context =
        WysiwygRouting.WysiwygRoutingContext.fromComponentSessionController(scc)
            .withContributionId(ContributionIdentifier.from(scc.getComponentId(), id, UNKNOWN))
            .withBrowseInfo(URLEncoder.encode(nameSite, "UTF-8"))
            .withFileName(URLEncoder.encode(name, "UTF-8") + FOLDER_PATH_FILTER +
                URLEncoder.encode(path, "UTF-8"))
            .withLanguage(I18NHelper.defaultLanguage)
            .withComeBackUrl(URLEncoder.encode(URLUtil.getApplicationURL() +
                URLUtil.getURL(scc.getSpaceId(), scc.getComponentId()) + "FromWysiwyg?Path=" +
                path + "&name=" + name + "&nameSite=" + nameSite + "&profile=" + userRole + "&id=" +
                id, "UTF-8"));
    return routing.getDestinationToWysiwygEditor(context);
  }

  private String suggestLink(final WebSiteSessionController scc, final HttpRequest request) {
    String nomSite = request.getParameter(SITE_NAME_PARAM);
    String description = request.getParameter(DESCRIPTION_PARAM);
    String nomPage = request.getParameter(PAGE_NAME_PARAM);
    String auteur = request.getParameter("auteur");
    String date = request.getParameter("date");
    String listeIcones = request.getParameter(ICON_LIST_PARAM);

    int begin = 0;
    int end = listeIcones.indexOf(',', begin);
    StringBuilder listeMessage = new StringBuilder("");

    // parcours des icones
    while (end != -1) {
      String nom = listeIcones.substring(begin, end);
      listeMessage.append("- ").append(nom).append("\n");
      begin = end + 1;
      end = listeIcones.indexOf(',', begin);
    }

    scc.notifyPublishers(auteur, nomSite, description, nomPage, listeMessage.toString(), date);

    request.setAttribute("SuggestionName", nomSite);
    request.setAttribute("SuggestionUrl", nomPage);
    return getDestination("Main", scc, request);
  }

  private String listSitesMatchingSearch(final WebSiteSessionController scc,
      final HttpRequest request) {
    String id = request.getParameter("Id");
    String typeRequest = defaultStringIfNotDefined(request.getParameter("Type"), "Site");
    String destination = "";
    if ("Publication".equals(typeRequest)) {
      // recherche de l'url complete d'acces a la page
      try {
        final PublicationDetail pubDetail = scc.getPublicationDetail(id);
        destination = "/webSites/jsp/openSiteFromSearch.jsp?siteId=" + pubDetail.getVersion();
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    } else if ("Site".equals(typeRequest)) {
      try {
        final SiteDetail sitedetail =  scc.getWebSite(id);
        destination = "/webSites/jsp/openSiteFromSearch.jsp?siteId=" + sitedetail.getId();
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    } else if ("Node".equals(typeRequest)) {
      destination = scc.getComponentUrl() + "listSite.jsp?Action=Search&Id=" + id;
    }
    return destination;
  }

  private String listAllSitesWithinPortlet(final WebSiteSessionController scc,
      final HttpRequest request, final String userRole) throws WebSitesException {
    FolderDetail webSitesCurrentFolder = scc.getFolder("0");
    scc.setSessionTopic(webSitesCurrentFolder);
    request.setAttribute(CURRENT_FOLDER_PARAM, webSitesCurrentFolder);

    if (PUBLISHER_ROLE.equals(userRole) || ADMIN_ROLE.equals(userRole)) {
      return "/webSites/jsp/listSitePortlet.jsp";
    } else {
      // reader
      return "/webSites/jsp/listSite_readerPortlet.jsp";
    }
  }

  private String listAllSitesForReader(final WebSiteSessionController scc,
      final HttpRequest request) throws WebSitesException {
    String action = request.getParameter("Action");
    String id = request.getParameter("Id");

    if (action == null) {
      id = "0";
      action = "Search";
    }

    FolderDetail webSitesCurrentFolder = scc.getFolder(id);
    scc.setSessionTopic(webSitesCurrentFolder);
    request.setAttribute(CURRENT_FOLDER_PARAM, webSitesCurrentFolder);

    return "/webSites/jsp/listSite_reader.jsp?Action=" + action + "&Id=" + id;
  }

  private String listAllSites(final WebSiteSessionController scc, final HttpRequest request)
      throws WebSitesException {
    String action = request.getParameter("Action");
    String id = request.getParameter("Id");

    if (action == null) {
      id = "0";
      action = "Search";
    }

    FolderDetail webSitesCurrentFolder = scc.getFolder(id);
    scc.setSessionTopic(webSitesCurrentFolder);
    request.setAttribute(CURRENT_FOLDER_PARAM, webSitesCurrentFolder);

    return "/webSites/jsp/listSite.jsp?Action=" + action + "&Id=" + id;
  }

  private String processForWebSiteHome(WebSiteSessionController scc, HttpRequest request,
      String userRole) throws WebSitesException {
    FolderDetail webSitesCurrentFolder = scc.getFolder("0");
    scc.setSessionTopic(webSitesCurrentFolder);
    request.setAttribute(CURRENT_FOLDER_PARAM, webSitesCurrentFolder);
    if (PUBLISHER_ROLE.equals(userRole) || ADMIN_ROLE.equals(userRole)) {
      return "/webSites/jsp/listSite.jsp";
    } else {
      // reader
      return "/webSites/jsp/listSite_reader.jsp";
    }
  }

  private String getFlag(String[] profiles) {
    String flag = "Reader";

    for (final String profile : profiles) {
      // if admin, return it, we won't find a better profile
      if (ADMIN_ROLE.equals(profile)) {
        return profile;
      }
      if (PUBLISHER_ROLE.equals(profile)) {
        flag = profile;
      }
    }
    return flag;
  }

}
