/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.silverpeas.blog;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.silverpeas.comment.CommentInstanciator;
import com.silverpeas.myLinks.MyLinksInstanciator;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.node.NodeInstanciator;
import com.stratelia.webactiv.publication.PublicationInstanciator;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;

public class BlogInstanciator implements ComponentsInstanciatorIntf {

  public BlogInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("blog", "BlogInstanciator.create()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);

    // create publication component
    PublicationInstanciator pub = new PublicationInstanciator("com.silverpeas.blog");
    pub.create(con, spaceId, componentId, userId);
    // create node component
    NodeInstanciator node = new NodeInstanciator("com.silverpeas.blog");
    node.create(con, spaceId, componentId, userId);
    // inserer les 1er noeuds = racines
    insertRootNodeCategories(con, componentId, userId);
    insertRootNodeArchives(con, componentId, userId);
    SilverTrace.info("blog", "BlogInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info("blog", "BlogInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);
    // delete posts
    PublicationInstanciator pub = new PublicationInstanciator("com.stratelia.webactiv.kmelia");
    pub.delete(con, spaceId, componentId, userId);
    // delete categories
    NodeInstanciator node = new NodeInstanciator("com.stratelia.webactiv.kmelia");
    node.delete(con, spaceId, componentId, userId);
    // delete comments
    CommentInstanciator comment = new CommentInstanciator();
    comment.delete(con, spaceId, componentId, userId);

    // delete links
    MyLinksInstanciator links = new MyLinksInstanciator();
    links.delete(con, spaceId, componentId, userId);
    SilverTrace.info("blog", "BlogInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }

  private void insertRootNodeCategories(Connection con, String componentId, String userId) throws
      InstanciationException {
    String query = null;
    String creationDate = DateUtil.today2SQLDate();
    query = "INSERT INTO SB_Node_Node(nodeId, nodeName, nodeDescription, nodeCreationDate, nodeCreatorId, "
        + "nodePath, nodeLevelNumber, nodeFatherId, modelId, nodeStatus, instanceId)	"
        + "VALUES (0, 'Accueil Catégories', 'Racine Catégories', ? , ? , '/0', 1, -1,'','Visible',?)";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
    } catch (SQLException se) {
      throw new InstanciationException("BlogInstanciator.insertRootNodeCategories()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED", "Query = " + query, se);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private void insertRootNodeArchives(Connection con, String componentId,
      String userId) throws InstanciationException {
    String query = null;
    String creationDate = DateUtil.today2SQLDate();
    query = "INSERT INTO SB_Node_Node(nodeId, nodeName, nodeDescription, nodeCreationDate, nodeCreatorId, "
        + "nodePath, nodeLevelNumber, nodeFatherId, modelId, nodeStatus, instanceId)	"
        + "VALUES (1, 'Accueil Archives', 'Racine Archives', ? , ? , '/0', 1, -1,'','Visible',?)";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
    } catch (SQLException se) {
      throw new InstanciationException("BlogInstanciator.insertRootNodeArchives()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED","Query = " + query, se);
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}
