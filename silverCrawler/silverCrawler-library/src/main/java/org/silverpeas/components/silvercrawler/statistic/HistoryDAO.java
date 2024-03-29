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

package org.silverpeas.components.silvercrawler.statistic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class HistoryDAO {
  public static Collection<HistoryDetail> getHistoryDetails(ResultSet rs) throws SQLException {
    List<HistoryDetail> list = new ArrayList<>();
    while (rs.next()) {
      Date date = new Date(Long.parseLong(rs.getString(1)));
      String userId = rs.getString(2);
      String path = rs.getString(3);
      HistoryDetail detail = new HistoryDetail(date, userId, path);
      list.add(detail);
    }
    return list;
  }

  public static void add(Connection con, String tableName, String userId, String path,
      String componentId, String objectType) throws SQLException {


    String insertStatement = "insert into " + tableName + " values (?, ?, ?, ?, ?)";
    Date date = new Date();
    try (PreparedStatement prepStmt = con.prepareStatement(insertStatement)) {
      prepStmt.setString(1, Long.toString(date.getTime()));
      prepStmt.setString(2, userId);
      prepStmt.setString(3, path);
      prepStmt.setString(4, componentId);
      prepStmt.setString(5, objectType);
      prepStmt.executeUpdate();
    }
  }

  public static Collection<HistoryDetail> getHistoryDetailByObject(Connection con, String tableName,
      String path, String componentId) throws SQLException {
    String selectStatement = "select * from " + tableName + " where path = ? and componentId = ? ";

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setString(1, path);
      prepStmt.setString(2, componentId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        Collection<HistoryDetail> list = getHistoryDetails(rs);
        return list;
      }
    }
  }

  public static Collection<HistoryDetail> getHistoryDetailByObjectAndUser(Connection con,
      String tableName, String path, String userId, String componentId) throws SQLException {

    String selectStatement =
        "select * from " + tableName + " where path = ? and componentId = ? and userId = ? " +
            " order by dateDownload desc";

    try (PreparedStatement prepStmt = con.prepareStatement(selectStatement)) {
      prepStmt.setString(1, path);
      prepStmt.setString(2, componentId);
      prepStmt.setString(3, userId);
      try (ResultSet rs = prepStmt.executeQuery()) {
        Collection<HistoryDetail> list = getHistoryDetails(rs);
        return list;
      }
    }
  }

  public static void deleteHistoryByObject(Connection con, String tableName, String path,
      String componentId) throws SQLException {
    String query = "delete from " + tableName + " where path = ? and componentId = ?";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      // initialisation des paramètres
      prepStmt.setString(1, path);
      prepStmt.setString(2, componentId);
      prepStmt.executeUpdate();
    }
  }

}
