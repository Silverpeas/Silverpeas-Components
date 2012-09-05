/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
 * WebSiteBmEJB.java
 *
 * Created on 9 Avril 2000, 12:34
 */

package com.stratelia.webactiv.webSites.control.ejb;

/** 
 * This is the WebSite manager EJB-tier controller of the MVC.
 * It is implemented as a session EJB. It controls all the activities 
 * that happen in a client session.
 * It also provides mechanisms to access other session EJBs.
 * @author Cecile BONIN
 * @version 1.0
 */

import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.DBUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.webSites.WebSitesContentManager;
import com.stratelia.webactiv.webSites.siteManage.dao.SiteDAO;
import com.stratelia.webactiv.webSites.siteManage.model.FolderDetail;
import com.stratelia.webactiv.webSites.siteManage.model.IconDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SiteDetail;
import com.stratelia.webactiv.webSites.siteManage.model.SitePK;
import com.stratelia.webactiv.webSites.siteManage.model.WebSitesRuntimeException;

/**
 * Session Bean implementation for WebSiteBm EJB.
 */

public class WebSiteBmEJB implements SessionBean {

  /**
   * 
   */
  private static final long serialVersionUID = 7063837019701682671L;

  /*-------------- Attributs ------------------*/
  private String prefixTableName; /* nom du prefix table Name : WA3 */
  private String componentId = null; /* id du composant : bookmark32 */

  private FolderDetail currentFolder;
  private UserDetail currentUser;
  private NodeBm currentNodeBm = null;
  private PublicationBm currentPublicationBm = null;

  /** use for the PDC utilization */
  private WebSitesContentManager webSitesContentManager = null;

  /*-------------- Methodes de l'interface SessionBean ------------------*/
  /**
   * WebSiteBmEJB
   */
  public WebSiteBmEJB() {
  }

  /**
   * ejbCreate
   */
  public void ejbCreate() {
  }

  /**
   * setSessionContext
   */
  public void setSessionContext(SessionContext sc) {
  }

  /**
   * ejbRemove
   */
  public void ejbRemove() {
  }

  /**
   * ejbActivate
   */
  public void ejbActivate() {
  }

  /**
   * ejbPassivate
   */
  public void ejbPassivate() {
  }

  /*-------------- Methodes metiers ----------------------------------------------------------*/

  /**
   * setComponentId
   */
  public void setComponentId(String componentId) {
    this.componentId = componentId;
    SilverTrace.info("webSites", "WebSiteBmEJB.setComponentId()",
        "root.MSG_GEN_PARAM_VALUE", "componentId= " + componentId);
  }

  /**
   * setPrefixTableName
   */
  public void setPrefixTableName(String prefixTableName) {
    this.prefixTableName = prefixTableName;

    SilverTrace.info("webSites", "WebSiteBmEJB.setPrefixTableName()",
        "root.MSG_GEN_PARAM_VALUE", "prefixTableName= " + prefixTableName);
  }

  public void setSpaceName(String space) {

  }

  public void setActor(UserDetail user) {
    this.currentUser = user;
  }

  /**
   * getNodeBm
   */
  public NodeBm getNodeBm() {
    if (currentNodeBm == null) {
      try {
        NodeBmHome nodeBmHome = (NodeBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
        currentNodeBm = nodeBmHome.create();
      } catch (Exception re) {
        throw new WebSitesRuntimeException("WebSiteBmEJB.getNodeBm()",
            SilverpeasRuntimeException.ERROR,
            "webSites.EX_NODEBM_CREATE_FAILED", re);
      }
    }
    return currentNodeBm;
  }

  /**
   * getPublicationBm
   */
  public PublicationBm getPublicationBm() {
    if (currentPublicationBm == null) {
      try {
        PublicationBmHome publicationBmHome = (PublicationBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
                PublicationBmHome.class);
        currentPublicationBm = publicationBmHome.create();
      } catch (Exception re) {
        throw new WebSitesRuntimeException("WebSiteBmEJB.getPublicationBm()",
            SilverpeasRuntimeException.ERROR,
            "webSites.EX_PUBLICATIONBM_CREATE_FAILED", re);
      }
    }
    return currentPublicationBm;
  }

  /*            ** Gestion des themes ** */

  /**
   * @param
   * @param
   * @return
   * @see
   * @since 1.0
   */
  public FolderDetail goTo(String id) {

    Collection<NodeDetail> newPath = new ArrayList<NodeDetail>();
    Collection<PublicationDetail> pubDetails = null;
    NodeDetail nodeDetail = null;
    Collection<NodeDetail> childrenPKs = null;
    int nbPub = 0;

    NodeBm nodeBm = getNodeBm();
    PublicationBm pubBm = getPublicationBm();

    NodePK pk = new NodePK(id, this.prefixTableName, this.componentId);

    // get the basic information (Header) of this folder
    try {
      nodeDetail = nodeBm.getDetail(pk);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.goTo()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_NODEBM_DETAIL_FAILED",
          " pk = " + pk.toString(), re);
    }

    // get the publications associated to this topic
    try {
      // get the publication details linked to this topic
      pubDetails = pubBm.getDetailsByFatherPK(nodeDetail.getNodePK());
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.goTo()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLICATIONBM_DETAIL_FAILED", " pk = " + pk.toString(),
          re);
    }

    // get the path to this topic
    if (currentFolder == null) {
      // currentTopic is undefined
      if ("0".equals(nodeDetail.getNodePK().getId())) {
        newPath.add(nodeDetail);
      } else {
        newPath = getPathFromAToZ(nodeDetail);
      }
    } else {
      // calculate the new path
      newPath = getNewPath(nodeDetail);
    }

    // Get the publication number associated to each subTopics
    // First, get the childrenPKs of current topic
    childrenPKs = nodeDetail.getChildrenDetails();

    List<Integer> nbPubByTopic = new ArrayList<Integer>();
    Iterator<NodeDetail> iterator = childrenPKs.iterator();
    // For each child, get the publication number associated to it
    while (iterator.hasNext()) {
      NodeDetail child = iterator.next();
      NodePK childPK = child.getNodePK();
      String childPath = child.getPath();
      try {
        // get the total number of publication associated to this descendant
        // topics
        nbPub = pubBm.getNbPubByFatherPath(childPK, childPath);
      } catch (Exception re) {
        throw new WebSitesRuntimeException("WebSiteBmEJB.goTo()",
            SilverpeasRuntimeException.ERROR,
            "webSites.EX_GET_NB_PUBLICATIONS_FAILED", re);
      }
      // add this total to the collection
      nbPubByTopic.add(Integer.valueOf(nbPub));
    }
    // set the currentTopic and return it
    this.currentFolder = new FolderDetail(newPath, nodeDetail, pubDetails,
        nbPubByTopic);
    return this.currentFolder;
  }

  /**
   * @param nd
   * @return
   */
  private Collection<NodeDetail> getPathFromAToZ(NodeDetail nd) {
    Collection<NodeDetail> newPath = new ArrayList<NodeDetail>();
    NodeBm nodeBm = getNodeBm();
    try {
      List<NodeDetail> pathInReverse = (List<NodeDetail>) nodeBm.getPath(nd.getNodePK());
      // reverse the path from root to leaf
      for (int i = pathInReverse.size() - 1; i >= 0; i--) {
        newPath.add(pathInReverse.get(i));
      }
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getPathFromAToZ()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_NODE_GETPATH_FAILED",
          " pk = " + nd.getNodePK().toString(), re);
    }
    return newPath;
  }

  /**
   * Return a NodeDetail Collection that represents the path from root to leaf
   * @param nd the NodeDetail of the leaf topic
   * @return a NodeDetail Collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @exception javax.ejb.FinderException
   * @exception javax.ejb.CreateException
   * @exception java.sql.SQLException
   * @since 1.0
   */
  private Collection<NodeDetail> getNewPath(NodeDetail nd) {

    NodeDetail n;
    Collection<NodeDetail> currentPath = currentFolder.getPath();
    Collection<NodeDetail> newPath = new ArrayList<NodeDetail>();
    Iterator<NodeDetail> iterator = currentPath.iterator();
    boolean find = false;

    // find = true if nd is in the path of the currentTopic
    while (iterator.hasNext() && !(find)) {
      n = iterator.next();
      if (n.getNodePK().getId().equals(nd.getNodePK().getId())) {
        find = true;
      }
    }
    if (find) {
      // cut the end of the current path collection from nodeDetail
      newPath = cutPath(currentPath, nd);
    } else {
      // si nodeDetail.getFatherPK.getId == id du 1er elem de currentPath
      if (nd.getFatherPK().getId().equals(
          currentFolder.getNodeDetail().getNodePK().getId())) {
        // this topic is a child of the current topic
        // add this topic to the end of path
        currentPath.add(nd);
        newPath = currentPath;
      } else {
        // compute path from a to z
        NodeBm nodeBm = getNodeBm();
        try {
          List<NodeDetail> pathInReverse = (List<NodeDetail>) nodeBm.getPath(nd.getNodePK());
          // reverse the path from root to leaf
          for (int i = pathInReverse.size() - 1; i >= 0; i--) {
            newPath.add(pathInReverse.get(i));
          }
        } catch (Exception re) {
          throw new WebSitesRuntimeException("WebSiteBmEJB.getNewPath()",
              SilverpeasRuntimeException.ERROR,
              "webSites.EX_NODE_GET_NEW_PATH_FAILED", " pk = "
                  + nd.getNodePK().toString(), re);
        }
      }
    }
    return newPath;
  }

  /**
   * Return a NodeDetail Collection that represents a sub path to nd of the path
   * @param currentPath a NodeDetail Collection that represents a path
   * @param nd the NodeDetail of the leaf topic
   * @return a NodeDetail Collection
   * @see com.stratelia.webactiv.util.node.model.NodeDetail
   * @since 1.0
   */
  private Collection<NodeDetail> cutPath(Collection<NodeDetail> currentPath, NodeDetail nd) {
    NodeDetail n;
    Iterator<NodeDetail> iterator = currentPath.iterator();
    boolean find = false;
    Collection<NodeDetail> resultPath = new ArrayList<NodeDetail>();

    while (iterator.hasNext() && !(find)) {
      n = iterator.next();
      resultPath.add(n);
      if (n.getNodePK().getId().equals(nd.getNodePK().getId())) {
        find = true;
      }
    }

    if (find) {
      return resultPath;
    } else {
      return null;
    }
  }

  /**
   * Utilise dans addFolder
   * @param
   * @param
   * @return
   * @see
   * @since 1.0
   */
  public NodePK addToFolder(String fatherId, NodeDetail subTopic) {
    SilverTrace.info("webSites", "WebSiteBmEJB.addToFolder()",
        "root.MSG_GEN_ENTER_METHOD");
    NodePK theNodePK = null;
    NodeBm nodeBm = getNodeBm();
    try {
      theNodePK = nodeBm.createNode(subTopic, currentFolder.getNodeDetail());
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.addToFolder()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_NODE_CREATE_FAILED",
          re);
    }
    return theNodePK;
  }

  /**
   * @param
   * @param
   * @return
   * @see
   * @since 1.0
   */
  public NodePK addFolder(NodeDetail subFolder, String alertType) {
    NodePK pk = null;
    if (subFolder == null) {
      SilverTrace.info("webSites", "WebSiteBmEJB.addFolder()",
          "root.MSG_GEN_PARAM_VALUE", "subFolder = null");
    }
    // add current space and component to subTopic detail
    subFolder.getNodePK().setSpace(this.prefixTableName);
    subFolder.getNodePK().setComponentName(this.componentId);

    // Construction de la date de creation (date courante)
    String creationDate = DateUtil.today2SQLDate();
    subFolder.setCreationDate(creationDate);
    subFolder.setCreatorId(currentUser.getId());

    if (currentFolder == null) {
      SilverTrace.info("webSites", "WebSiteBmEJB.addFolder()",
          "root.MSG_GEN_PARAM_VALUE", "currentFolder = null");
    }
    // add new topic to current topic
    pk = addToFolder(currentFolder.getNodePK().getId(), subFolder);
    SilverTrace.info("webSites", "WebSiteBmEJB.addFolder()",
        "root.MSG_GEN_EXIT_METHOD");

    return pk;
  }

  /**
   * @param topic
   * @param alertType
   * @return a NodePK
   */
  public NodePK updateFolder(NodeDetail topic, String alertType) {
    NodePK fatherPK = currentFolder.getNodePK();
    topic.setFatherPK(fatherPK);
    topic.getNodePK().setSpace(this.prefixTableName);
    topic.getNodePK().setComponentName(this.componentId);
    topic.setLevel(currentFolder.getNodeDetail().getLevel());

    // Set information to topic detail
    topic.getNodePK().setSpace(this.prefixTableName);
    topic.getNodePK().setComponentName(this.componentId);
    NodeBm nodeBm = getNodeBm();
    try {
      nodeBm.setDetail(topic);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updateFolder()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_NODE_UPDATE_FAILED",
          "topic = " + topic.toString(), re);
    }
    return topic.getNodePK();
  }

  /**
   * @param subTopicId
   * @return a NodeDetail
   */
  public NodeDetail getFolderDetail(String subTopicId) {
    Collection<NodeDetail> subTopics = currentFolder.getNodeDetail().getChildrenDetails();
    Iterator<NodeDetail> iterator = subTopics.iterator();
    NodeDetail subTopic = null;
    while (iterator.hasNext()) {
      subTopic = iterator.next();
      if (subTopic.getNodePK().getId().equals(subTopicId)) {
        return subTopic;
      }
    }
    NodeBm nodeBm = getNodeBm();
    NodePK pk = new NodePK(subTopicId, this.prefixTableName, this.componentId);
    // get the basic information (Header) of this topic
    try {
      subTopic = nodeBm.getDetail(pk);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getFolderDetail()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_GET_NODE_DETAIL_FAILED", "pk = " + pk.toString(), re);
    }
    return subTopic;
  }

  /**
   * @param topicId the topic identifier to delete
   */
  public void deleteFolder(String topicId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.deleteFolder()",
        "root.MSG_GEN_ENTER_METHOD");
    NodePK pkToDelete = new NodePK(topicId, this.prefixTableName,
        this.componentId);

    PublicationBm pubBm = getPublicationBm();
    NodeBm nodeBm = getNodeBm();

    try {
      // get all nodes which will be deleted
      Collection<NodePK> nodesToDelete = nodeBm.getDescendantPKs(pkToDelete);
      nodesToDelete.add(pkToDelete);

      Iterator<PublicationPK> itPub = null;
      Collection<PublicationPK> pubsToCheck = null; // contains all PubPKs concerned by the
      // delete
      NodePK oneNodeToDelete = null; // current node to delete
      // a given publication
      PublicationPK onePubToCheck = null; // current pub to check
      Iterator<NodePK> itNode = nodesToDelete.iterator();
      while (itNode.hasNext()) {
        oneNodeToDelete = itNode.next();
        // get pubs linked to current node
        pubsToCheck = pubBm.getPubPKsInFatherPK(oneNodeToDelete);
        itPub = pubsToCheck.iterator();
        // check each pub contained in current node
        while (itPub.hasNext()) {
          onePubToCheck = itPub.next();
          pubBm.removeFather(onePubToCheck, oneNodeToDelete);

        }
      }

      // Delete the topic
      nodeBm.removeNode(pkToDelete);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.deleteFolder()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_NODE_DELETE_FAILED",
          "pk = " + pkToDelete.toString(), re);
    }

  }

  /**
   * @param nodeId
   * @param nodes
   * @return
   */
  private int getIndexOfNode(String nodeId, List<NodeDetail> nodes) {
    SilverTrace.debug("webSites", "WebSiteBmEJB.getIndexOfNode()",
        "root.MSG_GEN_ENTER_METHOD", "nodeId = " + nodeId);
    NodeDetail node = null;
    int index = 0;
    if (nodes != null) {
      for (int i = 0; i < nodes.size(); i++) {
        node = nodes.get(i);
        if (nodeId.equals(node.getNodePK().getId())) {
          SilverTrace.debug("webSites", "WebSiteBmEJB.getIndexOfNode()",
              "root.MSG_GEN_EXIT_METHOD", "index = " + index);
          return index;
        }
        index++;
      }
    }
    SilverTrace.debug("webSites", "WebSiteBmEJB.getIndexOfNode()",
        "root.MSG_GEN_EXIT_METHOD", "index = " + index);
    return index;
  }

  /**
   * @param way
   * @param topicPK
   * @param fatherPK
   */
  public void changeTopicsOrder(String way, NodePK topicPK, NodePK fatherPK) {
    SilverTrace.info("webSites", "WebSiteBmEJB.changeTopicsOrder()",
        "root.MSG_GEN_ENTER_METHOD", "way = " + way + ", topicPK = "
            + topicPK.toString());

    List<NodeDetail> subTopics = null;
    try {
      subTopics = (List<NodeDetail>) getNodeBm().getChildrenDetails(fatherPK);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.changeTopicsOrder()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_GET_NODE_DETAIL_FAILED", e);
    }

    if (subTopics != null && subTopics.size() > 0) {
      // search the place of the topic we want to move
      int indexOfTopic = getIndexOfNode(topicPK.getId(), subTopics);

      // get the node to move
      NodeDetail node2move = (NodeDetail) subTopics.get(indexOfTopic);

      // remove the node to move
      subTopics.remove(indexOfTopic);

      if (way.equals("up")) {
        subTopics.add(indexOfTopic - 1, node2move);
      } else {
        subTopics.add(indexOfTopic + 1, node2move);
      }

      NodeDetail nodeDetail = null;
      // for each node, change the order and store it
      for (int i = 0; i < subTopics.size(); i++) {
        nodeDetail = (NodeDetail) subTopics.get(i);

        SilverTrace.info("webSites", "WebSiteBmEJB.changeTopicsOrder()",
            "root.MSG_GEN_PARAM_VALUE", "updating Node : nodeId = "
                + nodeDetail.getNodePK().getId() + ", order = " + i);
        try {
          nodeDetail.setOrder(i);
          getNodeBm().setDetail(nodeDetail);
        } catch (Exception e) {
          throw new WebSitesRuntimeException(
              "WebSiteBmEJB.changeTopicsOrder()",
              SilverpeasRuntimeException.ERROR,
              "webSites.EX_NODE_UPDATE_FAILED", e);
        }
      }
    }
  }

  /******************** Gestion des publications */

  /**
   * @param
   * @param
   * @return
   * @see
   * @since 1.0
   */
  public PublicationDetail getPublicationDetail(String pubId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getPublicationDetail()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationBm pubBm = getPublicationBm();
    try {
      PublicationPK pk = new PublicationPK(pubId, this.prefixTableName,
          this.componentId);
      return pubBm.getDetail(pk);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getPublicationDetail()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_PUBLICATION_DETAIL_FAILED",
          "pubId = " + pubId, re);
    }
  }

  /**
   * @param
   * @param
   * @return
   * @see
   * @since 1.0
   */
  public String createPublication(PublicationDetail pubDetail) {
    SilverTrace.info("webSites", "WebSiteBmEJB.createPublication()",
        "root.MSG_GEN_PARAM_VALUE", "pubDetail = " + pubDetail.toString());
    PublicationPK pubPK = null;
    pubDetail.getPK().setSpace(this.prefixTableName);
    pubDetail.getPK().setComponentName(this.componentId);
    PublicationBm pubBm = getPublicationBm();
    try {
      // create the publication
      pubPK = pubBm.createPublication(pubDetail);
      pubDetail.getPK().setId(pubPK.getId());
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.createPublication()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLICATION_CREATE_FAILED", "pubDetail = "
              + pubDetail.toString(), re);
    }
    return pubPK.getId();
  }

  /**
   * @param
   * @param
   * @return
   * @see
   * @since 1.0
   */
  // CBO : ADD
  public void updatePublication(PublicationDetail pubDetail) {
    SilverTrace.info("webSites", "WebSiteBmEJB.updatePublication()",
        "root.MSG_GEN_ENTER_METHOD");
    pubDetail.getPK().setSpace(this.prefixTableName);
    pubDetail.getPK().setComponentName(this.componentId);
    PublicationBm pubBm = getPublicationBm();
    try {
      pubBm.setDetail(pubDetail);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updatePublication()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLICATION_UPDATE_FAILED", "pubDetail = "
              + pubDetail.toString(), re);
    }
  }

  /**
   * @param
   * @param
   * @return
   * @see
   * @since 1.0
   */
  public void deletePublication(String pubId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.deletePublication()",
        "root.MSG_GEN_PARAM_VALUE", "pubId = " + pubId);
    PublicationPK pubPK = new PublicationPK(pubId, this.prefixTableName,
        this.componentId);
    PublicationBm pubBm = getPublicationBm();
    try {
      pubBm.removeAllFather(pubPK);
      // delete the publication
      pubBm.removePublication(pubPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.deletePublication()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLICATION_DELETE_FAILED", "pubPK = "
              + pubPK.toString(), re);
    }
  }

  /**
   * Utilise dans createPublication
   * @param
   * @param
   * @return
   * @see
   * @since 1.0
   */
  // private void addPublicationToTopic(String pubId, String fatherId) {
  public void addPublicationToTopic(String pubId, String fatherId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.addPublicationToTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = new PublicationPK(pubId, this.prefixTableName,
        this.componentId);
    NodePK fatherPK = new NodePK(fatherId, this.prefixTableName,
        this.componentId);

    // add publication to topic
    PublicationBm pubBm = getPublicationBm();
    try {
      pubBm.addFather(pubPK, fatherPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(
          "WebSiteBmEJB.addPublicationToTopic()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLICATION_ADD_TO_NODE_FAILED", "pubPK = "
              + pubPK.toString() + " - fatherPK = " + fatherPK.toString(), re);
    }
  }

  public void removePublicationToTopic(String pubId, String fatherId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.removePublicationToTopic()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = new PublicationPK(pubId, this.prefixTableName,
        this.componentId);
    NodePK fatherPK = new NodePK(fatherId, this.prefixTableName,
        this.componentId);

    // remove publication from topic
    PublicationBm pubBm = getPublicationBm();
    try {
      pubBm.removeFather(pubPK, fatherPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException(
          "WebSiteBmEJB.removePublicationToTopic()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLICATION_DELETE_TO_NODE_FAILED", "pubPK = "
              + pubPK.toString() + " - fatherPK = " + fatherPK.toString(), re);
    }
  }

  /**
   * @param pubId
   * @return
   */
  public Collection<NodePK> getAllFatherPK(String pubId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getAllFatherPK()",
        "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = new PublicationPK(pubId, this.prefixTableName,
        this.componentId);
    PublicationBm pubBm = getPublicationBm();
    Collection<NodePK> listFatherPK = null;
    try {
      listFatherPK = pubBm.getAllFatherPK(pubPK);
    } catch (Exception re) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getAllFatherPK()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_PUBLICATION_FATHER_FAILED",
          "pubId = " + pubId, re);
    }
    return listFatherPK;
  }

  /**
   * getIdPublication
   */
  public String getIdPublication(String idSite) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getIdPublication()", "root.MSG_GEN_ENTER_METHOD");
    String idPub = null;
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      idPub = dao.getIdPublication(idSite);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getIdPublication()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_GET_PUBLICATION_FAILED", "idSite = " + idSite, e);
    }
    return idPub;
  }

  public void updateClassification(String pubId, ArrayList<String> arrayTopic) {
    SilverTrace
        .info("webSites", "WebSiteBmEJB.updateClassification()", "root.MSG_GEN_ENTER_METHOD");
    PublicationPK pubPK = new PublicationPK(pubId, this.prefixTableName, this.componentId);
    PublicationBm pubBm = getPublicationBm();
    Collection<NodePK> oldFathersColl;
    try {
      oldFathersColl = (Collection<NodePK>) pubBm.getAllFatherPK(pubPK);
    } catch (RemoteException e1) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updateClassification()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_GET_PUBLICATION_FATHER_FAILED", "pubPK = "
              + pubPK.toString(), e1);
    }

    List<NodePK> oldFathers = new ArrayList<NodePK>(); // List of NodePK
    List<NodePK> newFathers = new ArrayList<NodePK>(); // List of NodePK
    Collection<String> remFathers = new ArrayList<String>(); // Collection of idNode

    // Compute the remove list
    NodePK nodePK = null;
    Iterator<NodePK> it = oldFathersColl.iterator();
    while (it.hasNext()) {
      nodePK = it.next();
      if (arrayTopic.indexOf(nodePK.getId()) == -1) {
        remFathers.add(nodePK.getId());
      }
      oldFathers.add(nodePK);
    }

    // Compute the add and stay list
    String idNode = null;
    for (int nI = 0; nI < arrayTopic.size(); nI++) {
      idNode = arrayTopic.get(nI);
      nodePK = new NodePK(idNode, this.prefixTableName, this.componentId);
      if (oldFathers.indexOf(nodePK) == -1) {
        newFathers.add(nodePK);
      }
    }

    try {
      it = newFathers.iterator();
      while (it.hasNext()) {
        nodePK = (NodePK) it.next();
        pubBm.addFather(pubPK, nodePK);
      }
    } catch (RemoteException e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updateClassification()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLICATION_ADD_TO_NODE_FAILED", "pubPK = "
              + pubPK.toString() + " - nodePK = " + nodePK.toString(), e);
    }

    try {
      pubBm.removeFathers(pubPK, remFathers);
    } catch (RemoteException e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updateClassification()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_DEPUBLISH_FAILED",
          "pubPK = " + pubPK.toString() + " - nodePK = " + nodePK.toString(), e);
    }
  }

  /**
   * @param pubId
   * @param nodePK
   * @param direction
   */
  public void changePubsOrder(String pubId, NodePK nodePK, int direction) {
    SilverTrace.info("webSites", "WebSiteBmEJB.changePubsOrder()",
        "root.MSG_GEN_ENTER_METHOD", "pubId = " + pubId + ", nodePK = "
            + nodePK.toString() + ", direction = " + direction);

    PublicationPK pubPK = new PublicationPK(pubId, this.prefixTableName, this.componentId);

    PublicationBm pubBm = getPublicationBm();
    try {
      pubBm.changePublicationOrder(pubPK, nodePK, direction);
    } catch (RemoteException e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.changePubsOrder()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_PUBLICATION_UPDATE_FAILED", "pubPK = "
              + pubPK.toString() + " - nodePK = " + nodePK.toString(), e);
    }
  }

  /***********************************************************************************************/
  // Gestion des sites

  /**
   * getAllWebSite
   */
  public Collection<SiteDetail> getAllWebSite() {
    SilverTrace.info("webSites", "WebSiteBmEJB.getAllWebSite()",
        "root.MSG_GEN_ENTER_METHOD");
    Collection<SiteDetail> theSiteList = null;
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      theSiteList = dao.getAllWebSite();
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getAllWebSite()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_WEBSITES_FAILED", e);
    }
    return theSiteList;
  }

  /**
   * getWebSite
   * @param id
   * @return
   */
  public SiteDetail getWebSite(String id) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getWebSite()",
        "root.MSG_GEN_ENTER_METHOD");
    SitePK pk = new SitePK(id, prefixTableName, componentId);
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      return dao.getWebSite(pk);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getWebSite()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_WEBSITE_FAILED",
          "id = " + id, e);
    }
  }

  /**
   * @param ids
   * @return
   */
  public List<SiteDetail> getWebSites(List<String> ids) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getWebSites()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      return dao.getWebSites(ids);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getWebSite()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_WEBSITE_FAILED",
          "ids = " + ids, e);
    }
  }

  /**
   * getIcons
   */
  public Collection<IconDetail> getIcons(String id) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getIcons()",
        "root.MSG_GEN_ENTER_METHOD");
    SitePK pk = new SitePK(id, prefixTableName, componentId);
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      return dao.getIcons(pk);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getIcons()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_ICONS_FAILED",
          "id = " + id, e);
    }
  }

  /**
   * getNextId
   */
  public String getNextId() {
    SilverTrace.info("webSites", "WebSiteBmEJB.getNextId()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      return dao.getNextId();
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getNextId()",
          SilverpeasRuntimeException.ERROR, "root.EX_GET_NEXTID_FAILED", e);
    }
  }

  /**
   * getAllIcons
   */
  public Collection<IconDetail> getAllIcons() {
    SilverTrace.info("webSites", "WebSiteBmEJB.getAllIcons()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      return dao.getAllIcons();
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getAllIcons()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_GET_ALL_ICONS_FAILED",
          e);
    }
  }

  /**
   * createWebSite
   */
  public String createWebSite(SiteDetail description) {
    SilverTrace.info("webSites", "WebSiteBmEJB.createWebSite()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      dao.createWebSite(description);

      String pubPk = createPublication(description);

      // register the new publication as a new content to content manager
      con = getConnection(); // connection usefull for content service
      createSilverContent(con, description, currentUser.getId(), prefixTableName, componentId);

      return pubPk;
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.createWebSite()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_CREATE_WEBSITE_FAILED", " SiteDetail = "
              + description.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * associateIcons
   */
  public void associateIcons(String id, Collection<String> liste) {
    SilverTrace.info("webSites", "WebSiteBmEJB.associateIcons()",
        "root.MSG_GEN_ENTER_METHOD");
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      dao.associateIcons(id, liste);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.associateIcons()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_ASSOCIATE_ICONS_FAILED", " id = " + id, e);
    }
  }

  /**
   * publish
   */
  public void publish(Collection<String> liste) {
    SilverTrace.info("webSites", "WebSiteBmEJB.publish()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      dao.publish(liste);

      // register the new publication as a new content to content manager
      con = getConnection(); // connection usefull for content service
      Iterator<String> i = liste.iterator();
      while (i.hasNext()) {
        String siteId = i.next();
        SiteDetail siteDetail = getWebSite(siteId);
        updateSilverContentVisibility(siteDetail, prefixTableName, componentId);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.publish()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_PUBLISH_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * dePublish
   */
  public void dePublish(Collection<String> liste) {
    SilverTrace.info("webSites", "WebSiteBmEJB.dePublish()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      dao.dePublish(liste);

      // register the new publication as a new content to content manager
      con = getConnection(); // connection usefull for content service
      Iterator<String> i = liste.iterator();
      while (i.hasNext()) {
        String siteId = i.next();
        SiteDetail siteDetail = getWebSite(siteId);
        updateSilverContentVisibility(siteDetail, prefixTableName, componentId);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.dePublish()",
          SilverpeasRuntimeException.ERROR, "webSites.EX_DEPUBLISH_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  /**
   * deleteWebSites
   */
  public void deleteWebSites(Collection<String> liste) {
    SilverTrace.info("webSites", "WebSiteBmEJB.deleteWebSites()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      dao.deleteWebSites(liste);

      // register the new publication as a new content to content manager
      con = getConnection(); // connection usefull for content service
      Iterator<String> i = liste.iterator();
      while (i.hasNext()) {
        String siteId = i.next();
        SitePK sitePK = new SitePK(siteId, prefixTableName, componentId);
        SilverTrace.info("webSites", "WebSiteBmEJB.deleteWebSites()",
            "root.MSG_GEN_PARAM_VALUE", "siteId =" + siteId);
        SilverTrace.info("webSites", "WebSiteBmEJB.deleteWebSites()",
            "root.MSG_GEN_PARAM_VALUE", "prefixTableName =" + prefixTableName);
        SilverTrace.info("webSites", "WebSiteBmEJB.deleteWebSites()",
            "root.MSG_GEN_PARAM_VALUE", "componentId =" + componentId);
        deleteSilverContent(con, sitePK, prefixTableName, componentId);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.deleteWebSites()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_DELETE_WEBSITES_FAILED", e);
    } finally {
      freeConnection(con);
    }
  }

  public void index() {
    try {
      // index all topics
      NodePK rootPK = new NodePK("0", "useless", componentId);
      List<NodeDetail> tree = getNodeBm().getSubTree(rootPK);
      Iterator<NodeDetail> itNode = tree.iterator();
      NodeDetail node = null;
      while (itNode.hasNext()) {
        node = (NodeDetail) itNode.next();
        getNodeBm().createIndex(node);
      }

      // index all publications
      PublicationPK pubPK = new PublicationPK("useless", "useless", componentId);
      Collection<PublicationDetail> publications = getPublicationBm().getAllPublications(pubPK);
      Iterator<PublicationDetail> itPub = publications.iterator();
      PublicationDetail pub = null;
      while (itPub.hasNext()) {
        pub = itPub.next();
        getPublicationBm().createIndex(pub);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.index(" + componentId
          + ")", SilverpeasRuntimeException.ERROR,
          "webSites.EX_INDEXING_COMPONENT_FAILED", e);
    }
  }

  /**
   * updateWebSite
   */
  public void updateWebSite(SiteDetail description) {
    SilverTrace.info("webSites", "WebSiteBmEJB.updateWebSite()",
        "root.MSG_GEN_ENTER_METHOD");
    Connection con = null;
    try {
      SiteDAO dao = new SiteDAO(prefixTableName, componentId);
      dao.updateWebSite(description);

    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updateWebSite()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_UPDATE_WEBSITE_FAILED", " SiteDetail = "
              + description.toString(), e);
    } finally {
      freeConnection(con);
    }
  }

  /*****************************************************************************************************************/
  /** ContentManager utilization to use PDC **/
  /*****************************************************************************************************************/

  public int getSilverObjectId(String id) {
    SilverTrace.info("webSites", "WebSiteBmEJB.getSilverObjectId()",
        "root.MSG_GEN_ENTER_METHOD", "id = " + id);
    int silverObjectId = -1;
    SiteDetail siteDetail = null;
    try {
      silverObjectId = getWebSitesContentManager().getSilverObjectId(id,
          componentId);
      if (silverObjectId == -1) {
        siteDetail = getWebSite(id);
        silverObjectId = createSilverContent(null, siteDetail, "-1",
            prefixTableName, componentId);
      }
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  private int createSilverContent(Connection con, SiteDetail siteDetail,
      String creatorId, String prefixTableName, String componentId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.createSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "siteId = "
            + siteDetail.getSitePK().getId());
    try {
      return getWebSitesContentManager().createSilverContent(con, siteDetail,
          "-1", prefixTableName, componentId);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.createSilverContent()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @param con
   * @param sitePK
   * @param prefixTableName
   * @param componentId
   */
  private void deleteSilverContent(Connection con, SitePK sitePK, String prefixTableName,
      String componentId) {
    SilverTrace.info("webSites", "WebSiteBmEJB.deleteSilverContent()",
        "root.MSG_GEN_ENTER_METHOD", "siteId = " + sitePK.getId());
    try {
      getWebSitesContentManager().deleteSilverContent(con, sitePK,
          prefixTableName, componentId);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.deleteSilverContent()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @param siteDetail
   * @param prefixTableName
   * @param componentId
   */
  private void updateSilverContentVisibility(SiteDetail siteDetail,
      String prefixTableName, String componentId) {
    try {
      getWebSitesContentManager().updateSilverContentVisibility(siteDetail,
          prefixTableName, componentId);
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.updateSilverContent()",
          SilverpeasRuntimeException.ERROR,
          "webSites.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @return a "singleton" instance of WebSitesContentManager
   */
  private WebSitesContentManager getWebSitesContentManager() {
    if (webSitesContentManager == null) {
      webSitesContentManager = new WebSitesContentManager();
    }
    return webSitesContentManager;
  }

  /*****************************************************************************************************************/
  /** Connection management methods used for the content service **/
  /*****************************************************************************************************************/

  private Connection getConnection() {
    try {
      Connection con = DBUtil.makeConnection(JNDINames.SILVERPEAS_DATASOURCE);
      return con;
    } catch (Exception e) {
      throw new WebSitesRuntimeException("WebSiteBmEJB.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  /**
   * @param con the connection to close
   */
  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        SilverTrace.error("webSites", "WebSiteBmEJB.freeConnection()",
            "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

}