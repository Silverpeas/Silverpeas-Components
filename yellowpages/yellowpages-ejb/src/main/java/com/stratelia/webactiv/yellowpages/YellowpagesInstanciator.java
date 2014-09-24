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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.stratelia.webactiv.yellowpages;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.contact.ContactInstanciator;
import com.stratelia.webactiv.node.NodeInstanciator;
import org.silverpeas.util.DateUtil;

public class YellowpagesInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  /** Creates new YellowpagesInstanciator */
  public YellowpagesInstanciator() {
    super("com.stratelia.webactiv.yellowpages");
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    try {
      SilverTrace.info("yellowpages", "YellowpagesInstanciator.create()",
          "root.MSG_GEN_ENTER_METHOD");
      SilverTrace.info("yellowpages", "YellowpagesInstanciator.create()", "root.MSG_GEN_PARAM_VALUE",
          "spaceId = " + spaceId + " , componentId = " + componentId);

      ContactInstanciator pub = new ContactInstanciator("com.stratelia.webactiv.yellowpages");
      pub.create(con, spaceId, componentId, userId);
      NodeInstanciator node = new NodeInstanciator("com.stratelia.webactiv.yellowpages");
      node.create(con, spaceId, componentId, userId);
      setInsertQueries();
      insertSpecialNode(con, componentId, userId);
    } catch (Exception e) {
      throw new InstanciationException("YellowpagesInstanciator.create()",
          InstanciationException.ERROR, "root.CREATING_DATA_DIRECTORY_FAILED", e);
    }
    SilverTrace.info("yellowpages", "YellowpagesInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("yellowpages", "YellowpagesInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD");
    SilverTrace.info("yellowpages", "YellowpagesInstanciator.delete()", "root.MSG_GEN_PARAM_VALUE",
        "spaceId = " + spaceId + " , componentId = " + componentId);
    ContactInstanciator pub = new ContactInstanciator("com.stratelia.webactiv.yellowpages");
    pub.delete(con, spaceId, componentId, userId);
    NodeInstanciator node = new NodeInstanciator("com.stratelia.webactiv.yellowpages");
    node.delete(con, spaceId, componentId, userId);
    SilverTrace.info("yellowpages", "YellowpagesInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }

  private void insertSpecialNode(Connection con, String componentId, String userId) throws
      InstanciationException {
    String insertQuery = getInsertQuery(componentId, "ACCUEIL");
    String creationDate = DateUtil.date2SQLDate(new Date());
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
      prepStmt.close();
    } catch (SQLException se) {
      throw new InstanciationException("YellowpagesInstanciator.insertSpecialNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "insertQuery = " + insertQuery, se);
    }
    insertQuery = getInsertQuery(componentId, "Basket");
    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
      prepStmt.close();
    } catch (SQLException se) {
      throw new InstanciationException("YellowpagesInstanciator.insertSpecialNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "insertQuery = " + insertQuery, se);
    }
    insertQuery = getInsertQuery(componentId, "DZ");
    try {
      prepStmt = con.prepareStatement(insertQuery);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
      prepStmt.close();
    } catch (SQLException se) {
      throw new InstanciationException("YellowpagesInstanciator.insertSpecialNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "insertQuery = " + insertQuery, se);
    }

  }
}