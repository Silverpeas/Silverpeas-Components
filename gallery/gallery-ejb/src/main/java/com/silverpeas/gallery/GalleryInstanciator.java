/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import com.silverpeas.gallery.control.ejb.GalleryBm;

import com.silverpeas.util.FileUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.node.NodeInstanciator;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.node.model.NodePK;

public class GalleryInstanciator implements ComponentsInstanciatorIntf {

  private static final String COMPONENT_NAME = GalleryComponentSettings.COMPONENT_NAME;

  public GalleryInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info(COMPONENT_NAME, "GalleryInstanciator.create()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);
    NodeInstanciator node = new NodeInstanciator("com.silverpeas.gallery");
    node.create(con, spaceId, componentId, userId);
    insertRootNode(con, componentId, userId);
    insertAlbumNode(con, componentId, userId);
    SilverTrace.info(COMPONENT_NAME, "GalleryInstanciator.create()", "root.MSG_GEN_EXIT_METHOD");
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    SilverTrace.info(COMPONENT_NAME, "GalleryInstanciator.delete()", "root.MSG_GEN_ENTER_METHOD",
        "space = " + spaceId + ", componentId = " + componentId + ", userId =" + userId);
    getGalleryBm().deleteAlbum(UserDetail.getById(userId), componentId,
        new NodePK(NodePK.ROOT_NODE_ID, componentId));
    FileUtil.deleteEmptyDir(new File(FileRepositoryManager.getAbsolutePath(componentId)));
    SilverTrace.info(COMPONENT_NAME, "GalleryInstanciator.delete()", "root.MSG_GEN_EXIT_METHOD");
  }

  private void insertRootNode(Connection con, String componentId, String userId) throws
      InstanciationException {
    String creationDate = DateUtil.today2SQLDate();
    String query =
        "INSERT INTO SB_Node_Node(nodeId, nodeName, nodeDescription, nodeCreationDate, "
            + "nodeCreatorId, nodePath, nodeLevelNumber, nodeFatherId, modelId, nodeStatus, instanceId)"
            + "	VALUES (0, 'Accueil', 'La Racine', ? , ? , '/', 1, -1,'','Visible',?)";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
    } catch (SQLException se) {
      throw new InstanciationException("GalleryInstanciator.insertRootNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED", "Query = " + query, se);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private void insertAlbumNode(Connection con, String componentId, String userId) throws
      InstanciationException {
    String creationDate = DateUtil.today2SQLDate();
    String query =
        "INSERT INTO SB_Node_Node(nodeId, nodeName, nodeDescription, nodeCreationDate, "
            + "nodeCreatorId, nodePath, nodeLevelNumber, nodeFatherId, modelId, nodeStatus, instanceId)"
            + "VALUES (?, 'Mon Album',' ', ? , ? , ? , 2, 0, '', 'Invisible',?)";
    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      int newId = DBUtil.getNextId(con, "sb_node_node", "nodeId");
      prepStmt.setInt(1, newId);
      prepStmt.setString(2, creationDate);
      prepStmt.setString(3, userId);
      String path = "/0/";
      prepStmt.setString(4, path);
      prepStmt.setString(5, componentId);
      prepStmt.executeUpdate();
      prepStmt.close();
    } catch (SQLException se) {
      throw new InstanciationException("GalleryInstanciator.insertAlbumNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED", "Query = " + query, se);
    }
  }

  private GalleryBm getGalleryBm() {
    return EJBUtilitaire.getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBm.class);
  }
}
