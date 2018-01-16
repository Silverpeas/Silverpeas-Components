/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.components.silvercrawler.control;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.silverpeas.components.silvercrawler.model.FileDetail;
import org.silverpeas.components.silvercrawler.model.FileFolder;
import org.silverpeas.components.silvercrawler.model.SilverCrawlerFileUploadException;
import org.silverpeas.components.silvercrawler.model.SilverCrawlerFolderCreationException;
import org.silverpeas.components.silvercrawler.model.SilverCrawlerFolderRenameException;
import org.silverpeas.components.silvercrawler.model.SilverCrawlerForbiddenActionException;
import org.silverpeas.components.silvercrawler.model.SilverCrawlerRuntimeException;
import org.silverpeas.components.silvercrawler.statistic.HistoryByUser;
import org.silverpeas.components.silvercrawler.statistic.HistoryDetail;
import org.silverpeas.components.silvercrawler.statistic.Statistic;
import org.silverpeas.components.silvercrawler.util.FileServerUtils;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.component.model.Parameter;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.service.AdministrationServiceProvider;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.index.indexing.model.RepositoryIndexer;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.ZipUtil;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.util.memory.MemoryUnit;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SilverCrawlerSessionController extends AbstractComponentSessionController {

  private static final String READ_WRITE_ACTIVATED = "readWriteActivated";
  private File currentPath;
  private File rootPath;
  private List<String> paths;
  private List<FileDetail> currentResultSearch = new ArrayList<>();
  private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
  private UploadReport lastReport;
  private static final Pattern WEIRD_CHARACTERS_REGEX = Pattern.compile("[/\\\\:*?\"<>|]");

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

    return new FileFolder(getRootPath(), getCurrentPath(), isAdmin, getComponentId());
  }

  public FileFolder getCurrentFolder() {

    return getCurrentFolder(false);
  }

  public boolean isRootPath() {

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
      SilverLogger.getLogger(this).error("download path error = " + downloadPath.getPath(), e);
    }

    String date = createDate();
    File zipFile = FileUtils
        .getFile(FileRepositoryManager.getTemporaryPath(), folderName + "_" + date + ".zip");

    long sizeMax = UnitUtil.convertTo(getSizeMax(), MemoryUnit.MB, MemoryUnit.B);
    long zipSize = 0;
    String url = "";

    // rechercher si la taille du répertoire est < à la taille maxi
    boolean sizeOk = getSize(downloadPath.getPath(), sizeMax);

    // si la taille est inferieur à celle autorisée :
    if (sizeOk) {
      try {
        zipSize = ZipUtil.compressPathToZip(downloadPath, zipFile);
        url = FileServerUtils
            .getSilverCrawlerUrl(zipFile.getName(), zipFile.getName(), getComponentId(),
                downloadPath.getPath().substring(getRootPath().length()));
      } catch (Exception e) {
        throw new SilverCrawlerRuntimeException(e);
      }
    } else {
      zipFile = FileUtils.getFile("");
    }

    // Fill in ZipFolderInfo object
    zipInfo.setFileZip(zipFile.getName());
    zipInfo.setSize(zipSize);
    zipInfo.setMaxiSize(getSizeMaxString());
    zipInfo.setUrl(url);



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
    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(), getComponentId());
    Path pathRepository = Paths.get(getFullPath(folderName));
    repositoryIndexer.removePath(pathRepository, getUserId());
  }

  public void unindexFile(String fileName) {
    Path path = Paths.get(currentPath.getPath(), fileName);
    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(), getComponentId());
    repositoryIndexer.removePath(path, getUserId());
  }

  public void indexPath(String folderName) {
    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(), getComponentId());
    Path pathRepository = Paths.get(getFullPath(folderName));
    repositoryIndexer.addPath(pathRepository, getUserId());
  }

  public void indexFile(String fileName) {
    Path path = Paths.get(currentPath.getPath(), fileName);
    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(), getComponentId());
    repositoryIndexer.addPath(path, getUserId());
  }

  public void indexPathSelected(Collection<String> dirToIndex) {


    for (final String name : dirToIndex) {

      indexPath(name);
    }
  }

  public void indexSelectedFiles(Collection<String> fileToIndex) {


    for (final String name : fileToIndex) {

      indexFile(name);
    }
  }

  public Collection<FileDetail> getResultSearch(String word) {

    Collection<FileDetail> docs = new ArrayList<>();
    try {
      if (word != null && !"*".equals(word.trim()) && !word.trim().isEmpty()) {
        QueryDescription query = new QueryDescription(word);
        query.setSearchingUser(getUserId());
        query.addComponent(getComponentId());

        List<MatchingIndexEntry> result =
            SearchEngineProvider.getSearchEngine().search(query).getEntries();


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

            } else if ("LinkedDir".equals(type)) {//Directory
              file = new FileDetail(matchIndex.getTitle(), path, absolutePath, 0, true);
              docs.add(file);

            }
          } else {
            // l'objet n'existe plus, suppression de son index
            IndexEngineProxy.removeIndexEntry(new IndexEntryKey(getComponentId(), type, path));
          }
        }
      }
    } catch (ParseException e) {
      SilverLogger.getLogger(this).warn(e);
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

  private String createDate() {
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
        StringUtil.getBooleanValue(getComponentParameterValue(READ_WRITE_ACTIVATED));
    boolean readWriteActivatedInPlatform = getSettings().getBoolean(READ_WRITE_ACTIVATED, false);

    return readWriteActivatedInInstance && readWriteActivatedInPlatform;
  }

  /**
   * Activate/Desactivate read//write access.
   * @param active true to activate read/write access
   * @throws SilverCrawlerForbiddenActionException
   */
  public void switchReadWriteAccess(boolean active) throws SilverCrawlerForbiddenActionException {

    // only people with admin profil AND listed in silverpeas configuration are allowed to set
    // read/write access
    checkRWSettingsAccess(true);
    try {
      ComponentInst instance =
          AdministrationServiceProvider.getAdminService().getComponentInst(getComponentId());
      List<Parameter> params = instance.getParameters();
      for (Parameter param : params) {
        if (param.getName().equals(READ_WRITE_ACTIVATED)) {
          params.remove(param);
          break;
        }
      }

      Parameter rwAccessParam = new Parameter();
      HashMap<String, String> labels = new HashMap<>();
      labels.put("fr", "Accès lecture/écriture");
      rwAccessParam.setName(READ_WRITE_ACTIVATED);
      rwAccessParam.setLabel(labels);
      rwAccessParam.setValue(active ? "yes" : "no");
      params.add(rwAccessParam);

      AdministrationServiceProvider.getAdminService().updateComponentInst(instance);
    } catch (AdminException e) {
      throw new SilverCrawlerRuntimeException(e);
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
    boolean readWriteActivatedInPlatform = getSettings().getBoolean(READ_WRITE_ACTIVATED, false);
    if (!readWriteActivatedInPlatform) {
      if (throwException) {
        throw new SilverCrawlerForbiddenActionException("readWriteActivated in platform: false");
      } else {
        return false;
      }
    }

    // Then checks admin profile
    Collection<SilverpeasRole> userRoles = getSilverpeasUserRoles();
    if (checkIsAdmin(throwException, userRoles)) {
      return false;
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
            "usersAllowedToSetRWAccess: " + usersAllowedToSetRWAccessParamValue);
      } else {
        return false;
      }
    }

    return true;
  }

  private boolean checkIsAdmin(final boolean throwException,
      final Collection<SilverpeasRole> userRoles)
      throws SilverCrawlerForbiddenActionException {
    if (!userRoles.contains(SilverpeasRole.admin)) {
      if (throwException) {
        throw new SilverCrawlerForbiddenActionException("userRoles: " + String.join(", ",
            userRoles.stream().map(SilverpeasRole::getName).collect(Collectors.toList())));
      } else {
        return true;
      }
    }
    return false;
  }

  /**
   * Remove given subfolder.
   * @param folderName name of folder to be removed
   * @param isAdmin flag to indicate if user has admin profile
   * @throws SilverCrawlerForbiddenActionException
   */
  public void removeSubFolder(String folderName, boolean isAdmin)
      throws SilverCrawlerForbiddenActionException {


    // 1st check : user must have admin profile for this component instance
    if (!isAdmin) {
      throw new SilverCrawlerForbiddenActionException("user has not admin rights");
    }

    // 2nd check : RW access must have been activated
    if (!isReadWriteActivated()) {
      throw new SilverCrawlerForbiddenActionException("RW Access not activated");
    }

    // Get Full Path
    String fullPath = getFullPath(folderName);

    FileFolderManager.deleteFolder(fullPath);
  }

  public void renameFolder(String folderName, String newName)
      throws SilverCrawlerFolderRenameException, IOException {
    // looks for weird characters in new file name (security)
    if (containsWeirdCharacters(newName)) {
      throw new SilverCrawlerFolderRenameException(getString("silverCrawler.nameIncorrect"));
    }

    // Get Full Path
    File after = FileUtils.getFile(getFullPath(newName));
    if (after.exists()) {
      throw new SilverCrawlerFolderRenameException(
          getString("silverCrawler.folderNameAlreadyExists"));
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
      throw new SilverCrawlerFolderCreationException(getString("silverCrawler.nameIncorrect"));
    }

    // Get Full Path
    String fullPath = getFullPath(newName);
    File newFile = new File(fullPath);
    if (newFile.exists()) {
      throw new SilverCrawlerFolderCreationException(
          getString("silverCrawler.folderNameAlreadyExists"));
    }

    // create folder
    try {
      FileUtils.forceMkdir(newFile);
    } catch (IOException e) {
      throw new SilverCrawlerFolderCreationException(
          getString("silverCrawler.notAllowedToDropCreateFolders"), e);
    }
  }

  public void removeFile(String fileName, boolean isAdminOrPublisher)
      throws SilverCrawlerForbiddenActionException {


    // 1st check : user must have admin or publisher profile for this component instance
    if (!isAdminOrPublisher) {
      throw new SilverCrawlerForbiddenActionException("user has not admin rights");
    }

    // 2nd check : RW access must have been activated
    if (!isReadWriteActivated()) {
      throw new SilverCrawlerForbiddenActionException("RW Access not activated");
    }

    // Get Full Path
    String fullPath = getFullPath(fileName);

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
        throw new SilverCrawlerFileUploadException(getString("silverCrawler.fileAlreadyExists"));
      }

      // Write file to disk
      try {
        fileItem.write(newFile);
      } catch (Exception e) {
        throw new SilverCrawlerFileUploadException(getString("silverCrawler.unknownCause"), e);
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
          SilverLogger.getLogger(this)
              .error("Processing upload failed. sourcePath :" + sourcePath + " , targetPath :" +
                  targetPath, e);
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
    Collection<SilverpeasRole> roles = getSilverpeasUserRoles();
    SilverpeasRole highestRole = SilverpeasRole.getHighestFrom(roles);
    if (highestRole != SilverpeasRole.admin && highestRole != SilverpeasRole.publisher) {
      return false;
    }

    // Step 2 - Checks component parameter value
    boolean allowAccessByLAN =
        StringUtil.getBooleanValue(getComponentParameterValue("allowAccessByLAN"));
    if (!allowAccessByLAN) {
      return false;
    }

    // Step 3 - Test remoteIPAddress over LAN subnetwork masks
    String subnetworkMasks = getComponentParameterValue("LANMasks");
    return IPMaskHelper.isIPElligible(remoteIPAdress, subnetworkMasks);
  }
}
