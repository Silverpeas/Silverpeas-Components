/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.websites.control;

import org.apache.commons.io.FilenameUtils;
import org.silverpeas.components.websites.model.FolderDetail;
import org.silverpeas.components.websites.model.IconDetail;
import org.silverpeas.components.websites.model.SiteDetail;
import org.silverpeas.components.websites.service.WebSiteService;
import org.silverpeas.components.websites.service.WebSitesException;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.exception.DecodingException;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.util.ZipUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileItem;
import org.silverpeas.core.util.file.FileUploadUtil;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.kernel.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;

/**
 * This is the webSite manager main interface It contains all the methods to be accessible to the
 * client
 *
 * @author Cécile BONIN
 */
public class WebSiteSessionController extends AbstractComponentSessionController {

  private FolderDetail sessionTopic = null;
  private NotificationSender notifSender = null;
  private SiteDetail sessionSite = null;
  private String siteName;
  public final static String TAB_PDC = "tabPdc";
  private static final String WEBSITE_REPO_PROPERTY = "uploadsPath";
  private static final String WEBSITE_WHITE_LIST = "whiteList";

  public WebSiteSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.webSites.multilang.webSiteBundle",
        null, "org.silverpeas.webSites.settings.webSiteSettings");
    initialize();
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
  private void initialize() {
    // Remove all data store by this SessionController
    removeSessionTopic();
    removeSessionSite();
  }

  public boolean isBookmarkMode() {
    return ("bookmark".equals(getComponentRootName()));
  }

  public boolean isSortedTopicsEnabled() {
    return "yes".equalsIgnoreCase(getComponentParameterValue("sortedTopics"));
  }

  /* WebSite - Gestion des objets session */

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
   * getWebSiteService
   */
  public synchronized WebSiteService getWebSiteService() {
    return WebSiteService.get();
  }

  public boolean isPdcUsed() {
    String parameterValue = getComponentParameterValue("usepdc");
    return StringUtil.isDefined(parameterValue) && StringUtil.getBooleanValue(parameterValue);
  }

  public synchronized FolderDetail getFolder(String id) throws WebSitesException {
    try {
      return getWebSiteService().goTo(new NodePK(id, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void updateFolderHeader(NodeDetail nd)
      throws WebSitesException {
    try {
      getWebSiteService().updateFolder(nd, sessionTopic.getNodePK());
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized NodeDetail getFolderDetail(String id) throws WebSitesException {
    try {
      return getWebSiteService().getFolderDetail(new NodePK(id, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void addFolder(NodeDetail nd) throws WebSitesException {
    try {
      getWebSiteService().addFolder(nd, sessionTopic.getNodePK(), getUserDetail());
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void deleteFolder(String id) throws WebSitesException {
    try {
      getWebSiteService().deleteFolder(new NodePK(id, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void changeTopicsOrder(String way, String topicId) {
    NodePK nodePK = new NodePK(topicId, getSpaceId(), getComponentId());
    getWebSiteService().changeTopicsOrder(way, nodePK, getSessionTopic().getNodePK());
  }

  /* ** gestion, des publi ** */
  public synchronized PublicationDetail getPublicationDetail(String pubId)
      throws WebSitesException {
    try {
      return getWebSiteService().getPublicationDetail(new PublicationPK(pubId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void deletePublication(String pubId) throws WebSitesException {
    try {
      getWebSiteService().deletePublication(new PublicationPK(pubId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized Collection<NodePK> getAllFatherPK(String pubId) throws WebSitesException {
    try {
      return getWebSiteService().getAllFatherPK(new PublicationPK(pubId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void addPublicationToFolder(String pubId, String folderId)
      throws WebSitesException {
    try {
      getWebSiteService().addPublicationToTopic(new PublicationPK(pubId, getComponentId()),
          new NodePK(folderId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void removePublicationToFolder(String pubId, String folderId)
      throws WebSitesException {
    try {
      getWebSiteService().removePublicationFromTopic(new PublicationPK(pubId, getComponentId()),
          new NodePK(folderId, getComponentId()));
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized String getIdPublication(String siteId) throws WebSitesException {
    try {
      return getWebSiteService().getIdPublication(getComponentId(), siteId);
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void changePubsOrder(String pubId, int direction) {
    getWebSiteService().changePubsOrder(new PublicationPK(pubId, getComponentId()), getSessionTopic().
        getNodePK(), direction);
  }

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
      return getWebSiteService().getAllWebSite(getComponentId());
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  /**
   * getWebSite
   */
  public synchronized SiteDetail getWebSite(String id) throws WebSitesException {
    try {
      return getWebSiteService().getWebSite(getComponentId(), id);
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  /**
   * getIcons
   */
  public synchronized Collection<IconDetail> getIcons(String id) throws WebSitesException {
    try {
      return getWebSiteService().getIcons(getComponentId(), id);
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  /**
   * getNextId
   */
  public synchronized String getNextId() throws WebSitesException {
    try {
      return getWebSiteService().getNextId(getComponentId());
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  /**
   * getAllIcons
   */
  public synchronized Collection<IconDetail> getAllIcons() throws WebSitesException {
    try {
      return getWebSiteService().getAllIcons(getComponentId());
    } catch (Exception re) {
      throw new WebSitesException(re);
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
      throw new WebSitesException(e);
    }
  }

  /**
   * getAllFile
   */
  public synchronized Collection<File> getAllFile(String chemin) throws WebSitesException {
    try {
      return FileFolderManager.getAllFile(getFullPath(chemin));
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
  }

  /**
   * getAllImages
   */
  public synchronized Collection<File> getAllImages(String chemin) throws WebSitesException {
    try {
      return FileFolderManager.getAllImages(getFullPath(chemin));
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
  }

  /**
   * getAllHTMLWebPages
   */
  public synchronized Collection<File> getAllHTMLWebPages(String chemin) throws WebSitesException {
    try {
      return FileFolderManager.getAllHTMLWebPages(getFullPath(chemin));
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
  }

  /**
   * createWebSite
   */
  public synchronized String createWebSite(SiteDetail description) throws WebSitesException {
    try {
      description.setCreatorId(getUserId());
      description.setCreationDate(new Date());
      String pubPK = getWebSiteService().createWebSite(getComponentId(), description, getUserDetail());
      classifyWebSites(description);
      return pubPK;
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  private void classifyWebSites(SiteDetail siteDetail) {
    String positions = siteDetail.getPositions();
    if (StringUtil.isDefined(positions)) {
      PdcClassificationEntity pdcClassif = null;
      try {
        pdcClassif = PdcClassificationEntity.fromJSON(positions);
      } catch (DecodingException e) {
        SilverLogger.getLogger(this).error(e);
      }
      if (pdcClassif != null && !pdcClassif.isUndefined()) {
        List<PdcPosition> pdcPositions = pdcClassif.getPdcPositions();
        PdcClassification classification =
            aPdcClassificationOfContent(siteDetail).withPositions(pdcPositions);
        classification.classifyContent(siteDetail);
      }
    }
  }

  /**
   * AssociateIcons
   */
  public synchronized void associateIcons(String id, Collection<String> listeIcones)
      throws WebSitesException {
    try {
      getWebSiteService().associateIcons(getComponentId(), id, listeIcones);
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  /**
   * publish
   */
  public synchronized void publish(Collection<String> listeSite) throws WebSitesException {
    /* Collection d'id de site */
    try {
      getWebSiteService().publish(getComponentId(), listeSite);
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  /**
   * dePublish
   */
  public synchronized void dePublish(Collection<String> listeSite) throws WebSitesException {
    try {
      getWebSiteService().dePublish(getComponentId(), listeSite);
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  /**
   * createFolder
   */
  public synchronized void createFolder(String path) throws WebSitesException {
    try {
      FileFolderManager.createFolder(getFullPath(path));
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
  }

  public String getWebSitePathById(String id) {
    return getComponentId() + "/" + id;
  }

  /**
   * renameFolder
   */
  public synchronized void renameFolder(String oldPath, String newPath) throws WebSitesException {
    try {
      FileFolderManager.moveFolder(getFullPath(oldPath), getFullPath(newPath));
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
  }

  /**
   * delFolder
   */
  public synchronized void delFolder(String folderPath) throws WebSitesException {
    try {
      FileFolderManager.deleteFolder(getFullPath(folderPath));
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
  }

  /**
   * createPage
   */
  public synchronized void createFile(String filePath, String fileName, String fileContent)
      throws WebSitesException {
    try {
      FileFolderManager.createFile(getFullPath(filePath), fileName, fileContent);
    } catch (org.silverpeas.core.util.UtilException e) {
      throw new WebSitesException(e);
    }
  }

  /**
   * unzip
   */
  public synchronized void unzip(String destPath, String zipFilePath) throws WebSitesException {
    try {
      File zip = new File(zipFilePath);
      File dest = new File(destPath);
      ZipUtil.extract(zip, dest);
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
  }

  public synchronized void deleteWebSites(Collection<String> liste) throws WebSitesException {
    try {
      getWebSiteService().deleteWebSites(getComponentId(), liste);
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void updateWebSite(SiteDetail siteDetail) throws WebSitesException {
    try {
      siteDetail.setCreatorId(getUserId());
      siteDetail.setCreationDate(new Date());

      getWebSiteService().updateWebSite(getComponentId(), siteDetail);
      String pubId = getWebSiteService().getIdPublication(getComponentId(), siteDetail.getSitePK().getId());

      PublicationPK pubPk = new PublicationPK(pubId, getSpaceId(), getComponentId());
      siteDetail.setPk(pubPk);
      getWebSiteService().updatePublication(siteDetail, getComponentId());
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public synchronized void deleteDirectory(String path) throws WebSitesException {
    boolean result;
    File directory = new File(getFullPath(path));
    try {
      if (directory.exists() && directory.isDirectory()) {
        deleteDirFiles(directory);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).warn(e);
    }
    result = directory.delete();
    if (!result) {
      throw new WebSitesException("The directory" + path + " fails to be deleted");
    }
  }

  private void deleteDirFiles(final File directory) {
    File[] dirFiles = directory.listFiles();
    if (dirFiles != null) {
      for (final File dirFile : dirFiles) {
        delDir(dirFile);
      }
    }
  }

  private synchronized void delDir(File dir) {
    try {
      if (dir.isDirectory()) {
        File[] dirFiles = dir.listFiles();
        if (dirFiles == null) {
          return;
        }
        for (final File dirFile : dirFiles) {
          delDir(dirFile);
        }
      }
      if (!dir.delete()) {
        SilverLogger.getLogger(this).warn("Cannot delete directory {0}", dir.getPath());
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Directory deletion failure: " + dir.getPath(), e);
    }
  }

  /**
   * renameFile
   */
  public synchronized void renameFile(String dir, String name, String newName)
      throws WebSitesException {
    /* chemin = c:\\j2sdk\\public_html\\WAUploads\\WA0webSite10\\nomSite\\Folder */
    try {
      String extension = FilenameUtils.getExtension(name);
      String newExtension = FilenameUtils.getExtension(newName);
      if (extension.equals(newExtension)) {
        FileFolderManager.renameFile(getFullPath(dir), name, newName);
      } else {
        throw new IllegalArgumentException(
            "the new and the old file names must have the same extension: " + extension);
      }
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
  }

  /**
   * deleteFile
   */
  public synchronized void deleteFile(String path) throws WebSitesException {
    try {
      FileFolderManager.deleteFile(getFullPath(path));
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
  }

  public synchronized String verif(String action, String currentPath, String name, String newName,
      String nomPage) throws WebSitesException {
    String res;
    String fullPath = getFullPath(currentPath);
    try {
      switch (action) {
        case "addFolder": {
          // create a folder
          File folder = new File(fullPath, name);
          if (folder.exists()) {
            res = "pbAjoutFolder";
          } else {
            res = "ok";
          }
          break;
        }
        case "renameFolder": {
          // Rename current folder
          File folder = new File(fullPath, newName);
          if (folder.exists()) {
            res = "pbRenommageFolder";
          } else {
            res = "ok";
          }
          break;
        }
        case "addPage": {
          // create a file
          File fichier = new File(fullPath, nomPage);
          if (fichier.exists()) {
            res = "pbAjoutFile";
          } else {
            res = "ok";
          }
          break;
        }
        case "renamePage": {
          // rename a file
          File fichier = new File(fullPath, newName);
          if (fichier.exists()) {
            res = "pbRenommageFile";
          } else {
            res = "ok";
          }
          break;
        }
        default:
          res = "ok";
          break;
      }
    } catch (Exception e) {
      throw new WebSitesException(e);
    }
    return res;
  }

  /**
   * notifyPublishers
   */
  public void notifyPublishers(String auteur, String nomSite, String description, String nomPage,
      String listeMessage, String date) {
    String subject = getString("SuggestionLink");
    String messageText = auteur + " " + getString("PropositionLien") + "  \n \n" +
        getString("VoiciDescriptionLien") + "\n \n" + getString("GML.name") + " : " + nomSite +
        "\n" + getString("GML.description") + " : " + description + "\n" + getString("URL") +
        " : " + nomPage + "\n" + getString("ListeIcones") + " : \n" + listeMessage + "\n " +
        getString("GML.creationDate") + " : \n" + date;

    try {
      List<String> profileNames = new ArrayList<>();
      profileNames.add("Admin");
      profileNames.add("Publisher");
      String[] users =
          getOrganisationController().getUsersIdsByRoleNames(getComponentId(), profileNames);

      List<UserRecipient> recipients = new ArrayList<>(users.length);
      for (String userId : users) {
        recipients.add(new UserRecipient(userId));
      }

      NotificationMetaData notifMetaData =
          new NotificationMetaData(NotificationParameters.PRIORITY_NORMAL, subject, messageText);
      notifMetaData.setSender(getUserId());
      notifMetaData.addUserRecipients(recipients);
      notifMetaData.setComponentId(getComponentId());
      getNotificationSender().notifyUser(notifMetaData);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
  }

  public int getSilverObjectId(String objectId) {
    int silverObjectId = -1;
    try {
      silverObjectId = getWebSiteService().getSilverObjectId(getComponentId(), objectId);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return silverObjectId;
  }

  public void updateClassification(String idSite, ArrayList<String> arrayTopic)
      throws WebSitesException {
    try {
      String idPub = getWebSiteService().getIdPublication(getComponentId(), idSite);
      getWebSiteService().updateClassification(new PublicationPK(idPub, getComponentId()), arrayTopic);
    } catch (Exception re) {
      throw new WebSitesException(re);
    }
  }

  public void checkPath(String path) throws WebSitesException {
    if (path.contains("..")) {
      throw new WebSitesException("Forbidden access path " + path);
    }
  }

  private String getFullPath(String relativePath) throws WebSitesException {
    checkPath(relativePath);
    return getWebSiteRepositoryPath() + relativePath;
  }

  public int addFileIntoWebSite(String webSitePath, FileItem fileItem) throws IOException {
    String fileName = FileUploadUtil.getFileName(fileItem);
    String path = getWebSiteRepositoryPath() + "/" + webSitePath;
    File file = new File(path, fileName);
    fileItem.saveTo(file);
    if (isNotInWhiteList(file)) {
      FileUtil.forceDeletion(file);
      return -3;
    }
    return 0;
  }

  /**
   * Creates a website from the content of an archive file (a ZIP file).
   * @param descriptionSite the site to create.
   * @param fileItem the zip archive with the content of the site to create.
   * @return the creation status. 0 means the creation succeed, other values means the site creation
   * failed: -1 the main page name is invalid and -2 the website folder creation failed.
   * @throws WebSitesException if an unexpected error occurs when creating the website.
   */
  public int createWebSiteFromZipFile(SiteDetail descriptionSite, FileItem fileItem)
      throws WebSitesException {
    /* Création du directory */
    String cheminZip = getWebSiteRepositoryPath() + getWebSitePathById(descriptionSite.getId());
    File directory = new File(cheminZip);
    if (directory.mkdir()) {
      /* creation du zip sur le serveur */
      String fichierZipName = FileUploadUtil.getFileName(fileItem);
      File fichier = new File(cheminZip + "/" + fichierZipName);

      saveFileItem(fileItem, fichier);

      /* dezip du fichier.zip sur le serveur */
      String cheminFichierZip = cheminZip + "/" + fichierZipName;
      unzip(cheminZip, cheminFichierZip);

      /* check the files are thoses expected */
      Collection<File> files = FileFolderManager.getAllWebPages(cheminZip);
      for (File uploadedFile : files) {
        if (!uploadedFile.getName().equals(fichierZipName) && isNotInWhiteList(uploadedFile)) {
          return -3;
        }
      }

      /* verif que le nom de la page principale est correcte */
      Collection<File> collPages = getAllHTMLWebPages(getWebSitePathById(descriptionSite.getId()));
      Iterator<File> j = collPages.iterator();
      boolean searchOk = false;
      File f;
      while (j.hasNext()) {
        f = j.next();
        if (f.getName().equals(descriptionSite.getContentPagePath())) {
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

  private static void saveFileItem(FileItem fileItem, File fichier) throws WebSitesException {
    try {
      fileItem.saveTo(fichier);
    } catch (IOException e) {
      throw new WebSitesException(e);
    }
  }

  private boolean isNotInWhiteList(File file) {
    String authorizedMimeTypes = getSettings().getString(WEBSITE_WHITE_LIST);
    if (StringUtil.isDefined(authorizedMimeTypes)) {
      String[] whiteList = authorizedMimeTypes.split(" ");
      String mimeType = FileUtil.getMimeType(file.getPath());
      for (String whiteMiteType : whiteList) {
        if (mimeType.matches(whiteMiteType.replace("*", ".*").replace("+", "\\+"))) {
          return false;
        }
      }
    }
    return true;
  }
}
