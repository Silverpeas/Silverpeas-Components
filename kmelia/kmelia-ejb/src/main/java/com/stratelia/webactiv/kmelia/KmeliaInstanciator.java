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
 * FLOSS exception.  You should have received a copy of the text describing
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

/*
 * KmeliaInstanciator.java
 *
 * Created on 13 juillet 2000, 09:54
 */

package com.stratelia.webactiv.kmelia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.silverpeas.versioning.VersioningInstanciator;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SQLRequest;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.calendar.backbone.TodoBackboneAccess;
import com.stratelia.webactiv.node.NodeInstanciator;
import com.stratelia.webactiv.publication.PublicationInstanciator;
import com.stratelia.webactiv.util.DateUtil;

/**
 * @author nesseric
 */
public class KmeliaInstanciator extends SQLRequest implements ComponentsInstanciatorIntf {

  /** Creates new KmeliaInstanciator */
  public KmeliaInstanciator() {
    super("com.stratelia.webactiv.kmelia");
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("kmelia", "KmeliaInstanciator.create()",
        "root.MSG_GEN_PARAM_VALUE", "Space = " + spaceId);

    // create publication component
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.stratelia.webactiv.kmelia");
    pub.create(con, spaceId, componentId, userId);

    // create node component
    NodeInstanciator node = new NodeInstanciator(
        "com.stratelia.webactiv.kmelia");
    node.create(con, spaceId, componentId, userId);

    setInsertQueries();
    insertSpecialNode(con, componentId, userId);

  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("kmelia", "KmeliaInstanciator.delete()",
        "root.MSG_GEN_PARAM_VALUE", "Space = " + spaceId);

    // delete publication component
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.stratelia.webactiv.kmelia");
    pub.delete(con, spaceId, componentId, userId);

    TodoBackboneAccess todoBBA = new TodoBackboneAccess();
    todoBBA.removeEntriesByInstanceId(componentId);

    // delete node component
    NodeInstanciator node = new NodeInstanciator(
        "com.stratelia.webactiv.kmelia");
    node.delete(con, spaceId, componentId, userId);

    // delete versioning infos
    VersioningInstanciator version = new VersioningInstanciator();
    version.delete(con, spaceId, componentId, userId);
  }

  private void insertSpecialNode(Connection con, String componentId,
      String userId) throws InstanciationException {
    // Insert the line corresponding to the Root
    String insertStatement = getInsertQuery(componentId, "Root");

    String creationDate = DateUtil.today2SQLDate();
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
      prepStmt.close();
    } catch (SQLException se) {
      throw new InstanciationException(
          "KmeliaInstanciator.insertSpecialNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "Query = " + insertStatement, se);
    }

    // Insert the line corresponding to the Basket
    insertStatement = getInsertQuery(componentId, "Basket");
    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
      prepStmt.close();
    } catch (SQLException se) {
      throw new InstanciationException(
          "KmeliaInstanciator.insertSpecialNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "INSERT BASKET with query = " + insertStatement, se);
    }

    // Insert the line corresponding to the DZ
    insertStatement = getInsertQuery(componentId, "DZ");

    try {
      prepStmt = con.prepareStatement(insertStatement);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
      prepStmt.close();
    } catch (SQLException se) {
      throw new InstanciationException(
          "KmeliaInstanciator.insertSpecialNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "INSERT DZ with query = " + insertStatement, se);
    }

  }

}