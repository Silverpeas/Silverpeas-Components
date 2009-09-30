package com.silverpeas.blog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.silverpeas.comment.CommentInstanciator;
import com.silverpeas.myLinks.MyLinksInstanciator;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.node.NodeInstanciator;
import com.stratelia.webactiv.publication.PublicationInstanciator;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;

public class BlogInstanciator implements ComponentsInstanciatorIntf {

  public BlogInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("blog", "BlogInstanciator.create()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
            + componentId + ", userId =" + userId);

    // create publication component
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.silverpeas.blog");
    pub.create(con, spaceId, componentId, userId);

    // create node component
    NodeInstanciator node = new NodeInstanciator("com.silverpeas.blog");
    node.create(con, spaceId, componentId, userId);

    // inserer les 1er noeuds = racines
    insertRootNodeCategories(con, componentId, userId);
    insertRootNodeArchives(con, componentId, userId);

    SilverTrace.info("blog", "BlogInstanciator.create()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("blog", "BlogInstanciator.delete()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + spaceId + ", componentId = "
            + componentId + ", userId =" + userId);

    // delete posts
    PublicationInstanciator pub = new PublicationInstanciator(
        "com.stratelia.webactiv.kmelia");
    pub.delete(con, spaceId, componentId, userId);

    // delete categories
    NodeInstanciator node = new NodeInstanciator(
        "com.stratelia.webactiv.kmelia");
    node.delete(con, spaceId, componentId, userId);

    // delete comments
    CommentInstanciator comment = new CommentInstanciator();
    comment.delete(con, spaceId, componentId, userId);

    // delete links
    MyLinksInstanciator links = new MyLinksInstanciator();
    links.delete(con, spaceId, componentId, userId);

    SilverTrace.info("blog", "BlogInstanciator.delete()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  private void insertRootNodeCategories(Connection con, String componentId,
      String userId) throws InstanciationException {
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
      throw new InstanciationException(
          "BlogInstanciator.insertRootNodeCategories()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "Query = " + query, se);
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
      throw new InstanciationException(
          "BlogInstanciator.insertRootNodeArchives()",
          InstanciationException.ERROR, "root.EX_RECORD_INSERTION_FAILED",
          "Query = " + query, se);
    } finally {
      DBUtil.close(prepStmt);
    }
  }
}
