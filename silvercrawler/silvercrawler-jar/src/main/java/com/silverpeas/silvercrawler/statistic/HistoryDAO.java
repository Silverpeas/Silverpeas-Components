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
package com.silverpeas.silvercrawler.statistic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.DBUtil;

public class HistoryDAO {
  public static Collection getHistoryDetails(ResultSet rs) throws SQLException {
    ArrayList list = new ArrayList();
    Date date;
    String userId;
    String path;

    while (rs.next()) {
      date = new Date(Long.parseLong(rs.getString(1)));
      userId = rs.getString(2);
      path = rs.getString(3);
      HistoryDetail detail = new HistoryDetail(date, userId, path);

      list.add(detail);
    }
    return list;
  }

  public static void add(Connection con, String tableName, String userId,
      String path, String componentId, String objectType) throws SQLException {
    SilverTrace.info("silverCrawler", "HistoryDAO.add()",
        "root.MSG_GEN_ENTER_METHOD");

    String insertStatement = "insert into " + tableName
        + " values (?, ?, ?, ?, ?)";
    PreparedStatement prepStmt = null;
    Date date = new Date();
    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, Long.toString(date.getTime()));
      prepStmt.setString(2, userId);
      prepStmt.setString(3, path);
      prepStmt.setString(4, componentId);
      prepStmt.setString(5, objectType);
      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  public static Collection getHistoryDetailByObject(Connection con,
      String tableName, String path, String componentId) throws SQLException {
    SilverTrace.info("silverCrawler", "HistoryDAO.getHistoryDetailByObject",
        "root.MSG_GEN_ENTER_METHOD");
    String selectStatement = "select * from " + tableName
        + " where path = ? and componentId = ? ";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, path);
      prepStmt.setString(2, componentId);
      rs = prepStmt.executeQuery();

      Collection list = getHistoryDetails(rs);
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static Collection getHistoryDetailByObjectAndUser(Connection con,
      String tableName, String path, String userId, String componentId)
      throws SQLException {
    SilverTrace.info("silverCrawler",
        "HistoryDAO.getHistoryDetailByObjectAndUser()",
        "root.MSG_GEN_ENTER_METHOD");
    String selectStatement = "select * from " + tableName
        + " where path = ? and componentId = ? and userId = ? "
        + " order by dateDownload desc";

    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    try {
      prepStmt = con.prepareStatement(selectStatement);
      prepStmt.setString(1, path);
      prepStmt.setString(2, componentId);
      prepStmt.setString(3, userId);
      rs = prepStmt.executeQuery();
      Collection list = getHistoryDetails(rs);
      return list;
    } finally {
      DBUtil.close(rs, prepStmt);
    }
  }

  public static void deleteHistoryByObject(Connection con, String tableName,
      String path, String componentId) throws SQLException {
    SilverTrace.info("statistic", "HistoryObjectDAO.deleteHistoryByObject",
        "root.MSG_GEN_ENTER_METHOD");
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from " + tableName
          + " where path = ? and componentId = ?";
      // initialisation des paramètres

      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, path);
      prepStmt.setString(2, componentId);

      prepStmt.executeUpdate();
    } finally {
      DBUtil.close(prepStmt);
    }
  }

}
