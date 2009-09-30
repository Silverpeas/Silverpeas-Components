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
package com.stratelia.webactiv.yellowpages.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.exception.UtilException;

public class GroupDAO {
  public static Collection getGroupIds(Connection con, String fatherId,
      String instanceId) throws SQLException {
    ArrayList groupIds = new ArrayList();

    String query = "select groupId from SC_Contact_GroupFather where fatherId = ? and instanceId = ? ";
    PreparedStatement prepStmt = null;
    ResultSet rs = null;
    String groupId = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(fatherId));
      prepStmt.setString(2, instanceId);
      rs = prepStmt.executeQuery();
      while (rs.next()) {
        groupId = rs.getString(1);
        groupIds.add(groupId);
      }
    } finally {
      // fermeture
      DBUtil.close(rs, prepStmt);
    }
    return groupIds;
  }

  public static void addGroup(Connection con, String groupId, String fatherId,
      String instanceId) throws SQLException, UtilException {
    PreparedStatement prepStmt = null;
    try {
      String query = "insert into SC_Contact_GroupFather values (?,?,?)";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(groupId));
      prepStmt.setInt(2, Integer.parseInt(fatherId));
      prepStmt.setString(3, instanceId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void removeGroup(Connection con, String groupId)
      throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SC_Contact_GroupFather where groupId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(groupId));
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }

  public static void removeGroup(Connection con, String groupId,
      String fatherId, String instanceId) throws SQLException {
    PreparedStatement prepStmt = null;
    try {
      String query = "delete from SC_Contact_GroupFather where groupId = ? and fatherId = ? and instanceId = ? ";
      prepStmt = con.prepareStatement(query);
      prepStmt.setInt(1, Integer.parseInt(groupId));
      prepStmt.setInt(2, Integer.parseInt(fatherId));
      prepStmt.setString(3, instanceId);
      prepStmt.executeUpdate();
    } finally {
      // fermeture
      DBUtil.close(prepStmt);
    }
  }
}
