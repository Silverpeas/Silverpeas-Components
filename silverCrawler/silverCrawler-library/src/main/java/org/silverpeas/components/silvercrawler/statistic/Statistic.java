/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.components.silvercrawler.statistic;

import org.silverpeas.components.silvercrawler.model.SilverCrawlerRuntimeException;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

public class Statistic {
  private static final String HISTORY_TABLE_NAME = "SC_SilverCrawler_Statistic";

  public static final String DIRECTORY = "Directory";
  public static final String FILE = "File";

  private Statistic() {

  }

  private static Connection getConnection() {
    Connection con;
    try {
      con = DBUtil.openConnection();
    } catch (SQLException e) {
      throw new SilverCrawlerRuntimeException(e);
    }
    return con;
  }

  public static void addStat(String userId, File path, String componentId, String objectType) {

    try (Connection con = getConnection()) {
      // ajout dans les stats de l'objet
      HistoryDAO
          .add(con, HISTORY_TABLE_NAME, userId, path.getAbsolutePath(), componentId, objectType);
      if (objectType.equals(DIRECTORY)) {
        // dans le cas d'un répertoire, on le parcours pour ajouter les stats
        // sur les sous répertoires et les fichiers
        processFileList(con, path, userId, componentId);
      }
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException(e);
    }
  }

  private static void processFileList(Connection con, File path, String userId, String componentId)
      throws SQLException {
    if (path.isDirectory()) {
      File[] fileList = path.listFiles();
      if (fileList != null) {
        for (File currentFile : fileList) {
          if (currentFile.isDirectory()) {
            // ajout du répertoire dans les stats
            HistoryDAO
                .add(con, HISTORY_TABLE_NAME, userId, currentFile.getAbsolutePath(), componentId,
                    DIRECTORY);
            // et appel récursif de la fonction sur ce répertoire
            processFileList(con, currentFile, userId, componentId);
          } else {
            // ajout du fichier dans les stats
            HistoryDAO
                .add(con, HISTORY_TABLE_NAME, userId, currentFile.getAbsolutePath(), componentId,
                    FILE);
          }
        }
      }
    }
  }

  public static Collection<HistoryByUser> getHistoryByObject(String path, String componentId) {
    Collection<HistoryDetail> list = getHistoryByAction(path, componentId);

    OrganizationController orga = OrganizationControllerProvider.getOrganisationController();
    Collection<HistoryByUser> statByUser = new ArrayList<>();

    for (final HistoryDetail historyObject : list) {
      // rechercher si le user est déjà enregistré
      Iterator<HistoryByUser> itStat = statByUser.iterator();
      boolean trouve = false;
      while (itStat.hasNext()) {
        HistoryByUser historyUser = itStat.next();
        if (historyUser.getUser().getId().equals(historyObject.getUserId())) {
          // mettre à jour, l'enregistrement existe
          long currentLastAccess = historyUser.getLastDownload().getTime();
          long newDate = historyObject.getDate().getTime();
          if (newDate > currentLastAccess) {
            historyUser.setLastDownload(new Date(newDate));
          }
          historyUser.setNbDownload(historyUser.getNbDownload() + 1);
          trouve = true;
        }
      }
      if (!trouve) {
        // créer l'enregistrement
        UserDetail user = orga.getUserDetail(historyObject.getUserId());
        HistoryByUser historyByUser = new HistoryByUser(user, historyObject.getDate(), 1);
        statByUser.add(historyByUser);
      }
    }


    return statByUser;
  }

  public static Collection<HistoryDetail> getHistoryByAction(String path, String componentId) {

    try (Connection con = getConnection()) {
      return
          HistoryDAO.getHistoryDetailByObject(con, HISTORY_TABLE_NAME, path, componentId);
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException(e);
    }
  }

  public static Collection<HistoryDetail> getHistoryByObjectAndUser(String path, String userId,
      String componentId) {

    try (Connection con = getConnection()) {
      return HistoryDAO
          .getHistoryDetailByObjectAndUser(con, HISTORY_TABLE_NAME, path, userId, componentId);
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException(e);
    }
  }

  public static void deleteHistoryByObject(String path, String componentId) {
    try (Connection con = getConnection()) {
      HistoryDAO.deleteHistoryByObject(con, HISTORY_TABLE_NAME, path, componentId);
    } catch (Exception e) {
      throw new SilverCrawlerRuntimeException(e);
    }
  }

}
