/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.stratelia.webactiv.webSites.control;

/**
 * This is the webSite manager main interface It contains all of the methods to be accessible to the
 * client
 *
 * @author Cécile BONIN
 */
import com.silverpeas.pdc.PdcServiceFactory;
import com.silverpeas.pdc.model.PdcClassification;
import com.silverpeas.pdc.model.PdcPosition;
import com.silverpeas.pdc.service.PdcClassificationService;
import com.silverpeas.pdc.web.PdcClassificationEntity;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationParameters;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.notificationManager.UserRecipient;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.webSites.control.ejb.WebSiteBm;
import com.stratelia.webactiv.webSites.siteManage.model.FolderDetail;
import com.stratelia.webactiv.webSites.siteManage.model.IconDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;
import com.stratelia.webactiv.webSites.siteManage.util.Expand;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJBException;
import javax.xml.bind.JAXBException;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;

import static com.silverpeas.pdc.model.PdcClassification.aPdcClassificationOfContent;

public class WebSiteSessionController extends AbstractComponentSessionController {

  private WebSiteBm webSiteEjb = null;
  // Session objects
  private FolderDetail sessionTopic = null;
  private NotificationSender notifSender = null;
  private SiteDetail sessionSite = null;
  private String siteName;
  public final static String TAB_PDC = "tabPdc";
  private static final String WEBSITE_REPO_PROPERTY = "uploadsPath";
  private static final String WEBSITE_WHITE_LIST = "whiteList";

  public WebSiteSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.webSites.multilang.webSiteBundle", null,
        "org.silverpeas.webSites.settings.webSiteSettings");
    initEJB();
  }

  protected String getWebSiteRepositoryPath() {
    String path = getSettings().getString(WEBSITE_REPO_PROPERTY);
    if (!path.endsWith("/") && !path.endsWith("\\")) {
      path += "/";
    }
    return path;
  }

  /**
   * initEJB
   */
  private void initEJB() {
    // 1 - Remove all data store by this SessionController (includes EJB)
    webSiteEjb = null;
    removeSessionTopic();

    removeSessionSite();

    // 2 - Init EJB used by this SessionController
    try {
      setWebSiteEJB();
    } catch (Exception e) {
      throw new EJBException(e);
    }
  }

  public boolean isBookmarkMode() {
    return ("bookmark".equals(getComponentRootName()));
  }

  public boolean isSortedTopicsEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("sortedTopics"));
  }

  /**
   * ***********************************************************************************
   */
  /* WebSite - Gestion des objets session */
  /**
   * ***********************************************************************************
   */
  public synchronized void setSessionTopic(FolderDetail topicDetail) {
    this.sessionTopic = topicDetail;
  }

  public synchronized void setSessionSite(SiteDetail siteDetail) {
    this.sessionSite = siteDetail;
  }

  public synchronized FolderDetail getSessionTopic() {
    return this.sessionTopic;
  }

  public synchronized SiteDetail getSessionSite() {
    return this.sessionSite;
  }

  public synchronized void removeSessionTopic() {
    setSessionTopic(null);
  }

  public synchronized void removeSessionSite() {
    setSessionSite(null);
  }

  public NotificationSender getNotificationSender() {
    if (notifSender == null) {
      notifSender = new NotificationSender(getComponentId());
    }
    return notifSender;
  }

  /**
   * setWebSiteEJB
   */
  private synchronized void setWebSiteEJB() throws WebSitesException {
    if (webSiteEjb == null) {
      try {
        webSiteEjb = EJBUtilitaire.getEJBObjectRef(JNDINames.WEBSITESBM_EJBHOME,
            WebSiteBm.class);
      } catch (Exception e) {
        throw new WebSitesException("WebSiteSessionController.setWebSiteEJB()",
            SilverpeasException.ERROR, "webSites.EX_EJB_CREATION_FAIL", e);
      }
    }
  }

  /**
   * getWebSiteEJB
   */
  public synchronized WebSiteBm getWebSiteEJB() {
    return webSiteEjb;
  }

  public boolean isPdcUsed() {
    String parameterValue = getComponentParameterValue("usepdc");
    if (parameterValue == null || parameterValue.length() <= 0) {
      return false;
    } else {
      return StringUtil.getBooleanValue(parameterValue);
    }
  }

  public synchronized FolderDetail getFolder(String id) throws WebSitesException {
    try {
      return webSiteEjb.goTo(new NodePK(id, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getFolder()",
          SilverpeasException.ERROR, "webSites.EX_GET_FOLDER_FAILED", "id = " + id, re);
    }
  }

  public synchronized NodePK updateFolderHeader(NodeDetail nd, String alertType)
      throws WebSitesException {
    try {
      return webSiteEjb.updateFolder(nd, sessionTopic.getNodePK());
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.updateFolderHeader()",
          SilverpeasException.ERROR, "webSites.EX_UPDATE_FOLDER_HEADER_FAILED", re);
    }
  }

  public synchronized NodeDetail getFolderDetail(String id)
      throws WebSitesException {
    try {
      return webSiteEjb.getFolderDetail(new NodePK(id, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getFolderDetail()",
          SilverpeasException.ERROR, "webSites.EX_GET_FOLDER_DETAIL_FAILED", "id = " + id, re);
    }
  }

  public synchronized NodePK addFolder(NodeDetail nd, String alertType)
      throws WebSitesException {
    try {
      return webSiteEjb.addFolder(nd, sessionTopic.getNodePK(), getUserDetail());
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.addFolder()",
          SilverpeasException.ERROR, "webSites.EX_ADD_FOLDER_FAILED", re);
    }
  }

  public synchronized void deleteFolder(String id) throws WebSitesException {
    try {
      webSiteEjb.deleteFolder(new NodePK(id, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.deleteFolder()",
          SilverpeasException.ERROR, "webSites.EX_DELETE_FOLDER_FAILED", "id = " + id, re);
    }
  }

  /**
   * @param way
   * @param topicId
   * @throws RemoteException
   */
  public synchronized void changeTopicsOrder(String way, String topicId)
      throws RemoteException {
    NodePK nodePK = new NodePK(topicId, getSpaceId(), getComponentId());
    webSiteEjb.changeTopicsOrder(way, nodePK, getSessionTopic().getNodePK());
  }

  /* ** gestion, des publi ** */
  public synchronized PublicationDetail getPublicationDetail(String pubId)
      throws WebSitesException {
    try {
      return webSiteEjb.getPublicationDetail(new PublicationPK(pubId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getPublicationDetail()",
          SilverpeasException.ERROR, "root.EX_GET_PUBLICATION_FAILED", "pubId = " + pubId, re);
    }
  }

  public synchronized void deletePublication(String pubId)
      throws WebSitesException {
    try {
      webSiteEjb.deletePublication(new PublicationPK(pubId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.deletePublication()",
          SilverpeasException.ERROR, "root.EX_DELETE_PUBLICATION_FAILED", "pubId = " + pubId, re);
    }
  }

  public synchronized Collection<NodePK> getAllFatherPK(String pubId)
      throws WebSitesException {
    try {
      return webSiteEjb.getAllFatherPK(new PublicationPK(pubId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getPublicationFather()",
          SilverpeasException.ERROR, "webSites.EX_GET_PUBLICATION_FATHER_FAILED", "pubId = " + pubId,
          re);
    }
  }

  public synchronized void addPublicationToFolder(String pubId, String folderId)
      throws WebSitesException {
    try {
      webSiteEjb.addPublicationToTopic(new PublicationPK(pubId, getComponentId()), new NodePK(
          folderId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.addPublicationToFolder()",
          SilverpeasException.ERROR, "webSites.EX_PUBLICATION_ADD_TO_NODE_FAILED", "pubId = "
          + pubId + ", folderId = " + folderId, re);
    }
  }

  public synchronized void removePublicationToFolder(String pubId, String folderId) throws
      WebSitesException {
    try {
      webSiteEjb.removePublicationFromTopic(new PublicationPK(pubId, getComponentId()),
          new NodePK(folderId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.removePublicationToFolder()",
          SilverpeasException.ERROR, "webSites.EX_PUBLICATION_DELETE_TO_NODE_FAILED", "pubId = "
          + pubId + ", folderId = " + folderId, re);
    }
  }

  /**
   * @param siteId
   * @return
   * @throws WebSitesException
   */
  public synchronized String getIdPublication(String siteId)
      throws WebSitesException {
    try {
      return webSiteEjb.getIdPublication(getComponentId(), siteId);
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getIdPublication()",
          SilverpeasException.ERROR, "webSites.EX_GET_PUBLICATION_FAILED",
          "siteId =" + siteId, re);
    }
  }

  /**
   * @param pubId
   * @param direction
   * @throws RemoteException
   */
  public synchronized void changePubsOrder(String pubId, int direction)
      throws RemoteException {
    webSiteEjb.changePubsOrder(new PublicationPK(pubId, getComponentId()), getSessionTopic().
        getNodePK(), direction);
  }

  /* ** Gestion des sites ** */
  /**
   * setSiteName
   */
  public synchronized void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  /**
   * getSiteName
   */
  public synchronized String getSiteName() {
    return siteName;
  }

  /**
   * getAllWebSite
   */
  public synchronized Collection<SiteDetail> getAllWebSite() throws WebSitesException {
    try {
      return webSiteEjb.getAllWebSite(getComponentId());
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getAllWebSite()",
          SilverpeasException.ERROR, "webSites.EX_GET_ALL_WEBSITES_FAILED", "", re);
    }
  }

  /**
   * getWebSite
   */
  public synchronized SiteDetail getWebSite(String id) throws WebSitesException {
    try {
      return webSiteEjb.getWebSite(getComponentId(), id);
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getWebSite",
          SilverpeasException.ERROR, "webSites.EX_GET_WEBSITE_FAILED", "siteId =" + id, re);
    }
  }

  /**
   * getIcons
   */
  public synchronized Collection<IconDetail> getIcons(String id) throws WebSitesException {
    try {
      return webSiteEjb.getIcons(getComponentId(), id);
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getIcons(id)",
          SilverpeasException.ERROR, "webSites.EX_GET_ICONS_FAILED", "siteId ="
          + id, re);
    }
  }

  /**
   * getNextId
   */
  public synchronized String getNextId() throws WebSitesException {
    try {
      return webSiteEjb.getNextId(getComponentId());
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getNextId()",
          SilverpeasException.ERROR, "root.EX_GET_NEXTID_FAILED", "", re);
    }
  }

  /**
   * getAllIcons
   */
  public synchronized Collection<IconDetail> getAllIcons() throws WebSitesException {
    try {
      return webSiteEjb.getAllIcons(getComponentId());
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.getAllIcons()",
          SilverpeasException.ERROR, "webSites.EX_GET_ALL_ICONS_FAILED", "", re);
    }
  }

  /**
   * getAllSubFolder
   */
  public synchronized Collection<File> getAllSubFolder(String chemin) throws WebSitesException {
    try {
      String fullPath = getFullPath(chemin);
      return FileFolderManager.getAllSubFolder(fullPath);
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.getAllSubFolder()",
          SilverpeasException.ERROR, "webSites.EX_GET_ALL_SUB_FOLDERS_FAIL", e);
    }
  }

  /**
   * getAllFile
   */
  public synchronized Collection<File> getAllFile(String chemin) throws WebSitesException {
    try {
      return FileFolderManager.getAllFile(getFullPath(chemin));
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.getAllFile()",
          SilverpeasException.ERROR, "webSites.EX_GET_ALL_FILES_FAIL", e);
    }
  }

  /**
   * getAllImages
   */
  public synchronized Collection<File> getAllImages(String chemin) throws WebSitesException {
    try {
      return FileFolderManager.getAllImages(getFullPath(chemin));
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.getAllImages()",
          SilverpeasException.ERROR, "webSites.EX_GET_ALL_IMAGES_FAIL", e);
    }
  }

  /**
   * getAllWebPages2
   */
  public synchronized Collection<File> getAllWebPages2(String chemin) throws WebSitesException {
    try {
      return FileFolderManager.getAllWebPages2(getFullPath(chemin));
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.getAllWebPages2()",
          SilverpeasException.ERROR, "webSites.EX_GET_ALL_WEB_PAGES_FAIL", e);
    }
  }

  /**
   * createWebSite
   */
  public synchronized String createWebSite(SiteDetail description)
      throws WebSitesException {
    try {
      description.setCreatorId(getUserId());
      description.setCreationDate(new Date());
      String pubPK = webSiteEjb.createWebSite(getComponentId(), description, getUserDetail());
      classifyWebSites(description);
      return pubPK;
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.createWebSite(description)",
          SilverpeasException.ERROR, "webSites.EX_CREATE_WEBSITE_FAILED",
          "siteDetail =" + description.toString(), re);
    }
  }

  /**
   * @param siteDetail
   */
  private void classifyWebSites(SiteDetail siteDetail) {
    String positions = siteDetail.getPositions();
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity pdcClassif = null;
      try {
        pdcClassif = PdcClassificationEntity.fromJSON(positions);
      } catch (JAXBException e) {
        SilverTrace.error("quickInfo", "QuickInfoSessionController.classifyQuickInfo",
            "PdcClassificationEntity error", "Problem to read JSON", e);
      }
      if (pdcClassif != null && !pdcClassif.isUndefined()) {
        List<PdcPosition> pdcPositions = pdcClassif.getPdcPositions();
        String siteId = siteDetail.getId();
        PdcClassification classification = aPdcClassificationOfContent(siteId, getComponentId())
            .withPositions(pdcPositions);
        if (!classification.isEmpty()) {
          PdcClassificationService service = PdcServiceFactory.getFactory().
              getPdcClassificationService();
          classification.ofContent(siteId);
          service.classifyContent(siteDetail, classification);
        }
      }
    }
  }

  /**
   * AssociateIcons
   */
  public synchronized void associateIcons(String id, Collection<String> listeIcones)
      throws WebSitesException {
    try {
      webSiteEjb.associateIcons(getComponentId(), id, listeIcones);
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.associateIcons(id, listeIcones)",
          SilverpeasException.ERROR, "webSites.EX_ASSOCIATE_ICONS_FAILED", "siteId =" + id, re);
    }
  }

  /**
   * publish
   */
  public synchronized void publish(Collection<String> listeSite) throws WebSitesException {
    /* Collection d'id de site */
    try {
      webSiteEjb.publish(getComponentId(), listeSite);
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.publish(listeSite)",
          SilverpeasException.ERROR, "webSites.EX_PUBLISH_SELECTED_FAILED",
          "listeSite =" + listeSite.toString(), re);
    }
  }

  /**
   * dePublish
   */
  public synchronized void dePublish(Collection<String> listeSite) throws WebSitesException {
    try {
      webSiteEjb.dePublish(getComponentId(), listeSite);
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.dePublish(listeSite)",
          SilverpeasException.ERROR, "webSites.EX_DEPUBLISH_SELECTED_FAILED",
          "listeSite =" + listeSite.toString(), re);
    }
  }

  /**
   * createFolder
   */
  public synchronized void createFolder(String chemin) throws WebSitesException {
    try {
      FileFolderManager.createFolder(getFullPath(chemin));
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.createFolder()",
          SilverpeasException.ERROR, "webSites.EX_CREATE_FOLDER_FAIL", e);
    }
  }

  public String getWebSitePathById(String id) {
    return getComponentId() + "/" + id;
  }

  /**
   * renameFolder
   */
  public synchronized void renameFolder(String cheminRep, String newCheminRep) throws
      WebSitesException {
    try {
      FileFolderManager.renameFolder(getFullPath(cheminRep), getFullPath(newCheminRep));
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.renameFolder()",
          SilverpeasException.ERROR, "webSites.EX_RENAME_FOLDER_FAIL", e);
    }
  }

  /**
   * delFolder
   */
  public synchronized void delFolder(String chemin) throws WebSitesException {
    try {
      FileFolderManager.deleteFolder(getFullPath(chemin));
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.delFolder()",
          SilverpeasException.ERROR, "webSites.EX_DELETE_FOLDER_FAIL", e);
    }
  }

  /**
   * createPage
   */
  public synchronized void createFile(String cheminFichier, String nomFichier,
      String contenuFichier) throws WebSitesException {
    String nouveauNom = nomFichier;
    try {
      FileFolderManager.createFile(getFullPath(cheminFichier), nouveauNom,
          contenuFichier);
    } catch (UtilException e) {
      throw new WebSitesException("WebSiteSessionController.createFile()",
          SilverpeasException.ERROR, "webSites.EX_CREATE_FILE_FAIL", e);
    }
  }

  /**
   * unzip
   */
  public synchronized void unzip(String cheminDirResultat, String cheminFichierZip) throws
      WebSitesException {
    SilverTrace.debug("webSites", "WebSiteSessionController.unzip",
        "root.MSG_GEN_ENTER_METHOD", "cheminDirResultat = " + cheminDirResultat
        + ", cheminFichierZip = " + cheminFichierZip);
    /*
     * cheminDirResultat = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite
     */
    /*
     * cheminFichierZip = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\toto.zip
     */
    try {
      Expand exp = new Expand();
      File zip = new File(cheminFichierZip);
      File dest = new File(cheminDirResultat);
      exp.setSrc(zip);
      exp.setDest(dest);
      exp.execute();
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.unzip()",
          SilverpeasException.ERROR, "webSites.EX_UNZIP_FILE_FAIL", e);
    }
  }

  /**
   * deleteWebSites
   */
  public synchronized void deleteWebSites(Collection<String> liste) throws WebSitesException {
    try {
      webSiteEjb.deleteWebSites(getComponentId(), liste);
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.deleteWebSites(liste)",
          SilverpeasException.ERROR, "webSites.EX_DELETE_WEBSITES_FAILED", "listeSite =" + liste, re);
    }
  }

  /**
   * @param description
   * @throws WebSitesException
   */
  public synchronized void updateWebSite(SiteDetail description) throws WebSitesException {
    try {
      description.setCreatorId(getUserId());
      description.setCreationDate(new Date());

      webSiteEjb.updateWebSite(getComponentId(), description);
      String pubId = webSiteEjb.getIdPublication(getComponentId(), description.getSitePK().getId());

      PublicationPK pubPk = new PublicationPK(pubId, getSpaceId(), getComponentId());
      description.setPk(pubPk);
      webSiteEjb.updatePublication(description, getComponentId());
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.updateWebSite(description)",
          SilverpeasException.ERROR, "webSites.EX_UPDATE_WEBSITE_FAILED", "siteDetail ="
          + description, re);
    }
  }

  /**
   * deleteDirectory
   */
  public synchronized void deleteDirectory(String chemin) throws WebSitesException {
    boolean result;
    File directory = new File(getFullPath(chemin));
    /* recupere la liste des fichiers et directory du chemin */
    try {
      File[] dirFiles = directory.listFiles();

      for (int i = 0; i < dirFiles.length; i++) {
        delDir(dirFiles[i]);
      }
    } catch (Exception e) {
      SilverTrace.warn("webSites", "WebSiteSessionController.deleteDirectory()",
          "webSites.EXE_LIST_FILES_FAIL", "path = " + chemin, e);
    }
    result = directory.delete();
    if (!result) {
      throw new WebSitesException("WebSiteSessionClientController.deleteDirectory()",
          SilverpeasException.ERROR, "webSites.EXE_DELETE_DIRECTORY_FAIL", "path = " + chemin);
    }

  }

  /**
   * delDir : procedure privee recursive
   */
  private synchronized void delDir(File dir) {
    try {
      if (dir.isDirectory()) {
        File[] dirFiles = dir.listFiles();
        for (int i = 0; i < dirFiles.length; i++) {
          delDir(dirFiles[i]);
        }
      }
      dir.delete();
    } catch (Exception e) {
      SilverTrace.warn("webSites", "WebSiteSessionController.delDir()",
          "webSites.EXE_DELETE_DIRECTORY_FAIL", "path = " + dir.getPath(), e);
    }
  }

  /**
   * renameFile
   */
  public synchronized void renameFile(String rep, String name, String newName)
      throws WebSitesException {
    /* chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder */
    try {
      FileFolderManager.renameFile(getFullPath(rep), name, newName);
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.renameFile()",
          SilverpeasException.ERROR, "webSites.EX_RENAME_FILE_FAIL", e);
    }
  }

  /**
   * deleteFile
   */
  public synchronized void deleteFile(String chemin) throws WebSitesException {
    try {
      FileFolderManager.deleteFile(getFullPath(chemin));
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.deleteFile()",
          SilverpeasException.ERROR, "webSites.EX_DELETE_FILE_FAIL", e);
    }
  }

  /**
   * getCode
   */
  public synchronized String getCode(String cheminFichier, String nomFichier) throws
      WebSitesException {

    try {
      return FileFolderManager.getCode(getFullPath(cheminFichier), nomFichier);
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.getCode()",
          SilverpeasException.ERROR, "webSites.EX_GET_CODE_FAIL", e);
    }
  }

  /**
   * verif
   *
   * @param action
   * @param currentPath
   * @param name
   * @param newName
   * @param nomPage
   * @return
   * @throws WebSitesException
   */
  public synchronized String verif(String action, String currentPath,
      String name, String newName, String nomPage) throws WebSitesException {
    String res = "";
    String fullPath = getFullPath(currentPath);
    try {
      if (action.equals("addFolder")) { // create a folder
        File folder = new File(fullPath, name);
        if (folder.exists()) {
          res = "pbAjoutFolder";
        } else {
          res = "ok";
        }
      } else if (action.equals("renameFolder")) { // Rename current folder
        File folder = new File(fullPath, newName);
        if (folder.exists()) {
          res = "pbRenommageFolder";
        } else {
          res = "ok";
        }
      } else if (action.equals("addPage")) { // create a file
        File fichier = new File(fullPath, nomPage);
        if (fichier.exists()) {
          res = "pbAjoutFile";
        } else {
          res = "ok";
        }
      } else if (action.equals("renamePage")) { // rename a file
        // fichier
        File fichier = new File(fullPath, newName);
        if (fichier.exists()) {
          res = "pbRenommageFile";
        } else {
          res = "ok";
        }
      } else {
        res = "ok";
      }
    } catch (Exception e) {
      throw new WebSitesException("WebSiteSessionController.verif()",
          SilverpeasException.ERROR, "webSites.EX_VERIF_FAIL", e);
    }
    return res;
  }

  /**
   * index web sites component
   *
   * @throws RemoteException
   */
  public void index() throws RemoteException {
    getWebSiteEJB().index(getComponentId());
  }

  /**
   * notifyPublishers
   */
  public void notifyPublishers(String auteur, String nomSite,
      String description, String nomPage, String listeMessage, String date) {
    String subject = getString("SuggestionLink");
    String messageText = auteur + " " + getString("PropositionLien")
        + "  \n \n" + getString("VoiciDescriptionLien") + "\n \n"
        + getString("GML.name") + " : " + nomSite + "\n"
        + getString("GML.description") + " : " + description + "\n"
        + getString("URL") + " : " + nomPage + "\n" + getString("ListeIcones")
        + " : \n" + listeMessage + "\n " + getString("GML.creationDate")
        + " : \n" + date;

    try {
      List<String> profileNames = new ArrayList<String>();
      profileNames.add("Admin");
      profileNames.add("Publisher");
      String[] users = getOrganisationController().getUsersIdsByRoleNames(
          getComponentId(), profileNames);

      List<UserRecipient> recipients = new ArrayList<UserRecipient>(users.length);
      for (String userId : users) {
        recipients.add(new UserRecipient(userId));
      }

      NotificationMetaData notifMetaData = new NotificationMetaData(
          NotificationParameters.NORMAL, subject, messageText);
      notifMetaData.setSender(getUserId());
      notifMetaData.addUserRecipients(recipients);
      notifMetaData.setSource(getSpaceLabel() + " - " + getComponentLabel());
      getNotificationSender().notifyUser(notifMetaData);
    } catch (Exception e) {
      SilverTrace.warn("webSites", "WebSiteSessionController.notifyPublishers()",
          "webSites.MSG_NOTIFY_PUBLISHERS_FAIL", null, e);
    }
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      silverObjectId = webSiteEjb.getSilverObjectId(getComponentId(), objectId);
    } catch (Exception e) {
      SilverTrace.error("webSites", "WebSiteSessionController.getSilverObjectId()",
          "root.EX_CANT_GET_LANGUAGE_RESOURCE", "objectId=" + objectId, e);
    }
    return silverObjectId;
  }

  @Override
  public void close() {
    if (webSiteEjb != null) {
      webSiteEjb = null;
    }
  }

  /**
   * @param idSite
   * @param arrayTopic
   * @throws WebSitesException
   */
  public void updateClassification(String idSite, ArrayList<String> arrayTopic)
      throws WebSitesException {
    try {
      String idPub = webSiteEjb.getIdPublication(getComponentId(), idSite);
      webSiteEjb.updateClassification(new PublicationPK(idPub, getComponentId()), arrayTopic);
    } catch (Exception re) {
      throw new WebSitesException("WebSiteSessionController.updateClassification",
          SilverpeasException.ERROR, "webSites.EX_PUBLICATION_UPDATE_FAILED", "site id =" + idSite,
          re);
    }
  }

  public void checkPath(String path) throws WebSitesException {
    if (path.contains("..")) {
      throw new WebSitesException(getClass().getSimpleName() + ".checkPath",
          SilverpeasException.ERROR, "peasCore.RESOURCE_ACCESS_FORBIDDEN");
    }
  }

  private String getFullPath(String relativePath) throws WebSitesException {
    checkPath(relativePath);
    return getWebSiteRepositoryPath() + relativePath;
  }

  public int addFileIntoWebSite(String webSitePath, FileItem fileItem) throws Exception {
    String fileName = FileUploadUtil.getFileName(fileItem);
    if (isInWhiteList(fileName)) {
      String path = getWebSiteRepositoryPath() + "/" + webSitePath;
      File file = new File(path, fileName);
      fileItem.write(file);
      return 0;
    } else {
      return -2;
    }
  }

  /**
   * Creates a web site from the content of an archive file (a ZIP file).
   *
   * @param descriptionSite the site to create.
   * @param fileItem the zip archive with the content of the site to create.
   * @return the creation status. 0 means the creation succeed, other values means the site creation
   * failed: -1 the main page name is invalid and -2 the web site folder creation failed.
   * @throws Exception if an unexpected error occurs when creating the web site.
   */
  public int createWebSiteFromZipFile(SiteDetail descriptionSite, FileItem fileItem) throws
      Exception {
    /* Création du directory */
    String cheminZip = getWebSiteRepositoryPath() + getWebSitePathById(descriptionSite.getId());
    File directory = new File(cheminZip);
    if (directory.mkdir()) {
      /* creation du zip sur le serveur */
      String fichierZipName = FileUploadUtil.getFileName(fileItem);
      File fichier = new File(cheminZip + "/" + fichierZipName);

      fileItem.write(fichier);

      /* dezip du fichier.zip sur le serveur */
      String cheminFichierZip = cheminZip + "/" + fichierZipName;
      unzip(cheminZip, cheminFichierZip);

      /* check the files are thoses expected */
      Collection<File> files = FileFolderManager.getAllFile(cheminZip);
      for (File uploadedFile : files) {
        if (!uploadedFile.getName().equals(fichierZipName) && !isInWhiteList(uploadedFile.getName())) {
          return -2;
        }
      }

      /* verif que le nom de la page principale est correcte */
      Collection<File> collPages = getAllWebPages2(getWebSitePathById(descriptionSite.getId()));
      SilverTrace.debug("webSites", "RequestRouter.EffectiveUploadSiteZip",
          "root.MSG_GEN_PARAM_VALUE", collPages.size() + " files in zip");
      SilverTrace.debug("webSites", "RequestRouter.EffectiveUploadSiteZip",
          "root.MSG_GEN_PARAM_VALUE", "nomPage = " + descriptionSite.getContent());
      Iterator<File> j = collPages.iterator();
      boolean searchOk = false;
      File f;
      while (j.hasNext()) {
        f = j.next();
        SilverTrace.debug("webSites", "RequestRouter.EffectiveUploadSiteZip",
            "root.MSG_GEN_PARAM_VALUE", "f.getName() = " + f.getName());
        if (f.getName().equals(descriptionSite.getContent())) {
          searchOk = true;
          break;
        }
      }

      if (!searchOk) {
        // le nom de la page principale n'est pas bonne
        return -1;
      }
    } else {
      return -2;
    }
    return 0;
  }

  public boolean isInWhiteList(String fileName) {
    String authorizedExtensions = getSettings().getString(WEBSITE_WHITE_LIST);
    if (StringUtil.isDefined(authorizedExtensions)) {
      List<String> whiteList = Arrays.asList(authorizedExtensions.split(" "));
      String extension = FilenameUtils.getExtension(fileName).toLowerCase();
      return whiteList.contains(extension);
    }
    return false;
  }
}
