package com.silverpeas.silvercrawler.statistic;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import com.silverpeas.silvercrawler.model.SilverCrawlerRuntimeException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;

public class Statistic {
  private final static String historyTableName = "SC_SilverCrawler_Statistic";

  public static String DIRECTORY = "Directory";
  public static String FILE = "File";

  private static Connection getConnection() {
    Connection con;
    // initialisation de la connexion
    try {
      con = DBUtil.makeConnection(JNDINames.DATABASE_DATASOURCE);
    } catch (UtilException e) {
      // traitement des exceptions
      throw new SilverCrawlerRuntimeException("Statistic.getConnection()",
          SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
    return con;
  }

  private static void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        throw new SilverCrawlerRuntimeException("Statistic.freeConnection()",
            SilverpeasException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
      }
    }
  }

  public static void addStat(String userId, String path, String componentId,
      String objectType) {
    SilverTrace.info("silverCrawler", "Statistic.addStat()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      // ajout dans les stats de l'objet
      HistoryDAO.add(con, historyTableName, userId, path, componentId,
          objectType);
      if (objectType.equals(DIRECTORY)) {
        // dans le cas d'un répertoire, on le parcours pour ajouter les stats
        // sur les sous répertoires et les fichiers
        addStatForDirectory(con, userId, path, componentId);
      }
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException("Statistic.addstat()",
          SilverpeasRuntimeException.ERROR, "silverCrawler.CANNOT_ADD_STAT", e);
    } finally {
      freeConnection(con);
    }
  }

  private static void addStatForDirectory(Connection con, String userId,
      String path, String componentId) throws SQLException {
    File dir = new File(path);
    if (dir.isDirectory()) {
      String[] list = dir.list();
      processFileList(con, list, userId, path, componentId);
    }
  }

  private static void processFileList(Connection con, String[] fileList,
      String userId, String path, String componentId) throws SQLException {
    File currentFile = null;
    String fileName = null;
    for (int i = 0; fileList != null && i < fileList.length; i++) {
      fileName = fileList[i];
      currentFile = new File(path + "\\" + fileName);

      if (currentFile.isDirectory()) {
        // ajout du répertoire dans les stats
        HistoryDAO.add(con, historyTableName, userId, currentFile
            .getAbsolutePath(), componentId, DIRECTORY);
        // et appel récursif de la fonction sur ce répertoire
        processFileList(con, currentFile.list(), userId, currentFile
            .getAbsolutePath()
            + "\\", componentId);
      } else {
        // ajout du fichier dans les stats
        HistoryDAO.add(con, historyTableName, userId, currentFile
            .getAbsolutePath(), componentId, FILE);
      }
    }
  }

  public static Collection getHistoryByObject(String path, String componentId) {
    SilverTrace.info("silverCrawler", "Statistic.getHistoryByObject()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection list = getHistoryByAction(path, componentId);

    OrganizationController orga = new OrganizationController();
    Collection statByUser = new ArrayList();

    Iterator it = list.iterator();
    while (it.hasNext()) {
      HistoryDetail historyObject = (HistoryDetail) it.next();

      // rechercher si le user est déjà enregistré
      Iterator itStat = statByUser.iterator();
      boolean trouve = false;
      while (itStat.hasNext()) {
        HistoryByUser historyUser = (HistoryByUser) itStat.next();
        if (historyUser.getUser().getId().equals(historyObject.getUserId())) {
          // mettre à jour, l'enregistrement existe
          long currentLastAccess = historyUser.getLastDownload().getTime();
          long newDate = historyObject.getDate().getTime();
          if (newDate > currentLastAccess)
            historyUser.setLastDownload(new Date(newDate));
          historyUser.setNbDownload(historyUser.getNbDownload() + 1);
          trouve = true;
        }
      }
      if (!trouve) {
        // créer l'enregistrement
        UserDetail user = orga.getUserDetail(historyObject.getUserId());
        HistoryByUser historyByUser = new HistoryByUser(user, historyObject
            .getDate(), 1);
        statByUser.add(historyByUser);
      }
    }

    SilverTrace.info("silverCrawler", "Statistic.getHistoryByObject()",
        "root.MSG_GEN_EXIT_METHOD");
    return statByUser;
  }

  public static Collection getHistoryByAction(String path, String componentId) {
    SilverTrace.info("silverCrawler", "Statistic.getHistoryByAction",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      Collection result = HistoryDAO.getHistoryDetailByObject(con,
          historyTableName, path, componentId);

      return result;
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException("Statistic.getHistoryByAction()",
          SilverpeasRuntimeException.ERROR,
          "silverCrawler.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
  }

  public static Collection getHistoryByObjectAndUser(String path,
      String userId, String componentId) {
    SilverTrace.info("silverCrawler", "Statistic.getHistoryByObjectAndUser()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      Collection result = HistoryDAO.getHistoryDetailByObjectAndUser(con,
          historyTableName, path, userId, componentId);

      return result;
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException(
          "Statistic.getHistoryByObjectAndUser()",
          SilverpeasRuntimeException.ERROR,
          "silverCrawler.CANNOT_GET_HISTORY_STATISTICS_PUBLICATION", e);
    } finally {
      freeConnection(con);
    }
  }

  public static void deleteHistoryByObject(String path, String componentId) {
    SilverTrace.info("silverCrawler", "Statistic.deleteHistoryByObject",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;

    try {
      con = getConnection();
      HistoryDAO
          .deleteHistoryByObject(con, historyTableName, path, componentId);
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException(
          "Statistic.deleteHistoryByObject()",
          SilverpeasRuntimeException.ERROR,
          "silverCrawler.CANNOT_DELETE_HISTORY_STATISTICS", e);
    } finally {
      freeConnection(con);
    }
  }

}
