/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites.servlets.design;

import org.silverpeas.components.websites.control.WebSiteSessionController;
import org.silverpeas.components.websites.service.WebSitesException;
import org.silverpeas.components.websites.servlets.WebSitesRequestRouter;
import org.silverpeas.components.websites.siteManage.model.SiteDetail;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.web.http.HttpRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.silverpeas.components.websites.servlets.WebSitesUtil.buildTab;
import static org.silverpeas.components.websites.servlets.WebSitesUtil.getComponentURL;

/**
 * A handler of actions in the web site designing.
 * @author mmoquillon
 */
public class SiteDesignActionHandler {

  private static final int WEBSITE_BASE_URL_LENGTH = 19;

  private final WebSiteSessionController controller;

  public SiteDesignActionHandler(final WebSiteSessionController controller) {
    this.controller = controller;
  }

  public String handle(final HttpRequest request) throws WebSitesException {
    // Action = never null
    String action = request.getParameter("Action");
    // never null expected in creation
    String id = request.getParameter("Id");
    // Retrieve currentPath parameter: null when creating a webSite
    String currentPath = getCurrentPath(request);

    switch (action) {
      case "newSite":
        currentPath = addNewDesignedSite(controller, request);
        break;
      case "updateDescription":
        // type 0 design ou 2 upload
        updateDesignedSite(controller, request, currentPath);
        break;
      case "addFolder":
        addFolderInSite(controller, request, currentPath);
        break;
      case "renameFolder":
        renameFolderInSite(controller, request, currentPath);
        break;
      case "deleteFolder":
        deleteFolderInSite(controller, request, currentPath);
        break;
      case "addPage":
        addPageInSite(controller, request, currentPath);
        break;
      case "renamePage":
        renamePageInSite(controller, request, currentPath);
        break;
      case "deletePage":
        deletePageInSite(controller, request, currentPath);
        break;
      case "classifySite":
        classifySite(controller, request, id);
        break;
      case "design":
        setSiteToDesign(controller, request, id);
        break;
      default:
        id = backToDesignedSite(controller, request);
        break;
    }

    return "/webSites/jsp/design.jsp?Action=design&Path=" + currentPath + "&Id=" + id;
  }

  private String getCurrentPath(final HttpRequest request) {
    String currentPath = request.getParameter(WebSitesRequestRouter.PATH_PARAM);
    if (currentPath != null) {
      currentPath = StringUtil.doubleAntiSlash(currentPath);
    }
    return  currentPath;
  }

  private String backToDesignedSite(final WebSiteSessionController controller,
      final HttpRequest request) {
    SiteDetail site = controller.getSessionSite();
    request.setAttribute("Site", site);
    return site.getPK().getId();
  }

  private void setSiteToDesign(final WebSiteSessionController controller, final HttpRequest request,
      final String id) throws WebSitesException {
    // DESIGN -------------------------------------------------------------
    SiteDetail site = controller.getWebSite(id);
    controller.setSessionSite(site);
    request.setAttribute("Site", site);
  }

  private void classifySite(final WebSiteSessionController controller, final HttpRequest request,
      final String id) throws WebSitesException {
    // CLASSIFY SITE -------------------------------------------------------------
    // = en cas de new Site ou de classifySite
    String listeTopics = request.getParameter(WebSitesRequestRouter.TOPIC_LIST_PARAM);

    request.setAttribute("Site", controller.getSessionSite());

    /* publications : classer le site dans les themes cochés */
    ArrayList<String> arrayToClassify = new ArrayList<>();
    boolean publish = false;

    ArrayList<String> arrayTopic = new ArrayList<>();
    int begin = 0;
    int end = listeTopics.indexOf(',', begin);
    String idTopic;
    while (end != -1) {
      idTopic = listeTopics.substring(begin, end);

      begin = end + 1;
      end = listeTopics.indexOf(',', begin);

      arrayTopic.add(idTopic);
      publish = true;
    }
    controller.updateClassification(id, arrayTopic);

    arrayToClassify.add(id);
    if (publish) {
      controller.publish(arrayToClassify);
    } else {
      controller.dePublish(arrayToClassify);
    }
  }

  private void deletePageInSite(final WebSiteSessionController controller, final HttpRequest request,
      final String currentPath) throws WebSitesException {
    // DELETE PAGE -------------------------------------------------------------
    // = null la premiere fois, puis = nom du repertoire courant
    String name = request.getParameter("name");

     /* Supprimer la page */
    controller.deleteFile(currentPath + "/" + name);

    request.setAttribute("Site", controller.getSessionSite());
  }

  private void renamePageInSite(final WebSiteSessionController controller, final HttpRequest request,
      final String currentPath) throws WebSitesException {
    // RENAME PAGE -------------------------------------------------------------
    // = null la premiere fois, puis = nom du repertoire courant
    String name = request.getParameter("name");
    // = changement de noms des fichiers et repertoires
    String newName = request.getParameter("newName");

          /* Modifier le nom du fichier */
    controller.renameFile(currentPath, name, newName);

    request.setAttribute("Site", controller.getSessionSite());
  }

  private void addPageInSite(final WebSiteSessionController controller, final HttpRequest request,
      final String currentPath) throws WebSitesException {
    // = rempli la premiere fois a la creation, puis toujours null
    String nomPage = request.getParameter(WebSitesRequestRouter.PAGE_NAME_PARAM);

    // ADD PAGE -------------------------------------------------------------
    // = code de la page a parser
    String code = request.getParameter("Code");

    code = WebEncodeHelper.htmlStringToJavaString(code);
  /*
   * enleve les http :// localhost :8000/ WAwebSiteUploads / WA0webSite17 /18/ et on garde
   * seulement rep /icon .gif
   */
    String newCode = parseCodeSupprImage(controller, code, request, currentPath);
    /*
     * enleve les http://localhost :8000 /webactiv/RwebSite /jsp/ et on garde seulement
     * rep/page.html
     */
    newCode = parseCodeSupprHref(controller, newCode, currentPath);

    // Creer une nouvelle page
    controller.createFile(currentPath, nomPage, newCode);

    request.setAttribute("Site", controller.getSessionSite());
  }

  private void deleteFolderInSite(final WebSiteSessionController controller, final HttpRequest request,
      final String currentPath) throws WebSitesException {
    // null la premiere fois, puis = nom du repertoire courant
    String name = request.getParameter("name");

    // DELETE FOLDER -------------------------------------------------------------

          /* Supprimer le repertoire */
    controller.delFolder(currentPath + "/" + name);

    request.setAttribute("Site", controller.getSessionSite());
  }

  private void renameFolderInSite(final WebSiteSessionController controller, final HttpRequest request,
      final String currentPath) throws WebSitesException {
    // = null la premiere fois, puis = nom du repertoire courant
    String name = request.getParameter("name");
    // = changement de noms des fichiers et repertoires
    String newName = request.getParameter("newName");

    // RENAME FOLDER -------------------------------------------------------------

          /* Modifier le nom du repertoire */
    controller.renameFolder(currentPath + "/" + name, currentPath + "/" + newName);

    request.setAttribute("Site", controller.getSessionSite());
  }

  private void addFolderInSite(final WebSiteSessionController controller, final HttpRequest request,
      final String currentPath) throws WebSitesException {
    // = null la premiere fois, puis = nom du repertoire courant
    String name = request.getParameter("name");

    // ADD FOLDER -------------------------------------------------------------
          /* Creer le nouveau repertoire */
    controller.createFolder(currentPath + "/" + name);

    request.setAttribute("Site", controller.getSessionSite());

  }

  private void updateDesignedSite(final WebSiteSessionController controller, final HttpRequest request,
      String currentPath) throws WebSitesException {
    // = rempli au premier acces a designSite pui toujours null
    String nomSite = request.getParameter(WebSitesRequestRouter.SITE_NAME_PARAM);

    // = rempli la premiere fois a la creation, puis toujours null
    String description = request.getParameter(WebSitesRequestRouter.DESCRIPTION_PARAM);

    // = rempli la premiere fois a la creation, puis toujours null
    String nomPage = request.getParameter(WebSitesRequestRouter.PAGE_NAME_PARAM);
    String tempPopup = request.getParameter(WebSitesRequestRouter.POPUP_PARAM);
    String etat = request.getParameter("etat");
    // = rempli la premiere fois a la creation, puis toujours null
    String listeIcones = request.getParameter(WebSitesRequestRouter.ICON_LIST_PARAM);

    int popup = (tempPopup != null) && (tempPopup.length() > 0) ? 1 : 0;
    ArrayList<String> listIcons = new ArrayList<>();
    if (listeIcones != null) {
      for (String icon : listeIcones.split(",")) {
        if (!icon.isEmpty()) {
          listIcons.add(icon);
        }
      }
    }

    SiteDetail ancien = controller.getSessionSite();
    String id = ancien.getSitePK().getId();
    int type = ancien.getSiteType();

          /* verif que le nom de la page principale est correcte */
    boolean searchOk = false;
    Collection<File> collPages = controller.getAllHTMLWebPages(currentPath);
    for (File f : collPages) {
      if (f.getName().equals(nomPage)) {
        searchOk = true;
        break;
      }
    }

    SiteDetail descriptionSite2 =
        new SiteDetail(id, controller.getComponentId(), nomSite, description, nomPage, type, null, null,
            Integer.parseInt(etat), popup);

    if (searchOk) {
            /* update description en BD */
      controller.updateWebSite(descriptionSite2);

      if (!listIcons.isEmpty()) {
        controller.associateIcons(id, listIcons);
      }
    } else {
      request.setAttribute("SearchOK", Boolean.FALSE);
      request.setAttribute(WebSitesRequestRouter.ICON_LIST_PARAM, listeIcones);
    }

    descriptionSite2 = controller.getWebSite(id);
    controller.setSessionSite(descriptionSite2);
    request.setAttribute("Site", descriptionSite2);
  }

  private String addNewDesignedSite(final WebSiteSessionController controller, final HttpRequest request)
      throws WebSitesException {
    // Filled at first access then null
    String nomSite = request.getParameter(WebSitesRequestRouter.SITE_NAME_PARAM);
    // Filled at first access then null
    String description = request.getParameter(WebSitesRequestRouter.DESCRIPTION_PARAM);
    // Filled at first access then null
    String nomPage = request.getParameter(WebSitesRequestRouter.PAGE_NAME_PARAM);
    String tempPopup = request.getParameter(WebSitesRequestRouter.POPUP_PARAM);
    // Filled at first creation then null
    String listeIcones = request.getParameter(WebSitesRequestRouter.ICON_LIST_PARAM);
    // = en cas de new Site ou de classifySite
    String listeTopics = request.getParameter(WebSitesRequestRouter.TOPIC_LIST_PARAM);
    // Retrieve positions
    String positions = request.getParameter("Positions");

    int popup = 0;
    if ((tempPopup != null) && (tempPopup.length() > 0)) {
      popup = 1;
    }

    ArrayList<String> listIcons = new ArrayList<>();
    if (listeIcones != null) {
      for (String icon : listeIcones.split(",")) {
        if (!icon.isEmpty()) {
          listIcons.add(icon);
        }
      }
    }
          /* recuperation de l'id */
    String id = controller.getNextId();
          /* Creer le repertoire id */
    controller.createFolder(controller.getWebSitePathById(id));

    // Persist siteDetail inside database type 0 = site cree
    SiteDetail descriptionSite =
        new SiteDetail(id, controller.getComponentId(), nomSite, description, nomPage, 0, null, null, 0,
            popup);
    descriptionSite.setPositions(positions);

    String pubId = controller.createWebSite(descriptionSite);
    descriptionSite = controller.getWebSite(id);
    controller.setSessionSite(descriptionSite);

    if (!listIcons.isEmpty()) {
      controller.associateIcons(id, listIcons);
    }

    String currentPath = controller.getWebSitePathById(id);

          /* ajout de la page principale */
    String code = " ";

          /* Creer la page principale */
    controller.createFile(currentPath, nomPage, code);

          /* publications : classer le site dans les themes cochés */
    ArrayList<String> arrayToClassify = new ArrayList<>();
    boolean publish = false;
    for (String idTopic : listeTopics.split(",")) {
      if (!idTopic.isEmpty()) {
        // ajout de la publication dans le theme
        controller.addPublicationToFolder(pubId, idTopic);
        publish = true;
      }
    }

    if (publish) {
      arrayToClassify.add(id);
      controller.publish(arrayToClassify);
    }

    request.setAttribute("Site", descriptionSite);
    return currentPath;
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

  public String removeDoubleAntiSlash(String chemin) {
    /* ex : id\\rep1\\rep11\\rep111 */
    /* res = id\rep1\rep11\re111 */
    return chemin.replace("\\\\", "\\");
  }

  public String finNode(WebSiteSessionController scc, String path) {
    /* ex : ....webSite17\\id\\rep1\\rep2\\rep3 */
    /* res : id\rep1\rep2\rep3 */

    int longueur = scc.getComponentId().length();
    int index = path.lastIndexOf(scc.getComponentId());
    String chemin = path.substring(index + longueur);

    chemin = ignoreAntiSlash(chemin);
    chemin = removeDoubleAntiSlash(chemin);

    return chemin;
  }

  private List<String> sortCommun(List<String> tabContexte, List<String> tab) {
    /* tabContexte = [id | rep1 | rep2] */
    /* tab = [id | rep1 | rep3] */
    /* res = [id | rep1] */
    int i = 0;
    boolean ok = true;
    List<String> array = new ArrayList<>();

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
    StringBuilder res = new StringBuilder();
    for (int i = tabCommun.size(); i < tab.size(); i++) {
      res.append(tab.get(i)).append("/");
    }

    if (res.length() > 0) {
      res = res.deleteCharAt(res.length() - 1);
    }

    return res.toString();
  }

  private String parseCodeSupprImage(WebSiteSessionController scc, String code,
      HttpServletRequest request, String currentPath) {
    String avant;
    String apres;
    int index;
    String finChemin;
    final String componentURL = getComponentURL(request, scc.getComponentId()) + "/";
    String image = "<IMG border=0 src=\"" + componentURL;
    int longueurImage = WEBSITE_BASE_URL_LENGTH + componentURL.length();
    index = code.indexOf(image);
    if (index == -1) {
      return code;
    } else {
      avant = code.substring(0, index + WEBSITE_BASE_URL_LENGTH);
      finChemin = code.substring(index + longueurImage);

      int indexGuillemet = finChemin.indexOf("\"");
      String absolute = finChemin.substring(0, indexGuillemet);

      apres = finChemin.substring(indexGuillemet);
      int indexSlash = absolute.lastIndexOf("/");
      String fichier = absolute.substring(indexSlash + 1);

      String deb = absolute.substring(0, indexSlash);
      List<String> tab = buildTab(deb + "/");

      /* id/rep1 */
      String cheminContexte = finNode(scc, currentPath);
      List<String> tabContexte = buildTab(cheminContexte + "/");
      apres = computeApres(apres, tabContexte, tab, fichier);
      return avant + parseCodeSupprImage(scc, apres, request, currentPath);
    }
  }

  private String computeApres(String apres, List<String> tabContexte, List<String> tab,
      String fichier) {
    List<String> tabCommun = sortCommun(tabContexte, tab);
    String reste = sortReste(tab, tabCommun);
    int nbPas = tabContexte.size() - tabCommun.size();
    StringBuilder relatif = new StringBuilder();
    for (int i = 0; i < nbPas; i++) {
      relatif.append("../");
    }

    if (!reste.isEmpty()) {
      relatif.append(reste).append("/");
    }
    relatif.append(fichier);
    return relatif.toString() + apres;
  }

  private String parseCodeSupprHref(WebSiteSessionController scc, String code, String currentPath) {
    String avant;
    String apres;
    int index;
    String href = "<A href=\"";
    int hrefLength = href.length();

    String finChemin;
    String fichier;
    String deb;
    String theReturn = "";

    index = code.indexOf(href);
    if (index == -1) {
      theReturn = code;
    } else {
      final int customSchemaLength = 3;
      avant = code.substring(0, index + hrefLength);
      apres = code.substring(index + hrefLength);

      if (apres.startsWith("http://")) {
        /* external link */
        theReturn = avant + parseCodeSupprHref(scc, apres, currentPath);
      } else if (apres.startsWith("ftp://")) {
        /* external link */
        theReturn = avant + parseCodeSupprHref(scc, apres, currentPath);
      } else if (apres.startsWith("rr:")) {
        /* already in a relative form */
        apres = apres.substring(customSchemaLength);
        theReturn = avant + parseCodeSupprHref(scc, apres, currentPath);
      } else if (apres.startsWith("aa:")) {
        /* the link is in an absolute form: convert it into the relative form */
        /* finChemin = rep/coucou.html">... */
        finChemin = code.substring(index + hrefLength + customSchemaLength);
        int indexGuillemet = finChemin.indexOf("\"");
        /* absolute = rep/coucou.html */
        String absolute = finChemin.substring(0, indexGuillemet);
        /* apres = ">... */
        apres = finChemin.substring(indexGuillemet);
        int indexSlash = absolute.lastIndexOf("\\");
        if (indexSlash == -1) {
          // pas d'arborescence, le fichier du lien est sur la racine
          fichier = absolute;
          deb = "";
        } else {
          /* fichier = coucou.html */
          fichier = absolute.substring(indexSlash + 1);
          deb = absolute.substring(0, indexSlash);
        }
        List<String> tab = buildTab(deb + "/");
        // dans ce tableau il manque l'id

        /* cheminContexte = id/rep */
        int longueur = scc.getComponentId().length();
        int index2 = currentPath.lastIndexOf(scc.getComponentId());
        String chemin = currentPath.substring(index2 + longueur);

        chemin = chemin.substring(1);
        chemin = removeDoubleAntiSlash(chemin);
        String cheminContexte = chemin;
        List<String> tabContexte = buildTab(cheminContexte + "/");
        /* ajoute l'id dans le premier tableau */
        tab.add(0, tabContexte.get(0));

        apres = computeApres(apres, tabContexte, tab, fichier);
        theReturn = avant + parseCodeSupprHref(scc, apres, currentPath);
      }
    }
    return theReturn;
  }
}
