/**
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
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
package com.silverpeas.silvercrawler.control;

import com.silverpeas.admin.components.Parameter;
import com.silverpeas.silvercrawler.model.*;
import com.silverpeas.silvercrawler.statistic.HistoryByUser;
import com.silverpeas.silvercrawler.statistic.HistoryDetail;
import com.silverpeas.silvercrawler.statistic.Statistic;
import com.silverpeas.silvercrawler.util.FileServerUtils;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdminReference;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.silverpeas.search.SearchEngineFactory;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;
import org.silverpeas.search.indexEngine.model.RepositoryIndexer;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class SilverCrawlerSessionController extends AbstractComponentSessionController {

  private String currentPath = "";
  private String rootPath = "";
  private Collection<String> paths = null;
  private Collection<FileDetail> currentResultSearch = new ArrayList<FileDetail>();
  private String separator = "";
  private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
  private UploadReport lastReport;
  static private String[] WEIRD_CHARACTERS = {File.separator};

  /**
   * Standard Session Controller Constructor
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public SilverCrawlerSessionController(MainSessionController mainSessionCtrl,
    ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
      "com.silverpeas.silvercrawler.multilang.silverCrawlerBundle",
      "com.silverpeas.silvercrawler.settings.silverCrawlerIcons",
      "com.silverpeas.silvercrawler.settings.silverCrawlerSettings");
    rootPath = getComponentParameterValue("directory");
    separator = rootPath.substring(0, 1);
    if (!separator.equals("/") && !separator.equals("\\")) {
      separator = "\\";
    }
    if (rootPath.endsWith(separator)) {
      rootPath = rootPath.substring(rootPath.length() - 1, rootPath.length());
    }
    setRootPath();
  }

  public FileFolder getCurrentFolder(boolean isAdmin) {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.getCurrentFolder()",
      "root.MSG_GEN_PARAM_VALUE", "path = " + currentPath);
    return new FileFolder(rootPath, currentPath, isAdmin, getComponentId());
  }

  public FileFolder getCurrentFolder() {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.getCurrentFolder()",
      "root.MSG_GEN_PARAM_VALUE", "path = " + currentPath);
    return getCurrentFolder(false);
  }

  public boolean isRootPath() {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.isRootPath()",
      "root.MSG_GEN_PARAM_VALUE", "currentPath = " + currentPath
      + " rootPath = " + rootPath);
    return currentPath.equals(rootPath);
  }

  public void setRootPath() {
    currentPath = rootPath;

    // création de la collection des chemins
    paths = new ArrayList<String>();
  }

  public long getSizeMax() {
    return Long.parseLong(getComponentParameterValue("maxiSize"));
  }

  public Long getSizeMaxString() {
    return Long.valueOf(getComponentParameterValue("maxiSize"));
  }

  public String getNbMaxDirectoriesByPage() {
    if (StringUtil.isDefined(getComponentParameterValue("nbMaxDirectoriesByPage"))) {
      return getComponentParameterValue("nbMaxDirectoriesByPage");
    } else {
      return "10";
    }
  }

  public String getNbMaxFilesByPage() {
    if (StringUtil.isDefined(getComponentParameterValue("nbMaxFilesByPage"))) {
      return getComponentParameterValue("nbMaxFilesByPage");
    } else {
      return "10";
    }
  }

  public void goToDirectory(String directory) {
    // parcourir les répertoires et recréer les variables courantes
    Collection<String> newPaths = new ArrayList<String>();

    currentPath = rootPath;
    boolean trouve = false;

    Iterator<String> it = paths.iterator();
    while (it.hasNext() && !trouve) {
      String path = it.next();
      // on ajoute ce répertoire à la liste
      newPaths.add(path);
      // currentPath = currentPath + File.separator + path;
      currentPath = currentPath + separator + path;
      if (path.equals(directory)) {
        // on est sur le répertoire voulu
        trouve = true;
      }
    }
    // mise à jour de la collection des répertoires
    paths = newPaths;
  }

  public Boolean isDownload() {
    // retourne true si l'utilisateur peut télécharger un répertoire complet
    boolean download = true;
    if (getSizeMax() == 0) {
      download = false;
    }
    return new Boolean(download);
  }

  public Boolean isPrivateSearch() {
    // retourne true si on utilise le moteur de recherche dédié
    return new Boolean("yes".equalsIgnoreCase(getComponentParameterValue("privateSearch")));
  }

  public Boolean isAllowedNav() {
    // retourne true si les lecteurs ont le droit de naviguer dans
    // l'arborescence
    return new Boolean("yes".equalsIgnoreCase(getComponentParameterValue("allowedNav")));
  }

  public void setCurrentPath(String path) {
    // currentPath = currentPath + File.separator + path;
    currentPath = currentPath + separator + path;
    // mise à jour de la collection des chemins
    paths.add(path);
  }

  public void setCurrentPathFromResult(String path) {
    currentPath = rootPath + separator + path;
    // mise à jour de la collection des chemins
    paths.clear();
    // décomposer le chemin pour créer le path
    SilverTrace.debug("silverCrawler",
      "SilverCrawlerSessionController.getDestination()",
      "root.MSG_GEN_PARAM_VALUE", "separator = " + separator + " path = "
      + path);

    StringTokenizer st = new StringTokenizer(path, separator);
    String name = "";

    while (st.hasMoreTokens()) {
      name = st.nextToken();
      paths.add(name);
    }
  }

  public String getNameFromPath(String path) {
    StringTokenizer st = new StringTokenizer(path, separator);
    String name = "";

    while (st.hasMoreTokens()) {
      name = st.nextToken();
    }
    return name;
  }

  public String getCurrentPath() {
    return currentPath;
  }

  public String getFullPath(String directory) {
    String exportPath = currentPath + separator + directory;
    return exportPath;
  }

  public Collection<String> getPath() {
    return paths;
  }

  public FolderZIPInfo zipFolder(String folderName) {
    FolderZIPInfo zipInfo = new FolderZIPInfo();
    String downloadPath = getFullPath(folderName);
    SilverTrace.info("silverCrawler", "SilverCrawlerRequestRouter.zipFolder()",
      "root.MSG_GEN_PARAM_VALUE", "downloadPath = " + downloadPath);

    Calendar calendar = Calendar.getInstance(Locale.FRENCH);
    String date = createDate(calendar);
    String fileZip = folderName + "_" + date + ".zip";
    String pathZip = FileRepositoryManager.getTemporaryPath();

    SilverTrace.debug("silverCrawler",
      "SilverCrawlerSessionController.zipFolder()",
      "root.MSG_GEN_PARAM_VALUE", "fileZip = " + fileZip);

    long sizeMax = getSizeMax() * 1000000;
    long sizeZip = 0;
    String url = "";
    SilverTrace.debug("silverCrawler",
      "SilverCrawlerSessionController.zipFolder()",
      "root.MSG_GEN_PARAM_VALUE", "sizeMax = " + sizeMax);

    // rechercher si la taille du répertoire est < à la taille maxi
    boolean sizeOk = getSize(downloadPath, sizeMax);

    SilverTrace.debug("silverCrawler",
      "SilverCrawlerSessionController.zipFolder()",
      "root.MSG_GEN_PARAM_VALUE", "sizeOk = " + sizeOk);

    // si la taille est inferieur à celle autorisée :
    if (sizeOk) {
      try {
        sizeZip = ZipManager.compressPathToZip(downloadPath, pathZip + fileZip);

        if (fileZip != null && !fileZip.equals("null")) {
          downloadPath = downloadPath.substring(rootPath.length() + 1);
          url = FileServerUtils.getUrlToTempDir(fileZip, fileZip,
            "application/zip", getUserId(), getComponentId(), downloadPath);
        }
      } catch (Exception e) {
        throw new SilverCrawlerRuntimeException(
          "SilverCrawlerSessionController.zipFolder()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_ZIP_DIRECTORY", e);
      }
    } else {
      fileZip = null;
    }

    // Fill in ZipFolderInfo object
    zipInfo.setFileZip(fileZip);
    zipInfo.setSize(sizeZip);
    zipInfo.setMaxiSize(getSizeMaxString());
    zipInfo.setUrl(url);

    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.zipFolder()",
      "root.MSG_GEN_PARAM_VALUE", "fileZip = " + fileZip);

    return zipInfo;
  }

  public Collection<HistoryByUser> getHistoryByFolder(String folderName) {
    String path = getFullPath(folderName);
    return Statistic.getHistoryByObject(path, getComponentId());

  }

  public Collection<HistoryByUser> getHistoryByFolderFromResult(String folderName) {
    String path = rootPath + separator + folderName;
    return Statistic.getHistoryByObject(path, getComponentId());

  }

  public Collection<HistoryByUser> getHistoryByFile(String fileName) {
    String path = getFullPath(fileName);
    return Statistic.getHistoryByObject(path, getComponentId());

  }

  public Collection<HistoryDetail> getHistoryByUser(String folderName, String userId) {
    String path = getFullPath(folderName);
    return Statistic.getHistoryByObjectAndUser(path, userId, getComponentId());
  }

  public void unindexPath(String folderName) {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.unindexPath()",
      "root.MSG_GEN_ENTER_METHOD", "folderName = " + folderName);

    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(),
      getComponentId());
    String pathRepository = getFullPath(folderName);
    if (!pathRepository.endsWith(separator)) {
      pathRepository += separator;
    }
    Date date = new Date();
    repositoryIndexer.pathIndexer(pathRepository, date.toString(), getUserId(),
      "remove");
  }

  public void unindexFile(String fileName) {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.unindexFile()",
      "root.MSG_GEN_ENTER_METHOD", "fileName = " + fileName);

    String path = currentPath + separator + fileName;

    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(),
      getComponentId());
    repositoryIndexer.indexFile("remove", new Date().toString(), getUserId(),
      new File(path));
  }

  public void indexPath(String folderName) {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.indexPath()",
      "root.MSG_GEN_ENTER_METHOD", "folderName = " + folderName);

    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(),
      getComponentId());
    String pathRepository = getFullPath(folderName);
    if (!pathRepository.endsWith(separator)) {
      pathRepository += separator;
    }
    Date date = new Date();
    repositoryIndexer.pathIndexer(pathRepository, date.toString(), getUserId(),
      "add");
  }

  public void indexFile(String fileName) {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.indexFile()",
      "root.MSG_GEN_ENTER_METHOD", "fileName = " + fileName);

    String path = currentPath + separator + fileName;

    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(),
      getComponentId());
    repositoryIndexer.indexFile("add", new Date().toString(), getUserId(),
      new File(path));
  }

  public void indexPathSelected(Collection<String> dirToIndex) {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.indexPathSelected()",
      "root.MSG_GEN_ENTER_METHOD", "dirToIndex = " + dirToIndex.size());

    Iterator<String> it = dirToIndex.iterator();
    while (it.hasNext()) {
      String name = it.next();
      SilverTrace.info("silverCrawler",
        "SilverCrawlerSessionController.indexPathSelected()",
        "root.MSG_GEN_ENTER_METHOD", "name = " + name);
      indexPath(name);
    }
  }

  public void indexSelectedFiles(Collection<String> fileToIndex) {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.indexSelectedFiles()",
      "root.MSG_GEN_ENTER_METHOD", "fileToIndex = " + fileToIndex.size());

    Iterator<String> it = fileToIndex.iterator();
    while (it.hasNext()) {
      String name = it.next();
      SilverTrace.info("silverCrawler",
        "SilverCrawlerSessionController.indexSelectedFiles()",
        "root.MSG_GEN_ENTER_METHOD", "name = " + name);
      indexFile(name);
    }
  }

  public Collection<FileDetail> getResultSearch(String word) {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.getResultSearch()",
      "root.MSG_GEN_PARAM_VALUE", "word =" + word);
    Collection<FileDetail> docs = new ArrayList<FileDetail>();
    try {
      if (word != null && !"*".equals(word.trim()) && !word.trim().isEmpty()) {
        QueryDescription query = new QueryDescription(word);
        query.setSearchingUser(getUserId());
        query.addSpaceComponentPair(getSpaceId(), getComponentId());
        SilverTrace.info("silverCrawler",
          "SilverCrawlerSessionController.getResultSearch()",
          "root.MSG_GEN_PARAM_VALUE", "query =" + query.getQuery());
        List<MatchingIndexEntry> result = SearchEngineFactory.getSearchEngine().search(query)
          .getEntries();
        SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.getResultSearch()",
          "root.MSG_GEN_PARAM_VALUE", "result =" + result.size());

        FileDetail file = null;
        for (MatchingIndexEntry matchIndex : result) {
          String type = matchIndex.getObjectType();
          String path = matchIndex.getObjectId();

          File fileOnServer = new File(path);

          if (fileOnServer.exists()) {
            // Récupération des objects indéxés
            // Modification du chemin absolu pour masquer le contexte
            String absolutePath = path;
            path = path.substring(rootPath.length() + 1);
            if ("LinkedFile".equals(type)) {//File
              file =
                  new FileDetail(matchIndex.getTitle(), path, absolutePath, fileOnServer.length(),
                      false);
              docs.add(file);
              SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.getResultSearch()",
                "root.MSG_GEN_PARAM_VALUE", "fichier = " + path);
            } else if ("LinkedDir".equals(type)) {//Directory
              file = new FileDetail(matchIndex.getTitle(), path, absolutePath, 0, true);
              docs.add(file);
              SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.getResultSearch()",
                "root.MSG_GEN_PARAM_VALUE", "répertoire = " + path);
            }
          } else {
            // l'objet n'existe plus, suppression de son index
            IndexEngineProxy.removeIndexEntry(new IndexEntryPK(getComponentId(), type, path));
          }
        }
      }
    } catch (Exception e) {
      SilverTrace.info("silverCrawler",
        "SilverCrawlerSessionController.getResultSearch()",
        "silverCrawler.EX_CAN_SEARCH_QUERY", "query : " + word, e);
    }
    currentResultSearch.clear();
    currentResultSearch.addAll(docs);
    return docs;
  }

  public Collection<FileDetail> getCurrentResultSearch() {
    return currentResultSearch;
  }

  private boolean getSize(String path, long sizeMaxi) {
    long size = 0;
    boolean ok = true;

    File dir = new File(path);
    if (dir.isDirectory()) {
      String[] list = dir.list();
      size = processFileList(list, path, sizeMaxi);
    }
    if (size > sizeMaxi) {
      ok = false;
    }

    return ok;
  }

  private long processFileList(String[] fileList, String path, long sizeMaxi) {
    File currentFile = null;
    String fileName = null;
    long filesSize = 0;
    for (int i = 0; fileList != null && i < fileList.length; i++) {
      fileName = fileList[i];
      // currentFile = new File(path + "\\" + fileName);
      currentFile = new File(path + separator + fileName);

      if (currentFile.isDirectory()) {
        // recursive call to get the current object
        filesSize += processFileList(currentFile.list(), currentFile.getAbsolutePath()
          + separator, sizeMaxi);
        if (filesSize > sizeMaxi) {
          return filesSize;
        }
      } else {
        filesSize += currentFile.length();
        if (filesSize > sizeMaxi) {
          return filesSize;
        }
      }
    }
    return filesSize;
  }

  private String createDate(Calendar calendar) {
    return formatter.format(new Date());
  }

  public String getRootPath() {
    return rootPath;
  }

  /**
   * Is read//write access has been activated
   *
   * @return true only if is activated both in platform and component instance
   */
  public boolean isReadWriteActivated() {
    boolean readWriteActivatedInInstance =
      StringUtil.getBooleanValue(getComponentParameterValue("readWriteActivated"));
    boolean readWriteActivatedInPlatform = getSettings().getBoolean("readWriteActivated", false);

    return readWriteActivatedInInstance && readWriteActivatedInPlatform;
  }

  /**
   * Activate/Desactivate read//write access.
   *
   * @param active true to activate read/write access
   * @throws SilverCrawlerForbiddenActionException
   *
   */
  public void switchReadWriteAccess(boolean active)
    throws SilverCrawlerRuntimeException, SilverCrawlerForbiddenActionException {

    // only people with admin profil AND listed in silverpeas configuration are allowed to set read/write access
    checkRWSettingsAccess(true);
    try {
      ComponentInst instance = AdminReference.getAdminService().getComponentInst(getComponentId());
      List<Parameter> params = instance.getParameters();
      for (Parameter param : params) {
        if (param.getName().equals("readWriteActivated")) {
          params.remove(param);
          break;
        }
      }

      Parameter rwAccessParam = new Parameter();
      HashMap<String, String> labels = new HashMap<String, String>();
      labels.put("fr", "Accès lecture/écriture");
      rwAccessParam.setName("readWriteActivated");
      rwAccessParam.setLabel(labels);
      rwAccessParam.setValue(active ? "yes" : "no");
      params.add(rwAccessParam);

      AdminReference.getAdminService().updateComponentInst(instance);
    } catch (AdminException e) {
      throw new SilverCrawlerRuntimeException(
        "SilverCrawlerSessionController.switchReadWriteAccess", SilverpeasException.ERROR,
        "silvercrawler.EX_SWITCH_RW_ACCESS", e);
    }
  }

  /**
   * Only people with admin profil AND listed in silverpeas configuration are allowed to set
   * read/write access
   *
   * @param throwException true if exception must be thrown if checks failed. (else just return
   * false)
   * @throws SilverCrawlerForbiddenActionException if user doesn't have those requirements.
   */
  public boolean checkRWSettingsAccess(boolean throwException)
    throws SilverCrawlerForbiddenActionException {

    // First checks read/Write access is enable in silverpeas platform
    boolean readWriteActivatedInPlatform = getSettings().getBoolean("readWriteActivated", false);
    if (!readWriteActivatedInPlatform) {
      if (throwException) {
        throw new SilverCrawlerForbiddenActionException(
          "SilverCrawlerSessionController.checkRWSettingsAccess", SilverpeasException.ERROR,
          "readWriteActivated in platform : " + readWriteActivatedInPlatform);
      } else {
        return false;
      }
    }

    // Then checks admin profile
    String[] userRoles = getUserRoles();
    boolean isAdmin = false;
    for (String userRole : userRoles) {
      if (userRole.equals("admin")) {
        isAdmin = true;
        break;
      }
    }
    if (!isAdmin) {
      if (throwException) {
        throw new SilverCrawlerForbiddenActionException(
          "SilverCrawlerSessionController.checkRWSettingsAccess", SilverpeasException.ERROR,
          "userRoles : " + userRoles);
      } else {
        return false;
      }
    }

    // And checks that current user is present in user list authorized to set read/write access
    String usersAllowedToSetRWAccessParamValue =
      getSettings().getString("usersAllowedToSetRWAccess");
    String userId = getUserId();

    boolean userAllowedToSetRWAccess = false;
    if (StringUtil.isDefined(usersAllowedToSetRWAccessParamValue)) {
      String[] usersAllowedToSetRWAccess = usersAllowedToSetRWAccessParamValue.split(",");
      for (String userIdAllowedToSetRWAccess : usersAllowedToSetRWAccess) {
        if (userId.equals(userIdAllowedToSetRWAccess)) {
          userAllowedToSetRWAccess = true;
          break;
        }
      }
    }
    if (!userAllowedToSetRWAccess) {
      if (throwException) {
        throw new SilverCrawlerForbiddenActionException(
          "SilverCrawlerSessionController.checkRWSettingsAccess", SilverpeasException.ERROR,
          "usersAllowedToSetRWAccess : " + usersAllowedToSetRWAccessParamValue);
      } else {
        return false;
      }
    }

    return true;
  }

  /**
   * Remove given subfolder.
   *
   * @param folderName name of folder to be removed
   * @param isAdmin flag to indicate if user has admin profile
   * @throws SilverCrawlerForbiddenActionException
   *
   */
  public void removeSubFolder(String folderName, boolean isAdmin)
    throws SilverCrawlerForbiddenActionException {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.removeSubFolder()",
      "root.MSG_GEN_ENTER_METHOD", "folderName = " + folderName);

    // 1st check : user must have admin profile for this component instance
    if (!isAdmin) {
      throw new SilverCrawlerForbiddenActionException(
        "SilverCrawlerSessionController.removeSubFolder", SilverpeasException.ERROR,
        "user has not admin rights");
    }

    // 2nd check : RW access must have been activated
    if (!isReadWriteActivated()) {
      throw new SilverCrawlerForbiddenActionException(
        "SilverCrawlerSessionController.removeSubFolder", SilverpeasException.ERROR,
        "RW Access not activated");
    }

    // Get Full Path
    String fullPath = getFullPath(folderName);
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.removeSubFolder()",
      "root.MSG_GEN_PARAM_VALUE", "fullPath = " + fullPath);
    FileFolderManager.deleteFolder(fullPath);
  }

  public void renameFolder(String folderName, String newName)
    throws SilverCrawlerFolderRenameException {
    // looks for weird characters in new file name
    if (containsWeirdCharacters(newName)) {
      throw new SilverCrawlerFolderRenameException("SilverCrawlerSessionController.renameFolder",
        SilverpeasException.ERROR, getString("silverCrawler.nameIncorrect"));
    }

    // Get Full Path
    String fullPath = getFullPath(newName);
    File newFile = new File(fullPath);
    if (newFile.exists()) {
      throw new SilverCrawlerFolderRenameException("SilverCrawlerSessionController.renameFolder",
        SilverpeasException.ERROR, getString("silverCrawler.folderNameAlreadyExists"));
    }

    // Rename file
    String oldPath = getFullPath(folderName);
    File oldFile = new File(oldPath);
    oldFile.renameTo(newFile);
  }

  private boolean containsWeirdCharacters(String newName) {
    for (String weirdChar : WEIRD_CHARACTERS) {
      if (newName.contains(weirdChar)) {
        return true;
      }
    }

    return false;
  }

  public void createFolder(String newName) throws SilverCrawlerFolderCreationException {
    // Get Full Path
    String fullPath = getFullPath(newName);
    File newFile = new File(fullPath);
    if (newFile.exists()) {
      throw new SilverCrawlerFolderCreationException("SilverCrawlerSessionController.createFolder",
        SilverpeasException.ERROR, getString("silverCrawler.folderNameAlreadyExists"));
    }

    // create folder
    try {
      FileUtils.forceMkdir(newFile);
    } catch (IOException e) {
      throw new SilverCrawlerFolderCreationException("SilverCrawlerSessionController.createFolder",
        SilverpeasException.ERROR, getString("silverCrawler.notAllowedToDropCreateFolders"), e);
    }
  }

  public void removeFile(String fileName, boolean isAdminOrPublisher)
    throws SilverCrawlerForbiddenActionException {
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.removeFile()",
      "root.MSG_GEN_ENTER_METHOD", "fileName = " + fileName);

    // 1st check : user must have admin or publisher profile for this component instance
    if (!isAdminOrPublisher) {
      throw new SilverCrawlerForbiddenActionException("SilverCrawlerSessionController.removeFile",
        SilverpeasException.ERROR, "user has not admin rights");
    }

    // 2nd check : RW access must have been activated
    if (!isReadWriteActivated()) {
      throw new SilverCrawlerForbiddenActionException("SilverCrawlerSessionController.removeFile",
        SilverpeasException.ERROR, "RW Access not activated");
    }

    // Get Full Path
    String fullPath = getFullPath(fileName);
    SilverTrace.info("silverCrawler",
      "SilverCrawlerSessionController.removeFile()",
      "root.MSG_GEN_PARAM_VALUE", "fullPath = " + fullPath);
    FileFolderManager.deleteFile(fullPath);
  }

  public void saveFile(FileItem fileItem, boolean replaceFile)
    throws SilverCrawlerFileUploadException {
    String name = FileUtil.getFilename(fileItem.getName());
    if (StringUtil.isDefined(name)) {
      // compute full path
      String fullPath = getFullPath(name);
      File newFile = new File(fullPath);

      // Checks if file already exists
      if (newFile.exists() && !replaceFile) {
        throw new SilverCrawlerFileUploadException("SilverCrawlerSessionController.saveFile",
          SilverpeasException.ERROR, getString("silverCrawler.fileAlreadyExists"));
      }

      // Write file to disk
      try {
        fileItem.write(newFile);
      } catch (Exception e) {
        throw new SilverCrawlerFileUploadException("SilverCrawlerSessionController.saveFile",
          SilverpeasException.ERROR, getString("silverCrawler.unknownCause"), e);
      }
    }

  }

  ;

  public void setLastUploadReport(UploadReport report) {
    this.lastReport = report;
  }

  public UploadReport getLastUploadReport() {
    return lastReport;
  }

  /**
   * Checks upload info coming from DragNDrop. Uses UploadReport to retrieves folders/files list and
   * detect conflicts
   *
   * @return
   */
  public UploadReport checkLastUpload() {
    for (UploadItem item : lastReport.items) {

      // Test if file already exists
      String fullPath = currentPath + item.getFileName();
      File targetFile = new File(fullPath);
      if (targetFile.exists()) {
        item.setItemAlreadyExists(true);
        lastReport.setConflictous(true);
      }
    }

    return lastReport;
  }

  /**
   * Process copy from DragAndDrop temp repository to current folder
   *
   * @return
   */
  public UploadReport processLastUpload() {
    String repositoryPath = lastReport.getRepositoryPath();
    for (UploadItem item : lastReport.items) {

      // Build full paths
      String sourcePath = repositoryPath + item.getFileName();
      String targetPath = currentPath + item.getFileName();
      String parentPath = currentPath + File.separator + item.getParentPath();

      // Creates parent folder(s) if needed
      File parentDir = new File(parentPath);
      if (!parentDir.exists()) {
        parentDir.mkdirs();
      }

      // copy File from repository to current folder
      if ((!item.itemAlreadyExists) || (item.replace == true)) {

        try {
          FileRepositoryManager.copyFile(sourcePath, targetPath);
          if (item.itemAlreadyExists) {
            lastReport.nbReplaced++;
          } else {
            lastReport.nbCopied++;
          }
        } catch (IOException e) {
          SilverTrace.error("silverCrawler", "SilverCrawlerSessionController.processLastUpload",
            "silverCrawler.EX_PROCESSING_UPLOAD",
            "sourcePath :" + sourcePath + " , targetPath :" + targetPath);
          item.setCopyFailed(e);
          lastReport.setFailed(true);
        }
      } else {
        lastReport.nbIgnored++;
      }
    }

    return lastReport;
  }

  /**
   * Reset any existing upload report and clean temp folder if exists.
   */
  public void resetLastUploadReport() {
    if (lastReport != null) {
      if (lastReport.getRepositoryPath() != null) {
        File repository = new File(lastReport.getRepositoryPath());
        if (repository.exists()) {
          FileFolderManager.deleteFolder(lastReport.getRepositoryPath(), false);
        }
      }

      lastReport = null;
    }

  }
  
  public boolean checkUserLANAccess(String remoteIPAdress) {

    // Step 1 - Checks user profile
    String[] profiles = getUserRoles();
    String bestProfile = ProfileHelper.getBestProfile(profiles);
    if ((!bestProfile.equals("admin"))
        && (!bestProfile.equals("publisher"))) {
      SilverTrace.debug("silverCrawler",
          "SilverCrawlerSessionController.checkUserLANAccess()",
          "root.MSG_GEN_PARAM_VALUE",
          "user is only reader => no LAN access");
      return false;
    }

    // Step 2 - Checks component parameter value
    boolean allowAccessByLAN = StringUtil
        .getBooleanValue(getComponentParameterValue("allowAccessByLAN"));
    SilverTrace.debug("silverCrawler",
        "SilverCrawlerSessionController.checkUserLANAccess()",
        "root.MSG_GEN_PARAM_VALUE", "allowAccessByLAN = "
            + allowAccessByLAN);
    if (!allowAccessByLAN) {
      return false;
    }

    // Step 3 - Test remoteIPAddress over LAN subnetwork masks
    String subnetworkMasks = getComponentParameterValue("LANMasks");
    boolean ipElligible = IPMaskHelper.isIPElligible(remoteIPAdress,
        subnetworkMasks);
    SilverTrace.debug("silverCrawler",
        "SilverCrawlerSessionController.checkUserLANAccess()",
        "root.MSG_GEN_PARAM_VALUE", "remoteIP = " + remoteIPAdress
            + ", masks = " + subnetworkMasks + ", elligible :"
            + ipElligible);

    return ipElligible;
  }
}
