/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.silverpeas.silvercrawler.control;

import com.silverpeas.admin.components.Parameter;
import com.silverpeas.silvercrawler.model.FileDetail;
import com.silverpeas.silvercrawler.model.FileFolder;
import com.silverpeas.silvercrawler.model.SilverCrawlerFileUploadException;
import com.silverpeas.silvercrawler.model.SilverCrawlerFolderCreationException;
import com.silverpeas.silvercrawler.model.SilverCrawlerFolderRenameException;
import com.silverpeas.silvercrawler.model.SilverCrawlerForbiddenActionException;
import com.silverpeas.silvercrawler.model.SilverCrawlerRuntimeException;
import com.silverpeas.silvercrawler.statistic.HistoryByUser;
import com.silverpeas.silvercrawler.statistic.HistoryDetail;
import com.silverpeas.silvercrawler.statistic.Statistic;
import com.silverpeas.silvercrawler.util.FileServerUtils;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminException;
import com.stratelia.webactiv.beans.admin.AdministrationServiceProvider;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.silverpeas.search.SearchEngineProvider;
import org.silverpeas.search.indexEngine.model.IndexEngineProxy;
import org.silverpeas.search.indexEngine.model.IndexEntryPK;
import org.silverpeas.search.indexEngine.model.RepositoryIndexer;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import org.silverpeas.util.FileRepositoryManager;
import org.silverpeas.util.FileUtil;
import org.silverpeas.util.StringUtil;
import org.silverpeas.util.UnitUtil;
import org.silverpeas.util.ZipUtil;
import org.silverpeas.util.exception.SilverpeasException;
import org.silverpeas.util.exception.SilverpeasRuntimeException;
import org.silverpeas.util.fileFolder.FileFolderManager;
import org.silverpeas.util.memory.MemoryUnit;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class SilverCrawlerSessionController extends AbstractComponentSessionController {

  private File currentPath;
  private File rootPath;
  private List<String> paths;
  private List<FileDetail> currentResultSearch = new ArrayList<>();
  private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
  private UploadReport lastReport;
  private static Pattern WEIRD_CHARACTERS_REGEX = Pattern.compile("[/\\\\:*?\"<>|]");

  /**
   * Standard Session Controller Constructor
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public SilverCrawlerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.silvercrawler.multilang.silverCrawlerBundle",
        "org.silverpeas.silvercrawler.settings.silverCrawlerIcons",
        "org.silverpeas.silvercrawler.settings.silverCrawlerSettings");
    rootPath = new File(getComponentParameterValue("directory"));
    setRootPath();
  }

  public FileFolder getCurrentFolder(boolean isAdmin) {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.getCurrentFolder()",
        "root.MSG_GEN_PARAM_VALUE", "path = " + currentPath);
    return new FileFolder(getRootPath(), getCurrentPath(), isAdmin, getComponentId());
  }

  public FileFolder getCurrentFolder() {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.getCurrentFolder()",
        "root.MSG_GEN_PARAM_VALUE", "path = " + currentPath);
    return getCurrentFolder(false);
  }

  public boolean isRootPath() {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.isRootPath()",
        "root.MSG_GEN_PARAM_VALUE", "currentPath = " + currentPath + " rootPath = " + rootPath);
    return currentPath.equals(rootPath);
  }

  public void setRootPath() {
    currentPath = rootPath;
    paths = new ArrayList<>();
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
    List<String> newPaths = new ArrayList<>();
    currentPath = rootPath;
    for (final String path : paths) {
      // on ajoute ce répertoire à la liste
      newPaths.add(path);
      // currentPath = currentPath + File.separator + path;
      currentPath = FileUtils.getFile(currentPath, path);
      if (path.equals(directory)) {
        // on est sur le répertoire voulu
        break;
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
    return download;
  }

  public Boolean isPrivateSearch() {
    // retourne true si on utilise le moteur de recherche dédié
    return "yes".equalsIgnoreCase(getComponentParameterValue("privateSearch"));
  }

  public Boolean isAllowedNav() {
    // retourne true si les lecteurs ont le droit de naviguer dans l'arborescence
    return "yes".equalsIgnoreCase(getComponentParameterValue("allowedNav"));
  }

  public void setCurrentPath(String path) {
    // currentPath = currentPath + File.separator + path;
    currentPath = FileUtils.getFile(currentPath, path);
    // mise à jour de la collection des chemins
    paths.add(path);
  }

  public void setCurrentPathFromResult(String path) {
    File pathFile = FileUtils.getFile(path);
    if (pathFile.getPath().startsWith(rootPath.getPath())) {
      pathFile = FileUtils.getFile(pathFile.getPath().substring(rootPath.getPath().length()));
    }
    currentPath = FileUtils.getFile(rootPath, pathFile.getPath());
    // mise à jour de la collection des chemins
    paths.clear();
    // décomposer le chemin pour créer le path
    SilverTrace.debug("silverCrawler", "SilverCrawlerSessionController.getDestination()",
        "root.MSG_GEN_PARAM_VALUE",
        "separator = " + File.separator + " path = " + pathFile.getPath());

    while (pathFile != null && pathFile.getName().length() > 0) {
      paths.add(pathFile.getName());
      pathFile = pathFile.getParentFile();
    }
    Collections.reverse(paths);
  }

  public String getNameFromPath(String path) {
    return FileUtils.getFile(path).getName();
  }

  public String getCurrentPath() {
    return currentPath.getPath();
  }

  public String getFullPath(String directory) {
    return FileUtils.getFile(currentPath, directory).getPath();
  }

  public Collection<String> getPath() {
    return paths;
  }

  public FolderZIPInfo zipFolder(String folderName) {
    FolderZIPInfo zipInfo = new FolderZIPInfo();
    File downloadPath = FileUtils.getFile(getFullPath(folderName));
    try {
      FileUtil.validateFilename(downloadPath.getPath(), getRootPath());
    } catch (IOException e) {
      SilverTrace.error("silverCrawler", "SilverCrawlerRequestRouter.zipFolder()",
          "root.MSG_GEN_PARAM_VALUE", "downloadPath error = " + downloadPath.getPath());
    }

    Calendar calendar = Calendar.getInstance(Locale.FRENCH);
    String date = createDate(calendar);
    File zipFile = FileUtils
        .getFile(FileRepositoryManager.getTemporaryPath(), folderName + "_" + date + ".zip");

    SilverTrace.debug("silverCrawler", "SilverCrawlerSessionController.zipFolder()",
        "root.MSG_GEN_PARAM_VALUE", "fileZip = " + zipFile.getName());

    long sizeMax = UnitUtil.convertTo(getSizeMax(), MemoryUnit.MB, MemoryUnit.B);
    long zipSize = 0;
    String url = "";
    SilverTrace.debug("silverCrawler", "SilverCrawlerSessionController.zipFolder()",
        "root.MSG_GEN_PARAM_VALUE", "sizeMax = " + sizeMax);

    // rechercher si la taille du répertoire est < à la taille maxi
    boolean sizeOk = getSize(downloadPath.getPath(), sizeMax);

    SilverTrace.debug("silverCrawler", "SilverCrawlerSessionController.zipFolder()",
        "root.MSG_GEN_PARAM_VALUE", "sizeOk = " + sizeOk);

    // si la taille est inferieur à celle autorisée :
    if (sizeOk) {
      try {
        zipSize = ZipUtil.compressPathToZip(downloadPath, zipFile);
        url = FileServerUtils
            .getSilverCrawlerUrl(zipFile.getName(), zipFile.getName(), getComponentId(),
                downloadPath.getPath().substring(getRootPath().length()));
      } catch (Exception e) {
        throw new SilverCrawlerRuntimeException("SilverCrawlerSessionController.zipFolder()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_ZIP_DIRECTORY", e);
      }
    } else {
      zipFile = FileUtils.getFile("");
    }

    // Fill in ZipFolderInfo object
    zipInfo.setFileZip(zipFile.getName());
    zipInfo.setSize(zipSize);
    zipInfo.setMaxiSize(getSizeMaxString());
    zipInfo.setUrl(url);

    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.zipFolder()",
        "root.MSG_GEN_PARAM_VALUE", "fileZip = " + zipFile.getName());

    return zipInfo;
  }

  public Collection<HistoryByUser> getHistoryByFolder(String folderName) {
    String path = getFullPath(folderName);
    return Statistic.getHistoryByObject(path, getComponentId());
  }

  public Collection<HistoryByUser> getHistoryByFolderFromResult(String folderName) {
    File path = FileUtils.getFile(rootPath, folderName);
    return Statistic.getHistoryByObject(path.getPath(), getComponentId());
  }

  public Collection<HistoryDetail> getHistoryByUser(String folderName, String userId) {
    String path = getFullPath(folderName);
    return Statistic.getHistoryByObjectAndUser(path, userId, getComponentId());
  }

  public void unindexPath(String folderName) {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.unindexPath()",
        "root.MSG_GEN_ENTER_METHOD", "folderName = " + folderName);

    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(), getComponentId());
    String pathRepository = getFullPath(folderName);
    Date date = new Date();
    repositoryIndexer.pathIndexer(pathRepository, date.toString(), getUserId(), "remove");
  }

  public void unindexFile(String fileName) {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.unindexFile()",
        "root.MSG_GEN_ENTER_METHOD", "fileName = " + fileName);

    File path = FileUtils.getFile(currentPath, fileName);

    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(), getComponentId());
    repositoryIndexer.indexFile("remove", new Date().toString(), getUserId(), path);
  }

  public void indexPath(String folderName) {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.indexPath()",
        "root.MSG_GEN_ENTER_METHOD", "folderName = " + folderName);

    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(), getComponentId());
    String pathRepository = getFullPath(folderName);
    Date date = new Date();
    repositoryIndexer.pathIndexer(pathRepository, date.toString(), getUserId(), "add");
  }

  public void indexFile(String fileName) {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.indexFile()",
        "root.MSG_GEN_ENTER_METHOD", "fileName = " + fileName);

    File path = FileUtils.getFile(currentPath, fileName);

    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(), getComponentId());
    repositoryIndexer.indexFile("add", new Date().toString(), getUserId(), path);
  }

  public void indexPathSelected(Collection<String> dirToIndex) {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.indexPathSelected()",
        "root.MSG_GEN_ENTER_METHOD", "dirToIndex = " + dirToIndex.size());

    for (final String name : dirToIndex) {
      SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.indexPathSelected()",
          "root.MSG_GEN_ENTER_METHOD", "name = " + name);
      indexPath(name);
    }
  }

  public void indexSelectedFiles(Collection<String> fileToIndex) {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.indexSelectedFiles()",
        "root.MSG_GEN_ENTER_METHOD", "fileToIndex = " + fileToIndex.size());

    for (final String name : fileToIndex) {
      SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.indexSelectedFiles()",
          "root.MSG_GEN_ENTER_METHOD", "name = " + name);
      indexFile(name);
    }
  }

  public Collection<FileDetail> getResultSearch(String word) {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.getResultSearch()",
        "root.MSG_GEN_PARAM_VALUE", "word =" + word);
    Collection<FileDetail> docs = new ArrayList<>();
    try {
      if (word != null && !"*".equals(word.trim()) && !word.trim().isEmpty()) {
        QueryDescription query = new QueryDescription(word);
        query.setSearchingUser(getUserId());
        query.addSpaceComponentPair(getSpaceId(), getComponentId());
        SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.getResultSearch()",
            "root.MSG_GEN_PARAM_VALUE", "query =" + query.getQuery());
        List<MatchingIndexEntry> result =
            SearchEngineProvider.getSearchEngine().search(query).getEntries();
        SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.getResultSearch()",
            "root.MSG_GEN_PARAM_VALUE", "result =" + result.size());

        FileDetail file;
        for (MatchingIndexEntry matchIndex : result) {
          String type = matchIndex.getObjectType();
          String path = matchIndex.getObjectId();

          File fileOnServer = new File(path);

          if (fileOnServer.exists()) {
            // Récupération des objects indéxés
            // Modification du chemin absolu pour masquer le contexte
            String absolutePath = fileOnServer.getAbsolutePath();
            path = FileUtils.getFile(fileOnServer.getPath().substring(getRootPath().length()))
                .getPath();
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
      SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.getResultSearch()",
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
      size = FileUtils.sizeOf(dir);
    }
    if (size > sizeMaxi) {
      ok = false;
    }

    return ok;
  }

  private String createDate(Calendar calendar) {
    return formatter.format(new Date());
  }

  public String getRootPath() {
    return rootPath.getPath();
  }

  /**
   * Is read//write access has been activated
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
   * @param active true to activate read/write access
   * @throws SilverCrawlerForbiddenActionException
   */
  public void switchReadWriteAccess(boolean active)
      throws SilverCrawlerRuntimeException, SilverCrawlerForbiddenActionException {

    // only people with admin profil AND listed in silverpeas configuration are allowed to set
    // read/write access
    checkRWSettingsAccess(true);
    try {
      ComponentInst instance =
          AdministrationServiceProvider.getAdminService().getComponentInst(getComponentId());
      List<Parameter> params = instance.getParameters();
      for (Parameter param : params) {
        if (param.getName().equals("readWriteActivated")) {
          params.remove(param);
          break;
        }
      }

      Parameter rwAccessParam = new Parameter();
      HashMap<String, String> labels = new HashMap<>();
      labels.put("fr", "Accès lecture/écriture");
      rwAccessParam.setName("readWriteActivated");
      rwAccessParam.setLabel(labels);
      rwAccessParam.setValue(active ? "yes" : "no");
      params.add(rwAccessParam);

      AdministrationServiceProvider.getAdminService().updateComponentInst(instance);
    } catch (AdminException e) {
      throw new SilverCrawlerRuntimeException(
          "SilverCrawlerSessionController.switchReadWriteAccess", SilverpeasException.ERROR,
          "silvercrawler.EX_SWITCH_RW_ACCESS", e);
    }
  }

  /**
   * Only people with admin profil AND listed in silverpeas configuration are allowed to set
   * read/write access
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
            "readWriteActivated in platform : false");
      } else {
        return false;
      }
    }

    // Then checks admin profile
    String[] userRoles = getUserRoles();
    boolean isAdmin = false;
    for (String userRole : userRoles) {
      if ("admin".equals(userRole)) {
        isAdmin = true;
        break;
      }
    }
    if (!isAdmin) {
      if (throwException) {
        throw new SilverCrawlerForbiddenActionException(
            "SilverCrawlerSessionController.checkRWSettingsAccess", SilverpeasException.ERROR,
            "userRoles : " + Arrays.toString(userRoles));
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
   * @param folderName name of folder to be removed
   * @param isAdmin flag to indicate if user has admin profile
   * @throws SilverCrawlerForbiddenActionException
   */
  public void removeSubFolder(String folderName, boolean isAdmin)
      throws SilverCrawlerForbiddenActionException {
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.removeSubFolder()",
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
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.removeSubFolder()",
        "root.MSG_GEN_PARAM_VALUE", "fullPath = " + fullPath);
    FileFolderManager.deleteFolder(fullPath);
  }

  public void renameFolder(String folderName, String newName)
      throws SilverCrawlerFolderRenameException, IOException {
    // looks for weird characters in new file name (security)
    if (containsWeirdCharacters(newName)) {
      throw new SilverCrawlerFolderRenameException("SilverCrawlerSessionController.renameFolder",
          SilverpeasException.ERROR, getString("silverCrawler.nameIncorrect"));
    }

    // Get Full Path
    File after = FileUtils.getFile(getFullPath(newName));
    if (after.exists()) {
      throw new SilverCrawlerFolderRenameException("SilverCrawlerSessionController.renameFolder",
          SilverpeasException.ERROR, getString("silverCrawler.folderNameAlreadyExists"));
    }

    // Rename file
    File before = FileUtils.getFile(getFullPath(folderName));
    if (before.isDirectory()) {
      FileUtils.moveDirectory(before, after);
    } else {
      FileUtils.moveFile(before, after);
    }
  }

  private boolean containsWeirdCharacters(String newName) {
    return StringUtil.isDefined(newName) && WEIRD_CHARACTERS_REGEX.matcher(newName).find();
  }

  public void createFolder(String newName) throws SilverCrawlerFolderCreationException {

    // looks for weird characters in new file name (security)
    if (containsWeirdCharacters(newName)) {
      throw new SilverCrawlerFolderCreationException("SilverCrawlerSessionController.renameFolder",
          SilverpeasException.ERROR, getString("silverCrawler.nameIncorrect"));
    }

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
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.removeFile()",
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
    SilverTrace.info("silverCrawler", "SilverCrawlerSessionController.removeFile()",
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

  public void setLastUploadReport(UploadReport report) {
    this.lastReport = report;
  }

  public UploadReport getLastUploadReport() {
    return lastReport;
  }

  /**
   * Checks upload info coming from DragNDrop. Uses UploadReport to retrieves folders/files list
   * and detect conflicts
   * @return an upload report
   */
  public UploadReport checkLastUpload() {
    for (UploadItem item : lastReport.items) {

      // Test if file already exists
      File targetFile = FileUtils.getFile(currentPath, item.getRelativePath().getPath());
      if (targetFile.exists()) {
        item.setItemAlreadyExists(true);
        lastReport.setConflictous(true);
      }
    }

    return lastReport;
  }

  /**
   * Process copy from DragAndDrop temp repository to current folder
   * @return
   */
  public UploadReport processLastUpload() {
    for (UploadItem item : lastReport.items) {

      // Build full paths
      File sourcePath =
          FileUtils.getFile(lastReport.getRepositoryPath(), item.getRelativePath().getPath());
      File targetPath = FileUtils.getFile(currentPath, item.getRelativePath().getPath());

      // copy File from repository to current folder
      if (!item.itemAlreadyExists || item.replace) {

        try {
          FileUtils.copyFile(sourcePath, targetPath);
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
        File repository = lastReport.getRepositoryPath();
        if (repository.exists()) {
          FileUtils.deleteQuietly(lastReport.getRepositoryPath());
        }
      }
      lastReport = null;
    }

  }

  public boolean checkUserLANAccess(String remoteIPAdress) {

    // Step 1 - Checks user profile
    String[] profiles = getUserRoles();
    String bestProfile = ProfileHelper.getBestProfile(profiles);
    if ((!bestProfile.equals("admin")) && (!bestProfile.equals("publisher"))) {
      SilverTrace.debug("silverCrawler", "SilverCrawlerSessionController.checkUserLANAccess()",
          "root.MSG_GEN_PARAM_VALUE", "user is only reader => no LAN access");
      return false;
    }

    // Step 2 - Checks component parameter value
    boolean allowAccessByLAN =
        StringUtil.getBooleanValue(getComponentParameterValue("allowAccessByLAN"));
    SilverTrace.debug("silverCrawler", "SilverCrawlerSessionController.checkUserLANAccess()",
        "root.MSG_GEN_PARAM_VALUE", "allowAccessByLAN = " + allowAccessByLAN);
    if (!allowAccessByLAN) {
      return false;
    }

    // Step 3 - Test remoteIPAddress over LAN subnetwork masks
    String subnetworkMasks = getComponentParameterValue("LANMasks");
    boolean ipElligible = IPMaskHelper.isIPElligible(remoteIPAdress, subnetworkMasks);
    SilverTrace.debug("silverCrawler", "SilverCrawlerSessionController.checkUserLANAccess()",
        "root.MSG_GEN_PARAM_VALUE",
        "remoteIP = " + remoteIPAdress + ", masks = " + subnetworkMasks + ", elligible :" +
            ipElligible);

    return ipElligible;
  }
}
