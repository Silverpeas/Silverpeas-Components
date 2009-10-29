/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.silvercrawler.control;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.StringTokenizer;

import com.silverpeas.silvercrawler.model.FileDetail;
import com.silverpeas.silvercrawler.model.FileFolder;
import com.silverpeas.silvercrawler.model.SilverCrawlerRuntimeException;
import com.silverpeas.silvercrawler.statistic.Statistic;
import com.silverpeas.silvercrawler.util.FileServerUtils;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.ZipManager;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.indexEngine.model.IndexEngineProxy;
import com.stratelia.webactiv.util.indexEngine.model.IndexEntryPK;
import com.stratelia.webactiv.util.indexEngine.model.RepositoryIndexer;

public class SilverCrawlerSessionController extends
    AbstractComponentSessionController {
  private String currentPath = "";
  private String rootPath = "";
  private Collection paths = null;
  private Collection currentResultSearch = new ArrayList();
  private String separator = "";
  private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");

  /**
   * Standard Session Controller Constructeur
   *
   *
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   *
   * @see
   */
  public SilverCrawlerSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.silvercrawler.multilang.silverCrawlerBundle",
        "com.silverpeas.silvercrawler.settings.silverCrawlerIcons");
    rootPath = getComponentParameterValue("directory");
    separator = rootPath.substring(0, 1);
    if (!separator.equals("/") && !separator.equals("\\"))
      separator = "\\";
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
    paths = new ArrayList();
  }

  public long getSizeMax() {
    return Long.parseLong(getComponentParameterValue("maxiSize"));
  }

  public String getSizeMaxString() {
    return getComponentParameterValue("maxiSize");
  }

  public String getNbMaxDirectoriesByPage() {
    if (StringUtil
        .isDefined(getComponentParameterValue("nbMaxDirectoriesByPage")))
      return getComponentParameterValue("nbMaxDirectoriesByPage");
    else
      return "10";
  }

  public String getNbMaxFilesByPage() {
    if (StringUtil.isDefined(getComponentParameterValue("nbMaxFilesByPage")))
      return getComponentParameterValue("nbMaxFilesByPage");
    else
      return "10";
  }

  public void goToDirectory(String directory) {
    // parcourir les répertoires et recréer les variables courantes
    Collection newPaths = new ArrayList();

    currentPath = rootPath;
    boolean trouve = false;

    Iterator it = paths.iterator();
    while (it.hasNext() && !trouve) {
      String path = (String) it.next();
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
    if (getSizeMax() == 0)
      download = false;
    return new Boolean(download);
  }

  public Boolean isPrivateSearch() {
    // retourne true si on utilise le moteur de recherche dédié
    return new Boolean("yes"
        .equalsIgnoreCase(getComponentParameterValue("privateSearch")));
  }

  public Boolean isAllowedNav() {
    // retourne true si les lecteurs ont le droit de naviguer dans
    // l'arborescence
    return new Boolean("yes"
        .equalsIgnoreCase(getComponentParameterValue("allowedNav")));
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

  public String getDownloadPath(String directory) {
    // String exportPath = currentPath + File.separator + directory;
    String exportPath = currentPath + separator + directory;
    return exportPath;
  }

  public Collection getPath() {
    return paths;
  }

  public String[] zipFolder(String folderName) {
    String[] result = new String[5];
    String downloadPath = getDownloadPath(folderName);
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

    result[0] = fileZip;
    result[1] = Long.toString(sizeZip);
    result[2] = getSizeMaxString();
    result[3] = url;

    SilverTrace.info("silverCrawler",
        "SilverCrawlerSessionController.zipFolder()",
        "root.MSG_GEN_PARAM_VALUE", "fileZip = " + fileZip);

    return result;
  }

  public Collection getHistoryByFolder(String folderName) {
    String path = getDownloadPath(folderName);
    return Statistic.getHistoryByObject(path, getComponentId());

  }

  public Collection getHistoryByFolderFromResult(String folderName) {
    String path = rootPath + separator + folderName;
    return Statistic.getHistoryByObject(path, getComponentId());

  }

  public Collection getHistoryByFile(String fileName) {
    String path = getDownloadPath(fileName);
    return Statistic.getHistoryByObject(path, getComponentId());

  }

  public Collection getHistoryByUser(String folderName, String userId) {
    String path = getDownloadPath(folderName);
    return Statistic.getHistoryByObjectAndUser(path, userId, getComponentId());
  }

  public void indexPath(String folderName) {
    SilverTrace.info("silverCrawler",
        "SilverCrawlerSessionController.indexPath()",
        "root.MSG_GEN_ENTER_METHOD", "folderName = " + folderName);

    RepositoryIndexer repositoryIndexer = new RepositoryIndexer(getSpaceId(),
        getComponentId());
    String pathRepository = getDownloadPath(folderName);
    if (!pathRepository.endsWith(separator))
      pathRepository += separator;
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

  public void indexPathSelected(Collection dirToIndex) {
    SilverTrace.info("silverCrawler",
        "SilverCrawlerSessionController.indexPathSelected()",
        "root.MSG_GEN_ENTER_METHOD", "dirToIndex = " + dirToIndex.size());

    Iterator it = dirToIndex.iterator();
    while (it.hasNext()) {
      String name = (String) it.next();
      SilverTrace.info("silverCrawler",
          "SilverCrawlerSessionController.indexPathSelected()",
          "root.MSG_GEN_ENTER_METHOD", "name = " + name);
      indexPath(name);
    }
  }

  public void filePathSelected(Collection fileToIndex) {
    SilverTrace.info("silverCrawler",
        "SilverCrawlerSessionController.filePathSelected()",
        "root.MSG_GEN_ENTER_METHOD", "fileToIndex = " + fileToIndex.size());

    Iterator it = fileToIndex.iterator();
    while (it.hasNext()) {
      String name = (String) it.next();
      SilverTrace.info("silverCrawler",
          "SilverCrawlerSessionController.indexPathSelected()",
          "root.MSG_GEN_ENTER_METHOD", "name = " + name);
      indexFile(name);
    }
  }

  public Collection getResultSearch(String word) {
    SilverTrace.info("silverCrawler",
        "SilverCrawlerSessionController.getResultSearch()",
        "root.MSG_GEN_PARAM_VALUE", "word =" + word);
    Collection docs = new ArrayList();
    try {
      if (word != null && !word.trim().equals("*") && word.trim().length() > 0) {
        QueryDescription query = new QueryDescription(word);
        query.setSearchingUser(getUserId());
        query.addSpaceComponentPair(getSpaceId(), getComponentId());
        MatchingIndexEntry[] result = null;
        SilverTrace.info("silverCrawler",
            "SilverCrawlerSessionController.getResultSearch()",
            "root.MSG_GEN_PARAM_VALUE", "query =" + query.getQuery());
        SearchEngineBm searchEngineBm = getSearchEngineBm();

        searchEngineBm.search(query);
        result = searchEngineBm.getRange(0, searchEngineBm.getResultLength());
        SilverTrace.info("silverCrawler",
            "SilverCrawlerSessionController.getResultSearch()",
            "root.MSG_GEN_PARAM_VALUE", "result =" + result.length
                + "length = " + getSearchEngineBm().getResultLength());

        FileDetail file = null;
        File fileOnServer = null;
        MatchingIndexEntry matchIndex = null;
        String path = null;
        String type = "";
        for (int i = 0; i < result.length; i++) {
          matchIndex = result[i];

          type = matchIndex.getObjectType();
          path = matchIndex.getObjectId();

          fileOnServer = new File(path);

          if (fileOnServer.exists()) {
            // Récupération des objects indéxés
            // Modification du chemin absolu pour masquer le contexte
            path = path.substring(rootPath.length() + 1);
            if (type.equals("LinkedFile")) {
              // un fichier
              file = new FileDetail(matchIndex.getTitle(), path, fileOnServer
                  .length(), false);
              docs.add(file);
              SilverTrace.info("silverCrawler",
                  "SilverCrawlerSessionController.getResultSearch()",
                  "root.MSG_GEN_PARAM_VALUE", "fichier = " + path);
            } else if (type.equals("LinkedDir")) {
              // un répertoires
              file = new FileDetail(matchIndex.getTitle(), path, 0, true);
              docs.add(file);
              SilverTrace.info("silverCrawler",
                  "SilverCrawlerSessionController.getResultSearch()",
                  "root.MSG_GEN_PARAM_VALUE", "répertoire = " + path);
            }
          } else {
            // l'objet n'existe plus, suppression de son index
            IndexEngineProxy.removeIndexEntry(new IndexEntryPK(
                getComponentId(), type, path));
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

  public Collection getCurrentResultSearch() {
    return currentResultSearch;
  }

  private SearchEngineBm getSearchEngineBm() {
    SearchEngineBm searchEngineBm = null;
    {
      try {
        SearchEngineBmHome searchEngineHome = (SearchEngineBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME,
                SearchEngineBmHome.class);
        searchEngineBm = searchEngineHome.create();
      } catch (Exception e) {
        throw new SilverCrawlerRuntimeException(
            "SilverCrawlerSessionController.getSearchEngineBm()",
            SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return searchEngineBm;
  }

  private boolean getSize(String path, long sizeMaxi) {
    long size = 0;
    boolean ok = true;

    File dir = new File(path);
    if (dir.isDirectory()) {
      String[] list = dir.list();
      size = processFileList(list, path, sizeMaxi);
    }
    if (size > sizeMaxi)
      ok = false;

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
        filesSize += processFileList(currentFile.list(), currentFile
            .getAbsolutePath()
            + separator, sizeMaxi);
        if (filesSize > sizeMaxi)
          return filesSize;
      } else {
        filesSize += currentFile.length();
        if (filesSize > sizeMaxi)
          return filesSize;
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

}