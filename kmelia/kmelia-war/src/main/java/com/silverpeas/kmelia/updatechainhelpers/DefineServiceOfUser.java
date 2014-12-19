/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.kmelia.updatechainhelpers;

import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.node.model.NodeDetail;
import com.stratelia.webactiv.publication.model.PublicationDetail;
import org.silverpeas.util.DBUtil;
import org.silverpeas.util.exception.SilverpeasRuntimeException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DefineServiceOfUser extends UpdateChainHelperImpl {

  public void execute(UpdateChainHelperContext uchc) {
    // Retrieve publication detail
    PublicationDetail pubDetail = uchc.getPubDetail();

    // Retrieve user service
    String service = getUserService(pubDetail.getName());

    // associer le service au node
    String[] topics = new String[1];
    List<NodeDetail> allTopics = uchc.getAllTopics();
    for (NodeDetail node : allTopics) {
      if (node.getName().equals(service)) {
        // enregistrer
        topics[0] = node.getId() + "," + node.getNodePK().getInstanceId();
      }
    }
    uchc.setTopics(topics);
  }

  /**
   * @param userName the user name
   * @return user service from user name given in parameter
   */
  private String getUserService(String userName) {
    Connection con = getConnection();
    String service = "";
    String query = "select service from personnel where (lastname||' '||firstname) = ? ";
    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, userName);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          // Retrieve service
          service = rs.getString(1);
        }
      }
    } catch (SQLException sqlEx) {
      throw new KmeliaRuntimeException("DefineServiceOfUser.getUserService()",
          SilverpeasRuntimeException.ERROR, "kmelia.SERVICE_NOT_EXIST", sqlEx);
    } finally {
      freeConnection(con);
    }
    return service;
  }

  private Connection getConnection() {
    try {
      Connection con = DBUtil.openConnection();
      return con;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefineServiceOfUser.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("DefineServiceOfUser.getConnection()",
            SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }
}