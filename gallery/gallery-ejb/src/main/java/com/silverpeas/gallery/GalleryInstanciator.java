package com.silverpeas.gallery;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.silverpeas.gallery.control.ejb.GalleryBm;
import com.silverpeas.gallery.control.ejb.GalleryBmHome;
import com.silverpeas.gallery.model.GalleryRuntimeException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.node.NodeInstanciator;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.node.model.NodeRuntimeException;

public class GalleryInstanciator implements ComponentsInstanciatorIntf {

  public GalleryInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("gallery", "GalleryInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
            + componentId + ", userId =" + userId);

    // create node component
    NodeInstanciator node = new NodeInstanciator("com.silverpeas.gallery");
    node.create(con, spaceId, componentId, userId);
    // inserer le 1er noeud = racine
    insertRootNode(con, componentId, userId);
    // inserer le premier album
    insertAlbumNode(con, componentId, userId);

    SilverTrace.info("gallery", "GalleryInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("gallery", "GalleryInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
            + componentId + ", userId =" + userId);

    // supression de tous les albums
    try {
      getGalleryBm().deleteAlbum(new NodePK("0", componentId));
    } catch (RemoteException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    SilverTrace.info("gallery", "GalleryInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void insertRootNode(Connection con, String componentId, String userId)
      throws InstanciationException {
    String query = null;
    String creationDate = DateUtil.today2SQLDate();
    query = "INSERT INTO SB_Node_Node(nodeId, nodeName, nodeDescription, nodeCreationDate, nodeCreatorId, "
        + "nodePath, nodeLevelNumber, nodeFatherId, modelId, nodeStatus, instanceId)	"
        + "VALUES (0, 'Accueil', 'La Racine', ? , ? , '/', 1, -1,'','Visible',?)";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);
      prepStmt.setString(1, creationDate);
      prepStmt.setString(2, userId);
      prepStmt.setString(3, componentId);
      prepStmt.executeUpdate();
    } catch (SQLException se) {
      throw new InstanciationException("GalleryInstanciator.insertRootNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "Query = " + query, se);
    } finally {
      DBUtil.close(prepStmt);
    }
  }

  private void insertAlbumNode(Connection con, String componentId, String userId)
      throws InstanciationException {
    String query = null;
    String creationDate = DateUtil.today2SQLDate();
    query = "INSERT INTO SB_Node_Node(nodeId, nodeName, nodeDescription, nodeCreationDate, nodeCreatorId, "
        + "nodePath, nodeLevelNumber, nodeFatherId, modelId, nodeStatus, instanceId)"
        + "VALUES (?, 'Mon Album',' ', ? , ? , ? , 2, 0, '', 'Invisible',?)";

    PreparedStatement prepStmt = null;
    try {
      prepStmt = con.prepareStatement(query);

      // Recherche de la nouvelle PK de la table
      int newId = DBUtil.getNextId("SB_Node_Node", new String("nodeId"));
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
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "Query = " + query, se);
    } catch (UtilException ue) {
      throw new InstanciationException("GalleryInstanciator.insertAlbumNode()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "Query = " + query, ue);
    }
  }

  private GalleryBm getGalleryBm() {
    GalleryBm galleryBm = null;
    try {
      GalleryBmHome galleryBmHome = (GalleryBmHome) EJBUtilitaire
          .getEJBObjectRef(JNDINames.GALLERYBM_EJBHOME, GalleryBmHome.class);
      galleryBm = galleryBmHome.create();
    } catch (Exception e) {
      throw new GalleryRuntimeException(
          "GallerySessionController.getGalleryBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return galleryBm;
  }

}
